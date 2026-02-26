import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

export interface SnapshotPreviewStats {
  labeled: number;
  unlabeled: number;
  noClass: number;
  trainCount: number;
  devCount: number;
  testCount: number;
  unassignedCount: number;
}

export interface SnapshotCreateDialogData {
  projectId: number;
  totalImages: number;
  previewStats: SnapshotPreviewStats;
}

export interface SnapshotCreateDialogResult {
  snapshotName: string;
  description: string;
}

@Component({
  selector: "app-snapshot-create-dialog",
  standalone: false,
  templateUrl: "./snapshot-create-dialog.component.html",
  styleUrls: ["./snapshot-create-dialog.component.scss"],
})
export class SnapshotCreateDialogComponent implements OnInit {
  snapshotForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<SnapshotCreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: SnapshotCreateDialogData
  ) {
    this.snapshotForm = this.fb.group({
      snapshotName: ["", [Validators.required, Validators.maxLength(100)]],
      description: ["", [Validators.maxLength(500)]],
    });
  }

  ngOnInit(): void {
    // Set default snapshot name with timestamp
    const defaultName = this.generateDefaultSnapshotName();
    this.snapshotForm.patchValue({ snapshotName: defaultName });
  }

  /**
   * Generate default snapshot name with current date and time
   * Format: Snapshot-MM-DD-YYYY HH:mm
   */
  private generateDefaultSnapshotName(): string {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    const year = now.getFullYear();
    const hours = String(now.getHours()).padStart(2, "0");
    const minutes = String(now.getMinutes()).padStart(2, "0");
    return `Snapshot-${month}-${day}-${year} ${hours}:${minutes}`;
  }

  /**
   * Get image status bar width percentages
   */
  getImageStatusBarWidth(type: "labeled" | "unlabeled" | "noClass"): number {
    const total = this.data.totalImages || 1;
    const stats = this.data.previewStats;
    if (!stats) return 0;

    switch (type) {
      case "labeled":
        return (stats.labeled / total) * 100;
      case "unlabeled":
        return (stats.unlabeled / total) * 100;
      case "noClass":
        return (stats.noClass / total) * 100;
      default:
        return 0;
    }
  }

  /**
   * Get split distribution bar width percentages
   */
  getSplitBarWidth(type: "train" | "dev" | "test" | "unassigned"): number {
    const stats = this.data.previewStats;
    if (!stats) return 0;

    const totalLabeled = stats.labeled || 1;
    switch (type) {
      case "train":
        return (stats.trainCount / totalLabeled) * 100;
      case "dev":
        return (stats.devCount / totalLabeled) * 100;
      case "test":
        return (stats.testCount / totalLabeled) * 100;
      case "unassigned":
        return (stats.unassignedCount / totalLabeled) * 100;
      default:
        return 0;
    }
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.snapshotForm.valid) {
      const result: SnapshotCreateDialogResult = {
        snapshotName: this.snapshotForm.value.snapshotName,
        description: this.snapshotForm.value.description || "",
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle cancel
   */
  onCancel(): void {
    this.dialogRef.close();
  }
}
