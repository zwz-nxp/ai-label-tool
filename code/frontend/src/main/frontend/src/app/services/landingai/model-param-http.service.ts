import { Injectable } from "@angular/core";
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { environment } from "environments/environment";
import {
  ModelParam,
  ModelParamCreateRequest,
  ModelParamUpdateRequest,
} from "app/models/landingai/model-param.model";

/**
 * HTTP Service for Model Parameter Configuration
 * Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1
 */
@Injectable({
  providedIn: "root",
})
export class ModelParamHttpService {
  private readonly apiUrl = `${environment.server}api/landingai/model-params`;

  constructor(private http: HttpClient) {}

  /**
   * Get all model parameters for a location
   * Requirements: 2.1
   * @param locationId The location ID
   * @returns Observable of ModelParam array
   */
  getModelParams(locationId: number): Observable<ModelParam[]> {
    const params = new HttpParams().set("locationId", locationId.toString());
    return this.http
      .get<ModelParam[]>(this.apiUrl, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get model parameters filtered by model type
   * Requirements: 2.3
   * @param locationId The location ID
   * @param modelType The model type to filter by
   * @returns Observable of ModelParam array
   */
  getModelParamsByType(
    locationId: number,
    modelType: string
  ): Observable<ModelParam[]> {
    const params = new HttpParams()
      .set("locationId", locationId.toString())
      .set("modelType", modelType);
    return this.http
      .get<ModelParam[]>(this.apiUrl, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Search model parameters by name
   * Requirements: 2.4
   * @param locationId The location ID
   * @param modelName The model name search term
   * @returns Observable of ModelParam array
   */
  searchModelParams(
    locationId: number,
    modelName: string
  ): Observable<ModelParam[]> {
    const params = new HttpParams()
      .set("locationId", locationId.toString())
      .set("modelName", modelName);
    return this.http
      .get<ModelParam[]>(`${this.apiUrl}/search`, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get a single model parameter by ID
   * Requirements: 6.1
   * @param id The model parameter ID
   * @returns Observable of ModelParam
   */
  getModelParamById(id: number): Observable<ModelParam> {
    return this.http
      .get<ModelParam>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new model parameter
   * Requirements: 3.2
   * @param request The create request
   * @param locationId The location ID
   * @param userId The user ID
   * @returns Observable of created ModelParam
   */
  createModelParam(
    request: ModelParamCreateRequest,
    locationId: number,
    userId: string
  ): Observable<ModelParam> {
    const params = new HttpParams()
      .set("locationId", locationId.toString())
      .set("userId", userId);
    return this.http
      .post<ModelParam>(this.apiUrl, request, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing model parameter
   * Requirements: 4.2
   * @param id The model parameter ID
   * @param request The update request
   * @param userId The user ID
   * @returns Observable of updated ModelParam
   */
  updateModelParam(
    id: number,
    request: ModelParamUpdateRequest,
    userId: string
  ): Observable<ModelParam> {
    const params = new HttpParams().set("userId", userId);
    return this.http
      .put<ModelParam>(`${this.apiUrl}/${id}`, request, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a model parameter
   * Requirements: 5.2
   * @param id The model parameter ID
   * @param userId The user ID
   * @returns Observable of void
   */
  deleteModelParam(id: number, userId: string): Observable<void> {
    const params = new HttpParams().set("userId", userId);
    return this.http
      .delete<void>(`${this.apiUrl}/${id}`, { params })
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
      } else if (error.status === 400) {
        errorMessage = "Invalid request. Please check your input.";
      } else if (error.status === 404) {
        errorMessage = "Model parameter not found.";
      } else {
        errorMessage = `Server error: ${error.status} - ${error.statusText}`;
      }
    }

    console.error("ModelParamHttpService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
