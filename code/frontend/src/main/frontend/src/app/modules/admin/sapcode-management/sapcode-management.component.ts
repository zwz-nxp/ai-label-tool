import { Component, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { Location } from "app/models/location";
import { SapCode } from "app/models/sap-code";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { MatPaginator } from "@angular/material/paginator";
import { MatSort } from "@angular/material/sort";
import { MatTable, MatTableDataSource } from "@angular/material/table";
import { Observable, Subscription } from "rxjs";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { CRUD_ACTION } from "app/components/crud/crud-action";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { MatTabChangeEvent } from "@angular/material/tabs";
import { SapCodeCrudComponent } from "app/components/crud/sap-code-crud/sap-code-crud.component";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";
import * as SystemSelectors from "app/state/system/system.selectors";

@Component({
  selector: "app-sapcode-management",
  templateUrl: "./sapcode-management.component.html",
  standalone: false,
})
export class SapCodeManagementComponent
  extends BaseDialogComponent
  implements OnInit, OnDestroy
{
  @ViewChild(MatPaginator) public paginator!: MatPaginator;
  @ViewChild("SapCodeTable") public sapSort = new MatSort();
  @ViewChild("SapCodeTable") public sapCodeTable!: MatTable<
    MatTableDataSource<Array<SapCode>>
  >;
  @ViewChild("SiteTable") public siteTable!: MatTable<
    MatTableDataSource<Array<Location>>
  >;

  public sapCode: SapCode = new SapCode();

  public allSapCodes: Array<SapCode> = [];
  public allLocations: Array<Location> = [];
  public wrappedLocations: Array<LocationWrapper> = [];
  public sapCodeDataSource!: MatTableDataSource<SapCode>;
  public siteDataSource!: MatTableDataSource<LocationWrapper>;
  public selectedSapCode: SapCode = new SapCode();
  public originalSapCode: SapCode = new SapCode();
  public selectManagedById = -1;
  public originalManagedById = -1;
  public newSapCode = false;

  public displaySapCodeColumns = [
    "plantCode",
    "city",
    "country",
    "managedBy",
    "lastUpdated",
    "updatedBy",
    "actions",
  ];
  public displaySiteColumns = [
    "location.acronym",
    "location.city",
    "location.country",
    "manages",
  ];

  private sapCodeSubscription!: Subscription;
  private locationsSubscription!: Subscription;

  public constructor(
    dialogRef: MatDialogRef<SapCodeManagementComponent>,
    dataService: DataService,
    snackbar: MatSnackBar,
    store: Store,
    public dialogService: MatDialog,
    public authorizationService: AuthorizationService
  ) {
    super(dialogRef, dataService, snackbar, store);

    this.store
      .select(LocationSelectors.selectAllLocations)
      .subscribe((locations) => {
        this.allLocations = locations;
      });
  }

  public ngOnInit(): void {
    this.getAllSapCodes();
  }

  public selectForEdit(sapCode: SapCode): void {
    if (this.isAuthorized()) {
      this.updateSelectedSapCode(sapCode);
      this.newSapCode = false;
      const dialogRef = this.dialogService.open(SapCodeCrudComponent, {
        disableClose: true,
      });

      dialogRef.componentInstance.selectedSapCode = sapCode;
      dialogRef.componentInstance.crudAction = CRUD_ACTION.UPDATE;

      dialogRef.afterClosed().subscribe(() => {
        this.getAllSapCodes();
      });
    }
  }

  public isAuthorized(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      "Manager_PlanningGroups_Global"
    );
  }

  public locationAcronym(sapCode: SapCode): string {
    if (sapCode && sapCode.managedBy) {
      return sapCode.managedBy.acronym;
    } else {
      return "N/A";
    }
  }

  public deleteSapCode(sapCode: SapCode): void {
    if (this.isAuthorized()) {
      const dialogRef = this.dialogService.open(SapCodeCrudComponent, {
        disableClose: true,
      });

      dialogRef.componentInstance.selectedSapCode = sapCode;
      dialogRef.componentInstance.crudAction = CRUD_ACTION.DELETE;

      dialogRef.afterClosed().subscribe(() => {
        this.getAllSapCodes();
      });
    }
  }

  public ngOnDestroy(): void {
    if (this.sapCodeSubscription != null) {
      this.sapCodeSubscription.unsubscribe();
    }

    if (this.locationsSubscription != null) {
      this.locationsSubscription.unsubscribe();
    }
  }

  public openAddSapCode(): void {
    const sapCode = new SapCode();
    const dialogRef = this.dialogService.open(SapCodeCrudComponent, {
      disableClose: true,
    });

    dialogRef.componentInstance.selectedSapCode = sapCode;
    dialogRef.componentInstance.crudAction = CRUD_ACTION.CREATE;

    dialogRef.afterClosed().subscribe(() => {
      this.getAllSapCodes();
    });
  }

  public attachPaginator(e: MatTabChangeEvent): void {
    if (e.index === 0) {
      this.sapCodeDataSource.paginator = this.paginator;
    } else {
      this.siteDataSource.paginator = this.paginator;
    }
  }

  protected getUsernameByWbi(wbi: string): Observable<string> {
    return this.store.select(SystemSelectors.selectUserName(wbi));
  }

  private getAllSapCodes(): void {
    this.sapCodeSubscription = this.dataService
      .getAllSapCodes()
      .subscribe((sapCodes) => {
        this.allSapCodes = sapCodes;
        this.allSapCodes.sort((a, b) => (a.plantCode > b.plantCode ? 1 : -1));
        this.sapCodeDataSource = new MatTableDataSource(this.allSapCodes);

        this.sapCodeDataSource.paginator = this.paginator;
        this.sapCodeDataSource.sort = this.sapSort;

        this.wrapLocations();
      });
  }

  private wrapLocations(): void {
    this.allLocations = this.allLocations.filter((e) => e.id > 0);
    this.allLocations.push(this.buildNotManagedLocation());
    this.allLocations.forEach((elem) => this.addToWrapperArray(elem));
    this.siteDataSource = new MatTableDataSource(this.wrappedLocations);
  }

  private buildNotManagedLocation(): Location {
    const result = new Location();
    result.id = -2;
    result.acronym = "Not managed";
    return result;
  }

  private addToWrapperArray(location: Location): void {
    const locationWrapper = new LocationWrapper();
    locationWrapper.location = location;

    const managedSapCodes = this.allSapCodes.filter((e) =>
      this.isManagedByLocation(e, location)
    );
    managedSapCodes.forEach(
      (elem) => (locationWrapper.manages += elem.plantCode + ", ")
    );
    const length = locationWrapper.manages?.length;
    if (length != undefined && length > 0) {
      locationWrapper.manages = locationWrapper.manages.substring(
        0,
        length - 2
      );
    }
    this.wrappedLocations.push(locationWrapper);
  }

  private isManagedByLocation(sapCode: SapCode, loc: Location): boolean {
    return sapCode.managedBy && sapCode.managedBy.id === loc.id;
  }

  private updateSelectedSapCode(sapCode: SapCode): void {
    this.selectedSapCode = sapCode;
    if (this.selectedSapCode.managedBy) {
      this.selectManagedById = this.selectedSapCode.managedBy.id;
    } else {
      this.selectManagedById = -1;
    }

    this.originalSapCode = new SapCode();
    this.originalSapCode.city = this.selectedSapCode.city;
    this.originalSapCode.country = this.selectedSapCode.country;
    this.originalSapCode.plantCode = this.selectedSapCode.plantCode;
    this.originalManagedById = this.selectManagedById;
  }
}

class LocationWrapper {
  public location?: Location;
  public manages = "";
}
