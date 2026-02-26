import { Component, Input, OnInit } from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import { MassUploadResponse } from "app/models/mass-upload";
import { BaseDialogComponent } from "../BaseDialogComponent";

@Component({
  selector: "app-mass-upload-response",
  templateUrl: "mass-upload-response.component.html",
  standalone: false,
})
export class MassUploadResponseComponent
  extends BaseDialogComponent
  implements OnInit
{
  @Input() public title!: string;
  @Input() public response!: MassUploadResponse;

  public isLoadingResults = false;
  public errorsDataSource!: MatTableDataSource<MassUploadResponseFeedback>;
  public listOfUnuploadedObjects!: MatTableDataSource<MassUploadResponseFeedback>;
  public displayColumnsErrors = ["rowNumber", "errorMsg"];
  public displayColumnsErrorsBasic = ["errorMsg"];
  public displayColumnsUnuploadedObjects = ["rowNumber", "object"];
  public displayColumnsUnuploadedObjectsBasic = ["object"];

  public ngOnInit(): void {
    const errors: MassUploadResponseFeedback[] = [];
    this.response.errorMessages.forEach((errorMessage) => {
      errors.push(
        new MassUploadResponseFeedback(
          this.getRowNumber(errorMessage),
          this.getErrorMessage(errorMessage)
        )
      );
    });
    this.response.warningMessages.forEach((warningMessage) => {
      errors.push(
        new MassUploadResponseFeedback(
          this.getRowNumber(warningMessage),
          this.getErrorMessage(warningMessage)
        )
      );
    });
    this.errorsDataSource = new MatTableDataSource(errors);

    const unuploadedObjects: MassUploadResponseFeedback[] = [];
    this.response.excludedFromUploadList.forEach((unuploadedObject) => {
      unuploadedObjects.push(
        new MassUploadResponseFeedback(
          this.getRowNumber(unuploadedObject),
          this.getErrorMessage(unuploadedObject)
        )
      );
    });
    this.listOfUnuploadedObjects = new MatTableDataSource(unuploadedObjects);
  }

  public hasDelimiter(error: string): boolean {
    return error.indexOf(";;") > -1 && error.split(";;")[0] != undefined;
  }

  public getRowNumber(error: string): number {
    if (this.hasDelimiter(error)) {
      return +error.split(";;")[0];
    }

    return -1;
  }

  public getErrorMessage(error: string): string {
    if (this.hasDelimiter(error)) {
      return error.split(";;")[1];
    }

    return error;
  }

  public downloadUserFeedback(): void {
    const file = new Blob(this.buildErrorLinesForDownload(), { type: ".txt" });

    const a = document.createElement("a"),
      url = URL.createObjectURL(file);
    a.href = url;
    a.download = "MU_Feedback";
    document.body.appendChild(a);
    a.click();
    setTimeout(function () {
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    }, 0);
  }

  public isPlanningFlowOrActivationPlanningFlow(): boolean {
    return (
      this.title.indexOf("Activation Flows") > -1 ||
      this.title.indexOf("Planning Flows") > -1
    );
  }

  private buildErrorLinesForDownload(): string[] {
    const warningMessages = this.response.warningMessages.map(
      (warning) => warning.replace(";;", " - ") + "\n"
    );
    return this.response.errorMessages
      .map((error) => error.replace(";;", " - ") + "\n")
      .concat(warningMessages);
  }
}

export class MassUploadResponseFeedback {
  public excelRowNumber: number;
  public message: string;

  public constructor(excelRowNumber: number, message: string) {
    this.excelRowNumber = excelRowNumber;
    this.message = message;
  }
}
