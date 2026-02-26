import { Component } from "@angular/core";
import { MatDialogRef } from "@angular/material/dialog";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Configuration } from "../../utils/configuration";

@Component({
  selector: "app-switch-user-dialog",
  templateUrl: "./switch-user-dialog.component.html",
  standalone: false,
})
export class SwitchUserDialogComponent {
  wbi = "";
  loading = false;
  errorMessage = "";

  private readonly apiUrl: string;

  constructor(
    private dialogRef: MatDialogRef<SwitchUserDialogComponent>,
    private http: HttpClient,
    configuration: Configuration
  ) {
    this.apiUrl = configuration.ServerWithApiUrl + "users/switch";
  }

  onWbiChange(): void {
    this.errorMessage = "";
  }

  confirm(): void {
    const trimmed = this.wbi.trim();
    if (!trimmed) return;
    this.loading = true;
    this.errorMessage = "";

    const params = new HttpParams().set("wbi", trimmed);
    this.http
      .post(this.apiUrl, null, { params, responseType: "text" })
      .subscribe({
        next: () => {
          this.loading = false;
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage =
            err.status === 404
              ? `User '${trimmed}' not found in user management`
              : "An error occurred. Please try again.";
        },
      });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
