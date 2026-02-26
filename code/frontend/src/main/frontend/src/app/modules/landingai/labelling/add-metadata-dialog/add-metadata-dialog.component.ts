import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectMetadata } from "app/services/landingai/metadata.service";

export interface AddMetadataDialogData {
  availableMetadata: ProjectMetadata[];
}

export interface AddMetadataDialogResult {
  metadata: ProjectMetadata;
  value: string;
}

@Component({
  selector: "app-add-metadata-dialog",
  templateUrl: "./add-metadata-dialog.component.html",
  styleUrls: ["./add-metadata-dialog.component.scss"],
  standalone: false,
})
export class AddMetadataDialogComponent implements OnInit {
  selectedMetadata: ProjectMetadata | null = null;
  metadataValue: string = "";
  selectedValues: string[] = []; // For multiple select

  constructor(
    public dialogRef: MatDialogRef<AddMetadataDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AddMetadataDialogData
  ) {}

  ngOnInit(): void {
    // Auto-select first metadata if only one available
    if (this.data.availableMetadata.length === 1) {
      this.selectedMetadata = this.data.availableMetadata[0];
    }
  }

  /**
   * Get predefined values as an array for dropdown fields
   */
  getPredefinedValues(): string[] {
    if (!this.selectedMetadata?.predefinedValues) {
      return [];
    }
    return this.selectedMetadata.predefinedValues
      .split(",")
      .map((v) => v.trim());
  }

  /**
   * Check if the form is valid
   */
  isValid(): boolean {
    if (!this.selectedMetadata) {
      return false;
    }

    // For predefined values with multiple select
    if (this.usesPredefinedValues() && this.allowsMultipleValues()) {
      return this.selectedValues.length > 0;
    }

    // For other types, check if value is not empty (allow "0" and "false")
    return (
      this.metadataValue !== null &&
      this.metadataValue !== undefined &&
      this.metadataValue.toString().trim().length > 0
    );
  }

  /**
   * Handle metadata selection change
   */
  onMetadataChange(): void {
    // Reset values when metadata changes
    this.metadataValue = "";
    this.selectedValues = [];
  }

  /**
   * Handle save button click
   */
  onSave(): void {
    if (!this.isValid() || !this.selectedMetadata) {
      return;
    }

    let finalValue = this.metadataValue;

    // For multiple select, join values with comma
    if (this.usesPredefinedValues() && this.allowsMultipleValues()) {
      finalValue = this.selectedValues.join(", ");
    }

    const result: AddMetadataDialogResult = {
      metadata: this.selectedMetadata,
      value: finalValue,
    };

    this.dialogRef.close(result);
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Get the input type based on metadata type
   */
  getInputType(): string {
    if (!this.selectedMetadata) {
      return "text";
    }

    switch (this.selectedMetadata.type?.toLowerCase()) {
      case "number":
        return "number";
      case "boolean":
        return "text"; // We'll use a select for boolean
      default:
        return "text";
    }
  }

  /**
   * Check if the selected metadata uses predefined values (case-insensitive)
   */
  usesPredefinedValues(): boolean {
    if (!this.selectedMetadata) {
      return false;
    }
    const valueFrom = this.selectedMetadata.valueFrom?.toLowerCase();
    return (
      valueFrom === "predefined" || !!this.selectedMetadata.predefinedValues
    );
  }

  /**
   * Check if the selected metadata allows multiple values
   */
  allowsMultipleValues(): boolean {
    return this.selectedMetadata?.multipleValues === true;
  }

  /**
   * Check if the selected metadata is boolean type
   */
  isBooleanType(): boolean {
    return this.selectedMetadata?.type?.toLowerCase() === "boolean";
  }
}
