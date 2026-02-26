import { Component, Inject, OnDestroy } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ImageService } from "../../../../services/landingai/image.service";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

export interface UploadDialogData {
  projectId: number;
}

export interface FileUploadItem {
  file: File;
  progress: number;
  status: "pending" | "uploading" | "success" | "error";
  error?: string;
}

@Component({
  selector: "app-upload-dialog",
  standalone: false,
  templateUrl: "./upload-dialog.component.html",
  styleUrls: ["./upload-dialog.component.scss"],
})
export class UploadDialogComponent implements OnDestroy {
  files: FileUploadItem[] = [];
  isDragging = false;
  isUploading = false;
  private destroy$ = new Subject<void>();

  // Supported image formats (only JPG/JPEG/PNG allowed)
  private readonly SUPPORTED_FORMATS = ["image/png", "image/jpeg", "image/jpg"];

  private readonly SUPPORTED_EXTENSIONS = [".png", ".jpg", ".jpeg"];

  constructor(
    public dialogRef: MatDialogRef<UploadDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UploadDialogData,
    private imageService: ImageService
  ) {}

  /**
   * Check if upload button should be disabled
   */
  get isUploadDisabled(): boolean {
    return (
      this.isUploading ||
      this.files.length === 0 ||
      this.files.every((item) => item.status !== "pending")
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle file selection from file browser
   */
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.addFiles(Array.from(input.files));
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
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  /**
   * Remove file from the list
   */
  removeFile(index: number): void {
    this.files.splice(index, 1);
  }

  /**
   * Upload all pending files
   */
  uploadFiles(): void {
    const pendingFiles = this.files.filter((item) => item.status === "pending");
    if (pendingFiles.length === 0) {
      return;
    }

    this.isUploading = true;

    // Upload files
    const filesToUpload = pendingFiles.map((item) => item.file);

    this.imageService
      .uploadImagesWithProgress(filesToUpload, this.data.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (event) => {
          // Update progress for all uploading files
          pendingFiles.forEach((item) => {
            item.status = "uploading";
            item.progress = event.progress;
          });

          // If upload is complete, mark all as success
          if (event.results) {
            event.results.forEach((result, index) => {
              if (index < pendingFiles.length) {
                if (result.success) {
                  pendingFiles[index].status = "success";
                  pendingFiles[index].progress = 100;
                } else {
                  pendingFiles[index].status = "error";
                  pendingFiles[index].error =
                    result.errorMessage || "Upload failed";
                }
              }
            });

            this.isUploading = false;

            // Check if all uploads are complete
            const allComplete = this.files.every(
              (item) => item.status === "success" || item.status === "error"
            );

            if (allComplete) {
              // Close dialog after a short delay to show completion
              setTimeout(() => {
                this.dialogRef.close({
                  success: true,
                  uploadedCount: event.results?.length || 0,
                });
              }, 1000);
            }
          }
        },
        error: (error) => {
          // Mark all uploading files as error
          pendingFiles.forEach((item) => {
            item.status = "error";
            item.error = error.message || "Upload failed";
          });
          this.isUploading = false;
        },
      });
  }

  /**
   * Cancel upload and close dialog
   */
  cancel(): void {
    this.dialogRef.close({ success: false });
  }

  /**
   * Get total file size in MB
   */
  getTotalSize(): string {
    const totalBytes = this.files.reduce(
      (sum, item) => sum + item.file.size,
      0
    );
    const totalMB = totalBytes / (1024 * 1024);
    return totalMB.toFixed(2);
  }

  /**
   * Get count of files by status
   */
  getFileCountByStatus(status: string): number {
    return this.files.filter((item) => item.status === status).length;
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

  /**
   * Add files to the upload list with validation
   */
  private addFiles(files: File[]): void {
    files.forEach((file) => {
      const validationError = this.validateFile(file);
      if (validationError) {
        this.files.push({
          file,
          progress: 0,
          status: "error",
          error: validationError,
        });
      } else {
        this.files.push({
          file,
          progress: 0,
          status: "pending",
        });
      }
    });
  }

  /**
   * Validate file format
   */
  private validateFile(file: File): string | null {
    // Check MIME type
    if (!this.SUPPORTED_FORMATS.includes(file.type)) {
      // Also check file extension as fallback
      const extension = file.name
        .toLowerCase()
        .substring(file.name.lastIndexOf("."));
      if (!this.SUPPORTED_EXTENSIONS.includes(extension)) {
        return "Only JPG, JPEG, and PNG formats are supported";
      }
    }
    return null;
  }
}
