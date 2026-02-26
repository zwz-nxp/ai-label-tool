import { createAction, props } from "@ngrx/store";
import { ModelDisplayDto, SearchFilters } from "app/models/landingai/model";

// Load Models Actions
export const loadModels = createAction("[Model] Load Models");

export const loadModelsSuccess = createAction(
  "[Model] Load Models Success",
  props<{ models: ModelDisplayDto[] }>()
);

export const loadModelsFailure = createAction(
  "[Model] Load Models Failure",
  props<{ error: string }>()
);

// Load Models by Project Actions
export const loadModelsByProject = createAction(
  "[Model] Load Models By Project",
  props<{ projectId: number }>()
);

export const loadModelsByProjectSuccess = createAction(
  "[Model] Load Models By Project Success",
  props<{ models: ModelDisplayDto[] }>()
);

export const loadModelsByProjectFailure = createAction(
  "[Model] Load Models By Project Failure",
  props<{ error: string }>()
);

// Search Models Actions
export const searchModels = createAction(
  "[Model] Search Models",
  props<{ filters: SearchFilters }>()
);

export const searchModelsSuccess = createAction(
  "[Model] Search Models Success",
  props<{ models: ModelDisplayDto[] }>()
);

export const searchModelsFailure = createAction(
  "[Model] Search Models Failure",
  props<{ error: string }>()
);

// Toggle Favorite Actions
export const toggleFavorite = createAction(
  "[Model] Toggle Favorite",
  props<{ modelId: number }>()
);

export const toggleFavoriteSuccess = createAction(
  "[Model] Toggle Favorite Success",
  props<{ model: ModelDisplayDto }>()
);

export const toggleFavoriteFailure = createAction(
  "[Model] Toggle Favorite Failure",
  props<{ modelId: number; error: string }>()
);

// Update Search Filters Actions
export const updateSearchTerm = createAction(
  "[Model] Update Search Term",
  props<{ searchTerm: string }>()
);

export const updateFavoritesFilter = createAction(
  "[Model] Update Favorites Filter",
  props<{ showFavoritesOnly: boolean }>()
);

// Clear Error Action
export const clearError = createAction("[Model] Clear Error");
