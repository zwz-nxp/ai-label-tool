import { Component, Inject, OnDestroy, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Subject, takeUntil } from "rxjs";
import {
  PredictionResult,
  TestModelResponse,
  UploadedImage,
} from "../../../models/landingai/test-model.model";
import { TestModelService } from "../../../services/landingai/test-model.service";
import {
  FileUploadResponse,
  FileUploadService,
} from "../../../services/landingai/file-upload.service";
import {
  createUploadedImages,
  revokeBlobUrls,
  validateFiles,
} from "../../../utils/image-upload.utils";

/**
 * Dialog data passed when opening the test model dialog.
 */
export interface TestModelDialogData {
  modelFullName: string;
  version: number;
  trackId: string;
  modelId: number;
  projectId: number; // 專案 ID,用於查詢 project classes
}

/**
 * Main dialog component for testing machine learning models.
 *
 * Manages the overall state and orchestrates child components for
 * image upload, prediction display, and result visualization.
 */
@Component({
  selector: "app-test-model-dialog",
  standalone: false,
  templateUrl: "./test-model-dialog.component.html",
  styleUrls: ["./test-model-dialog.component.scss"],
})
export class TestModelDialogComponent implements OnInit, OnDestroy {
  // State properties
  uploadedImages: UploadedImage[] = [];
  selectedImageIndex: number = 0;
  confidenceThreshold: number = 0.75;
  predictionResults: PredictionResult[] = [];
  isLoading: boolean = false;
  error: string | null = null;

  // View state
  showResults: boolean = false;

  private destroy$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<TestModelDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TestModelDialogData,
    private testModelService: TestModelService,
    private fileUploadService: FileUploadService,
    private snackBar: MatSnackBar
  ) {}

  /**
   * Gets the currently selected image.
   */
  get selectedImage(): UploadedImage | null {
    if (this.uploadedImages.length === 0 || this.selectedImageIndex < 0) {
      return null;
    }
    return this.uploadedImages[this.selectedImageIndex] || null;
  }

  /**
   * Gets prediction results for the currently selected image.
   */
  get selectedImagePredictions(): PredictionResult | null {
    if (this.predictionResults.length === 0 || !this.selectedImage) {
      return null;
    }

    // Match prediction result by blob URL
    return (
      this.predictionResults.find(
        (result) => result.image === this.selectedImage?.blobUrl
      ) || null
    );
  }

  ngOnInit(): void {
    console.log("=== TestModelDialog Initialized ===");
    console.log("Dialog data:", this.data);
    console.log("Model ID:", this.data.modelId);
    console.log("Project ID:", this.data.projectId);
    console.log("Track ID:", this.data.trackId);
  }

  ngOnDestroy(): void {
    // Clean up blob URLs to free memory
    if (this.uploadedImages.length > 0) {
      revokeBlobUrls(this.uploadedImages);
    }

    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handles images uploaded from the upload zone.
   * Validates files, creates UploadedImage objects, and calls the API.
   */
  async handleImagesUploaded(files: File[]): Promise<void> {
    console.log("Handling uploaded images:", files.length);

    // Validate files
    const { validFiles, errors } = validateFiles(files);

    // Display validation errors
    if (errors.length > 0) {
      errors.forEach((error) => {
        this.snackBar.open(error, "Close", {
          duration: 5000,
          verticalPosition: "top",
          panelClass: "snackbar-warning",
        });
      });
    }

    if (validFiles.length === 0) {
      return;
    }

    try {
      // Create UploadedImage objects with blob URLs
      const newImages = await createUploadedImages(validFiles);
      this.uploadedImages = [...this.uploadedImages, ...newImages];

      // 自動選擇第一張圖片
      if (this.uploadedImages.length > 0 && this.selectedImageIndex < 0) {
        this.selectedImageIndex = 0;
        console.log("Auto-selected first image after upload");
      }

      // Call API to get predictions
      await this.callPredictionApi();
    } catch (error) {
      console.error("Error processing uploaded images:", error);
      this.showError("Failed to process uploaded images. Please try again.");
    }
  }

  /**
   * Handles image selection from thumbnail list.
   */
  handleImageSelected(index: number): void {
    console.log("Image selected:", index);
    this.selectedImageIndex = index;
  }

  /**
   * Handles confidence threshold changes.
   * Re-calls the API with the new threshold.
   */
  handleThresholdChanged(threshold: number): void {
    console.log("Threshold changed to:", threshold);
    this.confidenceThreshold = threshold;

    // Re-call API with new threshold
    this.callPredictionApi();
  }

  /**
   * Handles adding more images after initial upload.
   */
  async handleContinueUpload(files: File[]): Promise<void> {
    await this.handleImagesUploaded(files);
  }

  /**
   * Closes the dialog.
   */
  closeDialog(): void {
    this.dialogRef.close();
  }

  /**
   * Calls the prediction API with all uploaded images.
   * 完整流程:
   * 1. 將圖片打包成 zip
   * 2. 上傳 zip 到伺服器
   * 3. 呼叫 Test Model API
   */
  private async callPredictionApi(): Promise<void> {
    if (this.uploadedImages.length === 0) {
      return;
    }

    this.isLoading = true;
    this.error = null;

    try {
      // 步驟 1: 將圖片打包成 zip
      console.log("Step 1: Creating zip file from uploaded images...");
      const zipBlob = await this.createZipFromImages();
      console.log("Zip file created, size:", zipBlob.size);

      // 步驟 2: 上傳 zip 到伺服器
      console.log("Step 2: Uploading zip file to server...");
      const uploadResponse = await this.uploadZipFile(zipBlob);
      console.log("Upload response:", uploadResponse);

      if (uploadResponse.status !== "SUCCESS") {
        throw new Error(uploadResponse.error || "Failed to upload file");
      }

      // 步驟 3: 呼叫 Test Model API
      console.log("Step 3: Calling Test Model API...");
      const request: any = {
        trackId: this.data.trackId,
        zipFilenames: [uploadResponse.filename],
        zipPath: uploadResponse.zipPath,
      };

      console.log("Test Model API request:", request);

      this.testModelService
        .testModel(request)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: any) => {
            console.log("Received test model submission response:", response);

            // 新的 API 回傳格式: {trackId, runId, errorMessage}
            if (response.errorMessage) {
              this.showError(response.errorMessage);
              this.isLoading = false;
              this.cleanupUploadedFile(
                this.data.trackId,
                uploadResponse.filename
              );
              return;
            }

            // 步驟 4: 使用 runId 查詢測試結果
            console.log(
              "Step 4: Fetching test results with runId:",
              response.runId
            );
            this.fetchTestResults(
              response.trackId,
              response.runId,
              uploadResponse.filename
            );
          },
          error: (error: Error) => {
            console.error("Test Model API error:", error);
            this.showError(error.message);
            this.isLoading = false;
            this.cleanupUploadedFile(
              this.data.trackId,
              uploadResponse.filename
            );
          },
        });
    } catch (error: any) {
      console.error("Error in prediction workflow:", error);
      this.showError(error.message || "Failed to process images");
      this.isLoading = false;
    }
  }

  /**
   * 使用 runId 查詢測試結果
   */
  private fetchTestResults(
    trackId: string,
    runId: number,
    uploadedFilename: string
  ): void {
    console.log("Fetching test results for trackId:", trackId, "runId:", runId);

    this.testModelService
      .getTestResults(trackId, runId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log("Received test results response:", response);

          if (response.status === "ERROR") {
            this.showError(response.error || "Failed to retrieve test results");
            this.isLoading = false;
            this.cleanupUploadedFile(trackId, uploadedFilename);
            return;
          }

          // 從檔案路徑讀取 test_predictions JSON
          if (response.test_predictions) {
            this.loadPredictionsFromFile(
              response.test_predictions,
              uploadedFilename
            );
          } else {
            this.showError("Test predictions file path not available");
            this.isLoading = false;
            this.cleanupUploadedFile(trackId, uploadedFilename);
          }
        },
        error: (error: Error) => {
          console.error("Fetch test results error:", error);
          this.showError(error.message);
          this.isLoading = false;
          this.cleanupUploadedFile(trackId, uploadedFilename);
        },
      });
  }

  /**
   * 從檔案路徑載入 predictions JSON
   */
  private loadPredictionsFromFile(
    filePath: string,
    uploadedFilename: string
  ): void {
    console.log("Loading predictions from file:", filePath);

    this.testModelService
      .loadPredictionsFile(filePath)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (predictions: any[]) => {
          console.log("Loaded predictions from file:", predictions);

          // 轉換為 TestModelResponse 格式
          const response: TestModelResponse = {
            prediction_images: predictions.map((pred: any) => ({
              image: pred.image || pred.filename,
              predictions: (pred.predictions || pred.labels || []).map(
                (label: any) => {
                  // 將 bbox_normalized 轉換為 bbox (frontend 期望的格式)
                  const transformedLabel: any = {
                    confidence: label.confidence,
                    class_id: label.class_id,
                    class_name: label.class_name,
                    bbox: label.bbox_normalized || label.bbox || [],
                  };
                  return transformedLabel;
                }
              ),
            })),
          };

          this.handlePredictionResponse(response);
          this.isLoading = false;
          this.showResults = true;
          this.cleanupUploadedFile(this.data.trackId, uploadedFilename);
        },
        error: (error: Error) => {
          console.error("Load predictions file error:", error);
          this.showError(error.message);
          this.isLoading = false;
          this.cleanupUploadedFile(this.data.trackId, uploadedFilename);
        },
      });
  }

  /**
   * 將上傳的圖片打包成 zip 檔案
   */
  private async createZipFromImages(): Promise<Blob> {
    // 使用動態 import 來載入 JSZip
    const JSZipModule: any = await import("jszip");
    // 處理不同的模組格式
    const JSZipConstructor = JSZipModule.default || JSZipModule;
    const zip = new JSZipConstructor();

    // 將每個圖片加入 zip
    for (const image of this.uploadedImages) {
      try {
        // 從 blob URL 取得檔案內容
        const response = await fetch(image.blobUrl);
        const blob = await response.blob();

        // 使用原始檔名加入 zip
        zip.file(image.file.name, blob);
        console.log(`Added ${image.file.name} to zip`);
      } catch (error) {
        console.error(`Failed to add ${image.file.name} to zip:`, error);
        throw new Error(`Failed to process image: ${image.file.name}`);
      }
    }

    // 產生 zip blob
    const zipBlob = await zip.generateAsync({ type: "blob" });
    return zipBlob;
  }

  /**
   * 上傳 zip 檔案到伺服器
   */
  private uploadZipFile(zipBlob: Blob): Promise<FileUploadResponse> {
    const zipFile = new File(
      [zipBlob],
      `test_images_${this.data.trackId}.zip`,
      { type: "application/zip" }
    );

    return new Promise((resolve, reject) => {
      this.fileUploadService
        .uploadZipFile(zipFile, this.data.trackId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => resolve(response),
          error: (error) =>
            reject(new Error("Failed to upload file: " + error.message)),
        });
    });
  }

  /**
   * 清理伺服器上的上傳檔案
   */
  private cleanupUploadedFile(trackId: string, filename: string): void {
    this.fileUploadService
      .deleteFile(trackId, filename)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => console.log("Uploaded file cleaned up successfully"),
        error: (error) =>
          console.warn("Failed to cleanup uploaded file:", error),
      });
  }

  /**
   * Processes the prediction API response.
   * 將 API 返回的圖片檔名對應到前端的 blob URL
   */
  private handlePredictionResponse(response: TestModelResponse): void {
    console.log("=== handlePredictionResponse ===");
    console.log("Full API response:", response);
    console.log("response.prediction_images:", response.prediction_images);

    if (response.prediction_images && response.prediction_images.length > 0) {
      console.log(
        "Number of prediction images:",
        response.prediction_images.length
      );

      // 建立檔名到 blob URL 的對應
      const filenameToBlob = new Map<string, string>();
      this.uploadedImages.forEach((img) => {
        filenameToBlob.set(img.file.name, img.blobUrl);
      });

      // 將 API 返回的 image (檔名) 替換成 blob URL
      this.predictionResults = response.prediction_images.map((result) => {
        console.log("Processing prediction result:", result);
        console.log("  - image:", result.image);
        console.log("  - predictions:", result.predictions);
        console.log("  - predictions length:", result.predictions?.length);

        const blobUrl = filenameToBlob.get(result.image);
        if (blobUrl) {
          return { ...result, image: blobUrl };
        }
        console.warn(`No blob URL found for image: ${result.image}`);
        return result;
      });

      console.log("Stored prediction results:", this.predictionResults.length);
      console.log("First prediction result:", this.predictionResults[0]);

      // 自動選擇第一張圖片
      if (this.predictionResults.length > 0) {
        this.selectedImageIndex = 0;
        console.log("Auto-selected first image");
      }
    } else {
      console.error("No prediction_images in response or empty array");
      this.showError("No prediction results returned from API.");
    }
  }

  /**
   * Displays an error message to the user.
   */
  private showError(message: string): void {
    this.error = message;
    this.snackBar.open(message, "Close", {
      duration: 5000,
      verticalPosition: "top",
      panelClass: "snackbar-error",
    });
  }
}
