import { Injectable } from "@angular/core";

import { ConfirmDialogComponent } from "app/components/dialogs/confirm-dialog/confirm-dialog.component";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";

@Injectable()
export class ConfirmDialogService {
  public constructor(private dialogService: MatDialog) {}

  public openDialog(
    title: string,
    message: string,
    buttonOk = "Confirm",
    buttonCancel = "Cancel"
  ): MatDialogRef<ConfirmDialogComponent> {
    const dialog = this.dialogService.open(ConfirmDialogComponent, {
      disableClose: true,
    });

    dialog.componentInstance.title = title;
    dialog.componentInstance.message = message;
    dialog.componentInstance.btnOkText = buttonOk;
    dialog.componentInstance.btnCancelText = buttonCancel;

    return dialog;
  }
}
