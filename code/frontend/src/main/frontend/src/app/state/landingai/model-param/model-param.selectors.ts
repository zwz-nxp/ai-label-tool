import { createFeatureSelector, createSelector } from "@ngrx/store";
import { ModelParamState } from "./model-param.state";

/**
 * Feature selector for model parameter state
 */
export const selectModelParamState =
  createFeatureSelector<ModelParamState>("modelParam");

// ============================================
// Model Parameter Selectors
// Requirements: 2.1
// ============================================

/**
 * Select all model parameters
 * Requirements: 2.1
 */
export const selectAllModelParams = createSelector(
  selectModelParamState,
  (state: ModelParamState) => state.modelParams
);

/**
 * Select filtered model parameters based on current filters
 * Requirements: 2.3, 2.4
 */
export const selectFilteredModelParams = createSelector(
  selectAllModelParams,
  selectModelParamState,
  (modelParams, state) => {
    let filtered = modelParams;

    // Apply model type filter
    if (state.filters.modelType) {
      filtered = filtered.filter(
        (mp) => mp.modelType === state.filters.modelType
      );
    }

    // Apply search term filter (case-insensitive)
    if (state.filters.searchTerm) {
      const searchLower = state.filters.searchTerm.toLowerCase();
      filtered = filtered.filter((mp) =>
        mp.modelName.toLowerCase().includes(searchLower)
      );
    }

    return filtered;
  }
);

/**
 * Select model parameter count
 */
export const selectModelParamCount = createSelector(
  selectAllModelParams,
  (modelParams) => modelParams.length
);

/**
 * Select filtered model parameter count
 */
export const selectFilteredModelParamCount = createSelector(
  selectFilteredModelParams,
  (modelParams) => modelParams.length
);

// ============================================
// Selection Selectors
// ============================================

/**
 * Select the currently selected model parameter
 */
export const selectSelectedModelParam = createSelector(
  selectModelParamState,
  (state: ModelParamState) => state.selectedModelParam
);

// ============================================
// Filter Selectors
// Requirements: 2.3, 2.4
// ============================================

/**
 * Select current filters
 */
export const selectFilters = createSelector(
  selectModelParamState,
  (state: ModelParamState) => state.filters
);

/**
 * Select model type filter
 */
export const selectModelTypeFilter = createSelector(
  selectFilters,
  (filters) => filters.modelType
);

/**
 * Select search term filter
 */
export const selectSearchTermFilter = createSelector(
  selectFilters,
  (filters) => filters.searchTerm
);

/**
 * Select whether filters are active
 */
export const selectHasActiveFilters = createSelector(
  selectFilters,
  (filters) => !!(filters.modelType || filters.searchTerm)
);

// ============================================
// Loading Selectors
// ============================================

/**
 * Select loading state
 */
export const selectLoading = createSelector(
  selectModelParamState,
  (state: ModelParamState) => state.loading
);

// ============================================
// Error Selectors
// ============================================

/**
 * Select error state
 */
export const selectError = createSelector(
  selectModelParamState,
  (state: ModelParamState) => state.error
);

/**
 * Select whether there is an error
 */
export const selectHasError = createSelector(
  selectError,
  (error) => error !== null
);

// ============================================
// Composite Selectors
// ============================================

/**
 * Select view model for the model parameter list component
 * Combines multiple selectors for efficient component binding
 */
export const selectModelParamListViewModel = createSelector(
  selectFilteredModelParams,
  selectLoading,
  selectError,
  selectFilters,
  selectHasActiveFilters,
  (modelParams, loading, error, filters, hasActiveFilters) => ({
    modelParams,
    loading,
    error,
    filters,
    hasActiveFilters,
  })
);
