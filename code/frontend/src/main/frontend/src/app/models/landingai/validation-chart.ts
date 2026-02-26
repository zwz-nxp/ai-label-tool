/**
 * Validation Chart Data Model
 * Represents validation mAP (mean Average Precision) data points for model training visualization
 * Requirements: 7.4, 7.6, 7.7, 7.8, 7.9
 */

import {
  ChartData,
  TimeInterval,
  calculateTimeInterval,
  formatTimeInterval,
} from "./loss-chart";

export interface ValidationChart {
  id: number;
  modelId: number;
  map: number; // Backend: BigDecimal (NUMERIC(5,4)), will be received as number in JSON (0-1 range)
  createdAt: Date;
  createdBy: string;
}

/**
 * Transform validation chart data to Chart.js format
 * Requirements: 7.4, 7.6, 7.7, 7.8, 7.9
 * @param validationData Array of validation chart data points
 * @param trainingStartTime Training start timestamp
 * @returns Chart.js compatible data structure
 */
export function transformValidationChartData(
  validationData: ValidationChart[],
  trainingStartTime: Date
): ChartData {
  // Sort by createdAt to ensure chronological order
  const sortedData = [...validationData].sort(
    (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  );

  // Calculate time intervals and format labels
  const labels = sortedData.map((dataPoint) => {
    const interval = calculateTimeInterval(
      trainingStartTime,
      new Date(dataPoint.createdAt)
    );
    return formatTimeInterval(interval);
  });

  return {
    labels: labels,
    datasets: [
      {
        label: "Validation mAP",
        data: sortedData.map((d) => d.map), // mAP values (0-1 range)
        borderColor: "#4CAF50",
        backgroundColor: "rgba(76, 175, 80, 0.1)",
        fill: true,
        tension: 0.4, // Smooth curve
      },
    ],
  };
}
