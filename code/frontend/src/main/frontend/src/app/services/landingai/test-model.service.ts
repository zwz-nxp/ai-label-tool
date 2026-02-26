import { Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import {
  TestModelRequest,
  TestModelResponse,
} from "../../models/landingai/test-model.model";
import { Configuration } from "../../utils/configuration";

/**
 * Service for testing machine learning models with images.
 *
 * Handles communication with the Databricks API for model inference.
 */
@Injectable({
  providedIn: "root",
})
export class TestModelService {
  private readonly apiUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    // Interface service URL (Port 8083)
    let interfaceBaseUrl = this.configuration.Server.replace(":8080", ":8083");
    interfaceBaseUrl = interfaceBaseUrl.replace(/\/$/, "");
    this.apiUrl = `${interfaceBaseUrl}/infc/databricks/model/test`;
  }

  /**
   * Tests a model with provided images and returns prediction results.
   *
   * @param request Test model request with zip file info
   * @returns Observable of test model response with predictions
   */
  testModel(request: any): Observable<any> {
    console.log("TestModelService: Calling API with request:", {
      trackId: request.trackId,
      zipFilenames: request.zipFilenames,
      zipPath: request.zipPath,
    });

    return this.http.post<any>(this.apiUrl, request).pipe(
      tap((response) => {
        console.log("TestModelService: Received response:", {
          trackId: response.trackId,
          runId: response.runId,
          errorMessage: response.errorMessage,
        });
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Gets test results using trackId and runId.
   *
   * @param trackId Training track ID
   * @param runId Databricks run ID
   * @returns Observable of test results file paths response
   */
  getTestResults(trackId: string, runId: number): Observable<any> {
    const url = `${this.apiUrl}/results/files`;
    const params = { trackId, runId: runId.toString() };

    console.log("TestModelService: Fetching test results:", { trackId, runId });

    return this.http.get<any>(url, { params }).pipe(
      tap((response) => {
        console.log("TestModelService: Test results response:", {
          status: response.status,
          testPredictions: response.test_predictions,
        });
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Loads predictions from a JSON file path.
   * Uses the backend endpoint to read file content.
   *
   * @param filePath Local file path to predictions JSON
   * @returns Observable of predictions array
   */
  loadPredictionsFile(filePath: string): Observable<any[]> {
    console.log("TestModelService: Loading predictions file:", filePath);

    // 使用新的 backend endpoint 來讀取檔案內容
    let interfaceBaseUrl = this.configuration.Server.replace(":8080", ":8083");
    interfaceBaseUrl = interfaceBaseUrl.replace(/\/$/, "");
    const url = `${interfaceBaseUrl}/infc/databricks/file/content`;
    const params = { path: filePath };

    console.log("TestModelService: Calling file content endpoint:", url);

    return this.http.get<any[]>(url, { params }).pipe(
      tap((predictions) => {
        console.log(
          "TestModelService: Loaded predictions:",
          Array.isArray(predictions) ? predictions.length : "not an array"
        );
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Handles HTTP errors from the API.
   *
   * @param error HTTP error response
   * @returns Observable that throws a user-friendly error message
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred while testing the model.";

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      console.error(
        "TestModelService: Client-side error:",
        error.error.message
      );
      errorMessage = `Network error: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      console.error(
        `TestModelService: Backend error: ${error.status}, ` +
          `body: ${JSON.stringify(error.error)}`
      );

      switch (error.status) {
        case 400:
          errorMessage =
            "Invalid request. Please check your input and try again.";
          break;
        case 401:
          errorMessage = "Authentication failed. Please log in and try again.";
          break;
        case 404:
          errorMessage = "Model not found. Please verify the model exists.";
          break;
        case 429:
          errorMessage = "Too many requests. Please try again later.";
          break;
        case 500:
        case 502:
        case 503:
          errorMessage = "Server error. Please try again later.";
          break;
        case 504:
          errorMessage = "Request timed out. Please try again.";
          break;
        default:
          errorMessage = `Error ${error.status}: ${error.message}`;
      }
    }

    return throwError(() => new Error(errorMessage));
  }
}
