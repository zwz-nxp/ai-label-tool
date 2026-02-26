import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectClass } from "../../../../models/landingai/project-class.model";

export interface BatchSetClassDialogData {
  projectId: number;
  selectedImageIds: number[];
  availableClasses: ProjectClass[];
}

@Component({
  selector: "app-batch-set-class-dialog",
  standalone: false,
  templateUrl: "./batch-set-class-dialog.component.html",
  styleUrls: ["./batch-set-class-dialog.component.scss"],
})
export class BatchSetClassDialogComponent implements OnInit {
  availableClasses: ProjectClass[] = [];
  selectedClassId: number | null = null;
  selectedImageCount: number = 0;

  constructor(
    private dialogRef: MatDialogRef<BatchSetClassDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: BatchSetClassDialogData
  ) {
    this.availableClasses = data.availableClasses;
    this.selectedImageCount = data.selectedImageIds.length;
  }

  ngOnInit(): void {}

  /**
   * Handle class selection
   */
  onClassSelect(classId: number): void {
    this.selectedClassId = classId;
  }

  /**
   * Check if a class is selected
   */
  isClassSelected(classId: number): boolean {
    return this.selectedClassId === classId;
  }

  /**
   * Handle save button click
   */
  onSave(): void {
    if (this.selectedClassId !== null) {
      this.dialogRef.close({
        classId: this.selectedClassId,
        imageIds: this.data.selectedImageIds,
      });
    }
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Check if save button should be enabled
   */
  canSave(): boolean {
    return this.selectedClassId !== null;
  }
}
