import { Component, OnDestroy, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { combineLatest, Observable, Subject } from "rxjs";
import { filter, map, take, takeUntil } from "rxjs/operators";

// NgRx
import { Store } from "@ngrx/store";

// Angular Material Modules
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSelectModule } from "@angular/material/select";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";

// Auto Split Dialog
import {
  AutoSplitDialogComponent,
  AutoSplitDialogData,
} from "../../image-upload/auto-split-dialog/auto-split-dialog.component";

// Project Service — to load project detail (locationId, type)
import { ProjectService } from "app/services/landingai/project.service";

// State Management, Models, and Components
import { ModelParameterComponent } from "../model-parameter/model-parameter.component";
import {
  addModelConfig,
  AugmentationConfig,
  AugmentationsConfigComponent,
  clearError,
  HyperparametersConfig,
  HyperparametersConfigComponent,
  initializeTraining,
  loadSplitPreview,
  ModelConfig,
  nextStep,
  previousStep,
  ProjectClass,
  removeModelConfig,
  resetTrainingState,
  selectAllValidationErrors,
  selectCanStartTraining,
  selectCurrentStep,
  selectDistribution,
  selectError,
  selectHasSplitConfiguration,
  selectIsDistributionValid,
  selectIsFirstStep,
  selectIsFormValid,
  selectIsLastStep,
  selectLabeledImagesBelowMinimum,
  selectLoading,
  selectModelConfigs,
  selectProjectClasses,
  selectProjectId,
  selectSelectedSnapshotId,
  selectSnapshot,
  selectSnapshots,
  selectSplitPreview,
  selectTotalImages,
  selectTrainingRequest,
  selectUnassignedCount,
  Snapshot,
  SplitDistribution,
  SplitPreview,
  SplitPreviewComponent,
  startTraining,
  TrainingRequest,
  TrainingState,
  TransformConfig,
  TransformsConfigComponent,
  updateModelConfig,
  validateTrainingForm,
  ValidationError,
} from "app/state/landingai/ai-training";

/**
 * CustomerTrainingComponent
 *
 * Main container component for the AI training configuration workflow.
 * Provides a two-step wizard interface:
 * - Step 1: Data Setup (split configuration, preview)
 * - Step 2: Model Configuration (hyperparameters, transforms, augmentations)
 *
 * Validates: Requirements 20.1, 20.2, 20.3, 20.4, 20.5, 20.6, 21.1, 21.2, 29.1-29.6, 30.3, 30.4
 * - Displays Back, Next, Start Training, and Cancel buttons (20.1)
 * - Back returns to previous step (20.2)
 * - Next proceeds to next step (20.3)
 * - Back button disabled on first step (20.4)
 * - Start Training shown on last step instead of Next (20.5)
 * - Cancel returns to previous page without saving (20.6)
 * - Validates all configuration parameters (21.1)
 * - Displays error messages for invalid fields (21.2)
 * - Validates Epoch value between 1 and 100 (29.1)
 * - Validates split distribution percentages sum to 100 (29.2)
 * - Validates all required fields are filled (29.3)
 * - Validates numeric inputs contain valid numbers (29.4)
 * - Displays inline error messages below invalid fields (29.5)
 * - Disables Next/Start Training button when validation fails (29.6)
 * - Loads when user navigates to /landingai/ai-training/:projectId (30.3)
 * - Extracts projectId from route parameters (30.4)
 */
@Component({
  selector: "app-customer-training",
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDialogModule,
    SplitPreviewComponent,
    HyperparametersConfigComponent,
    TransformsConfigComponent,
    ModelParameterComponent,
  ],
  templateUrl: "./customer-training.component.html",
  styleUrls: ["./customer-training.component.scss"],
})
export class CustomerTrainingComponent implements OnInit, OnDestroy {
  /** Maximum number of steps in the wizard */
  readonly maxSteps = 2;

  /** Maximum number of model configurations allowed */
  readonly maxModelConfigs = 9;

  // ============================================================================
  // NgRx State Observables
  // ============================================================================

  /** Available data snapshots */
  snapshots$: Observable<Snapshot[]>;

  /** Current split preview data */
  splitPreview$: Observable<SplitPreview | null>;

  /** Target split distribution percentages */
  distribution$: Observable<SplitDistribution>;

  /** Model configurations for training */
  modelConfigs$: Observable<ModelConfig[]>;

  /** Current wizard step (1 = data setup, 2 = model config) */
  currentStep$: Observable<number>;

  /** Loading state for async operations */
  isLoading$: Observable<boolean>;

  /** Error message if any operation failed */
  error$: Observable<string | null>;

  /** Current project ID from state */
  projectId$: Observable<number | null>;

  /** Count of unassigned images */
  unassignedCount$: Observable<number>;

  /** Total images count */
  totalImages$: Observable<number>;

  /** Whether user is on the first step */
  isFirstStep$: Observable<boolean>;

  /** Whether user is on the last step */
  isLastStep$: Observable<boolean>;

  /** Whether training can be started */
  canStartTraining$: Observable<boolean>;

  /** Training request payload */
  trainingRequest$: Observable<TrainingRequest | null>;

  /** Currently selected snapshot ID */
  selectedSnapshotId$: Observable<number | null>;

  /** Available project classes */
  projectClasses$: Observable<ProjectClass[]>;

  /** All validation errors */
  validationErrors$: Observable<{ field: string; message: string }[]>;

  /** Whether the form is valid */
  isFormValid$: Observable<boolean>;

  /** Whether the distribution is valid */
  isDistributionValid$: Observable<boolean>;

  /** Whether project has split configuration */
  hasSplitConfiguration$: Observable<boolean>;

  /** Whether labeled images count is below minimum required for training */
  labeledImagesBelowMinimum$: Observable<boolean>;
  /** Track collapsed state per model config index */
  collapsedModels: Set<number> = new Set();
  /** Current location ID for passing to hyperparameters config */
  currentLocationId: number | null = null;
  /** Current project model type for passing to hyperparameters config */
  currentModelType: string | null = null;
  /** Model aliases generated from project ID and current timestamp for each model */
  private modelAliases: string[] = [];
  /** Current project ID stored for generating model aliases */
  private currentProjectId: number | null = null;
  /** Subject for managing subscription cleanup */
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private store: Store<{ training: TrainingState }>,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private projectService: ProjectService
  ) {
    // Initialize observables from store
    this.snapshots$ = this.store.select(selectSnapshots);
    this.splitPreview$ = this.store.select(selectSplitPreview);
    this.distribution$ = this.store.select(selectDistribution);
    this.modelConfigs$ = this.store.select(selectModelConfigs);
    this.currentStep$ = this.store.select(selectCurrentStep);
    this.isLoading$ = this.store.select(selectLoading);
    this.error$ = this.store.select(selectError);
    this.projectId$ = this.store.select(selectProjectId);
    this.unassignedCount$ = this.store.select(selectUnassignedCount);
    this.totalImages$ = this.store.select(selectTotalImages);
    this.isFirstStep$ = this.store.select(selectIsFirstStep);
    this.isLastStep$ = this.store.select(selectIsLastStep);
    this.canStartTraining$ = this.store.select(selectCanStartTraining);
    this.trainingRequest$ = this.store.select(selectTrainingRequest);
    this.selectedSnapshotId$ = this.store.select(selectSelectedSnapshotId);
    this.projectClasses$ = this.store.select(selectProjectClasses);
    this.validationErrors$ = this.store.select(selectAllValidationErrors);
    this.isFormValid$ = this.store.select(selectIsFormValid);
    this.isDistributionValid$ = this.store.select(selectIsDistributionValid);
    this.hasSplitConfiguration$ = this.store.select(
      selectHasSplitConfiguration
    );
    this.labeledImagesBelowMinimum$ = this.store.select(
      selectLabeledImagesBelowMinimum
    );
  }

  ngOnInit(): void {
    // Extract projectId from route parameters (Requirement 30.4)
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const projectIdParam = params.get("projectId");
      if (projectIdParam) {
        const projectId = parseInt(projectIdParam, 10);
        if (!isNaN(projectId)) {
          this.initializeTrainingState(projectId);
          // Load project detail to get locationId and type for model size loading
          this.projectService
            .getProjectById(projectId)
            .pipe(takeUntil(this.destroy$))
            .subscribe((project) => {
              this.currentLocationId = project.locationId ?? null;
              this.currentModelType = project.type ?? null;
            });
        }
      }
    });

    // Subscribe to error state to display error messages
    this.error$
      .pipe(
        takeUntil(this.destroy$),
        filter((error): error is string => error !== null)
      )
      .subscribe((error) => {
        this.showErrorMessage(error);
      });
  }

  ngOnDestroy(): void {
    // Reset state when leaving the component
    this.store.dispatch(resetTrainingState());
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Navigate to the next step in the wizard
   * Validates: Requirement 20.3
   * WHEN a user clicks Next, THE System SHALL proceed to the next configuration step
   */
  onNext(): void {
    this.store.dispatch(nextStep());
  }

  /**
   * Handle snapshot selection change
   * Validates: Requirements 1.3, 1.4
   * WHEN a user selects a different snapshot, THE System SHALL update the split preview data accordingly
   * THE System SHALL store the selected snapshot ID for the training request
   *
   * @param snapshotId The selected snapshot ID, or null for "Current version"
   */
  onSnapshotChange(snapshotId: number | null): void {
    this.store.dispatch(selectSnapshot({ snapshotId }));
  }

  /**
   * Get the selected snapshot object by ID
   * Used by the template to display the selected snapshot name in the dropdown trigger
   *
   * @param snapshotId The selected snapshot ID
   * @param snapshots The list of available snapshots
   * @returns The selected snapshot or null if not found
   */
  getSelectedSnapshot(
    snapshotId: number | null,
    snapshots: Snapshot[] | null
  ): Snapshot | null {
    if (snapshotId === null || !snapshots) {
      return null;
    }
    return snapshots.find((s) => s.id === snapshotId) || null;
  }

  /**
   * Open the auto-split dialog to configure and assign splits
   * When the dialog closes with a result, refresh the split preview
   */
  openAutoSplitDialog(): void {
    this.projectId$.pipe(take(1)).subscribe((projectId) => {
      if (projectId === null) {
        this.showErrorMessage("No project selected");
        return;
      }

      const dialogData: AutoSplitDialogData = { projectId };

      const dialogRef = this.dialog.open(AutoSplitDialogComponent, {
        width: "800px",
        maxHeight: "90vh",
        data: dialogData,
        disableClose: true,
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          // Refresh split preview after successful split assignment
          this.store.dispatch(
            loadSplitPreview({
              projectId,
              snapshotId: undefined,
            })
          );
        }
      });
    });
  }

  /**
   * Navigate to the previous step in the wizard
   * Validates: Requirement 20.2
   * WHEN a user clicks Back, THE System SHALL return to the previous configuration step
   */
  onBack(): void {
    this.store.dispatch(previousStep());
  }

  /**
   * Cancel the training configuration and return to the previous page
   * Validates: Requirement 20.6
   * WHEN a user clicks Cancel, THE System SHALL return to the previous page without saving
   */
  onCancel(): void {
    // Get current project ID from state synchronously for navigation
    let projectId: number | null = null;
    this.projectId$
      .pipe(takeUntil(this.destroy$))
      .subscribe((id) => {
        projectId = id;
      })
      .unsubscribe();

    // Navigate back to the project page
    if (projectId) {
      this.router.navigate(["/landingai/projects", projectId]);
    } else {
      this.router.navigate(["/landingai/projects"]);
    }
  }

  /**
   * Start the training process
   * Validates: Requirements 21.1, 21.2, 21.3, 21.4, 21.6, 21.7
   * - WHEN a user clicks Start Training, THE System SHALL validate all configuration parameters (21.1)
   * - IF validation fails, THEN THE System SHALL display error messages for invalid fields (21.2)
   * - WHEN validation passes, THE System SHALL send a training request to the backend API (21.3)
   * - THE System SHALL support multiple model configurations in a single training request (21.4)
   * - WHEN training starts successfully, THE System SHALL display a success message (21.6)
   * - IF training fails to start, THEN THE System SHALL display an error message with details (21.7)
   */
  onStartTraining(): void {
    // Get the training request and distribution from state using combineLatest for atomic read
    combineLatest([
      this.trainingRequest$,
      this.distribution$,
      this.modelConfigs$,
      this.projectId$,
      this.selectedSnapshotId$,
    ])
      .pipe(take(1))
      .subscribe(
        ([request, distribution, modelConfigs, projectId, snapshotId]) => {
          // Build the complete training request
          const trainingRequest = this.buildTrainingRequest(
            projectId,
            snapshotId,
            modelConfigs
          );

          if (!trainingRequest) {
            this.showErrorMessage(
              "Unable to start training: Invalid project configuration"
            );
            return;
          }

          // Validate the form before starting training
          const validationResult = validateTrainingForm(
            distribution,
            trainingRequest
          );

          if (!validationResult.isValid) {
            // Display validation errors
            this.displayValidationErrors(validationResult.errors);
            return;
          }

          // Dispatch the start training action
          this.store.dispatch(startTraining({ request: trainingRequest }));
        }
      );
  }

  /**
   * Check if a specific field has a validation error
   * Validates: Requirement 29.5
   * THE System SHALL display inline error messages below the invalid fields
   *
   * @param field Field name to check
   * @returns Observable of whether the field has an error
   */
  hasFieldError(field: string): Observable<boolean> {
    return this.validationErrors$.pipe(
      map((errors) => errors.some((e) => e.field === field))
    );
  }

  /**
   * Get the error message for a specific field
   * Validates: Requirement 29.5
   * THE System SHALL display inline error messages below the invalid fields
   *
   * @param field Field name to get error for
   * @returns Error message or null
   */
  getFieldErrorMessage(field: string): string | null {
    let errorMessage: string | null = null;
    this.validationErrors$.pipe(take(1)).subscribe((errors) => {
      const error = errors.find((e) => e.field === field);
      errorMessage = error ? error.message : null;
    });
    return errorMessage;
  }

  /**
   * Handle hyperparameters configuration change
   * Validates: Requirements 5.5, 6.5
   * WHEN a user changes the Epoch value, THE System SHALL synchronize both controls (5.5)
   * WHEN a user selects a different model size, THE System SHALL update the model configuration (6.5)
   *
   * @param config The updated hyperparameters configuration
   * @param modelIndex The index of the model configuration to update (default: 0)
   */
  onHyperparametersChange(
    config: HyperparametersConfig,
    modelIndex: number = 0
  ): void {
    this.store.dispatch(
      updateModelConfig({
        index: modelIndex,
        config: {
          epochs: config.epochs,
          modelSize: config.modelSize,
        },
      })
    );
  }

  /**
   * Handle transforms configuration change
   * Validates: Requirements 7.1, 7.2, 7.3, 7.6, 7.7
   * Updates the model configuration with the new transform settings
   *
   * @param config The updated transforms configuration
   * @param modelIndex The index of the model configuration to update (default: 0)
   */
  onTransformsChange(config: TransformConfig, modelIndex: number = 0): void {
    this.store.dispatch(
      updateModelConfig({
        index: modelIndex,
        config: {
          transforms: config,
        },
      })
    );
  }

  /**
   * Handle augmentations configuration change
   * Validates: Requirements 10.1, 10.2, 10.3, 11.1, 11.2, 11.3, 11.4, 11.5
   * Updates the model configuration with the new augmentation settings
   *
   * @param config The updated augmentations configuration
   * @param modelIndex The index of the model configuration to update (default: 0)
   */
  onAugmentationsChange(
    config: AugmentationConfig,
    modelIndex: number = 0
  ): void {
    this.store.dispatch(
      updateModelConfig({
        index: modelIndex,
        config: {
          augmentations: config,
        },
      })
    );
  }

  /**
   * Handle model parameters change from the tree editor
   * @param params The JSON string from the tree editor
   * @param modelIndex The index of the model configuration to update
   */
  onModelParametersChange(params: string, modelIndex: number = 0): void {
    this.store.dispatch(
      updateModelConfig({
        index: modelIndex,
        config: {
          modelParam: params,
        },
      })
    );
  }

  /**
   * Add a new model configuration
   * Validates: Requirement 21.4
   * THE System SHALL support multiple model configurations in a single training request
   * Maximum 9 model configurations allowed
   */
  onAddModel(): void {
    this.modelConfigs$.pipe(take(1)).subscribe((configs) => {
      if (configs.length < this.maxModelConfigs) {
        // Generate alias for the new model
        if (this.currentProjectId !== null) {
          this.modelAliases.push(
            this.generateModelAlias(this.currentProjectId, configs.length)
          );
        }
        this.store.dispatch(addModelConfig());
      }
    });
  }

  /**
   * Remove a model configuration by index
   * @param index The index of the model configuration to remove
   */
  onRemoveModel(index: number): void {
    this.modelConfigs$.pipe(take(1)).subscribe((configs) => {
      if (configs.length > 1) {
        // Remove the alias at the given index
        this.modelAliases.splice(index, 1);
        this.store.dispatch(removeModelConfig({ index }));
      }
    });
  }

  /**
   * Get the model alias for a specific model index
   * @param index The index of the model
   * @returns The model alias string
   */
  /**
   * TrackBy function for ngFor to prevent component destruction/recreation
   * when the modelConfigs array reference changes in the store.
   */
  trackByIndex(index: number): number {
    return index;
  }

  getModelAlias(index: number): string {
    // Ensure alias exists for this index
    if (index >= this.modelAliases.length && this.currentProjectId !== null) {
      this.modelAliases[index] = this.generateModelAlias(
        this.currentProjectId,
        index
      );
    }
    return this.modelAliases[index] || "";
  }

  toggleModelCollapse(index: number): void {
    if (this.collapsedModels.has(index)) {
      this.collapsedModels.delete(index);
    } else {
      this.collapsedModels.add(index);
    }
  }

  isModelCollapsed(index: number): boolean {
    return this.collapsedModels.has(index);
  }

  /**
   * Initialize training state with project ID and load initial data
   * @param projectId The project ID to initialize with
   */
  private initializeTrainingState(projectId: number): void {
    // Store project ID for generating model aliases
    this.currentProjectId = projectId;

    // Generate initial model alias for the first model
    this.modelAliases = [this.generateModelAlias(projectId, 0)];

    // Initialize training state with project ID
    // Note: loadSnapshots, loadSplitPreview, and loadProjectClasses are triggered
    // automatically by effects when initializeTraining is dispatched
    this.store.dispatch(initializeTraining({ projectId }));
  }

  /**
   * Generate model alias based on project ID, model index, and current timestamp
   * Format: MODEL-PG{projectId}-{year}-{month}-{day}-{timestamp}
   * @param projectId The project ID
   * @param modelIndex The index of the model (used to ensure unique timestamps)
   * @returns The generated model alias
   */
  private generateModelAlias(
    projectId: number,
    modelIndex: number = 0
  ): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    // Add modelIndex to timestamp to ensure uniqueness when adding multiple models quickly
    const timestamp = now.getTime() + modelIndex;
    return `MODEL-PJ${projectId}-${year}-${month}-${day}-${timestamp}`;
  }

  /**
   * Display error message using snackbar
   * @param message Error message to display
   */
  private showErrorMessage(message: string): void {
    this.snackBar.open(message, "Close", {
      duration: 5000,
      horizontalPosition: "center",
      verticalPosition: "bottom",
      panelClass: ["error-snackbar"],
    });
    // Clear error after displaying
    this.store.dispatch(clearError());
  }

  /**
   * Build the training request from current state
   * Validates: Requirement 21.4
   * THE System SHALL support multiple model configurations in a single training request
   *
   * @param projectId The project ID
   * @param snapshotId The selected snapshot ID (optional)
   * @param modelConfigs The model configurations
   * @returns The training request or null if invalid
   */
  private buildTrainingRequest(
    projectId: number | null,
    snapshotId: number | null,
    modelConfigs: ModelConfig[]
  ): TrainingRequest | null {
    if (projectId === null || projectId <= 0) {
      return null;
    }

    if (!modelConfigs || modelConfigs.length === 0) {
      return null;
    }

    return {
      projectId,
      snapshotId: snapshotId ?? undefined,
      modelConfigs: modelConfigs.map((config, index) => ({
        ...config,
        // Use the generated model alias from the component
        modelAlias: this.getModelAlias(index),
        // Set status to PENDING
        status: "PENDING",
        epochs: config.epochs,
        modelSize: config.modelSize,
        transforms: config.transforms || {},
        modelParam: config.modelParam || undefined,
        augmentations: config.modelParam
          ? undefined
          : config.augmentations || {},
      })),
    };
  }

  /**
   * Display validation errors to the user
   * Validates: Requirement 21.2, 29.5
   * IF validation fails, THEN THE System SHALL display error messages for invalid fields
   * THE System SHALL display inline error messages below the invalid fields
   *
   * @param errors List of validation errors to display
   */
  private displayValidationErrors(errors: ValidationError[]): void {
    if (errors.length === 0) {
      return;
    }

    // Show the first error in a snackbar
    const firstError = errors[0];
    this.snackBar.open(firstError.message, "Close", {
      duration: 5000,
      horizontalPosition: "center",
      verticalPosition: "bottom",
      panelClass: ["error-snackbar"],
    });

    // If there are multiple errors, show a summary
    if (errors.length > 1) {
      console.warn("Validation errors:", errors);
    }
  }
}
