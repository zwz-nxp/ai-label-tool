import { createReducer, on } from "@ngrx/store";
import { Model } from "app/models/landingai/model";
import { TrainingRecord } from "app/models/landingai/training-record";
import { ConfidentialReport } from "app/models/landingai/confidential-report";
import { LossChart } from "app/models/landingai/loss-chart";
import { ValidationChart } from "app/models/landingai/validation-chart";
import { Image } from "app/models/landingai/image";
import * as ModelDetailActions from "./model-detail.actions";

export interface ModelDetailState {
  // Panel state
  isPanelOpen: boolean;
  panelWidth: "normal" | "expanded"; // 75% or 100%
  selectedModelId: number | null;
  activeTab: "training" | "performance";

  // Model data
  modelData: Model | null;
  trainingRecord: TrainingRecord | null;
  confidentialReport: ConfidentialReport | null;

  // Chart data
  lossChartData: LossChart[];
  validationChartData: ValidationChart[];

  // Performance report state
  selectedEvaluationSet: "train" | "dev" | "test";
  images: Image[];

  // Loading states
  loading: {
    modelData: boolean;
    chartData: boolean;
    images: boolean;
  };

  // Error states
  error: {
    modelData: string | null;
    chartData: string | null;
    images: string | null;
    rename: string | null;
    csvExport: string | null;
  };
}

export const modelDetailInitialState: ModelDetailState = {
  isPanelOpen: false,
  panelWidth: "normal",
  selectedModelId: null,
  activeTab: "training",
  modelData: null,
  trainingRecord: null,
  confidentialReport: null,
  lossChartData: [],
  validationChartData: [],
  selectedEvaluationSet: "train",
  images: [],
  loading: {
    modelData: false,
    chartData: false,
    images: false,
  },
  error: {
    modelData: null,
    chartData: null,
    images: null,
    rename: null,
    csvExport: null,
  },
};

export const modelDetailReducer = createReducer(
  modelDetailInitialState,

  // Panel Control
  on(
    ModelDetailActions.openPanel,
    (state, { modelId, initialTab }): ModelDetailState => ({
      ...state,
      isPanelOpen: true,
      selectedModelId: modelId,
      activeTab: initialTab,
      panelWidth: "normal",
    })
  ),
  on(
    ModelDetailActions.closePanel,
    (state): ModelDetailState => ({
      ...state,
      isPanelOpen: false,
    })
  ),
  on(
    ModelDetailActions.togglePanelWidth,
    (state): ModelDetailState => ({
      ...state,
      panelWidth: state.panelWidth === "normal" ? "expanded" : "normal",
    })
  ),

  // Tab Navigation
  on(
    ModelDetailActions.switchTab,
    (state, { tab }): ModelDetailState => ({
      ...state,
      activeTab: tab,
    })
  ),

  // Load Model Data
  on(
    ModelDetailActions.loadModelData,
    (state): ModelDetailState => ({
      ...state,
      loading: {
        ...state.loading,
        modelData: true,
      },
      error: {
        ...state.error,
        modelData: null,
      },
    })
  ),
  on(
    ModelDetailActions.loadModelDataSuccess,
    (
      state,
      { model, trainingRecord, confidentialReport }
    ): ModelDetailState => ({
      ...state,
      modelData: model,
      trainingRecord: trainingRecord,
      confidentialReport: confidentialReport,
      loading: {
        ...state.loading,
        modelData: false,
      },
      error: {
        ...state.error,
        modelData: null,
      },
    })
  ),
  on(
    ModelDetailActions.loadModelDataFailure,
    (state, { error }): ModelDetailState => ({
      ...state,
      loading: {
        ...state.loading,
        modelData: false,
      },
      error: {
        ...state.error,
        modelData: error,
      },
    })
  ),

  // Load Chart Data
  on(
    ModelDetailActions.loadChartData,
    (state): ModelDetailState => ({
      ...state,
      loading: {
        ...state.loading,
        chartData: true,
      },
      error: {
        ...state.error,
        chartData: null,
      },
    })
  ),
  on(
    ModelDetailActions.loadChartDataSuccess,
    (state, { lossChartData, validationChartData }): ModelDetailState => ({
      ...state,
      lossChartData: lossChartData,
      validationChartData: validationChartData,
      loading: {
        ...state.loading,
        chartData: false,
      },
      error: {
        ...state.error,
        chartData: null,
      },
    })
  ),
  on(
    ModelDetailActions.loadChartDataFailure,
    (state, { error }): ModelDetailState => ({
      ...state,
      loading: {
        ...state.loading,
        chartData: false,
      },
      error: {
        ...state.error,
        chartData: error,
      },
    })
  ),

  // Evaluation Set Selection
  on(
    ModelDetailActions.selectEvaluationSet,
    (state, { evaluationSet }): ModelDetailState => ({
      ...state,
      selectedEvaluationSet: evaluationSet,
    })
  ),

  // Load Images
  on(
    ModelDetailActions.loadImages,
    (state): ModelDetailState => ({
      ...state,
      loading: {
        ...state.loading,
        images: true,
      },
      error: {
        ...state.error,
        images: null,
      },
    })
  ),
  on(
    ModelDetailActions.loadImagesSuccess,
    (state, { images }): ModelDetailState => ({
      ...state,
      images: images,
      loading: {
        ...state.loading,
        images: false,
      },
      error: {
        ...state.error,
        images: null,
      },
    })
  ),
  on(
    ModelDetailActions.loadImagesFailure,
    (state, { error }): ModelDetailState => ({
      ...state,
      loading: {
        ...state.loading,
        images: false,
      },
      error: {
        ...state.error,
        images: error,
      },
    })
  ),

  // Rename Model
  on(ModelDetailActions.renameModel, (state): ModelDetailState => {
    return {
      ...state,
      error: {
        ...state.error,
        rename: null,
      },
    };
  }),
  on(
    ModelDetailActions.renameModelSuccess,
    (state, { model }): ModelDetailState => ({
      ...state,
      modelData: model,
      error: {
        ...state.error,
        rename: null,
      },
    })
  ),
  on(
    ModelDetailActions.renameModelFailure,
    (state, { error }): ModelDetailState => ({
      ...state,
      error: {
        ...state.error,
        rename: error,
      },
    })
  ),

  // CSV Export
  on(ModelDetailActions.exportCsv, (state): ModelDetailState => {
    return {
      ...state,
      error: {
        ...state.error,
        csvExport: null,
      },
    };
  }),
  on(ModelDetailActions.exportCsvSuccess, (state): ModelDetailState => {
    return {
      ...state,
      error: {
        ...state.error,
        csvExport: null,
      },
    };
  }),
  on(
    ModelDetailActions.exportCsvFailure,
    (state, { error }): ModelDetailState => ({
      ...state,
      error: {
        ...state.error,
        csvExport: error,
      },
    })
  ),

  // Clear Error
  on(
    ModelDetailActions.clearError,
    (state): ModelDetailState => ({
      ...state,
      error: {
        modelData: null,
        chartData: null,
        images: null,
        rename: null,
        csvExport: null,
      },
    })
  )
);
