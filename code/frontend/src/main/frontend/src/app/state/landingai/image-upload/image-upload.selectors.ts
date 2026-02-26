import { createFeatureSelector, createSelector } from "@ngrx/store";
import { ImageUploadState } from "./image-upload.reducer";

// Feature selector for image upload state
export const selectImageUploadState =
  createFeatureSelector<ImageUploadState>("imageUpload");

// Select images
export const selectImages = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.images
);

// Select view mode
export const selectViewMode = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.viewMode
);

// Select filters
export const selectFilters = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.filters
);

// Select sort method
export const selectSortMethod = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.sortMethod
);

// Select zoom level
export const selectZoomLevel = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.zoomLevel
);

// Select images per row based on zoom level
export const selectImagesPerRow = createSelector(
  selectZoomLevel,
  (zoomLevel: number) => {
    // Zoom level 1 = 6 images per row
    // Zoom level 2 = 5 images per row
    // Zoom level 3 = 4 images per row
    // Zoom level 4 = 3 images per row
    // Zoom level 5 = 2 images per row
    return 7 - zoomLevel;
  }
);

// Select current page (convert from 0-indexed to 1-indexed for UI display)
export const selectCurrentPage = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.currentPage + 1
);

// Select total pages
export const selectTotalPages = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.totalPages
);

// Select total elements
export const selectTotalElements = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.totalElements
);

// Select page size
export const selectPageSize = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.pageSize
);

// Select loading state
export const selectLoading = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.loading
);

// Select error
export const selectError = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.error
);

// Select project ID
export const selectProjectId = createSelector(
  selectImageUploadState,
  (state: ImageUploadState) => state.projectId
);

// Select pagination info
export const selectPaginationInfo = createSelector(
  selectCurrentPage,
  selectTotalPages,
  selectTotalElements,
  selectPageSize,
  (currentPage, totalPages, totalElements, pageSize) => ({
    currentPage,
    totalPages,
    totalElements,
    pageSize,
  })
);

// Select whether filters are active
export const selectHasActiveFilters = createSelector(
  selectFilters,
  (filters) => {
    return Object.keys(filters).length > 0;
  }
);

// Select whether at first page
export const selectIsFirstPage = createSelector(
  selectCurrentPage,
  (currentPage) => currentPage === 1
);

// Select whether at last page
export const selectIsLastPage = createSelector(
  selectCurrentPage,
  selectTotalPages,
  (currentPage, totalPages) => currentPage >= totalPages
);

// Select whether zoom in is disabled
export const selectZoomInDisabled = createSelector(
  selectZoomLevel,
  (zoomLevel) => zoomLevel <= 1
);

// Select whether zoom out is disabled
export const selectZoomOutDisabled = createSelector(
  selectZoomLevel,
  (zoomLevel) => zoomLevel >= 5
);
