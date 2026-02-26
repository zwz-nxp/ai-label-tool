export class TibcoJobResult {
  public jobResult?: JobResult;
  public message = "";
}

export enum JobResult {
  SUCCESS = "SUCCESS",
  DISABLED = "DISABLED",
  FAILED = "FAILED",
}
