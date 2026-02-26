import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { ProjectClass } from "../../models/landingai/project-class.model";
import { Configuration } from "../../utils/configuration";

@Injectable({
  providedIn: "root",
})
export class ProjectClassService {
  private readonly actionUrl: string;

  constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  getClassesByProjectId(projectId: number): Observable<ProjectClass[]> {
    return this.http.get<ProjectClass[]>(
      `${this.actionUrl}landingai/project-classes/project/${projectId}`
    );
  }

  createClass(
    projectId: number,
    projectClass: Omit<ProjectClass, "id" | "project">
  ): Observable<ProjectClass> {
    return this.http.post<ProjectClass>(
      `${this.actionUrl}landingai/project-classes?projectId=${projectId}`,
      projectClass
    );
  }

  updateClass(
    classId: number,
    projectClass: Partial<ProjectClass>
  ): Observable<ProjectClass> {
    return this.http.put<ProjectClass>(
      `${this.actionUrl}landingai/project-classes/${classId}`,
      projectClass
    );
  }

  deleteClass(classId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.actionUrl}landingai/project-classes/${classId}`
    );
  }
}
