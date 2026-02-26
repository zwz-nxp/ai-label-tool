/**
 * Hyperparameters Configuration Models
 *
 * Defines hyperparameter configuration interfaces for model training.
 *
 * Requirements 5, 6:
 * - Epoch configuration with range 1-100 and default 40
 * - Model size selection
 */

/**
 * Hyperparameters configuration for model training
 */
export interface HyperparametersConfig {
  /** Number of training epochs */
  epochs: number;
  /** Selected model size */
  modelSize: string;
}

/**
 * Epoch configuration constants
 * Requirement 5.2: THE System SHALL set default Epoch value to 40
 * Requirement 5.3: THE System SHALL restrict Epoch range to 1-100
 */
export const EPOCH_CONFIG = {
  /** Minimum allowed epoch value */
  MIN: 1,
  /** Maximum allowed epoch value */
  MAX: 100,
  /** Default epoch value */
  DEFAULT: 40,
  /** Step size for slider */
  STEP: 1,
} as const;

/**
 * Default hyperparameters configuration
 * Requirement 5.2: THE System SHALL set default Epoch value to 40
 * Requirement 6.3: THE System SHALL set default Model Size to "RepPoints-[37M]"
 */
export const DEFAULT_HYPERPARAMETERS: HyperparametersConfig = {
  epochs: EPOCH_CONFIG.DEFAULT,
  modelSize: "RepPoints-[37M]",
};

/**
 * Clamps an epoch value to the valid range [1, 100]
 * Requirement 5.6: IF a user enters a value outside the valid range, THEN THE System SHALL clamp the value to the nearest valid boundary
 *
 * @param value - The input epoch value
 * @returns The clamped epoch value within valid range
 */
export function clampEpoch(value: number): number {
  if (value < EPOCH_CONFIG.MIN) {
    return EPOCH_CONFIG.MIN;
  }
  if (value > EPOCH_CONFIG.MAX) {
    return EPOCH_CONFIG.MAX;
  }
  return Math.round(value);
}

/**
 * Validates if an epoch value is within the valid range
 * Requirement 29.1: THE System SHALL validate that Epoch value is between 1 and 100
 *
 * @param value - The epoch value to validate
 * @returns True if the value is valid, false otherwise
 */
export function isValidEpoch(value: number): boolean {
  return (
    Number.isInteger(value) &&
    value >= EPOCH_CONFIG.MIN &&
    value <= EPOCH_CONFIG.MAX
  );
}
