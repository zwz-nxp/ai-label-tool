import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";
import { environment } from "../../../environments/environment";
import { Snapshot } from "app/state/landingai/ai-training/training.state";
import { TrainingRequest } from "app/models/landingai/training-config.model";

/**
 * Backend snapshot response interface
 * Maps to the actual API response structure
 */
interface SnapshotResponse {
  id: number;
  projectId: number;
  name: string; // Changed from snapshotName to match backend DTO
  description: string;
  imageCount: number;
  labelCount: number;
  createdAt: Date;
  createdBy: string;
}

export interface TrainingRecord {
  id: number;
  projectId: number;
  status: string;
  modelAlias?: string;
  trackId?: string;
  epochs?: number;
  modelSize?: string;
  trainingCount?: number;
  devCount?: number;
  testCount?: number;
  startedAt: Date;
  completedAt?: Date;
  createdBy: string;
}

export interface TrainingStatus {
  id: number;
  status: string;
  progress: number;
  currentPhase: string;
  startedAt: Date;
  estimatedCompletionAt?: Date;
  errorMessage?: string;
}

@Injectable({
  providedIn: "root",
})
export class TrainingService {
  private readonly apiUrl = `${environment.server}api/landingai/training`;

  constructor(private http: HttpClient) {}

  /**
   * Start training with the provided parameters
   */
  startTraining(request: TrainingRequest): Observable<TrainingRecord> {
    return this.http.post<TrainingRecord>(`${this.apiUrl}/start`, request);
  }

  /**
   * Get training status by ID
   */
  getTrainingStatus(trainingId: number): Observable<TrainingStatus> {
    return this.http.get<TrainingStatus>(`${this.apiUrl}/${trainingId}/status`);
  }

  /**
   * Get available snapshots for a project
   *
   * @param projectId Project ID
   * @returns Observable of snapshots with mapped property names
   */
  getSnapshots(projectId: number): Observable<Snapshot[]> {
    const params = new HttpParams().set("projectId", projectId.toString());
    return this.http
      .get<SnapshotResponse[]>(`${this.apiUrl}/snapshots`, { params })
      .pipe(
        map((responses) =>
          responses.map((response) => ({
            id: response.id,
            name: response.name,
            createdAt: response.createdAt,
            imageCount: response.imageCount,
          }))
        )
      );
  }
}
