import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Configuration } from "app/utils/configuration";
import { SapCode } from "app/models/sap-code";

@Injectable()
export class SapCodeService {
  private readonly actionUrl: string;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  public getAllSapCodes(): Observable<SapCode[]> {
    return this.http.get<SapCode[]>(this.actionUrl + "sapcode/all");
  }
}
