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

import { RandomAugmentConfig } from "app/state/landingai/ai-training";

/**
 * Dialog data interface for RandomAugmentDialog
 */
export interface RandomAugmentDialogData {
  config: RandomAugmentConfig;
}

/**
 * RandomAugmentDialogComponent
 *
 * Dialog for configuring random augment settings.
 *
 * Validates: Requirement 10.5
 * - WHEN a user clicks Edit on "Random Augment", THE System SHALL open a dialog with Number of transforms input and Magnitude slider
 */
@Component({
  selector: "app-random-augment-dialog",
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
  templateUrl: "./random-augment-dialog.component.html",
  styleUrls: ["./random-augment-dialog.component.scss"],
})
export class RandomAugmentDialogComponent implements OnInit {
  /** Number of transforms to apply */
  numTransforms: number = 2;

  /** Magnitude of transforms (0-10) */
  magnitude: number = 9;

  /** Validation error messages */
  numTransformsError: string = "";

  constructor(
    public dialogRef: MatDialogRef<RandomAugmentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: RandomAugmentDialogData
  ) {}

  ngOnInit(): void {
    if (this.data?.config) {
      this.numTransforms = this.data.config.numTransforms;
      this.magnitude = this.data.config.magnitude;
    }
  }

  /**
   * Handle magnitude slider change
   */
  onMagnitudeChange(value: number): void {
    this.magnitude = value;
  }

  /**
   * Handle number of transforms input change
   */
  onNumTransformsChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);

    if (!isNaN(value) && value >= 1) {
      this.numTransforms = value;
      this.numTransformsError = "";
    } else {
      this.numTransformsError = "Must be at least 1";
    }
  }

  /**
   * Validate number of transforms
   */
  validateNumTransforms(): boolean {
    if (!this.numTransforms || this.numTransforms < 1) {
      this.numTransformsError = "Must be at least 1";
      return false;
    }
    this.numTransformsError = "";
    return true;
  }

  /**
   * Check if form is valid
   */
  isValid(): boolean {
    return (
      this.validateNumTransforms() &&
      this.magnitude >= 0 &&
      this.magnitude <= 10
    );
  }

  /**
   * Handle Save button click
   */
  onSave(): void {
    if (this.isValid()) {
      const result: RandomAugmentConfig = {
        numTransforms: this.numTransforms,
        magnitude: this.magnitude,
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle Cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Format magnitude value for slider display
   */
  formatMagnitude(value: number): string {
    return value.toString();
  }
}
