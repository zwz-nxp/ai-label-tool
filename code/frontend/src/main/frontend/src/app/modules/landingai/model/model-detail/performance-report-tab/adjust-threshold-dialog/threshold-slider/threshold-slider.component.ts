import {
  Component,
  EventEmitter,
  Input,
  Output,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from "@angular/core";

/**
 * Threshold Slider Component
 * 提供滑桿和數字輸入來調整 confidence threshold
 * Requirements: 27.1, 27.2, 27.3, 27.4, 27.5, 27.6, 27.7, 27.8
 */
@Component({
  selector: "app-threshold-slider",
  standalone: false,
  templateUrl: "./threshold-slider.component.html",
  styleUrls: ["./threshold-slider.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ThresholdSliderComponent {
  /** 當前閾值 (0.00-0.99) */
  @Input() value: number = 0.5;

  /** 最小值 */
  @Input() min: number = 0.0;

  /** 最大值 */
  @Input() max: number = 0.99;

  /** 步進值 */
  @Input() step: number = 0.01;

  /** 原始閾值（用於顯示標記） */
  @Input() originalThreshold?: number;

  /** 是否禁用 */
  @Input() disabled: boolean = false;

  /** 值變更事件 */
  @Output() valueChange = new EventEmitter<number>();

  constructor(private cdr: ChangeDetectorRef) {}

  /**
   * 滑桿值變更處理（即時觸發）
   */
  onSliderChange(newValue: number): void {
    if (!isNaN(newValue)) {
      this.value = newValue;
      this.valueChange.emit(this.value);
      // 手動觸發變更檢測，確保 input 值即時更新
      this.cdr.markForCheck();
    }
  }

  /**
   * 滑桿拖拉中的即時更新（input 事件）
   */
  onSliderInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const numValue = parseFloat(input.value);

    if (!isNaN(numValue)) {
      this.value = numValue;
      this.valueChange.emit(this.value);
      this.cdr.markForCheck();
    }
  }

  /**
   * 輸入框值變更處理
   */
  onInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const numValue = parseFloat(input.value);

    if (!isNaN(numValue) && numValue >= this.min && numValue <= this.max) {
      this.value = numValue;
      this.valueChange.emit(this.value);
      this.cdr.markForCheck();
    }
  }

  /**
   * 格式化數值為兩位小數
   */
  formatValue(value: number): string {
    return value.toFixed(2);
  }

  /**
   * 取得滑桿顯示值（用於標記）
   */
  formatLabel(value: number): string {
    return this.formatValue(value);
  }

  /**
   * 計算原始閾值標記的位置
   * 考慮到 Material Slider 的內部 padding (通常是 8px)
   */
  getMarkerPosition(): string {
    if (this.originalThreshold === undefined) {
      return "0%";
    }

    // 計算百分比位置
    const percentage =
      ((this.originalThreshold - this.min) / (this.max - this.min)) * 100;

    // Material Slider 的軌道通常有 8px 的左右 padding
    // 所以實際可用寬度是 100% - 16px
    // 我們需要將百分比轉換為考慮 padding 的位置
    return `calc(8px + (100% - 16px) * ${percentage / 100})`;
  }
}
