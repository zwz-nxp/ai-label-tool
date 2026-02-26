import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";

// Angular Material Modules
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatSliderModule } from "@angular/material/slider";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatIconModule } from "@angular/material/icon";

// Models
import {
  DEFAULT_SPLIT_DISTRIBUTION,
  SplitDistribution,
} from "app/state/landingai/ai-training";
import { ProjectClass } from "app/state/landingai/ai-training/training.state";

/**
 * SplitDistributionComponent
 *
 * Component for configuring data split distribution ratios (Train/Dev/Test).
 * Provides slider controls for adjusting percentages and ensures the total
 * always equals 100%.
 *
 * Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
 * - Provides dropdown with "All Classes" and "Per Class" options (3.1)
 * - Displays class selector when "Per Class" is selected (3.2)
 * - Provides slider controls for Train/Dev/Test ratio allocation (3.3)
 * - Sets default ratio to 70% Train / 15% Dev / 15% Test (3.4)
 * - Ensures total equals 100% when any slider is adjusted (3.5)
 * - Emits updated values to parent component when distribution changes (3.6)
 */
@Component({
  selector: "app-split-distribution",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSliderModule,
    MatTooltipModule,
    MatIconModule,
  ],
  templateUrl: "./split-distribution.component.html",
  styleUrls: ["./split-distribution.component.scss"],
})
export class SplitDistributionComponent implements OnInit, OnChanges {
  /**
   * Current split distribution values
   * Requirement 3.4: Default ratio is 70% Train / 15% Dev / 15% Test
   */
  @Input() distribution: SplitDistribution = { ...DEFAULT_SPLIT_DISTRIBUTION };

  /**
   * Available project classes for "Per Class" mode
   * Requirement 3.2: Display class selector when "Per Class" is selected
   */
  @Input() classes: ProjectClass[] = [];

  /**
   * Emits when distribution values change
   * Requirement 3.6: Emit updated values to parent component
   */
  @Output() distributionChange = new EventEmitter<SplitDistribution>();

  /**
   * Current scope selection: 'all' for all classes, 'perClass' for specific class
   * Requirement 3.1: Dropdown with "All Classes" and "Per Class" options
   */
  scope: "all" | "perClass" = "all";

  /**
   * Selected class ID when in "Per Class" mode
   * Requirement 3.2: Class selector for choosing specific classes
   */
  selectedClassId: number | null = null;

  /** Internal distribution values for slider binding */
  trainValue: number = DEFAULT_SPLIT_DISTRIBUTION.train;
  devValue: number = DEFAULT_SPLIT_DISTRIBUTION.dev;
  testValue: number = DEFAULT_SPLIT_DISTRIBUTION.test;
  /** Slider configuration */
  readonly sliderMin = 0;
  readonly sliderMax = 100;
  readonly sliderStep = 1;
  /** Track which slider is being actively adjusted */
  private activeSlider: "train" | "dev" | "test" | null = null;

  /**
   * Get the current sum of all distribution values
   */
  get totalPercentage(): number {
    return this.trainValue + this.devValue + this.testValue;
  }

  /**
   * Check if distribution is valid (sums to 100%)
   */
  get isValid(): boolean {
    return this.validateDistribution();
  }

  /**
   * Get tooltip text for train slider
   */
  get trainTooltip(): string {
    return `Training set: ${this.trainValue}% of images will be used for training the model`;
  }

  /**
   * Get tooltip text for dev slider
   */
  get devTooltip(): string {
    return `Development/Validation set: ${this.devValue}% of images will be used for validation during training`;
  }

  /**
   * Get tooltip text for test slider
   */
  get testTooltip(): string {
    return `Test set: ${this.testValue}% of images will be used for final model evaluation`;
  }

  ngOnInit(): void {
    this.syncFromInput();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["distribution"] && !changes["distribution"].firstChange) {
      this.syncFromInput();
    }
  }

  /**
   * Handle scope dropdown change
   * Requirement 3.1: Dropdown with "All Classes" and "Per Class" options
   *
   * @param scope The selected scope ('all' or 'perClass')
   */
  onScopeChange(scope: "all" | "perClass"): void {
    this.scope = scope;
    if (scope === "all") {
      this.selectedClassId = null;
    }
  }

  /**
   * Handle class selection in "Per Class" mode
   * Requirement 3.2: Class selector for choosing specific classes
   *
   * @param classId The selected class ID
   */
  onClassSelect(classId: number): void {
    this.selectedClassId = classId;
  }

  /**
   * Handle slider value change
   * Requirement 3.5: Ensure total equals 100% when any slider is adjusted
   *
   * @param type The slider type ('train', 'dev', or 'test')
   * @param value The new slider value
   */
  onSliderChange(type: "train" | "dev" | "test", value: number): void {
    this.activeSlider = type;
    this.adjustDistribution(type, value);
    this.emitChange();
  }

  /**
   * Handle slider input (during drag)
   *
   * @param type The slider type
   * @param event The slider input event
   */
  onSliderInput(type: "train" | "dev" | "test", event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);
    if (!isNaN(value)) {
      this.activeSlider = type;
      this.adjustDistribution(type, value);
    }
  }

  /**
   * Validate that distribution sums to 100%
   * Requirement 3.5: Ensure total equals 100%
   *
   * @returns true if distribution is valid
   */
  validateDistribution(): boolean {
    const sum = this.trainValue + this.devValue + this.testValue;
    return sum === 100;
  }

  /**
   * Emit the current distribution to parent component
   * Requirement 3.6: Emit updated values to parent component
   */
  emitChange(): void {
    const distribution: SplitDistribution = {
      train: this.trainValue,
      dev: this.devValue,
      test: this.testValue,
    };
    this.distributionChange.emit(distribution);
  }

  /**
   * Sync internal values from input distribution
   */
  private syncFromInput(): void {
    if (this.distribution) {
      this.trainValue = this.distribution.train;
      this.devValue = this.distribution.dev;
      this.testValue = this.distribution.test;
    }
  }

  /**
   * Adjust distribution values to ensure sum equals 100%
   * Requirement 3.5: Ensure total equals 100%
   *
   * @param changedType The slider that was changed
   * @param newValue The new value for that slider
   */
  private adjustDistribution(
    changedType: "train" | "dev" | "test",
    newValue: number
  ): void {
    // Clamp value to valid range
    newValue = Math.max(0, Math.min(100, newValue));

    // Calculate the remaining value to distribute
    const remaining = 100 - newValue;

    // Get the other two sliders
    const otherSliders = this.getOtherSliders(changedType);
    const currentOtherSum = otherSliders.reduce(
      (sum, s) => sum + this.getValue(s),
      0
    );

    if (currentOtherSum === 0) {
      // If other sliders are both 0, distribute remaining equally
      const half = Math.floor(remaining / 2);
      this.setValue(changedType, newValue);
      this.setValue(otherSliders[0], half);
      this.setValue(otherSliders[1], remaining - half);
    } else {
      // Proportionally adjust other sliders
      this.setValue(changedType, newValue);

      const ratio = remaining / currentOtherSum;
      let distributed = 0;

      for (let i = 0; i < otherSliders.length - 1; i++) {
        const slider = otherSliders[i];
        const adjustedValue = Math.round(this.getValue(slider) * ratio);
        this.setValue(slider, adjustedValue);
        distributed += adjustedValue;
      }

      // Last slider gets the remainder to ensure exact 100%
      this.setValue(
        otherSliders[otherSliders.length - 1],
        remaining - distributed
      );
    }

    // Ensure no negative values
    this.ensureNonNegative();
  }

  /**
   * Get the other two slider types
   */
  private getOtherSliders(
    type: "train" | "dev" | "test"
  ): ("train" | "dev" | "test")[] {
    switch (type) {
      case "train":
        return ["dev", "test"];
      case "dev":
        return ["train", "test"];
      case "test":
        return ["train", "dev"];
    }
  }

  /**
   * Get value for a slider type
   */
  private getValue(type: "train" | "dev" | "test"): number {
    switch (type) {
      case "train":
        return this.trainValue;
      case "dev":
        return this.devValue;
      case "test":
        return this.testValue;
    }
  }

  /**
   * Set value for a slider type
   */
  private setValue(type: "train" | "dev" | "test", value: number): void {
    switch (type) {
      case "train":
        this.trainValue = value;
        break;
      case "dev":
        this.devValue = value;
        break;
      case "test":
        this.testValue = value;
        break;
    }
  }

  /**
   * Ensure all values are non-negative
   */
  private ensureNonNegative(): void {
    this.trainValue = Math.max(0, this.trainValue);
    this.devValue = Math.max(0, this.devValue);
    this.testValue = Math.max(0, this.testValue);
  }
}
