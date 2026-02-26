import { Component, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import * as ModelDetailActions from "app/state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * CSV Export Button Component
 * Requirements: 20.1-20.15
 */
@Component({
  selector: "app-csv-export-button",
  standalone: false,
  template: `
    <button
      mat-raised-button
      color="primary"
      (click)="onExportCsv()"
      [disabled]="(imageCount$ | async) === 0"
    >
      <mat-icon>download</mat-icon>
      Download CSV
    </button>
  `,
  styles: [
    `
      button {
        margin: 16px;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CsvExportButtonComponent {
  public imageCount$: Observable<number>;
  public selectedModelId$: Observable<number | null>;
  public selectedEvaluationSet$: Observable<"train" | "dev" | "test">;

  constructor(private store: Store) {
    this.imageCount$ = this.store.select(ModelDetailSelectors.selectImageCount);
    this.selectedModelId$ = this.store.select(
      ModelDetailSelectors.selectSelectedModelId
    );
    this.selectedEvaluationSet$ = this.store.select(
      ModelDetailSelectors.selectSelectedEvaluationSet
    );
  }

  public onExportCsv(): void {
    // Get current values and dispatch export action
    this.selectedModelId$
      .subscribe((modelId) => {
        this.selectedEvaluationSet$
          .subscribe((split) => {
            if (modelId) {
              this.store.dispatch(
                ModelDetailActions.exportCsv({ modelId, split })
              );
            }
          })
          .unsubscribe();
      })
      .unsubscribe();
  }
}
