import { createAction, props } from "@ngrx/store";
import { Image } from "app/models/landingai/image";
import { Snapshot } from "app/services/landingai/snapshot.service";
import { SnapshotFilterState, SnapshotSortMethod } from "./snapshot-list.state";

/**
 * Paginated response interface for snapshot images
 */
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

// ============================================
// Load Snapshots Actions
// Requirements: 2.1
// ============================================

export const loadSnapshots = createAction(
  "[Snapshot List] Load Snapshots",
  props<{ projectId: number }>()
);

export const loadSnapshotsSuccess = createAction(
  "[Snapshot List] Load Snapshots Success",
  props<{ snapshots: Snapshot[] }>()
);

export const loadSnapshotsFailure = createAction(
  "[Snapshot List] Load Snapshots Failure",
  props<{ error: string }>()
);

// ============================================
// Select Snapshot Actions
// Requirements: 2.3
// ============================================

export const selectSnapshot = createAction(
  "[Snapshot List] Select Snapshot",
  props<{ snapshotId: number }>()
);

export const selectSnapshotSuccess = createAction(
  "[Snapshot List] Select Snapshot Success",
  props<{ snapshotId: number }>()
);

// ============================================
// Load Snapshot Images Actions
// Requirements: 1.1
// ============================================

export const loadSnapshotImages = createAction(
  "[Snapshot List] Load Snapshot Images",
  props<{
    snapshotId: number;
    page: number;
    size: number;
    filters: SnapshotFilterState;
    sortBy: SnapshotSortMethod;
  }>()
);

export const loadSnapshotImagesSuccess = createAction(
  "[Snapshot List] Load Snapshot Images Success",
  props<{ response: PaginatedResponse<Image> }>()
);

export const loadSnapshotImagesFailure = createAction(
  "[Snapshot List] Load Snapshot Images Failure",
  props<{ error: string }>()
);

// ============================================
// Filter Actions
// Requirements: 3.1
// ============================================

export const applyFilters = createAction(
  "[Snapshot List] Apply Filters",
  props<{ filters: SnapshotFilterState }>()
);

export const clearFilters = createAction("[Snapshot List] Clear Filters");

// ============================================
// Sort Actions
// Requirements: 3.2
// ============================================

export const applySortCriteria = createAction(
  "[Snapshot List] Apply Sort Criteria",
  props<{ sortCriteria: SnapshotSortMethod }>()
);

// ============================================
// Pagination Actions
// Requirements: 1.3, 1.4
// ============================================

export const changePage = createAction(
  "[Snapshot List] Change Page",
  props<{ page: number }>()
);

export const nextPage = createAction("[Snapshot List] Next Page");

export const previousPage = createAction("[Snapshot List] Previous Page");

// ============================================
// Create Project from Snapshot Actions
// Requirements: 4.3
// ============================================

export const createProjectFromSnapshot = createAction(
  "[Snapshot List] Create Project From Snapshot",
  props<{ snapshotId: number; projectName: string }>()
);

export const createProjectFromSnapshotSuccess = createAction(
  "[Snapshot List] Create Project From Snapshot Success",
  props<{ projectId: number; projectName: string }>()
);

export const createProjectFromSnapshotFailure = createAction(
  "[Snapshot List] Create Project From Snapshot Failure",
  props<{ error: string }>()
);

// ============================================
// Revert to Snapshot Actions
// Requirements: 5.3
// ============================================

export const revertToSnapshot = createAction(
  "[Snapshot List] Revert To Snapshot",
  props<{ snapshotId: number; projectId: number }>()
);

export const revertToSnapshotSuccess = createAction(
  "[Snapshot List] Revert To Snapshot Success",
  props<{ message: string }>()
);

export const revertToSnapshotFailure = createAction(
  "[Snapshot List] Revert To Snapshot Failure",
  props<{ error: string }>()
);

// ============================================
// Download Snapshot Actions
// Requirements: 6.2
// ============================================

export const downloadSnapshot = createAction(
  "[Snapshot List] Download Snapshot",
  props<{ snapshotId: number }>()
);

export const downloadSnapshotSuccess = createAction(
  "[Snapshot List] Download Snapshot Success",
  props<{ message: string }>()
);

export const downloadSnapshotFailure = createAction(
  "[Snapshot List] Download Snapshot Failure",
  props<{ error: string }>()
);

// ============================================
// Delete Snapshot Actions
// Requirements: 7.3
// ============================================

export const deleteSnapshot = createAction(
  "[Snapshot List] Delete Snapshot",
  props<{ snapshotId: number }>()
);

export const deleteSnapshotSuccess = createAction(
  "[Snapshot List] Delete Snapshot Success",
  props<{ snapshotId: number }>()
);

export const deleteSnapshotFailure = createAction(
  "[Snapshot List] Delete Snapshot Failure",
  props<{ error: string }>()
);

// ============================================
// UI Actions
// ============================================

export const toggleSidebar = createAction("[Snapshot List] Toggle Sidebar");

export const setSidebarCollapsed = createAction(
  "[Snapshot List] Set Sidebar Collapsed",
  props<{ collapsed: boolean }>()
);

// ============================================
// Reset State Action
// ============================================

export const resetSnapshotListState = createAction(
  "[Snapshot List] Reset State"
);

// ============================================
// Operation Loading Actions
// ============================================

export const setOperationLoading = createAction(
  "[Snapshot List] Set Operation Loading",
  props<{ loading: boolean }>()
);

// ============================================
// Clear Error Action
// ============================================

export const clearError = createAction("[Snapshot List] Clear Error");
