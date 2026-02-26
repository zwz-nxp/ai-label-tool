import { Component, EventEmitter, Input, Output } from "@angular/core";
import { UploadedImage } from "../../../../models/landingai/test-model.model";
import { ACCEPTED_IMAGE_FORMATS } from "../../../../utils/image-upload.utils";

/**
 * Component for displaying image thumbnails with selection.
 */
@Component({
  selector: "app-image-thumbnail-list",
  standalone: false,
  templateUrl: "./image-thumbnail-list.component.html",
  styleUrls: ["./image-thumbnail-list.component.scss"],
})
export class ImageThumbnailListComponent {
  @Input() images: UploadedImage[] = [];
  @Input() selectedIndex: number = 0;
  @Output() imageSelected = new EventEmitter<number>();
  @Output() continueUpload = new EventEmitter<File[]>();

  acceptedFormats = ACCEPTED_IMAGE_FORMATS.join(",");

  /**
   * Handles thumbnail click.
   */
  selectImage(index: number): void {
    this.imageSelected.emit(index);
  }

  /**
   * Checks if thumbnail is selected.
   */
  isSelected(index: number): boolean {
    return index === this.selectedIndex;
  }

  /**
   * Triggers file input for continue upload.
   */
  triggerContinueUpload(): void {
    const fileInput = document.getElementById(
      "continue-upload-input"
    ) as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  /**
   * Handles continue upload file selection.
   */
  onContinueUploadChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      this.continueUpload.emit(files);

      // Reset input
      input.value = "";
    }
  }
}
