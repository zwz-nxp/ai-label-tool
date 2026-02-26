import { Component, Input, OnInit } from "@angular/core";
import {
  JobExecutionLogDto,
  JobExecutionLogRequest,
  JobOverview,
} from "app/models/scheduled-jobs";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { MatTableDataSource } from "@angular/material/table";
import { firstValueFrom } from "rxjs";

@Component({
  selector: "app-job-execution-logs",
  templateUrl: "./job-execution-logs.component.html",
  standalone: false,
})
export class JobExecutionLogsComponent
  extends BaseDialogComponent
  implements OnInit
{
  @Input() public jobOverview!: JobOverview;

  public title!: string;
  public jobExecutionLogs: JobExecutionLogDto[] = [];
  public jobOverviewAudit: JobOverview[] = [];
  public isJobOverviewAuditLoaded = false;
  public isJobExecutionLogsLoaded = false;

  public jobOverviewDataSource = new MatTableDataSource<JobOverview>();
  public jobExecutionLogDataSource =
    new MatTableDataSource<JobExecutionLogDto>();
  public auditColumns = [
    "jobDescription",
    "cronExpression",
    "timeZone",
    "triggerName",
    "triggerDescription",
    "action",
    "lastUpdated",
    "updatedBy",
  ] as const;

  public executionTimeColumns = ["executionTime", "status"] as const;

  public ngOnInit(): void {
    this.title = `View Job Auditing ${this.jobOverview.jobName}`;
    this.loadJobExecutionLogs();
  }

  public loadJobExecutionLogs(): void {
    const logTimeUpperBound = "2199-12-31T23:59:59.999";
    const jobExecutionLogRequest =
      this.getjobExecutionLogRequest(logTimeUpperBound);
    this.isLoading = true;
    let isReady = false;
    firstValueFrom(
      this.dataService.scheduledJobOverviewsAudit(
        this.jobOverview.jobName,
        this.jobOverview.triggerName ?? undefined
      )
    )
      .then((data) => {
        this.jobOverviewAudit = data;
        this.jobOverviewDataSource = new MatTableDataSource(
          this.jobOverviewAudit
        );
        this.isJobOverviewAuditLoaded = this.jobOverviewAudit.length > 0;
      })
      .catch((error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error)
      )
      .finally(() => {
        if (isReady) {
          this.isLoading = false;
        }
        isReady = true;
      });

    firstValueFrom(
      this.dataService.scheduledJobExecutionLogs(jobExecutionLogRequest)
    )
      .then((data) => {
        this.jobExecutionLogs = data;
        this.jobExecutionLogDataSource = new MatTableDataSource(
          this.jobExecutionLogs
        );
        this.isJobExecutionLogsLoaded = this.jobExecutionLogs.length > 0;
      })
      .catch((error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error)
      )
      .finally(() => {
        if (isReady) {
          this.isLoading = false;
        }
        isReady = true;
      });
  }

  public loadMore(): void {
    const logTimeUpperBound =
      this.jobExecutionLogs.length === 0
        ? "2199-12-31T23:59:59.999"
        : this.jobExecutionLogs[this.jobExecutionLogs.length - 1].time;
    const jobExecutionLogRequest =
      this.getjobExecutionLogRequest(logTimeUpperBound);

    firstValueFrom(
      this.dataService.scheduledJobExecutionLogs(jobExecutionLogRequest)
    )
      .then((data) => {
        this.jobExecutionLogDataSource = new MatTableDataSource(data);
      })
      .catch((error) =>
        SnackbarUtils.displayServerErrorMsg(this.snackBar, error)
      )
      .finally(() => {
        this.isLoading = false;
      });
  }

  private getjobExecutionLogRequest(
    logTimeUpperBound: string
  ): JobExecutionLogRequest {
    const jobExecutionLogRequest = new JobExecutionLogRequest();
    jobExecutionLogRequest.jobName = this.jobOverview.jobName;
    jobExecutionLogRequest.timestampUpperBound = logTimeUpperBound;
    jobExecutionLogRequest.maxResults = 10;
    return jobExecutionLogRequest;
  }
}
