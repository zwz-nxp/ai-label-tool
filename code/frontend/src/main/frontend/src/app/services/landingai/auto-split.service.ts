import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { Configuration } from "../../utils/configuration";
import {
  AutoSplitStats,
  AutoSplitRequest,
} from "../../models/landingai/auto-split.model";

@Injectable({
  providedIn: "root",
})
export class AutoSplitService {
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private config: Configuration
  ) {
    this.apiUrl = this.config.ServerWithApiUrl + "landingai/auto-split";
  }

  /**
   * Get auto-split statistics
   */
  getAutoSplitStats(
    projectId: number,
    includeAssigned: boolean
  ): Observable<AutoSplitStats> {
    const params = new HttpParams()
      .set("projectId", projectId.toString())
      .set("includeAssigned", includeAssigned.toString());

    return this.http.get<AutoSplitStats>(`${this.apiUrl}/stats`, { params });
  }

  /**
   * Assign splits to images
   */
  assignSplits(request: AutoSplitRequest): Observable<number> {
    return this.http.post<number>(`${this.apiUrl}/assign`, request);
  }
}
