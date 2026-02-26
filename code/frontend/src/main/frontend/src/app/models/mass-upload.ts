export class MassUploadTemplateData {
  public url = "";
}

export class MassUploadResponse {
  public successCount = 0;
  public insertCount = 0;
  public updateCount = 0;
  public errorCount = 0;
  public warningCount = 0;
  public duplicateCount = 0;
  public ignoreCount = 0;
  public deleteCount = 0;
  public recordsToBeLoaded = 0;
  public errorMessages: Array<string> = [];
  public warningMessages: Array<string> = [];
  public excludedFromUploadList: Array<string> = [];
}
