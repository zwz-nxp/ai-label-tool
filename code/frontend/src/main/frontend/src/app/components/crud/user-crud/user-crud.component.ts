import { Component, Input, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { CrudComponent } from "../CrudComponent";
import { CRUD_ACTION } from "../crud-action";
import { Person } from "app/models/person";
import { Location } from "app/models/location";
import { FormBuilder } from "@angular/forms";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { DataService } from "app/utils/api-access/data-service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { MatTableDataSource } from "@angular/material/table";
import { Observable, Subscription } from "rxjs";
import { MatSort } from "@angular/material/sort";
import { Role, RoleEnum } from "app/models/role";
import { UserRole } from "app/models/user-role";
import { DateUtils } from "app/utils/date-utils";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";
import * as SystemSelectors from "app/state/system/system.selectors";

@Component({
  selector: "app-user-crud",
  templateUrl: "./user-crud.component.html",
  standalone: false,
})
export class UserCrudComponent
  extends CrudComponent
  implements OnInit, OnDestroy
{
  @ViewChild(MatSort) public sort!: MatSort;
  @Input() public wbiSearch = "";
  @Input() public selectedLocation = new Location();
  @Input() public selectedRole = new Role();

  public selectedUser: Person = new Person();
  public foundUsers: Person[] = [];
  public userRoles: UserRole[] = [];
  public foundUsersDataSource!: MatTableDataSource<Person>;
  public userRolesDataSource!: MatTableDataSource<UserRole>;
  public allLocations: Location[] = [];
  public allAvailableRoles: Array<Role> = new Array<Role>();
  public hasDelegate = false;
  public displayedColumnsUsers = ["name", "wbi", "actions"];
  public displayedColumnsRoles = ["wbi", "name", "site", "role", "actions"];
  public delegateColumns = ["delegateWbi", "startDate", "endDate", "actions"];

  public dateUtils = new DateUtils();

  private allRolesSubscription!: Subscription;
  private roleForUserSubscription!: Subscription;

  public constructor(
    dialogRef: MatDialogRef<CrudComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public dialogService: MatDialog,
    public formBuilder: FormBuilder,
    public authorizationService: AuthorizationService
  ) {
    super(dialogRef, dataService, snackBar, store);

    this.store
      .select(LocationSelectors.selectAllLocations)
      .subscribe((locations) => {
        this.allLocations = locations;
      });
  }

  public get isUserSelected(): boolean {
    return (
      this.selectedUser != null &&
      this.selectedUser.name != null &&
      this.selectedUser.name != ""
    );
  }

  public get showSearch(): boolean {
    return this.crudAction == CRUD_ACTION.CREATE && !this.isUserSelected;
  }

  public get hasUserRoles(): boolean {
    return this.userRoles.length > 0;
  }

  public get showClearUser(): boolean {
    return this.crudAction == CRUD_ACTION.CREATE && this.isUserSelected;
  }

  public get showDelegate(): boolean {
    return this.crudAction == CRUD_ACTION.UPDATE;
  }

  public get isAddRoleDisabled(): boolean {
    this.selectedLocation = this.inputForm.get(`selectedLocation`)?.value;
    this.selectedRole = this.inputForm.get(`selectedRole`)?.value;
    return (
      this.selectedLocation.acronym == "" || this.selectedRole.roleName == ""
    );
  }

  public get isSaveEnabled(): boolean {
    return (
      !(this.crudAction == CRUD_ACTION.CREATE) ||
      (this.selectedUser.name != null &&
        this.selectedUser.name != "" &&
        this.selectedUser.primaryLocation?.acronym != "")
    );
  }

  @Input()
  public set inputWbiSearch(value: unknown) {
    this.wbiSearch = value ? (value as string) : "";
  }

  public displayLocationOption(item: Location): string {
    return item.acronym;
  }

  public compareLocationOption(a: Location, b: Location): boolean {
    return a.acronym === b.acronym;
  }

  public ngOnInit(): void {
    this.getAllAvailableRoles();
    this.fillForm();

    switch (this.crudAction) {
      case CRUD_ACTION.CREATE:
        break;
      case CRUD_ACTION.UPDATE:
        this.getAllRolesForUser();
        break;
    }
  }

  public ngOnDestroy(): void {
    [this.allRolesSubscription].forEach((s) => {
      if (s) s.unsubscribe();
    });

    [this.roleForUserSubscription].forEach((s) => {
      if (s) s.unsubscribe();
    });
  }

  public override fillForm(): void {
    this.inputForm = this.formBuilder.group({
      wbiSearch: [""],
      selectedLocation: [""],

      selectedRole: [""],
      primaryLocation: [""],
    });

    this.inputForm.controls["wbiSearch"].setValue(this.wbiSearch);
    this.inputForm.controls["selectedLocation"].setValue(this.selectedLocation);
    this.inputForm.controls["selectedRole"].setValue(this.selectedRole);
    this.inputForm.controls["primaryLocation"].setValue(
      this.allLocations.find(
        (location) => location.id === this.selectedUser.primaryLocation.id
      )
    );
  }

  public override submit(): void {
    switch (this.crudAction) {
      case CRUD_ACTION.CREATE:
        if (this.inputForm.valid) {
          this.create();
        }
        break;
      case CRUD_ACTION.DELETE:
        this.delete();
        break;
      case CRUD_ACTION.UPDATE:
        if (this.inputForm.valid) {
          this.update();
        }
        break;
    }
  }

  public override create(): void {
    this.assignValues();
    this.selectedUser.loginAllowed = true;

    this.dataService.createUser(this.selectedUser).subscribe({
      next: (_data) => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "The user has been created successfully"
        );
        this.closeDialog();
      },
      error: (err) => SnackbarUtils.displayErrorMsg(this.snackBar, err),
    });
  }

  public override update(): void {
    this.assignValues();

    this.dataService.saveUser(this.selectedUser).subscribe({
      next: (_data) => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "The user has been successfully updated"
        );
        this.closeDialog();
      },
      error: (err) => SnackbarUtils.displayErrorMsg(this.snackBar, err),
    });
  }

  public override delete(): void {
    throw new Error("Method not implemented.");
  }

  public searchUsers(): void {
    this.assignValuesToWbiSearch();
    this.dataService.searchUser(this.wbiSearch).subscribe((users) => {
      this.foundUsers = users.filter((user) => user.primaryLocation == null);
      this.foundUsersDataSource = new MatTableDataSource(this.foundUsers);
      this.foundUsersDataSource.sort = this.sort;
    });
  }

  public selectForAddition(user: Person): void {
    this.selectedUser = Object.assign({}, user);
  }

  public submitNewRoleForUser(): void {
    this.isLoading = true;
    this.selectedLocation = this.inputForm.get(`selectedLocation`)?.value;
    this.selectedRole = this.inputForm.get(`selectedRole`)?.value;

    const user = this.selectedUser;
    const location = this.selectedLocation;
    const role = this.selectedRole;

    this.dataService.addUserRole(user, role, location).subscribe({
      next: (userRole) => {
        this.userRoles.push(userRole);
        this.userRolesDataSource = new MatTableDataSource(this.userRoles);
      },
      error: (error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error),
      complete: () => (this.isLoading = false),
    });
  }

  public removeUserRole(userRole: UserRole): void {
    this.isLoading = true;
    const user = this.selectedUser;
    const location = userRole.location;
    const role = userRole.role;
    this.dataService.removeUserRole(user, role, location).subscribe({
      next: (_) =>
        this.dataService.getAllRolesForUser(user).subscribe({
          next: (userRoles) => {
            this.userRoles = userRoles;
            this.userRolesDataSource = new MatTableDataSource(this.userRoles);
          },
          error: (error) =>
            SnackbarUtils.displayServerErrorMsg(this.snackBar, error),
          complete: () => (this.isLoading = false),
        }),
      error: (error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error),
      complete: () => (this.isLoading = false),
    });
  }

  public clearSelectedUser(): void {
    this.selectedUser = new Person();
    this.selectedLocation = new Location();
    this.selectedRole = new Role();
    this.userRoles = [];
    this.fillForm();
  }

  public isAuthorized(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      RoleEnum.ADMINISTRATOR_SYSTEM
    );
  }

  protected locationSelected(location: Location): void {
    if (location) {
      this.selectedLocation = location;
      this.inputForm.controls["primaryLocation"]?.setValue(location);
    }
  }

  protected getUsernameByWbi(delegateWbi: string): Observable<string> {
    return this.store.select(SystemSelectors.selectUserName(delegateWbi));
  }

  private getAllAvailableRoles(): void {
    this.allRolesSubscription = this.dataService
      .getAllRoles()
      .subscribe((roles) => {
        this.allAvailableRoles = roles;
      });
  }

  private getAllRolesForUser(): void {
    this.roleForUserSubscription = this.dataService
      .getAllRolesForUser(this.selectedUser)
      .subscribe((roles) => {
        this.userRoles = roles;
        this.userRolesDataSource = new MatTableDataSource(roles);

        this.userRolesDataSource.sortingDataAccessor = (
          item,
          column
        ): string => {
          switch (column) {
            case "site":
              return item.location.acronym;
            case "role":
              return item.role.roleName;
            default:
              return (item as any)[column];
          }
        };
        setTimeout(() => {
          this.userRolesDataSource.sort = this.sort;
        });
      });
  }

  private assignValues(): void {
    this.selectedUser.primaryLocation =
      this.inputForm.get(`primaryLocation`)?.value;

    const roles: Record<number, Role[]> = {};

    for (const role of this.userRoles) {
      const values = roles[role.location.id];
      if (values) {
        roles[role.location.id] = [...values, role.role];
      } else {
        roles[role.location.id] = [role.role];
      }
    }

    this.selectedUser.roles = roles;
  }

  private assignValuesToWbiSearch(): void {
    this.wbiSearch = this.inputForm.get(`wbiSearch`)?.value;
  }
}
