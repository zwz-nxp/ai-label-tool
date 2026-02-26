import { Injectable } from "@angular/core";
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { environment } from "environments/environment";
import { Project } from "app/models/landingai/project";

/**
 * Snapshot entity representing a point-in-time capture of a project's dataset state
 * Requirements: 2.1, 2.2
 */
export interface Snapshot {
  id: number;
  projectId: number;
  name: string; // Changed from snapshotName to match backend DTO
  description: string;
  imageCount: number;
  labelCount: number;
  classCount?: number;
  createdAt: Date;
  createdBy: string;
}

export interface SnapshotCreateRequest {
  projectId: number;
  snapshotName: string;
  description?: string;
}

export interface SnapshotPreviewStats {
  labeled: number;
  unlabeled: number;
  noClass: number;
  trainCount: number;
  devCount: number;
  testCount: number;
  unassignedCount: number;
}

/**
 * Label overlay information for displaying labels on image thumbnails
 */
export interface LabelOverlayDTO {
  id: number;
  classId: number;
  className: string;
  colorCode: string;
  position: string;
  annotationType: string;
  confidenceRate: number;
}

/**
 * Image list item DTO for paginated image responses
 * Requirements: 1.1, 1.5
 */
export interface ImageListItemDTO {
  id: number;
  fileName: string;
  fileSize: number;
  width: number;
  height: number;
  split: string;
  isLabeled: boolean;
  isNoClass: boolean;
  labelCount: number;
  thumbnailImage: string; // Base64 encoded
  thumbnailWidthRatio: number;
  thumbnailHeightRatio: number;
  createdAt: Date;
  labels: LabelOverlayDTO[];
  instanceLabelId?: number;
  focusedLabel?: LabelOverlayDTO;
}

/**
 * Paginated response wrapper for API responses
 * Requirements: 1.3, 1.4
 */
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/**
 * Request DTO for creating a new project from a snapshot
 * Requirements: 4.2, 4.3
 */
export interface CreateProjectFromSnapshotRequest {
  projectName: string;
}

@Injectable({
  providedIn: "root",
})
export class SnapshotService {
  private readonly apiUrl = `${environment.server}api/landingai/snapshots`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new snapshot for a project
   */
  createSnapshot(request: SnapshotCreateRequest): Observable<Snapshot> {
    return this.http
      .post<Snapshot>(this.apiUrl, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all snapshots for a project
   * Requirements: 2.1
   * @param projectId The project ID
   * @returns Observable of Snapshot array
   */
  getProjectSnapshots(projectId: number): Observable<Snapshot[]> {
    return this.http
      .get<Snapshot[]>(`${this.apiUrl}/project/${projectId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all snapshots for a project (alias for getProjectSnapshots)
   * @deprecated Use getProjectSnapshots instead
   */
  getSnapshotsForProject(projectId: number): Observable<Snapshot[]> {
    return this.getProjectSnapshots(projectId);
  }

  /**
   * Get snapshot preview stats for a project
   */
  getSnapshotPreviewStats(projectId: number): Observable<SnapshotPreviewStats> {
    return this.http
      .get<SnapshotPreviewStats>(
        `${this.apiUrl}/project/${projectId}/preview-stats`
      )
      .pipe(catchError(this.handleError));
  }

  /**
   * Get paginated images for a specific snapshot
   * Requirements: 2.1, 3.1, 3.2
   * @param snapshotId The snapshot ID
   * @param page The page number (0-indexed)
   * @param size The page size
   * @param sortBy The sort method (optional)
   * @param filters The filter criteria (optional)
   * @returns Observable of PaginatedResponse containing ImageListItemDTO
   */
  getSnapshotImages(
    snapshotId: number,
    page: number,
    size: number,
    sortBy?: string,
    filters?: any
  ): Observable<PaginatedResponse<ImageListItemDTO>> {
    console.log("SnapshotService.getSnapshotImages called with:", {
      snapshotId,
      page,
      size,
      sortBy,
      filters,
    });

    let params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    if (sortBy) {
      params = params.set("sortBy", sortBy);
    }

    console.log(
      "SnapshotService: Making HTTP POST request with filters:",
      filters
    );

    // Use POST request to properly send filters in body
    return this.http
      .post<
        PaginatedResponse<ImageListItemDTO>
      >(`${this.apiUrl}/${snapshotId}/images/search`, filters || null, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get project classes from snapshot
   * @param snapshotId The snapshot ID
   * @returns Observable of ProjectClass array from snapshot
   */
  getSnapshotClasses(snapshotId: number): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.apiUrl}/${snapshotId}/classes`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get project tags from snapshot
   * @param snapshotId The snapshot ID
   * @returns Observable of ProjectTag array from snapshot
   */
  getSnapshotTags(snapshotId: number): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.apiUrl}/${snapshotId}/tags`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get project metadata from snapshot
   * @param snapshotId The snapshot ID
   * @returns Observable of ProjectMetadata array from snapshot
   */
  getSnapshotMetadata(snapshotId: number): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.apiUrl}/${snapshotId}/metadata`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get project splits from snapshot
   * @param snapshotId The snapshot ID
   * @returns Observable of ProjectSplit array from snapshot
   */
  getSnapshotSplits(snapshotId: number): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.apiUrl}/${snapshotId}/splits`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new project from a snapshot's data
   * Requirements: 4.3
   * @param snapshotId The snapshot ID to create project from
   * @param projectName The name for the new project
   * @returns Observable of the created Project
   */
  createProjectFromSnapshot(
    snapshotId: number,
    projectName: string
  ): Observable<Project> {
    const request: CreateProjectFromSnapshotRequest = { projectName };
    return this.http
      .post<Project>(`${this.apiUrl}/${snapshotId}/create-project`, request)
      .pipe(catchError(this.handleError));
  }

  /**
   * Revert a project to a snapshot state
   * Requirements: 5.3
   * @param snapshotId The snapshot ID to revert to
   * @param projectId The project ID to revert
   * @returns Observable of void
   */
  revertToSnapshot(snapshotId: number, projectId: number): Observable<void> {
    const params = new HttpParams().set("projectId", projectId.toString());
    return this.http
      .post<void>(`${this.apiUrl}/${snapshotId}/revert`, null, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Download a snapshot's dataset as a ZIP file for training
   * Requirements: 6.2
   * @param snapshotId The snapshot ID to download
   * @returns Observable of Blob containing the ZIP file
   */
  downloadSnapshot(snapshotId: number): Observable<Blob> {
    return this.http
      .get(`${this.apiUrl}/${snapshotId}/download-dataset`, {
        responseType: "blob",
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a snapshot
   * Requirements: 7.3
   * @param snapshotId The snapshot ID to delete
   * @returns Observable of void
   */
  deleteSnapshot(snapshotId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/${snapshotId}`)
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
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    console.error("SnapshotService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
