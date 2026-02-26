import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";

export interface ImageLabel {
  id?: number;
  imageId: number;
  classId: number;
  position: string | null; // JSON string or null for classification
  confidenceRate?: number;
  annotationType: string;
  createdAt?: Date;
  createdBy?: string;
}

@Injectable({
  providedIn: "root",
})
export class LabelService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    this.actionUrl = this.configuration.ServerWithApiUrl + "landingai/labels/";
  }

  /**
   * Save a single label
   * @param label The label to save
   * @returns Observable of the created ImageLabel
   */
  public saveLabel(label: ImageLabel): Observable<ImageLabel> {
    return this.http
      .post<ImageLabel>(`${this.actionUrl}create`, label)
      .pipe(catchError(this.handleError));
  }

  /**
   * Save multiple labels in batch
   * @param labels Array of labels to save
   * @returns Observable of created ImageLabel array
   */
  public saveBatch(labels: ImageLabel[]): Observable<ImageLabel[]> {
    return this.http
      .post<ImageLabel[]>(`${this.actionUrl}batch`, labels)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all labels for a specific image
   * @param imageId The image ID
   * @returns Observable of ImageLabel array
   */
  public getLabelsByImageId(imageId: number): Observable<ImageLabel[]> {
    return this.http
      .get<ImageLabel[]>(`${this.actionUrl}image/${imageId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing label
   * @param labelId The label ID to update
   * @param label The updated label data
   * @returns Observable of updated ImageLabel
   */
  public updateLabel(
    labelId: number,
    label: ImageLabel
  ): Observable<ImageLabel> {
    return this.http
      .put<ImageLabel>(`${this.actionUrl}${labelId}`, label)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a single label
   * @param labelId The label ID to delete
   * @returns Observable of void
   */
  public deleteLabel(labelId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.actionUrl}${labelId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete all labels for a specific image
   * @param imageId The image ID
   * @returns Observable of void
   */
  public deleteLabelsByImageId(imageId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.actionUrl}image/${imageId}`)
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

    console.error("LabelService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
