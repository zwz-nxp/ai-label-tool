import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { Model } from "app/models/landingai/model";

@Injectable({
  providedIn: "root",
})
export class ModelService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl = this.configuration.ServerWithApiUrl + "landingai/models/";
  }

  /**
   * Get all models for a specific project
   * @param projectId The project ID
   * @returns Observable of Model array
   */
  public getModelsByProjectId(projectId: number): Observable<Model[]> {
    return this.http
      .get<Model[]>(`${this.actionUrl}project/${projectId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a single model by ID
   * @param modelId The model ID
   * @returns Observable of Model
   */
  public getModelById(modelId: number): Observable<Model> {
    return this.http
      .get<Model>(`${this.actionUrl}${modelId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a model by ID (soft delete - sets status to INACTIVE)
   * @param modelId The model ID to delete
   * @returns Observable of void
   */
  public deleteModel(modelId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.actionUrl}${modelId}`)
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

    console.error("ModelService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
