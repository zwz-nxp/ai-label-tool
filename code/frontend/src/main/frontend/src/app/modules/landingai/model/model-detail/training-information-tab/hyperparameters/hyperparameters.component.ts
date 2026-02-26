import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

/**
 * Hyperparameters Component
 * Displays training hyperparameters
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5
 */
@Component({
  selector: "app-hyperparameters",
  standalone: false,
  template: `
    <table class="hyperparameters">
      <tr>
        <th rowspan="2">Hyperparameter</th>
        <td>
          <span class="param-label">Epoch </span>
          <span class="param-value">{{ epochs ?? "--" }}</span>
        </td>
      </tr>
      <tr>
        <td>
          <span class="param-label">Model size </span>
          <span class="param-value">{{ modelSize ?? "--" }}</span>
        </td>
      </tr>
    </table>
  `,
  styles: [
    `
      .hyperparameters {
        width: 100%;
        border-collapse: collapse;
        margin: 4px 0;
      }
      .hyperparameters th {
        text-align: left;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        padding: 4px 16px;
        width: 200px;
        vertical-align: top;
      }
      .hyperparameters td {
        text-align: left;
        font-size: 14px;
        padding: 4px 16px;
      }
      .param-label {
        color: #666;
        margin-right: 8px;
      }
      .param-value {
        color: #333;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HyperparametersComponent {
  @Input() public epochs: number | null = null;
  @Input() public modelSize: string | null = null;
}
