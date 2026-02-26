import { Component, Inject, OnDestroy, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { interval, Subscription } from "rxjs";

export interface DownloadProgressDialogData {
  title: string;
  message: string;
}

/**
 * Download Progress Dialog Component
 * Displays a blocking progress dialog with timer during download operations
 */
@Component({
  selector: "app-download-progress-dialog",
  standalone: false,
  templateUrl: "./download-progress-dialog.component.html",
  styleUrls: ["./download-progress-dialog.component.scss"],
})
export class DownloadProgressDialogComponent implements OnInit, OnDestroy {
  elapsedTime: number = 0;
  private timerSubscription?: Subscription;

  constructor(
    public dialogRef: MatDialogRef<DownloadProgressDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DownloadProgressDialogData
  ) {
    // Disable closing by clicking outside or pressing ESC
    dialogRef.disableClose = true;
  }

  ngOnInit(): void {
    // Start timer - update every second
    this.timerSubscription = interval(1000).subscribe(() => {
      this.elapsedTime++;
    });
  }

  ngOnDestroy(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  /**
   * Format elapsed time as MM:SS
   */
  getFormattedTime(): string {
    const minutes = Math.floor(this.elapsedTime / 60);
    const seconds = this.elapsedTime % 60;
    return `${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;
  }
}
