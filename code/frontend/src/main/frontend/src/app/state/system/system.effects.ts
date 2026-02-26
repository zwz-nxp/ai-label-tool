import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import * as SystemActions from "./system.actions";
import { catchError, filter, map, of, switchMap } from "rxjs";
import { SystemService } from "app/services/system.service";
import { initializeApp, updateData } from "app/state/app.actions";
import { UpdateType } from "app/models/update";

@Injectable()
export class SystemEffects {
  public loadConfigurationItems$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(initializeApp),
      map(SystemActions.loadConfigurationItems)
    );
  });

  public refreshConfigurationItems$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(SystemActions.loadConfigurationItems),
      switchMap(() =>
        this.systemService.getAllConfigurationItems().pipe(
          map((configurationItems) =>
            SystemActions.loadConfigurationItemsSuccess({
              configurationItems,
            })
          ),
          catchError(() => of(SystemActions.loadConfigurationItemsFailure()))
        )
      )
    );
  });

  public loadGlobalLookupData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(initializeApp),
      map(SystemActions.loadGlobalLookupData)
    );
  });

  public refreshGlobalLookupData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(SystemActions.loadGlobalLookupData),
      switchMap(() =>
        this.systemService.getGlobalLookupData().pipe(
          map((globalLookupData) =>
            SystemActions.loadGlobalLookupDataSuccess({
              globalLookupData,
            })
          ),
          catchError(() => of(SystemActions.loadGlobalLookupDataFailure()))
        )
      )
    );
  });

  public updateDataConfigurationItems$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(
        ({ update }) => update.updatedType === UpdateType.CONFIG_VALUE_ITEM
      ),
      map(SystemActions.loadConfigurationItems)
    );
  });

  public updateDataUser$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(({ update }) => update.updatedType === UpdateType.PERSON),
      map(() => SystemActions.loadGlobalLookupData())
    );
  });

  public updateDataEquipmentCode$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(({ update }) => update.updatedType === UpdateType.EQUIPMENTCODE),
      map(() => SystemActions.loadGlobalLookupData())
    );
  });

  public constructor(
    private actions$: Actions,
    private systemService: SystemService
  ) {}
}
