import { Component, EventEmitter, Output } from "@angular/core";
import { ACCEPTED_IMAGE_FORMATS } from "../../../../utils/image-upload.utils";

/**
 * Upload zone component for drag-and-drop and click-to-upload functionality.
 */
@Component({
  selector: "app-test-model-upload-zone",
  standalone: false,
  templateUrl: "./test-model-upload-zone.component.html",
  styleUrls: ["./test-model-upload-zone.component.scss"],
})
export class TestModelUploadZoneComponent {
  @Output() filesUploaded = new EventEmitter<File[]>();

  isDragOver: boolean = false;
  acceptedFormats = ACCEPTED_IMAGE_FORMATS.join(",");

  /**
   * Handles drag over event.
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  /**
   * Handles drag leave event.
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  /**
   * Handles file drop event.
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      const files = Array.from(event.dataTransfer.files);
      this.filesUploaded.emit(files);
    }
  }

  /**
   * Triggers file input click.
   */
  triggerFileInput(): void {
    const fileInput = document.getElementById("file-input") as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  /**
   * Handles file input change event.
   */
  onFileInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const files = Array.from(input.files);
      this.filesUploaded.emit(files);

      // Reset input to allow selecting the same file again
      input.value = "";
    }
  }
}
