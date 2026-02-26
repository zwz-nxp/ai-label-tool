import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError, of } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { ValidationChart } from "app/models/landingai/validation-chart";

/**
 * Service for retrieving validation chart data
 * Requirements: 7.4, 7.11
 */
@Injectable({
  providedIn: "root",
})
export class ValidationChartService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  /**
   * Get validation chart data for a specific model
   * @param modelId Model ID
   * @returns Observable of ValidationChart array ordered by created_at ascending
   */
  getValidationChartData(modelId: number): Observable<ValidationChart[]> {
    return this.http
      .get<
        ValidationChart[]
      >(`${this.actionUrl}landingai/charts/validation/${modelId}`)
      .pipe(
        catchError((error) => {
          console.error("Error loading validation chart data:", error);
          return of([]); // Return empty array on error
        })
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred loading validation chart data";

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error("ValidationChartService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
