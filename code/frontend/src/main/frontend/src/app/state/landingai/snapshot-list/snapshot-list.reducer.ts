import { createReducer, on } from "@ngrx/store";
import {
  SnapshotListState,
  snapshotListInitialState,
} from "./snapshot-list.state";
import * as SnapshotListActions from "./snapshot-list.actions";

/**
 * Reducer for snapshot list state management
 * Requirements: 1.1, 2.1, 2.3, 3.1, 3.2
 */
export const snapshotListReducer = createReducer(
  snapshotListInitialState,

  // ============================================
  // Load Snapshots
  // ============================================
  on(
    SnapshotListActions.loadSnapshots,
    (state, { projectId }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, snapshots: true },
      error: null,
      projectId,
    })
  ),

  on(
    SnapshotListActions.loadSnapshotsSuccess,
    (state, { snapshots }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, snapshots: false },
      snapshots,
      error: null,
    })
  ),

  on(
    SnapshotListActions.loadSnapshotsFailure,
    (state, { error }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, snapshots: false },
      error,
    })
  ),

  // ============================================
  // Select Snapshot
  // ============================================
  on(
    SnapshotListActions.selectSnapshot,
    (state, { snapshotId }): SnapshotListState => ({
      ...state,
      selectedSnapshotId: snapshotId,
      // Reset pagination when selecting a new snapshot
      pagination: {
        ...state.pagination,
        currentPage: 0,
      },
    })
  ),

  on(
    SnapshotListActions.selectSnapshotSuccess,
    (state, { snapshotId }): SnapshotListState => ({
      ...state,
      selectedSnapshotId: snapshotId,
    })
  ),

  // ============================================
  // Load Snapshot Images
  // ============================================
  on(
    SnapshotListActions.loadSnapshotImages,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, images: true },
      error: null,
    })
  ),

  on(
    SnapshotListActions.loadSnapshotImagesSuccess,
    (state, { response }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, images: false },
      images: response.content,
      pagination: {
        currentPage: response.page,
        pageSize: response.size,
        totalItems: response.totalElements,
        totalPages: response.totalPages,
      },
      error: null,
    })
  ),

  on(
    SnapshotListActions.loadSnapshotImagesFailure,
    (state, { error }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, images: false },
      error,
    })
  ),

  // ============================================
  // Apply Filters
  // ============================================
  on(
    SnapshotListActions.applyFilters,
    (state, { filters }): SnapshotListState => ({
      ...state,
      filters,
      // Reset to first page when applying filters
      pagination: {
        ...state.pagination,
        currentPage: 0,
      },
    })
  ),

  on(
    SnapshotListActions.clearFilters,
    (state): SnapshotListState => ({
      ...state,
      filters: {},
      // Reset to first page when clearing filters
      pagination: {
        ...state.pagination,
        currentPage: 0,
      },
    })
  ),

  // ============================================
  // Apply Sort Criteria
  // ============================================
  on(
    SnapshotListActions.applySortCriteria,
    (state, { sortCriteria }): SnapshotListState => ({
      ...state,
      sortCriteria,
      // Reset to first page when changing sort
      pagination: {
        ...state.pagination,
        currentPage: 0,
      },
    })
  ),

  // ============================================
  // Pagination
  // ============================================
  on(
    SnapshotListActions.changePage,
    (state, { page }): SnapshotListState => ({
      ...state,
      pagination: {
        ...state.pagination,
        currentPage: Math.max(
          0,
          Math.min(state.pagination.totalPages - 1, page)
        ),
      },
    })
  ),

  on(
    SnapshotListActions.nextPage,
    (state): SnapshotListState => ({
      ...state,
      pagination: {
        ...state.pagination,
        currentPage: Math.min(
          state.pagination.totalPages - 1,
          state.pagination.currentPage + 1
        ),
      },
    })
  ),

  on(
    SnapshotListActions.previousPage,
    (state): SnapshotListState => ({
      ...state,
      pagination: {
        ...state.pagination,
        currentPage: Math.max(0, state.pagination.currentPage - 1),
      },
    })
  ),

  // ============================================
  // Create Project from Snapshot
  // ============================================
  on(
    SnapshotListActions.createProjectFromSnapshot,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: true },
      error: null,
    })
  ),

  on(
    SnapshotListActions.createProjectFromSnapshotSuccess,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error: null,
    })
  ),

  on(
    SnapshotListActions.createProjectFromSnapshotFailure,
    (state, { error }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error,
    })
  ),

  // ============================================
  // Revert to Snapshot
  // ============================================
  on(
    SnapshotListActions.revertToSnapshot,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: true },
      error: null,
    })
  ),

  on(
    SnapshotListActions.revertToSnapshotSuccess,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error: null,
    })
  ),

  on(
    SnapshotListActions.revertToSnapshotFailure,
    (state, { error }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error,
    })
  ),

  // ============================================
  // Download Snapshot
  // ============================================
  on(
    SnapshotListActions.downloadSnapshot,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: true },
      error: null,
    })
  ),

  on(
    SnapshotListActions.downloadSnapshotSuccess,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error: null,
    })
  ),

  on(
    SnapshotListActions.downloadSnapshotFailure,
    (state, { error }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error,
    })
  ),

  // ============================================
  // Delete Snapshot
  // ============================================
  on(
    SnapshotListActions.deleteSnapshot,
    (state): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: true },
      error: null,
    })
  ),

  on(
    SnapshotListActions.deleteSnapshotSuccess,
    (state, { snapshotId }): SnapshotListState => {
      const updatedSnapshots = state.snapshots.filter(
        (s) => s.id !== snapshotId
      );

      // If the deleted snapshot was selected, select the most recent remaining snapshot
      let newSelectedId = state.selectedSnapshotId;
      if (state.selectedSnapshotId === snapshotId) {
        newSelectedId =
          updatedSnapshots.length > 0 ? updatedSnapshots[0].id : null;
      }

      return {
        ...state,
        loading: { ...state.loading, operation: false },
        snapshots: updatedSnapshots,
        selectedSnapshotId: newSelectedId,
        // Clear images if no snapshot is selected
        images: newSelectedId ? state.images : [],
        error: null,
      };
    }
  ),

  on(
    SnapshotListActions.deleteSnapshotFailure,
    (state, { error }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: false },
      error,
    })
  ),

  // ============================================
  // UI Actions
  // ============================================
  on(
    SnapshotListActions.toggleSidebar,
    (state): SnapshotListState => ({
      ...state,
      sidebarCollapsed: !state.sidebarCollapsed,
    })
  ),

  on(
    SnapshotListActions.setSidebarCollapsed,
    (state, { collapsed }): SnapshotListState => ({
      ...state,
      sidebarCollapsed: collapsed,
    })
  ),

  // ============================================
  // Operation Loading
  // ============================================
  on(
    SnapshotListActions.setOperationLoading,
    (state, { loading }): SnapshotListState => ({
      ...state,
      loading: { ...state.loading, operation: loading },
    })
  ),

  // ============================================
  // Clear Error
  // ============================================
  on(
    SnapshotListActions.clearError,
    (state): SnapshotListState => ({
      ...state,
      error: null,
    })
  ),

  // ============================================
  // Reset State
  // ============================================
  on(
    SnapshotListActions.resetSnapshotListState,
    (): SnapshotListState => snapshotListInitialState
  )
);
