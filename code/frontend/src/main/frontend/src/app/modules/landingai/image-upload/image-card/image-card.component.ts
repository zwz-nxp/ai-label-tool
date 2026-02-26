import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
} from "@angular/core";
import { Image, ImageLabel } from "../../../../models/landingai/image";
import { ProjectType } from "../../../../models/landingai/project";
import { ProjectClass } from "../../../../models/landingai/project-class.model";

interface LabelPosition {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface ClassChangeEvent {
  imageId: number;
  classId: number | null;
  previousClassId: number | null;
}

@Component({
  selector: "app-image-card",
  standalone: false,
  templateUrl: "./image-card.component.html",
  styleUrls: ["./image-card.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageCardComponent {
  @Input() image!: Image;
  @Input() viewMode: "images" | "instances" = "images";
  @Input() projectType: ProjectType = "Object Detection";
  @Input() availableClasses: ProjectClass[] = [];
  @Input() showLabelDetails: boolean = true;
  @Input() selectMode: boolean = false;
  @Input() isSelected: boolean = false;

  @Output() cardClick = new EventEmitter<Image>();
  @Output() classChange = new EventEmitter<ClassChangeEvent>();
  @Output() selectionToggle = new EventEmitter<{
    imageId: number;
    selected: boolean;
  }>();

  // Cache for instance view styles to avoid recalculation
  private _cachedInstanceViewStyles: any = null;
  private _lastFocusedLabelId: number | null = null;

  /**
   * Calculate the position and size of the labels overlay to match the actual
   * rendered image area within the square container.
   *
   * When using object-fit: contain in a square container, a non-square image
   * (e.g. 1024x256) will be letterboxed. The overlay must match only the
   * rendered image area so that label percentages align correctly.
   */
  getOverlayStyles(): { [key: string]: string } {
    if (!this.image?.width || !this.image?.height) {
      return { top: "0", left: "0", width: "100%", height: "100%" };
    }

    const imgAspect = this.image.width / this.image.height;
    // Container is square (aspect-ratio: 1), so containerAspect = 1
    const containerAspect = 1;

    if (imgAspect > containerAspect) {
      // Image is wider than container → full width, reduced height, centered vertically
      const renderedHeightPercent = (containerAspect / imgAspect) * 100;
      const topOffset = (100 - renderedHeightPercent) / 2;
      return {
        left: "0",
        top: topOffset + "%",
        width: "100%",
        height: renderedHeightPercent + "%",
      };
    } else if (imgAspect < containerAspect) {
      // Image is taller than container → full height, reduced width, centered horizontally
      const renderedWidthPercent = (imgAspect / containerAspect) * 100;
      const leftOffset = (100 - renderedWidthPercent) / 2;
      return {
        top: "0",
        left: leftOffset + "%",
        width: renderedWidthPercent + "%",
        height: "100%",
      };
    } else {
      // Square image → overlay matches container exactly
      return { top: "0", left: "0", width: "100%", height: "100%" };
    }
  }

  /**
   * Handle card click
   */
  onCardClick(): void {
    if (this.selectMode) {
      // In select mode, toggle selection instead of navigating
      this.onCheckboxToggle();
    } else {
      // In view mode, navigate to labeling page
      this.cardClick.emit(this.image);
    }
  }

  /**
   * Handle checkbox toggle
   * Called when the Material checkbox change event fires
   */
  onCheckboxToggle(): void {
    this.selectionToggle.emit({
      imageId: this.image.id,
      selected: !this.isSelected,
    });
  }

  /**
   * Get image URL for display
   * Uses thumbnail if available, otherwise constructs URL from file path
   */
  getImageUrl(): string {
    if (this.image.thumbnailImage) {
      // If thumbnailImage is base64 encoded
      if (this.image.thumbnailImage.startsWith("data:image")) {
        return this.image.thumbnailImage;
      }
      // If thumbnailImage is a base64 string without prefix
      return `data:image/jpeg;base64,${this.image.thumbnailImage}`;
    }
    // Fallback to file URL
    return this.image.fileUrl || "";
  }

  /**
   * Parse position JSON and calculate overlay positions
   * Position format: JSON string with {x, y, width, height} as ratios (0-1)
   * - x: center X relative to image width
   * - y: center Y relative to image height
   * - width: rectangle width relative to image width
   * - height: rectangle height relative to image height
   * Example: {"type":"rectangle","x":0.29,"y":0.43,"width":0.035,"height":0.125}
   * Convert to top-left corner percentages for CSS positioning
   * Clamp values to ensure boxes stay within image boundaries (0-100%)
   */
  getLabelPosition(label: ImageLabel): LabelPosition {
    try {
      // Handle null position (valid for Classification projects)
      if (!label.position) {
        return { x: 0, y: 0, width: 0, height: 0 };
      }

      const position = JSON.parse(label.position);

      // Handle null or invalid parsed position
      if (!position || typeof position !== "object") {
        return { x: 0, y: 0, width: 0, height: 0 };
      }

      // Position values are ratios (0-1) with x,y being the center point
      // Convert center-based to top-left corner for CSS positioning
      const widthPercent = (position.width || 0) * 100;
      const heightPercent = (position.height || 0) * 100;
      const xPercent = (position.x || 0) * 100 - widthPercent / 2;
      const yPercent = (position.y || 0) * 100 - heightPercent / 2;

      return {
        x: Math.max(0, xPercent),
        y: Math.max(0, yPercent),
        width: widthPercent,
        height: heightPercent,
      };
    } catch (error) {
      console.error("Error parsing label position:", error);
      return { x: 0, y: 0, width: 0, height: 0 };
    }
  }

  /**
   * Get color code for label border
   * Supports both flat structure (from API DTO) and nested structure (from full entity)
   */
  getLabelColor(label: any): string {
    return label.colorCode || label.projectClass?.colorCode || "#00ff00";
  }

  /**
   * Get class name for label
   * Supports both flat structure (from API DTO) and nested structure (from full entity)
   */
  getLabelClassName(label: any): string {
    return label.className || label.projectClass?.className || "Unknown";
  }

  /**
   * Check if label is a prediction label (for dotted border styling)
   * Ground truth labels have annotationType "Ground Truth" (with space)
   * Prediction labels have annotationType "Prediction"
   */
  isPredictionLabel(label: any): boolean {
    return label.annotationType === "Prediction";
  }

  /**
   * Determine if label text should be shown at bottom instead of top
   * If the box is in the top area of the image (top 30%), show at bottom
   * Otherwise show at top (default)
   */
  shouldShowLabelTextBottom(label: ImageLabel): boolean {
    const position = this.getLabelPosition(label);
    // If the box is in the top area, show text at bottom
    return position.y < 30;
  }

  /**
   * Determine if confidence badge should be shown at top-left instead of bottom-right
   * If the box is in the bottom-right area of the image (bottom 30% and right 70%),
   * show at top-left to keep it inside the image bounds
   */
  shouldShowConfidenceTopLeft(label: ImageLabel): boolean {
    const position = this.getLabelPosition(label);
    // If the box extends into the bottom-right area, show confidence at top-left
    const boxBottom = position.y + position.height;
    const boxRight = position.x + position.width;
    return boxBottom > 70 && boxRight > 30;
  }

  /**
   * Format confidence rate as percentage string
   * @param confidence - confidence rate as decimal (0-1) or percentage (0-100)
   */
  formatConfidence(confidence: number): string {
    // If confidence is already a percentage (> 1), use as is
    // Otherwise multiply by 100 to convert from decimal
    const percentage = confidence > 1 ? confidence : confidence * 100;
    return `${percentage.toFixed(0)}%`;
  }

  /**
   * Check if image has labels
   */
  hasLabels(): boolean {
    return !!this.image.labels && this.image.labels.length > 0;
  }

  /**
   * Get label count
   */
  getLabelCount(): number {
    return this.image.labels?.length || 0;
  }

  /**
   * Check if this is a Classification project
   */
  isClassificationProject(): boolean {
    return this.projectType === "Classification";
  }

  /**
   * Check if image should show classification class badge
   * Only for Classification projects when is_labeled=true and is_no_class=false
   */
  shouldShowClassBadge(): boolean {
    return (
      this.isClassificationProject() &&
      this.hasLabels() &&
      !this.image.isNoClass
    );
  }

  /**
   * Get the first ground truth label for classification display
   * For Classification projects, there should be only one class per image
   */
  getClassificationLabel(): any | null {
    if (!this.image.labels || this.image.labels.length === 0) {
      return null;
    }
    // Find the first ground truth label
    // Note: annotationType can be "Ground Truth" (with space) from the API
    const groundTruthLabel = this.image.labels.find(
      (label: any) =>
        label.annotationType === "Ground Truth" ||
        label.annotationType === "Ground-Truth" ||
        label.annotationType === "GroundTruth"
    );
    return groundTruthLabel || this.image.labels[0];
  }

  /**
   * Get labels to display based on project type and annotation type
   * For Classification: Show only Ground Truth labels (user-created)
   * For Object Detection/Segmentation: Show all labels (both Ground Truth and Predictions)
   */
  getDisplayLabels(): any[] {
    if (!this.image.labels || this.image.labels.length === 0) {
      return [];
    }

    if (this.isClassificationProject()) {
      // For Classification, only show Ground Truth labels
      const groundTruthLabels = this.image.labels.filter(
        (label: any) =>
          label.annotationType === "Ground Truth" ||
          label.annotationType === "Ground-Truth" ||
          label.annotationType === "GroundTruth"
      );

      // If no Ground Truth labels exist, show Predictions (for model evaluation)
      return groundTruthLabels.length > 0
        ? groundTruthLabels
        : this.image.labels;
    }

    // For Object Detection and Segmentation, show all labels
    return this.image.labels;
  }

  /**
   * Get the current class ID for the image (for dropdown selection)
   * Supports both 'classId' (from API DTO) property names
   */
  getCurrentClassId(): number | null {
    const label = this.getClassificationLabel();
    if (!label) {
      return null;
    }
    // The API returns classId in the LabelOverlayDTO
    const classId = label.classId;
    return classId ? Number(classId) : null;
  }

  /**
   * Check if the class dropdown should be shown
   * Show for Classification projects (both labeled and unlabeled images)
   */
  shouldShowClassDropdown(): boolean {
    return this.isClassificationProject() && !this.image.isNoClass;
  }

  /**
   * Handle class selection change from mat-select dropdown
   */
  onMatClassChange(newClassId: number | null): void {
    const previousClassId = this.getCurrentClassId();
    if (newClassId !== previousClassId) {
      this.classChange.emit({
        imageId: this.image.id,
        classId: newClassId,
        previousClassId: previousClassId,
      });
    }
  }

  /**
   * Handle class selection change from dropdown
   */
  onClassChange(event: Event): void {
    event.stopPropagation(); // Prevent card click
    const selectElement = event.target as HTMLSelectElement;
    const newClassId = selectElement.value
      ? parseInt(selectElement.value, 10)
      : null;
    const previousClassId = this.getCurrentClassId();

    if (newClassId !== previousClassId) {
      this.classChange.emit({
        imageId: this.image.id,
        classId: newClassId,
        previousClassId: previousClassId,
      });
    }
  }

  /**
   * Prevent click propagation on dropdown
   */
  onDropdownClick(event: Event): void {
    event.stopPropagation();
  }

  /**
   * Get the color for a class by ID
   */
  getClassColor(classId: number): string {
    const projectClass = this.availableClasses.find((c) => c.id === classId);
    return projectClass?.colorCode || "#9e9e9e";
  }

  /**
   * Get the name for a class by ID
   */
  getClassName(classId: number): string {
    const projectClass = this.availableClasses.find((c) => c.id === classId);
    return projectClass?.className || "";
  }

  /**
   * Check if this is an instance view (single label focused)
   */
  isInstanceView(): boolean {
    return this.viewMode === "instances" && !!this.image.focusedLabel;
  }

  /**
   * Get CSS styles for instance view cropping/zooming (with caching)
   * Fixed container size: 200x200px
   * Crop area: label + 100% of label size as padding on all sides (3x label size in each dimension)
   * Constraint: crop area cannot exceed original image boundaries (0-100%)
   * Smart edge handling: if label is near edge, shift crop area to fit within image
   * Zoom factor: calculated to FILL the 200x200px container (no white space)
   *
   * The transform works as follows:
   * 1. Scale the image so that the crop area fills the container
   * 2. Translate the image so that the crop area's top-left is at the container's top-left
   */
  getInstanceViewStyles(): any {
    if (!this.isInstanceView() || !this.image.focusedLabel) {
      return {};
    }

    // Return cached styles if the focused label hasn't changed
    const currentLabelId = this.image.focusedLabel.id;
    if (
      this._cachedInstanceViewStyles &&
      this._lastFocusedLabelId === currentLabelId
    ) {
      return this._cachedInstanceViewStyles;
    }

    const position = this.getLabelPosition(this.image.focusedLabel);

    // Target: Add 100% of label size as padding on all sides
    // This means crop area = 3x label size (label + 100% on each side)
    const targetPaddingHorizontal = position.width; // 100% of label width on each side
    const targetPaddingVertical = position.height; // 100% of label height on each side

    // Calculate the center point of the label
    const labelCenterX = position.x + position.width / 2;
    const labelCenterY = position.y + position.height / 2;

    // Calculate ideal crop area (label + 100% padding on each side = 3x label size)
    let idealCropWidth = position.width + targetPaddingHorizontal * 2;
    let idealCropHeight = position.height + targetPaddingVertical * 2;

    // Make crop area square (use the larger dimension)
    const cropSize = Math.max(idealCropWidth, idealCropHeight);

    // Calculate ideal crop boundaries (centered on label)
    let cropLeft = labelCenterX - cropSize / 2;
    let cropTop = labelCenterY - cropSize / 2;
    let cropRight = cropLeft + cropSize;
    let cropBottom = cropTop + cropSize;

    // Constrain to image boundaries (0-100%)
    // If crop area exceeds boundaries, shift it to fit within image
    if (cropLeft < 0) {
      cropRight = Math.min(100, cropRight - cropLeft);
      cropLeft = 0;
    }
    if (cropRight > 100) {
      cropLeft = Math.max(0, cropLeft - (cropRight - 100));
      cropRight = 100;
    }
    if (cropTop < 0) {
      cropBottom = Math.min(100, cropBottom - cropTop);
      cropTop = 0;
    }
    if (cropBottom > 100) {
      cropTop = Math.max(0, cropTop - (cropBottom - 100));
      cropBottom = 100;
    }

    // Recalculate actual crop dimensions after boundary constraint
    const actualCropWidth = cropRight - cropLeft;
    const actualCropHeight = cropBottom - cropTop;

    // Calculate zoom factor to FILL the container (no white space)
    // Use Math.max to ensure the crop area completely fills the 200x200 container
    const zoomX = 100 / actualCropWidth;
    const zoomY = 100 / actualCropHeight;
    const zoom = Math.max(zoomX, zoomY); // Use max to fill container completely

    // After scaling, we need to translate to position the crop area at origin
    const translateX = -cropLeft;
    const translateY = -cropTop;

    // Cache the result
    this._cachedInstanceViewStyles = {
      transform: `scale(${zoom}) translate(${translateX}%, ${translateY}%)`,
      "transform-origin": "0 0",
    };
    this._lastFocusedLabelId = currentLabelId;

    return this._cachedInstanceViewStyles;
  }

  /**
   * Determine if the instance label badge should be positioned at the bottom
   * to avoid overlapping with the label box when it's in the top-left corner
   */
  shouldPositionLabelBadgeAtBottom(): boolean {
    if (!this.isInstanceView() || !this.image.focusedLabel) {
      return false;
    }

    const position = this.getLabelPosition(this.image.focusedLabel);
    // Only move badge to bottom if label is in the top-left corner
    // (top 30% AND left 30% of the image)
    return position.y < 30 && position.x < 30;
  }
}
