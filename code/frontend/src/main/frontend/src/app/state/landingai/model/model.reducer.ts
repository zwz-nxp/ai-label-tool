import { createReducer, on } from "@ngrx/store";
import { ModelDisplayDto, SearchFilters } from "app/models/landingai/model";
import * as ModelActions from "./model.actions";

export interface ModelState {
  models: ModelDisplayDto[];
  filteredModels: ModelDisplayDto[];
  searchFilters: SearchFilters;
  currentProjectId: number | null;
  loading: boolean;
  error: string | null;
}

export const modelInitialState: ModelState = {
  models: [],
  filteredModels: [],
  searchFilters: {
    searchTerm: "",
    showFavoritesOnly: false,
  },
  currentProjectId: null,
  loading: false,
  error: null,
};

export const modelReducer = createReducer(
  modelInitialState,

  // Load Models
  on(
    ModelActions.loadModels,
    (state): ModelState => ({
      ...state,
      loading: true,
      error: null,
      currentProjectId: null,
    })
  ),
  on(
    ModelActions.loadModelsSuccess,
    (state, { models }): ModelState => ({
      ...state,
      loading: false,
      models,
      filteredModels: models,
      error: null,
    })
  ),
  on(
    ModelActions.loadModelsFailure,
    (state, { error }): ModelState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Load Models by Project
  on(
    ModelActions.loadModelsByProject,
    (state, { projectId }): ModelState => ({
      ...state,
      loading: true,
      error: null,
      currentProjectId: projectId,
    })
  ),
  on(
    ModelActions.loadModelsByProjectSuccess,
    (state, { models }): ModelState => ({
      ...state,
      loading: false,
      models,
      filteredModels: models,
      error: null,
    })
  ),
  on(
    ModelActions.loadModelsByProjectFailure,
    (state, { error }): ModelState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Search Models
  on(
    ModelActions.searchModels,
    (state): ModelState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),
  on(
    ModelActions.searchModelsSuccess,
    (state, { models }): ModelState => ({
      ...state,
      loading: false,
      filteredModels: models,
      error: null,
    })
  ),
  on(
    ModelActions.searchModelsFailure,
    (state, { error }): ModelState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Toggle Favorite
  on(ModelActions.toggleFavorite, (state, { modelId }): ModelState => {
    // Optimistic update
    const updateModel = (model: ModelDisplayDto) =>
      model.id === modelId
        ? { ...model, isFavorite: !model.isFavorite }
        : model;

    return {
      ...state,
      models: state.models.map(updateModel),
      filteredModels: state.filteredModels.map(updateModel),
    };
  }),
  on(ModelActions.toggleFavoriteSuccess, (state, { model }): ModelState => {
    // Update with server response
    const updateModel = (m: ModelDisplayDto) =>
      m.id === model.id ? { ...m, isFavorite: model.isFavorite } : m;

    return {
      ...state,
      models: state.models.map(updateModel),
      filteredModels: state.filteredModels.map(updateModel),
      error: null,
    };
  }),
  on(
    ModelActions.toggleFavoriteFailure,
    (state, { modelId, error }): ModelState => {
      // Revert optimistic update
      const revertModel = (model: ModelDisplayDto) =>
        model.id === modelId
          ? { ...model, isFavorite: !model.isFavorite }
          : model;

      return {
        ...state,
        models: state.models.map(revertModel),
        filteredModels: state.filteredModels.map(revertModel),
        error,
      };
    }
  ),

  // Update Search Filters
  on(
    ModelActions.updateSearchTerm,
    (state, { searchTerm }): ModelState => ({
      ...state,
      searchFilters: {
        ...state.searchFilters,
        searchTerm,
      },
    })
  ),
  on(
    ModelActions.updateFavoritesFilter,
    (state, { showFavoritesOnly }): ModelState => ({
      ...state,
      searchFilters: {
        ...state.searchFilters,
        showFavoritesOnly,
      },
    })
  ),

  // Clear Error
  on(
    ModelActions.clearError,
    (state): ModelState => ({
      ...state,
      error: null,
    })
  )
);
