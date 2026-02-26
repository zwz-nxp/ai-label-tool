/**
 * Augmentation Configuration Models
 *
 * Defines data augmentation configuration interfaces for all supported augmentation types.
 *
 * Requirement 23.4:
 * - THE System SHALL define AugmentationConfig model with all supported augmentation type fields
 */

/**
 * Main augmentation configuration containing all augmentation options
 */
export interface AugmentationConfig {
  /** Horizontal flip augmentation */
  horizontalFlip?: HorizontalFlipConfig;
  /** Random augment configuration */
  randomAugment?: RandomAugmentConfig;
  /** Random brightness adjustment */
  randomBrightness?: RandomBrightnessConfig;
  /** Blur augmentation */
  blur?: BlurConfig;
  /** Motion blur augmentation */
  motionBlur?: MotionBlurConfig;
  /** Gaussian blur augmentation */
  gaussianBlur?: GaussianBlurConfig;
  /** Hue, saturation, value adjustment */
  hueSaturationValue?: HueSaturationValueConfig;
  /** Random contrast adjustment */
  randomContrast?: RandomContrastConfig;
  /** Vertical flip augmentation */
  verticalFlip?: VerticalFlipConfig;
  /** Random rotation augmentation */
  randomRotate?: RandomRotateConfig;
}

/**
 * Horizontal flip configuration
 * Requirement 10.4: WHEN a user clicks Edit on "Horizontal Flip", THE System SHALL open a dialog with Probability slider (0-1)
 */
export interface HorizontalFlipConfig {
  /** Probability of applying horizontal flip (0-1) */
  probability: number;
}

/**
 * Random augment configuration
 * Requirement 10.5: WHEN a user clicks Edit on "Random Augment", THE System SHALL open a dialog with Number of transforms input and Magnitude slider
 */
export interface RandomAugmentConfig {
  /** Number of transforms to apply */
  numTransforms: number;
  /** Magnitude of the transforms */
  magnitude: number;
}

/**
 * Random brightness configuration
 * Requirement 12.1: WHEN the Random Brightness dialog opens, THE System SHALL display a Limit slider with range -1 to 1
 */
export interface RandomBrightnessConfig {
  /** Brightness adjustment limit (-1 to 1) */
  limit: number;
}

/**
 * Blur configuration
 * Requirement 13.1: WHEN the Blur dialog opens, THE System SHALL display a Blur limit slider
 */
export interface BlurConfig {
  /** Maximum blur kernel size */
  blurLimit: number;
}

/**
 * Motion blur configuration
 * Requirement 14.1: WHEN the Motion Blur dialog opens, THE System SHALL display a Blur limit slider
 */
export interface MotionBlurConfig {
  /** Maximum motion blur kernel size */
  blurLimit: number;
}

/**
 * Gaussian blur configuration
 * Requirement 15.1: WHEN the Gaussian Blur dialog opens, THE System SHALL display Blur limit and Sigma sliders
 */
export interface GaussianBlurConfig {
  /** Maximum blur kernel size */
  blurLimit: number;
  /** Gaussian sigma value */
  sigma: number;
}

/**
 * Hue, saturation, value configuration
 * Requirement 16.1: WHEN the Hue Saturation Value dialog opens, THE System SHALL display Hue shift limit, Saturation shift limit, and Value shift limit sliders
 */
export interface HueSaturationValueConfig {
  /** Hue shift limit */
  hueShiftLimit: number;
  /** Saturation shift limit */
  saturationShiftLimit: number;
  /** Value (brightness) shift limit */
  valueShiftLimit: number;
}

/**
 * Random contrast configuration
 * Requirement 17.1: WHEN the Random Contrast dialog opens, THE System SHALL display a Limit slider
 */
export interface RandomContrastConfig {
  /** Contrast adjustment limit */
  limit: number;
}

/**
 * Vertical flip configuration
 * Requirement 18.1: WHEN the Vertical Flip dialog opens, THE System SHALL display a Probability slider (0-1)
 */
export interface VerticalFlipConfig {
  /** Probability of applying vertical flip (0-1) */
  probability: number;
}

/**
 * Random rotate configuration
 * Requirement 19.1: WHEN the Random Rotate dialog opens, THE System SHALL display a Limit slider for rotation angle
 */
export interface RandomRotateConfig {
  /** Maximum rotation angle in degrees */
  limit: number;
  /** Border handling mode */
  borderMode: string;
}

/**
 * Available augmentation types that can be added
 * Requirement 11.3: THE System SHALL support adding: Random Brightness, Blur, Motion Blur, Gaussian Blur, Hue Saturation Value, Random Contrast, Vertical Flip, Random Rotate
 */
export const AUGMENTATION_TYPES = [
  { value: "randomBrightness", label: "Random Brightness" },
  { value: "blur", label: "Blur" },
  { value: "motionBlur", label: "Motion Blur" },
  { value: "gaussianBlur", label: "Gaussian Blur" },
  { value: "hueSaturationValue", label: "Hue Saturation Value" },
  { value: "randomContrast", label: "Random Contrast" },
  { value: "verticalFlip", label: "Vertical Flip" },
  { value: "randomRotate", label: "Random Rotate" },
] as const;

/** Type for augmentation type values */
export type AugmentationType = (typeof AUGMENTATION_TYPES)[number]["value"];

/**
 * Default augmentation types that are always present
 * Requirement 10.1, 10.2: THE Augmentations_Config_Component SHALL display "Horizontal Flip" and "Random Augment" as default augmentation items
 */
export const DEFAULT_AUGMENTATIONS = [
  "horizontalFlip",
  "randomAugment",
] as const;
