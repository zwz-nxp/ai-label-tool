import { Component, Inject, OnInit, OnDestroy } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { Subject, forkJoin } from "rxjs";
import { debounceTime, takeUntil } from "rxjs/operators";
import { ModelDetailService } from "app/services/landingai/model-detail.service";
import {
  AdjustThresholdDialogData,
  ConfusionMatrixData,
  PredictionLabel,
  ImageLabel,
  LabelsByImage,
  GenerateModelRequest,
  GenerateModelResponse,
  RecalculatedMetrics,
} from "app/models/landingai/adjust-threshold";
import { MatSnackBar } from "@angular/material/snack-bar";

/**
 * Adjust Threshold Dialog Component
 * 調整 Confidence Threshold 並即時顯示混淆矩陣變化
 * Requirements: 26.1, 26.2, 26.3, 26.4, 26.5, 27.9, 35.1, 35.3
 */
@Component({
  selector: "app-adjust-threshold-dialog",
  standalone: false,
  templateUrl: "./adjust-threshold-dialog.component.html",
  styleUrls: ["./adjust-threshold-dialog.component.scss"],
})
export class AdjustThresholdDialogComponent implements OnInit, OnDestroy {
  /** 當前閾值 */
  currentThreshold: number;

  /** 原始閾值 */
  originalThreshold: number;

  /** 混淆矩陣資料 */
  confusionMatrixData?: ConfusionMatrixData;

  /** 是否正在載入 */
  loading: boolean = true;

  /** 是否正在重新計算 */
  recalculating: boolean = false;

  /** 是否正在產生新模型 */
  generating: boolean = false;

  /** 所有 prediction labels（快取） - 按 evaluation set 分組 */
  private allPredictionLabelsBySet: {
    train: PredictionLabel[];
    dev: PredictionLabel[];
    test: PredictionLabel[];
  } = { train: [], dev: [], test: [] };

  /** 所有 ground truth labels（快取） - 按 evaluation set 分組 */
  private allGroundTruthLabelsBySet: {
    train: ImageLabel[];
    dev: ImageLabel[];
    test: ImageLabel[];
  } = { train: [], dev: [], test: [] };

  /** 閾值變更 Subject（用於 debounce） */
  private thresholdChange$ = new Subject<number>();

  /** 元件銷毀 Subject */
  private destroy$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<AdjustThresholdDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdjustThresholdDialogData,
    private modelDetailService: ModelDetailService,
    private snackBar: MatSnackBar
  ) {
    this.currentThreshold = data.currentThreshold;
    this.originalThreshold = data.currentThreshold;
  }

  ngOnInit(): void {
    // 載入資料
    this.loadData();

    // 設定閾值變更的 debounce (300ms)
    this.thresholdChange$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe((threshold) => {
        this.recalculateConfusionMatrix(threshold);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * 載入所有 evaluation sets 的 prediction labels 和 ground truth labels
   * 因為 threshold 是 model 層級的，調整後需要重新計算所有 evaluation sets 的 metrics
   * Requirements: 26.5, 35.2, 35.3
   */
  private loadData(): void {
    this.loading = true;

    console.log(
      "[Adjust Threshold] Loading data for all evaluation sets, model:",
      this.data.modelId
    );

    // 同時載入所有三個 evaluation sets 的資料
    forkJoin({
      trainPredictions: this.modelDetailService.getPredictionLabels(
        this.data.modelId,
        "train"
      ),
      trainGroundTruth: this.modelDetailService.getGroundTruthLabels(
        this.data.modelId,
        "train"
      ),
      devPredictions: this.modelDetailService.getPredictionLabels(
        this.data.modelId,
        "dev"
      ),
      devGroundTruth: this.modelDetailService.getGroundTruthLabels(
        this.data.modelId,
        "dev"
      ),
      testPredictions: this.modelDetailService.getPredictionLabels(
        this.data.modelId,
        "test"
      ),
      testGroundTruth: this.modelDetailService.getGroundTruthLabels(
        this.data.modelId,
        "test"
      ),
    }).subscribe({
      next: (result) => {
        console.log("[Adjust Threshold] All data loaded successfully");
        console.log(
          "[Adjust Threshold] Train Predictions:",
          result.trainPredictions.length
        );
        console.log(
          "[Adjust Threshold] Train Ground Truth:",
          result.trainGroundTruth.length
        );
        console.log(
          "[Adjust Threshold] Dev Predictions:",
          result.devPredictions.length
        );
        console.log(
          "[Adjust Threshold] Dev Ground Truth:",
          result.devGroundTruth.length
        );
        console.log(
          "[Adjust Threshold] Test Predictions:",
          result.testPredictions.length
        );
        console.log(
          "[Adjust Threshold] Test Ground Truth:",
          result.testGroundTruth.length
        );

        // 儲存所有 evaluation sets 的資料
        this.allPredictionLabelsBySet = {
          train: result.trainPredictions as PredictionLabel[],
          dev: result.devPredictions as PredictionLabel[],
          test: result.testPredictions as PredictionLabel[],
        };

        this.allGroundTruthLabelsBySet = {
          train: result.trainGroundTruth as ImageLabel[],
          dev: result.devGroundTruth as ImageLabel[],
          test: result.testGroundTruth as ImageLabel[],
        };

        this.loading = false;

        // 初始計算混淆矩陣（只顯示當前選擇的 evaluation set）
        this.recalculateConfusionMatrix(this.currentThreshold);
      },
      error: (error) => {
        console.error("[Adjust Threshold] Error loading labels:", error);
        this.loading = false;
        this.snackBar.open("載入資料失敗", "關閉", { duration: 3000 });
      },
    });
  }

  /**
   * 閾值變更處理
   * Requirements: 27.9, 35.1
   */
  onThresholdChange(newThreshold: number): void {
    this.currentThreshold = newThreshold;
    this.thresholdChange$.next(newThreshold);
  }

  /**
   * 重新計算混淆矩陣（只顯示當前選擇的 evaluation set）
   * Requirements: 28.2, 28.3, 28.11, 28.12
   */
  private recalculateConfusionMatrix(threshold: number): void {
    this.recalculating = true;

    console.log(
      "[Adjust Threshold] Recalculating confusion matrix with threshold:",
      threshold
    );
    console.log(
      "[Adjust Threshold] Current evaluation set:",
      this.data.evaluationSet
    );

    try {
      // 取得當前 evaluation set 的資料
      const currentPredictions =
        this.allPredictionLabelsBySet[this.data.evaluationSet];
      const currentGroundTruth =
        this.allGroundTruthLabelsBySet[this.data.evaluationSet];

      console.log(
        "[Adjust Threshold] Current Prediction Labels count:",
        currentPredictions.length
      );
      console.log(
        "[Adjust Threshold] Current Ground Truth Labels count:",
        currentGroundTruth.length
      );

      // 1. 根據閾值過濾 prediction labels
      const filteredPredictions = this.filterPredictionsByThreshold(
        currentPredictions,
        threshold
      );
      console.log(
        "[Adjust Threshold] Filtered Predictions (after threshold):",
        filteredPredictions
      );
      console.log(
        "[Adjust Threshold] Filtered Predictions count:",
        filteredPredictions.length
      );

      // 2. 計算混淆矩陣
      this.confusionMatrixData = this.calculateConfusionMatrix(
        currentGroundTruth,
        filteredPredictions
      );

      console.log(
        "[Adjust Threshold] Confusion Matrix Data:",
        this.confusionMatrixData
      );

      this.recalculating = false;
    } catch (error) {
      console.error(
        "[Adjust Threshold] Error calculating confusion matrix:",
        error
      );
      this.recalculating = false;
      this.snackBar.open("計算混淆矩陣失敗", "關閉", { duration: 3000 });
    }
  }

  /**
   * 根據閾值過濾 prediction labels
   * Requirements: 28.2
   */
  private filterPredictionsByThreshold(
    predictions: PredictionLabel[],
    threshold: number
  ): PredictionLabel[] {
    const thresholdInt = Math.round(threshold * 100);
    return predictions.filter((label) => label.confidenceRate >= thresholdInt);
  }

  /**
   * 計算混淆矩陣
   * Requirements: 28.3, 28.11, 28.12
   */
  private calculateConfusionMatrix(
    groundTruthLabels: ImageLabel[],
    predictionLabels: PredictionLabel[]
  ): ConfusionMatrixData {
    console.log(
      "[Adjust Threshold] calculateConfusionMatrix - Ground Truth count:",
      groundTruthLabels.length
    );
    console.log(
      "[Adjust Threshold] calculateConfusionMatrix - Prediction count:",
      predictionLabels.length
    );

    // 取得所有類別
    const classIds = new Set<number>();
    const classNames = new Map<number, string>();

    groundTruthLabels.forEach((label) => {
      classIds.add(label.classId);
      classNames.set(label.classId, label.className);
    });

    predictionLabels.forEach((label) => {
      classIds.add(label.classId);
      classNames.set(label.classId, label.className);
    });

    const sortedClassIds = Array.from(classIds).sort((a, b) => a - b);
    const classCount = sortedClassIds.length;

    console.log("[Adjust Threshold] Class IDs:", sortedClassIds);
    console.log("[Adjust Threshold] Class Names Map:", classNames);
    console.log("[Adjust Threshold] Class Count:", classCount);

    // 初始化混淆矩陣（包含 "No prediction" 欄位）
    // 矩陣大小：(classCount + 1) x (classCount + 1)
    // 最後一列：No label
    // 最後一欄：No prediction
    const matrixSize = classCount + 1;
    const matrix: number[][] = Array(matrixSize)
      .fill(0)
      .map(() => Array(matrixSize).fill(0));

    // 建立 GT 和 Pred 的 Map（使用 imageId + position 作為 key）
    const gtLabelMap = new Map<string, ImageLabel>();
    groundTruthLabels.forEach((label) => {
      const key = `${label.imageId}_${label.position}`;
      gtLabelMap.set(key, label);
    });

    const predLabelMap = new Map<string, PredictionLabel>();
    predictionLabels.forEach((label) => {
      const key = `${label.imageId}_${label.position}`;
      predLabelMap.set(key, label);
    });

    console.log("[Adjust Threshold] GT Label Map size:", gtLabelMap.size);
    console.log("[Adjust Threshold] Pred Label Map size:", predLabelMap.size);

    // 處理所有 GT labels
    gtLabelMap.forEach((gtLabel, key) => {
      const predLabel = predLabelMap.get(key);
      const gtIndex = sortedClassIds.indexOf(gtLabel.classId);

      if (predLabel) {
        // 有對應的 prediction
        const predIndex = sortedClassIds.indexOf(predLabel.classId);
        matrix[gtIndex][predIndex]++;
        console.log(
          `[Adjust Threshold] Match found - GT: ${gtLabel.className} (${gtIndex}), Pred: ${predLabel.className} (${predIndex}), Position: ${gtLabel.position}`
        );
      } else {
        // 沒有對應的 prediction → "No prediction" 欄位（最後一欄）
        matrix[gtIndex][classCount]++;
        console.log(
          `[Adjust Threshold] No prediction found for GT: ${gtLabel.className}, imageId: ${gtLabel.imageId}, position: ${gtLabel.position}`
        );
      }
    });

    // 處理沒有 GT 的 predictions（False Positives）→ "No label" 列（最後一列）
    predLabelMap.forEach((predLabel, key) => {
      if (!gtLabelMap.has(key)) {
        const predIndex = sortedClassIds.indexOf(predLabel.classId);
        matrix[classCount][predIndex]++;
        console.log(
          `[Adjust Threshold] No GT found for Pred: ${predLabel.className}, imageId: ${predLabel.imageId}, position: ${predLabel.position}`
        );
      }
    });

    console.log(
      "[Adjust Threshold] Final Matrix (with No prediction & No label):",
      matrix
    );

    // 計算 Precision 和 Recall（不包含 "No prediction" 和 "No label"）
    const precisionByClass = this.calculatePrecisionByClass(matrix, classCount);
    const recallByClass = this.calculateRecallByClass(matrix, classCount);

    console.log("[Adjust Threshold] Precision by Class:", precisionByClass);
    console.log("[Adjust Threshold] Recall by Class:", recallByClass);

    // 計算整體指標（micro-averaged）
    const metrics = this.calculateMicroAveragedMetrics(matrix, classCount);

    console.log("[Adjust Threshold] Overall Metrics:", metrics);

    // 建立 class names 列表
    // GT class names（包含 "No label"）
    const gtClassNames = [
      ...sortedClassIds.map((id) => classNames.get(id) || "Unknown"),
      "No label",
    ];

    // Prediction class names（包含 "No prediction"）
    const predictionClassNames = [
      ...sortedClassIds.map((id) => classNames.get(id) || "Unknown"),
      "No prediction",
    ];

    return {
      classNames: gtClassNames, // 保留原有的 classNames（用於 GT labels）
      predictionClassNames, // 新增 predictionClassNames（用於 Prediction labels）
      matrix,
      precisionByClass,
      recallByClass,
      overallPrecision: metrics.precision,
      overallRecall: metrics.recall,
      overallF1: metrics.f1,
    };
  }

  /**
   * 將 labels 按 imageId 分組
   */
  private groupLabelsByImage(
    groundTruthLabels: ImageLabel[],
    predictionLabels: PredictionLabel[]
  ): LabelsByImage {
    const grouped: LabelsByImage = {};

    groundTruthLabels.forEach((label) => {
      if (!grouped[label.imageId]) {
        grouped[label.imageId] = { groundTruth: [], predictions: [] };
      }
      grouped[label.imageId].groundTruth.push(label);
    });

    predictionLabels.forEach((label) => {
      if (!grouped[label.imageId]) {
        grouped[label.imageId] = { groundTruth: [], predictions: [] };
      }
      grouped[label.imageId].predictions.push(label);
    });

    return grouped;
  }

  /**
   * 計算每個類別的 Precision（包含 "No prediction" 欄位）
   * Requirements: 28.11
   * Formula: Precision = TP / (TP + FP) * 100
   */
  private calculatePrecisionByClass(
    matrix: number[][],
    classCount: number
  ): number[] {
    const precision: number[] = [];

    // 計算實際類別的 Precision
    for (let j = 0; j < classCount; j++) {
      let tp = matrix[j][j]; // True Positive (對角線)
      let fp = 0; // False Positive (該欄的其他值)

      // 該欄（column j）的其他值（不包含對角線）
      for (let i = 0; i < classCount; i++) {
        if (i !== j) {
          fp += matrix[i][j];
        }
      }
      // 加入 "No label" 列的 FP
      fp += matrix[classCount][j];

      const total = tp + fp;
      precision.push(total > 0 ? (tp / total) * 100 : 0);
    }

    // 計算 "No prediction" 欄位的 Precision
    // "No prediction" 欄位沒有 TP（因為它代表沒有預測），所以 Precision 為 0
    precision.push(0);

    return precision;
  }

  /**
   * 計算每個類別的 Recall（包含 "No label" 列）
   * Requirements: 28.12
   * Formula: Recall = TP / (TP + FN) * 100
   */
  private calculateRecallByClass(
    matrix: number[][],
    classCount: number
  ): number[] {
    const recall: number[] = [];

    // 計算實際類別的 Recall
    for (let i = 0; i < classCount; i++) {
      let tp = matrix[i][i]; // True Positive (對角線)
      let fn = 0; // False Negative (該列的其他值)

      // 該列（row i）的其他值（不包含對角線）
      for (let j = 0; j < classCount; j++) {
        if (j !== i) {
          fn += matrix[i][j];
        }
      }
      // 加入 "No prediction" 欄的 FN
      fn += matrix[i][classCount];

      const total = tp + fn;
      recall.push(total > 0 ? (tp / total) * 100 : 0);
    }

    // 計算 "No label" 列的 Recall
    // "No label" 列沒有 TP（因為它代表沒有 GT），所以 Recall 為 0
    recall.push(0);

    return recall;
  }

  /**
   * 計算 micro-averaged 指標
   * Requirements: 29.1, 29.2, 29.3, 29.4, 29.5
   * @param matrix 混淆矩陣（包含 "No prediction" 和 "No label"）
   * @param classCount 實際類別數量（不包含 "No prediction" 和 "No label"）
   */
  private calculateMicroAveragedMetrics(
    matrix: number[][],
    classCount: number
  ): {
    precision: number;
    recall: number;
    f1: number;
  } {
    let totalTP = 0;
    let totalFP = 0;
    let totalFN = 0;

    console.log(
      "[Adjust Threshold] calculateMicroAveragedMetrics - Matrix:",
      matrix
    );
    console.log(
      "[Adjust Threshold] calculateMicroAveragedMetrics - classCount:",
      classCount
    );

    // 只計算實際類別的 TP, FP, FN（不包含 "No prediction" 和 "No label"）
    for (let i = 0; i < classCount; i++) {
      // True Positive: 對角線上的值
      const tp = matrix[i][i];
      totalTP += tp;

      // False Positive: 該欄（column i）的其他值（不包含對角線）
      // 即其他類別被錯誤預測為類別 i
      let fp = 0;
      for (let row = 0; row < classCount; row++) {
        if (row !== i) {
          fp += matrix[row][i];
        }
      }
      // 加入 "No label" 列的 FP（沒有 GT 但被預測為類別 i）
      const fpFromNoLabel = matrix[classCount][i];
      fp += fpFromNoLabel;
      totalFP += fp;

      // False Negative: 該列（row i）的其他值（不包含對角線）
      // 即類別 i 被錯誤預測為其他類別
      let fn = 0;
      for (let col = 0; col < classCount; col++) {
        if (col !== i) {
          fn += matrix[i][col];
        }
      }
      // 加入 "No prediction" 欄的 FN（類別 i 沒有被預測）
      const fnFromNoPred = matrix[i][classCount];
      fn += fnFromNoPred;
      totalFN += fn;

      console.log(
        `[Adjust Threshold] Class ${i} - TP: ${tp}, FP: ${fp} (from No label: ${fpFromNoLabel}), FN: ${fn} (from No pred: ${fnFromNoPred})`
      );
    }

    console.log(
      `[Adjust Threshold] Total - TP: ${totalTP}, FP: ${totalFP}, FN: ${totalFN}`
    );

    // Micro-averaged Precision
    const precision =
      totalTP + totalFP > 0 ? (totalTP / (totalTP + totalFP)) * 100 : 0;

    // Micro-averaged Recall
    const recall =
      totalTP + totalFN > 0 ? (totalTP / (totalTP + totalFN)) * 100 : 0;

    // F1 Score
    const f1 =
      precision + recall > 0
        ? (2 * precision * recall) / (precision + recall)
        : 0;

    console.log(
      `[Adjust Threshold] Metrics - Precision: ${precision.toFixed(
        2
      )}%, Recall: ${recall.toFixed(2)}%, F1: ${f1.toFixed(2)}%`
    );

    return { precision, recall, f1 };
  }

  /**
   * 取得重新計算的 metrics（用於產生新模型）
   * 計算所有三個 evaluation sets 在新 threshold 下的 metrics
   */
  private getRecalculatedMetrics(): RecalculatedMetrics {
    console.log(
      "[Adjust Threshold] Calculating metrics for all evaluation sets with threshold:",
      this.currentThreshold
    );

    // 計算 train set 的 metrics
    const trainMetrics = this.calculateMetricsForEvaluationSet("train");
    console.log("[Adjust Threshold] Train metrics:", trainMetrics);

    // 計算 dev set 的 metrics
    const devMetrics = this.calculateMetricsForEvaluationSet("dev");
    console.log("[Adjust Threshold] Dev metrics:", devMetrics);

    // 計算 test set 的 metrics
    const testMetrics = this.calculateMetricsForEvaluationSet("test");
    console.log("[Adjust Threshold] Test metrics:", testMetrics);

    return {
      trainF1: trainMetrics.f1,
      trainPrecision: trainMetrics.precision,
      trainRecall: trainMetrics.recall,
      devF1: devMetrics.f1,
      devPrecision: devMetrics.precision,
      devRecall: devMetrics.recall,
      testF1: testMetrics.f1,
      testPrecision: testMetrics.precision,
      testRecall: testMetrics.recall,
    };
  }

  /**
   * 計算特定 evaluation set 的 metrics
   */
  private calculateMetricsForEvaluationSet(
    evaluationSet: "train" | "dev" | "test"
  ): { f1: number; precision: number; recall: number } {
    const predictions = this.allPredictionLabelsBySet[evaluationSet];
    const groundTruth = this.allGroundTruthLabelsBySet[evaluationSet];

    // 根據閾值過濾 predictions
    const filteredPredictions = this.filterPredictionsByThreshold(
      predictions,
      this.currentThreshold
    );

    // 計算混淆矩陣
    const confusionMatrix = this.calculateConfusionMatrix(
      groundTruth,
      filteredPredictions
    );

    return {
      f1: confusionMatrix.overallF1,
      precision: confusionMatrix.overallPrecision,
      recall: confusionMatrix.overallRecall,
    };
  }

  /**
   * 是否顯示 Generate 按鈕
   * Requirements: 30.3
   */
  get showGenerateButton(): boolean {
    return (
      this.currentThreshold !== this.originalThreshold &&
      !this.loading &&
      !this.generating
    );
  }

  /**
   * 取得 Generate 按鈕文字
   * Requirements: 30.2
   */
  get generateButtonText(): string {
    return `Generate a New Report at ${this.currentThreshold.toFixed(2)}`;
  }

  /**
   * 取消按鈕處理
   * Requirements: 30.1, 30.4
   */
  onCancel(): void {
    this.dialogRef.close({ success: false });
  }

  /**
   * 產生新模型按鈕處理
   * Requirements: 30.5, 31.1, 33.6
   */
  onGenerate(): void {
    if (this.generating) return;

    this.generating = true;

    // 顯示產生中的提醒信息
    const generatingSnackBar = this.snackBar.open(
      "Generating new report, please wait...",
      "",
      {
        duration: 0, // 不自動關閉
      }
    );

    try {
      const request: GenerateModelRequest = {
        sourceModelId: this.data.modelId,
        newThreshold: this.currentThreshold,
        recalculatedMetrics: this.getRecalculatedMetrics(),
      };

      this.modelDetailService
        .generateModelWithNewThreshold(this.data.modelId, request)
        .subscribe({
          next: (response: GenerateModelResponse) => {
            this.generating = false;

            // 關閉產生中的提醒
            generatingSnackBar.dismiss();

            // 顯示成功訊息
            this.snackBar.open(
              `Successfully generated new report (Model ID: ${response.newModelId})`,
              "Close",
              { duration: 3000 }
            );

            // 關閉 dialog，並傳遞成功結果
            this.dialogRef.close({
              success: true,
              newModelId: response.newModelId,
            });
          },
          error: (error: any) => {
            console.error("Error generating model:", error);
            this.generating = false;

            // 關閉產生中的提醒
            generatingSnackBar.dismiss();

            // 顯示錯誤訊息
            this.snackBar.open(
              "Failed to generate new report: " +
                (error.message || "Unknown error"),
              "Close",
              { duration: 5000 }
            );
          },
        });
    } catch (error: any) {
      console.error("Error preparing request:", error);
      this.generating = false;

      // 關閉產生中的提醒
      generatingSnackBar.dismiss();

      // 顯示錯誤訊息
      this.snackBar.open(
        "Failed to prepare request: " + error.message,
        "Close",
        {
          duration: 5000,
        }
      );
    }
  }
}
