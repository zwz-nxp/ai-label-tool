import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { Store } from "@ngrx/store";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Router } from "@angular/router";
import { of } from "rxjs";
import {
  catchError,
  debounceTime,
  map,
  switchMap,
  tap,
  withLatestFrom,
} from "rxjs/operators";
import { SnapshotService } from "app/services/landingai/snapshot.service";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import * as SnapshotListActions from "./snapshot-list.actions";
import {
  selectFilters,
  selectPageSize,
  selectProjectId,
  selectSelectedSnapshotId,
  selectSnapshotListState,
  selectSortCriteria,
  selectSnapshots,
} from "./snapshot-list.selectors";

/**
 * NgRx Effects for Snapshot List View
 * Requirements: 2.1, 2.3, 3.1, 3.2, 4.3, 5.3, 6.2, 7.3
 */
@Injectable()
export class SnapshotListEffects {
  /**
   * Effect to load snapshots for a project
   * Requirements: 2.1
   */
  loadSnapshots$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.loadSnapshots),
      switchMap(({ projectId }) =>
        this.snapshotService.getProjectSnapshots(projectId).pipe(
          map((snapshots) =>
            SnapshotListActions.loadSnapshotsSuccess({ snapshots })
          ),
          catchError((error) => {
            console.error("Failed to load snapshots:", error);
            const errorMessage =
              error.error?.message ||
              error.message ||
              "Failed to load snapshots. Please try again.";
            return of(
              SnapshotListActions.loadSnapshotsFailure({
                error: errorMessage,
              })
            );
          })
        )
      )
    )
  );

  /**
   * Effect to show error notification when loading snapshots fails
   */
  loadSnapshotsFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.loadSnapshotsFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to handle snapshot selection
   * Requirements: 2.3
   * When a snapshot is selected, load its images
   */
  selectSnapshot$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.selectSnapshot),
      withLatestFrom(
        this.store.select(selectPageSize),
        this.store.select(selectFilters),
        this.store.select(selectSortCriteria)
      ),
      map(([{ snapshotId }, size, filters, sortBy]) =>
        SnapshotListActions.loadSnapshotImages({
          snapshotId,
          page: 0, // Reset to first page when selecting new snapshot
          size,
          filters,
          sortBy,
        })
      )
    )
  );

  /**
   * Effect to load snapshot images
   * Requirements: 1.1, 3.1, 3.2
   */
  loadSnapshotImages$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.loadSnapshotImages),
      debounceTime(100), // Debounce to prevent rapid-fire requests
      tap(({ snapshotId, page, size, sortBy, filters }) => {
        console.log("Effect: loadSnapshotImages$ triggered with:", {
          snapshotId,
          page,
          size,
          sortBy,
          filters,
        });
      }),
      switchMap(({ snapshotId, page, size, sortBy, filters }) =>
        this.snapshotService
          .getSnapshotImages(snapshotId, page, size, sortBy, filters)
          .pipe(
            map((response) =>
              SnapshotListActions.loadSnapshotImagesSuccess({
                response: {
                  content: response.content as any[],
                  page: response.page,
                  size: response.size,
                  totalPages: response.totalPages,
                  totalElements: response.totalElements,
                  first: response.first,
                  last: response.last,
                },
              })
            ),
            catchError((error) => {
              console.error("Failed to load snapshot images:", error);
              const errorMessage =
                error.error?.message ||
                error.message ||
                "Failed to load snapshot images. Please try again.";
              return of(
                SnapshotListActions.loadSnapshotImagesFailure({
                  error: errorMessage,
                })
              );
            })
          )
      )
    )
  );

  /**
   * Effect to show error notification when loading images fails
   */
  loadSnapshotImagesFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.loadSnapshotImagesFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to reload images when filters are applied
   * Requirements: 3.1
   */
  applyFilters$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.applyFilters),
      withLatestFrom(
        this.store.select(selectSelectedSnapshotId),
        this.store.select(selectPageSize),
        this.store.select(selectSortCriteria)
      ),
      map(([{ filters }, snapshotId, size, sortBy]) => {
        if (snapshotId) {
          return SnapshotListActions.loadSnapshotImages({
            snapshotId,
            page: 0, // Reset to first page when applying filters
            size,
            filters,
            sortBy,
          });
        }
        return SnapshotListActions.loadSnapshotImagesFailure({
          error: "No snapshot selected",
        });
      })
    )
  );

  /**
   * Effect to reload images when filters are cleared
   */
  clearFilters$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.clearFilters),
      withLatestFrom(
        this.store.select(selectSelectedSnapshotId),
        this.store.select(selectPageSize),
        this.store.select(selectSortCriteria)
      ),
      map(([_, snapshotId, size, sortBy]) => {
        if (snapshotId) {
          return SnapshotListActions.loadSnapshotImages({
            snapshotId,
            page: 0, // Reset to first page when clearing filters
            size,
            filters: {},
            sortBy,
          });
        }
        return SnapshotListActions.loadSnapshotImagesFailure({
          error: "No snapshot selected",
        });
      })
    )
  );

  /**
   * Effect to reload images when sort criteria changes
   * Requirements: 3.2
   */
  applySortCriteria$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.applySortCriteria),
      withLatestFrom(
        this.store.select(selectSelectedSnapshotId),
        this.store.select(selectPageSize),
        this.store.select(selectFilters)
      ),
      map(([{ sortCriteria }, snapshotId, size, filters]) => {
        if (snapshotId) {
          return SnapshotListActions.loadSnapshotImages({
            snapshotId,
            page: 0, // Reset to first page when changing sort
            size,
            filters,
            sortBy: sortCriteria,
          });
        }
        return SnapshotListActions.loadSnapshotImagesFailure({
          error: "No snapshot selected",
        });
      })
    )
  );

  /**
   * Effect to reload images when page changes
   * Requirements: 1.3, 1.4
   */
  changePage$ = createEffect(() =>
    this.actions$.pipe(
      ofType(
        SnapshotListActions.changePage,
        SnapshotListActions.nextPage,
        SnapshotListActions.previousPage
      ),
      withLatestFrom(
        this.store.select(selectSelectedSnapshotId),
        this.store.select(selectSnapshotListState),
        this.store.select(selectPageSize),
        this.store.select(selectFilters),
        this.store.select(selectSortCriteria)
      ),
      map(([_, snapshotId, state, size, filters, sortBy]) => {
        if (snapshotId) {
          return SnapshotListActions.loadSnapshotImages({
            snapshotId,
            page: state.pagination.currentPage,
            size,
            filters,
            sortBy,
          });
        }
        return SnapshotListActions.loadSnapshotImagesFailure({
          error: "No snapshot selected",
        });
      })
    )
  );

  /**
   * Effect to create a new project from a snapshot
   * Requirements: 4.3
   */
  createProjectFromSnapshot$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.createProjectFromSnapshot),
      switchMap(({ snapshotId, projectName }) =>
        this.snapshotService
          .createProjectFromSnapshot(snapshotId, projectName)
          .pipe(
            map((project) =>
              SnapshotListActions.createProjectFromSnapshotSuccess({
                projectId: project.id,
                projectName: project.name,
              })
            ),
            catchError((error) => {
              console.error("Failed to create project from snapshot:", error);
              const errorMessage =
                error.error?.message ||
                error.message ||
                "Failed to create project from snapshot. Please try again.";
              return of(
                SnapshotListActions.createProjectFromSnapshotFailure({
                  error: errorMessage,
                })
              );
            })
          )
      )
    )
  );

  /**
   * Effect to show success notification when project is created
   * Requirements: 4.5
   */
  createProjectFromSnapshotSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.createProjectFromSnapshotSuccess),
        tap(({ projectName, projectId }) => {
          SnackbarUtils.displaySuccessMsg(
            this.snackBar,
            `Project "${projectName}" created successfully`
          );
          // Navigate to the new project
          this.router.navigate(["/landingai/projects", projectId, "images"]);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to show error notification when project creation fails
   */
  createProjectFromSnapshotFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.createProjectFromSnapshotFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to revert a project to a snapshot state
   * Requirements: 5.3
   */
  revertToSnapshot$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.revertToSnapshot),
      switchMap(({ snapshotId, projectId }) =>
        this.snapshotService.revertToSnapshot(snapshotId, projectId).pipe(
          map(() =>
            SnapshotListActions.revertToSnapshotSuccess({
              message: "Project reverted to snapshot successfully",
            })
          ),
          catchError((error) => {
            console.error("Failed to revert to snapshot:", error);
            const errorMessage =
              error.error?.message ||
              error.message ||
              "Failed to revert to snapshot. Your project data has not been changed.";
            return of(
              SnapshotListActions.revertToSnapshotFailure({
                error: errorMessage,
              })
            );
          })
        )
      )
    )
  );

  /**
   * Effect to show success notification and refresh data after revert
   * Requirements: 5.7
   */
  revertToSnapshotSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.revertToSnapshotSuccess),
      withLatestFrom(this.store.select(selectProjectId)),
      tap(([{ message }]) => {
        SnackbarUtils.displaySuccessMsg(this.snackBar, message);
      }),
      map(([_, projectId]) => {
        if (projectId) {
          // Reload snapshots to reflect the new backup snapshot
          return SnapshotListActions.loadSnapshots({ projectId });
        }
        return SnapshotListActions.loadSnapshotsFailure({
          error: "No project ID available",
        });
      })
    )
  );

  /**
   * Effect to show error notification when revert fails
   */
  revertToSnapshotFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.revertToSnapshotFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to download a snapshot dataset
   * Requirements: 6.2
   */
  downloadSnapshot$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.downloadSnapshot),
      withLatestFrom(this.store.select(selectSnapshots)),
      switchMap(([{ snapshotId }, snapshots]) => {
        const snapshot = snapshots.find((s) => s.id === snapshotId);
        const snapshotName = snapshot?.name || `snapshot-${snapshotId}`;

        // Note: Progress dialog should be opened in the component before dispatching this action
        return this.snapshotService.downloadSnapshot(snapshotId).pipe(
          tap((blob) => {
            // Trigger browser download
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = `${snapshotName}.zip`;
            link.click();
            window.URL.revokeObjectURL(url);
          }),
          map(() =>
            SnapshotListActions.downloadSnapshotSuccess({
              message: "Snapshot downloaded successfully",
            })
          ),
          catchError((error) => {
            console.error("Failed to download snapshot:", error);
            const errorMessage =
              error.error?.message ||
              error.message ||
              "Failed to download snapshot. Please try again.";
            return of(
              SnapshotListActions.downloadSnapshotFailure({
                error: errorMessage,
              })
            );
          })
        );
      })
    )
  );

  /**
   * Effect to show success notification when download completes
   */
  downloadSnapshotSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.downloadSnapshotSuccess),
        tap(({ message }) => {
          SnackbarUtils.displaySuccessMsg(this.snackBar, message);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to show error notification when download fails
   */
  downloadSnapshotFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.downloadSnapshotFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  /**
   * Effect to delete a snapshot
   * Requirements: 7.3
   */
  deleteSnapshot$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.deleteSnapshot),
      switchMap(({ snapshotId }) =>
        this.snapshotService.deleteSnapshot(snapshotId).pipe(
          map(() => SnapshotListActions.deleteSnapshotSuccess({ snapshotId })),
          catchError((error) => {
            console.error("Failed to delete snapshot:", error);
            const errorMessage =
              error.error?.message ||
              error.message ||
              "Failed to delete snapshot. Please try again.";
            return of(
              SnapshotListActions.deleteSnapshotFailure({
                error: errorMessage,
              })
            );
          })
        )
      )
    )
  );

  /**
   * Effect to show success notification and refresh snapshot list after deletion
   * Requirements: 7.4, 7.5, 7.6
   */
  deleteSnapshotSuccess$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.deleteSnapshotSuccess),
      withLatestFrom(
        this.store.select(selectSnapshots),
        this.store.select(selectSelectedSnapshotId),
        this.store.select(selectProjectId)
      ),
      tap(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "Snapshot deleted successfully"
        );
      }),
      switchMap(
        ([{ snapshotId }, snapshots, selectedSnapshotId, projectId]) => {
          // Refresh the snapshot list to remove the deleted snapshot from the sidebar
          if (projectId) {
            return of(SnapshotListActions.loadSnapshots({ projectId }));
          }
          return of(SnapshotListActions.clearError());
        }
      )
    )
  );

  /**
   * Effect to handle snapshot selection after deletion and list refresh
   * Requirements: 7.6
   */
  handleSelectionAfterDeletion$ = createEffect(() =>
    this.actions$.pipe(
      ofType(SnapshotListActions.loadSnapshotsSuccess),
      withLatestFrom(
        this.store.select(selectSelectedSnapshotId),
        this.store.select(selectSnapshots)
      ),
      map(([{ snapshots }, selectedSnapshotId, previousSnapshots]) => {
        // Check if we just completed a deletion (snapshot count decreased)
        if (previousSnapshots.length > snapshots.length) {
          // Check if the selected snapshot still exists
          const selectedStillExists = snapshots.some(
            (s) => s.id === selectedSnapshotId
          );

          if (!selectedStillExists && snapshots.length > 0) {
            // Select the most recent remaining snapshot
            return SnapshotListActions.selectSnapshot({
              snapshotId: snapshots[0].id,
            });
          }
        }
        // No action needed
        return SnapshotListActions.clearError();
      })
    )
  );

  /**
   * Effect to show error notification when deletion fails
   */
  deleteSnapshotFailure$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(SnapshotListActions.deleteSnapshotFailure),
        tap(({ error }) => {
          SnackbarUtils.displayErrorMsg(this.snackBar, error);
        })
      ),
    { dispatch: false }
  );

  constructor(
    private actions$: Actions,
    private store: Store,
    private snapshotService: SnapshotService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}
}
