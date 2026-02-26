import { createAction, props } from "@ngrx/store";
import {
  ModelParam,
  ModelParamCreateRequest,
  ModelParamUpdateRequest,
  ModelType,
} from "app/models/landingai/model-param.model";

// ============================================
// Load Model Parameters Actions
// Requirements: 2.1
// ============================================

export const loadModelParams = createAction(
  "[Model Param] Load Model Params",
  props<{ locationId: number }>()
);

export const loadModelParamsSuccess = createAction(
  "[Model Param] Load Model Params Success",
  props<{ modelParams: ModelParam[] }>()
);

export const loadModelParamsFailure = createAction(
  "[Model Param] Load Model Params Failure",
  props<{ error: string }>()
);

// ============================================
// Filter Actions
// Requirements: 2.3, 2.4
// ============================================

export const filterByModelType = createAction(
  "[Model Param] Filter By Model Type",
  props<{ modelType: ModelType | null }>()
);

export const searchByModelName = createAction(
  "[Model Param] Search By Model Name",
  props<{ searchTerm: string }>()
);

export const clearFilters = createAction("[Model Param] Clear Filters");

// ============================================
// Create Model Parameter Actions
// Requirements: 3.2
// ============================================

export const createModelParam = createAction(
  "[Model Param] Create Model Param",
  props<{
    request: ModelParamCreateRequest;
    locationId: number;
    userId: string;
  }>()
);

export const createModelParamSuccess = createAction(
  "[Model Param] Create Model Param Success",
  props<{ modelParam: ModelParam }>()
);

export const createModelParamFailure = createAction(
  "[Model Param] Create Model Param Failure",
  props<{ error: string }>()
);

// ============================================
// Update Model Parameter Actions
// Requirements: 4.2
// ============================================

export const updateModelParam = createAction(
  "[Model Param] Update Model Param",
  props<{
    id: number;
    request: ModelParamUpdateRequest;
    userId: string;
  }>()
);

export const updateModelParamSuccess = createAction(
  "[Model Param] Update Model Param Success",
  props<{ modelParam: ModelParam }>()
);

export const updateModelParamFailure = createAction(
  "[Model Param] Update Model Param Failure",
  props<{ error: string }>()
);

// ============================================
// Delete Model Parameter Actions
// Requirements: 5.2
// ============================================

export const deleteModelParam = createAction(
  "[Model Param] Delete Model Param",
  props<{ id: number; userId: string }>()
);

export const deleteModelParamSuccess = createAction(
  "[Model Param] Delete Model Param Success",
  props<{ id: number }>()
);

export const deleteModelParamFailure = createAction(
  "[Model Param] Delete Model Param Failure",
  props<{ error: string }>()
);

// ============================================
// Selection Actions
// ============================================

export const selectModelParam = createAction(
  "[Model Param] Select Model Param",
  props<{ id: number }>()
);

export const clearSelectedModelParam = createAction(
  "[Model Param] Clear Selected Model Param"
);

// ============================================
// Clear Error Action
// ============================================

export const clearError = createAction("[Model Param] Clear Error");
