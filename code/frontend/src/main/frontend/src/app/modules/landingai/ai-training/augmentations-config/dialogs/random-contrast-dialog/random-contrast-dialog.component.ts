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

import { RandomContrastConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for RandomContrastDialog
 */
export interface RandomContrastDialogData {
  config: RandomContrastConfig;
}

/**
 * RandomContrastDialogComponent
 *
 * Dialog for configuring random contrast augmentation settings.
 *
 * Validates: Requirements 17.1, 17.2, 17.3, 17.4
 * - WHEN the Random Contrast dialog opens, THE System SHALL display a Limit slider (17.1)
 * - THE System SHALL set a reasonable default value for contrast limit (17.2)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (17.3)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (17.4)
 */
@Component({
  selector: "app-random-contrast-dialog",
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
  templateUrl: "./random-contrast-dialog.component.html",
  styleUrls: ["./random-contrast-dialog.component.scss"],
})
export class RandomContrastDialogComponent implements OnInit {
  /** Contrast limit value */
  limit: number = 0.2;

  /** Math reference for template */
  Math = Math;

  constructor(
    public dialogRef: MatDialogRef<RandomContrastDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RandomContrastDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.limit = this.data.config.limit;
    }
  }

  /**
   * Handle slider value change
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
    if (value < 0) value = 0;
    if (value > 1) value = 1;

    this.limit = Math.round(value * 100) / 100;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return this.limit >= 0 && this.limit <= 1;
  }

  /**
   * Handle Save button click
   * Requirement 17.3: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: RandomContrastConfig = {
        limit: this.limit,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 17.4: Close dialog without saving
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
