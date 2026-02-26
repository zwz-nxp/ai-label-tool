import { Component, OnInit } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { DataService } from "app/utils/api-access/data-service";
import { Location } from "app/models/location";
import { Person } from "app/models/person";
import { Update, UpdateType } from "app/models/update";
import { JobResult, TibcoJobResult } from "app/models/job-result";
import { RoleEnum } from "app/models/role";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { FormControl } from "@angular/forms";
import { Store } from "@ngrx/store";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import * as LocationSelectors from "app/state/location/location.selectors";

@Component({
  selector: "app-sys-admin-tools",
  templateUrl: "./sys-admin-tools.component.html",
  standalone: false,
})
export class SysAdminToolsComponent implements OnInit {
  public allLocations: Array<Location> = [];
  public currentUser!: Person;
  public selectedLocation?: Location | null;
  public alertMsg = "";
  public updates: Array<string> = [];
  public selectedUpdate?: UpdateType;
  public siteSelection = new FormControl<Location | null>(null);
  public siteFilter = new FormControl("");
  public filteredLocations: Array<Location> = [];

  public calculationStatus = "";
  public calculatingPlanningView = false;
  public triggeringUpdate = false;
  public updateFilterInput!: UpdateType;

  // work flow
  public selectedFetchWorkFlow12NC = null;
  public selectedFetchWorkFlowSapCode? = "";
  public isFetching = false;
  public fourLocations: Array<Location> = [];
  public workFlowSiteSelection = new FormControl<Location | null>(null);

  public constructor(
    private dataService: DataService,
    public authorizationService: AuthorizationService,
    public snackbar: MatSnackBar,
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
      .select(LocationSelectors.selectAllLocations)
      .subscribe((locations) => {
        this.allLocations = locations;
        this.filteredLocations = this.allLocations;

        this.fourLocations = this.allLocations.filter((location) =>
          [1, 2, 3, 4].includes(location.id)
        );
      });

    this.siteSelection.valueChanges.subscribe((value) => {
      this.selectedLocation = value;
      this.calculationStatus = "";
    });

    this.workFlowSiteSelection.valueChanges.subscribe((value) => {
      this.selectedFetchWorkFlowSapCode = value?.sapCode;
    });

    this.siteFilter.valueChanges.subscribe((value) => {
      if (value) {
        this.filteredLocations = this.allLocations.filter((loc) =>
          loc.acronym.toLowerCase().includes(value?.toLowerCase())
        );
      } else {
        this.filteredLocations = this.allLocations;
      }
    });
  }

  public ngOnInit(): void {
    this.filteredLocations = this.allLocations;

    for (const u of Object.keys(UpdateType)) {
      this.updates.push(u);
    }

    if (!this.isAuthorized()) {
      SnackbarUtils.displayWarningMsg(
        this.snackbar,
        "You need " + RoleEnum.ADMINISTRATOR_SYSTEM + " role"
      );
    }
    this.watchForUpdate();
  }

  public selectUpdate(): void {
    this.selectedUpdate = this.updateFilterInput;
  }

  public alertIeMdm(): void {
    this.dataService.alertIeMdm(this.alertMsg).subscribe();
  }

  public isAuthorized(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      RoleEnum.ADMINISTRATOR_SYSTEM
    );
  }

  public triggerUpdate(): void {
    this.triggeringUpdate = true;
    this.dataService.manualTriggerUpdate(this.selectedUpdate ?? "").subscribe({
      next: () => {
        SnackbarUtils.displaySuccessMsg(
          this.snackbar,
          "Update is triggered successfully"
        );
        this.triggeringUpdate = false;
      },
      error: () => {
        SnackbarUtils.displayServerErrorMsg(
          this.snackbar,
          "Something went wrong"
        );
        this.triggeringUpdate = false;
      },
    });
  }

  public isFetchAble(): boolean {
    return !(
      this.selectedFetchWorkFlow12NC == null ||
      this.selectedFetchWorkFlowSapCode == null ||
      this.isFetching
    );
  }

  public fetchWorkFlow(): void {
    if (!this.isFetchAble()) return;

    this.isFetching = true;
    this.dataService
      .fetchWorkFlow(
        this.selectedFetchWorkFlow12NC,
        this.selectedFetchWorkFlowSapCode,
        this.currentUser.wbi
      )
      .subscribe({
        next: () => {
          SnackbarUtils.displaySuccessMsg(
            this.snackbar,
            "work flow has been fetched successfully"
          );
          this.isFetching = false;
        },
        error: (error) => {
          SnackbarUtils.displayServerErrorMsg(this.snackbar, error);
          this.isFetching = false;
        },
      });
  }

  private displayTibcoJobResult(tibcoJobResult: TibcoJobResult): void {
    const message = `${tibcoJobResult.jobResult}: `;
    switch (tibcoJobResult.jobResult) {
      case JobResult.FAILED: {
        SnackbarUtils.displayErrorMsg(
          this.snackbar,
          message + tibcoJobResult.message
        );
        break;
      }
      case JobResult.DISABLED: {
        SnackbarUtils.displayWarningMsg(
          this.snackbar,
          message + tibcoJobResult.message
        );
        break;
      }
      default: {
        SnackbarUtils.displaySuccessMsg(this.snackbar, `${message} completed`);
        break;
      }
    }
  }

  private watchForUpdate(): void {
    this.dataUpdateService.updateEmitter.subscribe((update: Update) => {
      if (
        update.updatedType === UpdateType.USER_ALERT &&
        update.userWbi === this.currentUser.wbi
      ) {
        const data = update.updateData as TibcoJobResult;
        if (data) {
          this.displayTibcoJobResult(data);
        }
      }
    });
  }
}
