import { createFeatureSelector, createSelector } from "@ngrx/store";
import { ModelDetailState } from "./model-detail.reducer";
import { ChartData } from "app/models/landingai/loss-chart";
import { transformLossChartData } from "app/models/landingai/loss-chart";
import { transformValidationChartData } from "app/models/landingai/validation-chart";

export const selectModelDetailState =
  createFeatureSelector<ModelDetailState>("modelDetail");

// Panel State Selectors
export const selectIsPanelOpen = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.isPanelOpen
);

export const selectPanelWidth = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.panelWidth
);

export const selectSelectedModelId = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.selectedModelId
);

export const selectActiveTab = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.activeTab
);

// Model Data Selectors
export const selectModelData = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.modelData
);

export const selectTrainingRecord = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.trainingRecord
);

export const selectConfidentialReport = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.confidentialReport
);

// Chart Data Selectors
export const selectLossChartData = createSelector(
  selectModelDetailState,
  selectTrainingRecord,
  (state: ModelDetailState, trainingRecord) => {
    if (!trainingRecord || !trainingRecord.startedAt) {
      return null;
    }
    // Convert startedAt to Date if it's a string
    const startedAt =
      trainingRecord.startedAt instanceof Date
        ? trainingRecord.startedAt
        : new Date(trainingRecord.startedAt);
    return transformLossChartData(state.lossChartData, startedAt);
  }
);

export const selectValidationChartData = createSelector(
  selectModelDetailState,
  selectTrainingRecord,
  (state: ModelDetailState, trainingRecord) => {
    if (!trainingRecord || !trainingRecord.startedAt) {
      return null;
    }
    // Convert startedAt to Date if it's a string
    const startedAt =
      trainingRecord.startedAt instanceof Date
        ? trainingRecord.startedAt
        : new Date(trainingRecord.startedAt);
    return transformValidationChartData(state.validationChartData, startedAt);
  }
);

// Performance Report Selectors
export const selectSelectedEvaluationSet = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.selectedEvaluationSet
);

export const selectImages = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.images
);

// Current Metrics based on selected evaluation set
export const selectCurrentMetrics = createSelector(
  selectModelData,
  selectSelectedEvaluationSet,
  (modelData, evaluationSet) => {
    if (!modelData) {
      return { f1: null, precision: null, recall: null };
    }

    // Convert from 0-100 range to 0-1 range
    const convertMetric = (value: number | undefined): number | null => {
      return value !== undefined ? value / 100 : null;
    };

    switch (evaluationSet) {
      case "train":
        return {
          f1: convertMetric(modelData.trainingF1Rate),
          precision: convertMetric(modelData.trainingPrecisionRate),
          recall: convertMetric(modelData.trainingRecallRate),
        };
      case "dev":
        return {
          f1: convertMetric(modelData.devF1Rate),
          precision: convertMetric(modelData.devPrecisionRate),
          recall: convertMetric(modelData.devRecallRate),
        };
      case "test":
        return {
          f1: convertMetric(modelData.testF1Rate),
          precision: convertMetric(modelData.testPrecisionRate),
          recall: convertMetric(modelData.testRecallRate),
        };
      default:
        return { f1: null, precision: null, recall: null };
    }
  }
);

// Image count based on selected evaluation set
export const selectImageCount = createSelector(
  selectTrainingRecord,
  selectSelectedEvaluationSet,
  (trainingRecord, evaluationSet) => {
    if (!trainingRecord) {
      return 0;
    }

    switch (evaluationSet) {
      case "train":
        return trainingRecord.trainingCount;
      case "dev":
        return trainingRecord.devCount;
      case "test":
        return trainingRecord.testCount;
      default:
        return 0;
    }
  }
);

// Loading State Selectors
export const selectIsLoadingModelData = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.loading.modelData
);

export const selectIsLoadingChartData = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.loading.chartData
);

export const selectIsLoadingImages = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.loading.images
);

export const selectIsLoading = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) =>
    state.loading.modelData || state.loading.chartData || state.loading.images
);

// Error State Selectors
export const selectModelDataError = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.error.modelData
);

export const selectChartDataError = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.error.chartData
);

export const selectImagesError = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.error.images
);

export const selectRenameError = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.error.rename
);

export const selectCsvExportError = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) => state.error.csvExport
);

export const selectError = createSelector(
  selectModelDetailState,
  (state: ModelDetailState) =>
    state.error.modelData ||
    state.error.chartData ||
    state.error.images ||
    state.error.rename ||
    state.error.csvExport
);

// Combined Data Selector for convenience
export const selectAllModelDetailData = createSelector(
  selectModelData,
  selectTrainingRecord,
  selectConfidentialReport,
  selectLossChartData,
  selectValidationChartData,
  (model, trainingRecord, confidentialReport, lossChart, validationChart) => ({
    model,
    trainingRecord,
    confidentialReport,
    lossChart,
    validationChart,
  })
);
