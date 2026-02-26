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

import { GaussianBlurConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for GaussianBlurDialog
 */
export interface GaussianBlurDialogData {
  config: GaussianBlurConfig;
}

/**
 * GaussianBlurDialogComponent
 *
 * Dialog for configuring Gaussian blur augmentation settings.
 *
 * Validates: Requirements 15.1, 15.2, 15.3, 15.4
 * - WHEN the Gaussian Blur dialog opens, THE System SHALL display Blur limit and Sigma sliders (15.1)
 * - THE System SHALL set reasonable default values for both parameters (15.2)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (15.3)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (15.4)
 */
@Component({
  selector: "app-gaussian-blur-dialog",
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
  templateUrl: "./gaussian-blur-dialog.component.html",
  styleUrls: ["./gaussian-blur-dialog.component.scss"],
})
export class GaussianBlurDialogComponent implements OnInit {
  /** Blur limit value (kernel size) */
  blurLimit: number = 7;

  /** Sigma value for Gaussian distribution */
  sigma: number = 0;

  constructor(
    public dialogRef: MatDialogRef<GaussianBlurDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: GaussianBlurDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.blurLimit = this.data.config.blurLimit;
      this.sigma = this.data.config.sigma;
    }
  }

  /**
   * Handle blur limit slider change
   */
  onBlurLimitChange(value: number): void {
    this.blurLimit = value;
  }

  /**
   * Handle sigma slider change
   */
  onSigmaChange(value: number): void {
    this.sigma = value;
  }

  /**
   * Handle blur limit input change
   */
  onBlurLimitInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let value = parseInt(target.value, 10);

    if (isNaN(value)) value = 7;
    if (value < 3) value = 3;
    if (value > 21) value = 21;
    if (value % 2 === 0) value = value + 1;

    this.blurLimit = value;
  }

  /**
   * Handle sigma input change
   */
  onSigmaInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    let value = parseFloat(target.value);

    if (isNaN(value)) value = 0;
    if (value < 0) value = 0;
    if (value > 10) value = 10;

    this.sigma = Math.round(value * 10) / 10;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return (
      this.blurLimit >= 3 &&
      this.blurLimit <= 21 &&
      this.blurLimit % 2 === 1 &&
      this.sigma >= 0 &&
      this.sigma <= 10
    );
  }

  /**
   * Handle Save button click
   * Requirement 15.3: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: GaussianBlurConfig = {
        blurLimit: this.blurLimit,
        sigma: this.sigma,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 15.4: Close dialog without saving
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

  /**
   * Format sigma value for slider display
   */
  formatSigma(value: number): string {
    return value.toFixed(1);
  }
}
