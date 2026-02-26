import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable } from "rxjs";
import { MassUploadType } from "app/models/mass-upload-type";
import { Configuration } from "../configuration";
import { GenericSearchArguments } from "app/models/generic-search";
import { MassUploadResponse } from "app/models/mass-upload";

@Injectable({
  providedIn: "root",
})
export class MassUploadService {
  public metaData = new BehaviorSubject<MassUploadType[]>([]);
  private readonly api: string;

  public constructor(
    private http: HttpClient,
    configuration: Configuration
  ) {
    this.api = configuration.ServerWithApiUrl;
    http
      .get<MassUploadType[]>(`${this.api}massUpload/metaData`)
      .subscribe((data) => this.metaData.next(data));
  }

  public getTemplateUrl(type: string): string {
    return `${this.api}massUpload/template/${type}`;
  }

  public getTemplatePrefilledUrl(
    type: string,
    searchValues: GenericSearchArguments
  ): string {
    const encodedSearchValues = window.btoa(JSON.stringify(searchValues));
    return `${this.api}massimport/template/${type}Prefilled/${encodedSearchValues}`;
  }

  public verify(file: File, type: string): Observable<MassUploadResponse> {
    const formData = new FormData();
    formData.append("file", file, file.name);

    return this.http.post<MassUploadResponse>(
      `${this.api}massUpload/verify/${type}`,
      formData
    );
  }

  public load(
    file: File,
    sendEmail: boolean,
    type: string
  ): Observable<MassUploadResponse> {
    const formData = new FormData();
    formData.append("file", file, file.name);
    formData.append("sendEmail", String(sendEmail));

    return this.http.post<MassUploadResponse>(
      `${this.api}massUpload/load/${type}`,
      formData
    );
  }

  public downloadResults(
    file: File,
    response: MassUploadResponse,
    type: string
  ): Observable<Blob> {
    const formData = new FormData();
    formData.append("file", file, file.name);
    formData.append("responseString", JSON.stringify(response));

    return this.http.post(`${this.api}massUpload/results/${type}`, formData, {
      responseType: "blob",
    });
  }
}
