import { Component, Input, ChangeDetectionStrategy } from "@angular/core";
import {
  TrainingRecord,
  calculateSplitDistribution,
} from "app/models/landingai/training-record";

/**
 * Split Distribution Component
 * Displays dataset split distribution
 * Requirements: 9.1-9.10
 */
@Component({
  selector: "app-split-distribution",
  standalone: false,
  template: `
    <table class="split-distribution" *ngIf="trainingRecord">
      <tr>
        <th>Split</th>
        <td>
          <div class="split-content">
            <div class="split-title">Split distribution on labeled images</div>
            <div class="split-bar-container">
              <div class="split-bar">
                <div
                  class="bar-segment train"
                  [style.width.%]="getTrainPercentage()"
                  [title]="
                    'train | ' +
                    trainingRecord.trainingCount +
                    ' (' +
                    getTrainPercentage().toFixed(1) +
                    '%)'
                  "
                ></div>
                <div
                  class="bar-segment dev"
                  [style.width.%]="getDevPercentage()"
                  [title]="
                    'dev | ' +
                    trainingRecord.devCount +
                    ' (' +
                    getDevPercentage().toFixed(1) +
                    '%)'
                  "
                ></div>
                <div
                  class="bar-segment test"
                  [style.width.%]="getTestPercentage()"
                  [title]="
                    'test | ' +
                    trainingRecord.testCount +
                    ' (' +
                    getTestPercentage().toFixed(1) +
                    '%)'
                  "
                ></div>
                <div
                  class="bar-segment unassigned"
                  [style.width.%]="getUnassignedPercentage()"
                  [title]="
                    'unassigned | ' +
                    getUnassignedCount() +
                    ' (' +
                    getUnassignedPercentage().toFixed(1) +
                    '%)'
                  "
                ></div>
              </div>
            </div>
            <div class="split-values">
              <div class="value-item">
                <div class="value">{{ trainingRecord.trainingCount }}</div>
                <div class="label">train</div>
              </div>
              <div class="value-item">
                <div class="value">{{ trainingRecord.devCount }}</div>
                <div class="label">dev</div>
              </div>
              <div class="value-item">
                <div class="value">{{ trainingRecord.testCount }}</div>
                <div class="label">test</div>
              </div>
              <div class="value-item">
                <div class="value">{{ getUnassignedCount() }}</div>
                <div class="label">unassigned</div>
              </div>
            </div>
          </div>
        </td>
      </tr>
    </table>
  `,
  styles: [
    `
      .split-distribution {
        width: 100%;
        border-collapse: collapse;
        margin: 4px 0;
      }
      .split-distribution th {
        text-align: left;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        padding: 4px 16px;
        width: 200px;
        vertical-align: top;
      }
      .split-distribution td {
        text-align: left;
        padding: 4px 16px;
      }
      .split-content {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .split-title {
        font-size: 14px;
        color: #666;
        margin-bottom: 4px;
      }
      .split-bar-container {
        width: 100%;
        margin-bottom: 8px;
      }
      .split-bar {
        display: flex;
        height: 24px;
        width: 100%;
        border-radius: 4px;
        overflow: hidden;
      }
      .bar-segment {
        height: 100%;
        transition: width 0.3s ease;
        cursor: pointer;
      }
      .bar-segment:hover {
        opacity: 0.8;
      }
      .bar-segment.train {
        background-color: rgb(118, 200, 147);
      }
      .bar-segment.dev {
        background-color: rgb(153, 217, 140);
      }
      .bar-segment.test {
        background-color: rgb(187, 229, 181);
      }
      .bar-segment.unassigned {
        background-color: rgb(185, 192, 212);
      }
      .split-values {
        display: flex;
        gap: 24px;
      }
      .value-item {
        display: flex;
        flex-direction: column;
        align-items: center;
      }
      .value-item .value {
        font-size: 16px;
        font-weight: 500;
        color: #333;
      }
      .value-item .label {
        font-size: 12px;
        color: #666;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SplitDistributionComponent {
  @Input() public trainingRecord: TrainingRecord | null = null;
  @Input() public totalImageCount: number = 0;

  public getUnassignedCount(): number {
    if (!this.trainingRecord) return 0;
    const dist = calculateSplitDistribution(
      this.trainingRecord,
      this.totalImageCount
    );
    return dist.unassigned;
  }

  public getTotalCount(): number {
    if (!this.trainingRecord) return 0;
    return (
      this.trainingRecord.trainingCount +
      this.trainingRecord.devCount +
      this.trainingRecord.testCount +
      this.getUnassignedCount()
    );
  }

  public getTrainPercentage(): number {
    const total = this.getTotalCount();
    if (total === 0) return 0;
    return (this.trainingRecord!.trainingCount / total) * 100;
  }

  public getDevPercentage(): number {
    const total = this.getTotalCount();
    if (total === 0) return 0;
    return (this.trainingRecord!.devCount / total) * 100;
  }

  public getTestPercentage(): number {
    const total = this.getTotalCount();
    if (total === 0) return 0;
    return (this.trainingRecord!.testCount / total) * 100;
  }

  public getUnassignedPercentage(): number {
    const total = this.getTotalCount();
    if (total === 0) return 0;
    return (this.getUnassignedCount() / total) * 100;
  }
}
