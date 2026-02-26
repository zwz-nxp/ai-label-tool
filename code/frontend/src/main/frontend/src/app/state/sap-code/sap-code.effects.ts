import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import * as SapCodeActions from "./sap-code.actions";
import { catchError, filter, map, of, switchMap } from "rxjs";
import { SapCodeService } from "app/services/sap-code.service";
import { updateData } from "app/state/app.actions";
import { UpdateType } from "app/models/update";

@Injectable()
export class SapCodeEffects {
  public loadSapCodes$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(SapCodeActions.loadSapCodes),
      switchMap(() =>
        this.sapCodeService.getAllSapCodes().pipe(
          map((sapCodes) => SapCodeActions.loadSapCodesSuccess({ sapCodes })),
          catchError(() => of(SapCodeActions.loadSapCodesFailure()))
        )
      )
    );
  });

  public updateData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(({ update }) => update.updatedType === UpdateType.SAPCODE),
      map(() => SapCodeActions.loadSapCodes())
    );
  });

  public constructor(
    private actions$: Actions,
    private sapCodeService: SapCodeService
  ) {}
}
