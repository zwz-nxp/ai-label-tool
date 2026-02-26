import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { Configuration } from "../../utils/configuration";

/**
 * 檔案上傳回應介面
 */
export interface FileUploadResponse {
  status: string;
  filename: string;
  zipPath: string;
  fullPath: string;
  size: string;
  error?: string;
}

/**
 * Service for uploading files to the server.
 * Handles zip file uploads for Test Model feature.
 */
@Injectable({
  providedIn: "root",
})
export class FileUploadService {
  private readonly uploadUrl: string;
  private readonly deleteUrl: string;

  constructor(
    private http: HttpClient,
    private configuration: Configuration
  ) {
    // Operational service URL (Port 8081)
    let operationalBaseUrl = this.configuration.Server.replace(
      ":8080",
      ":8081"
    );
    operationalBaseUrl = operationalBaseUrl.replace(/\/$/, "");
    this.uploadUrl = `${operationalBaseUrl}/operational/landingai/files/upload`;
    this.deleteUrl = `${operationalBaseUrl}/operational/landingai/files/delete`;
  }

  /**
   * 上傳 zip 檔案到伺服器
   *
   * @param file Zip 檔案
   * @param trackId Track ID
   * @returns Observable of upload response
   */
  uploadZipFile(file: File, trackId: string): Observable<FileUploadResponse> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("trackId", trackId);

    console.log("FileUploadService: Uploading file:", {
      filename: file.name,
      size: file.size,
      trackId: trackId,
    });

    return this.http.post<FileUploadResponse>(this.uploadUrl, formData);
  }

  /**
   * 刪除已上傳的檔案
   *
   * @param trackId Track ID
   * @param filename 檔案名稱
   * @returns Observable of delete response
   */
  deleteFile(
    trackId: string,
    filename: string
  ): Observable<{ status: string; message: string }> {
    const params = {
      trackId: trackId,
      filename: filename,
    };

    console.log("FileUploadService: Deleting file:", params);

    return this.http.delete<{ status: string; message: string }>(
      this.deleteUrl,
      { params }
    );
  }
}
