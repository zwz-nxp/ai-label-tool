import {
  Component,
  Input,
  ChangeDetectionStrategy,
  OnChanges,
  SimpleChanges,
} from "@angular/core";
import { ConfusionMatrixData } from "app/models/landingai/adjust-threshold";

/**
 * Dynamic Confusion Matrix Component
 * 動態顯示混淆矩陣，根據閾值變化即時更新
 * Requirements: 28.1, 28.4, 28.5, 28.6, 28.7, 28.8, 28.9, 28.10, 28.13
 */
@Component({
  selector: "app-dynamic-confusion-matrix",
  standalone: false,
  templateUrl: "./dynamic-confusion-matrix.component.html",
  styleUrls: ["./dynamic-confusion-matrix.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DynamicConfusionMatrixComponent implements OnChanges {
  /** 混淆矩陣資料 */
  @Input() data?: ConfusionMatrixData;

  /** 是否正在載入 */
  @Input() loading: boolean = false;

  /** 最大計數值（用於顏色強度計算） */
  maxCount: number = 0;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["data"] && this.data) {
      console.log("[Dynamic Confusion Matrix] Data changed:", this.data);
      console.log(
        "[Dynamic Confusion Matrix] Class Names:",
        this.data.classNames
      );
      console.log("[Dynamic Confusion Matrix] Matrix:", this.data.matrix);
      this.calculateMaxCount();
    }
  }

  /**
   * 計算矩陣中的最大計數值
   */
  private calculateMaxCount(): void {
    if (!this.data || !this.data.matrix) {
      this.maxCount = 0;
      console.log(
        "[Dynamic Confusion Matrix] No data or matrix, maxCount set to 0"
      );
      return;
    }

    this.maxCount = 0;
    for (const row of this.data.matrix) {
      for (const count of row) {
        if (count > this.maxCount) {
          this.maxCount = count;
        }
      }
    }
    console.log(
      "[Dynamic Confusion Matrix] Max count calculated:",
      this.maxCount
    );
  }

  /**
   * 取得儲存格的背景顏色（根據計數值）
   * 計數值越高，顏色越深
   * 使用與 Performance Report 相同的顏色邏輯
   */
  getCellColor(count: number, isDiagonal: boolean = false): string {
    if (count === 0 || this.maxCount === 0) {
      return "#ffffff";
    }

    // 計算顏色強度 (0-1)
    const intensity = Math.min(count / this.maxCount, 1);

    // 使用藍色漸層 (與 Performance Report 相同)
    const r = Math.round(255 - intensity * 222); // 255 -> 33
    const g = Math.round(255 - intensity * 105); // 255 -> 150
    const b = 243; // 固定藍色值

    return `rgb(${r}, ${g}, ${b})`;
  }

  /**
   * 取得儲存格的文字顏色（根據計數值）
   * 計數值高時使用白色，低時使用灰色
   */
  getTextColor(count: number): string {
    if (count === 0 || this.maxCount === 0) {
      return "#999999"; // 灰色
    }

    const intensity = Math.min(count / this.maxCount, 1);

    // 當強度超過 0.5 時使用白色，否則使用灰色
    return intensity > 0.5 ? "#ffffff" : "#999999";
  }

  /**
   * 格式化百分比（一位小數）
   */
  formatPercentage(value: number): string {
    if (value === null || value === undefined || isNaN(value)) {
      return "--";
    }
    return value.toFixed(1) + "%";
  }

  /**
   * 格式化計數值
   * 只有 "No label" 列和 "No prediction" 欄的交叉點顯示 "--"
   * 其他值為 0 的 cell 顯示 "0"
   */
  formatCount(count: number, rowIndex: number, colIndex: number): string {
    if (count === null || count === undefined) {
      return "--";
    }

    if (count === 0) {
      // 檢查是否是 "No label" 列和 "No prediction" 欄的交叉點
      const isNoLabelRow =
        this.data && rowIndex === this.data.matrix.length - 1;
      const isNoPredictionCol =
        this.data && colIndex === this.data.matrix[0].length - 1;

      // 只有在 "No label" 列和 "No prediction" 欄的交叉點才顯示 "--"
      if (isNoLabelRow && isNoPredictionCol) {
        return "--";
      }

      // 其他值為 0 的 cell 顯示 "0"
      return "0";
    }

    return count.toString();
  }

  /**
   * 取得類別名稱（處理過長的名稱）
   */
  getClassName(name: string, maxLength: number = 15): string {
    if (!name) return "--";
    if (name.length <= maxLength) return name;
    return name.substring(0, maxLength - 3) + "...";
  }
}
