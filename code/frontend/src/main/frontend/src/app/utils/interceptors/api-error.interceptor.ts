import { catchError, Observable, throwError } from "rxjs";
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SnackbarUtils } from "app/utils/snackbar-utils";

@Injectable()
export class ApiErrorInterceptor implements HttpInterceptor {
  public constructor(private snackBar: MatSnackBar) {}

  public intercept(
    req: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse | Error) => {
        if (err instanceof HttpErrorResponse && err.status === 0) {
          SnackbarUtils.displayErrorMsg(
            this.snackBar,
            "There seems to be a connection issue, please check your network and refresh this page."
          );
        }

        return throwError(() => err);
      })
    );
  }
}
