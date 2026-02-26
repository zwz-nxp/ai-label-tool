import { Component, Inject, OnDestroy } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ImageService } from "../../../../services/landingai/image.service";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

export interface UploadBatchDialogData {
  projectId: number;
}

export interface BatchUploadResult {
  success: boolean;
  totalImages: number;
  errors: string[];
}

@Component({
  selector: "app-upload-batch-dialog",
  standalone: false,
  templateUrl: "./upload-batch-dialog.component.html",
  styleUrls: ["./upload-batch-dialog.component.scss"],
})
export class UploadBatchDialogComponent implements OnDestroy {
  selectedFile: File | null = null;
  isDragging = false;
  isUploading = false;
  uploadProgress = 0;
  uploadResult: BatchUploadResult | null = null;
  errorMessage: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<UploadBatchDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UploadBatchDialogData,
    private imageService: ImageService
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check if upload button should be disabled
   */
  get isUploadDisabled(): boolean {
    return this.isUploading || !this.selectedFile;
  }

  /**
   * Handle file selection from file browser
   */
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectFile(input.files[0]);
    }
    // Reset input value to allow selecting the same file again
    input.value = "";
  }

  /**
   * Handle drag over event
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  /**
   * Handle drag leave event
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  /**
   * Handle drop event
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectFile(event.dataTransfer.files[0]);
    }
  }

  /**
   * Select and validate a file
   */
  private selectFile(file: File): void {
    this.errorMessage = null;
    this.uploadResult = null;

    // Validate file type
    if (!file.name.toLowerCase().endsWith(".zip")) {
      this.errorMessage = "Only ZIP files are supported";
      return;
    }

    // Validate file size (max 2048MB)
    const maxSize = 2048 * 1024 * 1024;
    if (file.size > maxSize) {
      this.errorMessage = "File size exceeds maximum allowed size of 2048MB";
      return;
    }

    this.selectedFile = file;
  }

  /**
   * Remove selected file
   */
  removeFile(): void {
    this.selectedFile = null;
    this.errorMessage = null;
    this.uploadResult = null;
  }

  /**
   * Upload the zip file
   */
  uploadFile(): void {
    if (!this.selectedFile) {
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;
    this.errorMessage = null;

    this.imageService
      .uploadBatchImagesZip(this.selectedFile, this.data.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (event) => {
          if (event.progress !== undefined) {
            this.uploadProgress = event.progress;
          }
          if (event.result) {
            this.uploadResult = event.result;
            this.isUploading = false;

            // Auto-close on success after a short delay
            if (event.result.success && event.result.errors.length === 0) {
              setTimeout(() => {
                this.dialogRef.close({
                  success: true,
                  uploadedCount: event.result!.totalImages,
                });
              }, 2000);
            }
          }
        },
        error: (error) => {
          this.errorMessage = error.message || "Upload failed";
          this.isUploading = false;
        },
      });
  }

  /**
   * Cancel and close dialog
   */
  cancel(): void {
    this.dialogRef.close({ success: false });
  }

  /**
   * Close dialog after successful upload
   */
  close(): void {
    this.dialogRef.close({
      success: this.uploadResult?.success || false,
      uploadedCount: this.uploadResult?.totalImages || 0,
    });
  }

  /**
   * Format file size
   */
  formatFileSize(bytes: number): string {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return (bytes / 1024).toFixed(2) + " KB";
    } else {
      return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    }
  }
}
