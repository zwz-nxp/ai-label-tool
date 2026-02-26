import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import {
  ConfusionMatrixData,
  CellDetailResponse,
  ImageWithLabels,
} from "app/models/landingai/confusion-matrix.model";

/**
 * Service for confusion matrix operations
 * Calls backend API endpoints for confusion matrix data
 */
@Injectable({
  providedIn: "root",
})
export class ConfusionMatrixService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl =
      this.configuration.ServerWithApiUrl + "landingai/confusion-matrix/";
  }

  /**
   * Get confusion matrix data for a model and evaluation set
   * @param modelId The model ID
   * @param evaluationSet The evaluation set (train, dev, test)
   * @returns Observable of ConfusionMatrixData
   */
  public getConfusionMatrix(
    modelId: number,
    evaluationSet: "train" | "dev" | "test"
  ): Observable<ConfusionMatrixData> {
    return this.http
      .get<ConfusionMatrixData>(`${this.actionUrl}${modelId}/${evaluationSet}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get cell detail (images for specific GT×Pred combination)
   * @param modelId The model ID
   * @param evaluationSet The evaluation set (train, dev, test)
   * @param gtClassId Ground truth class ID
   * @param predClassId Prediction class ID
   * @returns Observable of CellDetailResponse
   */
  public getCellDetail(
    modelId: number,
    evaluationSet: "train" | "dev" | "test",
    gtClassId: number,
    predClassId: number
  ): Observable<CellDetailResponse> {
    const params = {
      gtClassId: gtClassId.toString(),
      predClassId: predClassId.toString(),
    };

    return this.http
      .get<CellDetailResponse>(
        `${this.actionUrl}${modelId}/${evaluationSet}/cell`,
        { params }
      )
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all images in evaluation set with labels and predictions
   * @param modelId The model ID
   * @param evaluationSet The evaluation set (train, dev, test)
   * @returns Observable of ImageWithLabels array
   */
  public getAllImages(
    modelId: number,
    evaluationSet: "train" | "dev" | "test"
  ): Observable<ImageWithLabels[]> {
    return this.http
      .get<
        ImageWithLabels[]
      >(`${this.actionUrl}${modelId}/${evaluationSet}/all-images`)
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
      if (error.status === 404) {
        errorMessage = "Model or data not found";
      } else if (error.status === 400) {
        errorMessage = "Invalid request parameters";
      } else if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else {
        errorMessage = `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    console.error("ConfusionMatrixService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
