import { Component, EventEmitter, Input, Output } from "@angular/core";
import { MatAutocompleteSelectedEvent } from "@angular/material/autocomplete";
import { FilterState } from "app/state/landingai/image-upload/image-upload.actions";
import { ProjectClass } from "app/models/landingai/project-class.model";
import { ProjectTag } from "app/models/landingai/project-tag.model";
import { ProjectMetadata } from "app/models/landingai/project-metadata.model";

@Component({
  selector: "app-filter-panel",
  standalone: false,
  templateUrl: "./filter-panel.component.html",
  styleUrls: ["./filter-panel.component.scss"],
})
export class FilterPanelComponent {
  @Input() activeFilters: FilterState = {};
  @Input() availableClasses: ProjectClass[] = [];
  @Input() availableTags: ProjectTag[] = [];
  @Input() availableMetadata: ProjectMetadata[] = [];
  @Input() selectedModelId: number | undefined;
  @Input() hidePredictionLabels: boolean = false; // Hide prediction labels section (for snapshots)

  @Output() filterChange = new EventEmitter<FilterState>();
  @Output() filterClear = new EventEmitter<void>();
  @Output() closePanel = new EventEmitter<void>();

  // Local filter state for form binding
  localFilters: FilterState = {};

  // Tags combobox state
  tagSearchText: string = "";
  filteredTags: ProjectTag[] = [];

  // Media status options (No Class moved to Ground Truth Labels)
  mediaStatusOptions = [
    { value: "labeled", label: "Labeled" },
    { value: "unlabeled", label: "Unlabeled" },
  ];

  // Split options
  splitOptions = [
    { value: "training", label: "Training" },
    { value: "dev", label: "Dev" },
    { value: "test", label: "Test" },
    { value: "unassigned", label: "Unassigned" },
  ];

  ngOnInit(): void {
    // Initialize local filters from active filters (excluding toolbar-controlled filters)
    this.initLocalFilters();
    // Initialize filtered tags
    this.filteredTags = this.getUnselectedTags();
    console.log("FilterPanelComponent initialized with:", {
      availableClasses: this.availableClasses,
      availableTags: this.availableTags,
      availableMetadata: this.availableMetadata,
    });
  }

  ngOnChanges(): void {
    // Update local filters when active filters change (excluding toolbar-controlled filters)
    this.initLocalFilters();
    // Update filtered tags when available tags change
    this.filteredTags = this.getUnselectedTags();
  }

  /**
   * Toggle No Class filter (in Ground Truth Labels section)
   */
  toggleNoClass(): void {
    this.localFilters.noClass = !this.localFilters.noClass;
  }

  /**
   * Check if No Class is selected
   */
  isNoClassSelected(): boolean {
    return this.localFilters.noClass === true;
  }

  /**
   * Toggle Prediction No Class filter (in Prediction Labels section)
   */
  togglePredictionNoClass(): void {
    this.localFilters.predictionNoClass = !this.localFilters.predictionNoClass;
  }

  /**
   * Check if Prediction No Class is selected
   */
  isPredictionNoClassSelected(): boolean {
    return this.localFilters.predictionNoClass === true;
  }

  /**
   * Toggle media status filter
   */
  toggleMediaStatus(status: string): void {
    const currentStatus = this.localFilters.mediaStatus || [];
    const index = currentStatus.indexOf(status);
    if (index > -1) {
      // Remove - create new array without the item
      this.localFilters.mediaStatus = currentStatus.filter((s) => s !== status);
    } else {
      // Add - create new array with the item
      this.localFilters.mediaStatus = [...currentStatus, status];
    }
  }

  /**
   * Check if media status is selected
   */
  isMediaStatusSelected(status: string): boolean {
    return this.localFilters.mediaStatus?.includes(status) || false;
  }

  /**
   * Toggle ground truth label filter
   */
  toggleGroundTruthLabel(classId: number): void {
    const currentLabels = this.localFilters.groundTruthLabels || [];
    const index = currentLabels.indexOf(classId);
    if (index > -1) {
      this.localFilters.groundTruthLabels = currentLabels.filter(
        (id) => id !== classId
      );
    } else {
      this.localFilters.groundTruthLabels = [...currentLabels, classId];
    }
  }

  /**
   * Check if ground truth label is selected
   */
  isGroundTruthLabelSelected(classId: number): boolean {
    return this.localFilters.groundTruthLabels?.includes(classId) || false;
  }

  /**
   * Toggle prediction label filter
   */
  togglePredictionLabel(classId: number): void {
    const currentLabels = this.localFilters.predictionLabels || [];
    const index = currentLabels.indexOf(classId);
    if (index > -1) {
      this.localFilters.predictionLabels = currentLabels.filter(
        (id) => id !== classId
      );
    } else {
      this.localFilters.predictionLabels = [...currentLabels, classId];
    }
  }

  /**
   * Check if prediction label is selected
   */
  isPredictionLabelSelected(classId: number): boolean {
    return this.localFilters.predictionLabels?.includes(classId) || false;
  }

  /**
   * Toggle split filter
   */
  toggleSplit(split: string): void {
    const currentSplit = this.localFilters.split || [];
    const index = currentSplit.indexOf(split);
    if (index > -1) {
      this.localFilters.split = currentSplit.filter((s) => s !== split);
    } else {
      this.localFilters.split = [...currentSplit, split];
    }
  }

  /**
   * Check if split is selected
   */
  isSplitSelected(split: string): boolean {
    return this.localFilters.split?.includes(split) || false;
  }

  /**
   * Toggle tag filter
   */
  toggleTag(tagId: number): void {
    const currentTags = this.localFilters.tags || [];
    const index = currentTags.indexOf(tagId);
    if (index > -1) {
      this.localFilters.tags = currentTags.filter((id) => id !== tagId);
    } else {
      this.localFilters.tags = [...currentTags, tagId];
    }
  }

  /**
   * Handle tags dropdown selection change
   */
  onTagsSelectionChange(selectedTagIds: number[]): void {
    this.localFilters.tags =
      selectedTagIds.length > 0 ? selectedTagIds : undefined;
  }

  /**
   * Check if tag is selected
   */
  isTagSelected(tagId: number): boolean {
    return this.localFilters.tags?.includes(tagId) || false;
  }

  /**
   * Get tag name by ID
   */
  getTagName(tagId: number): string {
    const tag = this.availableTags.find((t) => t.id === tagId);
    return tag?.name || `Tag ${tagId}`;
  }

  /**
   * Filter tags based on search text
   */
  filterTags(searchText: string): void {
    this.tagSearchText = searchText;
    const unselectedTags = this.getUnselectedTags();
    if (!searchText) {
      this.filteredTags = unselectedTags;
    } else {
      const lowerSearch = searchText.toLowerCase();
      this.filteredTags = unselectedTags.filter((tag) =>
        tag.name.toLowerCase().includes(lowerSearch)
      );
    }
  }

  /**
   * Select a tag from autocomplete
   */
  selectTag(event: MatAutocompleteSelectedEvent): void {
    const tagId = event.option.value as number;
    const currentTags = this.localFilters.tags || [];
    if (!currentTags.includes(tagId)) {
      this.localFilters.tags = [...currentTags, tagId];
    }
    // Clear search text and update filtered tags
    this.tagSearchText = "";
    this.filteredTags = this.getUnselectedTags();
  }

  /**
   * Remove a tag from selection
   */
  removeTag(tagId: number): void {
    const currentTags = this.localFilters.tags || [];
    this.localFilters.tags = currentTags.filter((id) => id !== tagId);
    if (this.localFilters.tags.length === 0) {
      this.localFilters.tags = undefined;
    }
    // Update filtered tags
    this.filteredTags = this.getUnselectedTags();
  }

  /**
   * Update media name filter
   */
  updateMediaName(value: string): void {
    this.localFilters.mediaName = value || undefined;
  }

  /**
   * Update labeler filter
   */
  updateLabeler(value: string): void {
    this.localFilters.labeler = value || undefined;
  }

  /**
   * Update media ID filter
   */
  updateMediaId(value: string): void {
    this.localFilters.mediaId = value || undefined;
  }

  /**
   * Update metadata filter
   */
  updateMetadata(key: string, value: string): void {
    if (!this.localFilters.metadata) {
      this.localFilters.metadata = {};
    }
    if (value) {
      this.localFilters.metadata[key] = value;
    } else {
      delete this.localFilters.metadata[key];
    }
  }

  /**
   * Get metadata value
   */
  getMetadataValue(key: string): string {
    return this.localFilters.metadata?.[key] || "";
  }

  /**
   * Apply filters
   * Preserves annotationType and modelId from activeFilters as they are controlled by toolbar
   * Uses selectedModelId as fallback when modelId is not set (for predictionNoClass filter)
   */
  applyFilters(): void {
    console.log("FilterPanel: applyFilters called");
    console.log("FilterPanel: localFilters =", this.localFilters);
    console.log("FilterPanel: activeFilters =", this.activeFilters);

    // Merge local filters with toolbar-controlled filters (annotationType, modelId)
    // Use selectedModelId as fallback when modelId is not set in activeFilters
    const mergedFilters: FilterState = {
      ...this.localFilters,
      annotationType: this.activeFilters.annotationType,
      modelId: this.activeFilters.modelId || this.selectedModelId,
    };

    console.log(
      "FilterPanel: emitting filterChange with mergedFilters =",
      mergedFilters
    );
    this.filterChange.emit(mergedFilters);

    // Don't emit closePanel here - the parent (filter-dialog) will close itself
    // when it receives filterChange
  }

  /**
   * Close the filter panel
   */
  onClose(): void {
    this.closePanel.emit();
  }

  /**
   * Clear all filters
   * Preserves annotationType and modelId from activeFilters as they are controlled by toolbar
   */
  clearFilters(): void {
    this.localFilters = {};
    // Reset tag search state
    this.tagSearchText = "";
    this.filteredTags = this.availableTags;
    // Emit filters with only toolbar-controlled values preserved
    const clearedFilters: FilterState = {
      annotationType: this.activeFilters.annotationType,
      modelId: this.activeFilters.modelId,
    };
    this.filterChange.emit(clearedFilters);
  }

  /**
   * Check if any filters are active
   * Note: annotationType is not counted as it's controlled by the toolbar toggle
   */
  hasActiveFilters(): boolean {
    return (
      (this.localFilters.mediaStatus?.length || 0) > 0 ||
      (this.localFilters.groundTruthLabels?.length || 0) > 0 ||
      (this.localFilters.predictionLabels?.length || 0) > 0 ||
      (this.localFilters.split?.length || 0) > 0 ||
      (this.localFilters.tags?.length || 0) > 0 ||
      !!this.localFilters.mediaName ||
      !!this.localFilters.labeler ||
      !!this.localFilters.mediaId ||
      !!this.localFilters.noClass ||
      !!this.localFilters.predictionNoClass ||
      Object.keys(this.localFilters.metadata || {}).length > 0
    );
  }

  /**
   * Get count of active filters
   * Note: annotationType and modelId are not counted as they are controlled by the toolbar toggle
   */
  getActiveFilterCount(): number {
    let count = 0;
    if (this.localFilters.mediaStatus?.length) count++;
    if (this.localFilters.groundTruthLabels?.length) count++;
    if (this.localFilters.predictionLabels?.length) count++;
    if (this.localFilters.split?.length) count++;
    if (this.localFilters.tags?.length) count++;
    if (this.localFilters.mediaName) count++;
    if (this.localFilters.labeler) count++;
    if (this.localFilters.mediaId) count++;
    if (this.localFilters.noClass) count++;
    if (this.localFilters.predictionNoClass) count++;
    if (Object.keys(this.localFilters.metadata || {}).length > 0) count++;
    return count;
  }

  /**
   * Initialize local filters from active filters, excluding annotationType and modelId
   * which are controlled by the toolbar toggle, not the filter panel
   */
  private initLocalFilters(): void {
    const { annotationType, modelId, ...panelFilters } = this.activeFilters;
    // Deep copy arrays to avoid mutation issues
    this.localFilters = {
      ...panelFilters,
      mediaStatus: panelFilters.mediaStatus
        ? [...panelFilters.mediaStatus]
        : undefined,
      groundTruthLabels: panelFilters.groundTruthLabels
        ? [...panelFilters.groundTruthLabels]
        : undefined,
      predictionLabels: panelFilters.predictionLabels
        ? [...panelFilters.predictionLabels]
        : undefined,
      split: panelFilters.split ? [...panelFilters.split] : undefined,
      tags: panelFilters.tags ? [...panelFilters.tags] : undefined,
      metadata: panelFilters.metadata
        ? { ...panelFilters.metadata }
        : undefined,
      noClass: panelFilters.noClass,
      predictionNoClass: panelFilters.predictionNoClass,
    };
  }

  /**
   * Get unselected tags (for autocomplete options)
   */
  private getUnselectedTags(): ProjectTag[] {
    const selectedIds = this.localFilters.tags || [];
    return this.availableTags.filter(
      (tag) => tag.id !== undefined && !selectedIds.includes(tag.id)
    );
  }
}
