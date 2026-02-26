import { createAction, props } from "@ngrx/store";
import { Model } from "app/models/landingai/model";
import { TrainingRecord } from "app/models/landingai/training-record";
import { ConfidentialReport } from "app/models/landingai/confidential-report";
import { LossChart } from "app/models/landingai/loss-chart";
import { ValidationChart } from "app/models/landingai/validation-chart";
import { Image } from "app/models/landingai/image";

// Panel Control Actions
export const openPanel = createAction(
  "[Model Detail] Open Panel",
  props<{ modelId: number; initialTab: "training" | "performance" }>()
);

export const closePanel = createAction("[Model Detail] Close Panel");

export const togglePanelWidth = createAction(
  "[Model Detail] Toggle Panel Width"
);

// Tab Navigation Actions
export const switchTab = createAction(
  "[Model Detail] Switch Tab",
  props<{ tab: "training" | "performance" }>()
);

// Load Model Data Actions
export const loadModelData = createAction(
  "[Model Detail] Load Model Data",
  props<{ modelId: number; trainingRecordId: number }>()
);

export const loadModelDataSuccess = createAction(
  "[Model Detail] Load Model Data Success",
  props<{
    model: Model;
    trainingRecord: TrainingRecord;
    confidentialReport: ConfidentialReport;
  }>()
);

export const loadModelDataFailure = createAction(
  "[Model Detail] Load Model Data Failure",
  props<{ error: string }>()
);

// Load Chart Data Actions
export const loadChartData = createAction(
  "[Model Detail] Load Chart Data",
  props<{ modelId: number }>()
);

export const loadChartDataSuccess = createAction(
  "[Model Detail] Load Chart Data Success",
  props<{
    lossChartData: LossChart[];
    validationChartData: ValidationChart[];
  }>()
);

export const loadChartDataFailure = createAction(
  "[Model Detail] Load Chart Data Failure",
  props<{ error: string }>()
);

// Evaluation Set Actions
export const selectEvaluationSet = createAction(
  "[Model Detail] Select Evaluation Set",
  props<{ evaluationSet: "train" | "dev" | "test" }>()
);

// Load Images Actions
export const loadImages = createAction(
  "[Model Detail] Load Images",
  props<{ projectId: number; split: string }>()
);

export const loadImagesSuccess = createAction(
  "[Model Detail] Load Images Success",
  props<{ images: Image[] }>()
);

export const loadImagesFailure = createAction(
  "[Model Detail] Load Images Failure",
  props<{ error: string }>()
);

// Rename Model Actions
export const renameModel = createAction(
  "[Model Detail] Rename Model",
  props<{ modelId: number; newAlias: string }>()
);

export const renameModelSuccess = createAction(
  "[Model Detail] Rename Model Success",
  props<{ model: Model }>()
);

export const renameModelFailure = createAction(
  "[Model Detail] Rename Model Failure",
  props<{ error: string }>()
);

// CSV Export Actions
export const exportCsv = createAction(
  "[Model Detail] Export CSV",
  props<{ modelId: number; split: string }>()
);

export const exportCsvSuccess = createAction(
  "[Model Detail] Export CSV Success"
);

export const exportCsvFailure = createAction(
  "[Model Detail] Export CSV Failure",
  props<{ error: string }>()
);

// Clear Error Action
export const clearError = createAction("[Model Detail] Clear Error");
