import { MatSnackBar } from "@angular/material/snack-bar";

export class SnackbarUtils {
  private static currentMessage = "";

  /**
   * Displays a server error on a snackbar.
   * @param snackbar Display backend error message
   * @param message
   */
  public static displayServerErrorMsg(
    snackbar: MatSnackBar,
    message: string | object
  ): void {
    if (typeof message === "object") {
      if (Object.hasOwn(message, "error")) {
        SnackbarUtils.displayErrorMsg(
          snackbar,
          (message as any).error.replace(/[\r\n]+$/, "")
        );
      } else if (Object.hasOwn(message, "message")) {
        SnackbarUtils.displayErrorMsg(
          snackbar,
          (message as any).message.replace(/[\r\n]+$/, "")
        );
      } else {
        SnackbarUtils.displayErrorMsg(snackbar, JSON.stringify(message));
      }
    } else {
      SnackbarUtils.displayErrorMsg(snackbar, message.replace(/[\r\n]+$/, ""));
    }
  }

  /**
   * Displays the given error message in a red snackbar.
   */
  public static displayErrorMsg(
    snackbar: MatSnackBar,
    inputMessage: string
  ): void {
    const msg = SnackbarUtils.getMessage(inputMessage);

    if (msg) {
      snackbar
        .open(msg, "Close", {
          duration: 10000,
          verticalPosition: "top",
          panelClass: "snackbar-failure",
        })
        .afterDismissed()
        .subscribe(() => {
          SnackbarUtils.onAfterClosed();
        });
    }
  }

  /**
   * Displays the given success message on a green snackbar..
   */
  public static displaySuccessMsg(
    snackbar: MatSnackBar,
    inputMessage: string
  ): void {
    const msg = SnackbarUtils.getMessage(inputMessage);

    if (msg) {
      snackbar
        .open(msg, "Close", {
          duration: 5000,
          verticalPosition: "top",
          panelClass: "snackbar-success",
        })
        .afterDismissed()
        .subscribe(() => {
          SnackbarUtils.onAfterClosed();
        });
    }
  }

  /**
   * Displays the given success message on a yellow snackbar..
   */
  public static displayWarningMsg(
    snackbar: MatSnackBar,
    inputMessage: string
  ): void {
    const msg = SnackbarUtils.getMessage(inputMessage);

    if (msg) {
      snackbar
        .open(msg, "Close", {
          duration: 7000,
          verticalPosition: "top",
          panelClass: "snackbar-warning",
        })
        .afterDismissed()
        .subscribe(() => {
          SnackbarUtils.onAfterClosed();
        });
    }
  }

  /**
   * Displays the given success message on a yellow snackbar..
   */
  public static displayLongWarningMsg(
    snackbar: MatSnackBar,
    inputMessage: string
  ): void {
    const msg = SnackbarUtils.getMessage(inputMessage);

    if (msg) {
      snackbar
        .open(msg, "Close", {
          duration: 700000,
          verticalPosition: "top",
          panelClass: "snackbar-long-warning",
        })
        .afterDismissed()
        .subscribe(() => {
          SnackbarUtils.onAfterClosed();
        });
    }
  }

  private static getMessage(inputMessage: string): string {
    if (inputMessage === SnackbarUtils.currentMessage) {
      return "";
    } else if (
      SnackbarUtils.currentMessage == null ||
      SnackbarUtils.currentMessage.length == 0
    ) {
      SnackbarUtils.currentMessage = inputMessage == null ? "" : inputMessage;
    } else if (SnackbarUtils.currentMessage.indexOf(inputMessage) < 0) {
      SnackbarUtils.currentMessage += "\n" + inputMessage;
    }

    return this.currentMessage;
  }

  private static onAfterClosed(): void {
    SnackbarUtils.currentMessage = "";
  }
}
