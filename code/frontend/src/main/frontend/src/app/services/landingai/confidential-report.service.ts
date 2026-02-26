import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { ConfidentialReport } from "app/models/landingai/confidential-report";

/**
 * Service for confidential report operations
 * Requirements: 15.2
 */
@Injectable({
  providedIn: "root",
})
export class ConfidentialReportService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  /**
   * Get confidential report by model ID
   * @param modelId Model ID
   * @returns Observable of ConfidentialReport
   */
  getConfidentialReport(modelId: number): Observable<ConfidentialReport> {
    return this.http
      .get<ConfidentialReport>(
        `${this.actionUrl}confidential-reports/model/${modelId}`
      )
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred loading confidential report";

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }

    console.error("ConfidentialReportService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
