import {
  Component,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  OnInit,
  OnDestroy,
} from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable, Subject, combineLatest, of } from "rxjs";
import { takeUntil, switchMap, tap, catchError } from "rxjs/operators";
import { ConfusionMatrixService } from "app/services/landingai/confusion-matrix.service";
import {
  ConfusionMatrixData,
  ConfusionMatrixState,
  CellSelection,
  ViewMode,
} from "app/models/landingai/confusion-matrix.model";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Confusion Matrix Component
 * Main container component for confusion matrix visualization
 * Requirements: 1.1, 1.2, 2.1, 2.2, 2.3
 */
@Component({
  selector: "app-confusion-matrix",
  standalone: false,
  templateUrl: "./confusion-matrix.component.html",
  styleUrls: ["./confusion-matrix.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfusionMatrixComponent implements OnInit, OnDestroy {
  // Observables from NgRx store
  public selectedModelId$: Observable<number | null>;
  public selectedEvaluationSet$: Observable<"train" | "dev" | "test">;

  // Component state
  public state: ConfusionMatrixState = {
    modelId: null,
    evaluationSet: "train",
    matrixData: null,
    selectedCell: null,
    viewMode: "matrix",
    loading: false,
    error: null,
  };

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private confusionMatrixService: ConfusionMatrixService,
    private cdr: ChangeDetectorRef
  ) {
    this.selectedModelId$ = this.store.select(
      ModelDetailSelectors.selectSelectedModelId
    );
    this.selectedEvaluationSet$ = this.store.select(
      ModelDetailSelectors.selectSelectedEvaluationSet
    );
  }

  public ngOnInit(): void {
    // Subscribe to model ID and evaluation set changes
    combineLatest([this.selectedModelId$, this.selectedEvaluationSet$])
      .pipe(
        takeUntil(this.destroy$),
        tap(([modelId, evaluationSet]) => {
          console.log(
            "Confusion Matrix - Model ID:",
            modelId,
            "Evaluation Set:",
            evaluationSet
          );
          this.state.modelId = modelId;
          this.state.evaluationSet = evaluationSet;
          this.cdr.markForCheck();
        }),
        switchMap(([modelId, evaluationSet]) => {
          if (modelId) {
            return this.loadMatrixData(modelId, evaluationSet);
          }
          this.state.loading = false;
          this.state.error = null;
          this.state.matrixData = null;
          this.cdr.markForCheck();
          return of(null);
        })
      )
      .subscribe();
  }

  public ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load confusion matrix data from backend
   * Requirements: 1.2, 9.1, 9.2
   */
  private loadMatrixData(
    modelId: number,
    evaluationSet: "train" | "dev" | "test"
  ): Observable<ConfusionMatrixData | null> {
    console.log(
      "Loading confusion matrix data for model:",
      modelId,
      "evaluation set:",
      evaluationSet
    );
    this.state.loading = true;
    this.state.error = null;
    this.cdr.markForCheck();

    return this.confusionMatrixService
      .getConfusionMatrix(modelId, evaluationSet)
      .pipe(
        tap((data) => {
          console.log("Confusion matrix data received:", data);
          this.state.matrixData = data;
          this.state.loading = false;
          this.state.error = null;
          this.cdr.markForCheck();
        }),
        catchError((error) => {
          console.error("Error loading confusion matrix:", error);
          this.state.loading = false;
          this.state.error = error.message || "Failed to load confusion matrix";
          this.state.matrixData = null;
          this.cdr.markForCheck();
          return of(null);
        })
      );
  }

  /**
   * Handle cell click event from grid component
   * Requirements: 6.1
   */
  public onCellClick(cell: CellSelection): void {
    this.state.selectedCell = cell;
    // No longer change viewMode - keep matrix visible
    this.cdr.markForCheck();
  }

  /**
   * Handle close detail panel
   * Requirements: 6.6
   */
  public onCloseDetail(): void {
    this.state.selectedCell = null;
    // No longer change viewMode
    this.cdr.markForCheck();
  }

  /**
   * Handle clear filter button click
   * Clears the selected cell and closes the detail panel
   */
  public onClearFilter(): void {
    this.state.selectedCell = null;
    this.cdr.markForCheck();
  }

  /**
   * Handle analyze all images button click
   * Requirements: 7.1
   */
  public onAnalyzeAll(): void {
    this.state.viewMode = "analyzeAll";
    this.cdr.markForCheck();
  }

  /**
   * Handle close analyze all images view
   * Requirements: 7.6
   */
  public onCloseAnalyzeAll(): void {
    this.state.viewMode = "matrix";
    this.cdr.markForCheck();
  }

  /**
   * Retry loading data after error
   * Requirements: 9.3
   */
  public onRetry(): void {
    if (this.state.modelId) {
      this.loadMatrixData(
        this.state.modelId,
        this.state.evaluationSet
      ).subscribe();
    }
  }
}
