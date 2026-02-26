/**
 * Data models for Test Model feature.
 *
 * These interfaces define the structure for image upload, prediction results,
 * and bounding box visualization.
 */

/**
 * Represents an uploaded image with its blob URL and metadata.
 */
export interface UploadedImage {
  /** Unique identifier (UUID) */
  id: string;
  /** Original file object */
  file: File;
  /** Blob URL for display */
  blobUrl: string;
  /** Thumbnail blob URL */
  thumbnailUrl: string;
  /** Upload timestamp */
  uploadedAt: Date;
}

/**
 * Prediction result for a single image from the API.
 */
export interface PredictionResult {
  /** Image filename that was tested */
  image: string;
  /** List of detected object predictions */
  predictions: Label[];
}

/**
 * Label data for a single detected object.
 * Coordinates are normalized (0-1 range).
 *
 * Note: API 使用 snake_case 命名 (class_id),
 * 但 TypeScript 介面使用 camelCase (classId)
 */
export interface Label {
  /** Confidence score (0-1) */
  confidence: number;
  /** Bounding box coordinates [xcenter, ycenter, width, height] normalized (0-1) */
  bbox: number[];
  /** Class identifier (API 欄位名稱: class_id) */
  classId?: number;
  /** Class identifier (snake_case from API) */
  class_id?: number;
  /** Class name (optional) */
  className?: string;
  /** Class name (snake_case from API) */
  class_name?: string;
}

/**
 * Bounding box for visualization with pixel coordinates.
 */
export interface BoundingBox {
  /** Pixel x-coordinate (top-left) */
  x: number;
  /** Pixel y-coordinate (top-left) */
  y: number;
  /** Pixel width */
  width: number;
  /** Pixel height */
  height: number;
  /** Class identifier */
  classId: number;
  /** Human-readable class name */
  className: string;
  /** Confidence score */
  confidence: number;
  /** Display color (e.g., '#FF5C9A') */
  color: string;
  /** Border width (default: 2) */
  strokeWidth: number;
  /** Fill opacity (default: 0.2 for 20%) */
  fillOpacity: number;
  /** Label text to display */
  labelText: string;
  /** Label x-coordinate */
  labelX: number;
  /** Label y-coordinate */
  labelY: number;
  /** Label background width */
  labelWidth: number;
  /** Label background height */
  labelHeight: number;
  /** Font size for label text */
  fontSize: number;
}

/**
 * Grouped predictions by class for accordion display.
 */
export interface GroupedPrediction {
  /** Class identifier */
  classId: number;
  /** Human-readable class name */
  className: string;
  /** Display color */
  color: string;
  /** Number of instances */
  count: number;
  /** Individual prediction instances */
  instances: PredictionInstance[];
}

/**
 * Individual prediction instance within a class group.
 */
export interface PredictionInstance {
  /** Instance index (1-based) */
  index: number;
  /** Pixel x-coordinate */
  x: number;
  /** Pixel y-coordinate */
  y: number;
  /** Pixel width */
  width: number;
  /** Pixel height */
  height: number;
  /** Confidence score */
  confidence: number;
}

/**
 * Request DTO for testing a model.
 */
export interface TestModelRequest {
  /** Model full name identifier */
  modelFullName: string;
  /** Model version number */
  version: number;
  /** Training track identifier */
  trackId: string;
  /** List of image URLs to test */
  imageUrls: string[];
  /** Confidence threshold (0-1) */
  confidenceThreshhold: number; // Note: API uses 'threshhold' spelling
}

/**
 * Response DTO from model testing API.
 * 欄位名稱使用 snake_case 以對應後端 API
 */
export interface TestModelResponse {
  /** Track ID */
  track_id?: string;
  /** Model full name */
  model_full_name?: string;
  /** Model version */
  model_version?: string;
  /** F1 score rate */
  f1_rate?: string;
  /** Precision rate */
  precision_rate?: string;
  /** Recall rate */
  recall_rate?: string;
  /** Training F1 rate */
  training_f1_rate?: string;
  /** Training precision rate */
  training_precision_rate?: string;
  /** Training recall rate */
  training_recall_rate?: string;
  /** Training correct rate */
  training_correct_rate?: string;
  /** Dev F1 rate */
  dev_f1_rate?: string;
  /** Dev precision rate */
  dev_precision_rate?: string;
  /** Dev recall rate */
  dev_recall_rate?: string;
  /** Dev correct rate */
  dev_correct_rate?: string;
  /** Test F1 rate */
  test_f1_rate?: string;
  /** Test precision rate */
  test_precision_rate?: string;
  /** Test recall rate */
  test_recall_rate?: string;
  /** Test correct rate */
  test_correct_rate?: string;
  /** Confidence threshold used */
  confidence_threshold?: string;
  /** Loss chart data */
  loss_chart?: any[];
  /** Validation chart data */
  validation_chart?: any[];
  /** List of prediction results per image */
  prediction_images?: PredictionResult[];
}
