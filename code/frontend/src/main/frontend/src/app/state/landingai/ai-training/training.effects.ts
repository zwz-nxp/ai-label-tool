/**
 * Training Effects
 *
 * Handles async operations for the training state management using NgRx Effects.
 *
 * Requirement 25.3:
 * - THE System SHALL define effects to handle async operations like API calls
 *
 * Requirement 24.6:
 * - THE System SHALL handle HTTP errors and transform them into user-friendly error messages
 */

import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { Store } from "@ngrx/store";
import { Router } from "@angular/router";
import { catchError, map, of, switchMap, tap, withLatestFrom } from "rxjs";

import * as TrainingActions from "app/state/landingai/ai-training/training.actions";
import {
  selectProjectId,
  selectSelectedSnapshotId,
} from "app/state/landingai/ai-training/training.selectors";

// Services
import {
  TrainingService,
  SplitService,
  ErrorHandlerService,
} from "app/state/landingai/ai-training";

/**
 * Training Effects class
 * Handles all async operations for the training module
 */
@Injectable()
export class TrainingEffects {
  // ============================================================================
  // Snapshot Effects
  // ============================================================================

  /**
   * Load snapshots when initializeTraining or loadSnapshots action is dispatched
   *
   * Requirement 1.2: WHEN a user clicks the data version dropdown,
   * THE System SHALL display all available snapshots from the backend
   */
  public loadSnapshots$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.initializeTraining, TrainingActions.loadSnapshots),
      switchMap(({ projectId }) =>
        this.trainingService.getSnapshots(projectId).pipe(
          map((snapshots) =>
            TrainingActions.loadSnapshotsSuccess({ snapshots })
          ),
          catchError((error) => {
            const errorMessage = this.errorHandler.getErrorMessage(error);
            return of(
              TrainingActions.loadSnapshotsFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  /**
   * Show error notification for load snapshots failure
   */
  public loadSnapshotsFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(TrainingActions.loadSnapshotsFailure),
        tap(({ error }) => {
          this.errorHandler.showError(error);
        })
      );
    },
    { dispatch: false }
  );

  // ============================================================================
  // Split Preview Effects
  // ============================================================================

  /**
   * Load split preview when loadSplitPreview action is dispatched
   *
   * Requirement 4.6: WHEN split data changes, THE System SHALL automatically update the preview visualization
   */
  public loadSplitPreview$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.loadSplitPreview),
      switchMap(({ projectId, snapshotId }) =>
        this.splitService.getSplitPreview(projectId, snapshotId).pipe(
          map((splitPreview) =>
            TrainingActions.loadSplitPreviewSuccess({ splitPreview })
          ),
          catchError((error) => {
            const errorMessage = this.errorHandler.getErrorMessage(error);
            return of(
              TrainingActions.loadSplitPreviewFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  /**
   * Load split preview after initialization
   * Triggers after snapshots are loaded successfully
   */
  public loadSplitPreviewAfterInit$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.loadSnapshotsSuccess),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectSelectedSnapshotId)
      ),
      switchMap(([_, projectId, snapshotId]) => {
        if (projectId === null) {
          return of(
            TrainingActions.loadSplitPreviewFailure({
              error: "No project selected",
            })
          );
        }
        return of(
          TrainingActions.loadSplitPreview({
            projectId,
            snapshotId: snapshotId ?? undefined,
          })
        );
      })
    );
  });

  /**
   * Show error notification for load split preview failure
   */
  public loadSplitPreviewFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(TrainingActions.loadSplitPreviewFailure),
        tap(({ error }) => {
          this.errorHandler.showError(error);
        })
      );
    },
    { dispatch: false }
  );

  // ============================================================================
  // Snapshot Selection Effect
  // ============================================================================

  /**
   * Reload split preview when snapshot is selected
   *
   * Requirement 1.3: WHEN a user selects a different snapshot,
   * THE System SHALL update the split preview data accordingly
   */
  public selectSnapshot$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.selectSnapshot),
      withLatestFrom(this.store.select(selectProjectId)),
      switchMap(([{ snapshotId }, projectId]) => {
        if (projectId === null) {
          return of(
            TrainingActions.loadSplitPreviewFailure({
              error: "No project selected",
            })
          );
        }
        return of(
          TrainingActions.loadSplitPreview({
            projectId,
            snapshotId: snapshotId ?? undefined,
          })
        );
      })
    );
  });

  // ============================================================================
  // Split Assignment Effects
  // ============================================================================

  /**
   * Assign split when assignSplit action is dispatched, then reload split preview
   *
   * Requirement 2.3: WHEN a user clicks the Assign split button,
   * THE System SHALL automatically assign unassigned images based on the target distribution
   *
   * Requirement 2.5: WHEN the assignment completes,
   * THE System SHALL refresh the split preview to reflect the changes
   */
  public assignSplit$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.assignSplit),
      switchMap(({ projectId, distribution }) =>
        this.splitService.assignSplit(projectId, distribution).pipe(
          map(() => TrainingActions.assignSplitSuccess()),
          catchError((error) => {
            const errorMessage = this.errorHandler.getErrorMessage(error);
            return of(
              TrainingActions.assignSplitFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  /**
   * Reload split preview after successful split assignment
   */
  public assignSplitSuccess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.assignSplitSuccess),
      withLatestFrom(
        this.store.select(selectProjectId),
        this.store.select(selectSelectedSnapshotId)
      ),
      switchMap(([_, projectId, snapshotId]) => {
        if (projectId === null) {
          return of(
            TrainingActions.loadSplitPreviewFailure({
              error: "No project selected",
            })
          );
        }
        // Show success message
        this.errorHandler.showSuccess(
          "Split assignment completed successfully"
        );
        // Reload split preview
        return of(
          TrainingActions.loadSplitPreview({
            projectId,
            snapshotId: snapshotId ?? undefined,
          })
        );
      })
    );
  });

  /**
   * Show error notification for assign split failure
   */
  public assignSplitFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(TrainingActions.assignSplitFailure),
        tap(({ error }) => {
          this.errorHandler.showError(error);
        })
      );
    },
    { dispatch: false }
  );

  // ============================================================================
  // Training Effects
  // ============================================================================

  /**
   * Start training when startTraining action is dispatched
   *
   * Requirement 21.3: WHEN validation passes,
   * THE System SHALL send a training request to the backend API
   */
  public startTraining$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.startTraining),
      switchMap(({ request }) =>
        this.trainingService.startTraining(request).pipe(
          map((response) => {
            // Extract training ID from the response (single record)
            const trainingIds = [response.id];
            return TrainingActions.startTrainingSuccess({ trainingIds });
          }),
          catchError((error) => {
            const errorMessage = this.errorHandler.getErrorMessage(error);
            return of(
              TrainingActions.startTrainingFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  /**
   * Handle successful training start
   *
   * Requirement 21.6: WHEN training starts successfully,
   * THE System SHALL display a success message and navigate to the project page
   */
  public startTrainingSuccess$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(TrainingActions.startTrainingSuccess),
        withLatestFrom(this.store.select(selectProjectId)),
        tap(([{ trainingIds }, projectId]) => {
          const count = trainingIds.length;
          this.errorHandler.showSuccess(
            `Training started successfully for ${count} model${count !== 1 ? "s" : ""}`
          );
          // Navigate to project page
          if (projectId !== null) {
            this.router.navigate(["/landingai/projects", projectId]);
          }
        })
      );
    },
    { dispatch: false }
  );

  /**
   * Show error notification for start training failure
   *
   * Requirement 21.7: IF training fails to start,
   * THEN THE System SHALL display an error message with details
   */
  public startTrainingFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(TrainingActions.startTrainingFailure),
        tap(({ error }) => {
          this.errorHandler.showError(error);
        })
      );
    },
    { dispatch: false }
  );

  // ============================================================================
  // Project Classes Effects
  // ============================================================================

  /**
   * Load project classes when loadProjectClasses action is dispatched
   *
   * Requirement 3.2: WHEN "Per Class" is selected,
   * THE System SHALL display a class selector for choosing specific classes
   */
  public loadProjectClasses$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.loadProjectClasses),
      switchMap(({ projectId }) =>
        this.splitService.getProjectClasses(projectId).pipe(
          map((projectClasses) =>
            TrainingActions.loadProjectClassesSuccess({ projectClasses })
          ),
          catchError((error) => {
            const errorMessage = this.errorHandler.getErrorMessage(error);
            return of(
              TrainingActions.loadProjectClassesFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  /**
   * Load project classes after initialization
   * Triggers after snapshots are loaded successfully
   */
  public loadProjectClassesAfterInit$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(TrainingActions.loadSnapshotsSuccess),
      withLatestFrom(this.store.select(selectProjectId)),
      switchMap(([_, projectId]) => {
        if (projectId === null) {
          return of(
            TrainingActions.loadProjectClassesFailure({
              error: "No project selected",
            })
          );
        }
        return of(TrainingActions.loadProjectClasses({ projectId }));
      })
    );
  });

  /**
   * Show error notification for load project classes failure
   */
  public loadProjectClassesFailure$ = createEffect(
    () => {
      return this.actions$.pipe(
        ofType(TrainingActions.loadProjectClassesFailure),
        tap(({ error }) => {
          this.errorHandler.showError(error);
        })
      );
    },
    { dispatch: false }
  );

  // ============================================================================
  // Constructor
  // ============================================================================

  public constructor(
    private actions$: Actions,
    private store: Store,
    private trainingService: TrainingService,
    private splitService: SplitService,
    private errorHandler: ErrorHandlerService,
    private router: Router
  ) {}
}
