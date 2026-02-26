import { createReducer, on } from "@ngrx/store";
import { ModelParamState, modelParamInitialState } from "./model-param.state";
import * as ModelParamActions from "./model-param.actions";

/**
 * Reducer for model parameter state management
 * Requirements: 2.1, 2.3, 2.4, 3.2, 4.2, 5.2
 */
export const modelParamReducer = createReducer(
  modelParamInitialState,

  // ============================================
  // Load Model Parameters
  // ============================================
  on(
    ModelParamActions.loadModelParams,
    (state): ModelParamState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    ModelParamActions.loadModelParamsSuccess,
    (state, { modelParams }): ModelParamState => ({
      ...state,
      loading: false,
      modelParams,
      error: null,
    })
  ),

  on(
    ModelParamActions.loadModelParamsFailure,
    (state, { error }): ModelParamState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================
  // Filter Actions
  // ============================================
  on(
    ModelParamActions.filterByModelType,
    (state, { modelType }): ModelParamState => ({
      ...state,
      filters: {
        ...state.filters,
        modelType: modelType || undefined,
      },
    })
  ),

  on(
    ModelParamActions.searchByModelName,
    (state, { searchTerm }): ModelParamState => ({
      ...state,
      filters: {
        ...state.filters,
        searchTerm: searchTerm || undefined,
      },
    })
  ),

  on(
    ModelParamActions.clearFilters,
    (state): ModelParamState => ({
      ...state,
      filters: {
        modelType: undefined,
        searchTerm: undefined,
      },
    })
  ),

  // ============================================
  // Create Model Parameter
  // ============================================
  on(
    ModelParamActions.createModelParam,
    (state): ModelParamState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    ModelParamActions.createModelParamSuccess,
    (state, { modelParam }): ModelParamState => ({
      ...state,
      loading: false,
      modelParams: [...state.modelParams, modelParam],
      error: null,
    })
  ),

  on(
    ModelParamActions.createModelParamFailure,
    (state, { error }): ModelParamState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================
  // Update Model Parameter
  // ============================================
  on(
    ModelParamActions.updateModelParam,
    (state): ModelParamState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    ModelParamActions.updateModelParamSuccess,
    (state, { modelParam }): ModelParamState => ({
      ...state,
      loading: false,
      modelParams: state.modelParams.map((mp) =>
        mp.id === modelParam.id ? modelParam : mp
      ),
      selectedModelParam:
        state.selectedModelParam?.id === modelParam.id
          ? modelParam
          : state.selectedModelParam,
      error: null,
    })
  ),

  on(
    ModelParamActions.updateModelParamFailure,
    (state, { error }): ModelParamState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================
  // Delete Model Parameter
  // ============================================
  on(
    ModelParamActions.deleteModelParam,
    (state): ModelParamState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),

  on(
    ModelParamActions.deleteModelParamSuccess,
    (state, { id }): ModelParamState => ({
      ...state,
      loading: false,
      modelParams: state.modelParams.filter((mp) => mp.id !== id),
      selectedModelParam:
        state.selectedModelParam?.id === id ? null : state.selectedModelParam,
      error: null,
    })
  ),

  on(
    ModelParamActions.deleteModelParamFailure,
    (state, { error }): ModelParamState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // ============================================
  // Selection Actions
  // ============================================
  on(
    ModelParamActions.selectModelParam,
    (state, { id }): ModelParamState => ({
      ...state,
      selectedModelParam: state.modelParams.find((mp) => mp.id === id) || null,
    })
  ),

  on(
    ModelParamActions.clearSelectedModelParam,
    (state): ModelParamState => ({
      ...state,
      selectedModelParam: null,
    })
  ),

  // ============================================
  // Clear Error
  // ============================================
  on(
    ModelParamActions.clearError,
    (state): ModelParamState => ({
      ...state,
      error: null,
    })
  )
);
