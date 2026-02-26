import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { Person } from "app/models/person";
import { RoleEnum } from "app/models/role";
import { MatPaginator } from "@angular/material/paginator";
import { MatSort, Sort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import {
  GenericSearchArg,
  GenericSearchArguments,
  GenericSearchSortField,
  GenericSearchUtils,
} from "app/models/generic-search";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { MatDialog } from "@angular/material/dialog";
import { UserCrudComponent } from "app/components/crud/user-crud/user-crud.component";
import { CRUD_ACTION } from "app/components/crud/crud-action";
import { Store } from "@ngrx/store";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";

@Component({
  selector: "app-user-overview",
  templateUrl: "./user-overview.component.html",
  standalone: false,
})
export class UserOverviewComponent implements OnInit, AfterViewInit {
  @ViewChild(MatPaginator) public paginator!: MatPaginator;
  @ViewChild(MatSort) public sort!: MatSort;

  public currentUser: Person = new Person();

  public users: Person[] = [];
  public usersDataSource!: MatTableDataSource<Person>;
  public selectedUser: Person = new Person();
  public displayedColumnsUsers = [
    "name",
    "wbi",
    "primaryLocation.acronym",
    "actions",
  ];

  public isCurrentUserAuthorized = false;
  public isLoading = false;

  public searchArguments = new GenericSearchArguments();
  public searchColumns = [
    {
      sortField: GenericSearchSortField.U_NAME,
    },
    {
      sortField: GenericSearchSortField.U_WBI,
    },
    {
      sortField: GenericSearchSortField.U_PRIMARY_SITE,
    },
    {
      sortField: GenericSearchSortField.EMPTY,
    },
  ];
  public searchColumnNames = this.searchColumns.map((col) => col.sortField);

  public constructor(
    private dataService: DataService,
    public dialogService: MatDialog,
    public authorizationService: AuthorizationService,
    private dataUpdateService: DataUpdateService,
    private store: Store,
    private genericSearchUtils: GenericSearchUtils
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
    this.isCurrentUserAuthorized =
      this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
        0,
        RoleEnum.ADMINISTRATOR_SYSTEM
      ) ||
      this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
        0,
        RoleEnum.ADMINISTRATOR_USER
      );
  }

  public ngAfterViewInit(): void {
    this.getAllUsers();
  }

  public openEditUser(user: Person): void {
    if (this.isAuthorized(user)) {
      this.selectedUser = Person.clonePerson(user);

      const dialogRef = this.dialogService.open(UserCrudComponent, {
        disableClose: true,
      });

      dialogRef.componentInstance.crudAction = CRUD_ACTION.UPDATE;
      dialogRef.componentInstance.selectedUser = this.selectedUser;

      dialogRef.afterClosed().subscribe(() => {
        this.getAllUsers();
      });
    }
  }

  public openAddUser(): void {
    const dialogRef = this.dialogService.open(UserCrudComponent, {
      disableClose: true,
    });

    dialogRef.componentInstance.crudAction = CRUD_ACTION.CREATE;

    dialogRef.afterClosed().subscribe(() => {
      this.getAllUsers();
    });
  }

  public downloadExcel(): void {
    const url = this.dataService.downloadUsersExcel();
    window.open(url);
  }

  public searchFieldTriggered(triggeredSearchArg: GenericSearchArg): void {
    const searchArg = this.searchArguments.getSearchField(
      triggeredSearchArg.field
    );
    if (searchArg) {
      searchArg.value = triggeredSearchArg.value;
    } else {
      this.searchArguments.searchArgs.push(triggeredSearchArg);
    }

    this.getAllUsers();
  }

  public resetFilters(): void {
    this.searchArguments = new GenericSearchArguments();
    this.paginator.firstPage();
    this.getAllUsers();
    this.dataUpdateService.triggerReset(this.searchArguments);
  }

  public sortData(event: Sort): void {
    if (
      this.genericSearchUtils.sortData(
        event.active,
        event.direction,
        this.searchArguments
      )
    ) {
      this.paginator.firstPage();
      this.getAllUsers();
    }
  }

  public onPageEvent(): void {
    this.getAllUsers();
  }

  public isAuthorized(user: Person): boolean {
    return this.isCurrentUserAuthorized && user.wbi != this.currentUser.wbi;
  }

  public getEditTooltip(user: Person): string {
    if (this.isAuthorized(user)) {
      return "Edit user";
    }
    return "Not have enough rights to edit user";
  }

  public getAddTooltip(): string {
    if (this.isCurrentUserAuthorized) {
      return "Add a new user";
    }
    return "Not enough rights to add a user";
  }

  public getDownloadTooltip(): string {
    if (this.isCurrentUserAuthorized) {
      return "Download user list to excel";
    }
    return "Not have enough rights";
  }

  private getAllUsers(): void {
    this.isLoading = true;
    this.dataService
      .genericSearchPerson(
        this.searchArguments,
        this.paginator.pageIndex,
        this.paginator.pageSize
      )
      .subscribe((data) => {
        this.users = data.content;
        this.usersDataSource = new MatTableDataSource(this.users);
        this.paginator.length = data.totalElements;
        this.isLoading = false;
      });
  }
}
