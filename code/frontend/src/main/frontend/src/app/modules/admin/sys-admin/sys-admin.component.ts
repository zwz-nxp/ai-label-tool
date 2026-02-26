import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { Person } from "app/models/person";
import { Location } from "app/models/location";
import { NxpProductionYear } from "app/models/nxp-production-year";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { Update, UpdateType } from "app/models/update";
import { RoleEnum } from "app/models/role";
import { DateUtils } from "app/utils/date-utils";
import { MatDialog } from "@angular/material/dialog";
import { AddNxpProductionYearDialogComponent } from "./add-nxp-production-year-dialog/add-nxp-production-year-dialog.component";
import { firstValueFrom } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";

@Component({
  selector: "app-sys-admin",
  templateUrl: "./sys-admin.component.html",
  standalone: false,
})
export class SysAdminComponent implements OnInit {
  @ViewChild("resourceGroupUpload") public resourceGroupUpload!: ElementRef;
  @ViewChild("equipmentCodeUpload") public equipmentCodeUpload!: ElementRef;
  @ViewChild("equipmentUpload") public equipmentUpload!: ElementRef;

  public currentUser: Person = new Person();
  public location: Location = new Location();

  public du = new DateUtils();

  public nxpProdWeeks: Array<NxpProductionYear> = [];

  public isLoadingResults = false;
  public isAuthorized = false;

  public constructor(
    private dialogService: MatDialog,
    public snackBar: MatSnackBar,
    private dataService: DataService,
    public authorizationService: AuthorizationService,
    private dataUpdateService: DataUpdateService,
    private store: Store
  ) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        if (user) {
          this.currentUser = user;
        }
      });

    this.store
      .select(LocationSelectors.selectCurrentLocation)
      .subscribe((location) => {
        if (location) {
          this.location = location;
        }
      });
  }

  public ngOnInit(): void {
    this.getAllNxpProdWeeks();

    this.checkIfAuthorized();
    this.watchForUpdate();
  }

  public openAddProdYear(): void {
    this.dialogService
      .open(AddNxpProductionYearDialogComponent)
      .beforeClosed()
      .subscribe(() => {
        this.getAllNxpProdWeeks();
      });
  }

  public getAllNxpProdWeeks(): void {
    this.dataService
      .getNxpProductionYearForAllYears()
      .subscribe((years) => (this.nxpProdWeeks = years));
  }

  public isPastProductionYear(year: NxpProductionYear): boolean {
    return new Date(year.startDate).getTime() - new Date().getTime() < 0;
  }

  public submitNewYear(year: NxpProductionYear): void {
    if (year.startDate) {
      const clone = NxpProductionYear.clone(year);
      firstValueFrom(this.dataService.submitNxpProductionYear(clone))
        .then((data) => this.saveOk(data))
        .catch((err) => this.saveFailed(err))
        .finally(() => {
          this.getAllNxpProdWeeks();
        });
    }
  }

  private saveOk(_data: NxpProductionYear): void {
    SnackbarUtils.displaySuccessMsg(
      this.snackBar,
      "Successfully saved Production year"
    );
    this.getAllNxpProdWeeks();
  }

  private saveFailed(err: HttpErrorResponse): void {
    SnackbarUtils.displayServerErrorMsg(this.snackBar, err.error);
  }

  private checkIfAuthorized(): void {
    this.isAuthorized = this.authorizationService.doesUserHaveRoleForSite(
      RoleEnum.ADMINISTRATOR_SYSTEM,
      this.location.acronym
    );
    if (!this.isAuthorized) {
      SnackbarUtils.displayWarningMsg(
        this.snackBar,
        "You are not authorized to upload Excel file"
      );
    }
  }

  private watchForUpdate(): void {
    this.dataUpdateService.updateEmitter.subscribe((update: Update) => {
      if (
        update.updatedType == UpdateType.USER_ALERT &&
        update.userWbi === this.currentUser.wbi
      ) {
        SnackbarUtils.displayWarningMsg(this.snackBar, update.updateData);
      }
    });
  }
}
