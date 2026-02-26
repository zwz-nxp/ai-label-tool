import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Store } from "@ngrx/store";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { combineLatest, Observable, Subject, take, takeUntil, tap } from "rxjs";
import {
  ProjectListItem,
  ProjectType,
} from "../../../models/landingai/project";
import { ProjectClass } from "../../../models/landingai/project-class.model";
import { ProjectTag } from "../../../models/landingai/project-tag.model";
import { ProjectMetadata } from "../../../models/landingai/project-metadata.model";
import { Model } from "../../../models/landingai/model";
import { Image } from "../../../models/landingai/image";
import * as HomeSelectors from "../../../state/landingai/home/home.selectors";
import * as HomeActions from "../../../state/landingai/home/home.actions";
import * as ImageUploadSelectors from "../../../state/landingai/image-upload/image-upload.selectors";
import * as ImageUploadActions from "../../../state/landingai/image-upload/image-upload.actions";
import {
  FilterState,
  SortMethod,
  ViewMode,
} from "../../../state/landingai/image-upload/image-upload.actions";
import {
  ActionType,
  SnapshotActionType,
  TrainingActionType,
} from "./toolbar/toolbar.component";
import { UploadDialogComponent } from "./upload-dialog/upload-dialog.component";
import { UploadClassifiedDialogComponent } from "./upload-classified-dialog/upload-classified-dialog.component";
import { UploadBatchDialogComponent } from "./upload-batch-dialog/upload-batch-dialog.component";
import { SnapshotCreateDialogComponent } from "./snapshot-create-dialog/snapshot-create-dialog.component";
import { SnapshotListDialogComponent } from "./snapshot-list-dialog/snapshot-list-dialog.component";
import { ManageClassesDialogComponent } from "./manage-classes-dialog/manage-classes-dialog.component";
import { ManageTagsDialogComponent } from "./manage-tags-dialog/manage-tags-dialog.component";
import { ManageMetadataDialogComponent } from "./manage-metadata-dialog/manage-metadata-dialog.component";
import { AutoSplitDialogComponent } from "./auto-split-dialog/auto-split-dialog.component";
import { SnapshotService } from "../../../services/landingai/snapshot.service";
import { TrainingService } from "../../../services/landingai/training.service";
import { ProjectClassService } from "../../../services/landingai/project-class.service";
import { ProjectTagService } from "../../../services/landingai/project-tag.service";
import { ProjectMetadataService } from "../../../services/landingai/project-metadata.service";
import { ModelService } from "../../../services/landingai/model.service";
import { ImageService } from "../../../services/landingai/image.service";
import {
  ImageLabel as LabelServiceImageLabel,
  LabelService,
} from "../../../services/landingai/label.service";
import { BatchSetMetadataDialogComponent } from "./batch-set-metadata-dialog/batch-set-metadata-dialog.component";
import { DownloadProgressDialogComponent } from "./download-progress-dialog/download-progress-dialog.component";
import { BatchSetTagsDialogComponent } from "./batch-set-tags-dialog/batch-set-tags-dialog.component";
import { BatchSetClassDialogComponent } from "./batch-set-class-dialog/batch-set-class-dialog.component";
import { ConfirmDialogComponent } from "../../../components/dialogs/confirm-dialog/confirm-dialog.component";
import { ClassChangeEvent } from "./image-card/image-card.component";

@Component({
  selector: "app-image-upload-page",
  standalone: false,
  templateUrl: "./image-upload-page.component.html",
  styleUrls: ["./image-upload-page.component.scss"],
})
export class ImageUploadPageComponent implements OnInit, OnDestroy {
  projectId!: number;
  project$!: Observable<ProjectListItem | undefined>;

  // Image upload state observables
  images$: Observable<Image[]>;
  viewMode$: Observable<ViewMode>;
  filters$: Observable<FilterState>;
  sortMethod$: Observable<SortMethod>;
  zoomLevel$: Observable<number>;
  loading$: Observable<boolean>;
  currentPage$: Observable<number>;
  totalPages$: Observable<number>;
  totalElements$: Observable<number>;
  error$: Observable<string | null>;

  // Filter options
  availableClasses: ProjectClass[] = [];
  availableTags: ProjectTag[] = [];
  availableMetadata: ProjectMetadata[] = [];
  availableModels: Model[] = [];
  selectedModelId: number | undefined;
  showLabelDetails: boolean = true;

  // Selection state
  selectMode: boolean = false;
  selectedImageIds: Set<number> = new Set();
  currentPageImageIds: number[] = [];
  lastSelectAction: "manual" | "page" | "all" | "none" = "none";

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private store: Store,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private snapshotService: SnapshotService,
    private trainingService: TrainingService,
    private projectClassService: ProjectClassService,
    private projectTagService: ProjectTagService,
    private projectMetadataService: ProjectMetadataService,
    private modelService: ModelService,
    private imageService: ImageService,
    private labelService: LabelService
  ) {
    // Initialize observables from image upload state
    this.images$ = this.store.select(ImageUploadSelectors.selectImages);
    this.viewMode$ = this.store.select(ImageUploadSelectors.selectViewMode);
    this.filters$ = this.store.select(ImageUploadSelectors.selectFilters);
    this.sortMethod$ = this.store.select(ImageUploadSelectors.selectSortMethod);
    this.zoomLevel$ = this.store.select(ImageUploadSelectors.selectZoomLevel);
    this.loading$ = this.store.select(ImageUploadSelectors.selectLoading);
    this.currentPage$ = this.store.select(
      ImageUploadSelectors.selectCurrentPage
    );
    this.totalPages$ = this.store.select(ImageUploadSelectors.selectTotalPages);
    this.totalElements$ = this.store.select(
      ImageUploadSelectors.selectTotalElements
    );
    this.error$ = this.store.select(ImageUploadSelectors.selectError);
  }

  ngOnInit(): void {
    // Load projects to ensure project data is available
    this.store.dispatch(HomeActions.loadProjects({ viewAll: true }));

    // Get project ID from route parameter and query params for state restoration
    combineLatest([this.route.params, this.route.queryParams])
      .pipe(
        takeUntil(this.destroy$),
        tap(([params, queryParams]) => {
          this.projectId = +params["id"];

          // Load project details from home state
          this.project$ = this.store.select(
            HomeSelectors.selectProjectById(this.projectId)
          );

          // Check if we have a modelId in query params and set it BEFORE loading filter options
          // This prevents loadFilterOptions from overwriting it
          if (queryParams["modelId"]) {
            this.selectedModelId = +queryParams["modelId"];
            console.log(
              "[ngOnInit] ✓ Pre-setting modelId from query params:",
              this.selectedModelId
            );
          }

          // Load filter options (classes, tags, metadata, models) first
          this.loadFilterOptions();

          // Check if we have query params from labeling page or snapshot list navigation
          if (queryParams && Object.keys(queryParams).length > 0) {
            console.log("Restoring state from query params:", queryParams);
            this.loadImagesFromQueryParams(queryParams);
          } else {
            // For fresh page load, wait for models to load before loading images
            // This ensures selectedModelId is set before the first image load
            console.log(
              "[ngOnInit] Fresh page load - waiting for models to load"
            );
            // Images will be loaded after models are loaded in loadFilterOptions()
          }
        })
      )
      .subscribe();

    // Track current page image IDs for selection
    this.images$.pipe(takeUntil(this.destroy$)).subscribe((images) => {
      this.currentPageImageIds = images.map((img) => img.id);
    });
  }

  ngOnDestroy(): void {
    // Reset image upload state when leaving the page
    this.store.dispatch(ImageUploadActions.resetImageUploadState());
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle view mode change
   */
  onViewModeChange(viewMode: ViewMode): void {
    this.store.dispatch(ImageUploadActions.changeViewMode({ viewMode }));

    // When switching to instances view, force Ground Truth annotation type
    if (viewMode === "instances") {
      this.filters$.pipe(take(1)).subscribe((currentFilters) => {
        const updatedFilters: FilterState = {
          ...(currentFilters || {}),
          annotationType: "Ground-Truth",
          // Keep the modelId
          modelId: currentFilters?.modelId || this.selectedModelId,
        };
        this.store.dispatch(
          ImageUploadActions.applyFilters({ filters: updatedFilters })
        );
      });
    }
  }

  /**
   * Handle filter change from filter panel
   */
  onFilterChange(filters: FilterState): void {
    this.store.dispatch(ImageUploadActions.applyFilters({ filters }));
  }

  /**
   * Handle filter clear from filter panel
   */
  onFilterClear(): void {
    this.store.dispatch(ImageUploadActions.clearFilters());
  }

  /**
   * Handle sort method change
   */
  onSortChange(sortMethod: SortMethod): void {
    this.store.dispatch(ImageUploadActions.changeSortMethod({ sortMethod }));
  }

  /**
   * Handle annotation type change from toolbar
   * The modelId should always be sent to filter prediction labels, regardless of annotation type
   */
  onAnnotationTypeChange(annotationType: string | undefined): void {
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      const updatedFilters: FilterState = {
        ...(currentFilters || {}),
        annotationType: annotationType,
        // Always keep the modelId to filter prediction labels
        // Use current modelId or fall back to selectedModelId
        modelId: currentFilters?.modelId || this.selectedModelId,
      };
      this.store.dispatch(
        ImageUploadActions.applyFilters({ filters: updatedFilters })
      );
    });
  }

  /**
   * Handle model selection change from toolbar
   * Only updates the modelId, does not change annotationType
   */
  onModelChange(modelId: number | undefined): void {
    console.log(`[onModelChange] Model changed to: ${modelId}`);
    this.selectedModelId = modelId;
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      const updatedFilters: FilterState = {
        ...(currentFilters || {}),
        // Keep current annotationType, only update modelId
        modelId: modelId,
      };
      console.log(`[onModelChange] Applying filters:`, updatedFilters);
      this.store.dispatch(
        ImageUploadActions.applyFilters({ filters: updatedFilters })
      );
    });
  }

  /**
   * Handle action menu selection
   */
  onActionSelect(action: ActionType): void {
    switch (action) {
      case "manage_classes":
        this.openManageClassesDialog();
        break;
      case "manage_tags":
        this.openManageTagsDialog();
        break;
      case "manage_metadata":
        this.openManageMetadataDialog();
        break;
      case "auto_split":
        this.openAutoSplitDialog();
        break;
    }
  }

  /**
   * Handle snapshot action
   */
  onSnapshotAction(action: SnapshotActionType): void {
    if (action === "snapshot_entire_dataset") {
      this.openSnapshotCreateDialog();
    } else if (action === "view_snapshots") {
      this.navigateToSnapshotListView();
    }
  }

  /**
   * Handle upload button click (Upload Unspecified Images)
   */
  onUploadClick(): void {
    const dialogRef = this.dialog.open(UploadDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      disableClose: false,
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.snackBar.open(
          `Successfully uploaded ${result.uploadedCount} image(s)`,
          "Close",
          {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "bottom",
          }
        );
        this.loadImages();
      }
    });
  }

  /**
   * Handle upload classified images button click (Classification projects only)
   * Opens a dialog for uploading images with pre-specified class labels (e.g., from zip files)
   */
  onUploadClassifiedClick(): void {
    const dialogRef = this.dialog.open(UploadClassifiedDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      disableClose: false,
      panelClass: "upload-classified-dialog-panel",
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.snackBar.open(
          `Successfully uploaded ${result.uploadedCount} classified image(s)`,
          "Close",
          {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "bottom",
          }
        );
        // Reload images and classes
        this.loadImages();
        this.reloadClasses();
      }
    });
  }

  /**
   * Handle batch ZIP upload button click (Object Detection / Segmentation projects)
   * Opens a dialog for uploading a ZIP file containing images at root level
   */
  onUploadBatchZipClick(): void {
    const dialogRef = this.dialog.open(UploadBatchDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      disableClose: false,
      panelClass: "upload-batch-dialog-panel",
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.snackBar.open(
          `Successfully uploaded ${result.uploadedCount} image(s)`,
          "Close",
          {
            duration: 3000,
            horizontalPosition: "center",
            verticalPosition: "bottom",
          }
        );
        // Reload images
        this.loadImages();
      }
    });
  }

  /**
   * Handle training action
   */
  onTrainingAction(action: TrainingActionType): void {
    if (action === "train") {
      this.startTraining(false);
    } else if (action === "custom_training") {
      this.startTraining(true);
    }
  }

  /**
   * Handle zoom change
   */
  onZoomChange(direction: "in" | "out"): void {
    if (direction === "in") {
      this.store.dispatch(ImageUploadActions.zoomIn());
    } else {
      this.store.dispatch(ImageUploadActions.zoomOut());
    }
  }

  /**
   * Handle label details toggle change
   */
  onShowLabelDetailsChange(show: boolean): void {
    this.showLabelDetails = show;
  }

  /**
   * Handle model page button click - navigate to model page
   */
  onModelPageClick(): void {
    this.router.navigate(["/landingai/model", this.projectId]);
  }

  /**
   * Handle image click - navigate to labeling page with pagination context
   */
  onImageClick(image: Image): void {
    console.log("Image clicked:", image);

    // Get current pagination state to pass to labeling page
    combineLatest([
      this.currentPage$,
      this.filters$,
      this.viewMode$,
      this.sortMethod$,
    ])
      .pipe(take(1))
      .subscribe(([currentPage, filters, viewMode, sortMethod]) => {
        // Navigate with query params to maintain pagination context
        this.router.navigate(
          ["/landingai/labelling", this.projectId, image.id],
          {
            queryParams: {
              page: currentPage - 1, // Convert to 0-indexed for backend
              size: 20,
              viewMode: viewMode,
              sortBy: sortMethod,
              // Include filters if they exist
              ...(filters.mediaStatus &&
                filters.mediaStatus.length > 0 && {
                  mediaStatus: filters.mediaStatus.join(","),
                }),
              ...(filters.groundTruthLabels &&
                filters.groundTruthLabels.length > 0 && {
                  groundTruthLabels: filters.groundTruthLabels.join(","),
                }),
              ...(filters.predictionLabels &&
                filters.predictionLabels.length > 0 && {
                  predictionLabels: filters.predictionLabels.join(","),
                }),
              ...(filters.split &&
                filters.split.length > 0 && {
                  split: filters.split.join(","),
                }),
              ...(filters.tags &&
                filters.tags.length > 0 && {
                  tags: filters.tags.join(","),
                }),
              ...(filters.mediaName && { mediaName: filters.mediaName }),
              ...(filters.labeler && { labeler: filters.labeler }),
              ...(filters.mediaId && { mediaId: filters.mediaId }),
              ...(filters.noClass !== undefined && {
                noClass: filters.noClass,
              }),
              ...(filters.predictionNoClass !== undefined && {
                predictionNoClass: filters.predictionNoClass,
              }),
              // Include modelId from filters or fall back to selectedModelId
              ...((filters.modelId || this.selectedModelId) && {
                modelId: filters.modelId || this.selectedModelId,
              }),
              ...(filters.annotationType && {
                annotationType: filters.annotationType,
              }),
            },
          }
        );
      });
  }

  /**
   * Handle class change from image card dropdown
   * For Classification projects, this updates the image's class label
   */
  onClassChange(event: ClassChangeEvent): void {
    console.log("Class change:", event);

    const selectedClass = event.classId
      ? this.availableClasses.find((c) => c.id === event.classId)
      : null;

    if (event.classId === null) {
      // If class is cleared, delete the existing label
      if (event.previousClassId !== null) {
        // Find the label to delete
        this.images$.pipe(take(1)).subscribe((images) => {
          const image = images.find((img) => img.id === event.imageId);
          if (image?.labels && image.labels.length > 0) {
            const labelToDelete = image.labels.find(
              (l: any) => l.annotationType === "Ground Truth"
            );
            if (labelToDelete?.id) {
              this.labelService
                .deleteLabel(labelToDelete.id)
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                  next: () => {
                    // Update local state without reloading
                    this.store.dispatch(
                      ImageUploadActions.updateImageLabel({
                        imageId: event.imageId,
                        classId: null,
                        className: null,
                        colorCode: null,
                      })
                    );
                    this.snackBar.open("Class removed", "Close", {
                      duration: 2000,
                    });
                  },
                  error: (error: any) => {
                    console.error("Failed to remove class:", error);
                    this.snackBar.open("Failed to remove class", "Close", {
                      duration: 3000,
                    });
                  },
                });
            }
          }
        });
      }
      return;
    }

    // Create or update the label
    const newLabel: LabelServiceImageLabel = {
      imageId: event.imageId,
      classId: event.classId,
      position: null, // Classification doesn't need position
      annotationType: "Ground Truth",
    };

    if (event.previousClassId !== null) {
      // Update existing label - find the label ID first
      this.images$.pipe(take(1)).subscribe((images) => {
        const image = images.find((img) => img.id === event.imageId);
        if (image?.labels && image.labels.length > 0) {
          const existingLabel = image.labels.find(
            (l: any) => l.annotationType === "Ground Truth"
          );
          if (existingLabel?.id) {
            newLabel.id = existingLabel.id;
            this.labelService
              .updateLabel(existingLabel.id, newLabel)
              .pipe(takeUntil(this.destroy$))
              .subscribe({
                next: (savedLabel: any) => {
                  // Update local state without reloading
                  this.store.dispatch(
                    ImageUploadActions.updateImageLabel({
                      imageId: event.imageId,
                      classId: event.classId,
                      className: selectedClass?.className || null,
                      colorCode: selectedClass?.colorCode || null,
                      labelId: savedLabel?.id || existingLabel.id,
                    })
                  );
                  this.snackBar.open(
                    `Class changed to "${selectedClass?.className}"`,
                    "Close",
                    {
                      duration: 2000,
                    }
                  );
                },
                error: (error: any) => {
                  console.error("Failed to update class:", error);
                  this.snackBar.open("Failed to update class", "Close", {
                    duration: 3000,
                  });
                },
              });
          }
        }
      });
    } else {
      // Create new label
      this.labelService
        .saveLabel(newLabel)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (savedLabel: any) => {
            // Update local state without reloading
            this.store.dispatch(
              ImageUploadActions.updateImageLabel({
                imageId: event.imageId,
                classId: event.classId,
                className: selectedClass?.className || null,
                colorCode: selectedClass?.colorCode || null,
                labelId: savedLabel?.id,
              })
            );
            this.snackBar.open(
              `Class set to "${selectedClass?.className}"`,
              "Close",
              {
                duration: 2000,
              }
            );
          },
          error: (error: any) => {
            console.error("Failed to set class:", error);
            this.snackBar.open("Failed to set class", "Close", {
              duration: 3000,
            });
          },
        });
    }
  }

  /**
   * Handle page change
   */
  onPageChange(page: number): void {
    // Convert from 1-indexed (UI) to 0-indexed (backend/state)
    this.store.dispatch(ImageUploadActions.changePage({ page: page - 1 }));
  }

  /**
   * Get the Material icon name for a project type
   */
  getProjectTypeIcon(type: ProjectType): string {
    switch (type) {
      case "Classification":
        return "category";
      case "Object Detection":
        return "crop_free";
      case "Segmentation":
        return "polyline";
      default:
        return "image";
    }
  }

  /**
   * Handle select mode toggle
   */
  onSelectModeToggle(enabled: boolean): void {
    this.selectMode = enabled;
    if (!enabled) {
      // Clear selection when exiting select mode
      this.selectedImageIds.clear();
      this.lastSelectAction = "none";
    } else {
      // Entering manual select mode
      this.lastSelectAction = "manual";
    }
  }

  /**
   * Handle select page action
   */
  onSelectPage(): void {
    this.currentPageImageIds.forEach((id) => this.selectedImageIds.add(id));
    this.lastSelectAction = "page";
  }

  /**
   * Handle select all action - selects all images across all pages based on current filters
   */
  onSelectAll(): void {
    // Get current filters to fetch all matching image IDs
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      this.imageService
        .getAllImageIds(this.projectId, currentFilters || {})
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (imageIds: number[]) => {
            // Add all image IDs to selection
            imageIds.forEach((id) => this.selectedImageIds.add(id));
            this.lastSelectAction = "all";
            this.snackBar.open(
              `Selected ${imageIds.length} image(s) across all pages`,
              "Close",
              {
                duration: 2000,
              }
            );
          },
          error: (error: any) => {
            console.error("Failed to fetch all image IDs:", error);
            this.snackBar.open(
              "Failed to select all images. Please try again.",
              "Close",
              {
                duration: 3000,
              }
            );
          },
        });
    });
  }

  /**
   * Handle deselect all action
   */
  onDeselectAll(): void {
    this.selectedImageIds.clear();
    this.lastSelectAction = "none";
  }

  /**
   * Handle image selection toggle
   */
  onImageSelectionToggle(imageId: number, selected: boolean): void {
    if (selected) {
      this.selectedImageIds.add(imageId);
    } else {
      this.selectedImageIds.delete(imageId);
    }
    // When manually toggling, switch to manual mode
    if (this.lastSelectAction !== "manual") {
      this.lastSelectAction = "manual";
    }
  }

  /**
   * Check if an image is selected
   */
  isImageSelected(imageId: number): boolean {
    return this.selectedImageIds.has(imageId);
  }

  /**
   * Get selected image count
   */
  getSelectedCount(): number {
    return this.selectedImageIds.size;
  }

  /**
   * Check if batch actions should be enabled
   * Batch operations should work with 1 or more images selected
   */
  isBatchActionsEnabled(): boolean {
    return this.selectedImageIds.size > 0;
  }

  /**
   * Handle batch set metadata action
   */
  onBatchSetMetadata(): void {
    if (this.selectedImageIds.size === 0) {
      this.snackBar.open(
        "Please select at least 1 image for batch update",
        "Close",
        {
          duration: 3000,
        }
      );
      return;
    }

    // Convert Set to Array and log for debugging
    const selectedIds = Array.from(this.selectedImageIds);
    console.log("Selected image IDs for batch metadata update:", selectedIds);
    console.log("Total selected:", selectedIds.length);

    const dialogRef = this.dialog.open(BatchSetMetadataDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      disableClose: false,
      data: {
        projectId: this.projectId,
        selectedImageIds: selectedIds,
        availableMetadata: this.availableMetadata,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.metadata) {
        // Call backend API to batch update metadata
        this.imageService
          .batchSetMetadata(result.imageIds, result.metadata)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.snackBar.open(
                `Successfully updated metadata for ${result.imageIds.length} image(s)`,
                "Close",
                {
                  duration: 3000,
                }
              );
              // Deselect all images and exit select mode after successful update
              this.onDeselectAll();
              this.selectMode = false;
              // Reload images to reflect changes
              this.reloadCurrentPage();
            },
            error: (error: any) => {
              console.error("Failed to batch update metadata:", error);
              this.snackBar.open(
                "Failed to update metadata. Please try again.",
                "Close",
                {
                  duration: 3000,
                }
              );
            },
          });
      }
    });
  }

  /**
   * Handle batch set tags action
   */
  onBatchSetTags(): void {
    if (this.selectedImageIds.size === 0) {
      this.snackBar.open(
        "Please select at least 1 image for batch update",
        "Close",
        {
          duration: 3000,
        }
      );
      return;
    }

    // Convert Set to Array
    const selectedIds = Array.from(this.selectedImageIds);
    console.log("Selected image IDs for batch tags update:", selectedIds);
    console.log("Total selected:", selectedIds.length);

    const dialogRef = this.dialog.open(BatchSetTagsDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      disableClose: false,
      data: {
        projectId: this.projectId,
        selectedImageIds: selectedIds,
        availableTags: this.availableTags,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.tagIds) {
        // Call backend API to batch update tags
        this.imageService
          .batchSetTags(result.imageIds, result.tagIds)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.snackBar.open(
                `Successfully updated tags for ${result.imageIds.length} image(s)`,
                "Close",
                {
                  duration: 3000,
                }
              );
              // Deselect all images and exit select mode after successful update
              this.onDeselectAll();
              this.selectMode = false;
              // Reload images to reflect changes
              this.reloadCurrentPage();
            },
            error: (error: any) => {
              console.error("Failed to batch update tags:", error);
              this.snackBar.open(
                "Failed to update tags. Please try again.",
                "Close",
                {
                  duration: 3000,
                }
              );
            },
          });
      }
    });
  }

  /**
   * Handle batch set class action
   */
  onBatchSetClass(): void {
    if (this.selectedImageIds.size === 0) {
      this.snackBar.open(
        "Please select at least 1 image for batch update",
        "Close",
        {
          duration: 3000,
        }
      );
      return;
    }

    // Convert Set to Array
    const selectedIds = Array.from(this.selectedImageIds);
    console.log("Selected image IDs for batch class update:", selectedIds);
    console.log("Total selected:", selectedIds.length);

    const dialogRef = this.dialog.open(BatchSetClassDialogComponent, {
      width: "600px",
      maxWidth: "90vw",
      disableClose: false,
      data: {
        projectId: this.projectId,
        selectedImageIds: selectedIds,
        availableClasses: this.availableClasses,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.classId) {
        // Call backend API to batch update class
        this.imageService
          .batchSetClass(result.imageIds, result.classId)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.snackBar.open(
                `Successfully set class for ${result.imageIds.length} image(s)`,
                "Close",
                {
                  duration: 3000,
                }
              );
              // Deselect all images and exit select mode after successful update
              this.onDeselectAll();
              this.selectMode = false;
              // Reload images to reflect changes
              this.reloadCurrentPage();
            },
            error: (error: any) => {
              console.error("Failed to batch set class:", error);
              this.snackBar.open(
                "Failed to set class. Please try again.",
                "Close",
                {
                  duration: 3000,
                }
              );
            },
          });
      }
    });
  }

  /**
   * Handle batch delete action
   */
  onBatchDelete(): void {
    if (this.selectedImageIds.size === 0) {
      this.snackBar.open("Please select at least 1 image to delete", "Close", {
        duration: 3000,
      });
      return;
    }

    // Convert Set to Array
    const selectedIds = Array.from(this.selectedImageIds);
    const count = selectedIds.length;

    // Show confirmation dialog
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "400px",
    });

    // Set the dialog component inputs
    dialogRef.componentInstance.title = "Delete Images";
    dialogRef.componentInstance.message = `Are you sure you want to delete ${count} image(s)? This action cannot be undone. All associated data (labels, tags, metadata, files) will be permanently removed.`;
    dialogRef.componentInstance.btnOkText = "Delete";
    dialogRef.componentInstance.btnCancelText = "Cancel";

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        // Call backend API to batch delete images
        this.imageService
          .deleteImages(selectedIds)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.snackBar.open(
                `Successfully deleted ${count} image(s)`,
                "Close",
                {
                  duration: 3000,
                }
              );
              // Deselect all images and exit select mode after successful delete
              this.onDeselectAll();
              this.selectMode = false;
              // Reload images to reflect changes
              this.reloadCurrentPage();
            },
            error: (error: any) => {
              console.error("Failed to batch delete images:", error);
              this.snackBar.open(
                "Failed to delete images. Please try again.",
                "Close",
                {
                  duration: 3000,
                }
              );
            },
          });
      }
    });
  }

  /**
   * Handle export dataset action (for selected images)
   */
  onExportDataset(): void {
    const selectedIds = Array.from(this.selectedImageIds);

    if (selectedIds.length === 0) {
      this.snackBar.open("Please select images to export", "Close", {
        duration: 3000,
      });
      return;
    }

    console.log("Exporting selected images:", selectedIds.length);

    // Open progress dialog
    const dialogRef = this.dialog.open(DownloadProgressDialogComponent, {
      width: "450px",
      disableClose: true,
      data: {
        title: "Downloading Dataset",
        message: `Preparing ${selectedIds.length} selected images...`,
      },
    });

    this.imageService
      .exportDataset(this.projectId, selectedIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          // Close progress dialog
          dialogRef.close();

          // Create download link
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = url;

          // Generate filename with timestamp
          const timestamp = new Date()
            .toISOString()
            .replace(/[:.]/g, "-")
            .slice(0, -5);
          link.download = `dataset-project-${this.projectId}-selected-${timestamp}.zip`;

          // Trigger download
          document.body.appendChild(link);
          link.click();

          // Cleanup
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);

          this.snackBar.open(
            `Dataset downloaded successfully (${selectedIds.length} images)`,
            "Close",
            {
              duration: 3000,
            }
          );
        },
        error: (error: any) => {
          // Close progress dialog
          dialogRef.close();

          console.error("Failed to export dataset:", error);
          this.snackBar.open(
            "Failed to download dataset. Please try again.",
            "Close",
            {
              duration: 3000,
            }
          );
        },
      });
  }

  /**
   * Handle export all dataset action
   */
  onExportAllDataset(): void {
    console.log("Exporting all images for project:", this.projectId);

    // Open progress dialog
    const dialogRef = this.dialog.open(DownloadProgressDialogComponent, {
      width: "450px",
      disableClose: true,
      data: {
        title: "Downloading Dataset",
        message: "Preparing all images for download...",
      },
    });

    this.imageService
      .exportDataset(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          // Close progress dialog
          dialogRef.close();

          // Create download link
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = url;

          // Generate filename with timestamp
          const timestamp = new Date()
            .toISOString()
            .replace(/[:.]/g, "-")
            .slice(0, -5);
          link.download = `dataset-project-${this.projectId}-all-${timestamp}.zip`;

          // Trigger download
          document.body.appendChild(link);
          link.click();

          // Cleanup
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);

          this.snackBar.open(
            "Dataset downloaded successfully (all images)",
            "Close",
            {
              duration: 3000,
            }
          );
        },
        error: (error: any) => {
          // Close progress dialog
          dialogRef.close();

          console.error("Failed to export dataset:", error);
          this.snackBar.open(
            "Failed to download dataset. Please try again.",
            "Close",
            {
              duration: 3000,
            }
          );
        },
      });
  }

  /**
   * Load images for the current project with current state settings
   * Ensures modelId is always included to filter prediction labels
   */
  private loadImages(): void {
    // Get current filters and viewMode from state to preserve them
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      this.viewMode$.pipe(take(1)).subscribe((currentViewMode) => {
        this.sortMethod$.pipe(take(1)).subscribe((currentSortMethod) => {
          // Ensure modelId is included in filters
          const filtersWithModel: FilterState = {
            ...(currentFilters || {}),
            // Always include modelId if we have one selected
            modelId: currentFilters?.modelId || this.selectedModelId,
          };

          console.log("[loadImages] Loading with filters:", filtersWithModel);

          this.store.dispatch(
            ImageUploadActions.loadImages({
              projectId: this.projectId,
              page: 0,
              size: 20,
              viewMode: currentViewMode || "images",
              filters: filtersWithModel,
              sortBy: currentSortMethod || "upload_time_desc",
            })
          );
        });
      });
    });
  }

  /**
   * Load images from query parameters (when navigating back from labeling page or snapshot list)
   * This restores the exact pagination, filters, and sort state
   * Ensures modelId is always included to filter prediction labels
   */
  private loadImagesFromQueryParams(queryParams: any): void {
    const page = queryParams["page"] ? +queryParams["page"] : 0;
    const size = queryParams["size"] ? +queryParams["size"] : 20;
    const viewMode = queryParams["viewMode"] || "images";
    const sortBy = queryParams["sortBy"] || "upload_time_desc";

    // Parse filter parameters
    const filters: FilterState = {};
    if (queryParams["mediaStatus"]) {
      filters.mediaStatus = queryParams["mediaStatus"].split(",");
    }
    if (queryParams["groundTruthLabels"]) {
      filters.groundTruthLabels = queryParams["groundTruthLabels"]
        .split(",")
        .map((id: string) => +id);
    }
    if (queryParams["predictionLabels"]) {
      filters.predictionLabels = queryParams["predictionLabels"]
        .split(",")
        .map((id: string) => +id);
    }
    if (queryParams["split"]) {
      filters.split = queryParams["split"].split(",");
    }
    if (queryParams["tags"]) {
      filters.tags = queryParams["tags"].split(",").map((id: string) => +id);
    }
    if (queryParams["mediaName"]) {
      filters.mediaName = queryParams["mediaName"];
    }
    if (queryParams["labeler"]) {
      filters.labeler = queryParams["labeler"];
    }
    if (queryParams["mediaId"]) {
      filters.mediaId = queryParams["mediaId"];
    }
    if (queryParams["noClass"] !== undefined) {
      filters.noClass = queryParams["noClass"] === "true";
    }
    if (queryParams["predictionNoClass"] !== undefined) {
      filters.predictionNoClass = queryParams["predictionNoClass"] === "true";
    }
    if (queryParams["modelId"]) {
      filters.modelId = +queryParams["modelId"];
      // Also update the component's selectedModelId
      this.selectedModelId = +queryParams["modelId"];
      console.log(
        `[loadImagesFromQueryParams] ✓ Model ID restored from query params: ${this.selectedModelId}`
      );
    } else {
      console.log(`[loadImagesFromQueryParams] ⚠ No modelId in query params!`);
    }
    if (queryParams["annotationType"]) {
      filters.annotationType = queryParams["annotationType"];
    }

    console.log(
      `[loadImagesFromQueryParams] Restoring state: page=${page}, size=${size}, viewMode=${viewMode}, sortBy=${sortBy}`
    );
    console.log(`[loadImagesFromQueryParams] Filters:`, filters);

    // Dispatch action to load images with restored state
    this.store.dispatch(
      ImageUploadActions.loadImages({
        projectId: this.projectId,
        page: page,
        size: size,
        viewMode: viewMode as ViewMode,
        filters: filters,
        sortBy: sortBy as SortMethod,
      })
    );
  }

  /**
   * Reload images for the current project, preserving the current page
   * Use this after batch operations to keep the user on the same page
   * Ensures modelId is always included to filter prediction labels
   */
  private reloadCurrentPage(): void {
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      this.viewMode$.pipe(take(1)).subscribe((currentViewMode) => {
        this.sortMethod$.pipe(take(1)).subscribe((currentSortMethod) => {
          this.currentPage$.pipe(take(1)).subscribe((currentPage) => {
            // currentPage$ returns 1-indexed (for UI), but backend expects 0-indexed
            // So subtract 1 to convert back to 0-indexed for the API call
            const backendPage = (currentPage || 1) - 1;

            // Ensure modelId is included in filters
            const filtersWithModel: FilterState = {
              ...(currentFilters || {}),
              // Always include modelId if we have one selected
              modelId: currentFilters?.modelId || this.selectedModelId,
            };

            console.log(
              "[reloadCurrentPage] UI page:",
              currentPage,
              "-> Backend page:",
              backendPage
            );
            console.log("[reloadCurrentPage] Filters:", filtersWithModel);
            console.log("[reloadCurrentPage] Sort:", currentSortMethod);

            this.store.dispatch(
              ImageUploadActions.loadImages({
                projectId: this.projectId,
                page: backendPage,
                size: 20,
                viewMode: currentViewMode || "images",
                filters: filtersWithModel,
                sortBy: currentSortMethod || "upload_time_desc",
              })
            );
          });
        });
      });
    });
  }

  /**
   * Load filter options (classes, tags, metadata, models) for the current project
   */
  private loadFilterOptions(): void {
    // Load classes
    this.projectClassService
      .getClassesByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (classes: ProjectClass[]) => {
          this.availableClasses = classes;
        },
        error: (error: any) => {
          console.error("Failed to load classes:", error);
        },
      });

    // Load tags
    this.projectTagService
      .getTagsByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tags: ProjectTag[]) => {
          this.availableTags = tags;
        },
        error: (error: any) => {
          console.error("Failed to load tags:", error);
        },
      });

    // Load metadata
    this.projectMetadataService
      .getMetadataByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (metadata: ProjectMetadata[]) => {
          this.availableMetadata = metadata;
        },
        error: (error: any) => {
          console.error("Failed to load metadata:", error);
        },
      });

    // Load models - this is critical for prediction label filtering
    this.modelService
      .getModelsByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (models: Model[]) => {
          this.availableModels = models;
          console.log(`[loadFilterOptions] Loaded ${models.length} models`);

          // Only auto-select the latest model if no model is already selected
          // This prevents overwriting the model from query params
          if (models.length > 0 && !this.selectedModelId) {
            this.selectedModelId = models[0].id;
            console.log(
              "[loadFilterOptions] ✓ Auto-selected latest model:",
              this.selectedModelId
            );

            // Now that we have a model selected, load images for fresh page loads
            // Check if images haven't been loaded yet (no query params case)
            this.images$.pipe(take(1)).subscribe((currentImages) => {
              if (currentImages.length === 0) {
                console.log(
                  "[loadFilterOptions] Loading images with selected model:",
                  this.selectedModelId
                );
                this.loadImages();
              } else {
                console.log(
                  "[loadFilterOptions] Images already loaded, skipping"
                );
              }
            });
          } else if (this.selectedModelId) {
            console.log(
              "[loadFilterOptions] ✓ Model already selected (from query params):",
              this.selectedModelId
            );
          } else {
            console.log(
              "[loadFilterOptions] ⚠ No models available for this project"
            );
            // Load images even if no models (for projects without trained models)
            this.images$.pipe(take(1)).subscribe((currentImages) => {
              if (currentImages.length === 0) {
                console.log(
                  "[loadFilterOptions] Loading images without model filter"
                );
                this.loadImages();
              }
            });
          }
        },
        error: (error: any) => {
          console.error("Failed to load models:", error);
          // Load images even if model loading fails
          this.images$.pipe(take(1)).subscribe((currentImages) => {
            if (currentImages.length === 0) {
              this.loadImages();
            }
          });
        },
      });
  }

  /**
   * Reload images with the selected model ID
   * This ensures prediction labels are filtered to the selected model
   */
  private reloadImagesWithModel(modelId: number): void {
    this.filters$.pipe(take(1)).subscribe((currentFilters) => {
      const updatedFilters: FilterState = {
        ...(currentFilters || {}),
        modelId: modelId,
      };
      this.store.dispatch(
        ImageUploadActions.applyFilters({ filters: updatedFilters })
      );
    });
  }

  /**
   * Open manage classes dialog
   */
  private openManageClassesDialog(): void {
    const dialogRef = this.dialog.open(ManageClassesDialogComponent, {
      width: "1000px",
      maxWidth: "95vw",
      maxHeight: "90vh",
      disableClose: false,
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.hasChanges) {
        this.reloadClasses();
      }
    });
  }

  /**
   * Open manage tags dialog
   */
  private openManageTagsDialog(): void {
    const dialogRef = this.dialog.open(ManageTagsDialogComponent, {
      width: "800px",
      maxWidth: "95vw",
      maxHeight: "90vh",
      disableClose: false,
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.hasChanges) {
        this.reloadTags();
      }
    });
  }

  /**
   * Open manage metadata dialog
   */
  private openManageMetadataDialog(): void {
    const dialogRef = this.dialog.open(ManageMetadataDialogComponent, {
      width: "1000px",
      maxWidth: "95vw",
      maxHeight: "90vh",
      disableClose: false,
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.hasChanges) {
        this.reloadMetadata();
      }
    });
  }

  /**
   * Reload classes for filter options
   */
  private reloadClasses(): void {
    this.projectClassService
      .getClassesByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (classes: ProjectClass[]) => {
          this.availableClasses = classes;
        },
        error: (error: any) => {
          console.error("Failed to reload classes:", error);
        },
      });
  }

  /**
   * Reload tags for filter options
   */
  private reloadTags(): void {
    this.projectTagService
      .getTagsByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tags: ProjectTag[]) => {
          this.availableTags = tags;
        },
        error: (error: any) => {
          console.error("Failed to reload tags:", error);
        },
      });
  }

  /**
   * Reload metadata for filter options
   */
  private reloadMetadata(): void {
    this.projectMetadataService
      .getMetadataByProjectId(this.projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (metadata: ProjectMetadata[]) => {
          this.availableMetadata = metadata;
        },
        error: (error: any) => {
          console.error("Failed to reload metadata:", error);
        },
      });
  }

  /**
   * Open auto split dialog
   */
  private openAutoSplitDialog(): void {
    const dialogRef = this.dialog.open(AutoSplitDialogComponent, {
      width: "1200px",
      maxWidth: "95vw",
      disableClose: false,
      autoFocus: false,
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadImages();
      }
    });
  }

  /**
   * Open snapshot creation dialog
   */
  private openSnapshotCreateDialog(): void {
    // Get total images count and preview stats
    this.totalElements$.pipe(take(1)).subscribe((totalImages) => {
      // Fetch preview stats from backend
      this.snapshotService
        .getSnapshotPreviewStats(this.projectId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (previewStats) => {
            this.openSnapshotDialogWithStats(totalImages, previewStats);
          },
          error: (error) => {
            console.error("Failed to load preview stats:", error);
            // Open dialog with default stats if fetch fails
            this.openSnapshotDialogWithStats(totalImages, {
              labeled: 0,
              unlabeled: totalImages,
              noClass: 0,
              trainCount: 0,
              devCount: 0,
              testCount: 0,
              unassignedCount: 0,
            });
          },
        });
    });
  }

  /**
   * Open snapshot dialog with stats
   */
  private openSnapshotDialogWithStats(
    totalImages: number,
    previewStats: any
  ): void {
    const dialogRef = this.dialog.open(SnapshotCreateDialogComponent, {
      width: "520px",
      disableClose: false,
      data: {
        projectId: this.projectId,
        totalImages: totalImages,
        previewStats: previewStats,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.snapshotService
          .createSnapshot({
            projectId: this.projectId,
            snapshotName: result.snapshotName,
            description: result.description,
          })
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: (snapshot) => {
              this.snackBar.open(
                `Snapshot "${snapshot.name}" created successfully`,
                "Close",
                {
                  duration: 3000,
                  horizontalPosition: "center",
                  verticalPosition: "bottom",
                }
              );
            },
            error: (error) => {
              const errorMessage =
                error.error?.message || "Failed to create snapshot";
              this.snackBar.open(errorMessage, "Close", {
                duration: 5000,
                horizontalPosition: "center",
                verticalPosition: "bottom",
                panelClass: ["error-snackbar"],
              });
            },
          });
      }
    });
  }

  /**
   * Open snapshot list dialog
   */
  private openSnapshotListDialog(): void {
    this.dialog.open(SnapshotListDialogComponent, {
      width: "800px",
      maxWidth: "90vw",
      disableClose: false,
      data: {
        projectId: this.projectId,
      },
    });
  }

  /**
   * Navigate to snapshot list view page
   * First checks if snapshots exist, shows warning if none
   * Preserves ALL current filters as query params for return navigation
   */
  private navigateToSnapshotListView(): void {
    // Check if there are any snapshots for this project
    this.snapshotService
      .getSnapshotsForProject(this.projectId)
      .pipe(take(1))
      .subscribe({
        next: (snapshots) => {
          if (snapshots && snapshots.length > 0) {
            // Get current state to preserve when returning
            this.filters$.pipe(take(1)).subscribe((currentFilters) => {
              this.viewMode$.pipe(take(1)).subscribe((currentViewMode) => {
                this.sortMethod$
                  .pipe(take(1))
                  .subscribe((currentSortMethod) => {
                    this.currentPage$.pipe(take(1)).subscribe((currentPage) => {
                      // Build query params to preserve complete state
                      const queryParams: any = {};

                      // Preserve page and size
                      queryParams.page = (currentPage || 1) - 1; // Convert to 0-indexed
                      queryParams.size = 20;
                      queryParams.viewMode = currentViewMode || "images";
                      queryParams.sortBy =
                        currentSortMethod || "upload_time_desc";

                      // Preserve ALL filters
                      if (currentFilters) {
                        if (
                          currentFilters.mediaStatus &&
                          currentFilters.mediaStatus.length > 0
                        ) {
                          queryParams.mediaStatus =
                            currentFilters.mediaStatus.join(",");
                        }
                        if (
                          currentFilters.groundTruthLabels &&
                          currentFilters.groundTruthLabels.length > 0
                        ) {
                          queryParams.groundTruthLabels =
                            currentFilters.groundTruthLabels.join(",");
                        }
                        if (
                          currentFilters.predictionLabels &&
                          currentFilters.predictionLabels.length > 0
                        ) {
                          queryParams.predictionLabels =
                            currentFilters.predictionLabels.join(",");
                        }
                        if (
                          currentFilters.split &&
                          currentFilters.split.length > 0
                        ) {
                          queryParams.split = currentFilters.split.join(",");
                        }
                        if (
                          currentFilters.tags &&
                          currentFilters.tags.length > 0
                        ) {
                          queryParams.tags = currentFilters.tags.join(",");
                        }
                        if (currentFilters.mediaName) {
                          queryParams.mediaName = currentFilters.mediaName;
                        }
                        if (currentFilters.labeler) {
                          queryParams.labeler = currentFilters.labeler;
                        }
                        if (currentFilters.mediaId) {
                          queryParams.mediaId = currentFilters.mediaId;
                        }
                        if (currentFilters.noClass !== undefined) {
                          queryParams.noClass = currentFilters.noClass;
                        }
                        if (currentFilters.predictionNoClass !== undefined) {
                          queryParams.predictionNoClass =
                            currentFilters.predictionNoClass;
                        }
                        // CRITICAL: Preserve modelId for prediction label filtering
                        if (currentFilters.modelId) {
                          queryParams.modelId = currentFilters.modelId;
                          console.log(
                            `[navigateToSnapshotListView] ✓ Preserving modelId in query params: ${currentFilters.modelId}`
                          );
                        } else if (this.selectedModelId) {
                          // Fallback to selectedModelId if not in filters
                          queryParams.modelId = this.selectedModelId;
                          console.log(
                            `[navigateToSnapshotListView] ✓ Using selectedModelId in query params: ${this.selectedModelId}`
                          );
                        } else {
                          console.log(
                            `[navigateToSnapshotListView] ⚠ No modelId to preserve!`
                          );
                        }
                        if (currentFilters.annotationType) {
                          queryParams.annotationType =
                            currentFilters.annotationType;
                        }
                      }

                      console.log(
                        "[navigateToSnapshotListView] Full query params:",
                        queryParams
                      );

                      // Navigate to snapshot list view with complete state preservation
                      this.router.navigate(
                        ["/landingai/projects", this.projectId, "snapshots"],
                        { queryParams }
                      );
                    });
                  });
              });
            });
          } else {
            // Show warning message if no snapshots
            this.snackBar.open(
              "No snapshots available for this project. Create a snapshot first.",
              "Close",
              {
                duration: 5000,
                horizontalPosition: "center",
                verticalPosition: "bottom",
                panelClass: ["warning-snackbar"],
              }
            );
          }
        },
        error: (error) => {
          console.error("Error checking snapshots:", error);
          this.snackBar.open(
            "Failed to check snapshots. Please try again.",
            "Close",
            {
              duration: 3000,
              horizontalPosition: "center",
              verticalPosition: "bottom",
            }
          );
        },
      });
  }

  /**
   * Start training with default or custom parameters
   */
  private startTraining(isCustom: boolean): void {
    if (isCustom) {
      // Navigate to custom training page
      this.router.navigate(["/landingai/ai-training", this.projectId]);
      return;
    }

    this.trainingService
      .startTraining({
        projectId: this.projectId,
        isCustomTraining: false,
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (trainingRecord) => {
          this.snackBar.open(
            `Training started successfully (ID: ${trainingRecord.id}). Status: ${trainingRecord.status}`,
            "Close",
            {
              duration: 5000,
              horizontalPosition: "center",
              verticalPosition: "bottom",
            }
          );
        },
        error: (error) => {
          const errorMessage =
            error.error?.message || "Failed to start training";
          this.snackBar.open(errorMessage, "Close", {
            duration: 5000,
            horizontalPosition: "center",
            verticalPosition: "bottom",
            panelClass: ["error-snackbar"],
          });
        },
      });
  }
}
