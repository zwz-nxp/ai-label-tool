import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { ProjectSplit } from "app/models/landingai/project-split.model";
import { Configuration } from "app/utils/configuration";

@Injectable({
  providedIn: "root",
})
export class ProjectSplitService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl =
      this.configuration.ServerWithApiUrl + "landingai/project-splits/";
  }

  /**
   * Get a single project split by ID
   * @param splitId The project split ID
   * @returns Observable of ProjectSplit
   */
  public getProjectSplitById(splitId: number): Observable<ProjectSplit> {
    return this.http
      .get<ProjectSplit>(`${this.actionUrl}${splitId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all project splits for a specific project
   * @param projectId The project ID
   * @returns Observable of ProjectSplit array
   */
  public getProjectSplitsByProjectId(
    projectId: number
  ): Observable<ProjectSplit[]> {
    return this.http
      .get<ProjectSplit[]>(`${this.actionUrl}project/${projectId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new project split configuration
   * @param projectSplit The project split to create
   * @returns Observable of the created ProjectSplit
   */
  public createProjectSplit(
    projectSplit: Partial<ProjectSplit>
  ): Observable<ProjectSplit> {
    return this.http
      .post<ProjectSplit>(this.actionUrl, projectSplit)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing project split
   * @param splitId The project split ID
   * @param projectSplit The project split data to update
   * @returns Observable of the updated ProjectSplit
   */
  public updateProjectSplit(
    splitId: number,
    projectSplit: Partial<ProjectSplit>
  ): Observable<ProjectSplit> {
    return this.http
      .put<ProjectSplit>(`${this.actionUrl}${splitId}`, projectSplit)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a project split
   * @param splitId The project split ID to delete
   * @returns Observable of void
   */
  public deleteProjectSplit(splitId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.actionUrl}${splitId}`)
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
      }
    }

    console.error("ProjectSplitService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
