import { Component, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Performance Analysis Tabs Component
 * Requirements: 17.1, 17.2, 17.3
 */
@Component({
  selector: "app-performance-analysis-tabs",
  standalone: false,
  templateUrl: "./performance-analysis-tabs.component.html",
  styleUrls: ["./performance-analysis-tabs.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerformanceAnalysisTabsComponent {
  public selectedModelId$: Observable<number | null>;
  public selectedEvaluationSet$: Observable<"train" | "dev" | "test">;

  constructor(private store: Store) {
    this.selectedModelId$ = this.store.select(
      ModelDetailSelectors.selectSelectedModelId
    );
    this.selectedEvaluationSet$ = this.store.select(
      ModelDetailSelectors.selectSelectedEvaluationSet
    );
  }
}
