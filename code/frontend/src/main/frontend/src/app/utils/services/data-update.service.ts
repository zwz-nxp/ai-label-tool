import { EventEmitter, Injectable, Output } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { GenericSearchArguments } from "app/models/generic-search";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { Update, UpdateType } from "app/models/update";
import { RxStompService } from "app/utils/services/rx-stomp.service";
import { Store } from "@ngrx/store";
import { updateData } from "app/state";

@Injectable({
  providedIn: "root",
})
export class DataUpdateService {
  @Output() public updateEmitter = new EventEmitter<Update>();
  @Output() public resetEmitter = new EventEmitter<GenericSearchArguments>();

  public constructor(
    private rxStompService: RxStompService,
    public snackbar: MatSnackBar,
    private store: Store
  ) {
    this.rxStompService.watch("/topic/update").subscribe((message) => {
      const update: Update = JSON.parse(message.body);
      this.dataUpdateNotification(update);

      this.showIemdmAlertIfNeeded(update);
    });
  }

  public dataUpdateNotification(update: Update): void {
    this.store.dispatch(updateData({ update }));
    this.updateEmitter.emit(update);
  }

  public triggerReset(searchArgs: GenericSearchArguments): void {
    this.resetEmitter.emit(searchArgs);
  }

  private showIemdmAlertIfNeeded(update: Update): void {
    if (UpdateType.IEMDM_ALERT === update.updatedType) {
      SnackbarUtils.displayLongWarningMsg(this.snackbar, update.updateData);
    }
  }
}
