import { Component, Input } from "@angular/core";
import { MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: "app-confirm-dialog",
  templateUrl: "./confirm-dialog.component.html",
  standalone: false,
})
export class ConfirmDialogComponent {
  @Input() public title!: string;
  @Input() public message!: string;
  @Input() public btnOkText!: string;
  @Input() public btnCancelText!: string;

  public constructor(private dialog: MatDialogRef<ConfirmDialogComponent>) {}

  public decline(): void {
    this.dialog.close(false);
  }

  public accept(): void {
    this.dialog.close(true);
  }

  public dismiss(): void {
    this.dialog.close(false);
  }
}
