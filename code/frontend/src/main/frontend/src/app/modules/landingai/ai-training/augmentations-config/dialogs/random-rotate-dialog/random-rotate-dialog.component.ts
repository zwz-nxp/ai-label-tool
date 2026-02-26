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
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";

import { RandomRotateConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for RandomRotateDialog
 */
export interface RandomRotateDialogData {
  config: RandomRotateConfig;
}

/**
 * Border mode options for rotation
 */
export const BORDER_MODES = [
  { value: "reflect", label: "Reflect" },
  { value: "constant", label: "Constant (Black)" },
  { value: "replicate", label: "Replicate" },
  { value: "wrap", label: "Wrap" },
] as const;

/**
 * RandomRotateDialogComponent
 *
 * Dialog for configuring random rotation augmentation settings.
 *
 * Validates: Requirements 19.1, 19.2, 19.3, 19.4, 19.5
 * - WHEN the Random Rotate dialog opens, THE System SHALL display a Limit slider for rotation angle (19.1)
 * - THE System SHALL display a Border mode dropdown for selecting edge handling method (19.2)
 * - THE System SHALL set reasonable default values for both parameters (19.3)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (19.4)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (19.5)
 */
@Component({
  selector: "app-random-rotate-dialog",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSliderModule,
    MatSelectModule,
    MatIconModule,
  ],
  templateUrl: "./random-rotate-dialog.component.html",
  styleUrls: ["./random-rotate-dialog.component.scss"],
})
export class RandomRotateDialogComponent implements OnInit {
  /** Rotation angle limit in degrees */
  limit: number = 90;

  /** Border handling mode */
  borderMode: string = "reflect";

  /** Available border modes */
  readonly borderModes = BORDER_MODES;

  constructor(
    public dialogRef: MatDialogRef<RandomRotateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RandomRotateDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.limit = this.data.config.limit;
      this.borderMode = this.data.config.borderMode;
    }
  }

  /**
   * Handle limit slider change
   */
  onLimitChange(value: number): void {
    this.limit = value;
  }

  /**
   * Handle limit input change
   */
  onLimitInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let value = parseInt(target.value, 10);

    // Clamp value to valid range
    if (isNaN(value)) value = 90;
    if (value < 0) value = 0;
    if (value > 180) value = 180;

    this.limit = value;
  }

  /**
   * Handle border mode change
   */
  onBorderModeChange(value: string): void {
    this.borderMode = value;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return this.limit >= 0 && this.limit <= 180 && !!this.borderMode;
  }

  /**
   * Handle Save button click
   * Requirement 19.4: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: RandomRotateConfig = {
        limit: this.limit,
        borderMode: this.borderMode,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 19.5: Close dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Format limit value for slider display
   */
  formatLimit(value: number): string {
    return `${value}°`;
  }
}
