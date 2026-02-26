import { Component, EventEmitter, Input, Output } from "@angular/core";
import { MatButtonToggleChange } from "@angular/material/button-toggle";
import { MatDialog } from "@angular/material/dialog";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import {
  FilterState,
  SortMethod,
  ViewMode,
} from "../../../../state/landingai/image-upload/image-upload.actions";
import { ProjectClass } from "../../../../models/landingai/project-class.model";
import { ProjectTag } from "../../../../models/landingai/project-tag.model";
import { ProjectMetadata } from "../../../../models/landingai/project-metadata.model";
import { Model } from "../../../../models/landingai/model";
import {
  FilterDialogComponent,
  FilterDialogData,
  FilterDialogResult,
} from "../filter-dialog/filter-dialog.component";

export type ActionType =
  | "manage_classes"
  | "manage_tags"
  | "manage_metadata"
  | "auto_split";
export type SnapshotActionType = "snapshot_entire_dataset" | "view_snapshots";
export type TrainingActionType = "train" | "custom_training";

@Component({
  selector: "app-toolbar",
  standalone: false,
  templateUrl: "./toolbar.component.html",
  styleUrls: ["./toolbar.component.scss"],
})
export class ToolbarComponent {
  @Input() viewMode: ViewMode = "images";
  @Input() activeFilters: FilterState = {};
  @Input() sortMethod: SortMethod = "upload_time_desc";
  @Input() zoomLevel: number = 3;
  @Input() annotationType: string | undefined;
  @Input() selectedModelId: number | undefined;
  @Input() availableModels: Model[] = [];
  @Input() availableClasses: ProjectClass[] = [];
  @Input() availableTags: ProjectTag[] = [];
  @Input() availableMetadata: ProjectMetadata[] = [];
  @Input() showLabelDetails: boolean = true;
  @Input() projectType: string = "Object Detection";
  @Input() projectName: string = "";
  @Input() selectMode: boolean = false;
  @Input() selectedCount: number = 0;
  @Input() currentPageImageCount: number = 0;
  @Input() totalImageCount: number = 0;
  @Input() lastSelectAction: "manual" | "page" | "all" | "none" = "none";

  @Output() viewModeChange = new EventEmitter<ViewMode>();
  @Output() selectModeToggle = new EventEmitter<boolean>();
  @Output() selectPage = new EventEmitter<void>();
  @Output() selectAll = new EventEmitter<void>();
  @Output() deselectAll = new EventEmitter<void>();
  @Output() batchSetClass = new EventEmitter<void>();
  @Output() batchSetTags = new EventEmitter<void>();
  @Output() batchSetMetadata = new EventEmitter<void>();
  @Output() batchDelete = new EventEmitter<void>();
  @Output() exportDataset = new EventEmitter<void>();
  @Output() exportAllDataset = new EventEmitter<void>();
  @Output() filterClick = new EventEmitter<void>();
  @Output() filterChange = new EventEmitter<FilterState>();
  @Output() filterClear = new EventEmitter<void>();
  @Output() sortChange = new EventEmitter<SortMethod>();
  @Output() actionSelect = new EventEmitter<ActionType>();
  @Output() snapshotAction = new EventEmitter<SnapshotActionType>();
  @Output() uploadClick = new EventEmitter<void>();
  @Output() uploadClassifiedClick = new EventEmitter<void>();
  @Output() uploadBatchZipClick = new EventEmitter<void>();
  @Output() trainingAction = new EventEmitter<TrainingActionType>();
  @Output() zoomChange = new EventEmitter<"in" | "out">();
  @Output() annotationTypeChange = new EventEmitter<string>();
  @Output() modelChange = new EventEmitter<number | undefined>();
  @Output() modelPageClick = new EventEmitter<void>();
  @Output() showLabelDetailsChange = new EventEmitter<boolean>();

  readonly minZoom = 1;
  readonly maxZoom = 5;

  constructor(private dialog: MatDialog) {}

  /**
   * Check if zoom in is disabled
   */
  get isZoomInDisabled(): boolean {
    return this.zoomLevel <= this.minZoom;
  }

  /**
   * Check if zoom out is disabled
   */
  get isZoomOutDisabled(): boolean {
    return this.zoomLevel >= this.maxZoom;
  }

  /**
   * Get active filter count
   * Note: annotationType and modelId are not counted as they are controlled by the toolbar toggle, not the filter panel
   */
  get activeFilterCount(): number {
    let count = 0;
    if (this.activeFilters.mediaStatus?.length) count++;
    if (this.activeFilters.groundTruthLabels?.length) count++;
    if (this.activeFilters.predictionLabels?.length) count++;
    if (this.activeFilters.split?.length) count++;
    if (this.activeFilters.tags?.length) count++;
    if (this.activeFilters.mediaName) count++;
    if (this.activeFilters.labeler) count++;
    if (this.activeFilters.mediaId) count++;
    if (this.activeFilters.noClass) count++;
    if (this.activeFilters.predictionNoClass) count++;
    if (
      this.activeFilters.metadata &&
      Object.keys(this.activeFilters.metadata).length
    )
      count++;
    return count;
  }

  /**
   * Handle annotation type change
   */
  onAnnotationTypeChange(event: MatButtonToggleChange): void {
    // Convert "all" to undefined
    const annotationType = event.value === "all" ? undefined : event.value;
    this.annotationTypeChange.emit(annotationType);

    // Keep model selection - modelId should always be sent to filter prediction labels
    // No need to clear modelChange here
  }

  /**
   * Handle annotation type button click (for All and Ground truth)
   */
  onAnnotationTypeSelect(type: string): void {
    const annotationType = type === "all" ? undefined : type;
    this.annotationTypeChange.emit(annotationType);
    // Keep model selection - modelId should always be sent to filter prediction labels
    // No need to clear modelChange here
  }

  /**
   * Handle prediction button click
   * Switches to Prediction view showing all prediction labels (no model filter)
   */
  onPredictionClick(): void {
    // Emit annotationType change to Prediction without model filter
    this.annotationTypeChange.emit("Prediction");
  }

  /**
   * Handle prediction dropdown selection
   * Always requires a specific model ID
   * Emit modelChange first to ensure modelId is set before annotationType triggers the query
   */
  onPredictionSelect(modelId: number): void {
    // Emit model change first so the modelId is available when annotationType change triggers the query
    this.modelChange.emit(modelId);
  }

  /**
   * Handle model selection from dropdown
   * Only changes the model, keeps current annotation type
   */
  onModelSelect(modelId: number): void {
    this.modelChange.emit(modelId);
  }

  /**
   * Get display name for the model selector dropdown button
   */
  getSelectedModelDisplayName(): string {
    if (this.availableModels.length === 0) {
      return "No Models";
    }
    const modelId = this.selectedModelId || this.availableModels[0]?.id;
    const model = this.availableModels.find((m) => m.id === modelId);
    if (!model) {
      return "Select Model";
    }
    return model.modelAlias || "Model-" + model.id;
  }

  /**
   * Check if a model is selected in the dropdown
   * If no model is explicitly selected, the first model (latest) is considered selected
   */
  isModelSelected(modelId: number, isFirst: boolean): boolean {
    if (this.selectedModelId) {
      return this.selectedModelId === modelId;
    }
    // If no model selected yet, default to first (latest) model
    return isFirst;
  }

  /**
   * Get prediction button label
   * Always shows the selected model name (or first model if none selected)
   */
  getPredictionButtonLabel(): string {
    if (this.availableModels.length === 0) {
      return "Prediction";
    }
    // Use selected model or default to first (latest) model
    const modelId = this.selectedModelId || this.availableModels[0]?.id;
    const model = this.availableModels.find((m) => m.id === modelId);
    if (!model) {
      return "Prediction";
    }
    return `Prediction (${model.modelAlias || "Model-" + model.id})`;
  }

  /**
   * Handle model selection change
   */
  onModelChange(modelId: number | undefined): void {
    this.modelChange.emit(modelId);
  }

  /**
   * Get selected model display name
   */
  getSelectedModelName(): string {
    if (!this.selectedModelId || this.availableModels.length === 0) {
      return "Select Model";
    }
    const model = this.availableModels.find(
      (m) => m.id === this.selectedModelId
    );
    if (!model) {
      return "Select Model";
    }
    const f1Rate = model.devF1Rate
      ? (model.devF1Rate / 100).toFixed(2)
      : "0.00";
    return `${model.modelAlias || "Model-" + model.id}(${f1Rate})`;
  }

  /**
   * Handle view mode change
   */
  onViewModeChange(mode: ViewMode): void {
    this.viewModeChange.emit(mode);

    // When switching to instances view, force Ground Truth annotation type
    if (mode === "instances") {
      this.annotationTypeChange.emit("Ground-Truth");
    }
  }

  /**
   * Get the Material icon name for a project type
   */
  getProjectTypeIcon(): string {
    switch (this.projectType) {
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
   * Check if annotation type selector should be disabled
   * Disabled in instances view (always shows Ground Truth)
   */
  get isAnnotationTypeSelectorDisabled(): boolean {
    return this.viewMode === "instances";
  }

  /**
   * Handle filter button click - opens filter dialog
   */
  onFilterClick(): void {
    const dialogData: FilterDialogData = {
      activeFilters: this.activeFilters,
      availableClasses: this.availableClasses,
      availableTags: this.availableTags,
      availableMetadata: this.availableMetadata,
      selectedModelId: this.selectedModelId,
    };

    const dialogRef = this.dialog.open(FilterDialogComponent, {
      width: "650px",
      maxHeight: "85vh",
      data: dialogData,
      panelClass: "filter-dialog-panel",
    });

    dialogRef.afterClosed().subscribe((result: FilterDialogResult) => {
      if (result) {
        if (result.cleared) {
          this.filterClear.emit();
        } else if (result.filters) {
          this.filterChange.emit(result.filters);
        }
      }
    });
  }

  /**
   * Handle filter change from filter panel (kept for compatibility)
   */
  onFilterChange(filters: FilterState): void {
    this.filterChange.emit(filters);
  }

  /**
   * Handle filter clear from filter panel (kept for compatibility)
   */
  onFilterClear(): void {
    this.filterClear.emit();
  }

  /**
   * Handle sort method change
   */
  onSortChange(method: SortMethod): void {
    this.sortChange.emit(method);
  }

  /**
   * Handle action menu selection
   */
  onActionSelect(action: ActionType): void {
    this.actionSelect.emit(action);
  }

  /**
   * Handle snapshot action
   */
  onSnapshotAction(action: SnapshotActionType): void {
    this.snapshotAction.emit(action);
  }

  /**
   * Handle upload button click (for non-Classification projects or Upload Unclassified Images)
   */
  onUploadClick(): void {
    this.uploadClick.emit();
  }

  /**
   * Handle upload unclassified images menu item click (Classification projects)
   */
  onUploadUnclassified(): void {
    this.uploadClick.emit();
  }

  /**
   * Handle upload classified images menu item click (Classification projects)
   */
  onUploadClassified(): void {
    this.uploadClassifiedClick.emit();
  }

  /**
   * Handle upload batch ZIP menu item click (Object Detection / Segmentation projects)
   */
  onUploadBatchZip(): void {
    this.uploadBatchZipClick.emit();
  }

  /**
   * Handle training action
   */
  onTrainingAction(action: TrainingActionType): void {
    this.trainingAction.emit(action);
  }

  /**
   * Handle zoom in
   */
  onZoomIn(): void {
    this.zoomChange.emit("in");
  }

  /**
   * Handle zoom out
   */
  onZoomOut(): void {
    this.zoomChange.emit("out");
  }

  /**
   * Handle model page button click
   */
  onModelPageClick(): void {
    this.modelPageClick.emit();
  }

  /**
   * Handle label details toggle change
   */
  onShowLabelDetailsToggle(event: MatSlideToggleChange): void {
    this.showLabelDetailsChange.emit(event.checked);
  }

  /**
   * Get sort method display text
   */
  getSortMethodText(): string {
    switch (this.sortMethod) {
      case "upload_time_desc":
        return "Upload Time (Newest)";
      case "upload_time_asc":
        return "Upload Time (Oldest)";
      case "label_time_desc":
        return "Label Time (Newest)";
      case "label_time_asc":
        return "Label Time (Oldest)";
      default:
        return "Sort";
    }
  }

  /**
   * Handle select mode toggle
   */
  onSelectModeToggle(): void {
    this.selectModeToggle.emit(!this.selectMode);
  }

  /**
   * Handle select manually action - enters select mode
   */
  onSelectManually(): void {
    this.selectModeToggle.emit(true);
  }

  /**
   * Handle select page action - enters select mode and selects page
   */
  onSelectPage(): void {
    // Enter select mode first
    if (!this.selectMode) {
      this.selectModeToggle.emit(true);
    }
    this.selectPage.emit();
  }

  /**
   * Handle select all action - enters select mode and selects all
   */
  onSelectAll(): void {
    // Enter select mode first
    if (!this.selectMode) {
      this.selectModeToggle.emit(true);
    }
    this.selectAll.emit();
  }

  /**
   * Handle deselect all action - exits select mode
   */
  onDeselectAll(): void {
    this.deselectAll.emit();
    // Exit select mode after deselecting all
    this.selectModeToggle.emit(false);
  }

  /**
   * Handle batch set class action
   */
  onBatchSetClass(): void {
    this.batchSetClass.emit();
  }

  /**
   * Handle batch set tags action
   */
  onBatchSetTags(): void {
    this.batchSetTags.emit();
  }

  /**
   * Handle batch set metadata action
   */
  onBatchSetMetadata(): void {
    this.batchSetMetadata.emit();
  }

  /**
   * Handle batch delete action
   */
  onBatchDelete(): void {
    this.batchDelete.emit();
  }

  /**
   * Handle export dataset action
   */
  onExportDataset(): void {
    this.exportDataset.emit();
  }

  /**
   * Handle export all dataset action
   */
  onExportAllDataset(): void {
    this.exportAllDataset.emit();
  }

  /**
   * Get the display text for the Select button based on current selection state
   */
  getSelectButtonText(): string {
    if (!this.selectMode) {
      return "Select";
    }

    if (this.lastSelectAction === "page") {
      return "Select page";
    } else if (this.lastSelectAction === "all") {
      return "Select all";
    } else if (this.lastSelectAction === "manual") {
      return "Select image";
    }

    return "Select";
  }

  /**
   * Check if batch actions should be enabled
   * Batch operations should work with 1 or more images selected
   */
  get isBatchActionsEnabled(): boolean {
    return this.selectedCount > 0;
  }

  /**
   * Check if "Select Manually" should be highlighted
   */
  get isManualSelectActive(): boolean {
    return this.selectMode && this.lastSelectAction === "manual";
  }

  /**
   * Check if "Select page" should be highlighted
   */
  get isPageSelectActive(): boolean {
    return (
      this.selectMode &&
      this.lastSelectAction === "page" &&
      this.selectedCount > 0
    );
  }

  /**
   * Check if "Select All" should be highlighted
   */
  get isAllSelectActive(): boolean {
    return (
      this.selectMode &&
      this.lastSelectAction === "all" &&
      this.selectedCount > 0
    );
  }
}
