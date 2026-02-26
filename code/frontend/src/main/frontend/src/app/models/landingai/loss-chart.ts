/**
 * Loss Chart Data Model
 * Represents training loss data points for model training visualization
 * Requirements: 7.3, 7.7, 7.8, 7.9
 */

export interface LossChart {
  id: number;
  modelId: number;
  loss: number; // Backend: Integer, will be received as number in JSON
  createdAt: Date;
  createdBy: string;
}

/**
 * Chart data point with time interval from training start
 */
export interface ChartDataPoint {
  timestamp: Date; // Original timestamp from database
  timeFromStart: number; // Milliseconds from training start
  value: number; // loss or mAP value
}

/**
 * Chart.js compatible data structure
 */
export interface ChartData {
  labels: string[]; // X-axis labels in "XmYs" format
  datasets: ChartDataset[];
}

export interface ChartDataset {
  label: string;
  data: number[];
  borderColor: string;
  backgroundColor?: string;
  fill: boolean;
  tension?: number; // For smooth curves
}

/**
 * Time interval representation
 */
export interface TimeInterval {
  minutes: number;
  seconds: number;
}

/**
 * Calculate time interval between two timestamps
 * Requirements: 7.7, 7.9
 * @param startTime Training start time
 * @param currentTime Current data point time
 * @returns Time interval with minutes and seconds
 */
export function calculateTimeInterval(
  startTime: Date,
  currentTime: Date
): TimeInterval {
  const diffMs = currentTime.getTime() - startTime.getTime();
  const totalSeconds = Math.round(diffMs / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return { minutes, seconds };
}

/**
 * Format time interval as "XmYs"
 * Requirements: 7.8
 * @param interval Time interval object
 * @returns Formatted string like "5m30s"
 */
export function formatTimeInterval(interval: TimeInterval): string {
  return `${interval.minutes}m${interval.seconds}s`;
}

/**
 * Transform loss chart data to Chart.js format
 * Requirements: 7.3, 7.5, 7.7, 7.8, 7.9
 * @param lossData Array of loss chart data points
 * @param trainingStartTime Training start timestamp
 * @returns Chart.js compatible data structure
 */
export function transformLossChartData(
  lossData: LossChart[],
  trainingStartTime: Date
): ChartData {
  // Sort by createdAt to ensure chronological order
  const sortedData = [...lossData].sort(
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
        label: "Training Loss",
        data: sortedData.map((d) => d.loss),
        borderColor: "#1976D2",
        backgroundColor: "rgba(25, 118, 210, 0.1)",
        fill: true,
        tension: 0.4, // Smooth curve
      },
    ],
  };
}
