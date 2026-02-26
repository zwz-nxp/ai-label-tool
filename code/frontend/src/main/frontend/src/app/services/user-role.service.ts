import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Person } from "app/models/person";
import { Configuration } from "app/utils/configuration";
import { RoleAllowed } from "app/models/user-role";

@Injectable()
export class UserRoleService {
  private readonly actionUrl: string;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  public getUserRoles(user: Person): Observable<RoleAllowed[]> {
    return this.http.get<RoleAllowed[]>(
      this.actionUrl + "authorization/" + user.wbi + "/getAllRolesAllowed"
    );
  }
}
