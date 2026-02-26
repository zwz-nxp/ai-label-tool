import {
  HttpClient,
  HttpErrorResponse,
  HttpParams,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, throwError } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { Configuration } from "app/utils/configuration";
import {
  Project,
  ProjectCreateRequest,
  ProjectListItem,
} from "app/models/landingai/project";

@Injectable({
  providedIn: "root",
})
export class ProjectService {
  private readonly actionUrl: string;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  /**
   * Get projects for the current user and location
   * @param viewAll - If true, returns all projects in the location; if false, returns only user's projects
   * @param locationId - The location ID to filter projects
   * @returns Observable of ProjectListItem array
   */
  public getProjects(
    viewAll: boolean,
    locationId: number
  ): Observable<ProjectListItem[]> {
    const params = new HttpParams()
      .set("viewAll", viewAll.toString())
      .set("locationId", locationId.toString());

    return this.http
      .get<any[]>(this.actionUrl + "landingai/projects", { params })
      .pipe(
        catchError(this.handleError),
        // Map the response to convert byte array to data URL
        map((projects) =>
          projects.map((project) => this.mapToProjectListItem(project))
        )
      );
  }

  /**
   * Create a new project
   * @param request - Project creation request with name and type
   * @param locationId - The location ID for the project
   * @returns Observable of created Project
   */
  public createProject(
    request: ProjectCreateRequest,
    locationId: number
  ): Observable<Project> {
    const params = new HttpParams().set("locationId", locationId.toString());

    return this.http
      .post<Project>(this.actionUrl + "landingai/projects", request, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get project details by ID
   * @param id - Project ID
   * @returns Observable of Project
   */
  public getProjectById(id: number): Observable<Project> {
    return this.http
      .get<Project>(this.actionUrl + `landingai/projects/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Update project name, model name, and group name
   * @param id - Project ID
   * @param name - New project name
   * @param modelName - New model name
   * @param groupName - New group name (optional)
   * @returns Observable of updated Project
   */
  public updateProject(
    id: number,
    name: string,
    modelName: string,
    groupName?: string
  ): Observable<Project> {
    const updateRequest = { name, modelName, groupName };
    return this.http
      .put<Project>(this.actionUrl + `landingai/projects/${id}`, updateRequest)
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete a project
   * @param id - Project ID
   * @returns Observable of void
   */
  public deleteProject(id: number): Observable<void> {
    return this.http
      .delete<void>(this.actionUrl + `landingai/projects/${id}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Map backend response to ProjectListItem, converting thumbnail byte array to data URL
   * @param data - Raw project data from backend
   * @returns ProjectListItem with thumbnail URL
   */
  private mapToProjectListItem(data: any): ProjectListItem {
    let thumbnailUrl = "";

    // The backend sends the thumbnail as a base64 string, not a byte array
    if (data.firstImageThumbnail && data.firstImageThumbnail.length > 0) {
      try {
        const base64Data = data.firstImageThumbnail;
        // Detect image format from base64 signature
        // JPEG starts with /9j/, PNG starts with iVBOR
        const imageType = base64Data.startsWith("/9j/") ? "jpeg" : "png";
        thumbnailUrl = `data:image/${imageType};base64,${base64Data}`;
      } catch (error) {
        console.error("Error setting thumbnail URL:", error);
      }
    }

    return {
      id: data.id,
      name: data.name,
      type: data.type,
      modelName: data.modelName || "",
      groupName: data.groupName || undefined,
      createdBy: data.createdBy,
      createdAt: new Date(data.createdAt),
      imageCount: data.imageCount || 0,
      labelCount: data.labelCount || 0,
      modelCount: data.modelCount || 0,
      thumbnailUrl: thumbnailUrl,
    };
  }

  /**
   * Handle HTTP errors
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

    console.error("ProjectService Error:", errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
