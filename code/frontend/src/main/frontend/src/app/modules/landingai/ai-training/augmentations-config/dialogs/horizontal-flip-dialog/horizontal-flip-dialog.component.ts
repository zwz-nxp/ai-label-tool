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

import { HorizontalFlipConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for HorizontalFlipDialog
 */
export interface HorizontalFlipDialogData {
  config: HorizontalFlipConfig;
}

/**
 * HorizontalFlipDialogComponent
 *
 * Dialog for configuring horizontal flip augmentation settings.
 *
 * Validates: Requirement 10.4
 * - WHEN a user clicks Edit on "Horizontal Flip", THE System SHALL open a dialog with Probability slider (0-1)
 */
@Component({
  selector: "app-horizontal-flip-dialog",
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
  templateUrl: "./horizontal-flip-dialog.component.html",
  styleUrls: ["./horizontal-flip-dialog.component.scss"],
})
export class HorizontalFlipDialogComponent implements OnInit {
  /** Probability value (0-1) */
  probability: number = 0.5;

  constructor(
    public dialogRef: MatDialogRef<HorizontalFlipDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: HorizontalFlipDialogData
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
   */
  onSave(): void {
    if (this.isValid()) {
      const result: HorizontalFlipConfig = {
        probability: this.probability,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
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
