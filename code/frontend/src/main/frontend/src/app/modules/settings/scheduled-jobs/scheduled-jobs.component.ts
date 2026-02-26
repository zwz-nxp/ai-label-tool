import { Component, OnInit, ViewChild } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { DataService } from "app/utils/api-access/data-service";
import { RoleEnum } from "app/models/role";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { MatTable, MatTableDataSource } from "@angular/material/table";
import { JobOverview } from "app/models/scheduled-jobs";
import { MatDialog } from "@angular/material/dialog";
import { EditTriggerModalComponent } from "app/modules/settings/scheduled-jobs/edit-trigger-modal/edit-trigger-modal.component";
import { JobExecutionLogsComponent } from "./job-execution-logs/job-execution-logs.component";
import { ModelFactory } from "app/utils/api-access/model-factory";
import { MatSort } from "@angular/material/sort";
import { HelpScreenComponent } from "./help-screen/help-screen.component";
import { firstValueFrom } from "rxjs";
import cronstrue from "cronstrue";
import { ConfirmDialogService } from "app/utils/services/confirm-dialog.service";
import { EditJobModalComponent } from "app/modules/settings/scheduled-jobs/edit-job-modal/edit-job-modal.component";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { Update, UpdateType } from "app/models/update";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Store } from "@ngrx/store";
import { Person } from "app/models/person";

@Component({
  selector: "app-scheduled-jobs",
  templateUrl: "./scheduled-jobs.component.html",
  standalone: false,
})
export class ScheduledJobsComponent implements OnInit {
  @ViewChild("scheduledJobsTable") public scheduledJobsTable!: MatTable<
    MatTableDataSource<JobOverview>
  >;
  @ViewChild("allJobsTable") public allJobsTable!: MatTable<
    MatTableDataSource<JobOverview>
  >;
  @ViewChild("scheduledJobsTable") public scheduledSort!: MatSort;
  @ViewChild("allJobsTable") public allJobsSort!: MatSort;

  public tabIndex = 0;
  public loading = false;
  public currentUser!: Person;
  public scheduledJobsOverviewDataSource = new MatTableDataSource<JobOverview>(
    []
  );
  public allJobsOverviewDataSource = new MatTableDataSource<JobOverview>([]);

  public scheduledTableColumns = [
    "jobDescription",
    "cronExpression",
    "timeZone",
    "previousFireTime",
    "nextFireTime",
    "triggerState",
    "triggerName",
    "triggerDescription",
    "actions",
  ] as const;
  public allJobsTableColumns = [
    "jobName",
    "jobDescription",
    "actions",
  ] as const;

  public constructor(
    private dataService: DataService,
    public dialogService: MatDialog,
    public authorizationService: AuthorizationService,
    public snackbar: MatSnackBar,
    private confirmDialogService: ConfirmDialogService,
    private dataUpdateService: DataUpdateService,
    private store: Store
  ) {}

  public ngOnInit(): void {
    if (!this.isAuthorized()) {
      SnackbarUtils.displayWarningMsg(
        this.snackbar,
        "You need " + RoleEnum.ADMINISTRATOR_SYSTEM + " role"
      );
    }
    this.loadScheduledJobOverviews().then(() => {
      this.scheduledSort.sort({
        id: "jobDescription",
        start: "asc",
        disableClear: false,
      });
      this.allJobsSort.sort({
        id: "jobName",
        start: "asc",
        disableClear: false,
      });
    });

    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        if (user) {
          this.currentUser = user;
        }
      });
    this.watchForUpdate();
  }

  public isAuthorized(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      RoleEnum.ADMINISTRATOR_SYSTEM
    );
  }

  public addTrigger(): void {
    const dialogReference = this.dialogService.open(EditTriggerModalComponent);

    dialogReference.componentInstance.jobOverview = new JobOverview();
    dialogReference
      .afterClosed()
      .subscribe(() => this.loadScheduledJobOverviews());
  }

  public editTrigger(jobOverview: JobOverview): void {
    const dialogReference = this.dialogService.open(EditTriggerModalComponent);

    dialogReference.componentInstance.jobOverview = jobOverview;
    dialogReference.componentInstance.isNew = false;
    dialogReference
      .afterClosed()
      .subscribe(() => this.loadScheduledJobOverviews());
  }

  public isPaused(scheduledJobOverview: JobOverview): boolean {
    return "PAUSED" === scheduledJobOverview.triggerState?.toUpperCase();
  }

  public pauseTrigger(jobOverview: JobOverview): void {
    this.loading = true;
    firstValueFrom(this.dataService.pauseCronTrigger(jobOverview.triggerName))
      .then((_) => {
        SnackbarUtils.displaySuccessMsg(this.snackbar, `Paused trigger`);
        return this.loadScheduledJobOverviews();
      })
      .catch((error: Error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackbar, error.message)
      )
      .finally(() => (this.loading = false));
  }

  public resumeTrigger(jobOverview: JobOverview): void {
    this.loading = true;
    firstValueFrom(this.dataService.resumeCronTrigger(jobOverview.triggerName))
      .then((_) => {
        SnackbarUtils.displaySuccessMsg(this.snackbar, `Resumed trigger`);
        return this.loadScheduledJobOverviews();
      })
      .catch((error: Error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackbar, error.message)
      )
      .finally(() => (this.loading = false));
  }

  public editJob(jobOverview: JobOverview): void {
    const dialogReference = this.dialogService.open(EditJobModalComponent);

    dialogReference.componentInstance.jobOverview = jobOverview;
    dialogReference
      .afterClosed()
      .subscribe(() => this.loadScheduledJobOverviews());
  }

  public runJob(jobOverview: JobOverview): void {
    this.loading = true;
    firstValueFrom(this.dataService.runJob(jobOverview))
      .then((_) => {})
      .catch((error: Error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackbar, error.message)
      )
      .finally(() => {
        this.loading = false;
        this.loadScheduledJobOverviews();
      });
  }

  public deleteTrigger(jobOverview: JobOverview): void {
    const dialog = this.confirmDialogService.openDialog(
      "Please confirm",
      "Do you really want to delete this schedule?"
    );
    firstValueFrom(dialog.afterClosed()).then((result) => {
      if (result) {
        this.loading = true;
        firstValueFrom(this.dataService.deleteTrigger(jobOverview))
          .then((_) => {
            SnackbarUtils.displaySuccessMsg(
              this.snackbar,
              `"${jobOverview.triggerName}" deleted`
            );
            return this.loadScheduledJobOverviews();
          })
          .catch((error: Error) =>
            SnackbarUtils.displayServerErrorMsg(this.snackbar, error.message)
          )
          .finally(() => (this.loading = false));
      }
    });
  }

  public showJobExecutionLogs(jobOverview: JobOverview): void {
    const dialogReference = this.dialogService.open(JobExecutionLogsComponent);

    dialogReference.componentInstance.jobOverview = jobOverview;
    dialogReference
      .afterClosed()
      .subscribe(() => this.loadScheduledJobOverviews());
  }

  public openGuidelinesPopup(): void {
    this.dialogService.open(HelpScreenComponent);
  }

  protected parseCron(cronExpression: string): string {
    try {
      return cronstrue.toString(cronExpression, {
        use24HourTimeFormat: true,
      });
    } catch (err) {
      return "Invalid Cron Expression";
    }
  }

  protected parseJobStatus(status: string): string {
    return status.toLowerCase() === "blocked" ? "running" : status;
  }

  private loadScheduledJobOverviews(): Promise<void> {
    this.loading = true;
    return firstValueFrom(this.dataService.scheduledJobOverviews())
      .then((data) => {
        const scheduledJobsOverview = ModelFactory.createArray(
          data,
          JobOverview
        );
        const scheduled: JobOverview[] = [];
        const allJobs: JobOverview[] = [];

        scheduledJobsOverview.forEach((job) => {
          if (job.cronExpression?.trim()?.length > 0) {
            scheduled.push(job);
          }
          if (!allJobs.some((other) => job.jobName === other.jobName)) {
            allJobs.push(job);
          }
        });
        this.scheduledJobsOverviewDataSource = new MatTableDataSource(
          scheduled
        );
        this.allJobsOverviewDataSource = new MatTableDataSource(allJobs);
        this.scheduledJobsOverviewDataSource.sort = this.scheduledSort;
        this.allJobsOverviewDataSource.sort = this.allJobsSort;
      })
      .finally(() => (this.loading = false));
  }

  private watchForUpdate(): void {
    this.dataUpdateService.updateEmitter.subscribe((update: Update) => {
      if (
        update.updatedType === UpdateType.USER_ALERT &&
        update.userWbi.includes(this.currentUser.wbi)
      ) {
        const data = update.updateData as string;
        if (data) {
          const information = data.split(";");
          const triggerName = information[0];
          const result = information[1];
          switch (result) {
            case "failed":
              SnackbarUtils.displayErrorMsg(
                this.snackbar,
                `${triggerName} failed to complete`
              );
              break;
            case "veto":
              SnackbarUtils.displayWarningMsg(
                this.snackbar,
                `${triggerName} was cancelled`
              );
              break;
            case "success":
              SnackbarUtils.displaySuccessMsg(
                this.snackbar,
                `${triggerName} successfully ran to completion`
              );
              break;
            default:
              SnackbarUtils.displaySuccessMsg(
                this.snackbar,
                `${triggerName} started`
              );
              break;
          }
        }
      }
    });
  }
}
