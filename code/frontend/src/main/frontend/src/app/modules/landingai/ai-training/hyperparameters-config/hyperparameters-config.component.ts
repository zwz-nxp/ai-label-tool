import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

// Angular Material Modules
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatSliderModule } from "@angular/material/slider";
import { MatInputModule } from "@angular/material/input";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";

// Models
import {
  clampEpoch,
  DEFAULT_HYPERPARAMETERS,
  EPOCH_CONFIG,
  HyperparametersConfig,
} from "app/state/landingai/ai-training";
import { MODEL_SIZES } from "app/models/landingai/training-config.model";
import { ModelParamHttpService } from "app/services/landingai/model-param-http.service";

/**
 * HyperparametersConfigComponent
 *
 * Component for configuring model training hyperparameters including
 * Epoch count and Model Size selection.
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 6.1, 6.2, 6.3, 6.4, 6.5
 * - Provides number input and slider for Epoch configuration (5.1)
 * - Sets default Epoch value to 40 (5.2)
 * - Restricts Epoch range to 1-100 (5.3)
 * - Displays tooltip explaining what an epoch is (5.4)
 * - Synchronizes input and slider when value changes (5.5)
 * - Clamps value to nearest valid boundary when outside range (5.6)
 * - Provides dropdown for Model Size selection (6.1)
 * - Displays three fixed options: RepPoints-[37M], RepPoints-[50M], RepPoints-[101M] (6.2)
 * - Sets default Model Size to "RepPoints-[37M]" (6.3)
 * - Displays tooltip explaining model size implications (6.4)
 * - Updates model configuration when model size changes (6.5)
 */
@Component({
  selector: "app-hyperparameters-config",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatSliderModule,
    MatInputModule,
    MatTooltipModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: "./hyperparameters-config.component.html",
  styleUrls: ["./hyperparameters-config.component.scss"],
})
export class HyperparametersConfigComponent
  implements OnInit, OnChanges, OnDestroy
{
  /**
   * Current hyperparameters configuration
   * Requirement 5.2: Default Epoch value is 40
   * Requirement 6.3: Default Model Size is "RepPoints-[37M]"
   */
  @Input() config: HyperparametersConfig = { ...DEFAULT_HYPERPARAMETERS };

  /**
   * Current location ID for loading model sizes from la_model_param
   */
  @Input() locationId: number | null = null;

  /**
   * Current project model type for filtering model sizes from la_model_param
   */
  @Input() modelType: string | null = null;

  /**
   * Emits when configuration values change
   */
  @Output() configChange = new EventEmitter<HyperparametersConfig>();

  /**
   * Available model sizes for selection.
   * Dynamically loaded from la_model_param table by location + model type.
   * Falls back to hardcoded MODEL_SIZES if loading fails or no data found.
   */
  modelSizeOptions: { value: string; label: string }[] = [...MODEL_SIZES];

  /** Whether model sizes are being loaded */
  isLoadingModelSizes = false;

  /** Fallback hardcoded model sizes */
  readonly FALLBACK_MODEL_SIZES = MODEL_SIZES;

  /**
   * Epoch configuration constants
   * Requirement 5.3: Range 1-100
   */
  readonly EPOCH_MIN = EPOCH_CONFIG.MIN;
  readonly EPOCH_MAX = EPOCH_CONFIG.MAX;
  readonly EPOCH_DEFAULT = EPOCH_CONFIG.DEFAULT;
  readonly EPOCH_STEP = EPOCH_CONFIG.STEP;

  /** Internal epoch value for two-way binding */
  epochValue: number = EPOCH_CONFIG.DEFAULT;

  /** Internal model size value for two-way binding */
  modelSizeValue: string = DEFAULT_HYPERPARAMETERS.modelSize;

  /**
   * Tooltip text for Epoch field
   * Requirement 5.4: Display tooltip explaining what an epoch is
   */
  readonly epochTooltip =
    "An epoch is one complete pass through the entire training dataset. " +
    "More epochs generally lead to better model performance, but may cause overfitting. " +
    "Recommended range: 20-60 epochs for most use cases.";

  /**
   * Tooltip text for Model Size field
   * Requirement 6.4: Display tooltip explaining model size implications
   */
  readonly modelSizeTooltip =
    "Model size affects training time and accuracy. " +
    "Larger models (101M) typically achieve higher accuracy but require more training time and resources. " +
    "Smaller models (37M) train faster and are suitable for simpler tasks.";

  /** Subject for managing subscription cleanup */
  private destroy$ = new Subject<void>();

  constructor(private modelParamHttpService: ModelParamHttpService) {}

  /**
   * Check if current epoch value is valid
   * Requirement 29.1: Validate Epoch value is between 1 and 100
   */
  get isEpochValid(): boolean {
    return (
      this.epochValue >= this.EPOCH_MIN && this.epochValue <= this.EPOCH_MAX
    );
  }

  /**
   * Get validation error message for epoch
   */
  get epochErrorMessage(): string {
    if (this.epochValue < this.EPOCH_MIN) {
      return `Epoch must be at least ${this.EPOCH_MIN}`;
    }
    if (this.epochValue > this.EPOCH_MAX) {
      return `Epoch must be at most ${this.EPOCH_MAX}`;
    }
    return "";
  }

  ngOnInit(): void {
    this.syncFromInput();
    this.loadModelSizes();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["config"] && !changes["config"].firstChange) {
      // Only sync if the actual values changed, not just the object reference
      const prev = changes["config"].previousValue as HyperparametersConfig;
      const curr = changes["config"].currentValue as HyperparametersConfig;
      if (
        prev?.epochs !== curr?.epochs ||
        prev?.modelSize !== curr?.modelSize
      ) {
        this.syncFromInput();
      }
    }
    // Reload model sizes only when locationId or modelType actually changes
    const locChange = changes["locationId"];
    const typeChange = changes["modelType"];
    if (
      (locChange && locChange.previousValue !== locChange.currentValue) ||
      (typeChange && typeChange.previousValue !== typeChange.currentValue)
    ) {
      this.loadModelSizes();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle epoch value change from number input
   * Requirement 5.5: Synchronize input and slider
   * Requirement 5.6: Clamp value to valid range
   *
   * @param event The input event
   */
  onEpochInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const rawValue = parseInt(target.value, 10);

    if (!isNaN(rawValue)) {
      // Clamp value to valid range (Requirement 5.6)
      this.epochValue = clampEpoch(rawValue);
      // Update input field to show clamped value
      target.value = this.epochValue.toString();
      this.emitChange();
    }
  }

  /**
   * Handle epoch value change from slider
   * Requirement 5.5: Synchronize input and slider
   *
   * @param value The new slider value
   */
  onEpochSliderChange(value: number): void {
    // Slider already constrains to valid range, but clamp for safety
    this.epochValue = clampEpoch(value);
    this.emitChange();
  }

  /**
   * Handle model size selection change
   * Requirement 6.5: Update model configuration when model size changes
   *
   * @param modelSize The selected model size
   */
  onModelSizeChange(modelSize: string): void {
    this.modelSizeValue = modelSize;
    this.emitChange();
  }

  /**
   * Emit the current configuration to parent component
   */
  emitChange(): void {
    const config: HyperparametersConfig = {
      epochs: this.epochValue,
      modelSize: this.modelSizeValue,
    };
    this.configChange.emit(config);
  }

  /**
   * Get display label for a model size value
   *
   * @param value The model size value
   * @returns The display label
   */
  getModelSizeLabel(value: string): string {
    const size = this.modelSizeOptions.find((s) => s.value === value);
    return size ? size.label : value;
  }

  /**
   * Load distinct model names from la_model_param table
   * filtered by current locationId + modelType.
   * Falls back to hardcoded MODEL_SIZES if no data found.
   */
  private loadModelSizes(): void {
    console.log(
      "[HyperparametersConfig] loadModelSizes called, locationId:",
      this.locationId,
      "modelType:",
      this.modelType
    );
    if (!this.locationId || !this.modelType) {
      this.modelSizeOptions = [...MODEL_SIZES];
      return;
    }

    this.isLoadingModelSizes = true;
    this.modelParamHttpService
      .getModelParamsByType(this.locationId, this.modelType)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (params) => {
          console.log("[HyperparametersConfig] API returned params:", params);
          // Extract distinct model names
          const uniqueNames = [...new Set(params.map((p) => p.modelName))];
          if (uniqueNames.length > 0) {
            this.modelSizeOptions = uniqueNames.map((name) => ({
              value: name,
              label: name,
            }));
            // If current selection is not in the new options, select the first one
            if (
              !this.modelSizeOptions.some(
                (opt) => opt.value === this.modelSizeValue
              )
            ) {
              this.modelSizeValue = this.modelSizeOptions[0].value;
              this.emitChange();
            }
          } else {
            console.log(
              "[HyperparametersConfig] No model params found, using fallback"
            );
            this.modelSizeOptions = [...MODEL_SIZES];
          }
          this.isLoadingModelSizes = false;
        },
        error: (err) => {
          console.error("[HyperparametersConfig] API error:", err);
          // Fallback to hardcoded options on error
          this.modelSizeOptions = [...MODEL_SIZES];
          this.isLoadingModelSizes = false;
        },
      });
  }

  /**
   * Sync internal values from input configuration
   */
  private syncFromInput(): void {
    if (this.config) {
      this.epochValue = this.config.epochs;
      this.modelSizeValue = this.config.modelSize;
    }
  }
}
