import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Store } from "@ngrx/store";
import { Actions, ofType } from "@ngrx/effects";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Observable, Subject, take, takeUntil, tap } from "rxjs";
import {
  ProjectListItem,
  ProjectType,
} from "../../../models/landingai/project";
import { ProjectClass } from "../../../models/landingai/project-class.model";
import { ProjectTag } from "../../../models/landingai/project-tag.model";
import { ProjectMetadata } from "../../../models/landingai/project-metadata.model";
import { Image } from "../../../models/landingai/image";
import {
  Snapshot,
  SnapshotService,
} from "../../../services/landingai/snapshot.service";
import { ConfirmDialogService } from "../../../utils/services/confirm-dialog.service";
import * as HomeSelectors from "../../../state/landingai/home/home.selectors";
import * as HomeActions from "../../../state/landingai/home/home.actions";
import * as SnapshotListActions from "../../../state/landingai/snapshot-list/snapshot-list.actions";
import * as SnapshotListSelectors from "../../../state/landingai/snapshot-list/snapshot-list.selectors";
import {
  LoadingState,
  PaginationState,
  SnapshotFilterState,
  SnapshotSortMethod,
} from "../../../state/landingai/snapshot-list/snapshot-list.state";
import { FilterState } from "../../../state/landingai/image-upload/image-upload.actions";
import {
  ProjectNameDialogComponent,
  ProjectNameDialogData,
  ProjectNameDialogResult,
} from "./project-name-dialog/project-name-dialog.component";
import {
  FilterDialogComponent,
  FilterDialogData,
  FilterDialogResult,
} from "../image-upload/filter-dialog/filter-dialog.component";
import { DownloadProgressDialogComponent } from "../image-upload/download-progress-dialog/download-progress-dialog.component";

/**
 * Manage action types for snapshot operations
 * Requirements: 4.1, 5.1, 6.1, 7.1
 */
export type ManageActionType =
  | "create_project"
  | "revert"
  | "download"
  | "delete";

/**
 * SnapshotListViewComponent - Main container for snapshot list view
 * Requirements: 1.1, 2.1, 2.3, 3.1, 3.2
 *
 * This component orchestrates the snapshot list view, connecting to NgRx store
 * for state management and coordinating between child components.
 */
@Component({
  selector: "app-snapshot-list-view",
  standalone: false,
  templateUrl: "./snapshot-list-view.component.html",
  styleUrls: ["./snapshot-list-view.component.scss"],
})
export class SnapshotListViewComponent implements OnInit, OnDestroy {
  // Route parameters
  projectId!: number;
  project$!: Observable<ProjectListItem | undefined>;

  // Snapshot state observables from NgRx store
  snapshots$: Observable<Snapshot[]>;
  selectedSnapshotId$: Observable<number | null>;
  selectedSnapshot$: Observable<Snapshot | null>;
  images$: Observable<Image[]>;
  pagination$: Observable<PaginationState>;
  filters$: Observable<SnapshotFilterState>;
  sortCriteria$: Observable<SnapshotSortMethod>;
  loading$: Observable<LoadingState>;
  sidebarCollapsed$: Observable<boolean>;
  error$: Observable<string | null>;

  // Derived observables for UI
  imagesLoading$: Observable<boolean>;
  snapshotsLoading$: Observable<boolean>;
  operationLoading$: Observable<boolean>;
  currentPage$: Observable<number>;
  totalPages$: Observable<number>;
  totalElements$: Observable<number>;

  // Filter options for filter panel
  availableClasses: ProjectClass[] = [];
  availableTags: ProjectTag[] = [];
  availableMetadata: ProjectMetadata[] = [];

  // UI state
  showFilterPanel: boolean = false;

  // Store query params from navigation for return journey
  private returnQueryParams: any = {};

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private store: Store,
    private actions$: Actions,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private confirmDialogService: ConfirmDialogService,
    private snapshotService: SnapshotService
  ) {
    // Initialize observables from snapshot list state
    this.snapshots$ = this.store.select(SnapshotListSelectors.selectSnapshots);
    this.selectedSnapshotId$ = this.store.select(
      SnapshotListSelectors.selectSelectedSnapshotId
    );
    this.selectedSnapshot$ = this.store.select(
      SnapshotListSelectors.selectSelectedSnapshot
    );
    this.images$ = this.store.select(SnapshotListSelectors.selectImages);
    this.pagination$ = this.store.select(
      SnapshotListSelectors.selectPagination
    );
    this.filters$ = this.store.select(SnapshotListSelectors.selectFilters);
    this.sortCriteria$ = this.store.select(
      SnapshotListSelectors.selectSortCriteria
    );
    this.loading$ = this.store.select(SnapshotListSelectors.selectLoading);
    this.sidebarCollapsed$ = this.store.select(
      SnapshotListSelectors.selectSidebarCollapsed
    );
    this.error$ = this.store.select(SnapshotListSelectors.selectError);

    // Derived loading observables
    this.imagesLoading$ = this.store.select(
      SnapshotListSelectors.selectImagesLoading
    );
    this.snapshotsLoading$ = this.store.select(
      SnapshotListSelectors.selectSnapshotsLoading
    );
    this.operationLoading$ = this.store.select(
      SnapshotListSelectors.selectOperationLoading
    );

    // Pagination observables (convert to 1-indexed for UI display)
    this.currentPage$ = this.store.select(
      SnapshotListSelectors.selectCurrentPageDisplay
    );
    this.totalPages$ = this.store.select(
      SnapshotListSelectors.selectTotalPages
    );
    this.totalElements$ = this.store.select(
      SnapshotListSelectors.selectTotalItems
    );
  }

  ngOnInit(): void {
    // Load projects to ensure project data is available
    this.store.dispatch(HomeActions.loadProjects({ viewAll: true }));

    // Get project ID from route parameter and store query params for return navigation
    this.route.params
      .pipe(
        takeUntil(this.destroy$),
        tap((params) => {
          this.projectId = +params["id"];

          // Load project details from home state
          this.project$ = this.store.select(
            HomeSelectors.selectProjectById(this.projectId)
          );

          // Load snapshots for this project
          this.store.dispatch(
            SnapshotListActions.loadSnapshots({ projectId: this.projectId })
          );
        })
      )
      .subscribe();

    // Store query params for return navigation (preserves modelId, annotationType, etc.)
    this.route.queryParams
      .pipe(takeUntil(this.destroy$), take(1))
      .subscribe((queryParams) => {
        this.returnQueryParams = { ...queryParams };
        console.log(
          "Stored query params for return navigation:",
          this.returnQueryParams
        );
      });

    // Auto-select first snapshot when snapshots are loaded
    this.snapshots$
      .pipe(
        takeUntil(this.destroy$),
        tap((snapshots) => {
          if (snapshots.length > 0) {
            this.selectedSnapshotId$.pipe(take(1)).subscribe((selectedId) => {
              // Only auto-select if no snapshot is currently selected
              if (selectedId === null) {
                this.store.dispatch(
                  SnapshotListActions.selectSnapshot({
                    snapshotId: snapshots[0].id,
                  })
                );
              }
            });
          }
        })
      )
      .subscribe();

    // Reload filter options when snapshot selection changes
    this.selectedSnapshotId$
      .pipe(
        takeUntil(this.destroy$),
        tap((snapshotId) => {
          if (snapshotId) {
            this.loadFilterOptions();
          }
        })
      )
      .subscribe();
  }

  ngOnDestroy(): void {
    // Reset snapshot list state when leaving the page
    this.store.dispatch(SnapshotListActions.resetSnapshotListState());
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle snapshot selection from sidebar
   * Requirements: 2.3
   * @param snapshotId The ID of the selected snapshot
   */
  onSnapshotSelected(snapshotId: number): void {
    this.store.dispatch(SnapshotListActions.selectSnapshot({ snapshotId }));
  }

  /**
   * Handle filter changes from filter panel
   * Requirements: 3.1
   * @param filters The new filter state
   */
  onFilterApplied(filters: FilterState): void {
    console.log("onFilterApplied called with filters:", filters);

    // Convert FilterState to SnapshotFilterState
    const snapshotFilters: SnapshotFilterState = {
      mediaStatus: filters.mediaStatus,
      groundTruthLabels: filters.groundTruthLabels,
      split: filters.split,
      tags: filters.tags,
      mediaName: filters.mediaName,
      labeler: filters.labeler,
      mediaId: filters.mediaId,
      metadata: filters.metadata,
      noClass: filters.noClass,
    };

    console.log("Dispatching applyFilters action with:", snapshotFilters);

    this.store.dispatch(
      SnapshotListActions.applyFilters({ filters: snapshotFilters })
    );
  }

  /**
   * Handle filter clear from filter panel
   */
  onFilterClear(): void {
    this.store.dispatch(SnapshotListActions.clearFilters());
  }

  /**
   * Handle sort criteria changes
   * Requirements: 3.2
   * @param sortCriteria The new sort criteria
   */
  onSortApplied(sortCriteria: SnapshotSortMethod): void {
    this.store.dispatch(
      SnapshotListActions.applySortCriteria({ sortCriteria })
    );
  }

  /**
   * Handle page changes from image grid
   * Requirements: 1.3, 1.4
   * @param page The new page number (1-indexed from UI)
   */
  onPageChanged(page: number): void {
    // Convert from 1-indexed (UI) to 0-indexed (backend/state)
    this.store.dispatch(SnapshotListActions.changePage({ page: page - 1 }));
  }

  /**
   * Handle manage actions from toolbar
   * Requirements: 4.1, 5.1, 6.1, 7.1
   * @param action The manage action to perform
   */
  onManageAction(action: ManageActionType): void {
    this.selectedSnapshotId$.pipe(take(1)).subscribe((snapshotId) => {
      if (!snapshotId) {
        this.snackBar.open("Please select a snapshot first", "Close", {
          duration: 3000,
        });
        return;
      }

      switch (action) {
        case "create_project":
          this.handleCreateProject(snapshotId);
          break;
        case "revert":
          this.handleRevert(snapshotId);
          break;
        case "download":
          this.handleDownload(snapshotId);
          break;
        case "delete":
          this.handleDelete(snapshotId);
          break;
      }
    });
  }

  /**
   * Handle sidebar toggle
   * Requirements: 2.5
   */
  onToggleSidebar(): void {
    this.store.dispatch(SnapshotListActions.toggleSidebar());
  }

  /**
   * Toggle filter panel visibility - opens filter dialog
   */
  onToggleFilterPanel(): void {
    // Get current filters
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      // Convert SnapshotFilterState to FilterState for the dialog
      const filterState: FilterState = {
        mediaStatus: currentFilters.mediaStatus,
        groundTruthLabels: currentFilters.groundTruthLabels,
        split: currentFilters.split,
        tags: currentFilters.tags,
        mediaName: currentFilters.mediaName,
        labeler: currentFilters.labeler,
        mediaId: currentFilters.mediaId,
        metadata: currentFilters.metadata,
        noClass: currentFilters.noClass,
      };

      // Prepare dialog data
      const dialogData: FilterDialogData = {
        activeFilters: filterState,
        availableClasses: this.availableClasses,
        availableTags: this.availableTags,
        availableMetadata: this.availableMetadata,
        selectedModelId: undefined, // Snapshots don't have model selection
        hidePredictionLabels: true, // Hide prediction labels for snapshots
      };

      console.log("Opening filter dialog with data:", {
        classesCount: this.availableClasses.length,
        tagsCount: this.availableTags.length,
        metadataCount: this.availableMetadata.length,
        classes: this.availableClasses,
      });

      // Open filter dialog
      const dialogRef = this.dialog.open(FilterDialogComponent, {
        width: "650px",
        maxHeight: "85vh",
        data: dialogData,
        panelClass: "filter-dialog-panel",
      });

      // Handle dialog result
      dialogRef.afterClosed().subscribe((result: FilterDialogResult) => {
        console.log("Filter dialog closed with result:", result);

        if (result) {
          if (result.cleared) {
            console.log("Filters cleared");
            this.onFilterClear();
          } else if (result.filters) {
            console.log("Applying filters:", result.filters);
            this.onFilterApplied(result.filters);
          }
        } else {
          console.log("Dialog closed without result (cancelled)");
        }
      });
    });
  }

  /**
   * Handle image click - navigate to labeling page (read-only for snapshots)
   */
  onImageClick(image: Image): void {
    // For snapshots, we might want to show a read-only view
    // For now, just log the click
    console.log("Image clicked:", image);
  }

  /**
   * Navigate back to project page
   * Preserves query params (modelId, annotationType) to maintain filter state
   */
  onBackToProject(): void {
    this.router.navigate(["/landingai/projects", this.projectId], {
      queryParams: this.returnQueryParams,
    });
  }

  /**
   * Check if any filters are currently active
   * @param filters The current filter state
   * @returns true if any filter is active
   */
  hasActiveFilters(filters: SnapshotFilterState | null): boolean {
    if (!filters) {
      return false;
    }

    return !!(
      (filters.mediaStatus && filters.mediaStatus.length > 0) ||
      (filters.groundTruthLabels && filters.groundTruthLabels.length > 0) ||
      (filters.split && filters.split.length > 0) ||
      (filters.tags && filters.tags.length > 0) ||
      (filters.mediaName && filters.mediaName.trim().length > 0) ||
      (filters.labeler && filters.labeler.trim().length > 0) ||
      (filters.mediaId && filters.mediaId.trim().length > 0) ||
      (filters.metadata && Object.keys(filters.metadata).length > 0) ||
      filters.noClass === true
    );
  }

  /**
   * Get the Material icon name for a project type
   */
  getProjectTypeIcon(type: ProjectType): string {
    switch (type) {
      case "Classification":
        return "category";
      case "Object Detection":
        return "crop_free";
      case "Segmentation":
        return "polyline";
      default:
        return "image";
    }
  }

  /**
   * Clear error message
   */
  onClearError(): void {
    this.store.dispatch(SnapshotListActions.clearError());
  }

  /**
   * Load filter options (classes, tags, metadata) for the selected snapshot
   * These are used by the filter panel component
   */
  private loadFilterOptions(): void {
    // Get the selected snapshot ID
    this.selectedSnapshotId$.pipe(take(1)).subscribe((snapshotId) => {
      if (!snapshotId) {
        console.warn("No snapshot selected, cannot load filter options");
        return;
      }

      // Load classes from snapshot
      this.snapshotService
        .getSnapshotClasses(snapshotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (classes: any[]) => {
            this.availableClasses = classes as ProjectClass[];
            console.log("Loaded snapshot classes:", this.availableClasses);
          },
          error: (error: any) => {
            console.error("Failed to load snapshot classes:", error);
          },
        });

      // Load tags from snapshot
      this.snapshotService
        .getSnapshotTags(snapshotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (tags: any[]) => {
            this.availableTags = tags as ProjectTag[];
            console.log("Loaded snapshot tags:", this.availableTags);
          },
          error: (error: any) => {
            console.error("Failed to load snapshot tags:", error);
          },
        });

      // Load metadata from snapshot
      this.snapshotService
        .getSnapshotMetadata(snapshotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (metadata: any[]) => {
            this.availableMetadata = metadata as ProjectMetadata[];
          },
          error: (error: any) => {
            console.error("Failed to load snapshot metadata:", error);
          },
        });
    });
  }

  /**
   * Handle create project from snapshot action
   * Requirements: 4.2, 4.3, 4.5
   */
  private handleCreateProject(snapshotId: number): void {
    // Get all existing project names for validation
    this.store
      .select(HomeSelectors.selectProjects)
      .pipe(take(1))
      .subscribe((projects) => {
        const existingProjectNames = projects.map((p) => p.name);

        // Get current project and selected snapshot for default name
        this.project$.pipe(take(1)).subscribe((project) => {
          // Generate timestamp in format: YYYYMMDD_HHmmss
          const now = new Date();
          const timestamp =
            now.getFullYear().toString() +
            (now.getMonth() + 1).toString().padStart(2, "0") +
            now.getDate().toString().padStart(2, "0") +
            "_" +
            now.getHours().toString().padStart(2, "0") +
            now.getMinutes().toString().padStart(2, "0") +
            now.getSeconds().toString().padStart(2, "0");

          // Use original project name with timestamp
          const defaultName = project
            ? `${project.name}-${timestamp}`
            : `NewProject-${timestamp}`;

          // Open ProjectNameDialog
          // Requirements: 4.2
          const dialogData: ProjectNameDialogData = {
            defaultName,
            existingProjectNames,
          };

          const dialogRef = this.dialog.open(ProjectNameDialogComponent, {
            width: "550px",
            minHeight: "350px",
            maxHeight: "90vh",
            data: dialogData,
            disableClose: false,
            autoFocus: true,
          });

          // Handle dialog result
          dialogRef
            .afterClosed()
            .subscribe((result: ProjectNameDialogResult) => {
              if (result && result.projectName) {
                // Dispatch createProjectFromSnapshot action
                // Requirements: 4.3
                this.store.dispatch(
                  SnapshotListActions.createProjectFromSnapshot({
                    snapshotId,
                    projectName: result.projectName,
                  })
                );
              }
            });
        });
      });
  }

  /**
   * Handle revert to snapshot action
   * Requirements: 5.2, 5.3, 8.4
   */
  private handleRevert(snapshotId: number): void {
    this.selectedSnapshot$.pipe(take(1)).subscribe((snapshot) => {
      const snapshotName = snapshot?.name || "this snapshot";

      // Open confirmation dialog
      // Requirements: 5.2, 8.4
      const dialogRef = this.confirmDialogService.openDialog(
        "Revert to Snapshot?",
        `This will replace your current project data with the snapshot '${snapshotName}'. Your current data will be backed up automatically. Do you want to continue?`,
        "Revert",
        "Cancel"
      );

      // Dispatch revert action only if confirmed
      dialogRef.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          this.store.dispatch(
            SnapshotListActions.revertToSnapshot({
              snapshotId,
              projectId: this.projectId,
            })
          );
        }
      });
    });
  }

  /**
   * Handle download snapshot action
   * Requirements: 6.2
   */
  private handleDownload(snapshotId: number): void {
    this.selectedSnapshot$.pipe(take(1)).subscribe((snapshot) => {
      const snapshotName = snapshot?.name || `snapshot-${snapshotId}`;

      // Open progress dialog
      const dialogRef = this.dialog.open(DownloadProgressDialogComponent, {
        width: "450px",
        disableClose: true,
        data: {
          title: "Downloading Snapshot",
          message: `Preparing snapshot "${snapshotName}" for download...`,
        },
      });

      // Dispatch download action
      this.store.dispatch(SnapshotListActions.downloadSnapshot({ snapshotId }));

      // Subscribe to download completion to close dialog
      this.actions$
        .pipe(
          ofType(
            SnapshotListActions.downloadSnapshotSuccess,
            SnapshotListActions.downloadSnapshotFailure
          ),
          take(1)
        )
        .subscribe(() => {
          dialogRef.close();
        });
    });
  }

  /**
   * Handle delete snapshot action
   * Requirements: 7.2, 7.3, 8.4
   */
  private handleDelete(snapshotId: number): void {
    this.selectedSnapshot$.pipe(take(1)).subscribe((snapshot) => {
      const snapshotName = snapshot?.name || "this snapshot";

      // Open confirmation dialog
      // Requirements: 7.2, 8.4
      const dialogRef = this.confirmDialogService.openDialog(
        "Delete Snapshot?",
        `Are you sure you want to delete '${snapshotName}'? This action cannot be undone.`,
        "Delete",
        "Cancel"
      );

      // Dispatch delete action only if confirmed
      dialogRef.afterClosed().subscribe((confirmed: boolean) => {
        if (confirmed) {
          this.store.dispatch(
            SnapshotListActions.deleteSnapshot({ snapshotId })
          );
        }
      });
    });
  }
}
