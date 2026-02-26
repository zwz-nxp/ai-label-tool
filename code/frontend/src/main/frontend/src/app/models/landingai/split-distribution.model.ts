/**
 * Split Distribution Models
 *
 * Defines data split distribution and preview interfaces.
 *
 * Requirements 23.5, 23.6:
 * - THE System SHALL define SplitDistribution model with train, dev, and test percentage fields
 * - THE System SHALL define SplitPreview model with class-based and split-based distribution data
 */

/**
 * Split distribution percentages
 * Requirement 3.4: THE System SHALL set default ratio to 70% Train / 15% Dev / 15% Test
 */
export interface SplitDistribution {
  /** Training set percentage (0-100) */
  train: number;
  /** Development/validation set percentage (0-100) */
  dev: number;
  /** Test set percentage (0-100) */
  test: number;
}

/**
 * Default split distribution values
 */
export const DEFAULT_SPLIT_DISTRIBUTION: SplitDistribution = {
  train: 70,
  dev: 15,
  test: 15,
};

/**
 * Split preview data containing distribution information
 * Requirement 4.1: THE Split_Preview_Component SHALL display a title "Preview your split ({count} images)"
 */
export interface SplitPreview {
  /** Total number of images in the dataset */
  totalImages: number;
  /** Number of images without assigned split */
  unassignedCount: number;
  /** Distribution data grouped by class */
  byClass: ClassSplitData[];
  /** Distribution data grouped by split type */
  bySplit: SplitClassData[];
}

/**
 * Split distribution data for a single class
 * Requirement 4.3: WHEN "By class" is selected, THE System SHALL display each class with its train/dev/test distribution
 */
export interface ClassSplitData {
  /** Name of the class */
  className: string;
  /** Color assigned to the class for visualization */
  classColor: string;
  /** Number of images in training set */
  train: number;
  /** Number of images in development set */
  dev: number;
  /** Number of images in test set */
  test: number;
  /** Number of unassigned images */
  unassigned: number;
}

/**
 * Class distribution data for a single split type
 * Requirement 4.4: WHEN "By Split" is selected, THE System SHALL display each split with its class distribution
 */
export interface SplitClassData {
  /** Type of split */
  splitType: SplitType;
  /** Classes and their counts within this split */
  classes: SplitClassCount[];
}

/**
 * Class count within a split
 */
export interface SplitClassCount {
  /** Name of the class */
  className: string;
  /** Color assigned to the class */
  classColor: string;
  /** Number of images of this class in the split */
  count: number;
}

/**
 * Available split types
 */
export type SplitType = "train" | "dev" | "test" | "unassigned";

/**
 * Split type display configuration
 * Requirement 28.2: THE System SHALL use distinct colors for Train (green), Dev (blue), and Test (orange) segments
 */
export const SPLIT_TYPE_CONFIG: Record<
  SplitType,
  { label: string; color: string }
> = {
  train: { label: "Train", color: "#4caf50" }, // Green
  dev: { label: "Dev", color: "#2196f3" }, // Blue
  test: { label: "Test", color: "#ff9800" }, // Orange
  unassigned: { label: "Unassigned", color: "#9e9e9e" }, // Gray
};

/**
 * Bar segment for visualization
 */
export interface BarSegment {
  /** Label for the segment */
  label: string;
  /** Color of the segment */
  color: string;
  /** Percentage width (0-100) */
  percentage: number;
  /** Actual count */
  count: number;
}
