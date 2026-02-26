import { Injectable } from "@angular/core";
import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { Configuration } from "../../utils/configuration";

/**
 * Response interface for download model API
 * 對應後端 DownloadModelResponse.java
 */
export interface DownloadModelResponse {
  modelFullName: string;
  version: number;
  trackId: string;
  artifact: {
    downloadUrl: string;
  };
}

/**
 * Service for downloading trained machine learning models.
 *
 * Handles communication with the Databricks API for model download.
 */
@Injectable({
  providedIn: "root",
})
export class DownloadModelService {
  private readonly apiUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    // Interface service URL (Port 8083)
    let interfaceBaseUrl = this.configuration.Server.replace(":8080", ":8083");
    interfaceBaseUrl = interfaceBaseUrl.replace(/\/$/, "");
    this.apiUrl = `${interfaceBaseUrl}/infc/databricks/model/download`;
  }

  /**
   * Downloads a trained model from Databricks.
   *
   * @param modelName Model full name (from modelAlias field)
   * @param version Model version number
   * @param trackId Training job track ID
   * @returns Observable of download model response with download URL
   */
  downloadModel(
    modelName: string,
    version: number,
    trackId: string
  ): Observable<DownloadModelResponse> {
    console.log("DownloadModelService: Calling API with params:", {
      modelName,
      version,
      trackId,
    });

    const params = new HttpParams()
      .set("model_name", modelName)
      .set("version", version.toString())
      .set("track_id", trackId);

    return this.http.get<DownloadModelResponse>(this.apiUrl, { params }).pipe(
      tap((response) => {
        console.log("DownloadModelService: Received response:", {
          modelFullName: response.modelFullName,
          version: response.version,
          hasDownloadUrl: !!response.artifact?.downloadUrl,
          downloadUrl: response.artifact?.downloadUrl,
        });
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
    let errorMessage = "An error occurred while downloading the model.";

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      console.error(
        "DownloadModelService: Client-side error:",
        error.error.message
      );
      errorMessage = `Network error: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      console.error(
        `DownloadModelService: Backend error: ${error.status}, ` +
          `body: ${JSON.stringify(error.error)}`
      );

      switch (error.status) {
        case 400:
          errorMessage =
            "Invalid request. Please check the model information and try again.";
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
