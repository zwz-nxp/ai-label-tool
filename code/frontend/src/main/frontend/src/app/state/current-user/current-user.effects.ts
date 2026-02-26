import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import * as CurrentUserActions from "./current-user.actions";
import { catchError, filter, map, of, switchMap } from "rxjs";
import { UserService } from "app/services/user.service";
import { DEFAULT_DEBOUNCE_TIME } from "app/state/current-user/current-user.reducer";
import { initializeApp, updateData } from "app/state/app.actions";
import { UpdateType } from "app/models/update";

@Injectable()
export class CurrentUserEffects {
  public loadCurrentUser$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(initializeApp),
      map(() => CurrentUserActions.loadCurrentUser())
    );
  });

  public refreshCurrentUser$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(CurrentUserActions.loadCurrentUser),
      switchMap(() =>
        this.userService.getCurrentUser().pipe(
          map((user) => CurrentUserActions.loadCurrentUserSuccess({ user })),
          catchError(() => of(CurrentUserActions.loadCurrentUserFailure()))
        )
      )
    );
  });

  public loadDebounceTime$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(CurrentUserActions.loadCurrentUserSuccess),
      map(({ user }) => CurrentUserActions.loadDebounceTime({ user }))
    );
  });

  public refreshDebounceTime$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(CurrentUserActions.loadDebounceTime),
      switchMap(({ user }) =>
        this.userService.getUserDebounceTime(user.wbi).pipe(
          map((setting) => {
            return CurrentUserActions.loadDebounceTimeSuccess({
              debounceTime: isNaN(+setting?.value)
                ? DEFAULT_DEBOUNCE_TIME
                : +setting.value,
            });
          }),
          catchError(() => of(CurrentUserActions.loadDebounceTimeFailure()))
        )
      )
    );
  });

  public updateData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(({ update }) => update.updatedType === UpdateType.PERSON),
      map(() => CurrentUserActions.loadCurrentUser())
    );
  });

  public constructor(
    private actions$: Actions,
    private userService: UserService
  ) {}
}
