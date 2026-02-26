import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { Model } from "app/models/landingai/model";
import {
  PredictionLabel,
  ImageLabel,
  GenerateModelRequest,
  GenerateModelResponse,
} from "app/models/landingai/adjust-threshold";

/**
 * Service for model detail operations
 * Requirements: 1.4, 4.4
 */
@Injectable({
  providedIn: "root",
})
export class ModelDetailService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  /**
   * Get model by ID
   * @param modelId Model ID
   * @returns Observable of Model
   */
  getModel(modelId: number): Observable<Model> {
    return this.http
      .get<Model>(`${this.actionUrl}landingai/models/${modelId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update model alias (rename)
   * @param modelId Model ID
   * @param newAlias New model alias
   * @returns Observable of updated Model
   */
  updateModelAlias(modelId: number, newAlias: string): Observable<Model> {
    return this.http
      .put<Model>(`${this.actionUrl}landingai/models/${modelId}/alias`, {
        modelAlias: newAlias,
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get prediction labels for a model filtered by evaluation set
   * Backend 根據 evaluationSet 參數過濾資料，只回傳指定 evaluation set 的資料
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (train, dev, test)
   * @returns Observable of PredictionLabel array
   * Requirements: 26.5, 31.1, 35.3
   */
  getPredictionLabels(
    modelId: number,
    evaluationSet: string
  ): Observable<PredictionLabel[]> {
    return this.http
      .get<
        PredictionLabel[]
      >(`${this.actionUrl}landingai/confusion-matrix/${modelId}/${evaluationSet}/prediction-labels`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get ground truth labels for a model's project filtered by evaluation set
   * Backend 根據 evaluationSet 參數過濾資料，只回傳指定 evaluation set 的資料
   * @param modelId Model ID (用來找到對應的 snapshot)
   * @param evaluationSet Evaluation set (train, dev, test)
   * @returns Observable of ImageLabel array
   * Requirements: 26.5, 31.1, 35.3
   */
  getGroundTruthLabels(
    modelId: number,
    evaluationSet: string
  ): Observable<ImageLabel[]> {
    return this.http
      .get<
        ImageLabel[]
      >(`${this.actionUrl}landingai/confusion-matrix/${modelId}/${evaluationSet}/ground-truth-labels`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Generate a new model with adjusted confidence threshold
   * @param sourceModelId Source model ID
   * @param request Generate model request with new threshold and metrics
   * @returns Observable of GenerateModelResponse
   * Requirements: 31.1, 33.6
   */
  generateModelWithNewThreshold(
    sourceModelId: number,
    request: GenerateModelRequest
  ): Observable<GenerateModelResponse> {
    return this.http
      .post<GenerateModelResponse>(
        `${this.actionUrl}landingai/models/${sourceModelId}/generate-with-threshold`,
        request
      )
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred";

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }

    console.error("ModelDetailService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
