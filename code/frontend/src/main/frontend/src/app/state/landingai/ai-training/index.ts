/**
 * Training State & Module - Public API
 *
 * This barrel file exports all NgRx state-related items and module exports
 * for the AI Training module.
 */

// ============================================================================
// STATE EXPORTS
// ============================================================================

// State definition and types
export {
  TrainingState,
  Snapshot,
  ProjectClass,
  initialTrainingState,
  createDefaultModelConfig,
  TRAINING_FEATURE_KEY,
} from "app/state/landingai/ai-training/training.state";

// Actions
export * as TrainingActions from "app/state/landingai/ai-training/training.actions";

// Effects
export { TrainingEffects } from "app/state/landingai/ai-training/training.effects";

// Individual action exports for convenience
export {
  initializeTraining,
  resetTrainingState,
  loadSnapshots,
  loadSnapshotsSuccess,
  loadSnapshotsFailure,
  selectSnapshot,
  loadSplitPreview,
  loadSplitPreviewSuccess,
  loadSplitPreviewFailure,
  assignSplit,
  assignSplitSuccess,
  assignSplitFailure,
  updateDistribution,
  addModelConfig,
  removeModelConfig,
  updateModelConfig,
  updateModelEpochs,
  updateModelSize,
  updateModelTransforms,
  updateModelAugmentations,
  nextStep,
  previousStep,
  setStep,
  startTraining,
  startTrainingSuccess,
  startTrainingFailure,
  loadProjectClasses,
  loadProjectClassesSuccess,
  loadProjectClassesFailure,
  clearError,
} from "app/state/landingai/ai-training/training.actions";

// Reducer
export { trainingReducer } from "app/state/landingai/ai-training/training.reducer";

// Selectors
export {
  selectTrainingState,
  selectSnapshots,
  selectSplitPreview,
  selectDistribution,
  selectModelConfigs,
  selectCurrentStep,
  selectLoading,
  selectError,
  selectProjectId,
  selectSelectedSnapshotId,
  selectProjectClasses,
  selectTotalImages,
  selectUnassignedCount,
  selectHasUnassignedImages,
  selectHasSplitConfiguration,
  selectLabeledImagesBelowMinimum,
  selectSelectedSnapshot,
  selectModelConfigCount,
  selectModelConfigByIndex,
  selectIsFirstStep,
  selectIsLastStep,
  selectIsDistributionValid,
  selectSplitPreviewByClass,
  selectSplitPreviewBySplit,
  selectCanProceedToNextStep,
  selectCanStartTraining,
  selectTrainingRequest,
  selectModelConfigValidationErrors,
  selectDistributionValidationErrors,
  selectAllValidationErrors,
  selectIsFormValid,
  selectCanProceedWithValidation,
} from "app/state/landingai/ai-training/training.selectors";

// ============================================================================
// MODULE EXPORTS (merged from modules/landingai/ai-training)
// ============================================================================

// Module
export * from "app/modules/landingai/ai-training/ai-training.module";

// Customer Training Component
export * from "app/modules/landingai/ai-training/customer-training/customer-training.component";

// ============================================================================
// COMPONENTS EXPORTS
// ============================================================================

// Split Distribution Component
export { SplitDistributionComponent } from "app/modules/landingai/ai-training/split-distribution/split-distribution.component";

// Split Preview Component
export { SplitPreviewComponent } from "app/modules/landingai/ai-training/split-preview/split-preview.component";

// Hyperparameters Config Component
export { HyperparametersConfigComponent } from "app/modules/landingai/ai-training/hyperparameters-config/hyperparameters-config.component";

// Transforms Config Component
export { TransformsConfigComponent } from "app/modules/landingai/ai-training/transforms-config/transforms-config.component";

// Augmentations Config Component
export { AugmentationsConfigComponent } from "app/modules/landingai/ai-training/augmentations-config/augmentations-config.component";

// ============================================================================
// MODELS EXPORTS
// ============================================================================

// Training configuration models
export {
  TrainingRequest,
  ModelConfig,
  MODEL_SIZES,
  ModelSizeValue,
} from "app/models/landingai/training-config.model";

// Transform configuration models
export {
  TransformConfig,
  RescaleWithPaddingConfig,
  CropConfig,
  ManualResizeConfig,
} from "app/models/landingai/transform-config.model";

// Augmentation configuration models
export {
  AugmentationConfig,
  HorizontalFlipConfig,
  RandomAugmentConfig,
  RandomBrightnessConfig,
  BlurConfig,
  MotionBlurConfig,
  GaussianBlurConfig,
  HueSaturationValueConfig,
  RandomContrastConfig,
  VerticalFlipConfig,
  RandomRotateConfig,
  AUGMENTATION_TYPES,
  AugmentationType,
  DEFAULT_AUGMENTATIONS,
} from "app/models/landingai/augmentation-config.model";

// Split distribution models
export {
  SplitDistribution,
  DEFAULT_SPLIT_DISTRIBUTION,
  SplitPreview,
  ClassSplitData,
  SplitClassData,
  SplitClassCount,
  SplitType,
  SPLIT_TYPE_CONFIG,
  BarSegment,
} from "app/models/landingai/split-distribution.model";

// Hyperparameters models
export {
  HyperparametersConfig,
  EPOCH_CONFIG,
  DEFAULT_HYPERPARAMETERS,
  clampEpoch,
  isValidEpoch,
} from "app/models/landingai/hyperparameters.model";

// Validation models
export {
  ValidationError,
  ValidationResult,
  VALIDATION_MESSAGES,
  validateDistribution,
  validateModelConfig,
  validateTrainingRequest,
  validateTrainingForm,
  getFieldError,
  hasFieldError,
} from "app/models/landingai/validation.model";

// ============================================================================
// SERVICES EXPORTS
// ============================================================================

export {
  TrainingService,
  TrainingRecord,
  TrainingStatus,
} from "app/services/landingai/training.service";
export { SplitService } from "app/services/landingai/split.service";
export {
  ErrorHandlerService,
  ErrorResponse,
} from "app/utils/services/landingai/ai-training-error-handler.service";

/**
 * Transform Config Dialogs - Public API
 *
 * This barrel file exports all dialog components for the transforms configuration.
 */

// Rescale Padding Dialog
export {
  RescalePaddingDialogComponent,
  RescalePaddingDialogData,
} from "app/modules/landingai/ai-training/transforms-config/dialogs/rescale-padding-dialog/rescale-padding-dialog.component";

// Manual Resize Dialog
export {
  ManualResizeDialogComponent,
  ManualResizeDialogData,
} from "app/modules/landingai/ai-training/transforms-config/dialogs/manual-resize-dialog/manual-resize-dialog.component";

// Crop Dialog
export {
  CropDialogComponent,
  CropDialogData,
} from "app/modules/landingai/ai-training/transforms-config/dialogs/crop-dialog/crop-dialog.component";

/**
 * Augmentation Config Dialogs - Public API
 *
 * This barrel file exports all dialog components for the augmentations configuration.
 */

// Horizontal Flip Dialog
export {
  HorizontalFlipDialogComponent,
  HorizontalFlipDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/horizontal-flip-dialog/horizontal-flip-dialog.component";

// Random Augment Dialog
export {
  RandomAugmentDialogComponent,
  RandomAugmentDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/random-augment-dialog/random-augment-dialog.component";

// Random Brightness Dialog
export {
  RandomBrightnessDialogComponent,
  RandomBrightnessDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/random-brightness-dialog/random-brightness-dialog.component";

// Blur Dialog
export {
  BlurDialogComponent,
  BlurDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/blur-dialog/blur-dialog.component";

// Motion Blur Dialog
export {
  MotionBlurDialogComponent,
  MotionBlurDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/motion-blur-dialog/motion-blur-dialog.component";

// Gaussian Blur Dialog
export {
  GaussianBlurDialogComponent,
  GaussianBlurDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/gaussian-blur-dialog/gaussian-blur-dialog.component";

// Hue Saturation Dialog
export {
  HueSaturationDialogComponent,
  HueSaturationDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/hue-saturation-dialog/hue-saturation-dialog.component";

// Random Contrast Dialog
export {
  RandomContrastDialogComponent,
  RandomContrastDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/random-contrast-dialog/random-contrast-dialog.component";

// Vertical Flip Dialog
export {
  VerticalFlipDialogComponent,
  VerticalFlipDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/vertical-flip-dialog/vertical-flip-dialog.component";

// Random Rotate Dialog
export {
  RandomRotateDialogComponent,
  RandomRotateDialogData,
} from "app/modules/landingai/ai-training/augmentations-config/dialogs/random-rotate-dialog/random-rotate-dialog.component";
