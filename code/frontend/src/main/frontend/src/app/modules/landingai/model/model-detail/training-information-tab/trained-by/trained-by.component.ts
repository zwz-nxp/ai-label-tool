import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

/**
 * Trained By Component
 * Displays training initiator
 * Requirements: 11.1, 11.2, 11.3
 */
@Component({
  selector: "app-trained-by",
  standalone: false,
  template: `
    <table class="trained-by">
      <tr>
        <th>Trained By</th>
        <td>{{ createdBy || "Unknown" }}</td>
      </tr>
    </table>
  `,
  styles: [
    `
      .trained-by {
        width: 100%;
        border-collapse: collapse;
        margin: 4px 0;
      }
      .trained-by th {
        text-align: left;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        padding: 4px 16px;
        width: 200px;
      }
      .trained-by td {
        text-align: left;
        font-size: 14px;
        color: #666;
        padding: 4px 16px;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrainedByComponent {
  @Input() public createdBy: string | null = null;
}
