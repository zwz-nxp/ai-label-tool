import { createReducer, on } from "@ngrx/store";
import { Image } from "app/models/landingai/image";
import * as ImageUploadActions from "./image-upload.actions";
import { FilterState, SortMethod, ViewMode } from "./image-upload.actions";

export interface ImageUploadState {
  images: Image[];
  viewMode: ViewMode;
  filters: FilterState;
  sortMethod: SortMethod;
  zoomLevel: number; // 1-5, where 1 = 6 images per row, 5 = 2 images per row
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  loading: boolean;
  error: string | null;
  projectId: number | null;
}

export const imageUploadInitialState: ImageUploadState = {
  images: [],
  viewMode: "images",
  filters: {},
  sortMethod: "upload_time_desc",
  zoomLevel: 3, // Default: 4 images per row
  currentPage: 0, // Backend uses 0-indexed pagination
  totalPages: 0,
  totalElements: 0,
  pageSize: 20,
  loading: false,
  error: null,
  projectId: null,
};

const MIN_ZOOM = 1;
const MAX_ZOOM = 5;

export const imageUploadReducer = createReducer(
  imageUploadInitialState,

  // Load Images
  on(
    ImageUploadActions.loadImages,
    (state, { projectId }): ImageUploadState => ({
      ...state,
      loading: true,
      error: null,
      projectId,
    })
  ),
  on(
    ImageUploadActions.loadImagesSuccess,
    (state, { response }): ImageUploadState => ({
      ...state,
      loading: false,
      images: response.content,
      currentPage: response.page,
      totalPages: response.totalPages,
      totalElements: response.totalElements,
      pageSize: response.size,
      error: null,
    })
  ),
  on(
    ImageUploadActions.loadImagesFailure,
    (state, { error }): ImageUploadState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Change View Mode
  on(
    ImageUploadActions.changeViewMode,
    (state, { viewMode }): ImageUploadState => ({
      ...state,
      viewMode,
      // Reset to page 0 when changing view mode (backend uses 0-indexed pagination)
      currentPage: 0,
    })
  ),

  // Apply Filters
  on(
    ImageUploadActions.applyFilters,
    (state, { filters }): ImageUploadState => ({
      ...state,
      filters,
      // Reset to page 0 when applying filters (backend uses 0-indexed pagination)
      currentPage: 0,
    })
  ),
  on(
    ImageUploadActions.clearFilters,
    (state): ImageUploadState => ({
      ...state,
      filters: {},
      // Reset to page 0 when clearing filters (backend uses 0-indexed pagination)
      currentPage: 0,
    })
  ),

  // Change Sort Method
  on(
    ImageUploadActions.changeSortMethod,
    (state, { sortMethod }): ImageUploadState => ({
      ...state,
      sortMethod,
      // Reset to page 0 when changing sort (backend uses 0-indexed pagination)
      currentPage: 0,
    })
  ),

  // Change Zoom Level
  on(
    ImageUploadActions.changeZoomLevel,
    (state, { zoomLevel }): ImageUploadState => ({
      ...state,
      zoomLevel: Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomLevel)),
    })
  ),
  on(
    ImageUploadActions.zoomIn,
    (state): ImageUploadState => ({
      ...state,
      zoomLevel: Math.max(MIN_ZOOM, state.zoomLevel - 1),
    })
  ),
  on(
    ImageUploadActions.zoomOut,
    (state): ImageUploadState => ({
      ...state,
      zoomLevel: Math.min(MAX_ZOOM, state.zoomLevel + 1),
    })
  ),

  // Change Page
  on(
    ImageUploadActions.changePage,
    (state, { page }): ImageUploadState => ({
      ...state,
      currentPage: Math.max(0, Math.min(state.totalPages - 1, page)),
    })
  ),
  on(
    ImageUploadActions.nextPage,
    (state): ImageUploadState => ({
      ...state,
      currentPage: Math.min(state.totalPages - 1, state.currentPage + 1),
    })
  ),
  on(
    ImageUploadActions.previousPage,
    (state): ImageUploadState => ({
      ...state,
      currentPage: Math.max(0, state.currentPage - 1),
    })
  ),

  // Reset State
  on(
    ImageUploadActions.resetImageUploadState,
    (): ImageUploadState => imageUploadInitialState
  ),

  // Update Single Image Label (local state update without reload)
  on(
    ImageUploadActions.updateImageLabel,
    (
      state,
      { imageId, classId, className, colorCode, labelId }
    ): ImageUploadState => ({
      ...state,
      images: state.images.map((image) => {
        if (image.id !== imageId) {
          return image;
        }

        // Update the image's labels
        let updatedLabels = image.labels ? [...image.labels] : [];

        if (classId === null) {
          // Remove the ground truth label
          updatedLabels = updatedLabels.filter(
            (l: any) => l.annotationType !== "Ground Truth"
          );
        } else {
          // Find existing ground truth label
          const existingLabelIndex = updatedLabels.findIndex(
            (l: any) => l.annotationType === "Ground Truth"
          );

          const newLabel: any = {
            id: labelId,
            classId: classId,
            className: className,
            colorCode: colorCode,
            annotationType: "Ground Truth",
            position: null,
          };

          if (existingLabelIndex >= 0) {
            // Update existing label
            updatedLabels[existingLabelIndex] = {
              ...updatedLabels[existingLabelIndex],
              ...newLabel,
            };
          } else {
            // Add new label
            updatedLabels.push(newLabel);
          }
        }

        return {
          ...image,
          labels: updatedLabels,
          isNoClass: classId === null && updatedLabels.length === 0,
        };
      }),
    })
  )
);
