import {
  ModelParam,
  ModelParamFilters,
} from "app/models/landingai/model-param.model";

/**
 * Main state interface for model parameter configuration
 * Requirements: 2.1, 2.3, 2.4
 * Validates: Requirements 2.1, 2.3, 2.4
 */
export interface ModelParamState {
  // Model parameter data
  modelParams: ModelParam[];
  selectedModelParam: ModelParam | null;

  // Loading state
  loading: boolean;

  // Error state
  error: string | null;

  // Filtering state
  filters: ModelParamFilters;
}

/**
 * Initial state for model parameter configuration
 */
export const modelParamInitialState: ModelParamState = {
  modelParams: [],
  selectedModelParam: null,
  loading: false,
  error: null,
  filters: {
    modelType: undefined,
    searchTerm: undefined,
  },
};
