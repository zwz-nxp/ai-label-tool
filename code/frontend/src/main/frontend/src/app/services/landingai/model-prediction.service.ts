import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { ImageLabel } from "./label.service";

export interface ModelPredictionStatus {
  enabled: boolean;
}

@Injectable({
  providedIn: "root",
})
export class ModelPredictionService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl =
      this.configuration.ServerWithApiUrl + "landingai/predictions/";
  }

  /**
   * Generate pre-annotations for an image using ML model
   * @param imageId The image ID
   * @param projectId The project ID
   * @returns Observable of ImageLabel array with pre-annotations
   */
  public generatePreAnnotations(
    imageId: number,
    projectId: number
  ): Observable<ImageLabel[]> {
    return this.http
      .post<
        ImageLabel[]
      >(`${this.actionUrl}generate?imageId=${imageId}&projectId=${projectId}`, null)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get prediction labels for an image and model
   * @param imageId The image ID
   * @param modelId The model ID
   * @returns Observable of ImageLabel array with prediction labels
   */
  public getPredictionsByImageAndModel(
    imageId: number,
    modelId: number
  ): Observable<ImageLabel[]> {
    return this.http
      .get<ImageLabel[]>(`${this.actionUrl}image/${imageId}/model/${modelId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Check if model prediction is enabled
   * @returns Observable of ModelPredictionStatus
   */
  public getStatus(): Observable<ModelPredictionStatus> {
    return this.http
      .get<ModelPredictionStatus>(`${this.actionUrl}status`)
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

    console.error("ModelPredictionService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
