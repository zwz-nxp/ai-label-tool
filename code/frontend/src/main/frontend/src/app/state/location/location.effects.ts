import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import * as LocationActions from "./location.actions";
import * as CurrentUserActions from "../current-user/current-user.actions";
import { catchError, filter, map, of, switchMap } from "rxjs";
import { LocationService } from "app/services/location.service";
import { initializeApp, updateData } from "app/state";
import { UpdateType } from "app/models/update";

@Injectable()
export class LocationEffects {
  public loadAllLocations$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(initializeApp),
      map(() => LocationActions.loadAllLocations())
    );
  });

  public refreshAllLocations$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(LocationActions.loadAllLocations),
      switchMap(() =>
        this.locationService.getAllLocations().pipe(
          map((allLocations) =>
            LocationActions.loadAllLocationsSuccess({ allLocations })
          ),
          catchError(() => of(LocationActions.loadAllLocationsFailure()))
        )
      )
    );
  });

  public setCurrentLocation$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(CurrentUserActions.loadCurrentUserSuccess),
      map(({ user }) =>
        LocationActions.setCurrentLocation({ location: user.primaryLocation })
      )
    );
  });

  public updateData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(({ update }) => update.updatedType === UpdateType.LOCATION),
      map(() => LocationActions.loadAllLocations())
    );
  });

  public constructor(
    private actions$: Actions,
    private locationService: LocationService
  ) {}
}
