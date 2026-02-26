import { createAction, props } from "@ngrx/store";
import { Image } from "app/models/landingai/image";

// Pagination Response
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

// Filter State
export interface FilterState {
  mediaStatus?: string[]; // labeled, unlabeled
  groundTruthLabels?: number[]; // class IDs
  predictionLabels?: number[]; // class IDs
  annotationType?: string; // "Ground truth" or "Prediction"
  modelId?: number; // model ID for prediction labels
  split?: string[]; // training, dev, test
  tags?: number[]; // tag IDs
  mediaName?: string;
  labeler?: string;
  mediaId?: string;
  metadata?: { [key: string]: string };
  noClass?: boolean; // filter for images with is_labeled=true and is_no_class=true
  predictionNoClass?: boolean; // filter for images without any prediction labels for the selected model
}

// Sort Method Type
export type SortMethod =
  | "upload_time_desc"
  | "upload_time_asc"
  | "label_time_desc"
  | "label_time_asc"
  | "name_asc"
  | "name_desc"
  | "random";

// View Mode Type
export type ViewMode = "images" | "instances";

// Load Images Actions
export const loadImages = createAction(
  "[Image Upload] Load Images",
  props<{
    projectId: number;
    page: number;
    size: number;
    viewMode: ViewMode;
    filters: FilterState;
    sortBy: SortMethod;
  }>()
);

export const loadImagesSuccess = createAction(
  "[Image Upload] Load Images Success",
  props<{ response: PaginatedResponse<Image> }>()
);

export const loadImagesFailure = createAction(
  "[Image Upload] Load Images Failure",
  props<{ error: string }>()
);

// Change View Mode Actions
export const changeViewMode = createAction(
  "[Image Upload] Change View Mode",
  props<{ viewMode: ViewMode }>()
);

// Apply Filters Actions
export const applyFilters = createAction(
  "[Image Upload] Apply Filters",
  props<{ filters: FilterState }>()
);

export const clearFilters = createAction("[Image Upload] Clear Filters");

// Change Sort Method Actions
export const changeSortMethod = createAction(
  "[Image Upload] Change Sort Method",
  props<{ sortMethod: SortMethod }>()
);

// Change Zoom Level Actions
export const changeZoomLevel = createAction(
  "[Image Upload] Change Zoom Level",
  props<{ zoomLevel: number }>()
);

export const zoomIn = createAction("[Image Upload] Zoom In");

export const zoomOut = createAction("[Image Upload] Zoom Out");

// Change Page Actions
export const changePage = createAction(
  "[Image Upload] Change Page",
  props<{ page: number }>()
);

export const nextPage = createAction("[Image Upload] Next Page");

export const previousPage = createAction("[Image Upload] Previous Page");

// Reset State Action
export const resetImageUploadState = createAction("[Image Upload] Reset State");

// Update Single Image Label Action (for local state update without reload)
export const updateImageLabel = createAction(
  "[Image Upload] Update Image Label",
  props<{
    imageId: number;
    classId: number | null;
    className: string | null;
    colorCode: string | null;
    labelId?: number;
  }>()
);
