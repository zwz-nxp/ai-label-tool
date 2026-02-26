import { Component, EventEmitter, Input, Output } from "@angular/core";
import { MatCheckboxChange } from "@angular/material/checkbox";
import { Project } from "app/models/landingai/project.model";
import { Image } from "app/models/landingai/image.model";

/**
 * Left toolbar component for displaying project status and image list
 * Supports batch operations (export, delete) on selected images
 */
@Component({
  selector: "app-left-toolbar",
  templateUrl: "./left-toolbar.component.html",
  styleUrls: ["./left-toolbar.component.scss"],
  standalone: false,
})
export class LeftToolbarComponent {
  @Input() project: Project | null = null;
  @Input() images: Image[] = [];
  @Input() currentImage: Image | null = null;
  @Input() isLoadingImages = false;

  @Output() imageSelected = new EventEmitter<Image>();
  @Output() batchExport = new EventEmitter<Image[]>();
  @Output() batchDelete = new EventEmitter<Image[]>();

  // Track selected images for batch operations
  selectedImages = new Set<number>();

  /**
   * Get the workflow status steps
   */
  get workflowSteps(): string[] {
    return ["Upload", "Label", "Train", "Predict"];
  }

  /**
   * Get the current status index
   */
  get currentStatusIndex(): number {
    return this.workflowSteps.indexOf(this.project?.status || "Upload");
  }

  /**
   * Get the next status in the workflow
   */
  get nextStatus(): string | null {
    const currentIndex = this.currentStatusIndex;
    if (currentIndex >= 0 && currentIndex < this.workflowSteps.length - 1) {
      return this.workflowSteps[currentIndex + 1];
    }
    return null;
  }

  /**
   * Check if any images are selected
   */
  get hasSelectedImages(): boolean {
    return this.selectedImages.size > 0;
  }

  /**
   * Get count of selected images
   */
  get selectedImageCount(): number {
    return this.selectedImages.size;
  }

  /**
   * Check if an image is selected
   */
  isImageSelected(imageId: number): boolean {
    return this.selectedImages.has(imageId);
  }

  /**
   * Toggle image selection
   */
  toggleImageSelection(image: Image, event: MatCheckboxChange): void {
    event.source._elementRef.nativeElement.blur(); // Prevent focus issues
    if (this.selectedImages.has(image.id)) {
      this.selectedImages.delete(image.id);
    } else {
      this.selectedImages.add(image.id);
    }
  }

  /**
   * Get abbreviated split label for display
   */
  getSplitLabel(split: string): string {
    if (!split) return "";
    const lower = split.toLowerCase();
    if (lower === "train" || lower === "training") return "TRA";
    return split;
  }

  /**
   * Check if an image is the current image
   */
  isCurrentImage(image: Image): boolean {
    return this.currentImage?.id === image.id;
  }

  /**
   * Handle image click to select it
   */
  onImageClick(image: Image): void {
    this.imageSelected.emit(image);
  }

  /**
   * Get selected images array
   */
  getSelectedImages(): Image[] {
    return this.images.filter((img) => this.selectedImages.has(img.id));
  }

  /**
   * Handle batch export
   */
  onBatchExport(): void {
    const selected = this.getSelectedImages();
    if (selected.length > 0) {
      this.batchExport.emit(selected);
    }
  }

  /**
   * Handle batch delete
   */
  onBatchDelete(): void {
    const selected = this.getSelectedImages();
    if (selected.length > 0) {
      this.batchDelete.emit(selected);
    }
  }

  /**
   * Copy selected image names to clipboard
   */
  onCopyImageNames(): void {
    const selected = this.getSelectedImages();
    if (selected.length > 0) {
      const imageNames = selected.map((img) => img.fileName).join("\n");
      navigator.clipboard.writeText(imageNames).then(
        () => {},
        (err) => {
          console.error("Failed to copy image names:", err);
        }
      );
    }
  }
}
