import { Component, OnInit, OnDestroy } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Store } from "@ngrx/store";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import {
  ModelDisplayDto,
  SearchFilters,
  formatMetricAsPercentage,
  formatConfidenceThreshold,
} from "../../../models/landingai/model";
import { ModelService } from "../../../services/landingai/model.service";
import { DownloadModelService } from "../../../services/landingai/download-model.service";
import * as ModelActions from "../../../state/landingai/model/model.actions";
import * as ModelSelectors from "../../../state/landingai/model/model.selectors";
import * as ModelDetailActions from "../../../state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "../../../state/landingai/model/model-detail/model-detail.selectors";
import { ModelRowClickEvent } from "./model-table/model-table.component";
import { ConfirmDialogComponent } from "../../../components/dialogs/confirm-dialog/confirm-dialog.component";
import { TestModelDialogComponent } from "../test-model/test-model-dialog.component";

/**
 * Model main page component
 * Implementation requirements 4.1, 4.2, 4.3: Page title, toolbar, and card container structure
 */
@Component({
  selector: "app-model",
  templateUrl: "./model.component.html",
  styleUrls: ["./model.component.scss"],
  standalone: false,
})
export class ModelComponent implements OnInit, OnDestroy {
  // Observables from store
  displayModels$ = this.store.select(ModelSelectors.selectAllModels);
  filteredModels$ = this.store.select(ModelSelectors.selectFilteredModels);
  loading$ = this.store.select(ModelSelectors.selectLoading);
  error$ = this.store.select(ModelSelectors.selectError);
  searchFilters$ = this.store.select(ModelSelectors.selectSearchFilters);
  isPanelOpen$ = this.store.select(ModelDetailSelectors.selectIsPanelOpen);

  // For template compatibility
  displayModels: ModelDisplayDto[] = [];
  filteredModels: ModelDisplayDto[] = [];
  searchFilters = {
    searchTerm: "",
    showFavoritesOnly: false,
  };
  loading = false;
  error: string | null = null;
  projectId: number | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private modelService: ModelService,
    private downloadModelService: DownloadModelService
  ) {}

  ngOnInit(): void {
    // Subscribe to store observables to update local properties for template
    this.displayModels$.pipe(takeUntil(this.destroy$)).subscribe((models) => {
      this.displayModels = models;
    });

    this.filteredModels$.pipe(takeUntil(this.destroy$)).subscribe((models) => {
      this.filteredModels = models;
    });

    this.searchFilters$.pipe(takeUntil(this.destroy$)).subscribe((filters) => {
      this.searchFilters = filters;
    });

    this.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading) => {
      this.loading = loading;
    });

    this.error$.pipe(takeUntil(this.destroy$)).subscribe((error) => {
      this.error = error;
    });

    // Check if projectId is provided in route params
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const projectIdParam = params.get("projectId");
      if (projectIdParam) {
        this.projectId = parseInt(projectIdParam, 10);
        this.loadModelsByProject(this.projectId);
      } else {
        this.projectId = null;
        this.loadModels();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load model data
   * Implementation requirements 1.2: By default, display all available models on page load
   */
  private loadModels(): void {
    this.store.dispatch(ModelActions.loadModels());
  }

  /**
   * Load models by project ID
   */
  private loadModelsByProject(projectId: number): void {
    this.store.dispatch(ModelActions.loadModelsByProject({ projectId }));
  }

  /**
   * Handle search input change (from ModelsSearchComponent)
   * Implementation requirements 2.1, 2.2: Real-time search with debouncing
   */
  onSearchChange(searchTerm: string): void {
    this.store.dispatch(ModelActions.updateSearchTerm({ searchTerm }));
  }

  /**
   * Handle favorites filter change (from ModelsSearchComponent)
   * Implementation requirements 2.3: When "Only show favorite models" checkbox is checked, only display models with Favorite_Status as true
   */
  onFavoritesFilterChange(showFavoritesOnly: boolean): void {
    this.store.dispatch(
      ModelActions.updateFavoritesFilter({ showFavoritesOnly })
    );
  }

  /**
   * Handle favorite toggle
   * Implementation requirements 3.1: When user clicks favorite star icon, toggle the model's Favorite_Status
   */
  onFavoriteToggle(modelId: number): void {
    this.store.dispatch(ModelActions.toggleFavorite({ modelId }));
  }

  /**
   * Handle action menu click (from ModelsTableComponent)
   * Implementation requirements 5.2: When user clicks kebab menu, display available model actions
   */
  onActionMenuClick(event: { modelId: number; action: string }): void {
    const { modelId, action } = event;
    const model = this.filteredModels.find((m) => m.id === modelId);

    if (!model) {
      console.warn(`Model with ID ${modelId} not found`);
      return;
    }

    switch (action) {
      case "copyId":
        this.handleCopyIdAction(model);
        break;
      case "testModel":
        this.handleTestModelAction(model);
        break;
      case "view":
        this.handleViewAction(model);
        break;
      case "edit":
        this.handleEditAction(model);
        break;
      case "download":
        this.handleDownloadAction(model);
        break;
      case "duplicate":
        this.handleDuplicateAction(model);
        break;
      case "delete":
        this.handleDeleteAction(model);
        break;
      default:
        console.warn("Unknown action:", action);
    }
  }

  /**
   * Handle copy model ID action
   * Implementation requirements 12.1, 12.2: Copy model ID to clipboard
   */
  private handleCopyIdAction(model: ModelDisplayDto): void {
    const modelId = model.id.toString();

    // Use Clipboard API to copy model ID
    navigator.clipboard
      .writeText(modelId)
      .then(() => {
        this.snackBar.open("Model ID copied to clipboard", "Close", {
          duration: 2000,
        });
      })
      .catch((err) => {
        console.error("Failed to copy Model ID:", err);
        this.snackBar.open("Failed to copy Model ID", "Close", {
          duration: 3000,
        });
      });
  }

  /**
   * Handle test model action
   * Opens the Test Model dialog with model information
   */
  private handleTestModelAction(model: ModelDisplayDto): void {
    console.log("Opening Test Model dialog for:", model);

    // 從 displayModels 取得 model 的完整資訊 (包含 projectId)
    // 需要呼叫 API 取得完整的 Model 資料
    this.modelService.getModelById(model.id).subscribe({
      next: (fullModel: any) => {
        // Open Test Model dialog
        const dialogRef = this.dialog.open(TestModelDialogComponent, {
          width: "90vw",
          maxWidth: "1400px",
          height: "95vh", // 從 90vh 增加到 95vh
          maxHeight: "1000px", // 從 900px 增加到 1000px
          data: {
            modelFullName: model.modelFullName || model.modelName,
            version: model.version || 1,
            trackId: model.trackId || "",
            modelId: model.id,
            projectId: fullModel.projectId, // 從完整 model 取得 projectId
          },
          disableClose: false,
          panelClass: "test-model-dialog-container",
        });

        dialogRef.afterClosed().subscribe((result: any) => {
          console.log("Test Model dialog closed:", result);
        });
      },
      error: (error: any) => {
        console.error("Failed to load model details:", error);
        this.snackBar.open("Failed to open Test Model dialog", "Close", {
          duration: 3000,
        });
      },
    });
  }

  /**
   * Handle view action
   */
  private handleViewAction(model: ModelDisplayDto): void {
    console.log("View model details:", model);
    // TODO: Implement view model details functionality
    // May navigate to details page or open modal dialog
  }

  /**
   * Handle edit action
   */
  private handleEditAction(model: ModelDisplayDto): void {
    console.log("Edit model:", model);
    // TODO: Implement edit model functionality
    // May navigate to edit page or open edit dialog
  }

  /**
   * Handle download action
   * Implementation requirements 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10:
   * Download model from Databricks
   */
  private handleDownloadAction(model: ModelDisplayDto): void {
    // Validate required fields (Requirements 13.3, 13.4, 13.5, 13.10)
    if (!model.modelFullName || !model.version || !model.trackId) {
      this.snackBar.open(
        "Cannot download model: missing required information",
        "Close",
        {
          duration: 3000,
        }
      );
      return;
    }

    // Show loading notification (Requirement 13.9)
    this.snackBar.open("Preparing download...", "", { duration: 2000 });

    // Call Databricks API (Requirements 13.2, 13.3, 13.4, 13.5)
    this.downloadModelService
      .downloadModel(model.modelFullName, model.version, model.trackId)
      .subscribe({
        next: (response) => {
          // Extract download URL (Requirement 13.6)
          if (response.artifact?.downloadUrl) {
            // Open download URL in new tab (Requirement 13.7)
            window.open(response.artifact.downloadUrl, "_blank");
            this.snackBar.open("Download started", "Close", { duration: 2000 });
          } else {
            // Handle missing download URL (Requirement 13.8)
            this.snackBar.open("Download URL not available", "Close", {
              duration: 3000,
            });
          }
        },
        error: (err) => {
          // Handle API errors (Requirements 13.8, 13.10)
          console.error("Download model error:", err);
          this.snackBar.open("Failed to download model", "Close", {
            duration: 3000,
          });
        },
      });
  }

  /**
   * Handle duplicate action
   */
  private handleDuplicateAction(model: ModelDisplayDto): void {
    console.log("Duplicate model:", model);
    // TODO: Implement duplicate model functionality
    // May open duplicate dialog or directly duplicate
  }

  /**
   * Handle delete action
   * Implementation requirements 11.1, 11.2, 11.3: Delete model with confirmation
   */
  private handleDeleteAction(model: ModelDisplayDto): void {
    // Open confirmation dialog
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: "400px",
    });

    // Set the dialog component inputs
    dialogRef.componentInstance.title = "Delete Model";
    dialogRef.componentInstance.message = `Are you sure you want to delete "${model.modelName}"?`;
    dialogRef.componentInstance.btnOkText = "Delete";
    dialogRef.componentInstance.btnCancelText = "Cancel";

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        // User confirmed deletion
        this.modelService.deleteModel(model.id).subscribe({
          next: () => {
            // Success: reload model list and show success message
            this.snackBar.open("Model deleted successfully", "Close", {
              duration: 3000,
            });
            this.onRefresh();
          },
          error: (err) => {
            // Error: show error message and keep model in list
            console.error("Failed to delete model:", err);
            this.snackBar.open("Failed to delete model", "Close", {
              duration: 3000,
            });
          },
        });
      }
    });
  }

  /**
   * Reload data
   */
  onRefresh(): void {
    if (this.projectId) {
      this.loadModelsByProject(this.projectId);
    } else {
      this.loadModels();
    }
  }

  /**
   * Clear error message
   */
  clearError(): void {
    this.store.dispatch(ModelActions.clearError());
  }

  /**
   * Format metric as percentage display
   * Implementation requirements 1.3, 7.2: Consistently format percentage values
   */
  formatMetric(value: number | null): string {
    return formatMetricAsPercentage(value);
  }

  /**
   * Format confidence threshold
   * Implementation requirements 1.4, 7.4: Display confidence threshold value accurate to two decimal places
   */
  formatThreshold(value: number): string {
    return formatConfidenceThreshold(value);
  }

  /**
   * Handle model row click
   * Implementation requirements 10.1, 10.2, 10.3, 10.4: Open Model Detail Panel with appropriate tab
   * Always default to Training tab
   */
  onModelRowClick(event: ModelRowClickEvent): void {
    // Always open with Training tab as default
    this.store.dispatch(
      ModelDetailActions.openPanel({
        modelId: event.modelId,
        initialTab: "training",
      })
    );
  }
}
