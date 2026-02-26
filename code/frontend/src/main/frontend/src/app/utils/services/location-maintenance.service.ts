import { Injectable } from "@angular/core";
import { Location, Manufacturer } from "app/models/location";
import { HttpClient, HttpResponse } from "@angular/common/http";
import { Configuration } from "../configuration";
import { firstValueFrom } from "rxjs";

@Injectable({
  providedIn: "root",
})
export class LocationMaintenanceService {
  private readonly api: string;

  public constructor(
    private http: HttpClient,
    protected configuration: Configuration
  ) {
    this.api = configuration.ServerWithApiUrl + "location";
  }

  public saveLocation(site: Location): Promise<HttpResponse<void>> {
    return firstValueFrom(
      this.http.post<HttpResponse<void>>(this.api + "/create", site)
    );
  }

  public deleteLocation(id: number): Promise<HttpResponse<void>> {
    return firstValueFrom(
      this.http.delete<HttpResponse<void>>(this.api + "/" + id)
    );
  }

  public saveManufacturerCode(
    manufacturer: Manufacturer
  ): Promise<HttpResponse<void>> {
    return firstValueFrom(
      this.http.post<HttpResponse<void>>(
        this.api + "/saveManufacturerCode",
        manufacturer
      )
    );
  }

  public deleteManufacturerCode(
    manufacturerCode: string
  ): Promise<HttpResponse<void>> {
    return firstValueFrom(
      this.http.delete<HttpResponse<void>>(
        `${this.api}/deleteManufacturerCode/${manufacturerCode}`
      )
    );
  }
}
