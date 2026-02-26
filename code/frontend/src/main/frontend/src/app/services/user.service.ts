import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Person } from "app/models/person";
import { Configuration } from "app/utils/configuration";
import { UserSetting } from "app/models/user-setting";

@Injectable()
export class UserService {
  private readonly actionUrl: string;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl;
  }

  public getCurrentUser(): Observable<Person> {
    return this.http.get<Person>(this.actionUrl + "users/currentuser");
  }

  public getUserDebounceTime(wbi: string): Observable<UserSetting> {
    return this.http.get<UserSetting>(
      this.actionUrl + "usersetting/" + wbi + "/debouncetime"
    );
  }
}
