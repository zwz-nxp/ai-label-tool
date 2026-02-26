/**
 * Transform Configuration Models
 *
 * Defines image transformation configuration interfaces.
 *
 * Requirement 23.3:
 * - THE System SHALL define TransformConfig model with rescaleWithPadding, crop, and manualResize fields
 */

/**
 * Main transform configuration containing all transformation options
 */
export interface TransformConfig {
  /** Rescale with padding configuration */
  rescaleWithPadding?: RescaleWithPaddingConfig;
  /** Crop configuration */
  crop?: CropConfig;
  /** Manual resize configuration */
  manualResize?: ManualResizeConfig;
}

/**
 * Rescale with padding configuration
 * Requirement 7.1: THE Transforms_Config_Component SHALL display a "Rescale with padding" configuration section
 */
export interface RescaleWithPaddingConfig {
  /** Whether rescale with padding is enabled */
  enabled: boolean;
  /** Target width in pixels */
  width: number;
  /** Target height in pixels */
  height: number;
}

/**
 * Crop configuration
 * Requirement 9.1: WHEN the Crop dialog opens, THE System SHALL display X offset, Y offset, Width, and Height number inputs
 */
export interface CropConfig {
  /** X offset from the left edge */
  xOffset: number;
  /** Y offset from the top edge */
  yOffset: number;
  /** Crop width in pixels */
  width: number;
  /** Crop height in pixels */
  height: number;
}

/**
 * Manual resize configuration
 * Requirement 8.1: WHEN the Manual Resize dialog opens, THE System SHALL display Width and Height number inputs
 */
export interface ManualResizeConfig {
  /** Target width in pixels */
  width: number;
  /** Target height in pixels */
  height: number;
  /** Whether to maintain aspect ratio during resize */
  keepAspectRatio: boolean;
}
