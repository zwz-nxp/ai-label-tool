import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { Store } from "@ngrx/store";
import { catchError, map, of, switchMap, tap, withLatestFrom } from "rxjs";
import * as ModelActions from "./model.actions";
import * as ModelSelectors from "./model.selectors";
import { ModelsService } from "app/services/landingai/models.service";

@Injectable()
export class ModelEffects {
  // Load Models Effect
  public loadModels$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.loadModels),
      switchMap(() =>
        this.modelsService.getModels().pipe(
          map((models) => {
            // Reverse array order to display data from oldest to newest
            const sortedModels = [...models].reverse();
            return ModelActions.loadModelsSuccess({ models: sortedModels });
          }),
          catchError((error) => {
            const errorMessage =
              error?.message ||
              "Failed to load model data, please try again later.";
            console.error("Failed to load model data:", error);
            return of(ModelActions.loadModelsFailure({ error: errorMessage }));
          })
        )
      )
    );
  });

  // Load Models by Project Effect
  public loadModelsByProject$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.loadModelsByProject),
      switchMap(({ projectId }) =>
        this.modelsService.getModelsByProject(projectId).pipe(
          map((models) => {
            // Reverse array order to display data from oldest to newest
            const sortedModels = [...models].reverse();
            return ModelActions.loadModelsByProjectSuccess({
              models: sortedModels,
            });
          }),
          catchError((error) => {
            const errorMessage =
              error?.message ||
              "Failed to load model data for this project, please try again later.";
            console.error("Failed to load models by project:", error);
            return of(
              ModelActions.loadModelsByProjectFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Search Models Effect
  public searchModels$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.searchModels),
      switchMap(({ filters }) =>
        this.modelsService.searchModels(filters).pipe(
          map((models) => {
            // Reverse array order to display data from oldest to newest
            const sortedModels = [...models].reverse();
            return ModelActions.searchModelsSuccess({ models: sortedModels });
          }),
          catchError((error) => {
            const errorMessage =
              error?.message || "Search failed, please try again later.";
            console.error("Failed to search models:", error);
            return of(
              ModelActions.searchModelsFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Auto-search when search term changes
  public updateSearchTerm$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.updateSearchTerm),
      withLatestFrom(
        this.store.select(ModelSelectors.selectSearchFilters),
        this.store.select(ModelSelectors.selectCurrentProjectId)
      ),
      map(([, filters, projectId]) => {
        const filtersWithProject = {
          ...filters,
          projectId: projectId || undefined,
        };
        return ModelActions.searchModels({ filters: filtersWithProject });
      })
    );
  });

  // Auto-search when favorites filter changes
  public updateFavoritesFilter$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.updateFavoritesFilter),
      withLatestFrom(
        this.store.select(ModelSelectors.selectSearchFilters),
        this.store.select(ModelSelectors.selectCurrentProjectId)
      ),
      map(([, filters, projectId]) => {
        const filtersWithProject = {
          ...filters,
          projectId: projectId || undefined,
        };
        return ModelActions.searchModels({ filters: filtersWithProject });
      })
    );
  });

  // Toggle Favorite Effect
  public toggleFavorite$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.toggleFavorite),
      switchMap(({ modelId }) =>
        this.modelsService.toggleFavorite(modelId).pipe(
          map((model) => ModelActions.toggleFavoriteSuccess({ model })),
          catchError((error) => {
            const errorMessage =
              error?.message ||
              "Failed to update favorite status, please try again later.";
            console.error("Failed to toggle favorite status:", error);
            return of(
              ModelActions.toggleFavoriteFailure({
                modelId,
                error: errorMessage,
              })
            );
          })
        )
      )
    );
  });

  // Re-search after successful favorite toggle if showing favorites only
  public toggleFavoriteSuccess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelActions.toggleFavoriteSuccess),
      withLatestFrom(this.store.select(ModelSelectors.selectSearchFilters)),
      switchMap(([, filters]) => {
        // If currently showing only favorite models, need to re-search
        if (filters.showFavoritesOnly) {
          return of(ModelActions.searchModels({ filters }));
        }
        return of({ type: "NO_ACTION" });
      })
    );
  });

  public constructor(
    private actions$: Actions,
    private store: Store,
    private modelsService: ModelsService
  ) {}
}
