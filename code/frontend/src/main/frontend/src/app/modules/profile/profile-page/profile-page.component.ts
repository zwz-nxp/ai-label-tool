import { Component, OnInit } from "@angular/core";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { Person } from "app/models/person";
import { RoleAllowed } from "app/models/user-role";
import { MatTableDataSource } from "@angular/material/table";
import { UserSetting } from "app/models/user-setting";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog } from "@angular/material/dialog";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Store } from "@ngrx/store";
import * as CurrentUserActions from "app/state/current-user/current-user.actions";
import { Observable } from "rxjs";
import * as SystemSelectors from "app/state/system/system.selectors";

@Component({
  selector: "app-profile-page",
  templateUrl: "./profile-page.component.html",
  standalone: false,
})
export class ProfilePageComponent implements OnInit {
  public currentUser!: Person;
  public rolesAllowed: Array<RoleAllowed> = [];
  public settingsForSelectedUser: Array<UserSetting> = [];
  public notificationMailReceive = "false";
  public debounceTime = "0";
  public rolesDataSource = new MatTableDataSource<RoleAllowed>([]);
  public displayColumns = ["roleName", "forSites", "forSapCodes"];

  public delegateColumns = ["delegateWbi", "startDate", "endDate", "actions"];

  public constructor(
    private dataService: DataService,
    public dialogService: MatDialog,
    public authorizationService: AuthorizationService,
    public snackBar: MatSnackBar,
    private store: Store
  ) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        if (user) {
          this.currentUser = user;
        }
      });
  }

  public ngOnInit(): void {
    this.getAllSettingsForUser();
    this.getAllRolesForUser();
  }

  public forSapCodes(roleAllowed: RoleAllowed): string {
    let result = "";
    roleAllowed.sapCodes.forEach((e) => (result += e + ", "));
    if (result.length > 4) {
      result = result.substring(0, result.length - 2);
    }
    return result;
  }

  public setNotificationMail(): void {
    const userSetting = new UserSetting();
    userSetting.user = this.currentUser;
    userSetting.key = "notificationmail";
    userSetting.value = this.notificationMailReceive;
    this.saveUserSetting(userSetting);
  }

  public setDebounceTime(): void {
    const userSetting = new UserSetting();
    userSetting.user = this.currentUser;
    userSetting.key = "debouncetime";
    userSetting.value = this.debounceTime;
    this.saveUserSetting(userSetting);
  }

  protected getUsernameByWbi(wbi: string): Observable<string> {
    return this.store.select(SystemSelectors.selectUserName(wbi));
  }

  private getAllSettingsForUser(): void {
    this.dataService.getAllUserSettings().subscribe((settings) => {
      this.settingsForSelectedUser = settings;
      for (const setting of settings) {
        if (setting.key.toLowerCase() == "notificationmail") {
          this.notificationMailReceive = setting.value;
        }
        if (setting.key.toLowerCase() == "debouncetime") {
          this.debounceTime = setting.value;
        }
      }
    });
  }

  private getAllRolesForUser(): void {
    this.dataService.getAllRolesAllowed(this.currentUser).subscribe((roles) => {
      this.processRolesAllowed(roles);
    });
  }

  private processRolesAllowed(roles: RoleAllowed[]): void {
    this.rolesAllowed = roles;
    this.rolesDataSource = new MatTableDataSource(this.rolesAllowed);
  }

  private saveUserSetting(userSetting: UserSetting): void {
    this.dataService.saveUserSetting(userSetting).subscribe({
      next: (_data) => this.saveOk(),
      error: (error) => this.saveFailed(error),
    });
  }

  private saveOk(): void {
    SnackbarUtils.displaySuccessMsg(
      this.snackBar,
      "Successfully saved user settings"
    );
    this.store.dispatch(
      CurrentUserActions.loadDebounceTime({ user: this.currentUser })
    );
  }

  private saveFailed(error: Error): void {
    SnackbarUtils.displayServerErrorMsg(this.snackBar, error.message);
  }
}
