/**
 * Confidential Report Data Model
 * Represents model performance metrics and confidence threshold
 * Requirements: 15.2, 16.2, 16.3, 16.4
 */

export interface ConfidentialReport {
  id: number;
  modelId: number;
  trainingCorrectRate: number | null;
  devCorrectRate: number | null;
  testCorrectRate: number | null;
  confidenceThreshold: number | null;
  createdAt: Date;
  createdBy: string;
}

/**
 * Performance metrics for a specific evaluation set
 * Requirements: 16.1
 */
export interface PerformanceMetrics {
  f1: number | null;
  precision: number | null;
  recall: number | null;
}

/**
 * Format confidence threshold with 2 decimal places
 * Requirements: 15.3
 * @param threshold Confidence threshold value (0-1 range)
 * @returns Formatted string with 2 decimal places
 */
export function formatConfidenceThreshold(threshold: number | null): string {
  if (threshold === null || threshold === undefined) {
    return "Not configured";
  }
  return threshold.toFixed(2);
}
