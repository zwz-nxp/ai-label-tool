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

import { CropConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for CropDialog
 */
export interface CropDialogData {
  config: CropConfig;
}

/**
 * CropDialogComponent
 *
 * Dialog for configuring crop settings.
 *
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4
 * - WHEN the Crop dialog opens, THE System SHALL display X offset, Y offset, Width, and Height number inputs (9.1)
 * - THE System SHALL validate that all values are non-negative numbers (9.2)
 * - WHEN a user clicks Save, THE System SHALL validate the inputs and close the dialog with the configuration (9.3)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (9.4)
 */
@Component({
  selector: "app-crop-dialog",
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
  templateUrl: "./crop-dialog.component.html",
  styleUrls: ["./crop-dialog.component.scss"],
})
export class CropDialogComponent implements OnInit {
  /** X offset value */
  xOffset: number = 0;

  /** Y offset value */
  yOffset: number = 0;

  /** Width value */
  width: number = 640;

  /** Height value */
  height: number = 640;

  /** Validation error messages */
  xOffsetError: string = "";
  yOffsetError: string = "";
  widthError: string = "";
  heightError: string = "";

  constructor(
    public dialogRef: MatDialogRef<CropDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CropDialogData
  ) {}

  /**
   * Get crop area display string
   */
  get cropAreaDisplay(): string {
    return `${this.width} × ${this.height} at (${this.xOffset}, ${this.yOffset})`;
  }

  ngOnInit(): void {
    if (this.data?.config) {
      this.xOffset = this.data.config.xOffset;
      this.yOffset = this.data.config.yOffset;
      this.width = this.data.config.width;
      this.height = this.data.config.height;
    }
  }

  /**
   * Validate X offset input
   * Requirement 9.2: Validate that all values are non-negative numbers
   */
  validateXOffset(): boolean {
    if (
      this.xOffset === null ||
      this.xOffset === undefined ||
      this.xOffset < 0
    ) {
      this.xOffsetError = "X offset must be 0 or greater";
      return false;
    }
    if (!Number.isInteger(this.xOffset)) {
      this.xOffsetError = "X offset must be a whole number";
      return false;
    }
    this.xOffsetError = "";
    return true;
  }

  /**
   * Validate Y offset input
   * Requirement 9.2: Validate that all values are non-negative numbers
   */
  validateYOffset(): boolean {
    if (
      this.yOffset === null ||
      this.yOffset === undefined ||
      this.yOffset < 0
    ) {
      this.yOffsetError = "Y offset must be 0 or greater";
      return false;
    }
    if (!Number.isInteger(this.yOffset)) {
      this.yOffsetError = "Y offset must be a whole number";
      return false;
    }
    this.yOffsetError = "";
    return true;
  }

  /**
   * Validate width input
   * Requirement 9.2: Validate that all values are non-negative numbers
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
   * Requirement 9.2: Validate that all values are non-negative numbers
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
    return (
      this.validateXOffset() &&
      this.validateYOffset() &&
      this.validateWidth() &&
      this.validateHeight()
    );
  }

  /**
   * Handle Save button click
   * Requirement 9.3: Validate inputs and close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: CropConfig = {
        xOffset: this.xOffset,
        yOffset: this.yOffset,
        width: this.width,
        height: this.height,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 9.4: Close dialog without saving changes
   */
  onCancel(): void {
    this.dialogRef.close();
  }
}
