import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { FormBuilder, FormGroup } from "@angular/forms";
import { ProjectMetadata } from "../../../../models/landingai/project-metadata.model";

export interface BatchSetMetadataDialogData {
  projectId: number;
  selectedImageIds: number[];
  availableMetadata: ProjectMetadata[];
}

@Component({
  selector: "app-batch-set-metadata-dialog",
  standalone: false,
  templateUrl: "./batch-set-metadata-dialog.component.html",
  styleUrls: ["./batch-set-metadata-dialog.component.scss"],
})
export class BatchSetMetadataDialogComponent implements OnInit {
  metadataForm: FormGroup;
  availableMetadata: ProjectMetadata[] = [];
  selectedImageCount: number = 0;

  constructor(
    private dialogRef: MatDialogRef<BatchSetMetadataDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: BatchSetMetadataDialogData,
    private fb: FormBuilder
  ) {
    this.availableMetadata = data.availableMetadata;
    this.selectedImageCount = data.selectedImageIds.length;

    // Create form with a control for each metadata field
    const formControls: { [key: string]: any } = {};
    this.availableMetadata.forEach((metadata) => {
      // For multiple values, initialize with empty array, otherwise empty string
      const initialValue = metadata.multipleValues ? [] : "";
      formControls[metadata.name] = [initialValue];
    });
    this.metadataForm = this.fb.group(formControls);
  }

  ngOnInit(): void {}

  /**
   * Get predefined values as an array
   */
  getPredefinedValues(metadata: ProjectMetadata): string[] {
    if (!metadata.predefinedValues) {
      return [];
    }
    return metadata.predefinedValues.split(",").map((v) => v.trim());
  }

  /**
   * Check if metadata field uses predefined values
   */
  isPredefined(metadata: ProjectMetadata): boolean {
    return metadata.valueFrom === "PREDEFINED";
  }

  /**
   * Check if metadata field allows multiple values
   */
  allowsMultiple(metadata: ProjectMetadata): boolean {
    return metadata.multipleValues === true;
  }

  /**
   * Handle save button click
   */
  onSave(): void {
    const metadataValues: { [key: string]: string } = {};

    // Only include metadata fields that have values
    this.availableMetadata.forEach((metadata) => {
      const value = this.metadataForm.get(metadata.name)?.value;
      if (value) {
        // Handle array values (multiple selection)
        if (Array.isArray(value) && value.length > 0) {
          metadataValues[metadata.name] = value.join(",");
        }
        // Handle string values
        else if (typeof value === "string" && value.trim()) {
          metadataValues[metadata.name] = value.trim();
        }
      }
    });

    // Close dialog with metadata values
    this.dialogRef.close({
      metadata: metadataValues,
      imageIds: this.data.selectedImageIds,
    });
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Check if form has any values
   */
  hasValues(): boolean {
    return this.availableMetadata.some((metadata) => {
      const value = this.metadataForm.get(metadata.name)?.value;
      // Check for array values (multiple selection)
      if (Array.isArray(value)) {
        return value.length > 0;
      }
      // Check for string values
      return value && value.trim();
    });
  }
}
