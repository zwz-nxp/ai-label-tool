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

import { RandomBrightnessConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for RandomBrightnessDialog
 */
export interface RandomBrightnessDialogData {
  config: RandomBrightnessConfig;
}

/**
 * RandomBrightnessDialogComponent
 *
 * Dialog for configuring random brightness augmentation settings.
 *
 * Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5
 * - WHEN the Random Brightness dialog opens, THE System SHALL display a Limit slider with range -1 to 1 (12.1)
 * - THE System SHALL set default Limit value to 0.2 (12.2)
 * - WHEN a user adjusts the slider, THE System SHALL update the limit value in real-time (12.3)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (12.4)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (12.5)
 */
@Component({
  selector: "app-random-brightness-dialog",
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
  templateUrl: "./random-brightness-dialog.component.html",
  styleUrls: ["./random-brightness-dialog.component.scss"],
})
export class RandomBrightnessDialogComponent implements OnInit {
  /** Brightness limit value (-1 to 1) */
  limit: number = 0.2;

  /** Math reference for template */
  Math = Math;

  constructor(
    public dialogRef: MatDialogRef<RandomBrightnessDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RandomBrightnessDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.limit = this.data.config.limit;
    }
  }

  /**
   * Handle slider value change
   * Requirement 12.3: Update limit value in real-time
   */
  onSliderChange(value: number): void {
    this.limit = value;
  }

  /**
   * Handle input value change
   */
  onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let value = parseFloat(target.value);

    // Clamp value to valid range
    if (isNaN(value)) value = 0.2;
    if (value < -1) value = -1;
    if (value > 1) value = 1;

    this.limit = Math.round(value * 100) / 100;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return this.limit >= -1 && this.limit <= 1;
  }

  /**
   * Handle Save button click
   * Requirement 12.4: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: RandomBrightnessConfig = {
        limit: this.limit,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 12.5: Close dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Format limit value for slider display
   */
  formatLimit(value: number): string {
    return value.toFixed(2);
  }
}
