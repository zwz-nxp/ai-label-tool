import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from "@angular/core";
import {
  CellSelection,
  ImageWithLabels,
} from "app/models/landingai/confusion-matrix.model";
import { ConfusionMatrixService } from "app/services/landingai/confusion-matrix.service";
import { ImageService } from "app/services/landingai/image.service";

/**
 * Detail Panel Component
 * Displays images for a selected confusion matrix cell
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.6, 6.7
 */
@Component({
  selector: "app-detail-panel",
  standalone: false,
  templateUrl: "./detail-panel.component.html",
  styleUrls: ["./detail-panel.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DetailPanelComponent implements OnInit, OnChanges {
  @Input() cellSelection!: CellSelection;
  @Input() modelId!: number;
  @Input() evaluationSet!: "train" | "dev" | "test";
  @Input() hideHeader: boolean = false; // New input to hide header
  @Output() close = new EventEmitter<void>();

  public images: ImageWithLabels[] = [];
  public selectedImage: ImageWithLabels | null = null;
  public loading = false;
  public error: string | null = null;
  public totalCount: number = 0; // Actual count of label pairs from backend

  constructor(
    private confusionMatrixService: ConfusionMatrixService,
    private imageService: ImageService,
    private cdr: ChangeDetectorRef
  ) {}

  public ngOnInit(): void {
    this.loadImages();
  }

  public ngOnChanges(changes: SimpleChanges): void {
    // Reload images when cellSelection, modelId, or evaluationSet changes
    if (
      changes["cellSelection"] ||
      changes["modelId"] ||
      changes["evaluationSet"]
    ) {
      // Only reload if not the first change (ngOnInit handles that)
      if (!changes["cellSelection"]?.firstChange) {
        this.loadImages();
      }
    }
  }

  /**
   * Load images for the selected cell
   * Requirements: 6.3
   */
  private loadImages(): void {
    this.loading = true;
    this.error = null;

    this.confusionMatrixService
      .getCellDetail(
        this.modelId,
        this.evaluationSet,
        this.cellSelection.gtClassId,
        this.cellSelection.predClassId
      )
      .subscribe({
        next: (response) => {
          this.images = response.images;
          this.totalCount = response.totalCount; // Use backend's label pair count
          this.loading = false;
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.error = error.message || "Failed to load images";
          this.loading = false;
          this.cdr.markForCheck();
        },
      });
  }

  /**
   * Handle image click
   * Requirements: 6.7
   */
  public onImageClick(image: ImageWithLabels): void {
    this.selectedImage = image;
  }

  /**
   * Close enlarged image view
   */
  public onCloseEnlargedView(): void {
    this.selectedImage = null;
  }

  /**
   * Handle close button click
   * Requirements: 6.6
   */
  public onClose(): void {
    this.close.emit();
  }

  /**
   * Retry loading images
   */
  public onRetry(): void {
    this.loadImages();
  }

  /**
   * Get image URL - returns mock image if in mock mode
   */
  public getImageUrl(imageId: number): string {
    if (this.imageService.isMockMode()) {
      return this.imageService.getMockImageUrl();
    }
    return `/api/landingai/images/${imageId}/file`;
  }
}
