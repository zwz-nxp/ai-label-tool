import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

/**
 * Trained At Component
 * Displays training timestamps
 * Requirements: 10.1, 10.2, 10.3, 10.4
 */
@Component({
  selector: "app-trained-at",
  standalone: false,
  template: `
    <table class="trained-at">
      <tr>
        <th>Trained At</th>
        <td *ngIf="completedAt">
          {{ formatDate(completedAt) }}
        </td>
        <td *ngIf="!completedAt && startedAt">
          {{ formatDate(startedAt) }}
        </td>
        <td *ngIf="!completedAt && !startedAt">Training time not recorded</td>
      </tr>
    </table>
  `,
  styles: [
    `
      .trained-at {
        width: 100%;
        border-collapse: collapse;
        margin: 4px 0;
      }
      .trained-at th {
        text-align: left;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        padding: 4px 16px;
        width: 200px;
      }
      .trained-at td {
        text-align: left;
        font-size: 14px;
        color: #666;
        padding: 4px 16px;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrainedAtComponent {
  @Input() public startedAt: Date | null = null;
  @Input() public completedAt: Date | null = null;

  public formatDate(date: Date | null): string {
    if (!date) return "N/A";
    return new Date(date).toLocaleString();
  }
}
