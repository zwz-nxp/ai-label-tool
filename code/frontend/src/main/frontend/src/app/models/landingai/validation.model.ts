/**
 * Validation Models
 *
 * Defines validation interfaces and error messages for training configuration.
 *
 * Requirements 21.1, 21.2, 29.1, 29.2, 29.3, 29.4, 29.5, 29.6:
 * - THE System SHALL validate all configuration parameters
 * - THE System SHALL display error messages for invalid fields
 * - THE System SHALL validate that Epoch value is between 1 and 100
 * - THE System SHALL validate that split distribution percentages sum to 100
 * - THE System SHALL validate that all required fields are filled
 * - THE System SHALL validate that numeric inputs contain valid numbers
 * - THE System SHALL display inline error messages below invalid fields
 * - THE System SHALL disable the Next/Start Training button when validation fails
 */

import {
  ModelConfig,
  TrainingRequest,
} from "app/models/landingai/training-config.model";
import { SplitDistribution } from "app/models/landingai/split-distribution.model";
import {
  EPOCH_CONFIG,
  isValidEpoch,
} from "app/models/landingai/hyperparameters.model";

/**
 * Validation error for a specific field
 */
export interface ValidationError {
  /** Field name that has the error */
  field: string;
  /** Error message to display */
  message: string;
}

/**
 * Validation result containing all errors
 */
export interface ValidationResult {
  /** Whether the validation passed */
  isValid: boolean;
  /** List of validation errors */
  errors: ValidationError[];
}

/**
 * Validation error messages
 * Requirement 29.5: THE System SHALL display inline error messages below the invalid fields
 */
export const VALIDATION_MESSAGES = {
  epochs: {
    required: "Epoch 值是必填项",
    min: `Epoch 值不能小于 ${EPOCH_CONFIG.MIN}`,
    max: `Epoch 值不能大于 ${EPOCH_CONFIG.MAX}`,
    invalid: "Epoch 值必须是有效的整数",
  },
  distribution: {
    sumNotHundred: "训练集、验证集和测试集的比例之和必须等于 100%",
    negativeValue: "分布比例不能为负数",
    invalidValue: "分布比例必须是有效的数字",
  },
  modelSize: {
    required: "请选择模型尺寸",
  },
  modelAlias: {
    required: "模型别名是必填项",
  },
  projectId: {
    required: "项目 ID 是必填项",
  },
  modelConfigs: {
    required: "至少需要一个模型配置",
  },
} as const;

/**
 * Validates the split distribution
 * Requirement 29.2: THE System SHALL validate that split distribution percentages sum to 100
 *
 * @param distribution - The split distribution to validate
 * @returns Validation result with any errors
 */
export function validateDistribution(
  distribution: SplitDistribution
): ValidationResult {
  const errors: ValidationError[] = [];

  // Check for negative values
  if (distribution.train < 0 || distribution.dev < 0 || distribution.test < 0) {
    errors.push({
      field: "distribution",
      message: VALIDATION_MESSAGES.distribution.negativeValue,
    });
  }

  // Check for invalid numbers
  if (
    isNaN(distribution.train) ||
    isNaN(distribution.dev) ||
    isNaN(distribution.test)
  ) {
    errors.push({
      field: "distribution",
      message: VALIDATION_MESSAGES.distribution.invalidValue,
    });
  }

  // Check sum equals 100
  const sum = distribution.train + distribution.dev + distribution.test;
  if (sum !== 100) {
    errors.push({
      field: "distribution",
      message: VALIDATION_MESSAGES.distribution.sumNotHundred,
    });
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validates a single model configuration
 * Requirements 29.1, 29.3, 29.4:
 * - THE System SHALL validate that Epoch value is between 1 and 100
 * - THE System SHALL validate that all required fields are filled
 * - THE System SHALL validate that numeric inputs contain valid numbers
 *
 * @param config - The model configuration to validate
 * @param index - The index of the model configuration (for error messages)
 * @returns Validation result with any errors
 */
export function validateModelConfig(
  config: ModelConfig,
  index: number = 0
): ValidationResult {
  const errors: ValidationError[] = [];
  const prefix = index > 0 ? `模型 ${index + 1}: ` : "";

  // Validate model alias
  if (!config.modelAlias || config.modelAlias.trim() === "") {
    errors.push({
      field: `modelConfigs[${index}].modelAlias`,
      message: prefix + VALIDATION_MESSAGES.modelAlias.required,
    });
  }

  // Validate epochs
  if (config.epochs === undefined || config.epochs === null) {
    errors.push({
      field: `modelConfigs[${index}].epochs`,
      message: prefix + VALIDATION_MESSAGES.epochs.required,
    });
  } else if (isNaN(config.epochs)) {
    errors.push({
      field: `modelConfigs[${index}].epochs`,
      message: prefix + VALIDATION_MESSAGES.epochs.invalid,
    });
  } else if (!isValidEpoch(config.epochs)) {
    if (config.epochs < EPOCH_CONFIG.MIN) {
      errors.push({
        field: `modelConfigs[${index}].epochs`,
        message: prefix + VALIDATION_MESSAGES.epochs.min,
      });
    } else if (config.epochs > EPOCH_CONFIG.MAX) {
      errors.push({
        field: `modelConfigs[${index}].epochs`,
        message: prefix + VALIDATION_MESSAGES.epochs.max,
      });
    }
  }

  // Validate model size
  if (!config.modelSize || config.modelSize.trim() === "") {
    errors.push({
      field: `modelConfigs[${index}].modelSize`,
      message: prefix + VALIDATION_MESSAGES.modelSize.required,
    });
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validates the entire training request
 * Requirement 21.1: WHEN a user clicks Start Training, THE System SHALL validate all configuration parameters
 *
 * @param request - The training request to validate
 * @returns Validation result with any errors
 */
export function validateTrainingRequest(
  request: TrainingRequest | null
): ValidationResult {
  const errors: ValidationError[] = [];

  if (!request) {
    errors.push({
      field: "request",
      message: "训练请求无效",
    });
    return { isValid: false, errors };
  }

  // Validate project ID
  if (!request.projectId || request.projectId <= 0) {
    errors.push({
      field: "projectId",
      message: VALIDATION_MESSAGES.projectId.required,
    });
  }

  // Validate model configs exist
  if (!request.modelConfigs || request.modelConfigs.length === 0) {
    errors.push({
      field: "modelConfigs",
      message: VALIDATION_MESSAGES.modelConfigs.required,
    });
    return { isValid: false, errors };
  }

  // Validate each model config
  request.modelConfigs.forEach((config: ModelConfig, index: number) => {
    const configResult = validateModelConfig(config, index);
    errors.push(...configResult.errors);
  });

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validates the complete training form (distribution + training request)
 * Combines all validation checks for the training configuration
 *
 * @param distribution - The split distribution
 * @param request - The training request
 * @returns Validation result with all errors
 */
export function validateTrainingForm(
  distribution: SplitDistribution,
  request: TrainingRequest | null
): ValidationResult {
  const errors: ValidationError[] = [];

  // Validate distribution
  const distributionResult = validateDistribution(distribution);
  errors.push(...distributionResult.errors);

  // Validate training request
  const requestResult = validateTrainingRequest(request);
  errors.push(...requestResult.errors);

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Gets error message for a specific field
 *
 * @param errors - List of validation errors
 * @param field - Field name to get error for
 * @returns Error message or null if no error
 */
export function getFieldError(
  errors: ValidationError[],
  field: string
): string | null {
  const error = errors.find((e) => e.field === field);
  return error ? error.message : null;
}

/**
 * Checks if a specific field has an error
 *
 * @param errors - List of validation errors
 * @param field - Field name to check
 * @returns True if field has an error
 */
export function hasFieldError(
  errors: ValidationError[],
  field: string
): boolean {
  return errors.some((e) => e.field === field);
}
