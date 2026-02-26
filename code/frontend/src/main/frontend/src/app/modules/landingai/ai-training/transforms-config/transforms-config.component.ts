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
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatCardModule } from "@angular/material/card";

// Models
// Dialog Components
import {
  CropConfig,
  CropDialogComponent,
  ManualResizeConfig,
  ManualResizeDialogComponent,
  RescalePaddingDialogComponent,
  RescaleWithPaddingConfig,
  TransformConfig,
} from "app/state/landingai/ai-training";

/**
 * TransformsConfigComponent
 *
 * Component for configuring image transformations including:
 * - Rescale with padding
 * - Manual resize
 * - Crop
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.6, 7.7
 * - THE Transforms_Config_Component SHALL display a "Rescale with padding" configuration section (7.1)
 * - WHEN a user hovers over the Rescale section, THE System SHALL display a tooltip explaining the rescale behavior (7.2)
 * - THE System SHALL provide an add button to add transform options: Manual resize and Crop (7.3)
 * - THE System SHALL display the current Image Size with an Edit button on hover (7.6)
 * - WHEN a user clicks the Edit button, THE System SHALL open the Rescale with padding configuration dialog (7.7)
 */
@Component({
  selector: "app-transforms-config",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatDialogModule,
    MatCardModule,
  ],
  templateUrl: "./transforms-config.component.html",
  styleUrls: ["./transforms-config.component.scss"],
})
export class TransformsConfigComponent implements OnInit, OnChanges {
  /**
   * Current transform configuration
   */
  @Input() config: TransformConfig = {};

  /**
   * Emits when configuration values change
   */
  @Output() configChange = new EventEmitter<TransformConfig>();
  /**
   * Default rescale with padding configuration
   */
  readonly DEFAULT_RESCALE: RescaleWithPaddingConfig = {
    enabled: true,
    width: 640,
    height: 640,
  };
  /**
   * Default manual resize configuration
   */
  readonly DEFAULT_MANUAL_RESIZE: ManualResizeConfig = {
    width: 640,
    height: 640,
    keepAspectRatio: true,
  };
  /**
   * Default crop configuration
   */
  readonly DEFAULT_CROP: CropConfig = {
    xOffset: 0,
    yOffset: 0,
    width: 640,
    height: 640,
  };
  /**
   * Tooltip text for Rescale with padding
   * Requirement 7.2: Display tooltip explaining the rescale behavior
   */
  readonly rescaleTooltip =
    "Rescale with padding resizes images to a target size while maintaining aspect ratio. " +
    "Padding is added to fill any remaining space, ensuring all images have consistent dimensions " +
    "without distortion.";
  /**
   * Tooltip text for Manual resize
   */
  readonly manualResizeTooltip =
    "Manual resize allows you to specify exact target dimensions. " +
    'Enable "Keep aspect ratio" to automatically calculate one dimension based on the other.';
  /**
   * Tooltip text for Crop
   */
  readonly cropTooltip =
    "Crop extracts a rectangular region from the image. " +
    "Specify the offset from the top-left corner and the dimensions of the crop area.";
  /** Whether rescale with padding is enabled */
  rescaleEnabled: boolean = true;
  /** Current rescale configuration */
  rescaleConfig: RescaleWithPaddingConfig = { ...this.DEFAULT_RESCALE };
  /** Whether manual resize is added */
  hasManualResize: boolean = false;
  /** Current manual resize configuration */
  manualResizeConfig: ManualResizeConfig = { ...this.DEFAULT_MANUAL_RESIZE };
  /** Whether crop is added */
  hasCrop: boolean = false;
  /** Current crop configuration */
  cropConfig: CropConfig = { ...this.DEFAULT_CROP };
  /** Track hover state for showing edit buttons */
  isRescaleHovered: boolean = false;
  isManualResizeHovered: boolean = false;
  isCropHovered: boolean = false;

  /**
   * MatDialog service for opening dialogs
   */
  constructor(private dialog: MatDialog) {}

  /**
   * Get the current image size display string
   * Requirement 7.6: THE System SHALL display the current Image Size
   */
  get imageSizeDisplay(): string {
    return `${this.rescaleConfig.width} × ${this.rescaleConfig.height}`;
  }

  /**
   * Check if Manual resize can be added (not already present)
   */
  get canAddManualResize(): boolean {
    return !this.hasManualResize;
  }

  /**
   * Check if Crop can be added (not already present)
   */
  get canAddCrop(): boolean {
    return !this.hasCrop;
  }

  ngOnInit(): void {
    this.syncFromInput();
    // Emit default config so parent/store receives initial values (e.g. rescaleWithPadding 640x640)
    this.emitChange();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["config"] && !changes["config"].firstChange) {
      this.syncFromInput();
    }
  }

  /**
   * Add a transform option
   * Requirement 7.3: THE System SHALL provide an add button to add transform options
   *
   * @param type The type of transform to add ('manualResize' | 'crop')
   */
  addTransform(type: "manualResize" | "crop"): void {
    if (type === "manualResize" && !this.hasManualResize) {
      this.hasManualResize = true;
      this.manualResizeConfig = { ...this.DEFAULT_MANUAL_RESIZE };
      this.emitChange();
      // Open dialog for configuration
      this.openManualResizeDialog();
    } else if (type === "crop" && !this.hasCrop) {
      this.hasCrop = true;
      this.cropConfig = { ...this.DEFAULT_CROP };
      this.emitChange();
      // Open dialog for configuration
      this.openCropDialog();
    }
  }

  /**
   * Remove a transform option
   *
   * @param type The type of transform to remove
   */
  removeTransform(type: "manualResize" | "crop"): void {
    if (type === "manualResize") {
      this.hasManualResize = false;
      this.manualResizeConfig = { ...this.DEFAULT_MANUAL_RESIZE };
      this.emitChange();
    } else if (type === "crop") {
      this.hasCrop = false;
      this.cropConfig = { ...this.DEFAULT_CROP };
      this.emitChange();
    }
  }

  /**
   * Open the Rescale with padding configuration dialog
   * Requirement 7.7: WHEN a user clicks the Edit button, THE System SHALL open the Rescale with padding configuration dialog
   */
  openRescaleDialog(): void {
    const dialogRef = this.dialog.open(RescalePaddingDialogComponent, {
      width: "450px",
      data: { config: { ...this.rescaleConfig } },
    });

    dialogRef
      .afterClosed()
      .subscribe((result: RescaleWithPaddingConfig | undefined) => {
        if (result) {
          this.rescaleConfig = result;
          this.emitChange();
        }
      });
  }

  /**
   * Open the Manual Resize configuration dialog
   * Requirement 7.4: WHEN a user clicks add and selects "Manual resize", THE System SHALL open a dialog
   */
  openManualResizeDialog(): void {
    const dialogRef = this.dialog.open(ManualResizeDialogComponent, {
      width: "450px",
      data: {
        config: { ...this.manualResizeConfig },
        originalAspectRatio:
          this.manualResizeConfig.width / this.manualResizeConfig.height,
      },
    });

    dialogRef
      .afterClosed()
      .subscribe((result: ManualResizeConfig | undefined) => {
        if (result) {
          this.manualResizeConfig = result;
          this.emitChange();
        }
      });
  }

  /**
   * Open the Crop configuration dialog
   * Requirement 7.5: WHEN a user clicks add and selects "Crop", THE System SHALL open a dialog
   */
  openCropDialog(): void {
    const dialogRef = this.dialog.open(CropDialogComponent, {
      width: "450px",
      data: { config: { ...this.cropConfig } },
    });

    dialogRef.afterClosed().subscribe((result: CropConfig | undefined) => {
      if (result) {
        this.cropConfig = result;
        this.emitChange();
      }
    });
  }

  /**
   * Handle rescale width change
   * @param event The input event
   */
  onRescaleWidthChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);
    if (!isNaN(value) && value > 0) {
      this.rescaleConfig.width = value;
      this.emitChange();
    }
  }

  /**
   * Handle rescale height change
   * @param event The input event
   */
  onRescaleHeightChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);
    if (!isNaN(value) && value > 0) {
      this.rescaleConfig.height = value;
      this.emitChange();
    }
  }

  /**
   * Handle manual resize width change
   * @param event The input event
   */
  onManualResizeWidthChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);
    if (!isNaN(value) && value > 0) {
      const oldWidth = this.manualResizeConfig.width;
      this.manualResizeConfig.width = value;

      // Auto-calculate height if keeping aspect ratio
      if (this.manualResizeConfig.keepAspectRatio && oldWidth > 0) {
        const aspectRatio = this.manualResizeConfig.height / oldWidth;
        this.manualResizeConfig.height = Math.round(value * aspectRatio);
      }

      this.emitChange();
    }
  }

  /**
   * Handle manual resize height change
   * @param event The input event
   */
  onManualResizeHeightChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);
    if (!isNaN(value) && value > 0) {
      const oldHeight = this.manualResizeConfig.height;
      this.manualResizeConfig.height = value;

      // Auto-calculate width if keeping aspect ratio
      if (this.manualResizeConfig.keepAspectRatio && oldHeight > 0) {
        const aspectRatio = this.manualResizeConfig.width / oldHeight;
        this.manualResizeConfig.width = Math.round(value * aspectRatio);
      }

      this.emitChange();
    }
  }

  /**
   * Handle keep aspect ratio toggle
   * @param checked Whether to keep aspect ratio
   */
  onKeepAspectRatioChange(checked: boolean): void {
    this.manualResizeConfig.keepAspectRatio = checked;
    this.emitChange();
  }

  /**
   * Handle crop configuration changes
   * @param field The field to update
   * @param event The input event
   */
  onCropChange(
    field: "xOffset" | "yOffset" | "width" | "height",
    event: Event
  ): void {
    const target = event.target as HTMLInputElement;
    const value = parseInt(target.value, 10);
    if (!isNaN(value) && value >= 0) {
      this.cropConfig[field] = value;
      this.emitChange();
    }
  }

  /**
   * Emit the current configuration to parent component
   */
  emitChange(): void {
    const config: TransformConfig = {};

    // Add rescale with padding if enabled
    if (this.rescaleEnabled) {
      config.rescaleWithPadding = { ...this.rescaleConfig };
    }

    // Add manual resize if present
    if (this.hasManualResize) {
      config.manualResize = { ...this.manualResizeConfig };
    }

    // Add crop if present
    if (this.hasCrop) {
      config.crop = { ...this.cropConfig };
    }

    this.configChange.emit(config);
  }

  /**
   * Sync internal values from input configuration
   */
  private syncFromInput(): void {
    if (this.config) {
      // Sync rescale with padding
      if (this.config.rescaleWithPadding) {
        this.rescaleEnabled = this.config.rescaleWithPadding.enabled;
        this.rescaleConfig = { ...this.config.rescaleWithPadding };
      } else {
        this.rescaleEnabled = true;
        this.rescaleConfig = { ...this.DEFAULT_RESCALE };
      }

      // Sync manual resize
      if (this.config.manualResize) {
        this.hasManualResize = true;
        this.manualResizeConfig = { ...this.config.manualResize };
      } else {
        this.hasManualResize = false;
        this.manualResizeConfig = { ...this.DEFAULT_MANUAL_RESIZE };
      }

      // Sync crop
      if (this.config.crop) {
        this.hasCrop = true;
        this.cropConfig = { ...this.config.crop };
      } else {
        this.hasCrop = false;
        this.cropConfig = { ...this.DEFAULT_CROP };
      }
    }
  }
}
