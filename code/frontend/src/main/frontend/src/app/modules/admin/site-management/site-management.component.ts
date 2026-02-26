import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { Location, Manufacturer } from "app/models/location";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { MatPaginator } from "@angular/material/paginator";
import { Sort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { MatDialog } from "@angular/material/dialog";
import { CRUD_ACTION } from "app/components/crud/crud-action";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { Update, UpdateType } from "app/models/update";
import {
  GenericSearchArg,
  GenericSearchArguments,
  GenericSearchMultiselectColumn,
  GenericSearchSortField,
  GenericSearchUtils,
  GenericSortArg,
} from "app/models/generic-search";
import { DataService } from "app/utils/api-access/data-service";
import { RoleEnum } from "app/models/role";
import { LocationCrudComponent } from "app/components/crud/location-crud/site-crud/location-crud.component";
import { MatTabChangeEvent } from "@angular/material/tabs";
import { ManufacturerCrudComponent } from "app/components/crud/location-crud/manufacturer-crud/manufacturer-crud.component";
import { firstValueFrom, Observable } from "rxjs";
import * as SystemSelectors from "app/state/system/system.selectors";
import { Store } from "@ngrx/store";

@Component({
  selector: "app-site-management",
  templateUrl: "./site-management.component.html",
  standalone: false,
})
export class SiteManagementComponent implements OnInit, AfterViewInit {
  public readonly role = RoleEnum.ADMINISTRATOR_SYSTEM;
  public tabIndex = 0;
  public booleanSearchValues: (boolean | undefined)[] = [
    true,
    false,
    undefined,
  ];
  public displayedColumnsSites = [
    "acronym",
    "isSubContractor",
    "tmdbCode",
    "city",
    "country",
    "sapCode",
    "vendorCode",
    "planningEngine",
    "menuGrouping",
    "extendedSuffix",
    "lastUpdated",
    "updater.name",
    "actions",
  ];
  public displayedColumnsManufacturerCodes = [
    "location",
    "manufacturerCode",
    "lastUpdated",
    "updater.name",
    "actions",
  ];
  public isLoading = false;
  public searchColumnsSites: GenericSearchMultiselectColumn[] = [
    {
      sortField: GenericSearchSortField.LOC_ACRONYM,
      sticky: true,
    },
    {
      sortField: GenericSearchSortField.LOC_SUBCON,
    },
    {
      sortField: GenericSearchSortField.LOC_TMDBCODE,
    },
    {
      sortField: GenericSearchSortField.LOC_CITY,
    },
    {
      sortField: GenericSearchSortField.LOC_COUNTRY,
    },
    {
      sortField: GenericSearchSortField.LOC_SAPCODE,
    },
    {
      sortField: GenericSearchSortField.LOC_VENDOR_CODE,
    },
    {
      sortField: GenericSearchSortField.LOC_PLANNINGENGINE,
    },
    {
      sortField: GenericSearchSortField.LOC_MENUGROUPING,
    },
    {
      sortField: GenericSearchSortField.LOC_EXTENDEDSUFFIX,
    },
    {
      sortField: GenericSearchSortField.LOC_LASTUPDATED,
    },
    {
      sortField: GenericSearchSortField.LOC_UPDATEDBY,
    },
    {
      sortField: GenericSearchSortField.EMPTY,
      stickyEnd: true,
    },
  ];
  public searchColumnsManufacturerCodes: GenericSearchMultiselectColumn[] = [
    {
      sortField: GenericSearchSortField.MC_SITE,
    },
    {
      sortField: GenericSearchSortField.MC_MANUFACTURER_CODE,
    },
    {
      sortField: GenericSearchSortField.MC_LASTUPDATED,
    },
    {
      sortField: GenericSearchSortField.MC_UPDATEDBY,
    },
    {
      sortField: GenericSearchSortField.EMPTY,
      stickyEnd: true,
    },
  ];
  public searchColumnsSitesNames = this.searchColumnsSites.map(
    (col) => col.sortField
  );
  public searchColumnsManufacturerCodesNames =
    this.searchColumnsManufacturerCodes.map((col) => col.sortField);
  public selectedLocation: Location = new Location();
  public sitesDataSource = new MatTableDataSource<Location>([]);
  public manufacturerCodesDataSource = new MatTableDataSource<Manufacturer>([]);
  @ViewChild("paginatorSites") private paginatorSites!: MatPaginator;
  @ViewChild("paginatorManufacturerCodes")
  private paginatorManufacturerCodes!: MatPaginator;
  private searchArgumentsLocations = new GenericSearchArguments();
  private searchArgumentsManufacturerCodes = new GenericSearchArguments();

  public constructor(
    public dialogService: MatDialog,
    public authorizationService: AuthorizationService,
    private dataUpdateService: DataUpdateService,
    private dataService: DataService,
    private genericSearchUtils: GenericSearchUtils,
    private store: Store
  ) {}

  /**
   * Converter used in view to show JDA instead of i2/I2
   * @param input
   * @returns
   */
  public convertPlanningEngine(input: string): string {
    const output = input?.toLowerCase();
    if (output?.includes("i2")) {
      return output.replaceAll("i2", "JDA");
    }
    return input;
  }

  public deleteTooltip(location: Location): string {
    const msgGlobalSite = "The global site cannot be removed";
    const msgNotAuthorized = `You are not authorized to remove this site`;
    const msgNotDeletableActiveEquipment = `Current site has '${location.activeEquipmentCount}' equipment installed, and therefore cannot be removed`;
    const msgNotDeletableActiveEvents = `Current site has '${location.activeEventsCount}' active events, and therefore cannot be removed.`;
    const msgNotDeletableUnknownCause =
      "Site cannot be removed, because of an unknown cause";
    if (location.id === 0) {
      return msgGlobalSite;
    } else if (this.isAuthorized() && this.isSiteDeletable(location)) {
      return "";
    } else if (!this.isAuthorized()) {
      return msgNotAuthorized;
    } else if (location.activeEquipmentCount > 0) {
      return msgNotDeletableActiveEquipment;
    } else if (location.activeEventsCount > 0) {
      return msgNotDeletableActiveEvents;
    } else {
      return msgNotDeletableUnknownCause;
    }
  }

  public isAuthorized(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      "Administrator_System"
    );
  }

  public isSiteDeletable(location: Location): boolean {
    return (
      location.activeEquipmentCount === 0 &&
      location.activeEventsCount === 0 &&
      location.id !== 0
    );
  }

  public ngAfterViewInit(): void {
    this.getLocations();
    this.watchForUpdate();
  }

  public ngOnInit(): void {
    this.initializeSearchArguments();
  }

  public attachPaginator(_: MatTabChangeEvent): void {
    this.getPages();
  }

  public addSite(): void {
    const dialogRef = this.dialogService.open(LocationCrudComponent, {
      disableClose: true,
    });

    dialogRef.componentInstance.crudAction = CRUD_ACTION.CREATE;

    dialogRef.afterClosed().subscribe(() => {
      this.getLocations();
    });
  }

  public addManufacturer(): void {
    const dialogRef = this.dialogService.open(ManufacturerCrudComponent, {
      disableClose: true,
    });

    dialogRef.componentInstance.crudAction = CRUD_ACTION.CREATE;

    dialogRef.afterClosed().subscribe(() => {
      this.getManufacturers();
    });
  }

  public add(): void {
    switch (this.tabIndex) {
      case 0:
        this.addSite();
        break;
      case 1:
        this.addManufacturer();
        break;
    }
  }

  public addButtonTooltip(): string {
    switch (this.tabIndex) {
      case 0:
        return "Add a new Location";
      case 1:
        return "Add a new Manufacturer Code";
      default:
        return "";
    }
  }

  public initializeSearchArguments(): void {
    this.searchArgumentsLocations = new GenericSearchArguments();
    this.searchArgumentsManufacturerCodes = new GenericSearchArguments();
    this.searchArgumentsLocations.searchArgs.push(
      new GenericSearchArg(GenericSearchSortField.LOC_STATUS, "ACTIVE")
    );
    this.searchArgumentsManufacturerCodes.sortArgs.push(
      new GenericSortArg(GenericSearchSortField.MC_SITE, false)
    );
  }

  public deleteSite(inputLocation: Location): void {
    if (this.isSiteDeletable(inputLocation)) {
      const location = this.createLocationCopy(inputLocation);

      const dialogRef = this.dialogService.open(LocationCrudComponent, {
        disableClose: true,
      });
      dialogRef.componentInstance.selectedLocation = location;
      dialogRef.componentInstance.crudAction = CRUD_ACTION.DELETE;
      firstValueFrom(dialogRef.afterClosed()).then(() => this.getLocations());
    }
  }

  public deleteManufacturerCode(inputManufacturerCode: Manufacturer): void {
    const manufacturerCode = this.createManufacturerCodeCopy(
      inputManufacturerCode
    );

    const dialogRef = this.dialogService.open(ManufacturerCrudComponent, {
      disableClose: true,
    });
    dialogRef.componentInstance.selectedManufacturer = manufacturerCode;
    dialogRef.componentInstance.crudAction = CRUD_ACTION.DELETE;
    firstValueFrom(dialogRef.afterClosed()).then(() => this.getManufacturers());
  }

  public resetFilters(): void {
    this.initializeSearchArguments();
    this.resetPaginator();
    this.getPages();
    this.dataUpdateService.triggerReset(this.searchArgumentsLocations);
  }

  public searchFieldTriggered(triggeredSearchArg: GenericSearchArg): void {
    switch (this.tabIndex) {
      case 0:
        this.searchLocations(triggeredSearchArg);
        break;
      case 1:
        this.searchManufacturerCode(triggeredSearchArg);
        break;
    }
  }

  public editLocation(inputLocation: Location): void {
    const location = this.createLocationCopy(inputLocation);

    const dialogRef = this.dialogService.open(LocationCrudComponent, {
      disableClose: true,
    });
    dialogRef.componentInstance.selectedLocation = location;
    dialogRef.componentInstance.crudAction = CRUD_ACTION.UPDATE;
    firstValueFrom(dialogRef.afterClosed()).then(() => this.getLocations());
  }

  public editManufacturerCode(inputManufacturerCode: Manufacturer): void {
    const manufacturerCode = this.createManufacturerCodeCopy(
      inputManufacturerCode
    );

    const dialogRef = this.dialogService.open(ManufacturerCrudComponent, {
      disableClose: true,
    });
    dialogRef.componentInstance.selectedManufacturer = manufacturerCode;
    dialogRef.componentInstance.crudAction = CRUD_ACTION.UPDATE;
    firstValueFrom(dialogRef.afterClosed()).then(() => this.getManufacturers());
  }

  public getPages(): void {
    switch (this.tabIndex) {
      case 0:
        this.getLocations();
        break;
      case 1:
        this.getManufacturers();
        break;
    }
  }

  public sortData(event: Sort): void {
    let searchArguments = new GenericSearchArguments();
    switch (this.tabIndex) {
      case 0:
        searchArguments = this.searchArgumentsLocations;
        break;
      case 1:
        searchArguments = this.searchArgumentsManufacturerCodes;
        break;
    }

    if (
      this.genericSearchUtils.sortData(
        event.active,
        event.direction,
        searchArguments
      )
    ) {
      this.resetPaginator();
      this.getPages();
    }
  }

  protected getUsernameByWbi(wbi: string): Observable<string> {
    return this.store.select(SystemSelectors.selectUserName(wbi));
  }

  private createLocationCopy(inputLocation: Location): Location {
    return Object.assign({}, inputLocation);
  }

  private createManufacturerCodeCopy(
    inputManufacturerCode: Manufacturer
  ): Manufacturer {
    return Object.assign({}, inputManufacturerCode);
  }

  private getLocations(): void {
    this.isLoading = true;
    this.dataService
      .searchLocations(
        this.searchArgumentsLocations,
        this.paginatorSites.pageIndex,
        this.paginatorSites.pageSize
      )
      .then((locationResult) => {
        this.sitesDataSource = new MatTableDataSource(locationResult.content);
        this.paginatorSites.length = locationResult.totalElements;
      })
      .finally(() => (this.isLoading = false));
  }

  private getManufacturers(): void {
    this.isLoading = true;
    this.dataService
      .searchManufacturerCodes(
        this.searchArgumentsManufacturerCodes,
        this.paginatorManufacturerCodes.pageIndex,
        this.paginatorManufacturerCodes.pageSize
      )
      .pipe()
      .subscribe((data) => {
        this.manufacturerCodesDataSource = new MatTableDataSource(data.content);
        this.paginatorManufacturerCodes.length = data.totalElements;
        this.isLoading = false;
      });
  }

  private searchLocations(triggeredSearchArg: GenericSearchArg): void {
    const searchArg = this.searchArgumentsLocations.getSearchField(
      triggeredSearchArg.field
    );
    if (searchArg) {
      searchArg.value = triggeredSearchArg.value;
    } else {
      this.searchArgumentsLocations.searchArgs.push(triggeredSearchArg);
    }
    this.resetPaginator();
    this.getLocations();
  }

  private searchManufacturerCode(triggeredSearchArg: GenericSearchArg): void {
    const searchArg = this.searchArgumentsManufacturerCodes.getSearchField(
      triggeredSearchArg.field
    );
    if (searchArg) {
      searchArg.value = triggeredSearchArg.value;
    } else {
      this.searchArgumentsManufacturerCodes.searchArgs.push(triggeredSearchArg);
    }
    this.resetPaginator();
    this.getManufacturers();
  }

  private resetPaginator(): void {
    switch (this.tabIndex) {
      case 0:
        this.paginatorSites.firstPage();
        break;
      case 1:
        this.paginatorManufacturerCodes.firstPage();
        break;
    }
  }

  private watchForUpdate(): void {
    this.dataUpdateService.updateEmitter.subscribe((update: Update) => {
      if (update.updatedType === UpdateType.LOCATION) {
        this.getLocations();
      }
    });
  }
}
