/**
 * Model Parameter Configuration TypeScript Models
 * Validates: Requirements 2.3, 3.2, 4.2
 */

/**
 * Model type enum matching backend validation
 */
export type ModelType = "Object Detection" | "Classification" | "Segmentation";

/**
 * Complete model parameter interface matching backend DTO
 */
export interface ModelParam {
  id: number;
  locationId: number;
  locationName?: string;
  modelName: string;
  modelType: ModelType;
  parameters: string; // JSON string
  createdAt: Date;
  createdBy: string;
}

/**
 * Request interface for creating a new model parameter
 */
export interface ModelParamCreateRequest {
  modelName: string;
  modelType: ModelType;
  parameters: string; // JSON string
}

/**
 * Request interface for updating an existing model parameter
 */
export interface ModelParamUpdateRequest {
  modelName: string;
  modelType: ModelType;
  parameters: string; // JSON string
}

/**
 * Filter interface for model parameter state management
 */
export interface ModelParamFilters {
  modelType?: ModelType;
  searchTerm?: string;
}
