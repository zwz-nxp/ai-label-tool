import { Component, Inject } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectListItem } from "app/models/landingai/project";

export interface ProjectEditDialogData {
  project: ProjectListItem;
}

export interface ProjectEditResult {
  name: string;
  modelName: string;
  groupName?: string;
}

@Component({
  selector: "app-project-edit-dialog",
  standalone: false,
  templateUrl: "./project-edit-dialog.component.html",
  styleUrls: ["./project-edit-dialog.component.scss"],
})
export class ProjectEditDialogComponent {
  editForm: FormGroup;

  // Available model names
  availableModels: string[] = [
    "RtmDet-[9M]",
    "RepPoints-[20M]",
    "RepPoints-[37M]",
    "ODEmbedded-[23M]",
  ];

  // Available group names
  availableGroups: string[] = ["WT", "FE", "BE", "QA", "AT"];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ProjectEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProjectEditDialogData
  ) {
    // Initialize form with actual values from the start
    this.editForm = this.fb.group({
      name: [
        data.project.name || "",
        [Validators.required, Validators.minLength(3)],
      ],
      modelName: [data.project.modelName || null],
      groupName: [data.project.groupName || null],
    });

    // Debug: Log the initial values
    console.log("Edit dialog - Initial modelName:", data.project.modelName);
    console.log("Edit dialog - Initial groupName:", data.project.groupName);
  }

  get nameControl() {
    return this.editForm.get("name");
  }

  get modelNameControl() {
    return this.editForm.get("modelName");
  }

  get groupNameControl() {
    return this.editForm.get("groupName");
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.editForm.valid) {
      const result: ProjectEditResult = {
        name: this.editForm.value.name,
        modelName: this.editForm.value.modelName || "",
        groupName: this.editForm.value.groupName || undefined,
      };
      this.dialogRef.close(result);
    }
  }
}
