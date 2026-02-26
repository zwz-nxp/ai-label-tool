import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";

export interface ProjectMetadata {
  id: number;
  name: string;
  type: "text" | "number" | "dropdown";
  valueFrom?: string;
  predefinedValues?: string;
  multipleValues?: boolean;
  createdAt?: string;
  createdBy?: string;
}

export interface ImageMetadata {
  id: number;
  image?: any;
  projectMetadata: ProjectMetadata;
  value: string;
  createdAt: string;
  createdBy: string;
}

export interface MetadataInput {
  metadataId: number;
  value: string;
}

@Injectable({
  providedIn: "root",
})
export class MetadataService {
  private readonly baseUrl: string;
  private readonly projectMetadataUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.baseUrl = this.configuration.ServerWithApiUrl + "landingai/images";
    this.projectMetadataUrl =
      this.configuration.ServerWithApiUrl + "landingai/project-metadata";
  }

  // ==================== Project Metadata Operations ====================

  /**
   * Get all metadata definitions for a specific project
   * @param projectId The project ID
   * @returns Observable of ProjectMetadata array
   */
  public getMetadataByProjectId(
    projectId: number
  ): Observable<ProjectMetadata[]> {
    return this.http
      .get<ProjectMetadata[]>(`${this.projectMetadataUrl}/project/${projectId}`)
      .pipe(catchError(this.handleError));
  }

  // ==================== Image Metadata Operations ====================

  /**
   * Get all metadata values for a specific image
   * @param imageId The image ID
   * @returns Observable of ImageMetadata array
   */
  public getMetadataByImageId(imageId: number): Observable<ImageMetadata[]> {
    return this.http
      .get<ImageMetadata[]>(`${this.baseUrl}/${imageId}/metadata`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Add a single metadata value to an image
   * @param imageId The image ID
   * @param metadataId The project metadata ID
   * @param value The metadata value
   * @returns Observable of the created ImageMetadata
   */
  public addImageMetadata(
    imageId: number,
    metadataId: number,
    value: string
  ): Observable<ImageMetadata> {
    return this.http
      .post<ImageMetadata>(`${this.baseUrl}/${imageId}/metadata`, {
        metadataId,
        value,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update a metadata value for an image
   * @param imageId The image ID
   * @param metadataId The image metadata ID (not project metadata ID)
   * @param value The new value
   * @returns Observable of the updated ImageMetadata
   */
  public updateImageMetadata(
    imageId: number,
    metadataId: number,
    value: string
  ): Observable<ImageMetadata> {
    return this.http
      .put<ImageMetadata>(`${this.baseUrl}/${imageId}/metadata/${metadataId}`, {
        value,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a metadata value from an image
   * @param imageId The image ID
   * @param metadataId The image metadata ID
   * @returns Observable of void
   */
  public deleteImageMetadata(
    imageId: number,
    metadataId: number
  ): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/${imageId}/metadata/${metadataId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update all metadata values for an image (batch update)
   * This will replace all existing metadata with the new ones
   * @param imageId The image ID
   * @param metadataList Array of metadata with metadataId and value
   * @returns Observable of ImageMetadata array
   */
  public updateImageMetadataBatch(
    imageId: number,
    metadataList: MetadataInput[]
  ): Observable<ImageMetadata[]> {
    return this.http
      .put<ImageMetadata[]>(`${this.baseUrl}/${imageId}/metadata`, {
        metadataList,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Save or update a metadata value for an image (legacy method for backward compatibility)
   * @deprecated Use addImageMetadata or updateImageMetadata instead
   * @param metadata The metadata value to save
   * @returns Observable of the saved ImageMetadata
   */
  public saveMetadataValue(metadata: any): Observable<ImageMetadata> {
    // For backward compatibility, determine if this is an add or update
    if (metadata.id) {
      // Update existing metadata
      return this.updateImageMetadata(
        metadata.imageId,
        metadata.id,
        metadata.value
      );
    } else {
      // Add new metadata
      return this.addImageMetadata(
        metadata.imageId,
        metadata.metadataId,
        metadata.value
      );
    }
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
      }
    }

    console.error("MetadataService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
