/**
 * Split Service
 *
 * Provides methods for split-related API calls.
 *
 * Requirement 24.3: THE Split_Service SHALL provide getSplitPreview method to fetch split preview data
 * Requirement 24.4: THE Split_Service SHALL provide assignSplit method to trigger automatic split assignment
 * Requirement 24.5: THE Split_Service SHALL provide getProjectClasses method to fetch project class list
 * Requirement 24.6: THE System SHALL handle HTTP errors and transform them into user-friendly error messages
 */

import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import {
  SplitDistribution,
  SplitPreview,
} from "app/state/landingai/ai-training";
import { ProjectClass } from "app/state/landingai/ai-training/training.state";
import { environment } from "../../../environments/environment";

@Injectable({ providedIn: "root" })
export class SplitService {
  private readonly apiUrl = `${environment.server}api/landingai/project-splits`;

  constructor(private http: HttpClient) {}

  /**
   * Get split preview data for a project
   *
   * Requirement 24.3: THE Split_Service SHALL provide getSplitPreview method
   *
   * @param projectId Project ID
   * @param snapshotId Optional snapshot ID (null for current version)
   * @returns Observable of split preview data
   */
  getSplitPreview(
    projectId: number,
    snapshotId?: number
  ): Observable<SplitPreview> {
    let params = new HttpParams().set("projectId", projectId.toString());
    if (snapshotId !== undefined && snapshotId !== null) {
      params = params.set("snapshotId", snapshotId.toString());
    }
    return this.http.get<SplitPreview>(`${this.apiUrl}/preview`, { params });
  }

  /**
   * Assign split to unassigned images based on distribution
   *
   * Requirement 24.4: THE Split_Service SHALL provide assignSplit method
   *
   * @param projectId Project ID
   * @param distribution Target split distribution
   * @returns Observable of void
   */
  assignSplit(
    projectId: number,
    distribution: SplitDistribution
  ): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/assign`, {
      projectId,
      distribution,
    });
  }

  /**
   * Get project classes for a project
   *
   * Requirement 24.5: THE Split_Service SHALL provide getProjectClasses method
   *
   * @param projectId Project ID
   * @returns Observable of project classes
   */
  getProjectClasses(projectId: number): Observable<ProjectClass[]> {
    return this.http.get<ProjectClass[]>(
      `${environment.server}api/landingai/project-classes/project/${projectId}`
    );
  }
}
