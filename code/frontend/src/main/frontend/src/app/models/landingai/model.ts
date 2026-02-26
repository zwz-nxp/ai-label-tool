import { TrainingRecord } from "./training-record";

export interface Model {
  id: number; // Corresponds to backend Long id
  projectId: number; // Corresponds to backend projectId
  trainingRecordId?: number; // Training record ID for loading training details
  trainingRecord?: TrainingRecord; // ManyToOne relationship
  modelAlias?: string; // Corresponds to backend modelAlias (displayed as modelName)
  trackId?: string; // Corresponds to backend trackId
  modelVersion?: string; // Corresponds to backend modelVersion
  status?: string;
  createdBy?: string; // Corresponds to backend createdBy (displayed as creator)
  isFavorite?: boolean; // Corresponds to backend isFavorite
  createdAt?: Date; // Corresponds to backend createdAt (displayed as createdDate)

  // Performance metrics (0-100 range)
  trainingF1Rate?: number;
  trainingPrecisionRate?: number;
  trainingRecallRate?: number;
  devF1Rate?: number;
  devPrecisionRate?: number;
  devRecallRate?: number;
  testF1Rate?: number;
  testPrecisionRate?: number;
  testRecallRate?: number;

  // Correct rates from ConfidentialReport (0-100 range)
  trainingCorrectRate?: number;
  devCorrectRate?: number;
  testCorrectRate?: number;
  confidenceThreshold?: number; // 0-100 range

  // Additional backend fields
  imageCount?: number;
  labelCount?: number;
}

// Frontend display DTO
export interface ModelDisplayDto {
  id: number;
  modelName: string; // From modelAlias
  creator?: string; // From createdBy
  trainMetric: number; // From trainingCorrectRate
  devMetric: number; // From devCorrectRate
  testMetric: number | null; // From testCorrectRate
  confidenceThreshold: number; // From confidenceThreshold (converted to 0-1 range)
  isFavorite: boolean;
  createdDate: Date; // From createdAt
  modelFullName?: string; // From modelAlias (same as modelName)
  version?: number; // Parsed from modelVersion or default to 1
  trackId?: string; // From trackId
}

export interface SearchFilters {
  searchTerm: string;
  showFavoritesOnly: boolean;
  projectId?: number;
}

export interface TableColumn {
  key: keyof ModelDisplayDto;
  label: string;
  sortable: boolean;
  width: string;
  align: "left" | "center" | "right";
}

// Conversion function: Convert backend Model to frontend display DTO
export function mapModelToDisplayDto(model: Model): ModelDisplayDto {
  // Parse version from modelVersion string (e.g., "v1" -> 1, "1.0" -> 1)
  let version = 1; // Default version
  if (model.modelVersion) {
    const versionMatch = model.modelVersion.match(/\d+/);
    if (versionMatch) {
      version = parseInt(versionMatch[0], 10);
    }
  }

  return {
    id: model.id,
    modelName: model.modelAlias || "",
    creator: model.createdBy,
    // Use correct rates from ConfidentialReport (0-100 range)
    trainMetric: model.trainingCorrectRate || 0,
    devMetric: model.devCorrectRate || 0,
    testMetric: model.testCorrectRate || null,
    // Confidence threshold from ConfidentialReport (0-100 range, display as 0.00-1.00)
    confidenceThreshold: model.confidenceThreshold || 0,
    isFavorite: model.isFavorite || false,
    createdDate: model.createdAt || new Date(),
    // Additional fields for Test Model feature
    modelFullName: model.modelAlias || "",
    version: version,
    trackId: model.trackId || "",
  };
}

/**
 * Format metric as percentage display
 * Implements requirements 1.3, 7.2: Consistently format percentage values
 */
export function formatMetricAsPercentage(value: number | null): string {
  if (value === null || value === undefined) {
    return "--";
  }
  return `${Math.round(value)}%`;
}

/**
 * Format confidence threshold to two decimal places
 * Implements requirements 1.4, 7.4: Precisely display confidence threshold values
 */
export function formatConfidenceThreshold(value: number): string {
  if (value === null || value === undefined) {
    return "N/A";
  }

  // Ensure value is within valid range (0-1)
  if (value < 0 || value > 1) {
    console.warn(
      `Invalid confidence threshold value: ${value}. Expected range: 0-1`
    );
    return "Invalid";
  }

  // Format to two decimal places
  return value.toFixed(2);
}
