import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { concatLatestFrom } from "@ngrx/operators";
import * as UserRoleActions from "./user-role.actions";
import * as CurrentUserActions from "../current-user/current-user.actions";
import { catchError, filter, map, mergeMap, of, switchMap } from "rxjs";
import { UserRoleService } from "app/services/user-role.service";
import { updateData } from "app/state/app.actions";
import { UpdateType } from "app/models/update";
import { Store } from "@ngrx/store";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Person } from "app/models/person";

@Injectable()
export class UserRoleEffects {
  public loadUserRoles$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(CurrentUserActions.loadCurrentUserSuccess),
      map(({ user }) => UserRoleActions.loadUserRoles({ user }))
    );
  });

  public refreshUserRoles$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(UserRoleActions.loadUserRoles),
      switchMap(({ user }) =>
        this.userRoleService.getUserRoles(user).pipe(
          map((roles) => UserRoleActions.loadUserRolesSuccess({ roles })),
          catchError(() => of(UserRoleActions.loadUserRolesFailure()))
        )
      )
    );
  });

  public updateData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(updateData),
      filter(
        ({ update }) => update.updatedType === UpdateType.CONFIG_VALUE_ITEM
      ),
      concatLatestFrom(() =>
        this.store.select(CurrentUserSelectors.selectCurrentUser)
      ),
      mergeMap(([_action, user]) =>
        of(UserRoleActions.loadUserRoles({ user: user ?? new Person() }))
      )
    );
  });

  public constructor(
    private actions$: Actions,
    private userRoleService: UserRoleService,
    private store: Store
  ) {}
}
