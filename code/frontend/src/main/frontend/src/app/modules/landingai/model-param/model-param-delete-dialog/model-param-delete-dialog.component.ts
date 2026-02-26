import { Component, Inject } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ModelParam } from "app/models/landingai/model-param.model";

export interface ModelParamDeleteDialogData {
  modelParam: ModelParam;
}

@Component({
  selector: "app-model-param-delete-dialog",
  templateUrl: "./model-param-delete-dialog.component.html",
  styleUrls: ["./model-param-delete-dialog.component.scss"],
  standalone: false,
})
export class ModelParamDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ModelParamDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModelParamDeleteDialogData
  ) {}

  /**
   * Handle confirm button click
   */
  onConfirm(): void {
    this.dialogRef.close(true);
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close(false);
  }
}
