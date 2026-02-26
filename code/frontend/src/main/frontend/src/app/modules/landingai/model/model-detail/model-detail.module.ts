import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";

// Angular Material Modules
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatTabsModule } from "@angular/material/tabs";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSelectModule } from "@angular/material/select";
import { MatDialogModule } from "@angular/material/dialog";
import { MatSliderModule } from "@angular/material/slider";
import { MatSnackBarModule } from "@angular/material/snack-bar";

// NgRx
import { StoreModule } from "@ngrx/store";
import { EffectsModule } from "@ngrx/effects";
import { modelDetailReducer } from "app/state/landingai/model/model-detail/model-detail.reducer";
import { ModelDetailEffects } from "app/state/landingai/model/model-detail/model-detail.effects";

// Components
import { ModelDetailPanelComponent } from "./model-detail-panel/model-detail-panel.component";
import { ModelDetailHeaderComponent } from "./model-detail-header/model-detail-header.component";
import { ModelDetailTabsComponent } from "./model-detail-tabs/model-detail-tabs.component";
import { TrainingInformationTabComponent } from "./training-information-tab/training-information-tab.component";
import { PerformanceReportTabComponent } from "./performance-report-tab/performance-report-tab.component";

// Training Information Tab Components
import { LossChartComponent } from "./training-information-tab/loss-chart/loss-chart.component";
import { ValidationChartComponent } from "./training-information-tab/validation-chart/validation-chart.component";
import { TrainedFromComponent } from "./training-information-tab/trained-from/trained-from.component";
import { SplitDistributionComponent } from "./training-information-tab/split-distribution/split-distribution.component";
import { TrainedAtComponent } from "./training-information-tab/trained-at/trained-at.component";
import { TrainedByComponent } from "./training-information-tab/trained-by/trained-by.component";
import { HyperparametersComponent } from "./training-information-tab/hyperparameters/hyperparameters.component";

// Performance Report Tab Components
import { EvaluationSetSelectorComponent } from "./performance-report-tab/evaluation-set-selector/evaluation-set-selector.component";
import { PerformanceMetricsComponent } from "./performance-report-tab/performance-metrics/performance-metrics.component";
import { CsvExportButtonComponent } from "./performance-report-tab/csv-export-button/csv-export-button.component";
import { PerformanceAnalysisTabsComponent } from "./performance-report-tab/performance-analysis-tabs/performance-analysis-tabs.component";
import { ConfusionMatrixComponent } from "./performance-report-tab/performance-analysis-tabs/confusion-matrix/confusion-matrix.component";
import { ConfusionMatrixGridComponent } from "./performance-report-tab/performance-analysis-tabs/confusion-matrix/confusion-matrix-grid/confusion-matrix-grid.component";
import { DetailPanelComponent } from "./performance-report-tab/performance-analysis-tabs/confusion-matrix/detail-panel/detail-panel.component";
import { AnalyzeAllImagesComponent } from "./performance-report-tab/performance-analysis-tabs/confusion-matrix/analyze-all-images/analyze-all-images.component";
import { ImageAnalysisComponent } from "./performance-report-tab/performance-analysis-tabs/image-analysis/image-analysis.component";

// Adjust Threshold Dialog Components
import { AdjustThresholdDialogComponent } from "./performance-report-tab/adjust-threshold-dialog/adjust-threshold-dialog.component";
import { ThresholdSliderComponent } from "./performance-report-tab/adjust-threshold-dialog/threshold-slider/threshold-slider.component";
import { DynamicConfusionMatrixComponent } from "./performance-report-tab/adjust-threshold-dialog/dynamic-confusion-matrix/dynamic-confusion-matrix.component";

/**
 * Model Detail Module
 * Provides model detail panel functionality
 * Requirements: 1.1
 */
@NgModule({
  declarations: [
    ModelDetailPanelComponent,
    ModelDetailHeaderComponent,
    ModelDetailTabsComponent,
    TrainingInformationTabComponent,
    PerformanceReportTabComponent,
    // Training Information Tab Components
    LossChartComponent,
    ValidationChartComponent,
    TrainedFromComponent,
    SplitDistributionComponent,
    TrainedAtComponent,
    TrainedByComponent,
    HyperparametersComponent,
    // Performance Report Tab Components
    EvaluationSetSelectorComponent,
    PerformanceMetricsComponent,
    CsvExportButtonComponent,
    PerformanceAnalysisTabsComponent,
    ConfusionMatrixComponent,
    ConfusionMatrixGridComponent,
    DetailPanelComponent,
    AnalyzeAllImagesComponent,
    ImageAnalysisComponent,
    // Adjust Threshold Dialog Components
    AdjustThresholdDialogComponent,
    ThresholdSliderComponent,
    DynamicConfusionMatrixComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    // Angular Material
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatDialogModule,
    MatSliderModule,
    MatSnackBarModule,
    // NgRx
    StoreModule.forFeature("modelDetail", modelDetailReducer),
    EffectsModule.forFeature([ModelDetailEffects]),
  ],
  exports: [ModelDetailPanelComponent],
})
export class ModelDetailModule {}
