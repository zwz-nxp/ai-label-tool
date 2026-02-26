/**
 * Adjust Threshold 相關的 TypeScript interfaces
 */

/**
 * Adjust Threshold Dialog 的輸入資料
 */
export interface AdjustThresholdDialogData {
  modelId: number;
  projectId: number;
  currentThreshold: number;
  evaluationSet: "train" | "dev" | "test";
  modelAlias: string;
}

/**
 * Threshold 調整結果
 */
export interface ThresholdAdjustmentResult {
  success: boolean;
  newModelId?: number;
  error?: string;
}

/**
 * Confusion Matrix 資料結構
 */
export interface ConfusionMatrixData {
  /** 類別名稱列表（用於 GT labels，包含 "No label"） */
  classNames: string[];
  /** Prediction 類別名稱列表（包含 "No prediction"） */
  predictionClassNames: string[];
  /** 混淆矩陣 (rows: ground truth, columns: predictions) */
  matrix: number[][];
  /** 每個類別的 Precision */
  precisionByClass: number[];
  /** 每個類別的 Recall */
  recallByClass: number[];
  /** 總體 Precision (micro-averaged) */
  overallPrecision: number;
  /** 總體 Recall (micro-averaged) */
  overallRecall: number;
  /** 總體 F1 Score (micro-averaged) */
  overallF1: number;
}

/**
 * Prediction Label (從 backend 取得)
 */
export interface PredictionLabel {
  id: number;
  imageId: number;
  classId: number;
  className: string;
  position: string;
  confidenceRate: number; // 0-100
  createdAt: string;
  createdBy: string;
}

/**
 * Image Label (Ground Truth)
 */
export interface ImageLabel {
  id: number;
  imageId: number;
  classId: number;
  className: string;
  position: string;
  createdAt: string;
  createdBy: string;
}

/**
 * Generate Model Request (發送到 backend)
 */
export interface GenerateModelRequest {
  sourceModelId: number;
  newThreshold: number;
  recalculatedMetrics: RecalculatedMetrics;
}

/**
 * Recalculated Metrics
 */
export interface RecalculatedMetrics {
  trainF1: number;
  trainPrecision: number;
  trainRecall: number;
  devF1: number;
  devPrecision: number;
  devRecall: number;
  testF1: number;
  testPrecision: number;
  testRecall: number;
}

/**
 * Generate Model Response (從 backend 接收)
 */
export interface GenerateModelResponse {
  newModelId: number;
  modelAlias: string;
  confidenceThreshold: number;
}

/**
 * 用於計算混淆矩陣的 Label 分組
 */
export interface LabelsByImage {
  [imageId: number]: {
    groundTruth: ImageLabel[];
    predictions: PredictionLabel[];
  };
}
