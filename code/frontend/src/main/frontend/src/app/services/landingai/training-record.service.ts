import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import { TrainingRecord } from "app/models/landingai/training-record";

/**
 * Service for training record operations
 * Requirements: 6.1
 */
@Injectable({
  providedIn: "root",
})
export class TrainingRecordService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  /**
   * Get training record by ID
   * @param trainingRecordId Training record ID
   * @returns Observable of TrainingRecord
   */
  getTrainingRecord(trainingRecordId: number): Observable<TrainingRecord> {
    return this.http
      .get<TrainingRecord>(
        `${this.actionUrl}training-records/${trainingRecordId}`
      )
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = "An error occurred loading training record";

    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }
    }

    console.error("TrainingRecordService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
