import { Component, OnInit, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import { TrainingRecord } from "app/models/landingai/training-record";
import { ChartData } from "app/models/landingai/loss-chart";
import { Model } from "app/models/landingai/model";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Training Information Tab Component
 * Container component for training information display
 * Requirements: 6.1, 6.2, 6.3, 22.3
 */
@Component({
  selector: "app-training-information-tab",
  standalone: false,
  templateUrl: "./training-information-tab.component.html",
  styleUrls: ["./training-information-tab.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TrainingInformationTabComponent implements OnInit {
  public modelData$: Observable<Model | null>;
  public trainingRecord$: Observable<TrainingRecord | null>;
  public lossChartData$: Observable<ChartData | null>;
  public validationChartData$: Observable<ChartData | null>;
  public isLoadingChartData$: Observable<boolean>;

  constructor(private store: Store) {
    this.modelData$ = this.store.select(ModelDetailSelectors.selectModelData);
    this.trainingRecord$ = this.store.select(
      ModelDetailSelectors.selectTrainingRecord
    );
    this.lossChartData$ = this.store.select(
      ModelDetailSelectors.selectLossChartData
    );
    this.validationChartData$ = this.store.select(
      ModelDetailSelectors.selectValidationChartData
    );
    this.isLoadingChartData$ = this.store.select(
      ModelDetailSelectors.selectIsLoadingChartData
    );
  }

  public ngOnInit(): void {}
}
