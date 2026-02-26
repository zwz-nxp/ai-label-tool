/**
 * Training Selectors
 *
 * Defines NgRx selectors for accessing training state slices.
 *
 * Requirement 25.4:
 * - THE System SHALL define selectors for accessing state slices including
 *   selectUnassignedCount and selectTotalImages
 */

import { createFeatureSelector, createSelector } from "@ngrx/store";
import {
  TRAINING_FEATURE_KEY,
  TrainingState,
} from "app/state/landingai/ai-training/training.state";

// ============================================================================
// Feature Selector
// ============================================================================

/**
 * Feature selector for training state
 */
export const selectTrainingState =
  createFeatureSelector<TrainingState>(TRAINING_FEATURE_KEY);

// ============================================================================
// Basic State Selectors
// ============================================================================

/**
 * Select all snapshots
 */
export const selectSnapshots = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.snapshots
);

/**
 * Select split preview data
 */
export const selectSplitPreview = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.splitPreview
);

/**
 * Select current distribution
 */
export const selectDistribution = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.distribution
);

/**
 * Select all model configurations
 */
export const selectModelConfigs = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.modelConfigs
);

/**
 * Select current wizard step
 */
export const selectCurrentStep = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.currentStep
);

/**
 * Select loading state
 */
export const selectLoading = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.loading
);

/**
 * Select error message
 */
export const selectError = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.error
);

/**
 * Select project ID
 */
export const selectProjectId = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.projectId
);

/**
 * Select selected snapshot ID
 */
export const selectSelectedSnapshotId = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.selectedSnapshotId
);

/**
 * Select project classes
 */
export const selectProjectClasses = createSelector(
  selectTrainingState,
  (state: TrainingState) => state.projectClasses
);

// ============================================================================
// Derived Selectors
// ============================================================================

/**
 * Select total images count from split preview
 * Requirement 4.1: THE Split_Preview_Component SHALL display a title "Preview your split ({count} images)"
 */
export const selectTotalImages = createSelector(
  selectSplitPreview,
  (splitPreview) => splitPreview?.totalImages ?? 0
);

/**
 * Minimum number of labeled images required to start training
 */
const MINIMUM_LABELED_IMAGES = 10;

/**
 * Select whether labeled images count is below the minimum required for training
 */
export const selectLabeledImagesBelowMinimum = createSelector(
  selectTotalImages,
  (totalImages) => totalImages < MINIMUM_LABELED_IMAGES
);

/**
 * Select unassigned images count from split preview
 * Requirement 2.1: THE System SHALL display an "Assign split to {count} images" button
 */
export const selectUnassignedCount = createSelector(
  selectSplitPreview,
  (splitPreview) => splitPreview?.unassignedCount ?? 0
);

/**
 * Select whether there are unassigned images
 */
export const selectHasUnassignedImages = createSelector(
  selectUnassignedCount,
  (count) => count > 0
);

/**
 * Select whether project has split configuration
 * Returns true if any images have been assigned to train/dev/test splits
 */
export const selectHasSplitConfiguration = createSelector(
  selectSplitPreview,
  (splitPreview) => {
    if (!splitPreview || !splitPreview.byClass) {
      return false;
    }
    // Check if any class has images assigned to train, dev, or test
    return splitPreview.byClass.some(
      (classData) =>
        classData.train > 0 || classData.dev > 0 || classData.test > 0
    );
  }
);

/**
 * Select the currently selected snapshot
 */
export const selectSelectedSnapshot = createSelector(
  selectSnapshots,
  selectSelectedSnapshotId,
  (snapshots, selectedId) => {
    if (selectedId === null) {
      return null;
    }
    return snapshots.find((s) => s.id === selectedId) ?? null;
  }
);

/**
 * Select number of model configurations
 */
export const selectModelConfigCount = createSelector(
  selectModelConfigs,
  (configs) => configs.length
);

/**
 * Select a specific model configuration by index
 */
export const selectModelConfigByIndex = (index: number) =>
  createSelector(selectModelConfigs, (configs) => configs[index] ?? null);

/**
 * Select whether user is on the first step
 * Requirement 20.4: WHEN a user is on the first step, THE System SHALL disable or hide the Back button
 */
export const selectIsFirstStep = createSelector(
  selectCurrentStep,
  (step) => step === 1
);

/**
 * Select whether user is on the last step
 * Requirement 20.5: WHEN a user is on the last step, THE System SHALL display Start Training instead of Next
 */
export const selectIsLastStep = createSelector(
  selectCurrentStep,
  (step) => step === 2
);

/**
 * Select whether the distribution is valid (sums to 100)
 * Requirement 29.2: THE System SHALL validate that split distribution percentages sum to 100
 */
export const selectIsDistributionValid = createSelector(
  selectDistribution,
  (distribution) => {
    const sum = distribution.train + distribution.dev + distribution.test;
    return sum === 100;
  }
);

/**
 * Select split preview data grouped by class
 */
export const selectSplitPreviewByClass = createSelector(
  selectSplitPreview,
  (splitPreview) => splitPreview?.byClass ?? []
);

/**
 * Select split preview data grouped by split type
 */
export const selectSplitPreviewBySplit = createSelector(
  selectSplitPreview,
  (splitPreview) => splitPreview?.bySplit ?? []
);

/**
 * Select whether can navigate to next step
 * Basic validation - can be extended with more complex validation
 */
export const selectCanProceedToNextStep = createSelector(
  selectCurrentStep,
  selectIsDistributionValid,
  selectLoading,
  (step, isDistributionValid, loading) => {
    if (loading) {
      return false;
    }
    if (step === 1) {
      return isDistributionValid;
    }
    return true;
  }
);

/**
 * Select whether can start training
 * Requirement 29.6: THE System SHALL disable the Next/Start Training button when validation fails
 */
export const selectCanStartTraining = createSelector(
  selectIsLastStep,
  selectModelConfigs,
  selectLoading,
  selectProjectId,
  selectDistribution,
  (isLastStep, modelConfigs, loading, projectId, distribution) => {
    if (!isLastStep || loading || projectId === null) {
      return false;
    }
    // Validate distribution sums to 100
    const distributionSum =
      distribution.train + distribution.dev + distribution.test;
    if (distributionSum !== 100) {
      return false;
    }
    // Validate all model configs have valid epochs and required fields
    return modelConfigs.every(
      (config) =>
        config.epochs >= 1 &&
        config.epochs <= 100 &&
        config.modelSize &&
        config.modelSize.trim() !== "" &&
        config.modelAlias &&
        config.modelAlias.trim() !== ""
    );
  }
);

/**
 * Select training request payload
 * Constructs the request object from current state
 */
export const selectTrainingRequest = createSelector(
  selectProjectId,
  selectSelectedSnapshotId,
  selectModelConfigs,
  (projectId, snapshotId, modelConfigs) => {
    if (projectId === null) {
      return null;
    }
    return {
      projectId,
      snapshotId: snapshotId ?? undefined,
      modelConfigs,
    };
  }
);

// ============================================================================
// Validation Selectors
// ============================================================================

/**
 * Select validation errors for model configurations
 * Requirement 29.1: THE System SHALL validate that Epoch value is between 1 and 100
 * Requirement 29.3: THE System SHALL validate that all required fields are filled
 */
export const selectModelConfigValidationErrors = createSelector(
  selectModelConfigs,
  (modelConfigs) => {
    const errors: { field: string; message: string }[] = [];

    modelConfigs.forEach((config, index) => {
      const prefix = modelConfigs.length > 1 ? `模型 ${index + 1}: ` : "";

      // Validate epochs
      if (config.epochs === undefined || config.epochs === null) {
        errors.push({
          field: `modelConfigs[${index}].epochs`,
          message: prefix + "Epoch 值是必填项",
        });
      } else if (config.epochs < 1) {
        errors.push({
          field: `modelConfigs[${index}].epochs`,
          message: prefix + "Epoch 值不能小于 1",
        });
      } else if (config.epochs > 100) {
        errors.push({
          field: `modelConfigs[${index}].epochs`,
          message: prefix + "Epoch 值不能大于 100",
        });
      }

      // Validate model size
      if (!config.modelSize || config.modelSize.trim() === "") {
        errors.push({
          field: `modelConfigs[${index}].modelSize`,
          message: prefix + "请选择模型尺寸",
        });
      }

      // Validate model alias
      if (!config.modelAlias || config.modelAlias.trim() === "") {
        errors.push({
          field: `modelConfigs[${index}].modelAlias`,
          message: prefix + "模型别名是必填项",
        });
      }
    });

    return errors;
  }
);

/**
 * Select validation errors for distribution
 * Requirement 29.2: THE System SHALL validate that split distribution percentages sum to 100
 */
export const selectDistributionValidationErrors = createSelector(
  selectDistribution,
  (distribution) => {
    const errors: { field: string; message: string }[] = [];

    // Check for negative values
    if (
      distribution.train < 0 ||
      distribution.dev < 0 ||
      distribution.test < 0
    ) {
      errors.push({
        field: "distribution",
        message: "分布比例不能为负数",
      });
    }

    // Check sum equals 100
    const sum = distribution.train + distribution.dev + distribution.test;
    if (sum !== 100) {
      errors.push({
        field: "distribution",
        message: "训练集、验证集和测试集的比例之和必须等于 100%",
      });
    }

    return errors;
  }
);

/**
 * Select all validation errors
 * Combines model config and distribution validation errors
 */
export const selectAllValidationErrors = createSelector(
  selectModelConfigValidationErrors,
  selectDistributionValidationErrors,
  (modelErrors, distributionErrors) => [...distributionErrors, ...modelErrors]
);

/**
 * Select whether the form is valid
 * Requirement 29.6: THE System SHALL disable the Next/Start Training button when validation fails
 */
export const selectIsFormValid = createSelector(
  selectAllValidationErrors,
  (errors) => errors.length === 0
);

/**
 * Select whether can proceed to next step with validation
 * Validates step 1 requirements before allowing navigation
 */
export const selectCanProceedWithValidation = createSelector(
  selectCurrentStep,
  selectIsDistributionValid,
  selectLoading,
  (step, isDistributionValid, loading) => {
    if (loading) {
      return false;
    }
    if (step === 1) {
      return isDistributionValid;
    }
    return true;
  }
);
