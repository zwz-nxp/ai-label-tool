import { Injectable } from "@angular/core";
import { Actions, createEffect, ofType } from "@ngrx/effects";
import { Store } from "@ngrx/store";
import {
  catchError,
  map,
  of,
  switchMap,
  tap,
  withLatestFrom,
  forkJoin,
  mergeMap,
  from,
} from "rxjs";
import * as ModelDetailActions from "./model-detail.actions";
import * as ModelDetailSelectors from "./model-detail.selectors";
import { ModelDetailService } from "app/services/landingai/model-detail.service";
import { TrainingRecordService } from "app/services/landingai/training-record.service";
import { ConfidentialReportService } from "app/services/landingai/confidential-report.service";
import { LossChartService } from "app/services/landingai/loss-chart.service";
import { ValidationChartService } from "app/services/landingai/validation-chart.service";
import { ImageService } from "app/services/landingai/image.service";
import { generateCsvData, downloadCsv } from "app/models/landingai/csv-export";

@Injectable()
export class ModelDetailEffects {
  // Open Panel Effect - triggers data loading
  public openPanel$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.openPanel),
      mergeMap(({ modelId, initialTab }) => {
        // Trigger both model data and chart data loading if opening training tab
        const actions: any[] = [
          ModelDetailActions.loadModelData({
            modelId,
            trainingRecordId: 0, // Placeholder, will be updated in effect
          }),
        ];

        // If opening training tab, also load chart data
        if (initialTab === "training") {
          actions.push(ModelDetailActions.loadChartData({ modelId }));
        }

        return from(actions);
      })
    );
  });

  // Load Model Data Effect - loads model, training record, and confidential report in parallel
  public loadModelData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.loadModelData),
      switchMap(({ modelId }) =>
        this.modelDetailService.getModel(modelId).pipe(
          switchMap((model) => {
            // Model has trainingRecordId field
            const trainingRecordId = model.trainingRecordId;
            if (!trainingRecordId) {
              throw new Error("Training record ID not found for this model");
            }

            return forkJoin({
              model: of(model),
              trainingRecord:
                this.trainingRecordService.getTrainingRecord(trainingRecordId),
              confidentialReport:
                this.confidentialReportService.getConfidentialReport(modelId),
            }).pipe(
              map(({ model, trainingRecord, confidentialReport }) =>
                ModelDetailActions.loadModelDataSuccess({
                  model,
                  trainingRecord,
                  confidentialReport,
                })
              ),
              catchError((error) => {
                const errorMessage =
                  error?.message ||
                  "Failed to load model data, please try again later.";
                console.error("Failed to load model data:", error);
                return of(
                  ModelDetailActions.loadModelDataFailure({
                    error: errorMessage,
                  })
                );
              })
            );
          }),
          catchError((error) => {
            const errorMessage =
              error?.message || "Failed to load model, please try again later.";
            console.error("Failed to load model:", error);
            return of(
              ModelDetailActions.loadModelDataFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Load Chart Data Effect - triggered when switching to training tab
  public switchToTrainingTab$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.switchTab),
      withLatestFrom(
        this.store.select(ModelDetailSelectors.selectSelectedModelId),
        this.store.select(ModelDetailSelectors.selectModelDetailState)
      ),
      switchMap(([{ tab }, modelId, state]) => {
        // Only load chart data if switching to training tab and data not already loaded
        if (
          tab === "training" &&
          modelId &&
          (state.lossChartData.length === 0 ||
            state.validationChartData.length === 0)
        ) {
          return of(ModelDetailActions.loadChartData({ modelId }));
        }
        return of({ type: "NO_ACTION" });
      })
    );
  });

  // Load Chart Data Effect
  public loadChartData$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.loadChartData),
      switchMap(({ modelId }) =>
        forkJoin({
          lossChartData: this.lossChartService.getLossChartData(modelId),
          validationChartData:
            this.validationChartService.getValidationChartData(modelId),
        }).pipe(
          map(({ lossChartData, validationChartData }) =>
            ModelDetailActions.loadChartDataSuccess({
              lossChartData,
              validationChartData,
            })
          ),
          catchError((error) => {
            const errorMessage =
              error?.message ||
              "Failed to load chart data, please try again later.";
            console.error("Failed to load chart data:", error);
            return of(
              ModelDetailActions.loadChartDataFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Select Evaluation Set Effect - triggers image loading
  // TODO: Temporarily disabled until image API is implemented
  public selectEvaluationSet$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.selectEvaluationSet),
      withLatestFrom(this.store.select(ModelDetailSelectors.selectModelData)),
      switchMap(([{ evaluationSet }, modelData]) => {
        // Temporarily disabled - image loading will be implemented later
        // if (!modelData) {
        //   return of({ type: "NO_ACTION" });
        // }
        // return of(
        //   ModelDetailActions.loadImages({
        //     projectId: modelData.projectId,
        //     split: evaluationSet,
        //   })
        // );
        return of({ type: "NO_ACTION" });
      })
    );
  });

  // Load Images Effect
  public loadImages$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.loadImages),
      switchMap(({ projectId, split }) =>
        this.imageService.getImagesByProjectAndSplit(projectId, split).pipe(
          map((images) => ModelDetailActions.loadImagesSuccess({ images })),
          catchError((error) => {
            const errorMessage =
              error?.message ||
              "Failed to load images, please try again later.";
            console.error("Failed to load images:", error);
            return of(
              ModelDetailActions.loadImagesFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Rename Model Effect
  public renameModel$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.renameModel),
      switchMap(({ modelId, newAlias }) =>
        this.modelDetailService.updateModelAlias(modelId, newAlias).pipe(
          map((model) => ModelDetailActions.renameModelSuccess({ model })),
          catchError((error) => {
            const errorMessage =
              error?.message ||
              "Failed to rename model, please try again later.";
            console.error("Failed to rename model:", error);
            return of(
              ModelDetailActions.renameModelFailure({ error: errorMessage })
            );
          })
        )
      )
    );
  });

  // Export CSV Effect
  public exportCsv$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(ModelDetailActions.exportCsv),
      withLatestFrom(
        this.store.select(ModelDetailSelectors.selectModelData),
        this.store.select(ModelDetailSelectors.selectImages)
      ),
      tap(([{ modelId, split }, modelData, images]) => {
        try {
          if (!modelData) {
            throw new Error("No model data available for export");
          }

          // Generate CSV data (pass empty array if no images)
          const csvData = generateCsvData(images || [], modelData, split);

          // Generate filename with timestamp
          const timestamp = new Date().toISOString().replace(/[:.]/g, "-");
          const filename = `model_${modelId}_${split}_${timestamp}.csv`;

          // Download CSV
          downloadCsv(csvData, filename);

          // Dispatch success action
          this.store.dispatch(ModelDetailActions.exportCsvSuccess());
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "Failed to export CSV, please try again later.";
          console.error("Failed to export CSV:", error);
          this.store.dispatch(
            ModelDetailActions.exportCsvFailure({ error: errorMessage })
          );
        }
      }),
      map(() => ({ type: "NO_ACTION" }))
    );
  });

  public constructor(
    private actions$: Actions,
    private store: Store,
    private modelDetailService: ModelDetailService,
    private trainingRecordService: TrainingRecordService,
    private confidentialReportService: ConfidentialReportService,
    private lossChartService: LossChartService,
    private validationChartService: ValidationChartService,
    private imageService: ImageService
  ) {}
}
