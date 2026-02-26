import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import {
  Snapshot,
  SnapshotService,
} from "../../../../services/landingai/snapshot.service";

export interface SnapshotListDialogData {
  projectId: number;
}

@Component({
  selector: "app-snapshot-list-dialog",
  standalone: false,
  templateUrl: "./snapshot-list-dialog.component.html",
  styleUrls: ["./snapshot-list-dialog.component.scss"],
})
export class SnapshotListDialogComponent implements OnInit {
  snapshots: Snapshot[] = [];
  loading = true;
  error: string | null = null;

  displayedColumns: string[] = [
    "snapshotName",
    "description",
    "imageCount",
    "labelCount",
    "createdAt",
    "createdBy",
  ];

  constructor(
    private snapshotService: SnapshotService,
    private dialogRef: MatDialogRef<SnapshotListDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: SnapshotListDialogData
  ) {}

  ngOnInit(): void {
    console.log("snapshots are", this.snapshots);
    this.loadSnapshots();
  }

  /**
   * Format date for display
   */
  formatDate(date: Date): string {
    return new Date(date).toLocaleString();
  }

  /**
   * Close dialog
   */
  onClose(): void {
    this.dialogRef.close();
  }

  /**
   * Load snapshots for the project
   */
  private loadSnapshots(): void {
    this.loading = true;
    this.error = null;

    this.snapshotService.getSnapshotsForProject(this.data.projectId).subscribe({
      next: (snapshots) => {
        this.snapshots = snapshots;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.error?.message || "Failed to load snapshots";
        this.loading = false;
      },
    });
  }
}
