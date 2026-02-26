import { createFeatureSelector, createSelector } from "@ngrx/store";
import { SnapshotListState } from "./snapshot-list.state";

/**
 * Feature selector for snapshot list state
 */
export const selectSnapshotListState =
  createFeatureSelector<SnapshotListState>("snapshotList");

// ============================================
// Snapshot Selectors
// ============================================

/**
 * Select all snapshots
 * Requirements: 2.1
 */
export const selectSnapshots = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.snapshots
);

/**
 * Select the currently selected snapshot ID
 * Requirements: 2.3, 2.4
 */
export const selectSelectedSnapshotId = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.selectedSnapshotId
);

/**
 * Select the currently selected snapshot object
 * Requirements: 2.3, 2.4
 */
export const selectSelectedSnapshot = createSelector(
  selectSnapshots,
  selectSelectedSnapshotId,
  (snapshots, selectedId) =>
    selectedId ? snapshots.find((s) => s.id === selectedId) || null : null
);

/**
 * Select snapshot count
 */
export const selectSnapshotCount = createSelector(
  selectSnapshots,
  (snapshots) => snapshots.length
);

// ============================================
// Image Selectors
// ============================================

/**
 * Select images for the selected snapshot
 * Requirements: 1.1
 */
export const selectImages = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.images
);

/**
 * Select image count
 */
export const selectImageCount = createSelector(
  selectImages,
  (images) => images.length
);

// ============================================
// Pagination Selectors
// ============================================

/**
 * Select pagination state
 * Requirements: 1.3, 1.4
 */
export const selectPagination = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.pagination
);

/**
 * Select current page (0-indexed for backend)
 */
export const selectCurrentPage = createSelector(
  selectPagination,
  (pagination) => pagination.currentPage
);

/**
 * Select current page for display (1-indexed for UI)
 */
export const selectCurrentPageDisplay = createSelector(
  selectPagination,
  (pagination) => pagination.currentPage + 1
);

/**
 * Select page size
 */
export const selectPageSize = createSelector(
  selectPagination,
  (pagination) => pagination.pageSize
);

/**
 * Select total items
 */
export const selectTotalItems = createSelector(
  selectPagination,
  (pagination) => pagination.totalItems
);

/**
 * Select total pages
 */
export const selectTotalPages = createSelector(
  selectPagination,
  (pagination) => pagination.totalPages
);

/**
 * Select whether at first page
 */
export const selectIsFirstPage = createSelector(
  selectCurrentPage,
  (currentPage) => currentPage === 0
);

/**
 * Select whether at last page
 */
export const selectIsLastPage = createSelector(
  selectCurrentPage,
  selectTotalPages,
  (currentPage, totalPages) => currentPage >= totalPages - 1
);

/**
 * Select pagination info for display
 */
export const selectPaginationInfo = createSelector(
  selectCurrentPageDisplay,
  selectTotalPages,
  selectTotalItems,
  selectPageSize,
  (currentPage, totalPages, totalItems, pageSize) => ({
    currentPage,
    totalPages,
    totalItems,
    pageSize,
  })
);

// ============================================
// Filter Selectors
// ============================================

/**
 * Select current filters
 * Requirements: 3.1
 */
export const selectFilters = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.filters
);

/**
 * Select whether filters are active
 */
export const selectHasActiveFilters = createSelector(
  selectFilters,
  (filters) => Object.keys(filters).length > 0
);

// ============================================
// Sort Selectors
// ============================================

/**
 * Select current sort criteria
 * Requirements: 3.2
 */
export const selectSortCriteria = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.sortCriteria
);

// ============================================
// Loading Selectors
// ============================================

/**
 * Select loading state
 */
export const selectLoading = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.loading
);

/**
 * Select snapshots loading state
 */
export const selectSnapshotsLoading = createSelector(
  selectLoading,
  (loading) => loading.snapshots
);

/**
 * Select images loading state
 */
export const selectImagesLoading = createSelector(
  selectLoading,
  (loading) => loading.images
);

/**
 * Select operation loading state
 */
export const selectOperationLoading = createSelector(
  selectLoading,
  (loading) => loading.operation
);

/**
 * Select any loading state (for general loading indicator)
 */
export const selectAnyLoading = createSelector(
  selectLoading,
  (loading) => loading.snapshots || loading.images || loading.operation
);

// ============================================
// Error Selectors
// ============================================

/**
 * Select error state
 */
export const selectError = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.error
);

/**
 * Select whether there is an error
 */
export const selectHasError = createSelector(
  selectError,
  (error) => error !== null
);

// ============================================
// UI Selectors
// ============================================

/**
 * Select sidebar collapsed state
 */
export const selectSidebarCollapsed = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.sidebarCollapsed
);

// ============================================
// Project Selectors
// ============================================

/**
 * Select project ID
 */
export const selectProjectId = createSelector(
  selectSnapshotListState,
  (state: SnapshotListState) => state.projectId
);

// ============================================
// Composite Selectors
// ============================================

/**
 * Select view model for the snapshot list component
 * Combines multiple selectors for efficient component binding
 */
export const selectSnapshotListViewModel = createSelector(
  selectSnapshots,
  selectSelectedSnapshot,
  selectImages,
  selectPaginationInfo,
  selectFilters,
  selectSortCriteria,
  selectLoading,
  selectError,
  selectSidebarCollapsed,
  (
    snapshots,
    selectedSnapshot,
    images,
    pagination,
    filters,
    sortCriteria,
    loading,
    error,
    sidebarCollapsed
  ) => ({
    snapshots,
    selectedSnapshot,
    images,
    pagination,
    filters,
    sortCriteria,
    loading,
    error,
    sidebarCollapsed,
  })
);
