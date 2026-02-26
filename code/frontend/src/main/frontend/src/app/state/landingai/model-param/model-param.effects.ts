import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { Store } from "@ngrx/store";
import { MatSnackBar } from "@angular/material/snack-bar";
import { of } from "rxjs";
import {
  catchError,
  map,
  switchMap,
  tap,
  withLatestFrom,
} from "rxjs/operators";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { ModelParamHttpService } from "app/services/landingai/model-param-http.service";
import * as ModelParamActions from "./model-param.actions";
import * as LocationActions from "app/state/location/location.actions";
import * as LocationSelectors from "app/state/location/location.selectors";

/**
 * NgRx Effects for Model Parameter Configuration
 * Requirements: 2.1, 2.2, 3.2, 3.6, 4.2, 4.4, 5.2, 5.3, 9.1, 9.2
 */
@Injectable()
export class ModelParamEffects {
  /**
   * Effect to load model parameters for a location
   * Requirements: 2.1
   */
  loadModelParams$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.loadModelParams),
      switchMap(({ locationId }) =>
        this.modelParamHttpService.getModelParams(locationId).pipe(
          map((modelParams) =>
            ModelParamActions.loadModelParamsSuccess({ modelParams })
          ),
          catchError((error) => {
            console.error("Failed to load model parameters:", error);
            const errorMessage =
              error.message ||
              "Failed to load model parameters. Please try again.";
            return of(
              ModelParamActions.loadModelParamsFailure({
                error: errorMessage,
              })
            );
          })
        )
      )
    )
  );

  /**
   * Effect to show error notification when loading fails
   */
  loadModelParamsFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(ModelParamActions.loadModelParamsFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to reload model parameters when location changes
   * Requirements: 2.2, 10.3
   */
  locationChange$ = createEffect(() =>
    this.actions$.pipe(
      ofType(LocationActions.setCurrentLocation),
      withLatestFrom(
        this.store.select(LocationSelectors.selectCurrentLocation)
      ),
      map(([_, location]) => {
        if (location && location.id) {
          return ModelParamActions.loadModelParams({ locationId: location.id });
        }
        return ModelParamActions.loadModelParamsFailure({
          error: "No location selected",
        });
      })
    )
  );

  /**
   * Effect to create a new model parameter
   * Requirements: 3.2
   */
  createModelParam$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.createModelParam),
      switchMap(({ request, locationId, userId }) =>
        this.modelParamHttpService
          .createModelParam(request, locationId, userId)
          .pipe(
            map((modelParam) =>
              ModelParamActions.createModelParamSuccess({ modelParam })
            ),
            catchError((error) => {
              console.error("Failed to create model parameter:", error);
              const errorMessage =
                error.message ||
                "Failed to create model parameter. Please try again.";
              return of(
                ModelParamActions.createModelParamFailure({
                  error: errorMessage,
                })
              );
            })
          )
      )
    )
  );

  /**
   * Effect to show success notification and reload list after creation
   * Requirements: 3.6, 9.1
   */
  createModelParamSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.createModelParamSuccess),
      withLatestFrom(
        this.store.select(LocationSelectors.selectCurrentLocation)
      ),
      tap(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "Model parameter created successfully"
        );
      }),
      map(([{ modelParam }, location]) => {
        if (location && location.id) {
          return ModelParamActions.loadModelParams({ locationId: location.id });
        }
        return ModelParamActions.clearError();
      })
    )
  );

  /**
   * Effect to show error notification when creation fails
   * Requirements: 9.2
   */
  createModelParamFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(ModelParamActions.createModelParamFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to update an existing model parameter
   * Requirements: 4.2
   */
  updateModelParam$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.updateModelParam),
      switchMap(({ id, request, userId }) =>
        this.modelParamHttpService.updateModelParam(id, request, userId).pipe(
          map((modelParam) =>
            ModelParamActions.updateModelParamSuccess({ modelParam })
          ),
          catchError((error) => {
            console.error("Failed to update model parameter:", error);
            const errorMessage =
              error.message ||
              "Failed to update model parameter. Please try again.";
            return of(
              ModelParamActions.updateModelParamFailure({
                error: errorMessage,
              })
            );
          })
        )
      )
    )
  );

  /**
   * Effect to show success notification and reload list after update
   * Requirements: 4.4, 9.1
   */
  updateModelParamSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.updateModelParamSuccess),
      withLatestFrom(
        this.store.select(LocationSelectors.selectCurrentLocation)
      ),
      tap(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "Model parameter updated successfully"
        );
      }),
      map(([{ modelParam }, location]) => {
        if (location && location.id) {
          return ModelParamActions.loadModelParams({ locationId: location.id });
        }
        return ModelParamActions.clearError();
      })
    )
  );

  /**
   * Effect to show error notification when update fails
   * Requirements: 9.2
   */
  updateModelParamFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(ModelParamActions.updateModelParamFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to delete a model parameter
   * Requirements: 5.2
   */
  deleteModelParam$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.deleteModelParam),
      switchMap(({ id, userId }) =>
        this.modelParamHttpService.deleteModelParam(id, userId).pipe(
          map(() => ModelParamActions.deleteModelParamSuccess({ id })),
          catchError((error) => {
            console.error("Failed to delete model parameter:", error);
            const errorMessage =
              error.message ||
              "Failed to delete model parameter. Please try again.";
            return of(
              ModelParamActions.deleteModelParamFailure({
                error: errorMessage,
              })
            );
          })
        )
      )
    )
  );

  /**
   * Effect to show success notification and reload list after deletion
   * Requirements: 5.3, 9.1
   */
  deleteModelParamSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ModelParamActions.deleteModelParamSuccess),
      withLatestFrom(
        this.store.select(LocationSelectors.selectCurrentLocation)
      ),
      tap(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "Model parameter deleted successfully"
        );
      }),
      map(([{ id }, location]) => {
        if (location && location.id) {
          return ModelParamActions.loadModelParams({ locationId: location.id });
        }
        return ModelParamActions.clearError();
      })
    )
  );

  /**
   * Effect to show error notification when deletion fails
   * Requirements: 9.2
   */
  deleteModelParamFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(ModelParamActions.deleteModelParamFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  constructor(
    private actions$: Actions,
    private store: Store,
    private snackBar: MatSnackBar,
    private modelParamHttpService: ModelParamHttpService
  ) {}
}
