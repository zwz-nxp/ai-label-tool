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
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatIconModule } from "@angular/material/icon";

import { ManualResizeConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for ManualResizeDialog
 */
export interface ManualResizeDialogData {
  config: ManualResizeConfig;
  /** Original aspect ratio for calculations (width/height) */
  originalAspectRatio?: number;
}

/**
 * ManualResizeDialogComponent
 *
 * Dialog for configuring manual resize settings with aspect ratio preservation.
 *
 * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5, 8.6
 * - WHEN the Manual Resize dialog opens, THE System SHALL display Width and Height number inputs (8.1)
 * - THE System SHALL provide a "Keep aspect ratio" checkbox (8.2)
 * - WHEN "Keep aspect ratio" is checked and Width changes, THE System SHALL automatically calculate Height (8.3)
 * - WHEN "Keep aspect ratio" is checked and Height changes, THE System SHALL automatically calculate Width (8.4)
 * - WHEN a user clicks Save, THE System SHALL validate the inputs and close the dialog with the configuration (8.5)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (8.6)
 */
@Component({
  selector: "app-manual-resize-dialog",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatIconModule,
  ],
  templateUrl: "./manual-resize-dialog.component.html",
  styleUrls: ["./manual-resize-dialog.component.scss"],
})
export class ManualResizeDialogComponent implements OnInit {
  /** Width value */
  width: number = 640;

  /** Height value */
  height: number = 640;

  /** Whether to keep aspect ratio */
  keepAspectRatio: boolean = true;
  /** Validation error messages */
  widthError: string = "";
  heightError: string = "";
  /** Current aspect ratio (width/height) */
  private aspectRatio: number = 1;
  /** Flag to prevent recursive updates */
  private isUpdating: boolean = false;

  constructor(
    public dialogRef: MatDialogRef<ManualResizeDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ManualResizeDialogData
  ) {}

  /**
   * Get current aspect ratio display string
   */
  get aspectRatioDisplay(): string {
    if (this.height === 0) return "N/A";
    const ratio = this.width / this.height;
    return ratio.toFixed(2);
  }

  ngOnInit(): void {
    if (this.data?.config) {
      this.width = this.data.config.width;
      this.height = this.data.config.height;
      this.keepAspectRatio = this.data.config.keepAspectRatio;
    }

    // Calculate initial aspect ratio
    if (this.data?.originalAspectRatio) {
      this.aspectRatio = this.data.originalAspectRatio;
    } else if (this.height > 0) {
      this.aspectRatio = this.width / this.height;
    }
  }

  /**
   * Handle width change
   * Requirement 8.3: Auto-calculate height when keeping aspect ratio
   */
  onWidthChange(): void {
    this.validateWidth();

    if (this.keepAspectRatio && !this.isUpdating && this.width > 0) {
      this.isUpdating = true;
      this.height = Math.round(this.width / this.aspectRatio);
      this.validateHeight();
      this.isUpdating = false;
    }
  }

  /**
   * Handle height change
   * Requirement 8.4: Auto-calculate width when keeping aspect ratio
   */
  onHeightChange(): void {
    this.validateHeight();

    if (this.keepAspectRatio && !this.isUpdating && this.height > 0) {
      this.isUpdating = true;
      this.width = Math.round(this.height * this.aspectRatio);
      this.validateWidth();
      this.isUpdating = false;
    }
  }

  /**
   * Handle keep aspect ratio toggle
   * Update aspect ratio when enabled
   */
  onKeepAspectRatioChange(): void {
    if (this.keepAspectRatio && this.height > 0) {
      // Store current aspect ratio when enabling
      this.aspectRatio = this.width / this.height;
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
   * Requirement 8.5: Validate inputs and close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: ManualResizeConfig = {
        width: this.width,
        height: this.height,
        keepAspectRatio: this.keepAspectRatio,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 8.6: Close dialog without saving changes
   */
  onCancel(): void {
    this.dialogRef.close();
  }
}
