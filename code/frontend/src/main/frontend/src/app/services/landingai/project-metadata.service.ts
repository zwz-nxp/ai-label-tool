import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ProjectMetadata } from "../../models/landingai/project-metadata.model";
import { Configuration } from "../../utils/configuration";

@Injectable({
  providedIn: "root",
})
export class ProjectMetadataService {
  private baseUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.baseUrl = `${_configuration.ServerWithApiUrl}landingai/project-metadata`;
  }

  getMetadataByProjectId(projectId: number): Observable<ProjectMetadata[]> {
    return this.http.get<ProjectMetadata[]>(
      `${this.baseUrl}/project/${projectId}`
    );
  }

  getMetadataById(id: number): Observable<ProjectMetadata> {
    return this.http.get<ProjectMetadata>(`${this.baseUrl}/${id}`);
  }

  createMetadata(
    projectId: number,
    metadata: Omit<ProjectMetadata, "id" | "project">
  ): Observable<ProjectMetadata> {
    return this.http.post<ProjectMetadata>(
      `${this.baseUrl}?projectId=${projectId}`,
      metadata
    );
  }

  updateMetadata(
    id: number,
    metadata: Partial<ProjectMetadata>
  ): Observable<ProjectMetadata> {
    return this.http.put<ProjectMetadata>(`${this.baseUrl}/${id}`, metadata);
  }

  deleteMetadata(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
