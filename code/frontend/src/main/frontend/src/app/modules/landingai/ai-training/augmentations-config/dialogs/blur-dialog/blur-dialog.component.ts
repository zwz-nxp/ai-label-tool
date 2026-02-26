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

import { BlurConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for BlurDialog
 */
export interface BlurDialogData {
  config: BlurConfig;
}

/**
 * BlurDialogComponent
 *
 * Dialog for configuring blur augmentation settings.
 *
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4
 * - WHEN the Blur dialog opens, THE System SHALL display a Blur limit slider (13.1)
 * - THE System SHALL set a reasonable default value for blur limit (13.2)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (13.3)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (13.4)
 */
@Component({
  selector: "app-blur-dialog",
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
  templateUrl: "./blur-dialog.component.html",
  styleUrls: ["./blur-dialog.component.scss"],
})
export class BlurDialogComponent implements OnInit {
  /** Blur limit value (kernel size) */
  blurLimit: number = 7;

  constructor(
    public dialogRef: MatDialogRef<BlurDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: BlurDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.blurLimit = this.data.config.blurLimit;
    }
  }

  /**
   * Handle slider value change
   */
  onSliderChange(value: number): void {
    this.blurLimit = value;
  }

  /**
   * Handle input value change
   */
  onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let value = parseInt(target.value, 10);

    // Clamp value to valid range
    if (isNaN(value)) value = 7;
    if (value < 3) value = 3;
    if (value > 21) value = 21;

    // Ensure odd number for kernel size
    if (value % 2 === 0) value = value + 1;

    this.blurLimit = value;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return (
      this.blurLimit >= 3 && this.blurLimit <= 21 && this.blurLimit % 2 === 1
    );
  }

  /**
   * Handle Save button click
   * Requirement 13.3: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: BlurConfig = {
        blurLimit: this.blurLimit,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 13.4: Close dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Format blur limit value for slider display
   */
  formatBlurLimit(value: number): string {
    return value.toString();
  }
}
