/**
 * Training Configuration Models
 *
 * Defines the core training request and model configuration interfaces.
 *
 * Requirements: 23.1, 23.2
 * - THE System SHALL define TrainingConfig model with projectId, snapshotId, and modelConfigs fields
 * - THE System SHALL define ModelConfig model with modelAlias, epochs, modelSize, transforms, and augmentations fields
 */

import { TransformConfig } from "app/models/landingai/transform-config.model";
import { AugmentationConfig } from "app/models/landingai/augmentation-config.model";

/**
 * Training request payload sent to the backend API
 */
export interface TrainingRequest {
  /** Project identifier */
  projectId: number;
  /** Optional snapshot identifier for versioned data */
  snapshotId?: number;
  /** Optional file path for training data */
  filePath?: string;
  /** Optional file name for training data */
  fileName?: string;
  /** Array of model configurations for training */
  modelConfigs?: ModelConfig[];
  /** Whether this is a custom training request */
  isCustomTraining?: boolean;
}

/**
 * Individual model configuration within a training request
 */
export interface ModelConfig {
  /** User-defined alias for the model */
  modelAlias: string;
  /** Training status */
  status?: string;
  /** Number of training epochs (1-100) */
  epochs: number;
  /** Model size selection */
  modelSize: string;
  /** Image transformation configuration */
  transforms: TransformConfig;
  /** Data augmentation configuration (used when modelParam is not provided) */
  augmentations?: AugmentationConfig;
  /** Raw JSON string from model parameter tree editor */
  modelParam?: string;
}

/**
 * Available model sizes for training
 * Requirement 6.2: THE System SHALL display three fixed options
 */
export const MODEL_SIZES = [
  { value: "RepPoints-[37M]", label: "RepPoints-[37M]" },
  { value: "RepPoints-[50M]", label: "RepPoints-[50M]" },
  { value: "RepPoints-[101M]", label: "RepPoints-[101M]" },
] as const;

/** Type for model size values */
export type ModelSizeValue = (typeof MODEL_SIZES)[number]["value"];
