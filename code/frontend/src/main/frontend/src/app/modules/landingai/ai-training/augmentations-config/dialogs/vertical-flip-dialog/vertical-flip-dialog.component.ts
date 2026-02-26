import { Component, Inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSliderModule } from "@angular/material/slider";
import { MatIconModule } from "@angular/material/icon";

import { VerticalFlipConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for VerticalFlipDialog
 */
export interface VerticalFlipDialogData {
  config: VerticalFlipConfig;
}

/**
 * VerticalFlipDialogComponent
 *
 * Dialog for configuring vertical flip augmentation settings.
 *
 * Validates: Requirements 18.1, 18.2, 18.3, 18.4
 * - WHEN the Vertical Flip dialog opens, THE System SHALL display a Probability slider (0-1) (18.1)
 * - THE System SHALL set default Probability value to 0.5 (18.2)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (18.3)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (18.4)
 */
@Component({
  selector: "app-vertical-flip-dialog",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSliderModule,
    MatIconModule,
  ],
  templateUrl: "./vertical-flip-dialog.component.html",
  styleUrls: ["./vertical-flip-dialog.component.scss"],
})
export class VerticalFlipDialogComponent implements OnInit {
  /** Probability value (0-1) */
  probability: number = 0.5;

  constructor(
    public dialogRef: MatDialogRef<VerticalFlipDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: VerticalFlipDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.probability = this.data.config.probability;
    }
  }

  /**
   * Handle slider value change
   */
  onSliderChange(value: number): void {
    this.probability = value;
  }

  /**
   * Handle input value change
   */
  onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let value = parseFloat(target.value);

    // Clamp value to valid range
    if (isNaN(value)) value = 0.5;
    if (value < 0) value = 0;
    if (value > 1) value = 1;

    this.probability = Math.round(value * 100) / 100;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return this.probability >= 0 && this.probability <= 1;
  }

  /**
   * Handle Save button click
   * Requirement 18.3: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: VerticalFlipConfig = {
        probability: this.probability,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 18.4: Close dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Format probability value for slider display
   */
  formatProbability(value: number): string {
    return value.toFixed(2);
  }
}
