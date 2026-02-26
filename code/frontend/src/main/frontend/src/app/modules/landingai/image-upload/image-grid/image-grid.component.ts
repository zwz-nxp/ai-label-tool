import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Image } from "../../../../models/landingai/image";
import { ProjectType } from "../../../../models/landingai/project";
import { ProjectClass } from "../../../../models/landingai/project-class.model";
import { ClassChangeEvent } from "../image-card/image-card.component";

@Component({
  selector: "app-image-grid",
  standalone: false,
  templateUrl: "./image-grid.component.html",
  styleUrls: ["./image-grid.component.scss"],
})
export class ImageGridComponent {
  @Input() images: Image[] = [];
  @Input() viewMode: "images" | "instances" = "images";
  @Input() zoomLevel: number = 3;
  @Input() currentPage: number = 1;
  @Input() totalPages: number = 1;
  @Input() totalElements: number = 0;
  @Input() projectType: ProjectType = "Object Detection";
  @Input() availableClasses: ProjectClass[] = [];
  @Input() showLabelDetails: boolean = true;
  @Input() selectMode: boolean = false;
  @Input() selectedImageIds: Set<number> = new Set();

  @Output() imageClick = new EventEmitter<Image>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() classChange = new EventEmitter<ClassChangeEvent>();
  @Output() imageSelectionToggle = new EventEmitter<{
    imageId: number;
    selected: boolean;
  }>();

  /**
   * Calculate grid columns based on zoom level
   * Zoom level 1 (min) = 6 images per row
   * Zoom level 2 = 5 images per row
   * Zoom level 3 = 4 images per row (default)
   * Zoom level 4 = 3 images per row
   * Zoom level 5 (max) = 2 images per row
   */
  getGridColumns(): number {
    const columnMap: { [key: number]: number } = {
      1: 6,
      2: 5,
      3: 4,
      4: 3,
      5: 2,
    };
    return columnMap[this.zoomLevel] || 4;
  }

  /**
   * Handle image card click
   */
  onImageClick(image: Image): void {
    this.imageClick.emit(image);
  }

  /**
   * Handle class change from image card
   */
  onClassChange(event: ClassChangeEvent): void {
    this.classChange.emit(event);
  }

  /**
   * Handle page change
   */
  onPageChange(page: number): void {
    this.pageChange.emit(page);
  }

  /**
   * Navigate to previous page
   */
  previousPage(): void {
    if (this.currentPage > 1) {
      this.onPageChange(this.currentPage - 1);
    }
  }

  /**
   * Navigate to next page
   */
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.onPageChange(this.currentPage + 1);
    }
  }

  /**
   * Check if previous button should be disabled
   */
  isPreviousDisabled(): boolean {
    return this.currentPage <= 1;
  }

  /**
   * Check if next button should be disabled
   */
  isNextDisabled(): boolean {
    return this.currentPage >= this.totalPages;
  }

  /**
   * Handle image selection toggle
   */
  onImageSelectionToggle(imageId: number, selected: boolean): void {
    this.imageSelectionToggle.emit({ imageId, selected });
  }

  /**
   * Check if an image is selected
   */
  isImageSelected(imageId: number): boolean {
    return this.selectedImageIds.has(imageId);
  }

  /**
   * Get the range of images being displayed on current page
   * For example: "1-20" for page 1, "21-22" for page 2 of 22 total
   */
  getImageRange(): string {
    if (this.totalElements === 0) {
      return "0";
    }

    const pageSize = 20; // Should match backend page size
    const startIndex = (this.currentPage - 1) * pageSize + 1;
    const endIndex = Math.min(this.currentPage * pageSize, this.totalElements);

    if (startIndex === endIndex) {
      return `${startIndex}`;
    }

    return `${startIndex}-${endIndex}`;
  }
}
