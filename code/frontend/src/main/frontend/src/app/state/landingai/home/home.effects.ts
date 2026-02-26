import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Store } from "@ngrx/store";
import { catchError, map, of, switchMap, tap, withLatestFrom } from "rxjs";
import * as HomeActions from "./home.actions";
import { ProjectService } from "app/services/landingai/project.service";
import { ImageService } from "app/services/landingai/image.service";
import * as LocationSelectors from "app/state/location/location.selectors";
import { SnackbarUtils } from "app/utils/snackbar-utils";

@Injectable()
export class HomeEffects {
  // Load Projects Effect
  public loadProjects$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(HomeActions.loadProjects),
      withLatestFrom(
        this.store.select(LocationSelectors.selectCurrentLocation)
      ),
      switchMap(([{ viewAll }, location]) => {
        if (!location || !location.id) {
          return of(
            HomeActions.loadProjectsFailure({
              error: "No location selected",
            })
          );
        }

        return this.projectService.getProjects(viewAll, location.id).pipe(
          map((projects) => HomeActions.loadProjectsSuccess({ projects })),
          catchError((error) => {
            const errorMessage = error?.message || "Failed to load projects";
            return of(HomeActions.loadProjectsFailure({ error: errorMessage }));
          })
        );
      })
    );
  });

  // Show error notification for load projects failure
  public loadProjectsFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.loadProjectsFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      );
    },
    { dispatch: false }
  );

  // Create Project Effect
  public createProject$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(HomeActions.createProject),
      withLatestFrom(
        this.store.select(LocationSelectors.selectCurrentLocation)
      ),
      switchMap(([{ request }, location]) => {
        if (!location || !location.id) {
          return of(
            HomeActions.createProjectFailure({
              error: "No location selected",
            })
          );
        }

        return this.projectService.createProject(request, location.id).pipe(
          map((project) => HomeActions.createProjectSuccess({ project })),
          catchError((error) => {
            const errorMessage = error?.message || "Failed to create project";
            return of(
              HomeActions.createProjectFailure({ error: errorMessage })
            );
          })
        );
      })
    );
  });

  // Show success notification for create project success
  public createProjectSuccess$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.createProjectSuccess),
        tap(({ project }) => {
          SnackbarUtils.displaySuccessMsg(
            this.snackBar,
            `Project "${project.name}" created successfully`
          );
        })
      );
    },
    { dispatch: false }
  );

  // Show error notification for create project failure
  public createProjectFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.createProjectFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      );
    },
    { dispatch: false }
  );

  // Upload Images Effect
  public uploadImages$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(HomeActions.uploadImages),
      switchMap(({ files, projectId }) =>
        this.imageService.uploadImages(files, projectId).pipe(
          map((results) => HomeActions.uploadImagesSuccess({ results })),
          catchError((error) => {
            const errorMessage = error?.message || "Failed to upload images";
            return of(HomeActions.uploadImagesFailure({ error: errorMessage }));
          })
        )
      )
    );
  });

  // Show success notification for upload images success
  public uploadImagesSuccess$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.uploadImagesSuccess),
        tap(({ results }) => {
          const successCount = results.filter((r) => r.success).length;
          const failureCount = results.filter((r) => !r.success).length;

          if (failureCount === 0) {
            SnackbarUtils.displaySuccessMsg(
              this.snackBar,
              `Successfully uploaded ${successCount} image${successCount !== 1 ? "s" : ""}`
            );
          } else if (successCount > 0) {
            SnackbarUtils.displayWarningMsg(
              this.snackBar,
              `Uploaded ${successCount} image${successCount !== 1 ? "s" : ""}, ${failureCount} failed`
            );
          } else {
            SnackbarUtils.displayErrorMsg(
              this.snackBar,
              `Failed to upload all ${failureCount} image${failureCount !== 1 ? "s" : ""}`
            );
          }
        })
      );
    },
    { dispatch: false }
  );

  // Show error notification for upload images failure
  public uploadImagesFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.uploadImagesFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      );
    },
    { dispatch: false }
  );

  // Update Project Effect
  public updateProject$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(HomeActions.updateProject),
      switchMap(({ id, name, modelName, groupName }) =>
        this.projectService.updateProject(id, name, modelName, groupName).pipe(
          map((project) => HomeActions.updateProjectSuccess({ project })),
          catchError((error) => {
            const errorMessage = error?.message || "Failed to update project";
            return of(
              HomeActions.updateProjectFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Show success notification for update project success
  public updateProjectSuccess$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.updateProjectSuccess),
        tap(({ project }) => {
          SnackbarUtils.displaySuccessMsg(
            this.snackBar,
            `Project "${project.name}" updated successfully`
          );
        })
      );
    },
    { dispatch: false }
  );

  // Show error notification for update project failure
  public updateProjectFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.updateProjectFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      );
    },
    { dispatch: false }
  );

  // Delete Project Effect
  public deleteProject$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(HomeActions.deleteProject),
      switchMap(({ id }) =>
        this.projectService.deleteProject(id).pipe(
          map(() => HomeActions.deleteProjectSuccess({ id })),
          catchError((error) => {
            const errorMessage = error?.message || "Failed to delete project";
            return of(
              HomeActions.deleteProjectFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Show success notification for delete project success
  public deleteProjectSuccess$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.deleteProjectSuccess),
        tap(() => {
          SnackbarUtils.displaySuccessMsg(
            this.snackBar,
            "Project deleted successfully"
          );
        })
      );
    },
    { dispatch: false }
  );

  // Show error notification for delete project failure
  public deleteProjectFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(HomeActions.deleteProjectFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      );
    },
    { dispatch: false }
  );

  public constructor(
    private actions$: Actions,
    private store: Store,
    private projectService: ProjectService,
    private imageService: ImageService,
    private snackBar: MatSnackBar
  ) {}
}
