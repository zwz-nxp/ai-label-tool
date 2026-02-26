import { Injectable } from "@angular/core";
import { environment } from "../../environments/environment";

@Injectable()
export class Configuration {
  public static NotificationCount = 0;
  public static PendingApprovalCount = 0;
  public static RequestCount = 0;
  public static IsProduction = true;
  public static MaxNotifications = 0;
  public Server = environment.server;
  public ApiUrl = "api/";
  public ServerWithApiUrl = this.Server + this.ApiUrl;
}
