import { createFeatureSelector, createSelector } from "@ngrx/store";
import { ModelState } from "./model.reducer";

export const selectModelState = createFeatureSelector<ModelState>("model");

export const selectAllModels = createSelector(
  selectModelState,
  (state: ModelState) => state.models
);

export const selectFilteredModels = createSelector(
  selectModelState,
  (state: ModelState) => state.filteredModels
);

export const selectSearchFilters = createSelector(
  selectModelState,
  (state: ModelState) => state.searchFilters
);

export const selectCurrentProjectId = createSelector(
  selectModelState,
  (state: ModelState) => state.currentProjectId
);

export const selectSearchTerm = createSelector(
  selectSearchFilters,
  (filters) => filters.searchTerm
);

export const selectShowFavoritesOnly = createSelector(
  selectSearchFilters,
  (filters) => filters.showFavoritesOnly
);

export const selectLoading = createSelector(
  selectModelState,
  (state: ModelState) => state.loading
);

export const selectError = createSelector(
  selectModelState,
  (state: ModelState) => state.error
);

export const selectModelById = (modelId: number) =>
  createSelector(selectAllModels, (models) =>
    models.find((model) => model.id === modelId)
  );

export const selectFavoriteModels = createSelector(selectAllModels, (models) =>
  models.filter((model) => model.isFavorite)
);

export const selectHasModels = createSelector(
  selectAllModels,
  (models) => models.length > 0
);

export const selectHasFilteredModels = createSelector(
  selectFilteredModels,
  (models) => models.length > 0
);
