import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Configuration } from "app/utils/configuration";
import { Location } from "app/models/location";

@Injectable()
export class LocationService {
  private readonly actionUrl: string;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  public getAllLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(this.actionUrl + "location/all");
  }
}
