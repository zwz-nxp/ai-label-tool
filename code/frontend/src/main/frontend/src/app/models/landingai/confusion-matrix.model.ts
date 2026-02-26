/**
 * Confusion Matrix Data Models
 * These interfaces match the backend DTOs for confusion matrix operations
 */

/**
 * Main confusion matrix data structure
 */
export interface ConfusionMatrixData {
  /** List of class information for Ground Truth axis (id, name, color) in display order */
  classes: ClassInfo[];
  /** List of class information for Prediction axis (includes "No prediction" at the end) */
  predictionClasses: ClassInfo[];
  /** N×N matrix of prediction counts */
  matrix: MatrixCell[][];
  /** Class-level metrics (Precision, Recall, TP, FP, FN) for each class */
  classMetrics: ClassMetrics[];
  /** Maximum count value in the matrix (for color scaling) */
  maxCount: number;
}

/**
 * Class information
 */
export interface ClassInfo {
  /** Class ID */
  id: number;
  /** Class name */
  name: string;
  /** Class color (hex format, e.g., "#FF5733") */
  color: string;
}

/**
 * Individual matrix cell
 */
export interface MatrixCell {
  /** Ground truth class ID */
  groundTruthClassId: number;
  /** Prediction class ID */
  predictionClassId: number;
  /** Number of images with this GT×Pred combination */
  count: number;
  /** Whether this is a diagonal cell (correct prediction) */
  isDiagonal: boolean;
}

/**
 * Class-level metrics
 */
export interface ClassMetrics {
  /** Class ID */
  classId: number;
  /** Precision (0-100) */
  precision: number;
  /** Recall (0-100) */
  recall: number;
  /** True positives count */
  truePositives: number;
  /** False positives count */
  falsePositives: number;
  /** False negatives count */
  falseNegatives: number;
}

/**
 * Cell selection for detail view
 */
export interface CellSelection {
  /** Ground truth class ID */
  gtClassId: number;
  /** Ground truth class name */
  gtClassName: string;
  /** Prediction class ID */
  predClassId: number;
  /** Prediction class name */
  predClassName: string;
  /** Number of images in this cell */
  count: number;
}

/**
 * Cell detail response
 */
export interface CellDetailResponse {
  /** Ground truth class ID */
  gtClassId: number;
  /** Ground truth class name */
  gtClassName: string;
  /** Prediction class ID */
  predClassId: number;
  /** Prediction class name */
  predClassName: string;
  /** Total count of label pairs (not unique images) */
  totalCount: number;
  /** List of images with labels */
  images: ImageWithLabels[];
}

/**
 * Image with labels for display
 */
export interface ImageWithLabels {
  /** Image ID */
  imageId: number;
  /** File name */
  fileName: string;
  /** File path */
  filePath: string;
  /** Ground truth labels */
  groundTruthLabels: LabelInfo[];
  /** Prediction labels */
  predictionLabels: LabelInfo[];
  /** Whether prediction is correct */
  isCorrect: boolean;
}

/**
 * Label information
 */
export interface LabelInfo {
  /** Label ID */
  id: number;
  /** Class ID */
  classId: number;
  /** Class name */
  className: string;
  /** Class color (hex format) */
  classColor: string;
  /** Position (JSON string for bounding box coordinates) */
  position: string;
  /** Confidence rate (0-100, null for ground truth) */
  confidenceRate: number | null;
  /** Annotation type */
  annotationType: "Ground truth" | "Prediction";
}

/**
 * View mode for confusion matrix component
 */
export type ViewMode = "matrix" | "detail" | "analyzeAll";

/**
 * Component state interface
 */
export interface ConfusionMatrixState {
  /** Model ID */
  modelId: number | null;
  /** Evaluation set (train, dev, test) */
  evaluationSet: "train" | "dev" | "test";
  /** Matrix data */
  matrixData: ConfusionMatrixData | null;
  /** Selected cell for detail view */
  selectedCell: CellSelection | null;
  /** Current view mode */
  viewMode: ViewMode;
  /** Loading state */
  loading: boolean;
  /** Error message */
  error: string | null;
}
