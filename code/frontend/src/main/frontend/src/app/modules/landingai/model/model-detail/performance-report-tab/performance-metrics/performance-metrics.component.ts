import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

/**
 * Performance Metrics Component
 * Displays F1, Precision, Recall metrics
 * Requirements: 16.1-16.11
 */
@Component({
  selector: "app-performance-metrics",
  standalone: false,
  template: `
    <div class="performance-metrics">
      <div class="metric">
        <div class="metric-label">F1</div>
        <div class="metric-value">{{ formatMetric(f1) }}</div>
      </div>
      <div class="metric">
        <div class="metric-label">Precision</div>
        <div class="metric-value">{{ formatMetric(precision) }}</div>
      </div>
      <div class="metric">
        <div class="metric-label">Recall</div>
        <div class="metric-value">{{ formatMetric(recall) }}</div>
      </div>
    </div>
  `,
  styles: [
    `
      .performance-metrics {
        display: flex;
        gap: 24px;
        padding: 16px;
        justify-content: center;
      }
      .metric {
        text-align: center;
      }
      .metric-label {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
      }
      .metric-value {
        font-size: 24px;
        font-weight: 500;
        color: #1976d2;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerformanceMetricsComponent {
  @Input() public f1: number | null = null;
  @Input() public precision: number | null = null;
  @Input() public recall: number | null = null;

  public formatMetric(value: number | null): string {
    if (value === null) return "--";
    return `${(value * 100).toFixed(1)}%`;
  }
}
