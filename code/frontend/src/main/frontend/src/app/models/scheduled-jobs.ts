export class JobOverview {
  public jobName!: string;
  public jobDescription = "";
  public className!: string;
  public triggerName = "";
  public triggerDescription = "";
  public cronExpression = "";
  public timeZone = "";
  public triggerState = "";
  public previousFireTime = "";
  public nextFireTime = "";
  public lastUpdated = new Date();
  public updatedBy = "";
}

export class JobExecutionLogDto {
  public time = "";
  public info = "";
}

export class JobExecutionLogRequest {
  public jobName?: string;
  public timestampUpperBound = "2199-12-31T23:59:59.999";
  public maxResults = 20;
}
