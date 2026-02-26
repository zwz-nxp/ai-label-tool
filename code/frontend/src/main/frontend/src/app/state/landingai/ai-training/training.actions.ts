/**
 * Training Actions
 *
 * Defines all NgRx actions for the training state management.
 *
 * Requirement 25.2:
 * - THE System SHALL define actions for loading snapshots, loading split preview,
 *   assigning split, updating distribution, managing model configs, and starting training
 */

import { createAction, props } from "@ngrx/store";
import {
  ModelConfig,
  SplitDistribution,
  SplitPreview,
  TrainingRequest,
} from "app/state/landingai/ai-training";
import {
  ProjectClass,
  Snapshot,
} from "app/state/landingai/ai-training/training.state";

// ============================================================================
// Initialization Actions
// ============================================================================

/**
 * Initialize training state with project ID
 */
export const initializeTraining = createAction(
  "[Training] Initialize",
  props<{ projectId: number }>()
);

/**
 * Reset training state to initial values
 */
export const resetTrainingState = createAction("[Training] Reset State");

// ============================================================================
// Snapshot Actions
// ============================================================================

/**
 * Load available snapshots for a project
 */
export const loadSnapshots = createAction(
  "[Training] Load Snapshots",
  props<{ projectId: number }>()
);

/**
 * Snapshots loaded successfully
 */
export const loadSnapshotsSuccess = createAction(
  "[Training] Load Snapshots Success",
  props<{ snapshots: Snapshot[] }>()
);

/**
 * Failed to load snapshots
 */
export const loadSnapshotsFailure = createAction(
  "[Training] Load Snapshots Failure",
  props<{ error: string }>()
);

/**
 * Select a snapshot for training
 * Requirement 1.3: WHEN a user selects a different snapshot, THE System SHALL update the split preview data accordingly
 */
export const selectSnapshot = createAction(
  "[Training] Select Snapshot",
  props<{ snapshotId: number | null }>()
);

// ============================================================================
// Split Preview Actions
// ============================================================================

/**
 * Load split preview data
 */
export const loadSplitPreview = createAction(
  "[Training] Load Split Preview",
  props<{ projectId: number; snapshotId?: number }>()
);

/**
 * Split preview loaded successfully
 */
export const loadSplitPreviewSuccess = createAction(
  "[Training] Load Split Preview Success",
  props<{ splitPreview: SplitPreview }>()
);

/**
 * Failed to load split preview
 */
export const loadSplitPreviewFailure = createAction(
  "[Training] Load Split Preview Failure",
  props<{ error: string }>()
);

// ============================================================================
// Split Assignment Actions
// ============================================================================

/**
 * Assign split to unassigned images
 * Requirement 2.3: WHEN a user clicks the Assign split button, THE System SHALL automatically assign unassigned images
 */
export const assignSplit = createAction(
  "[Training] Assign Split",
  props<{ projectId: number; distribution: SplitDistribution }>()
);

/**
 * Split assignment completed successfully
 */
export const assignSplitSuccess = createAction(
  "[Training] Assign Split Success"
);

/**
 * Failed to assign split
 */
export const assignSplitFailure = createAction(
  "[Training] Assign Split Failure",
  props<{ error: string }>()
);

// ============================================================================
// Distribution Actions
// ============================================================================

/**
 * Update split distribution percentages
 * Requirement 3.6: WHEN distribution changes, THE System SHALL emit the updated values
 */
export const updateDistribution = createAction(
  "[Training] Update Distribution",
  props<{ distribution: SplitDistribution }>()
);

// ============================================================================
// Model Config Actions
// ============================================================================

/**
 * Add a new model configuration
 * Requirement 21.4: THE System SHALL support multiple model configurations in a single training request
 */
export const addModelConfig = createAction("[Training] Add Model Config");

/**
 * Remove a model configuration by index
 */
export const removeModelConfig = createAction(
  "[Training] Remove Model Config",
  props<{ index: number }>()
);

/**
 * Update a model configuration at a specific index
 */
export const updateModelConfig = createAction(
  "[Training] Update Model Config",
  props<{ index: number; config: Partial<ModelConfig> }>()
);

/**
 * Update epochs for a model configuration
 * Requirement 5.5: WHEN a user changes the Epoch value, THE System SHALL synchronize both controls
 */
export const updateModelEpochs = createAction(
  "[Training] Update Model Epochs",
  props<{ index: number; epochs: number }>()
);

/**
 * Update model size for a model configuration
 * Requirement 6.5: WHEN a user selects a different model size, THE System SHALL update the model configuration
 */
export const updateModelSize = createAction(
  "[Training] Update Model Size",
  props<{ index: number; modelSize: string }>()
);

/**
 * Update transforms for a model configuration
 */
export const updateModelTransforms = createAction(
  "[Training] Update Model Transforms",
  props<{ index: number; transforms: ModelConfig["transforms"] }>()
);

/**
 * Update augmentations for a model configuration
 */
export const updateModelAugmentations = createAction(
  "[Training] Update Model Augmentations",
  props<{ index: number; augmentations: ModelConfig["augmentations"] }>()
);

// ============================================================================
// Navigation Actions
// ============================================================================

/**
 * Navigate to next step
 * Requirement 20.3: WHEN a user clicks Next, THE System SHALL proceed to the next configuration step
 */
export const nextStep = createAction("[Training] Next Step");

/**
 * Navigate to previous step
 * Requirement 20.2: WHEN a user clicks Back, THE System SHALL return to the previous configuration step
 */
export const previousStep = createAction("[Training] Previous Step");

/**
 * Set current step directly
 */
export const setStep = createAction(
  "[Training] Set Step",
  props<{ step: number }>()
);

// ============================================================================
// Training Actions
// ============================================================================

/**
 * Start training with current configuration
 * Requirement 21.3: WHEN validation passes, THE System SHALL send a training request to the backend API
 */
export const startTraining = createAction(
  "[Training] Start Training",
  props<{ request: TrainingRequest }>()
);

/**
 * Training started successfully
 * Requirement 21.6: WHEN training starts successfully, THE System SHALL display a success message
 */
export const startTrainingSuccess = createAction(
  "[Training] Start Training Success",
  props<{ trainingIds: number[] }>()
);

/**
 * Failed to start training
 * Requirement 21.7: IF training fails to start, THEN THE System SHALL display an error message
 */
export const startTrainingFailure = createAction(
  "[Training] Start Training Failure",
  props<{ error: string }>()
);

// ============================================================================
// Project Classes Actions
// ============================================================================

/**
 * Load project classes
 */
export const loadProjectClasses = createAction(
  "[Training] Load Project Classes",
  props<{ projectId: number }>()
);

/**
 * Project classes loaded successfully
 */
export const loadProjectClassesSuccess = createAction(
  "[Training] Load Project Classes Success",
  props<{ projectClasses: ProjectClass[] }>()
);

/**
 * Failed to load project classes
 */
export const loadProjectClassesFailure = createAction(
  "[Training] Load Project Classes Failure",
  props<{ error: string }>()
);

// ============================================================================
// Error Actions
// ============================================================================

/**
 * Clear current error
 */
export const clearError = createAction("[Training] Clear Error");
