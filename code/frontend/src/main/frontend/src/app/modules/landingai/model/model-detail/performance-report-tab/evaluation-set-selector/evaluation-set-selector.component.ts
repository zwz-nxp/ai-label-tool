import { Component, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import * as ModelDetailActions from "app/state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Evaluation Set Selector Component
 * Requirements: 13.1, 13.2, 13.3, 13.4, 14.1-14.7, 15.1-15.4, 20.1
 */
@Component({
  selector: "app-evaluation-set-selector",
  standalone: false,
  template: `
    <div class="evaluation-set-selector">
      <mat-form-field appearance="outline">
        <mat-label>Evaluation Set</mat-label>
        <mat-select
          [value]="selectedEvaluationSet$ | async"
          (selectionChange)="onEvaluationSetChange($event.value)"
        >
          <mat-option value="train">Train set</mat-option>
          <mat-option value="dev">Dev set</mat-option>
          <mat-option value="test">Test set</mat-option>
        </mat-select>
      </mat-form-field>
      <div class="data-info">
        <span>{{ imageCount$ | async }} images</span>
      </div>
    </div>
  `,
  styles: [
    `
      .evaluation-set-selector {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
      }
      .data-info {
        color: #666;
        font-size: 14px;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EvaluationSetSelectorComponent {
  public selectedEvaluationSet$: Observable<"train" | "dev" | "test">;
  public imageCount$: Observable<number>;

  constructor(private store: Store) {
    this.selectedEvaluationSet$ = this.store.select(
      ModelDetailSelectors.selectSelectedEvaluationSet
    );
    this.imageCount$ = this.store.select(ModelDetailSelectors.selectImageCount);
  }

  public onEvaluationSetChange(evaluationSet: "train" | "dev" | "test"): void {
    this.store.dispatch(
      ModelDetailActions.selectEvaluationSet({ evaluationSet })
    );
  }
}
