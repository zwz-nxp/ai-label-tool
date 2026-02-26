import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";

export interface ProjectTag {
  id: number;
  name: string;
  color?: string;
  createdAt?: string;
  createdBy?: string;
}

export interface ImageTag {
  id: number;
  image?: any;
  projectTag: ProjectTag;
  createdAt: string;
  createdBy: string;
}

@Injectable({
  providedIn: "root",
})
export class TagService {
  private readonly baseUrl: string;
  private readonly projectTagUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.baseUrl = this.configuration.ServerWithApiUrl + "landingai/images";
    this.projectTagUrl =
      this.configuration.ServerWithApiUrl + "landingai/project-tags";
  }

  // ==================== Project Tag Operations ====================

  /**
   * Get all tag definitions for a specific project
   * @param projectId The project ID
   * @returns Observable of ProjectTag array
   */
  public getTagsByProjectId(projectId: number): Observable<ProjectTag[]> {
    return this.http
      .get<ProjectTag[]>(`${this.projectTagUrl}/project/${projectId}`)
      .pipe(catchError(this.handleError));
  }

  // ==================== Image Tag Operations ====================

  /**
   * Get all tags for a specific image
   * @param imageId The image ID
   * @returns Observable of ImageTag array
   */
  public getTagsByImageId(imageId: number): Observable<ImageTag[]> {
    return this.http
      .get<ImageTag[]>(`${this.baseUrl}/${imageId}/tags`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Add a single tag to an image
   * @param imageId The image ID
   * @param tagId The project tag ID
   * @returns Observable of the created ImageTag
   */
  public addImageTag(imageId: number, tagId: number): Observable<ImageTag> {
    return this.http
      .post<ImageTag>(`${this.baseUrl}/${imageId}/tags`, { tagId })
      .pipe(catchError(this.handleError));
  }

  /**
   * Remove a tag from an image
   * @param imageId The image ID
   * @param tagId The image tag ID (not project tag ID)
   * @returns Observable of void
   */
  public removeImageTag(imageId: number, tagId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/${imageId}/tags/${tagId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update all tags for an image (batch update)
   * This will replace all existing tags with the new ones
   * @param imageId The image ID
   * @param tagIds Array of project tag IDs
   * @returns Observable of ImageTag array
   */
  public updateImageTags(
    imageId: number,
    tagIds: number[]
  ): Observable<ImageTag[]> {
    return this.http
      .put<ImageTag[]>(`${this.baseUrl}/${imageId}/tags`, { tagIds })
      .pipe(catchError(this.handleError));
  }

  /**
   * Add a new tag to an image (legacy method for backward compatibility)
   * @deprecated Use addImageTag instead
   * @param tag The tag to add
   * @returns Observable of the created ImageTag
   */
  public addTag(tag: any): Observable<ImageTag> {
    // For backward compatibility
    return this.addImageTag(tag.imageId, tag.tagId || tag.projectTagId);
  }

  /**
   * Delete a tag (legacy method for backward compatibility)
   * @deprecated Use removeImageTag instead
   * @param tagId The tag ID to delete
   * @returns Observable of void
   */
  public deleteTag(imageId: number, tagId: number): Observable<void> {
    return this.removeImageTag(imageId, tagId);
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

    console.error("TagService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
