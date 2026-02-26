import { ChangeDetectorRef, Component } from "@angular/core";
import { MatDialogRef } from "@angular/material/dialog";
import { Idle } from "@ng-idle/core";
import { IdleService } from "app/utils/services/idle.service";

@Component({
  selector: "app-idle-timeout",
  templateUrl: "./idle-timeout.component.html",
  standalone: false,
})
export class IdleTimeoutComponent {
  public countdown = 90;
  public interval?: NodeJS.Timeout;
  public timedOut = false;
  public dialogTitle = "Session Idle Warning";

  public constructor(
    idle: Idle,
    private idleService: IdleService,
    private dialogRef: MatDialogRef<IdleTimeoutComponent>,
    private ref: ChangeDetectorRef
  ) {
    idle.onTimeoutWarning.subscribe(() => {
      this.startTimer();
    });

    idle.onIdleEnd.subscribe(() => {
      this.resetTimer();
    });

    idle.onTimeout.subscribe(() => {
      this.dialogTitle = "Session Timed Out";
      this.timedOut = true;
    });
  }

  public get idleState(): string {
    return this.timedOut
      ? "Maximum idle time of 2 hours reached. You are signed out for security reasons. Press OK to sign in again."
      : `It looks like you have been idle for a while. For security reasons, your session has a maximum duration of 2 hours and will time out due to inactivity in ${this.countdown} minutes.`;
  }

  public closeDialog(): void {
    this.idleChanged();
    this.stopTimer();
    this.dialogRef.close();
  }

  private startTimer(): void {
    if (!this.interval) {
      this.interval = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
        } else {
          this.stopTimer();
        }
        this.ref.detectChanges();
      }, 60000);
    }
  }

  private stopTimer(): void {
    clearInterval(this.interval);
    this.interval = undefined;
  }

  private resetTimer(): void {
    this.stopTimer();
    this.countdown = 120;
    this.startTimer();
  }

  private idleChanged(): void {
    this.idleService.emitIdleChangeEvent(false);
  }
}
