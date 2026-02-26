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
import {
  AUGMENTATION_TYPES,
  AugmentationConfig,
  BlurConfig,
  GaussianBlurConfig,
  HorizontalFlipConfig,
  HueSaturationValueConfig,
  MotionBlurConfig,
  RandomAugmentConfig,
  RandomBrightnessConfig,
  RandomContrastConfig,
  RandomRotateConfig,
  VerticalFlipConfig,
} from "app/models/landingai/augmentation-config.model";

// Dialog Components
import {
  BlurDialogComponent,
  GaussianBlurDialogComponent,
  HorizontalFlipDialogComponent,
  HueSaturationDialogComponent,
  MotionBlurDialogComponent,
  RandomAugmentDialogComponent,
  RandomBrightnessDialogComponent,
  RandomContrastDialogComponent,
  RandomRotateDialogComponent,
  VerticalFlipDialogComponent,
} from "app/state/landingai/ai-training";

/**
 * Augmentation item display configuration
 */
interface AugmentationItem {
  type: string;
  label: string;
  icon: string;
  tooltip: string;
  isDefault: boolean;
}

/**
 * AugmentationsConfigComponent
 *
 * Component for configuring data augmentation options including:
 * - Default augmentations (Horizontal Flip, Random Augment)
 * - Additional augmentations (Random Brightness, Blur, etc.)
 *
 * Validates: Requirements 10.1, 10.2, 10.3, 11.1, 11.2, 11.3, 11.4, 11.5
 */
@Component({
  selector: "app-augmentations-config",
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
  templateUrl: "./augmentations-config.component.html",
  styleUrls: ["./augmentations-config.component.scss"],
})
export class AugmentationsConfigComponent implements OnInit, OnChanges {
  /**
   * Current augmentation configuration
   */
  @Input() config: AugmentationConfig = {};

  /**
   * Emits when configuration values change
   */
  @Output() configChange = new EventEmitter<AugmentationConfig>();
  /**
   * Available augmentation types that can be added
   * Requirement 11.3: Support adding various augmentation types
   */
  readonly AUGMENTATION_TYPES = AUGMENTATION_TYPES;
  /**
   * Augmentation item configurations
   */
  readonly augmentationItems: Record<string, AugmentationItem> = {
    horizontalFlip: {
      type: "horizontalFlip",
      label: "Horizontal Flip",
      icon: "flip",
      tooltip: "Randomly flip images horizontally with a specified probability",
      isDefault: true,
    },
    randomAugment: {
      type: "randomAugment",
      label: "Random Augment",
      icon: "auto_fix_high",
      tooltip:
        "Apply a random combination of augmentations with specified magnitude",
      isDefault: true,
    },
    randomBrightness: {
      type: "randomBrightness",
      label: "Random Brightness",
      icon: "brightness_6",
      tooltip: "Randomly adjust image brightness within specified limits",
      isDefault: false,
    },
    blur: {
      type: "blur",
      label: "Blur",
      icon: "blur_on",
      tooltip: "Apply blur effect to images",
      isDefault: false,
    },
    motionBlur: {
      type: "motionBlur",
      label: "Motion Blur",
      icon: "motion_photos_on",
      tooltip: "Apply motion blur effect to simulate camera movement",
      isDefault: false,
    },
    gaussianBlur: {
      type: "gaussianBlur",
      label: "Gaussian Blur",
      icon: "blur_circular",
      tooltip: "Apply Gaussian blur with configurable kernel size and sigma",
      isDefault: false,
    },
    hueSaturationValue: {
      type: "hueSaturationValue",
      label: "Hue Saturation Value",
      icon: "palette",
      tooltip:
        "Randomly adjust hue, saturation, and value (brightness) of images",
      isDefault: false,
    },
    randomContrast: {
      type: "randomContrast",
      label: "Random Contrast",
      icon: "contrast",
      tooltip: "Randomly adjust image contrast within specified limits",
      isDefault: false,
    },
    verticalFlip: {
      type: "verticalFlip",
      label: "Vertical Flip",
      icon: "flip_camera_android",
      tooltip: "Randomly flip images vertically with a specified probability",
      isDefault: false,
    },
    randomRotate: {
      type: "randomRotate",
      label: "Random Rotate",
      icon: "rotate_right",
      tooltip: "Randomly rotate images within specified angle limits",
      isDefault: false,
    },
  };
  /**
   * Default configurations for each augmentation type
   */
  readonly defaultConfigs: Record<string, any> = {
    horizontalFlip: { probability: 0.5 } as HorizontalFlipConfig,
    randomAugment: { numTransforms: 2, magnitude: 9 } as RandomAugmentConfig,
    randomBrightness: { limit: 0.2 } as RandomBrightnessConfig,
    blur: { blurLimit: 7 } as BlurConfig,
    motionBlur: { blurLimit: 7 } as MotionBlurConfig,
    gaussianBlur: { blurLimit: 7, sigma: 0 } as GaussianBlurConfig,
    hueSaturationValue: {
      hueShiftLimit: 20,
      saturationShiftLimit: 30,
      valueShiftLimit: 20,
    } as HueSaturationValueConfig,
    randomContrast: { limit: 0.2 } as RandomContrastConfig,
    verticalFlip: { probability: 0.5 } as VerticalFlipConfig,
    randomRotate: { limit: 90, borderMode: "reflect" } as RandomRotateConfig,
  };
  /** List of active augmentation types */
  activeAugmentations: string[] = [];
  /** Track hover state for showing edit buttons */
  hoveredAugmentation: string | null = null;

  constructor(private dialog: MatDialog) {}

  /**
   * Get available augmentation types that can be added
   */
  get availableAugmentations(): { value: string; label: string }[] {
    return AUGMENTATION_TYPES.filter((aug) =>
      this.canAddAugmentation(aug.value)
    ) as { value: string; label: string }[];
  }

  ngOnInit(): void {
    this.syncFromInput();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["config"] && !changes["config"].firstChange) {
      this.syncFromInput();
    }
  }

  /**
   * Add a new augmentation
   * Requirement 11.1, 11.4: Add augmentation and open configuration dialog
   */
  addAugmentation(type: string): void {
    if (!this.activeAugmentations.includes(type)) {
      this.activeAugmentations.push(type);

      // Set default config for the new augmentation
      (this.config as any)[type] = { ...this.defaultConfigs[type] };

      this.emitChange();

      // Open dialog for configuration
      this.openAugmentationDialog(type);
    }
  }

  /**
   * Remove an augmentation
   * Requirement 11.5: Allow removing added augmentation items
   */
  removeAugmentation(type: string): void {
    const index = this.activeAugmentations.indexOf(type);
    if (index > -1) {
      this.activeAugmentations.splice(index, 1);
      delete (this.config as any)[type];
      this.emitChange();
    }
  }

  /**
   * Open the configuration dialog for an augmentation type
   * Requirement 10.3: Display Edit button on hover
   * Requirement 10.4, 10.5: Open appropriate dialog on Edit click
   */
  openAugmentationDialog(type: string): void {
    let dialogRef: any;
    const currentConfig =
      (this.config as any)[type] || this.defaultConfigs[type];

    switch (type) {
      case "horizontalFlip":
        dialogRef = this.dialog.open(HorizontalFlipDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "randomAugment":
        dialogRef = this.dialog.open(RandomAugmentDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "randomBrightness":
        dialogRef = this.dialog.open(RandomBrightnessDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "blur":
        dialogRef = this.dialog.open(BlurDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "motionBlur":
        dialogRef = this.dialog.open(MotionBlurDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "gaussianBlur":
        dialogRef = this.dialog.open(GaussianBlurDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "hueSaturationValue":
        dialogRef = this.dialog.open(HueSaturationDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "randomContrast":
        dialogRef = this.dialog.open(RandomContrastDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "verticalFlip":
        dialogRef = this.dialog.open(VerticalFlipDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
      case "randomRotate":
        dialogRef = this.dialog.open(RandomRotateDialogComponent, {
          width: "450px",
          data: { config: { ...currentConfig } },
        });
        break;
    }

    if (dialogRef) {
      dialogRef.afterClosed().subscribe((result: any) => {
        if (result) {
          (this.config as any)[type] = result;
          this.emitChange();
        }
      });
    }
  }

  /**
   * Emit the current configuration to parent component
   */
  emitChange(): void {
    this.configChange.emit({ ...this.config });
  }

  /**
   * Get the display value for an augmentation
   */
  getAugmentationDisplayValue(type: string): string {
    const config = (this.config as any)[type];
    if (!config) return "";

    switch (type) {
      case "horizontalFlip":
      case "verticalFlip":
        return `p=${config.probability}`;
      case "randomAugment":
        return `n=${config.numTransforms}, m=${config.magnitude}`;
      case "randomBrightness":
      case "randomContrast":
        return `limit=${config.limit}`;
      case "blur":
      case "motionBlur":
        return `limit=${config.blurLimit}`;
      case "gaussianBlur":
        return `limit=${config.blurLimit}, σ=${config.sigma}`;
      case "hueSaturationValue":
        return `h=${config.hueShiftLimit}, s=${config.saturationShiftLimit}, v=${config.valueShiftLimit}`;
      case "randomRotate":
        return `±${config.limit}°`;
      default:
        return "";
    }
  }

  /**
   * Check if an augmentation type can be added (not already active)
   */
  canAddAugmentation(type: string): boolean {
    return !this.activeAugmentations.includes(type);
  }

  /**
   * Check if an augmentation is a default one (cannot be removed)
   */
  isDefaultAugmentation(type: string): boolean {
    return this.augmentationItems[type].isDefault || false;
  }

  /**
   * Track by function for ngFor
   */
  trackByType(index: number, type: string): string {
    return type;
  }

  /**
   * Sync internal state from input configuration
   */
  private syncFromInput(): void {
    this.activeAugmentations = [];

    if (this.config) {
      // Check each augmentation type
      if (this.config.horizontalFlip)
        this.activeAugmentations.push("horizontalFlip");
      if (this.config.randomAugment)
        this.activeAugmentations.push("randomAugment");
      if (this.config.randomBrightness)
        this.activeAugmentations.push("randomBrightness");
      if (this.config.blur) this.activeAugmentations.push("blur");
      if (this.config.motionBlur) this.activeAugmentations.push("motionBlur");
      if (this.config.gaussianBlur)
        this.activeAugmentations.push("gaussianBlur");
      if (this.config.hueSaturationValue)
        this.activeAugmentations.push("hueSaturationValue");
      if (this.config.randomContrast)
        this.activeAugmentations.push("randomContrast");
      if (this.config.verticalFlip)
        this.activeAugmentations.push("verticalFlip");
      if (this.config.randomRotate)
        this.activeAugmentations.push("randomRotate");
    }

    // If no augmentations, add defaults
    // Requirement 10.1, 10.2: Display default augmentation items
    if (this.activeAugmentations.length === 0) {
      this.activeAugmentations = ["horizontalFlip", "randomAugment"];
      this.config = {
        horizontalFlip: { ...this.defaultConfigs["horizontalFlip"] },
        randomAugment: { ...this.defaultConfigs["randomAugment"] },
      };
    }
  }
}
