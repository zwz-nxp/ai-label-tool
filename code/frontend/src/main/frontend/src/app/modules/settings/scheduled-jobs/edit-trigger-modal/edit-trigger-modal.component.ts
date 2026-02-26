import { Component, OnInit } from "@angular/core";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { JobOverview } from "app/models/scheduled-jobs";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { FormBuilder, FormControl, Validators } from "@angular/forms";
import { MatDialogRef } from "@angular/material/dialog";
import { DataService } from "app/utils/api-access/data-service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { firstValueFrom } from "rxjs";
import cronstrue from "cronstrue";
import { Store } from "@ngrx/store";

@Component({
  selector: "app-edit-trigger-modal",
  templateUrl: "./edit-trigger-modal.component.html",
  standalone: false,
})
export class EditTriggerModalComponent
  extends BaseDialogComponent
  implements OnInit
{
  public jobOverview = new JobOverview();
  public isNew = true;
  public title!: string;
  public submitted = false;
  public availableJobs: JobOverview[] = [];
  public filteredAvailableJobs: JobOverview[] = [];
  public triggerInputForm = this.formBuilder.group({
    job: new FormControl<JobOverview | null>(null, Validators.required),
    jobDescription: ["", Validators.required],
    jobFilter: ["", Validators.required],
    timeZone: ["UTC", Validators.required],
    triggerName: ["", Validators.required],
    triggerDescription: ["", Validators.required],
    cronExpression: ["", Validators.required],
  });

  public constructor(
    dialogRef: MatDialogRef<EditTriggerModalComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public formBuilder: FormBuilder
  ) {
    super(dialogRef, dataService, snackBar, store);

    this.triggerInputForm.controls.jobFilter.valueChanges.subscribe((value) => {
      this.filterJobs(value);
    });
  }

  public ngOnInit(): void {
    this.title = this.isNew
      ? "Add new Trigger"
      : `Edit Trigger ${this.jobOverview?.jobDescription}`;
    this.setFormValues();
    this.getAvailableJobs();

    if (!this.isNew) {
      this.triggerInputForm.controls.triggerName.disable();
    }
  }

  public displayJobNameAndDescription(job: JobOverview): string {
    if (job.jobDescription) {
      return `${job.jobName} (${job.jobDescription})`;
    }
    return `${job.jobName}`;
  }

  public updateSchedule(): void {
    this.assignValues();
    this.submitted = true;
    firstValueFrom(this.dataService.createJob(this.jobOverview))
      .then(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          `Successfully saved job with description: ${this.jobOverview.jobDescription} with schedule ${this.jobOverview.cronExpression}`
        );
        this.close();
      })
      .catch((error) => {
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error);
      });
  }

  protected compareJob(a: JobOverview, b: JobOverview): boolean {
    return a?.jobName === b?.jobName;
  }

  protected parseCron(value: string | null): string {
    try {
      return value
        ? cronstrue.toString(value ?? "", {
            use24HourTimeFormat: true,
          })
        : "";
    } catch (err) {
      return "Invalid Cron Expression";
    }
  }

  private filterJobs(value?: string | null): void {
    if (!value) {
      this.filteredAvailableJobs = this.availableJobs;
    } else {
      this.filteredAvailableJobs = this.availableJobs.filter((job) =>
        this.displayJobNameAndDescription(job)
          .toLowerCase()
          .includes(value.toLowerCase())
      );
    }
  }

  private getAvailableJobs(): void {
    firstValueFrom(this.dataService.getAvailableJobs())
      .then((jobs: JobOverview[]) => {
        this.availableJobs = jobs.sort((a, b) =>
          a.jobName.localeCompare(b.jobName)
        );
        this.filteredAvailableJobs = this.availableJobs;
      })
      .catch((error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error)
      );
  }

  private setFormValues(): void {
    if (!this.isNew) {
      this.triggerInputForm.patchValue({
        job: this.jobOverview,
        jobDescription: this.jobOverview.jobDescription,
        triggerName: this.jobOverview.triggerName,
        triggerDescription: this.jobOverview.triggerDescription,
        timeZone: this.jobOverview.timeZone,
        cronExpression: this.jobOverview.cronExpression,
      });
    }
  }

  private assignValues(): void {
    this.jobOverview.className =
      this.triggerInputForm.controls.job.value?.jobName ?? "";
    this.jobOverview.jobName =
      this.triggerInputForm.controls.job.value?.jobName ?? "";
    this.jobOverview.jobDescription =
      this.triggerInputForm.controls.job.value?.jobDescription.trim() ?? "";
    this.jobOverview.timeZone =
      this.triggerInputForm.controls.timeZone.value ?? "";
    this.jobOverview.triggerName =
      this.triggerInputForm.controls.triggerName.value?.trim() ?? "";
    this.jobOverview.triggerDescription =
      this.triggerInputForm.controls.triggerDescription.value?.trim() ?? "";
    this.jobOverview.cronExpression =
      this.triggerInputForm.controls.cronExpression.value?.trim() ?? "";
  }
}
