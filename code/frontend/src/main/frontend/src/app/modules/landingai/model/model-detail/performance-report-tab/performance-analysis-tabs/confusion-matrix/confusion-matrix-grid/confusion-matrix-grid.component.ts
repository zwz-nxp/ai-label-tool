import {
  Component,
  Input,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
} from "@angular/core";
import {
  ConfusionMatrixData,
  CellSelection,
  MatrixCell,
  ClassMetrics,
} from "app/models/landingai/confusion-matrix.model";

/**
 * Confusion Matrix Grid Component
 * Renders the N×N confusion matrix grid with metrics
 * Requirements: 2.1, 2.2, 2.3, 2.5, 2.7, 3.1, 3.4, 4.2, 5.2
 */
@Component({
  selector: "app-confusion-matrix-grid",
  standalone: false,
  templateUrl: "./confusion-matrix-grid.component.html",
  styleUrls: ["./confusion-matrix-grid.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfusionMatrixGridComponent {
  @Input() matrixData!: ConfusionMatrixData;
  @Output() cellClick = new EventEmitter<CellSelection>();

  /**
   * Handle cell click event
   * Requirements: 6.1, 6.5
   */
  public onCellClick(cell: MatrixCell): void {
    if (cell.count === 0) {
      return; // Disable click for cells with zero count
    }

    const gtClass = this.matrixData.classes.find(
      (c) => c.id === cell.groundTruthClassId
    );
    const predClass = this.matrixData.predictionClasses.find(
      (c) => c.id === cell.predictionClassId
    );

    if (gtClass && predClass) {
      this.cellClick.emit({
        gtClassId: cell.groundTruthClassId,
        gtClassName: gtClass.name,
        predClassId: cell.predictionClassId,
        predClassName: predClass.name,
        count: cell.count,
      });
    }
  }

  /**
   * Calculate color intensity for a cell based on count
   * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
   */
  public getCellColor(count: number): string {
    if (count === 0) {
      return "#ffffff"; // White for zero count
    }

    const maxCount = this.matrixData.maxCount;
    if (maxCount === 0) {
      return "#e3f2fd"; // Light blue for all zeros
    }

    // Linear scaling: darker = higher count
    // Use blue color scheme
    const intensity = count / maxCount;
    const baseColor = { r: 33, g: 150, b: 243 }; // #2196F3 (Material Blue)
    const lightColor = { r: 227, g: 242, b: 253 }; // #E3F2FD (Light Blue)

    const r = Math.round(
      lightColor.r + (baseColor.r - lightColor.r) * intensity
    );
    const g = Math.round(
      lightColor.g + (baseColor.g - lightColor.g) * intensity
    );
    const b = Math.round(
      lightColor.b + (baseColor.b - lightColor.b) * intensity
    );

    return `rgb(${r}, ${g}, ${b})`;
  }

  /**
   * Get text color based on background intensity
   */
  public getTextColor(count: number): string {
    if (count === 0) {
      return "#999"; // Gray for zero
    }

    const maxCount = this.matrixData.maxCount;
    const intensity = count / maxCount;

    // Use white text for dark backgrounds, dark text for light backgrounds
    return intensity > 0.5 ? "#ffffff" : "#333333";
  }

  /**
   * Get precision for a class
   * Requirements: 4.1, 4.2, 4.3, 4.4
   */
  public getPrecision(classId: number): string {
    // "No prediction" class should not have precision value
    const predClass = this.matrixData.predictionClasses.find(
      (c) => c.id === classId
    );
    if (predClass?.name === "No prediction") {
      return "";
    }

    const metrics = this.matrixData.classMetrics.find(
      (m) => m.classId === classId
    );
    if (!metrics) {
      return "--";
    }

    const total = metrics.truePositives + metrics.falsePositives;
    if (total === 0) {
      return "--";
    }

    return `${metrics.precision.toFixed(1)}%`;
  }

  /**
   * Get recall for a class
   * Requirements: 5.1, 5.2, 5.3, 5.4
   */
  public getRecall(classId: number): string {
    // "No label" class should not have recall value
    const gtClass = this.matrixData.classes.find((c) => c.id === classId);
    if (gtClass?.name === "No label") {
      return "";
    }

    const metrics = this.matrixData.classMetrics.find(
      (m) => m.classId === classId
    );
    if (!metrics) {
      return "--";
    }

    const total = metrics.truePositives + metrics.falseNegatives;
    if (total === 0) {
      return "--";
    }

    return `${metrics.recall.toFixed(1)}%`;
  }

  /**
   * Get overall precision (micro-averaged across all predictions)
   * Micro-average: Total TP / (Total TP + Total FP)
   * Includes ALL classes (even those without GT labels)
   * This matches the MetricsCalculator behavior for Object Detection
   */
  public getOverallPrecision(): string {
    if (
      !this.matrixData.classMetrics ||
      this.matrixData.classMetrics.length === 0
    ) {
      return "--";
    }

    // Calculate micro-averaged precision: sum(TP) / sum(TP + FP)
    // Include ALL classes (even those without GT)
    let totalTP = 0;
    let totalFP = 0;

    for (const metric of this.matrixData.classMetrics) {
      totalTP += metric.truePositives;
      totalFP += metric.falsePositives;
    }

    const totalPredictions = totalTP + totalFP;
    if (totalPredictions === 0) {
      return "--";
    }

    const precision = (totalTP / totalPredictions) * 100;
    return `${precision.toFixed(1)}%`;
  }

  /**
   * Get overall recall (micro-averaged across all ground truth labels)
   * Micro-average: Total TP / (Total TP + Total FN)
   * Only includes classes that have ground truth labels
   * This matches the MetricsCalculator behavior for Object Detection
   */
  public getOverallRecall(): string {
    if (
      !this.matrixData.classMetrics ||
      this.matrixData.classMetrics.length === 0
    ) {
      return "--";
    }

    // Calculate micro-averaged recall: sum(TP) / sum(TP + FN)
    // Only include classes that have GT labels (TP + FN > 0)
    let totalTP = 0;
    let totalFN = 0;

    for (const metric of this.matrixData.classMetrics) {
      const hasGT = metric.truePositives + metric.falseNegatives > 0;
      if (hasGT) {
        totalTP += metric.truePositives;
        totalFN += metric.falseNegatives;
      }
    }

    const totalGT = totalTP + totalFN;
    if (totalGT === 0) {
      return "--";
    }

    const recall = (totalTP / totalGT) * 100;
    return `${recall.toFixed(1)}%`;
  }

  /**
   * Check if cell is "No label" × "No prediction" intersection
   * This cell should always display "--" as it's logically impossible
   */
  public isNoLabelNoPredictionCell(cell: MatrixCell): boolean {
    const gtClass = this.matrixData.classes.find(
      (c) => c.id === cell.groundTruthClassId
    );
    const predClass = this.matrixData.predictionClasses.find(
      (c) => c.id === cell.predictionClassId
    );

    return gtClass?.name === "No label" && predClass?.name === "No prediction";
  }

  /**
   * Check if cell is on diagonal (correct prediction)
   * Requirements: 2.7
   */
  public isDiagonalCell(cell: MatrixCell): boolean {
    return cell.isDiagonal;
  }

  /**
   * Get cell for specific GT and Pred class IDs
   */
  public getCell(gtClassId: number, predClassId: number): MatrixCell | null {
    for (const row of this.matrixData.matrix) {
      for (const cell of row) {
        if (
          cell.groundTruthClassId === gtClassId &&
          cell.predictionClassId === predClassId
        ) {
          return cell;
        }
      }
    }
    return null;
  }
}
