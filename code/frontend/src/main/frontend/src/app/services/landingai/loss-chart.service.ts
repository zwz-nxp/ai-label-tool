import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError, of } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { LossChart } from "app/models/landingai/loss-chart";

/**
 * Service for retrieving loss chart data
 * Requirements: 7.3, 7.10
 */
@Injectable({
  providedIn: "root",
})
export class LossChartService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  /**
   * Get loss chart data for a specific model
   * @param modelId Model ID
   * @returns Observable of LossChart array ordered by created_at ascending
   */
  getLossChartData(modelId: number): Observable<LossChart[]> {
    return this.http
      .get<LossChart[]>(`${this.actionUrl}landingai/charts/loss/${modelId}`)
      .pipe(
        catchError((error) => {
          console.error("Error loading loss chart data:", error);
          return of([]); // Return empty array on error
        })
      );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred loading loss chart data";

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error("LossChartService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
