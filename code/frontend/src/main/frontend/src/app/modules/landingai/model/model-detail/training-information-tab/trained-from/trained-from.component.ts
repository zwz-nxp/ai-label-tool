import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

/**
 * Trained From Component
 * Displays transform parameters
 * Requirements: 8.1, 8.2, 8.3
 */
@Component({
  selector: "app-trained-from",
  standalone: false,
  template: `
    <table class="trained-from">
      <tr>
        <th>Trained from</th>
        <td>{{ transformParam || "No transformation parameters" }}</td>
      </tr>
    </table>
  `,
  styles: [
    `
      .trained-from {
        width: 100%;
        border-collapse: collapse;
        margin: 4px 0;
      }
      .trained-from th {
        text-align: left;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        padding: 4px 16px;
        width: 200px;
      }
      .trained-from td {
        text-align: left;
        font-size: 14px;
        color: #666;
        padding: 4px 16px;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrainedFromComponent {
  @Input() public transformParam: string | null = null;
}
