import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import * as ModelDetailActions from "app/state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Model Detail Tabs Component
 * Provides tab navigation between Training Information and Performance Report
 * Requirements: 5.1, 5.2, 5.3, 5.4
 */
@Component({
  selector: "app-model-detail-tabs",
  standalone: false,
  templateUrl: "./model-detail-tabs.component.html",
  styleUrls: ["./model-detail-tabs.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModelDetailTabsComponent implements OnInit {
  public activeTab$: Observable<"training" | "performance">;

  constructor(private store: Store) {
    this.activeTab$ = this.store.select(ModelDetailSelectors.selectActiveTab);
  }

  public ngOnInit(): void {}

  /**
   * Switch to Training Information tab
   * Requirements: 5.2, 5.4
   */
  public switchToTrainingTab(): void {
    this.store.dispatch(ModelDetailActions.switchTab({ tab: "training" }));
  }

  /**
   * Switch to Performance Report tab
   * Requirements: 5.3, 5.4
   */
  public switchToPerformanceTab(): void {
    this.store.dispatch(ModelDetailActions.switchTab({ tab: "performance" }));
  }
}
