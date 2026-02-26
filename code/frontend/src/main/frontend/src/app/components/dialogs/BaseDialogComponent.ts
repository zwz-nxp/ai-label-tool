import { Component } from "@angular/core";
import { Person } from "app/models/person";
import { DataService } from "app/utils/api-access/data-service";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialogRef } from "@angular/material/dialog";
import { Store } from "@ngrx/store";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";

/**
 * Base dialog abstract class with all necessary dependencies and methods for all dialogs in the app.
 */
@Component({
  selector: "app-base-dialog",
  template: "",
})
export abstract class BaseDialogComponent {
  /**
   * Flag for giving away whether the content is still loading.
   */
  public isLoading = false;

  /**
   * Holds object information regarding the current logged in user.
   */
  public currentUser!: Person;

  /**
   * Defines the index of a selected tab inside a dialog.
   */
  public tabIndex = -1;

  public constructor(
    public dialogRef: MatDialogRef<BaseDialogComponent>,
    public dataService: DataService,
    public snackBar: MatSnackBar,
    protected store: Store
  ) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        this.currentUser = user ?? new Person();
      });
  }

  /**
   * Closes the dialog.
   */
  public close(): void {
    this.dialogRef.close();
  }

  /**
   * Display success message in snackbar.
   * @param msg
   */
  public displayErrorMsg(msg: string): void {
    SnackbarUtils.displayErrorMsg(this.snackBar, msg);
  }

  /**
   * Display error message that comes from backend Json/Rest call.
   * @param msg
   */
  public displayServerErrorMsg(msg: string): void {
    SnackbarUtils.displayServerErrorMsg(this.snackBar, msg);
  }

  /**
   * Display warning message in snackbar.
   * @param msg
   */
  public displayWarningMsg(msg: string): void {
    SnackbarUtils.displayWarningMsg(this.snackBar, msg);
  }

  /**
   * Display success message in snackbar
   * @param msg
   */
  public displaySuccessMsg(msg: string): void {
    SnackbarUtils.displaySuccessMsg(this.snackBar, msg);
  }

  /**
   * Brings user to the first tab. Called on Back buttons.
   */
  public switchToFirstTab(): void {
    if (this.tabIndex > 0) {
      this.tabIndex = 0;
    }
  }
}
