/**
 * Training State Definition
 *
 * Defines the TrainingState interface and initial state for NgRx store.
 *
 * Requirement 25.1:
 * - THE System SHALL define training state with snapshots, splitPreview, distribution,
 *   modelConfigs, currentStep, loading, and error fields
 */

import {
  DEFAULT_SPLIT_DISTRIBUTION,
  SplitDistribution,
  SplitPreview,
} from "app/models/landingai/split-distribution.model";
import { ModelConfig } from "app/models/landingai/training-config.model";

/**
 * Snapshot model representing a data version
 */
export interface Snapshot {
  /** Unique identifier for the snapshot */
  id: number;
  /** Display name of the snapshot */
  name: string;
  /** Creation timestamp */
  createdAt: Date;
  /** Number of images in this snapshot */
  imageCount: number;
}

/**
 * Project class model
 */
export interface ProjectClass {
  /** Unique identifier for the class */
  id: number;
  /** Display name of the class */
  name: string;
  /** Color assigned to the class for visualization */
  color: string;
  /** Project this class belongs to */
  projectId: number;
}

/**
 * Training state interface
 * Contains all state needed for the training configuration workflow
 */
export interface TrainingState {
  /** Available data snapshots */
  snapshots: Snapshot[];
  /** Current split preview data */
  splitPreview: SplitPreview | null;
  /** Target split distribution percentages */
  distribution: SplitDistribution;
  /** Model configurations for training */
  modelConfigs: ModelConfig[];
  /** Current wizard step (1 = data setup, 2 = model config) */
  currentStep: number;
  /** Loading state for async operations */
  loading: boolean;
  /** Error message if any operation failed */
  error: string | null;
  /** Current project ID */
  projectId: number | null;
  /** Selected snapshot ID (null = current version) */
  selectedSnapshotId: number | null;
  /** Available project classes */
  projectClasses: ProjectClass[];
}

/**
 * Creates a default model configuration
 *
 * Requirement 5.2: THE System SHALL set default Epoch value to 40
 * Requirement 6.3: THE System SHALL set default Model Size to "RepPoints-[37M]"
 * Requirement 10.1, 10.2: Default augmentations include Horizontal Flip and Random Augment
 */
export function createDefaultModelConfig(index: number = 1): ModelConfig {
  return {
    modelAlias: `Model ${index}`,
    epochs: 40,
    modelSize: "RepPoints-[37M]",
    transforms: {},
    augmentations: {
      horizontalFlip: { probability: 0.5 },
      randomAugment: { numTransforms: 2, magnitude: 9 },
    },
  };
}

/**
 * Initial training state
 *
 * Requirement 3.4: THE System SHALL set default ratio to 70% Train / 15% Dev / 15% Test
 */
export const initialTrainingState: TrainingState = {
  snapshots: [],
  splitPreview: null,
  distribution: { ...DEFAULT_SPLIT_DISTRIBUTION },
  modelConfigs: [createDefaultModelConfig()],
  currentStep: 1,
  loading: false,
  error: null,
  projectId: null,
  selectedSnapshotId: null,
  projectClasses: [],
};

/**
 * Feature key for the training state in the store
 */
export const TRAINING_FEATURE_KEY = "training";
