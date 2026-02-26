import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Configuration } from "app/utils/configuration";
import { ConfigurationItem } from "app/models/configuration-item";
import { GlobalLookupData } from "app/models/lookup-data";

@Injectable()
export class SystemService {
  private readonly actionUrl: string;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  public getAllConfigurationItems(): Observable<ConfigurationItem[]> {
    return this.http.get<ConfigurationItem[]>(
      this.actionUrl + "configurationvalue/all"
    );
  }

  public getGlobalLookupData(): Observable<GlobalLookupData> {
    return this.http.get<GlobalLookupData>(
      this.actionUrl + "lookupdata/global"
    );
  }
}
