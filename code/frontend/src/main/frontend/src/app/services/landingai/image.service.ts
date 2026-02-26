import {
  HttpClient,
  HttpErrorResponse,
  HttpEvent,
  HttpEventType,
  HttpParams,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { Image, ImageUploadResult } from "app/models/landingai/image";
import { catchError, map } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import {
  FilterState,
  PaginatedResponse,
  SortMethod,
  ViewMode,
} from "app/state/landingai/image-upload/image-upload.actions";

export interface UploadProgress {
  file: File;
  progress: number;
  result?: ImageUploadResult;
  error?: string;
}

@Injectable({
  providedIn: "root",
})
export class ImageService {
  private readonly actionUrl: string;

  // Mock mode flag - set to true to use mock images for development
  private readonly useMockImages: boolean = true;

  // Mock image path (local asset)
  private readonly mockImagePath: string = "assets/images/mock-image.jpg";

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl = this.configuration.ServerWithApiUrl + "landingai/images/";
  }

  /**
   * Get a single image by ID
   * @param imageId The image ID
   * @returns Observable of Image
   */
  public getImageById(imageId: number): Observable<Image> {
    return this.http
      .get<Image>(`${this.actionUrl}${imageId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all images for a specific project
   * @param projectId The project ID
   * @returns Observable of Image array
   */
  public getImagesByProjectId(projectId: number): Observable<Image[]> {
    return this.http
      .get<Image[]>(`${this.actionUrl}project/${projectId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get images by project ID and split type
   * @param projectId The project ID
   * @param split The split type (train/dev/test)
   * @returns Observable of Image array
   */
  public getImagesByProjectAndSplit(
    projectId: number,
    split: string
  ): Observable<Image[]> {
    return this.http
      .get<Image[]>(`${this.actionUrl}project/${projectId}/split/${split}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all image IDs for a specific project with filters
   * Used for "Select All" functionality
   * @param projectId The project ID
   * @param filters Filter state
   * @returns Observable of number array (image IDs)
   */
  public getAllImageIds(
    projectId: number,
    filters: FilterState = {}
  ): Observable<number[]> {
    let params = new HttpParams();

    // Add filters if provided
    if (filters.mediaStatus && filters.mediaStatus.length > 0) {
      params = params.set("mediaStatus", filters.mediaStatus.join(","));
    }
    if (filters.groundTruthLabels && filters.groundTruthLabels.length > 0) {
      params = params.set(
        "groundTruthLabels",
        filters.groundTruthLabels.join(",")
      );
    }
    if (filters.predictionLabels && filters.predictionLabels.length > 0) {
      params = params.set(
        "predictionLabels",
        filters.predictionLabels.join(",")
      );
    }
    if (filters.annotationType) {
      params = params.set("annotationType", filters.annotationType);
    }
    if (filters.modelId) {
      params = params.set("modelId", filters.modelId.toString());
    }
    if (filters.split && filters.split.length > 0) {
      params = params.set("split", filters.split.join(","));
    }
    if (filters.tags && filters.tags.length > 0) {
      params = params.set("tags", filters.tags.join(","));
    }
    if (filters.mediaName) {
      params = params.set("mediaName", filters.mediaName);
    }
    if (filters.labeler) {
      params = params.set("labeler", filters.labeler);
    }
    if (filters.mediaId) {
      params = params.set("mediaId", filters.mediaId);
    }
    if (filters.noClass) {
      params = params.set("noClass", "true");
    }
    if (filters.predictionNoClass) {
      params = params.set("predictionNoClass", "true");
    }
    if (filters.metadata) {
      Object.keys(filters.metadata).forEach((key) => {
        params = params.set(`metadata.${key}`, filters.metadata![key]);
      });
    }

    return this.http
      .get<number[]>(`${this.actionUrl}project/${projectId}/all-ids`, {
        params,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all images for a specific project
   * @param projectId The project ID
   * @returns Observable of Image array
   */
  public getImagesPageableByProjectId(
    projectId: number,
    page: number = 1,
    size: number = 20,
    viewMode: ViewMode = "images",
    filters: FilterState = {},
    sortBy: SortMethod = "upload_time_desc",
    includeThumbnails: boolean = true
  ): Observable<PaginatedResponse<Image>> {
    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("viewMode", viewMode)
      .set("sortBy", sortBy)
      .set("includeThumbnails", includeThumbnails.toString())
      .set("_t", Date.now().toString()); // Cache buster to ensure fresh data

    // Add filters if provided
    if (filters.mediaStatus && filters.mediaStatus.length > 0) {
      params = params.set("mediaStatus", filters.mediaStatus.join(","));
    }
    if (filters.groundTruthLabels && filters.groundTruthLabels.length > 0) {
      params = params.set(
        "groundTruthLabels",
        filters.groundTruthLabels.join(",")
      );
    }
    if (filters.predictionLabels && filters.predictionLabels.length > 0) {
      params = params.set(
        "predictionLabels",
        filters.predictionLabels.join(",")
      );
    }
    if (filters.annotationType) {
      params = params.set("annotationType", filters.annotationType);
    }
    if (filters.modelId) {
      params = params.set("modelId", filters.modelId.toString());
    }
    if (filters.split && filters.split.length > 0) {
      params = params.set("split", filters.split.join(","));
    }
    if (filters.tags && filters.tags.length > 0) {
      params = params.set("tags", filters.tags.join(","));
    }
    if (filters.mediaName) {
      params = params.set("mediaName", filters.mediaName);
    }
    if (filters.labeler) {
      params = params.set("labeler", filters.labeler);
    }
    if (filters.mediaId) {
      params = params.set("mediaId", filters.mediaId);
    }
    if (filters.noClass) {
      params = params.set("noClass", "true");
    }
    if (filters.predictionNoClass) {
      params = params.set("predictionNoClass", "true");
    }
    if (filters.metadata) {
      Object.keys(filters.metadata).forEach((key) => {
        params = params.set(`metadata.${key}`, filters.metadata![key]);
      });
    }

    return this.http
      .get<PaginatedResponse<Image>>(
        `${this.actionUrl}project/${projectId}/pageable`,
        {
          params,
        }
      )
      .pipe(catchError(this.handleError));
  }

  /**
   * Upload a new image
   * @param file The image file to upload
   * @param projectId The project ID
   * @returns Observable of the created Image
   */
  public uploadImage(file: File, projectId: number): Observable<Image> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("projectId", projectId.toString());

    return this.http
      .post<Image>(`${this.actionUrl}upload`, formData)
      .pipe(catchError(this.handleError));
  }

  public uploadImages(
    files: File[],
    projectId: number
  ): Observable<ImageUploadResult[]> {
    const formData = new FormData();

    // Append project ID
    formData.append("projectId", projectId.toString());

    // Append all files
    files.forEach((file) => {
      formData.append("files", file, file.name);
    });

    return this.http
      .post<ImageUploadResult[]>(this.actionUrl + "upload", formData)
      .pipe(catchError(this.handleError));
  }

  /**
   * Upload images with progress tracking
   * @param files - Array of image files to upload
   * @param projectId - ID of the project to upload images to
   * @returns Observable of upload progress events
   */
  public uploadImagesWithProgress(
    files: File[],
    projectId: number
  ): Observable<{ progress: number; results?: ImageUploadResult[] }> {
    const formData = new FormData();

    // Append project ID
    formData.append("projectId", projectId.toString());

    // Append all files
    files.forEach((file) => {
      formData.append("files", file, file.name);
    });

    return this.http
      .post<ImageUploadResult[]>(this.actionUrl + "upload", formData, {
        reportProgress: true,
        observe: "events",
      })
      .pipe(
        map((event: HttpEvent<ImageUploadResult[]>) => {
          switch (event.type) {
            case HttpEventType.UploadProgress:
              const progress = event.total
                ? Math.round((100 * event.loaded) / event.total)
                : 0;
              return { progress };

            case HttpEventType.Response:
              return { progress: 100, results: event.body || [] };

            default:
              return { progress: 0 };
          }
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Delete a single image
   * @param imageId The image ID to delete
   * @returns Observable of void
   */
  public deleteImage(imageId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.actionUrl}${imageId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete multiple images in batch
   * @param imageIds Array of image IDs to delete
   * @returns Observable of void
   */
  public deleteImages(imageIds: number[]): Observable<void> {
    return this.http
      .post<void>(`${this.actionUrl}delete-batch`, { imageIds })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get thumbnail as blob (for downloading or processing)
   * @param imageId - ID of the image
   * @returns Observable of Blob
   */
  public getThumbnailBlob(imageId: number): Observable<Blob> {
    return this.http
      .get(this.actionUrl + `${imageId}/thumbnail`, {
        responseType: "blob",
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get thumbnail URL for an image
   * @param imageId - ID of the image
   * @returns URL string for the thumbnail endpoint
   */
  public getThumbnailUrl(imageId: number): string {
    return this.actionUrl + `${imageId}/thumbnail`;
  }

  /**
   * Get image file content by file name (deprecated - use getImageFileByImageId instead)
   * @param fileName The file name
   * @returns Observable of Blob
   */
  public getImageFile(fileName: string): Observable<Blob> {
    return this.http
      .get(`${this.actionUrl}file/${fileName}`, { responseType: "blob" })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get image file content by image ID
   * @param imageId The image ID
   * @returns Observable of Blob
   */
  public getImageFileByImageId(imageId: number): Observable<Blob> {
    return this.http
      .get(`${this.actionUrl}${imageId}/file`, { responseType: "blob" })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get image file content by image ID from database (legacy method)
   * @param imageId The image ID
   * @returns Observable of Blob
   * @deprecated Use getImageFileByImageId instead
   */
  public getImageFileByImageIdLegacy(imageId: number): Observable<Blob> {
    return this.http
      .get(
        `${this.configuration.ServerWithApiUrl}landingai/image-files/${imageId}`,
        { responseType: "blob" }
      )
      .pipe(catchError(this.handleError));
  }

  /**
   * Update the split assignment for an image
   * @param imageId The image ID
   * @param split The split value (Unassigned, Train, Dev, Test)
   * @returns Observable of updated Image
   */
  public updateSplit(
    imageId: number,
    split: "Unassigned" | "Train" | "Dev" | "Test"
  ): Observable<Image> {
    return this.http
      .put<Image>(`${this.actionUrl}${imageId}/split`, { split })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update the isNoClass flag for an image
   * @param imageId The image ID
   * @param isNoClass The no-class flag value
   * @returns Observable of updated Image
   */
  public updateIsNoClass(
    imageId: number,
    isNoClass: boolean
  ): Observable<Image> {
    return this.http
      .put<Image>(`${this.actionUrl}${imageId}/is-no-class`, { isNoClass })
      .pipe(catchError(this.handleError));
  }

  /**
   * Upload a ZIP file containing classified images
   * The ZIP should contain folders named by class, with images inside each folder
   * @param file - The ZIP file to upload
   * @param projectId - ID of the project to upload images to
   * @returns Observable of upload progress and result
   */
  public uploadClassifiedImagesZip(
    file: File,
    projectId: number
  ): Observable<{
    progress?: number;
    result?: {
      success: boolean;
      totalImages: number;
      classesCreated: number;
      classesReused: number;
      errors: string[];
    };
  }> {
    const formData = new FormData();
    formData.append("file", file, file.name);
    formData.append("projectId", projectId.toString());

    return this.http
      .post<{
        success: boolean;
        totalImages: number;
        classesCreated: number;
        classesReused: number;
        errors: string[];
      }>(this.actionUrl + "upload-classified", formData, {
        reportProgress: true,
        observe: "events",
      })
      .pipe(
        map((event: HttpEvent<any>) => {
          switch (event.type) {
            case HttpEventType.UploadProgress:
              const progress = event.total
                ? Math.round((100 * event.loaded) / event.total)
                : 0;
              return { progress };

            case HttpEventType.Response:
              return { progress: 100, result: event.body };

            default:
              return { progress: 0 };
          }
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Upload a ZIP file containing batch images (images at root level)
   * For Object Detection and Segmentation projects
   * @param file - The ZIP file to upload
   * @param projectId - ID of the project to upload images to
   * @returns Observable of upload progress and result
   */
  public uploadBatchImagesZip(
    file: File,
    projectId: number
  ): Observable<{
    progress?: number;
    result?: {
      success: boolean;
      totalImages: number;
      errors: string[];
    };
  }> {
    const formData = new FormData();
    formData.append("file", file, file.name);
    formData.append("projectId", projectId.toString());

    return this.http
      .post<{
        success: boolean;
        totalImages: number;
        errors: string[];
      }>(this.actionUrl + "upload-batch", formData, {
        reportProgress: true,
        observe: "events",
      })
      .pipe(
        map((event: HttpEvent<any>) => {
          switch (event.type) {
            case HttpEventType.UploadProgress:
              const progress = event.total
                ? Math.round((100 * event.loaded) / event.total)
                : 0;
              return { progress };

            case HttpEventType.Response:
              return { progress: 100, result: event.body };

            default:
              return { progress: 0 };
          }
        }),
        catchError(this.handleError)
      );
  }

  /**
   * Batch set metadata for multiple images
   * @param imageIds Array of image IDs
   * @param metadata Object with metadata key-value pairs
   * @returns Observable of void
   */
  public batchSetMetadata(
    imageIds: number[],
    metadata: { [key: string]: string }
  ): Observable<void> {
    return this.http
      .post<void>(`${this.actionUrl}batch-set-metadata`, {
        imageIds,
        metadata,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Batch set tags for multiple images
   * @param imageIds Array of image IDs
   * @param tagIds Array of tag IDs to assign
   * @returns Observable of void
   */
  public batchSetTags(imageIds: number[], tagIds: number[]): Observable<void> {
    return this.http
      .post<void>(`${this.actionUrl}batch-set-tags`, {
        imageIds,
        tagIds,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Batch set class for multiple images (Classification projects)
   * @param imageIds Array of image IDs
   * @param classId The class ID to assign
   * @returns Observable of void
   */
  public batchSetClass(imageIds: number[], classId: number): Observable<void> {
    return this.http
      .post<void>(`${this.actionUrl}batch-set-class`, {
        imageIds,
        classId,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get mock image URL for development
   * @returns string - URL to mock image asset
   */
  public getMockImageUrl(): string {
    return this.mockImagePath;
  }

  /**
   * Check if mock mode is enabled
   * @returns boolean
   */
  public isMockMode(): boolean {
    return this.useMockImages;
  }

  /**
   * Export dataset as ZIP file for training
   * Downloads the dataset with proper folder structure based on project type
   * @param projectId The project ID
   * @param imageIds Optional array of image IDs to export (if not provided, exports all images)
   * @returns Observable of Blob (ZIP file)
   */
  public exportDataset(
    projectId: number,
    imageIds?: number[]
  ): Observable<Blob> {
    return this.http
      .post(
        `${this.actionUrl}project/${projectId}/export-dataset`,
        imageIds || null,
        {
          responseType: "blob",
        }
      )
      .pipe(catchError(this.handleError));
  }

  /**
   * Handle HTTP errors
   * @param error The HTTP error response
   * @returns Observable that throws an error
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred";

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Backend error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    console.error("ImageService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
