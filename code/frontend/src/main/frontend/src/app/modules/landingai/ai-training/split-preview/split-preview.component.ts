import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { CommonModule } from "@angular/common";

// Angular Material Modules
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatIconModule } from "@angular/material/icon";

// Models
import {
  BarSegment,
  ClassSplitData,
  SPLIT_TYPE_CONFIG,
  SplitClassData,
  SplitPreview,
  SplitType,
} from "app/state/landingai/ai-training";

/**
 * SplitPreviewComponent
 *
 * Component for displaying a visual preview of data split distribution.
 * Shows color bars representing the distribution of images across splits
 * and classes, with toggle between "By class" and "By Split" views.
 *
 * Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6
 * - Displays title "Preview your split ({count} images)" (4.1)
 * - Provides toggle button group with "By class" and "By Split" options (4.2)
 * - Displays each class with its train/dev/test distribution when "By class" selected (4.3)
 * - Displays each split with its class distribution when "By Split" selected (4.4)
 * - Displays color legend showing mapping between colors and classes (4.5)
 * - Automatically updates preview when split data changes (4.6)
 *
 * Validates: Requirements 28.1, 28.2, 28.3, 28.4, 28.5
 * - Renders horizontal color bars for split distribution (28.1)
 * - Uses distinct colors for Train (green), Dev (blue), Test (orange) (28.2)
 * - Dynamically assigns colors to different classes (28.3)
 * - Displays segment width proportional to percentage value (28.4)
 * - Displays legend mapping colors to their meanings (28.5)
 */
@Component({
  selector: "app-split-preview",
  standalone: true,
  imports: [
    CommonModule,
    MatButtonToggleModule,
    MatTooltipModule,
    MatIconModule,
  ],
  templateUrl: "./split-preview.component.html",
  styleUrls: ["./split-preview.component.scss"],
})
export class SplitPreviewComponent implements OnChanges {
  /**
   * Split preview data containing distribution information
   * Requirement 4.6: Automatically update when split data changes
   */
  @Input() splitPreview: SplitPreview | null = null;

  /**
   * Total number of images in the dataset
   * Requirement 4.1: Display title with total image count
   */
  @Input() totalImages: number = 0;

  /**
   * Current view mode: 'byClass' or 'bySplit'
   * Requirement 4.2: Toggle between "By class" and "By Split" views
   */
  viewMode: "byClass" | "bySplit" = "byClass";

  /**
   * Calculated bar segments for visualization
   * Requirement 28.4: Segment width proportional to percentage value
   */
  barSegments: BarSegment[] = [];

  /**
   * Split type configuration for colors and labels
   * Requirement 28.2: Distinct colors for Train/Dev/Test
   */
  readonly splitTypeConfig = SPLIT_TYPE_CONFIG;

  /**
   * Default class colors for dynamic assignment
   * Requirement 28.3: Dynamically assign colors to different classes
   */
  private readonly defaultClassColors: string[] = [
    "#e91e63", // Pink
    "#9c27b0", // Purple
    "#673ab7", // Deep Purple
    "#3f51b5", // Indigo
    "#00bcd4", // Cyan
    "#009688", // Teal
    "#8bc34a", // Light Green
    "#cddc39", // Lime
    "#ffc107", // Amber
    "#ff5722", // Deep Orange
    "#795548", // Brown
    "#607d8b", // Blue Grey
  ];

  /**
   * Map to store class name to color assignments
   */
  private classColorMap: Map<string, string> = new Map();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["splitPreview"] || changes["totalImages"]) {
      this.calculateBarSegments();
    }
  }

  /**
   * Toggle between "By class" and "By Split" view modes
   * Requirement 4.2: Toggle button group with "By class" and "By Split" options
   *
   * @param mode The view mode to switch to
   */
  toggleView(mode: "byClass" | "bySplit"): void {
    this.viewMode = mode;
    this.calculateBarSegments();
  }

  /**
   * Calculate bar segments based on current view mode and data
   * Requirement 28.4: Segment width proportional to percentage value
   */
  calculateBarSegments(): void {
    if (!this.splitPreview) {
      this.barSegments = [];
      return;
    }

    if (this.viewMode === "byClass") {
      this.calculateByClassSegments();
    } else {
      this.calculateBySplitSegments();
    }
  }

  /**
   * Get color for a class name
   * Requirement 28.3: Dynamically assign colors to different classes
   *
   * @param className The name of the class
   * @returns The color assigned to the class
   */
  getClassColor(className: string): string {
    // First check if class has a predefined color in the data
    if (this.splitPreview?.byClass) {
      const classData = this.splitPreview.byClass.find(
        (c) => c.className === className
      );
      if (classData?.classColor) {
        return classData.classColor;
      }
    }

    // Check if we already assigned a color to this class
    if (this.classColorMap.has(className)) {
      return this.classColorMap.get(className)!;
    }

    // Assign a new color from the default palette
    const colorIndex = this.classColorMap.size % this.defaultClassColors.length;
    const color = this.defaultClassColors[colorIndex];
    this.classColorMap.set(className, color);
    return color;
  }

  /**
   * Get split type color
   * Requirement 28.2: Distinct colors for Train (green), Dev (blue), Test (orange)
   *
   * @param splitType The type of split
   * @returns The color for the split type
   */
  getSplitColor(splitType: SplitType): string {
    return this.splitTypeConfig[splitType]?.color || "#9e9e9e";
  }

  /**
   * Get split type label
   *
   * @param splitType The type of split
   * @returns The label for the split type
   */
  getSplitLabel(splitType: SplitType): string {
    return this.splitTypeConfig[splitType]?.label || splitType;
  }

  /**
   * Calculate percentage for a count relative to total
   * Requirement 28.4: Segment width proportional to percentage value
   *
   * @param count The count value
   * @param total The total value
   * @returns The percentage (0-100)
   */
  calculatePercentage(count: number, total: number): number {
    if (total === 0) return 0;
    return (count / total) * 100;
  }

  /**
   * Get total count for a class (sum of all splits)
   *
   * @param classData The class split data
   * @returns The total count for the class
   */
  getClassTotal(classData: ClassSplitData): number {
    return (
      classData.train + classData.dev + classData.test + classData.unassigned
    );
  }

  /**
   * Get total count for a split (sum of all classes)
   *
   * @param splitData The split class data
   * @returns The total count for the split
   */
  getSplitTotal(splitData: SplitClassData): number {
    return splitData.classes.reduce((sum, cls) => sum + cls.count, 0);
  }

  /**
   * Get bar segments for a class in "By class" view
   * Requirement 4.3: Display each class with its train/dev/test distribution
   *
   * @param classData The class split data
   * @returns Array of bar segments for the class
   */
  getClassBarSegments(classData: ClassSplitData): BarSegment[] {
    const total = this.getClassTotal(classData);
    if (total === 0) return [];

    const segments: BarSegment[] = [];
    const splitTypes: SplitType[] = ["train", "dev", "test", "unassigned"];

    for (const splitType of splitTypes) {
      const count = classData[splitType];
      if (count > 0) {
        segments.push({
          label: this.getSplitLabel(splitType),
          color: this.getSplitColor(splitType),
          percentage: this.calculatePercentage(count, total),
          count: count,
        });
      }
    }

    return segments;
  }

  /**
   * Get bar segments for a split in "By Split" view
   * Requirement 4.4: Display each split with its class distribution
   *
   * @param splitData The split class data
   * @returns Array of bar segments for the split
   */
  getSplitBarSegments(splitData: SplitClassData): BarSegment[] {
    const total = this.getSplitTotal(splitData);
    if (total === 0) return [];

    return splitData.classes
      .filter((cls) => cls.count > 0)
      .map((cls) => ({
        label: cls.className,
        color: cls.classColor || this.getClassColor(cls.className),
        percentage: this.calculatePercentage(cls.count, total),
        count: cls.count,
      }));
  }

  /**
   * Get unique classes for legend display
   * Requirement 4.5: Display color legend showing mapping between colors and classes
   *
   * @returns Array of unique class names and colors
   */
  getUniqueClasses(): { name: string; color: string }[] {
    if (!this.splitPreview?.byClass) return [];

    return this.splitPreview.byClass.map((classData) => ({
      name: classData.className,
      color: classData.classColor || this.getClassColor(classData.className),
    }));
  }

  /**
   * Get split types for legend display in "By class" view
   * Requirement 28.5: Display legend mapping colors to their meanings
   *
   * @returns Array of split types with labels and colors
   */
  getSplitTypesForLegend(): {
    type: SplitType;
    label: string;
    color: string;
  }[] {
    const types: SplitType[] = ["train", "dev", "test", "unassigned"];
    return types.map((type) => ({
      type,
      label: this.splitTypeConfig[type].label,
      color: this.splitTypeConfig[type].color,
    }));
  }

  /**
   * Check if there is any data to display
   *
   * @returns true if there is data to display
   */
  hasData(): boolean {
    return (
      this.splitPreview !== null &&
      (this.splitPreview.byClass.length > 0 ||
        this.splitPreview.bySplit.length > 0)
    );
  }

  /**
   * Get tooltip text for a bar segment
   *
   * @param segment The bar segment
   * @returns Tooltip text
   */
  getSegmentTooltip(segment: BarSegment): string {
    return `${segment.label}: ${segment.count} images (${segment.percentage.toFixed(1)}%)`;
  }

  /**
   * Calculate segments for "By class" view
   * Requirement 4.3: Display each class with its train/dev/test distribution
   */
  private calculateByClassSegments(): void {
    // In "By class" view, we show each class as a row with split distribution
    // This method prepares data for the template to render
    // The actual rendering is done in the template using byClass data
  }

  /**
   * Calculate segments for "By Split" view
   * Requirement 4.4: Display each split with its class distribution
   */
  private calculateBySplitSegments(): void {
    // In "By Split" view, we show each split as a row with class distribution
    // This method prepares data for the template to render
    // The actual rendering is done in the template using bySplit data
  }
}
