import { Component, OnDestroy, OnInit } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { MatDialog } from "@angular/material/dialog";
import { Location } from "app/models/location";
import { ModelParam } from "app/models/landingai/model-param.model";
import * as ModelParamActions from "app/state/landingai/model-param/model-param.actions";
import * as ModelParamSelectors from "app/state/landingai/model-param/model-param.selectors";
import * as LocationSelectors from "app/state/location/location.selectors";
import { ModelParamFormDialogComponent } from "../model-param-form-dialog/model-param-form-dialog.component";
import { ModelParamDetailDialogComponent } from "../model-param-detail-dialog/model-param-detail-dialog.component";
import { ModelParamDeleteDialogComponent } from "../model-param-delete-dialog/model-param-delete-dialog.component";

/**
 * Model Parameter List Component
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 4.1, 5.1, 6.1, 9.3, 9.4
 */
@Component({
  selector: "app-model-param-list",
  templateUrl: "./model-param-list.component.html",
  styleUrls: ["./model-param-list.component.scss"],
  standalone: false,
})
export class ModelParamListComponent implements OnInit, OnDestroy {
  // Observables from store
  modelParams$: Observable<ModelParam[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  currentLocation$: Observable<Location | null>;
  hasActiveFilters$: Observable<boolean>;

  // Local state
  currentLocation: Location | null = null;
  modelTypeFilter: string | null = null;
  searchTerm = "";
  hasActiveFilters = false;

  // Table configuration
  displayedColumns: string[] = [
    "modelName",
    "location",
    "modelType",
    "createdAt",
    "createdBy",
    "actions",
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private dialog: MatDialog
  ) {
    // Select observables from store
    this.modelParams$ = this.store.select(
      ModelParamSelectors.selectFilteredModelParams
    );
    this.loading$ = this.store.select(ModelParamSelectors.selectLoading);
    this.error$ = this.store.select(ModelParamSelectors.selectError);
    this.currentLocation$ = this.store.select(
      LocationSelectors.selectCurrentLocation
    );
    this.hasActiveFilters$ = this.store.select(
      ModelParamSelectors.selectHasActiveFilters
    );
  }

  ngOnInit(): void {
    // Subscribe to current location changes
    this.currentLocation$
      .pipe(takeUntil(this.destroy$))
      .subscribe((location) => {
        this.currentLocation = location;
        if (location && location.id) {
          // Load model parameters when location changes
          this.store.dispatch(
            ModelParamActions.loadModelParams({ locationId: location.id })
          );
        }
      });

    // Subscribe to active filters state
    this.hasActiveFilters$
      .pipe(takeUntil(this.destroy$))
      .subscribe((hasFilters) => {
        this.hasActiveFilters = hasFilters;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle model type filter change
   * Requirements: 2.3
   */
  onModelTypeChange(modelType: string | null): void {
    this.store.dispatch(
      ModelParamActions.filterByModelType({
        modelType: modelType as any,
      })
    );
  }

  /**
   * Handle search term change
   * Requirements: 2.4
   */
  onSearchChange(searchTerm: string): void {
    this.store.dispatch(ModelParamActions.searchByModelName({ searchTerm }));
  }

  /**
   * Clear all filters
   */
  onClearFilters(): void {
    this.modelTypeFilter = null;
    this.searchTerm = "";
    this.store.dispatch(ModelParamActions.clearFilters());
  }

  /**
   * Handle create button click
   * Requirements: 3.1
   */
  onCreate(): void {
    const dialogRef = this.dialog.open(ModelParamFormDialogComponent, {
      width: "600px",
      data: {
        locationId: this.currentLocation?.id,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && this.currentLocation?.id) {
        this.store.dispatch(
          ModelParamActions.createModelParam({
            request: {
              modelName: result.modelName,
              modelType: result.modelType,
              parameters: result.parameters,
            },
            locationId: this.currentLocation.id,
            userId: "current-user", // TODO: Get from user state
          })
        );
      }
    });
  }

  /**
   * Handle view button click
   * Requirements: 6.1
   */
  onView(modelParam: ModelParam): void {
    this.dialog.open(ModelParamDetailDialogComponent, {
      width: "600px",
      data: {
        modelParam: modelParam,
      },
    });
  }

  /**
   * Handle edit button click
   * Requirements: 4.1
   */
  onEdit(modelParam: ModelParam): void {
    const dialogRef = this.dialog.open(ModelParamFormDialogComponent, {
      width: "600px",
      data: {
        modelParam: modelParam,
        locationId: this.currentLocation?.id,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.id) {
        this.store.dispatch(
          ModelParamActions.updateModelParam({
            id: result.id,
            request: {
              modelName: result.modelName,
              modelType: result.modelType,
              parameters: result.parameters,
            },
            userId: "current-user", // TODO: Get from user state
          })
        );
      }
    });
  }

  /**
   * Handle delete button click
   * Requirements: 5.1
   */
  onDelete(modelParam: ModelParam): void {
    const dialogRef = this.dialog.open(ModelParamDeleteDialogComponent, {
      width: "500px",
      data: {
        modelParam: modelParam,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === true) {
        this.store.dispatch(
          ModelParamActions.deleteModelParam({
            id: modelParam.id,
            userId: "current-user", // TODO: Get from user state
          })
        );
      }
    });
  }

  /**
   * Handle retry button click
   * Requirements: 9.4
   */
  onRetry(): void {
    if (this.currentLocation && this.currentLocation.id) {
      this.store.dispatch(
        ModelParamActions.loadModelParams({
          locationId: this.currentLocation.id,
        })
      );
    }
  }
}
