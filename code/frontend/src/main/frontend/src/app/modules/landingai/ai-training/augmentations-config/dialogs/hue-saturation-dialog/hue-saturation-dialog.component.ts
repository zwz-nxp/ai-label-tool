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

import { HueSaturationValueConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for HueSaturationDialog
 */
export interface HueSaturationDialogData {
  config: HueSaturationValueConfig;
}

/**
 * HueSaturationDialogComponent
 *
 * Dialog for configuring hue, saturation, and value augmentation settings.
 *
 * Validates: Requirements 16.1, 16.2, 16.3, 16.4
 * - WHEN the Hue Saturation Value dialog opens, THE System SHALL display Hue shift limit, Saturation shift limit, and Value shift limit sliders (16.1)
 * - THE System SHALL set reasonable default values for all three parameters (16.2)
 * - WHEN a user clicks Save, THE System SHALL close the dialog with the configuration (16.3)
 * - WHEN a user clicks Cancel, THE System SHALL close the dialog without saving changes (16.4)
 */
@Component({
  selector: "app-hue-saturation-dialog",
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
  templateUrl: "./hue-saturation-dialog.component.html",
  styleUrls: ["./hue-saturation-dialog.component.scss"],
})
export class HueSaturationDialogComponent implements OnInit {
  /** Hue shift limit */
  hueShiftLimit: number = 20;

  /** Saturation shift limit */
  saturationShiftLimit: number = 30;

  /** Value (brightness) shift limit */
  valueShiftLimit: number = 20;

  constructor(
    public dialogRef: MatDialogRef<HueSaturationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: HueSaturationDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.hueShiftLimit = this.data.config.hueShiftLimit;
      this.saturationShiftLimit = this.data.config.saturationShiftLimit;
      this.valueShiftLimit = this.data.config.valueShiftLimit;
    }
  }

  /**
   * Handle hue shift slider change
   */
  onHueChange(value: number): void {
    this.hueShiftLimit = value;
  }

  /**
   * Handle saturation shift slider change
   */
  onSaturationChange(value: number): void {
    this.saturationShiftLimit = value;
  }

  /**
   * Handle value shift slider change
   */
  onValueChange(value: number): void {
    this.valueShiftLimit = value;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return (
      this.hueShiftLimit >= 0 &&
      this.hueShiftLimit <= 180 &&
      this.saturationShiftLimit >= 0 &&
      this.saturationShiftLimit <= 100 &&
      this.valueShiftLimit >= 0 &&
      this.valueShiftLimit <= 100
    );
  }

  /**
   * Handle Save button click
   * Requirement 16.3: Close dialog with configuration
   */
  onSave(): void {
    if (this.isValid()) {
      const result: HueSaturationValueConfig = {
        hueShiftLimit: this.hueShiftLimit,
        saturationShiftLimit: this.saturationShiftLimit,
        valueShiftLimit: this.valueShiftLimit,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   * Requirement 16.4: Close dialog without saving
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Format value for slider display
   */
  formatValue(value: number): string {
    return value.toString();
  }
}
