import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { MatDialog } from "@angular/material/dialog";
import { ConfidentialReport } from "app/models/landingai/confidential-report";
import { Image } from "app/models/landingai/image";
import { Model } from "app/models/landingai/model";
import * as ModelDetailActions from "app/state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";
import * as ModelActions from "app/state/landingai/model/model.actions";
import { AdjustThresholdDialogComponent } from "./adjust-threshold-dialog/adjust-threshold-dialog.component";
import { AdjustThresholdDialogData } from "app/models/landingai/adjust-threshold";

/**
 * Performance Report Tab Component
 * Container component for performance report display
 * Requirements: 13.1, 13.4, 22.4
 */
@Component({
  selector: "app-performance-report-tab",
  standalone: false,
  templateUrl: "./performance-report-tab.component.html",
  styleUrls: ["./performance-report-tab.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerformanceReportTabComponent implements OnInit {
  public confidentialReport$: Observable<ConfidentialReport | null>;
  public selectedEvaluationSet$: Observable<"train" | "dev" | "test">;
  public images$: Observable<Image[]>;
  public isLoadingImages$: Observable<boolean>;
  public currentMetrics$: Observable<{
    f1: number | null;
    precision: number | null;
    recall: number | null;
  }>;
  public imageCount$: Observable<number>;
  public instanceCount$: Observable<number>;
  public selectedModelId$: Observable<number | null>;
  public modelData$: Observable<Model | null>;
  public projectId$: Observable<number | null>;

  constructor(
    private store: Store,
    private dialog: MatDialog
  ) {
    this.confidentialReport$ = this.store.select(
      ModelDetailSelectors.selectConfidentialReport
    );
    this.selectedEvaluationSet$ = this.store.select(
      ModelDetailSelectors.selectSelectedEvaluationSet
    );
    this.images$ = this.store.select(ModelDetailSelectors.selectImages);
    this.isLoadingImages$ = this.store.select(
      ModelDetailSelectors.selectIsLoadingImages
    );
    this.currentMetrics$ = this.store.select(
      ModelDetailSelectors.selectCurrentMetrics
    );
    this.imageCount$ = this.store.select(ModelDetailSelectors.selectImageCount);
    this.selectedModelId$ = this.store.select(
      ModelDetailSelectors.selectSelectedModelId
    );

    this.modelData$ = this.store.select(ModelDetailSelectors.selectModelData);

    this.projectId$ = this.modelData$.pipe(
      map((model) => model?.projectId ?? null)
    );

    // Calculate instance count from images
    this.instanceCount$ = this.images$.pipe(
      map((images) => {
        // Count total instances (labels) across all images
        return images.reduce((total, image) => {
          return total + (image.labels?.length ?? 0);
        }, 0);
      })
    );
  }

  public ngOnInit(): void {}

  public onEvaluationSetChange(evaluationSet: "train" | "dev" | "test"): void {
    this.store.dispatch(
      ModelDetailActions.selectEvaluationSet({ evaluationSet })
    );
  }

  public onExportCsv(): void {
    // Get current values and dispatch export action
    this.selectedModelId$
      .subscribe((modelId) => {
        this.selectedEvaluationSet$
          .subscribe((split) => {
            if (modelId) {
              this.store.dispatch(
                ModelDetailActions.exportCsv({ modelId, split })
              );
            }
          })
          .unsubscribe();
      })
      .unsubscribe();
  }

  public formatMetric(value: number | null | undefined): string {
    if (value === null || value === undefined) return "--";
    return `${(value * 100).toFixed(1)}%`;
  }

  /**
   * 開啟 Adjust Confidence Threshold 對話框
   * Requirements: 26.1, 26.2, 33.2, 33.3
   */
  public openAdjustThresholdDialog(): void {
    // 取得當前資料
    let modelId: number | null = null;
    let projectId: number | null = null;
    let modelAlias: string = "";
    let currentThreshold: number = 0.51;
    let evaluationSet: "train" | "dev" | "test" = "train";

    this.selectedModelId$.subscribe((id) => (modelId = id)).unsubscribe();
    this.projectId$.subscribe((id) => (projectId = id)).unsubscribe();
    this.modelData$
      .subscribe((model) => {
        if (model) {
          modelAlias = model.modelAlias || "";
        }
      })
      .unsubscribe();
    this.confidentialReport$
      .subscribe((report) => {
        if (report?.confidenceThreshold) {
          currentThreshold = report.confidenceThreshold / 100;
        }
      })
      .unsubscribe();
    this.selectedEvaluationSet$
      .subscribe((set) => (evaluationSet = set))
      .unsubscribe();

    if (!modelId || !projectId) {
      console.error("Model ID or Project ID not available");
      return;
    }

    // 開啟對話框
    const dialogData: AdjustThresholdDialogData = {
      modelId,
      projectId,
      modelAlias,
      currentThreshold,
      evaluationSet,
    };

    const dialogRef = this.dialog.open(AdjustThresholdDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      maxHeight: "99vh",
      data: dialogData,
      disableClose: false,
    });

    // 處理對話框關閉事件
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        console.log("New model generated:", result.newModelId);

        // 1. 關閉 Model Detail Panel
        this.store.dispatch(ModelDetailActions.closePanel());

        // 2. 重新載入 models list
        // 如果有 projectId，則載入該 project 的 models；否則載入所有 models
        if (projectId) {
          this.store.dispatch(ModelActions.loadModelsByProject({ projectId }));
        } else {
          this.store.dispatch(ModelActions.loadModels());
        }
      }
    });
  }
}
