import {
  Component,
  Input,
  ChangeDetectionStrategy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  OnChanges,
  SimpleChanges,
  ChangeDetectorRef,
} from "@angular/core";
import { ChartData } from "app/models/landingai/loss-chart";
import { Chart, ChartConfiguration, registerables } from "chart.js";

// Register Chart.js components
Chart.register(...registerables);

/**
 * Loss Chart Component
 * Displays training loss over time using Chart.js
 * Requirements: 7.1, 7.3, 7.5, 7.7, 7.8, 7.9, 7.10, 7.12, 7.13, 7.14
 */
@Component({
  selector: "app-loss-chart",
  standalone: false,
  template: `
    <div class="loss-chart">
      <h4>Loss Chart</h4>
      <div *ngIf="chartData" class="chart-container">
        <canvas #chartCanvas></canvas>
      </div>
      <div *ngIf="!chartData" class="no-data">
        <p>No loss data available</p>
      </div>
    </div>
  `,
  styles: [
    `
      .loss-chart {
        padding: 16px;
        background: white;
        border-radius: 4px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      .loss-chart h4 {
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
      .chart-container {
        position: relative;
        height: 300px;
        width: 100%;
      }
      .no-data {
        text-align: center;
        padding: 40px;
        color: #999;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LossChartComponent implements AfterViewInit, OnChanges {
  @Input() public chartData: ChartData | null = null;
  @ViewChild("chartCanvas") chartCanvas?: ElementRef<HTMLCanvasElement>;

  private chart?: Chart;

  constructor(private cdr: ChangeDetectorRef) {}

  ngAfterViewInit(): void {
    // Render chart after view is initialized
    setTimeout(() => {
      this.renderChart();
    }, 0);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["chartData"] && !changes["chartData"].firstChange) {
      this.renderChart();
    }
  }

  private renderChart(): void {
    if (!this.chartData || !this.chartCanvas) {
      console.log("Loss Chart - No data or canvas", {
        hasData: !!this.chartData,
        hasCanvas: !!this.chartCanvas,
      });
      return;
    }

    console.log("Loss Chart - Rendering with data:", this.chartData);

    // Destroy existing chart
    if (this.chart) {
      this.chart.destroy();
    }

    const ctx = this.chartCanvas.nativeElement.getContext("2d");
    if (!ctx) {
      console.error("Loss Chart - Failed to get canvas context");
      return;
    }

    const config: ChartConfiguration = {
      type: "line",
      data: this.chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: "bottom",
          },
          tooltip: {
            mode: "index",
            intersect: false,
          },
        },
        scales: {
          x: {
            display: true,
            title: {
              display: true,
              text: "Time",
            },
          },
          y: {
            display: true,
            title: {
              display: true,
              text: "Loss",
            },
            beginAtZero: true,
          },
        },
        interaction: {
          mode: "nearest",
          axis: "x",
          intersect: false,
        },
      },
    };

    this.chart = new Chart(ctx, config);
    console.log("Loss Chart - Chart created successfully");
    this.cdr.markForCheck();
  }
}
