import { Component, Inject } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectListItem } from "../../../../models/landingai/project";

export interface ProjectDeleteDialogData {
  project: ProjectListItem;
}

@Component({
  selector: "app-project-delete-dialog",
  standalone: false,
  templateUrl: "./project-delete-dialog.component.html",
  styleUrls: ["./project-delete-dialog.component.scss"],
})
export class ProjectDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ProjectDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProjectDeleteDialogData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
