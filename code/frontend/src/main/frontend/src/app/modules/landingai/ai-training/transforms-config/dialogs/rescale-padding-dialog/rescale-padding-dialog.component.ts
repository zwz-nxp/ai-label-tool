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
import { MatIconModule } from "@angular/material/icon";

import { RescaleWithPaddingConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for RescalePaddingDialog
 */
export interface RescalePaddingDialogData {
  config: RescaleWithPaddingConfig;
}

/**
 * RescalePaddingDialogComponent
 *
 * Dialog for configuring rescale with padding settings.
 *
 * Validates: Requirements 7.4, 7.5
 * - WHEN a user clicks add and selects "Manual resize", THE System SHALL open a dialog with Width, Height fields (7.4)
 * - WHEN a user clicks add and selects "Crop", THE System SHALL open a dialog with fields (7.5)
 */
@Component({
  selector: "app-rescale-padding-dialog",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
  ],
  templateUrl: "./rescale-padding-dialog.component.html",
  styleUrls: ["./rescale-padding-dialog.component.scss"],
})
export class RescalePaddingDialogComponent implements OnInit {
  /** Width value */
  width: number = 640;

  /** Height value */
  height: number = 640;

  /** Validation error messages */
  widthError: string = "";
  heightError: string = "";

  constructor(
    public dialogRef: MatDialogRef<RescalePaddingDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RescalePaddingDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.width = this.data.config.width;
      this.height = this.data.config.height;
    }
  }

  /**
   * Validate width input
   */
  validateWidth(): boolean {
    if (!this.width || this.width < 1) {
      this.widthError = "Width must be at least 1 pixel";
      return false;
    }
    if (!Number.isInteger(this.width)) {
      this.widthError = "Width must be a whole number";
      return false;
    }
    this.widthError = "";
    return true;
  }

  /**
   * Validate height input
   */
  validateHeight(): boolean {
    if (!this.height || this.height < 1) {
      this.heightError = "Height must be at least 1 pixel";
      return false;
    }
    if (!Number.isInteger(this.height)) {
      this.heightError = "Height must be a whole number";
      return false;
    }
    this.heightError = "";
    return true;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return this.validateWidth() && this.validateHeight();
  }

  /**
   * Handle Save button click
   * Requirement 7.4, 7.5: Save configuration and close dialog
   */
  onSave(): void {
    if (this.isValid()) {
      const result: RescaleWithPaddingConfig = {
        enabled: true,
        width: this.width,
        height: this.height,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Close dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close();
  }
}
