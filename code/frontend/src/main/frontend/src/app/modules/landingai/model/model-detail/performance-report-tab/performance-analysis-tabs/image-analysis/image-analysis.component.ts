import { Component, ChangeDetectionStrategy } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import { Image } from "app/models/landingai/image";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Image Analysis Component
 * Requirements: 19.1-19.7, 22.5
 */
@Component({
  selector: "app-image-analysis",
  standalone: false,
  template: `
    <div class="image-analysis">
      <!-- Loading State -->
      <div *ngIf="(images$ | async) === null" class="loading-state">
        <mat-spinner diameter="40"></mat-spinner>
        <p>Loading images...</p>
      </div>

      <!-- No Images State -->
      <div *ngIf="(images$ | async)?.length === 0" class="no-images">
        <mat-icon>image_not_supported</mat-icon>
        <p>No images available for this split</p>
      </div>

      <!-- Images Grid with Placeholders -->
      <div *ngIf="(images$ | async)?.length ?? 0 > 0" class="images-container">
        <div class="image-count">{{ (images$ | async)?.length }} images</div>
        <div class="images-grid">
          <div *ngFor="let image of images$ | async" class="image-item">
            <div class="image-placeholder">
              <mat-icon>image</mat-icon>
              <span class="image-id">ID: {{ image.id }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .image-analysis {
        padding: 24px;
      }
      .loading-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 300px;
        gap: 16px;
      }
      .loading-state p {
        color: #666;
        font-size: 14px;
        margin: 0;
      }
      .no-images {
        text-align: center;
        color: #999;
        padding: 60px 20px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16px;
      }
      .no-images mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
      }
      .images-container {
        padding: 16px 0;
      }
      .image-count {
        font-size: 14px;
        color: #666;
        margin-bottom: 16px;
        font-weight: 500;
      }
      .images-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 16px;
      }
      .image-item {
        aspect-ratio: 1;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        overflow: hidden;
      }
      .image-placeholder {
        width: 100%;
        height: 100%;
        background: #f5f5f5;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
      }
      .image-placeholder mat-icon {
        font-size: 48px;
        width: 48px;
        height: 48px;
        color: #ccc;
      }
      .image-id {
        font-size: 11px;
        color: #999;
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageAnalysisComponent {
  public images$: Observable<Image[]>;

  constructor(private store: Store) {
    this.images$ = this.store.select(ModelDetailSelectors.selectImages);
  }
}
