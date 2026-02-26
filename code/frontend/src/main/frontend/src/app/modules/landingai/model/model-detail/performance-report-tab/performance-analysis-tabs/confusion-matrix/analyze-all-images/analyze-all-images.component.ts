import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from "@angular/core";
import { ImageWithLabels } from "app/models/landingai/confusion-matrix.model";
import { ConfusionMatrixService } from "app/services/landingai/confusion-matrix.service";
import { ImageService } from "app/services/landingai/image.service";

/**
 * Analyze All Images Component
 * Displays all images in evaluation set with correctness indicators
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6
 */
@Component({
  selector: "app-analyze-all-images",
  standalone: false,
  templateUrl: "./analyze-all-images.component.html",
  styleUrls: ["./analyze-all-images.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalyzeAllImagesComponent implements OnInit {
  @Input() modelId!: number;
  @Input() evaluationSet!: "train" | "dev" | "test";
  @Output() close = new EventEmitter<void>();

  public images: ImageWithLabels[] = [];
  public selectedImage: ImageWithLabels | null = null;
  public loading = false;
  public error: string | null = null;

  // Filter state
  public filterCorrect: boolean | null = null; // null = all, true = correct, false = incorrect
  public filteredImages: ImageWithLabels[] = [];

  constructor(
    private confusionMatrixService: ConfusionMatrixService,
    private imageService: ImageService,
    private cdr: ChangeDetectorRef
  ) {}

  public ngOnInit(): void {
    this.loadAllImages();
  }

  /**
   * Load all images in evaluation set
   * Requirements: 7.2
   */
  private loadAllImages(): void {
    this.loading = true;
    this.error = null;

    this.confusionMatrixService
      .getAllImages(this.modelId, this.evaluationSet)
      .subscribe({
        next: (images) => {
          this.images = images;
          this.applyFilter();
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
   * Apply filter to images
   */
  private applyFilter(): void {
    if (this.filterCorrect === null) {
      this.filteredImages = this.images;
    } else {
      this.filteredImages = this.images.filter(
        (img) => img.isCorrect === this.filterCorrect
      );
    }
  }

  /**
   * Handle filter change
   */
  public onFilterChange(filter: boolean | null): void {
    this.filterCorrect = filter;
    this.applyFilter();
    this.cdr.markForCheck();
  }

  /**
   * Get count of correct predictions
   */
  public getCorrectCount(): number {
    return this.images.filter((img) => img.isCorrect).length;
  }

  /**
   * Get count of incorrect predictions
   */
  public getIncorrectCount(): number {
    return this.images.filter((img) => !img.isCorrect).length;
  }

  /**
   * Handle image click
   * Requirements: 7.5
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
   * Requirements: 7.6
   */
  public onClose(): void {
    this.close.emit();
  }

  /**
   * Retry loading images
   */
  public onRetry(): void {
    this.loadAllImages();
  }

  /**
   * Get ground truth class name for image
   */
  public getGroundTruthClassName(image: ImageWithLabels): string {
    if (image.groundTruthLabels.length > 0) {
      return image.groundTruthLabels[0].className;
    }
    return "Unknown";
  }

  /**
   * Get prediction class name for image
   */
  public getPredictionClassName(image: ImageWithLabels): string {
    if (image.predictionLabels.length > 0) {
      return image.predictionLabels[0].className;
    }
    return "No prediction";
  }

  /**
   * Get confidence for image
   */
  public getConfidence(image: ImageWithLabels): string {
    if (
      image.predictionLabels.length > 0 &&
      image.predictionLabels[0].confidenceRate !== null
    ) {
      return image.predictionLabels[0].confidenceRate.toFixed(1) + "%";
    }
    return "N/A";
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
