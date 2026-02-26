/**
 * Training Reducer
 *
 * Implements state update logic for training state management.
 *
 * Requirement 25.4:
 * - THE System SHALL update state immutably in reducers
 */

import { createReducer, on } from "@ngrx/store";
import * as TrainingActions from "app/state/landingai/ai-training/training.actions";
import {
  createDefaultModelConfig,
  initialTrainingState,
  TrainingState,
} from "app/state/landingai/ai-training/training.state";
import { clampEpoch } from "app/state/landingai/ai-training";

/**
 * Maximum number of wizard steps
 */
const MAX_STEPS = 2;

/**
 * Minimum number of wizard steps
 */
const MIN_STEPS = 1;

/**
 * Training reducer
 * Handles all training-related state updates immutably
 */
export const trainingReducer = createReducer(
  initialTrainingState,

  // ============================================================================
  // Initialization
  // ============================================================================

  on(
    TrainingActions.initializeTraining,
    (state, { projectId }): TrainingState => ({
      ...state,
      projectId,
      loading: true,
      error: null,
    })
  ),

  on(
    TrainingActions.resetTrainingState,
    (): TrainingState => ({ ...initialTrainingState })
  ),

  // ============================================================================
  // Snapshots
  // ============================================================================

  on(
    TrainingActions.loadSnapshots,
    (state): TrainingState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    TrainingActions.loadSnapshotsSuccess,
    (state, { snapshots }): TrainingState => ({
      ...state,
      snapshots,
      loading: false,
    })
  ),

  on(
    TrainingActions.loadSnapshotsFailure,
    (state, { error }): TrainingState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  on(
    TrainingActions.selectSnapshot,
    (state, { snapshotId }): TrainingState => ({
      ...state,
      selectedSnapshotId: snapshotId,
    })
  ),

  // ============================================================================
  // Split Preview
  // ============================================================================

  on(
    TrainingActions.loadSplitPreview,
    (state): TrainingState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    TrainingActions.loadSplitPreviewSuccess,
    (state, { splitPreview }): TrainingState => ({
      ...state,
      splitPreview,
      loading: false,
    })
  ),

  on(
    TrainingActions.loadSplitPreviewFailure,
    (state, { error }): TrainingState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================================================
  // Split Assignment
  // ============================================================================

  on(
    TrainingActions.assignSplit,
    (state): TrainingState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    TrainingActions.assignSplitSuccess,
    (state): TrainingState => ({
      ...state,
      loading: false,
    })
  ),

  on(
    TrainingActions.assignSplitFailure,
    (state, { error }): TrainingState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================================================
  // Distribution
  // ============================================================================

  on(
    TrainingActions.updateDistribution,
    (state, { distribution }): TrainingState => ({
      ...state,
      distribution: { ...distribution },
    })
  ),

  // ============================================================================
  // Model Configs
  // ============================================================================

  on(
    TrainingActions.addModelConfig,
    (state): TrainingState => ({
      ...state,
      modelConfigs: [
        ...state.modelConfigs,
        createDefaultModelConfig(state.modelConfigs.length + 1),
      ],
    })
  ),

  on(TrainingActions.removeModelConfig, (state, { index }): TrainingState => {
    // Don't allow removing the last config
    if (state.modelConfigs.length <= 1) {
      return state;
    }
    return {
      ...state,
      modelConfigs: state.modelConfigs.filter((_, i) => i !== index),
    };
  }),

  on(
    TrainingActions.updateModelConfig,
    (state, { index, config }): TrainingState => ({
      ...state,
      modelConfigs: state.modelConfigs.map((existingConfig, i) =>
        i === index ? { ...existingConfig, ...config } : existingConfig
      ),
    })
  ),

  on(
    TrainingActions.updateModelEpochs,
    (state, { index, epochs }): TrainingState => ({
      ...state,
      modelConfigs: state.modelConfigs.map((config, i) =>
        i === index ? { ...config, epochs: clampEpoch(epochs) } : config
      ),
    })
  ),

  on(
    TrainingActions.updateModelSize,
    (state, { index, modelSize }): TrainingState => ({
      ...state,
      modelConfigs: state.modelConfigs.map((config, i) =>
        i === index ? { ...config, modelSize } : config
      ),
    })
  ),

  on(
    TrainingActions.updateModelTransforms,
    (state, { index, transforms }): TrainingState => ({
      ...state,
      modelConfigs: state.modelConfigs.map((config, i) =>
        i === index ? { ...config, transforms: { ...transforms } } : config
      ),
    })
  ),

  on(
    TrainingActions.updateModelAugmentations,
    (state, { index, augmentations }): TrainingState => ({
      ...state,
      modelConfigs: state.modelConfigs.map((config, i) =>
        i === index
          ? { ...config, augmentations: { ...augmentations } }
          : config
      ),
    })
  ),

  // ============================================================================
  // Navigation
  // ============================================================================

  on(
    TrainingActions.nextStep,
    (state): TrainingState => ({
      ...state,
      currentStep: Math.min(MAX_STEPS, state.currentStep + 1),
    })
  ),

  on(
    TrainingActions.previousStep,
    (state): TrainingState => ({
      ...state,
      currentStep: Math.max(MIN_STEPS, state.currentStep - 1),
    })
  ),

  on(
    TrainingActions.setStep,
    (state, { step }): TrainingState => ({
      ...state,
      currentStep: Math.max(MIN_STEPS, Math.min(MAX_STEPS, step)),
    })
  ),

  // ============================================================================
  // Training
  // ============================================================================

  on(
    TrainingActions.startTraining,
    (state): TrainingState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    TrainingActions.startTrainingSuccess,
    (state): TrainingState => ({
      ...state,
      loading: false,
    })
  ),

  on(
    TrainingActions.startTrainingFailure,
    (state, { error }): TrainingState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================================================
  // Project Classes
  // ============================================================================

  on(
    TrainingActions.loadProjectClasses,
    (state): TrainingState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    TrainingActions.loadProjectClassesSuccess,
    (state, { projectClasses }): TrainingState => ({
      ...state,
      projectClasses,
      loading: false,
    })
  ),

  on(
    TrainingActions.loadProjectClassesFailure,
    (state, { error }): TrainingState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================================================
  // Error Handling
  // ============================================================================

  on(
    TrainingActions.clearError,
    (state): TrainingState => ({
      ...state,
      error: null,
    })
  )
);
