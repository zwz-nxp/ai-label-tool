import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ModelParam } from "app/models/landingai/model-param.model";

export interface ModelParamDetailDialogData {
  modelParam: ModelParam;
}

@Component({
  selector: "app-model-param-detail-dialog",
  templateUrl: "./model-param-detail-dialog.component.html",
  styleUrls: ["./model-param-detail-dialog.component.scss"],
  standalone: false,
})
export class ModelParamDetailDialogComponent implements OnInit {
  formattedParameters: string = "";

  constructor(
    public dialogRef: MatDialogRef<ModelParamDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModelParamDetailDialogData
  ) {}

  ngOnInit(): void {
    this.formattedParameters = this.formatJson(this.data.modelParam.parameters);
  }

  /**
   * Format JSON string with proper indentation for display
   */
  formatJson(jsonString: string): string {
    try {
      const parsed = JSON.parse(jsonString);
      return JSON.stringify(parsed, null, 2);
    } catch (e) {
      return jsonString;
    }
  }

  /**
   * Format date for display
   */
  formatDate(date: Date | string): string {
    if (!date) {
      return "N/A";
    }

    const dateObj = typeof date === "string" ? new Date(date) : date;
    return dateObj.toLocaleString();
  }

  /**
   * Handle close button click
   */
  onClose(): void {
    this.dialogRef.close();
  }
}
