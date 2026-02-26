import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { EnhanceSettings } from "app/models/landingai/enhance-settings.model";

export interface EnhanceDialogData {
  brightness: number;
  contrast: number;
}

/**
 * Dialog component for adjusting image enhancement settings
 * Provides real-time preview of brightness and contrast adjustments
 */
@Component({
  selector: "app-enhance-dialog",
  templateUrl: "./enhance-dialog.component.html",
  styleUrls: ["./enhance-dialog.component.scss"],
  standalone: false,
})
export class EnhanceDialogComponent implements OnInit {
  brightness: number;
  contrast: number;

  // Store original values for reset
  private originalBrightness: number;
  private originalContrast: number;

  constructor(
    public dialogRef: MatDialogRef<EnhanceDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: EnhanceDialogData
  ) {
    this.brightness = data.brightness;
    this.contrast = data.contrast;
    this.originalBrightness = data.brightness;
    this.originalContrast = data.contrast;
  }

  ngOnInit(): void {
    // Initialize component
  }

  /**
   * Handle brightness slider change
   * Emits real-time updates for preview
   */
  onBrightnessChange(): void {
    // Real-time preview will be handled by the parent component
    // through the dialog data binding
  }

  /**
   * Handle contrast slider change
   * Emits real-time updates for preview
   */
  onContrastChange(): void {
    // Real-time preview will be handled by the parent component
    // through the dialog data binding
  }

  /**
   * Reset brightness and contrast to original values
   */
  onReset(): void {
    this.brightness = this.originalBrightness;
    this.contrast = this.originalContrast;
  }

  /**
   * Reset to default values (0, 0)
   */
  onResetToDefault(): void {
    this.brightness = 0;
    this.contrast = 0;
  }

  /**
   * Apply the changes and close the dialog
   */
  onApply(): void {
    const result: EnhanceSettings = {
      brightness: this.brightness,
      contrast: this.contrast,
    };
    this.dialogRef.close(result);
  }

  /**
   * Cancel and close the dialog without applying changes
   */
  onCancel(): void {
    // Return original values to revert any preview changes
    const result: EnhanceSettings = {
      brightness: this.originalBrightness,
      contrast: this.originalContrast,
    };
    this.dialogRef.close(result);
  }

  /**
   * Format slider value for display
   * @param value The slider value
   * @returns Formatted string with sign
   */
  formatSliderValue(value: number): string {
    if (value > 0) {
      return `+${value}`;
    }
    return value.toString();
  }
}
