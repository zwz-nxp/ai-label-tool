import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ProjectTag } from "../../models/landingai/project-tag.model";
import { Configuration } from "../../utils/configuration";

@Injectable({
  providedIn: "root",
})
export class ProjectTagService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.apiUrl = `${_configuration.ServerWithApiUrl}landingai/project-tags`;
  }

  /**
   * Create a new tag
   */
  createTag(
    projectId: number,
    tag: Omit<ProjectTag, "id">
  ): Observable<ProjectTag> {
    return this.http.post<ProjectTag>(
      `${this.apiUrl}?projectId=${projectId}`,
      tag
    );
  }

  /**
   * Get all tags for a project
   */
  getTagsByProjectId(projectId: number): Observable<ProjectTag[]> {
    return this.http.get<ProjectTag[]>(`${this.apiUrl}/project/${projectId}`);
  }

  /**
   * Update an existing tag
   */
  updateTag(tagId: number, tag: Partial<ProjectTag>): Observable<ProjectTag> {
    return this.http.put<ProjectTag>(`${this.apiUrl}/${tagId}`, tag);
  }

  /**
   * Delete a tag
   */
  deleteTag(tagId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${tagId}`);
  }
}
