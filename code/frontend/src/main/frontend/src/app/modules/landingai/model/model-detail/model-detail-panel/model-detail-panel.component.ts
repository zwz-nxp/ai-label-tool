import {
  Component,
  OnInit,
  OnDestroy,
  HostListener,
  ChangeDetectionStrategy,
} from "@angular/core";
import {
  trigger,
  state,
  style,
  transition,
  animate,
} from "@angular/animations";
import { Store } from "@ngrx/store";
import { Observable, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import * as ModelDetailActions from "app/state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Model Detail Panel Component
 * Container component that orchestrates the model detail panel display
 * Requirements: 1.1, 1.2, 2.1, 2.2, 2.3, 2.4, 21.2, 21.4, 22.1
 */
@Component({
  selector: "app-model-detail-panel",
  standalone: false,
  templateUrl: "./model-detail-panel.component.html",
  styleUrls: ["./model-detail-panel.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    // Panel slide-in/out animation
    trigger("slideIn", [
      transition(":enter", [
        style({ transform: "translateX(100%)" }),
        animate("300ms ease-in-out", style({ transform: "translateX(0)" })),
      ]),
      transition(":leave", [
        animate("300ms ease-in-out", style({ transform: "translateX(100%)" })),
      ]),
    ]),
    // Overlay fade-in/out animation
    trigger("fadeInOut", [
      transition(":enter", [
        style({ opacity: 0 }),
        animate("300ms ease-in-out", style({ opacity: 1 })),
      ]),
      transition(":leave", [
        animate("300ms ease-in-out", style({ opacity: 0 })),
      ]),
    ]),
  ],
})
export class ModelDetailPanelComponent implements OnInit, OnDestroy {
  public isPanelOpen$: Observable<boolean>;
  public panelWidth$: Observable<"normal" | "expanded">;
  public activeTab$: Observable<"training" | "performance">;
  public isLoading$: Observable<boolean>;
  public error$: Observable<string | null>;
  private selectedModelId: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(private store: Store) {
    this.isPanelOpen$ = this.store.select(
      ModelDetailSelectors.selectIsPanelOpen
    );
    this.panelWidth$ = this.store.select(ModelDetailSelectors.selectPanelWidth);
    this.activeTab$ = this.store.select(ModelDetailSelectors.selectActiveTab);
    this.isLoading$ = this.store.select(ModelDetailSelectors.selectIsLoading);
    this.error$ = this.store.select(ModelDetailSelectors.selectError);

    // Track selected model ID for retry
    this.store
      .select(ModelDetailSelectors.selectSelectedModelId)
      .pipe(takeUntil(this.destroy$))
      .subscribe((modelId) => {
        this.selectedModelId = modelId;
      });
  }

  public ngOnInit(): void {
    // Component initialization
  }

  public ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle Escape key to close panel
   * Requirements: 2.3
   */
  @HostListener("document:keydown.escape", ["$event"])
  public onEscapeKey(event: KeyboardEvent): void {
    this.store.dispatch(ModelDetailActions.closePanel());
  }

  /**
   * Close panel
   * Requirements: 2.2
   */
  public onClosePanel(): void {
    this.store.dispatch(ModelDetailActions.closePanel());
  }

  /**
   * Toggle panel width between 75% and 100%
   * Requirements: 21.2, 21.4
   */
  public onTogglePanelWidth(): void {
    this.store.dispatch(ModelDetailActions.togglePanelWidth());
  }

  /**
   * Handle overlay click to close panel
   * Requirements: 2.4
   */
  public onOverlayClick(): void {
    this.store.dispatch(ModelDetailActions.closePanel());
  }

  /**
   * Prevent click events from bubbling to overlay
   */
  public onPanelClick(event: Event): void {
    event.stopPropagation();
  }

  /**
   * Get user-friendly error message
   * Requirements: 23.1, 23.2, 23.3, 23.4, 23.5
   */
  public getErrorMessage(error: string): string {
    if (error.includes("Model not found") || error.includes("404")) {
      return "The requested model could not be found. It may have been deleted or you may not have permission to access it.";
    }
    if (error.includes("Training record not found")) {
      return "Training information is unavailable for this model. The model may not have completed training yet.";
    }
    if (error.includes("Confidential report")) {
      return "Performance report is unavailable for this model. Please try again later.";
    }
    if (
      error.includes("Network") ||
      error.includes("Connection") ||
      error.includes("timeout")
    ) {
      return "Unable to connect to the server. Please check your internet connection and try again.";
    }
    return error || "An unexpected error occurred. Please try again later.";
  }

  /**
   * Retry loading model data
   * Requirements: 23.5
   */
  public onRetry(): void {
    if (this.selectedModelId) {
      this.store.dispatch(
        ModelDetailActions.loadModelData({
          modelId: this.selectedModelId,
          trainingRecordId: 0, // Will be fetched from model
        })
      );
    }
  }
}
