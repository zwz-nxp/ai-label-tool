/**
 * Training Record Data Model
 * Represents training session metadata and configuration
 * Requirements: 6.1, 8.1, 9.2, 9.3, 9.4, 10.2, 10.3, 11.2, 12.2, 12.3
 */

export interface TrainingRecord {
  id: number;
  projectId: number;
  status: string;
  modelAlias: string;
  trackId: string | null;
  epochs: number | null;
  modelSize: string | null;
  transformParam: string | null;
  modelParam: string | null;
  creditConsumption: string | null;
  trainingCount: number;
  devCount: number;
  testCount: number;
  startedAt: Date | null;
  completedAt: Date | null;
  createdBy: string;
}

/**
 * Split distribution for labeled images
 * Requirements: 9.5
 */
export interface SplitDistribution {
  train: number;
  dev: number;
  test: number;
  unassigned: number;
  total: number;
}

/**
 * Calculate split distribution including unassigned count
 * Requirements: 9.5
 * @param trainingRecord Training record with split counts
 * @param totalImageCount Total image count from model
 * @returns Split distribution object
 */
export function calculateSplitDistribution(
  trainingRecord: TrainingRecord,
  totalImageCount: number
): SplitDistribution {
  const train = trainingRecord.trainingCount || 0;
  const dev = trainingRecord.devCount || 0;
  const test = trainingRecord.testCount || 0;
  const assigned = train + dev + test;
  const unassigned = Math.max(0, totalImageCount - assigned);

  return {
    train,
    dev,
    test,
    unassigned,
    total: totalImageCount,
  };
}
