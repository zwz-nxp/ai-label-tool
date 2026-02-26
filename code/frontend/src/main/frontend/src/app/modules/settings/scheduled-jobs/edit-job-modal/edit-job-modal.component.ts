import { Component, OnInit } from "@angular/core";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { JobOverview } from "app/models/scheduled-jobs";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { FormBuilder, FormControl } from "@angular/forms";
import { MatDialogRef } from "@angular/material/dialog";
import { DataService } from "app/utils/api-access/data-service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { firstValueFrom } from "rxjs";
import { Store } from "@ngrx/store";

@Component({
  selector: "app-edit-job-modal",
  templateUrl: "./edit-job-modal.component.html",
  standalone: false,
})
export class EditJobModalComponent
  extends BaseDialogComponent
  implements OnInit
{
  public jobOverview = new JobOverview();
  public submitted = false;
  public jobDescriptionControl = new FormControl("");

  public constructor(
    dialogRef: MatDialogRef<EditJobModalComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public formBuilder: FormBuilder
  ) {
    super(dialogRef, dataService, snackBar, store);
  }

  public ngOnInit(): void {
    this.setFormValues();
  }

  public updateSchedule(): void {
    this.assignValues();
    this.submitted = true;
    firstValueFrom(this.dataService.createJob(this.jobOverview))
      .then(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          `Successfully saved job with description: ${this.jobOverview.jobDescription}`
        );
        this.close();
      })
      .catch((error) => {
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error);
      });
  }

  private setFormValues(): void {
    this.jobDescriptionControl.patchValue(this.jobOverview.jobDescription);
  }

  private assignValues(): void {
    this.jobOverview.jobDescription = this.jobDescriptionControl.value ?? "";
    this.jobOverview.className = this.jobOverview.jobName;
  }
}
