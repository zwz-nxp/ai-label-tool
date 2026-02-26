import { Component } from "@angular/core";
import { MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: "app-help-screen",
  templateUrl: "./help-screen.component.html",
  standalone: false,
})
export class HelpScreenComponent {
  public constructor(private dialog: MatDialogRef<HelpScreenComponent>) {}

  public close(): void {
    this.dialog.close();
  }
}
