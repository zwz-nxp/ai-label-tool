import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { Store } from "@ngrx/store";
import { combineLatest, forkJoin, Subject } from "rxjs";
import { debounceTime, takeUntil } from "rxjs/operators";
import { Project } from "app/models/landingai/project";
import { Image } from "app/models/landingai/image.model";
import {
  Annotation,
  AnnotationType,
} from "app/models/landingai/annotation.model";
import { ProjectClass } from "app/models/landingai/project-class.model";
import { EnhanceSettings } from "app/models/landingai/enhance-settings.model";
import { Person } from "app/models/person";
import { RoleEnum } from "app/models/role";
import { ImageService } from "app/services/landingai/image.service";
import { ProjectService } from "app/services/landingai/project.service";
import { ProjectClassService } from "app/services/landingai/project-class.service";
import { ImageLabel, LabelService } from "app/services/landingai/label.service";
import { ModelPredictionService } from "app/services/landingai/model-prediction.service";
import { ModelService } from "app/services/landingai/model.service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { ClassCreationDialogComponent } from "../class-creation-dialog/class-creation-dialog.component";
import { DEFAULT_PROJECT_CLASSES } from "../default-project-config";

/**
 * Toolbar button configuration
 */
export interface ToolbarButton {
  id: string;
  label: string;
  icon: string;
  tooltip: string;
}

/**
 * Component state interface
 */
interface ComponentState {
  annotations: Annotation[];
  selectedAnnotation: Annotation | null;
  selectedAnnotations: Annotation[];
  selectedClass: ProjectClass | null;
  enhanceSettings: EnhanceSettings;
  annotationHistory: Annotation[][];
  historyIndex: number;
  showLabels: boolean;
  showPredictions: boolean;
  showLabelDetails: boolean;
}

/**
 * Main component for image annotation functionality
 * Supports Object Detection, Segmentation, and Classification project types
 */
@Component({
  selector: "app-image-labelling",
  templateUrl: "./image-labelling.component.html",
  styleUrls: ["./image-labelling.component.scss"],
  standalone: false,
})
export class ImageLabellingComponent implements OnInit, OnDestroy {
  // Remove @Input() decorator - we'll get data from route params
  currentImage!: Image;

  // Project information loaded from currentImage.projectId
  project: Project | null = null;

  // Current image file as Blob URL for display
  currentImageUrl: string | null = null;

  // Component state
  state: ComponentState = {
    annotations: [],
    selectedAnnotation: null,
    selectedAnnotations: [],
    selectedClass: null,
    enhanceSettings: {
      brightness: 0,
      contrast: 0,
    },
    annotationHistory: [],
    historyIndex: -1,
    showLabels: true,
    showPredictions: true,
    showLabelDetails: false,
  };

  // Data collections
  images: Image[] = [];
  classes: ProjectClass[] = [];

  // Toolbar configuration
  toolbarButtons: ToolbarButton[] = [];

  // Loading states
  isLoadingImages = false;
  isLoadingClasses = false;
  isLoadingAnnotations = false;
  isGeneratingPreAnnotations = false;

  // Save status
  saveStatus: "idle" | "saving" | "saved" | "error" = "idle";
  saveStatusMessage = "";

  // Pre-annotation state
  modelPredictionEnabled = false;
  preAnnotations: Annotation[] = [];
  selectedModelName: string = "";

  // UI state
  isPanMode = false;
  zoomLevel = 1.0;
  hideAnnotations = false;
  isLeftSidebarOpen = true; // Default shown to display image list
  isRightSidebarOpen = true; // Default shown
  expandedPanel:
    | "general"
    | "tags"
    | "metadata"
    | "labels"
    | "predictions"
    | "preAnnotations"
    | null = "general"; // Mutually exclusive panels
  activeTool:
    | "pan"
    | "boundingBox"
    | "polygon"
    | "brush"
    | "polyline"
    | "none" = "none";
  // Current logged-in user
  currentUser: Person = new Person();
  // Role for authorization check
  public readonly role = RoleEnum.ADMINISTRATOR_SYSTEM;
  // Destroy subject for cleanup
  private destroy$ = new Subject<void>();
  // Subject for debouncing save operations
  private saveAnnotation$ = new Subject<Annotation>();
  private projectId: number = 0;
  private modelId: number = 0;
  // Store query params for navigation back to upload page with same context
  private uploadPageQueryParams: any = {};

  constructor(
    private imageService: ImageService,
    private projectService: ProjectService,
    private projectClassService: ProjectClassService,
    private labelService: LabelService,
    private modelPredictionService: ModelPredictionService,
    private modelService: ModelService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private store: Store,
    public authorizationService: AuthorizationService
  ) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        if (user) {
          this.currentUser = user;
        }
      });
  }

  ngOnInit(): void {
    // Set default mode to bounding box tool when entering labeling interface
    this.isPanMode = false;
    this.activeTool = "boundingBox";

    // Get projectId and imageId from route parameters, and pagination from query params
    combineLatest([this.route.params, this.route.queryParams])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([params, queryParams]) => {
        this.projectId = +params["projectId"];
        const imageId = +params["imageId"];

        // Store query params for navigation back to upload page
        this.uploadPageQueryParams = { ...queryParams };

        // Extract modelId from query params
        if (queryParams["modelId"]) {
          this.modelId = +queryParams["modelId"];
        }

        if (this.projectId && imageId) {
          // Use forkJoin to wait for both project and image to load
          // This prevents race condition where loadAnnotationsForCurrentImage()
          // is called before currentImage is set
          forkJoin({
            project: this.projectService.getProjectById(this.projectId),
            image: this.imageService.getImageById(imageId),
          })
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: ({ project, image }) => {
                // 1. Set project and currentImage
                this.project = project;
                this.currentImage = image;

                // 2. Initialize toolbar buttons based on project type
                this.toolbarButtons = this.getToolbarButtons(project.type);

                // 3. Load image file for display
                this.loadImageFile(image.id);

                // 4. Setup keyboard listeners and debounced save
                this.setupKeyboardListeners();
                this.setupDebouncedSave();

                // 5. Load related data with pagination context from query params
                this.loadImagesWithPagination(queryParams);
                // Load classes first, then load annotations after classes are ready
                // This ensures class info is available when converting labels to annotations
                this.loadClasses(true);
                this.checkModelPredictionStatus();
                this.loadModelInfo();
              },
              error: (error) => {
                console.error("Error loading project or image:", error);
                alert(`Failed to load: ${error.message}`);
              },
            });
        } else {
          console.error("Missing projectId or imageId in route parameters");
        }
      });
  }

  ngOnDestroy(): void {
    // Clean up image URL
    if (this.currentImageUrl) {
      URL.revokeObjectURL(this.currentImageUrl);
    }

    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check if current user is authorized to perform labelling operations
   * @returns boolean indicating if user is authorized
   */
  public isAuthorized(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      "Administrator_System"
    );
  }

  /**
   * Toggle left sidebar visibility
   */
  toggleLeftSidebar(): void {
    this.isLeftSidebarOpen = !this.isLeftSidebarOpen;
    // Trigger canvas resize after sidebar animation completes
    this.triggerCanvasResize();
  }

  /**
   * Toggle right sidebar visibility
   */
  toggleRightSidebar(): void {
    this.isRightSidebarOpen = !this.isRightSidebarOpen;
    // Trigger canvas resize after sidebar animation completes
    this.triggerCanvasResize();
  }

  /**
   * Handle expansion panel opened event
   * Implements mutually exclusive behavior for all panels
   * @param panelName The name of the panel that was opened
   */
  onPanelOpened(
    panelName:
      | "general"
      | "tags"
      | "metadata"
      | "labels"
      | "predictions"
      | "preAnnotations"
  ): void {
    this.expandedPanel = panelName;
  }

  /**
   * Get toolbar buttons based on project type
   * @param projectType The type of the project
   * @returns Array of toolbar button configurations
   */
  getToolbarButtons(projectType: string): ToolbarButton[] {
    switch (projectType) {
      case "Object Detection":
        return [
          { id: "pan", label: "Pan", icon: "pan_tool", tooltip: "Pan mode" },
          {
            id: "boundingBox",
            label: "Bounding Box",
            icon: "crop_square",
            tooltip: "Draw bounding box",
          },
          {
            id: "undo",
            label: "Undo",
            icon: "undo",
            tooltip: "Undo last action",
          },
          {
            id: "redo",
            label: "Redo",
            icon: "redo",
            tooltip: "Redo last action",
          },
          {
            id: "clear",
            label: "Clear",
            icon: "delete_sweep",
            tooltip: "Clear all annotations",
          },
          {
            id: "zoomIn",
            label: "Zoom In",
            icon: "zoom_in",
            tooltip: "Zoom in",
          },
          {
            id: "zoomOut",
            label: "Zoom Out",
            icon: "zoom_out",
            tooltip: "Zoom out",
          },
          {
            id: "enhance",
            label: "Enhance",
            icon: "tune",
            tooltip: "Adjust brightness and contrast",
          },
          {
            id: "holdToHide",
            label: "Hold to Hide",
            icon: "visibility_off",
            tooltip: "Hold to hide annotations",
          },
        ];

      case "Segmentation":
        return [
          { id: "pan", label: "Pan", icon: "pan_tool", tooltip: "Pan mode" },
          {
            id: "smartLabeling",
            label: "Smart Labeling",
            icon: "auto_fix_high",
            tooltip: "Smart labeling tool",
          },
          {
            id: "brush",
            label: "Brush",
            icon: "brush",
            tooltip: "Brush annotation",
          },
          {
            id: "polygon",
            label: "Polygon",
            icon: "polyline",
            tooltip: "Draw polygon",
          },
          {
            id: "polyline",
            label: "Polyline",
            icon: "timeline",
            tooltip: "Draw polyline",
          },
          {
            id: "undo",
            label: "Undo",
            icon: "undo",
            tooltip: "Undo last action",
          },
          {
            id: "redo",
            label: "Redo",
            icon: "redo",
            tooltip: "Redo last action",
          },
          {
            id: "clear",
            label: "Clear",
            icon: "delete_sweep",
            tooltip: "Clear all annotations",
          },
          {
            id: "zoomIn",
            label: "Zoom In",
            icon: "zoom_in",
            tooltip: "Zoom in",
          },
          {
            id: "zoomOut",
            label: "Zoom Out",
            icon: "zoom_out",
            tooltip: "Zoom out",
          },
          {
            id: "enhance",
            label: "Enhance",
            icon: "tune",
            tooltip: "Adjust brightness and contrast",
          },
          {
            id: "holdToHide",
            label: "Hold to Hide",
            icon: "visibility_off",
            tooltip: "Hold to hide annotations",
          },
        ];

      case "Classification":
        return [
          {
            id: "zoomIn",
            label: "Zoom In",
            icon: "zoom_in",
            tooltip: "Zoom in",
          },
          {
            id: "zoomOut",
            label: "Zoom Out",
            icon: "zoom_out",
            tooltip: "Zoom out",
          },
          {
            id: "enhance",
            label: "Enhance",
            icon: "tune",
            tooltip: "Adjust brightness and contrast",
          },
        ];

      default:
        return [];
    }
  }

  /**
   * Handle image selection from left toolbar
   * @param image The selected image
   */
  onImageSelected(image: Image): void {
    this.selectImage(image);
  }

  /**
   * Get current image index in the images array
   * @returns The index of the current image, or 0 if not found
   */
  getCurrentImageIndex(): number {
    if (!this.currentImage || this.images.length === 0) {
      return 0;
    }
    const index = this.images.findIndex(
      (img) => img.id === this.currentImage.id
    );
    return index >= 0 ? index : 0;
  }

  /**
   * Handle image navigation from top toolbar
   * @param direction 'previous' or 'next'
   */
  onNavigateImage(direction: "previous" | "next"): void {
    const currentIndex = this.getCurrentImageIndex();

    if (direction === "previous" && currentIndex > 0) {
      this.selectImage(this.images[currentIndex - 1]);
    } else if (direction === "next" && currentIndex < this.images.length - 1) {
      this.selectImage(this.images[currentIndex + 1]);
    }
  }

  /**
   * Navigate back to project page with preserved pagination and filter context
   */
  onNavigateToProject(): void {
    if (this.project?.id) {
      // Navigate back with the same query params to restore pagination/filter state
      this.router.navigate(["/landingai/projects", this.project.id], {
        queryParams: this.uploadPageQueryParams,
      });
    }
  }

  /**
   * Handle batch export from left toolbar
   * @param images The images to export
   */
  onBatchExport(images: Image[]): void {
    if (images.length === 0) {
      return;
    }

    // Show loading indicator
    const exportingMessage = `Exporting ${images.length} image(s)...`;

    // Import JSZip dynamically
    import("jszip")
      .then((JSZip) => {
        const zip = new JSZip();
        const imageFolder = zip.folder("images");
        const annotationFolder = zip.folder("annotations");

        if (!imageFolder || !annotationFolder) {
          alert("Failed to create ZIP folders");
          return;
        }

        // Track completed operations
        let completedOperations = 0;
        const totalOperations = images.length * 2; // image + annotations for each

        // Process each image
        images.forEach((image) => {
          // Fetch image file
          this.imageService
            .getImageFile(image.fileName)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (blob) => {
                imageFolder.file(image.fileName, blob);
                completedOperations++;
                this.checkExportCompletion(
                  completedOperations,
                  totalOperations,
                  zip,
                  images.length
                );
              },
              error: (error) => {
                console.error(`Error fetching image ${image.fileName}:`, error);
                completedOperations++;
                this.checkExportCompletion(
                  completedOperations,
                  totalOperations,
                  zip,
                  images.length
                );
              },
            });

          // Fetch annotations for this image
          this.labelService
            .getLabelsByImageId(image.id)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (labels) => {
                // Convert labels to annotation format
                const annotations = labels
                  .map((label) => this.imageLabelToAnnotation(label))
                  .filter((annotation) => annotation !== null);

                // Create JSON file with annotations
                const annotationData = {
                  imageId: image.id,
                  fileName: image.fileName,
                  width: image.width,
                  height: image.height,
                  annotations: annotations.map((ann) => ({
                    id: ann!.id,
                    classId: ann!.classId,
                    className: ann!.className,
                    type: ann!.type,
                    color: ann!.color,
                    position:
                      ann!.type === "RECTANGLE"
                        ? {
                            x: ann!.x,
                            y: ann!.y,
                            width: ann!.width,
                            height: ann!.height,
                          }
                        : { points: ann!.points },
                  })),
                };

                const jsonFileName = image.fileName.replace(
                  /\.[^/.]+$/,
                  ".json"
                );
                annotationFolder.file(
                  jsonFileName,
                  JSON.stringify(annotationData, null, 2)
                );
                completedOperations++;
                this.checkExportCompletion(
                  completedOperations,
                  totalOperations,
                  zip,
                  images.length
                );
              },
              error: (error) => {
                console.error(
                  `Error fetching annotations for image ${image.id}:`,
                  error
                );
                // Create empty annotations file
                const annotationData = {
                  imageId: image.id,
                  fileName: image.fileName,
                  width: image.width,
                  height: image.height,
                  annotations: [],
                };
                const jsonFileName = image.fileName.replace(
                  /\.[^/.]+$/,
                  ".json"
                );
                annotationFolder.file(
                  jsonFileName,
                  JSON.stringify(annotationData, null, 2)
                );
                completedOperations++;
                this.checkExportCompletion(
                  completedOperations,
                  totalOperations,
                  zip,
                  images.length
                );
              },
            });
        });
      })
      .catch((error) => {
        console.error("Error loading JSZip:", error);
        alert("Failed to load export library. Please try again.");
      });
  }

  /**
   * Handle batch delete from left toolbar
   * @param images The images to delete
   */
  onBatchDelete(images: Image[]): void {
    if (images.length === 0) {
      return;
    }

    // Show confirmation dialog
    const confirmed = confirm(
      `Are you sure you want to delete ${images.length} image(s) and all their annotations? This action cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    // Extract image IDs
    const imageIds = images.map((img) => img.id);

    // Delete images via API
    this.imageService
      .deleteImages(imageIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Remove deleted images from local state
          this.images = this.images.filter((img) => !imageIds.includes(img.id));

          // If current image was deleted, select another one
          if (this.currentImage && imageIds.includes(this.currentImage.id)) {
            if (this.images.length > 0) {
              this.selectImage(this.images[0]);
            } else {
              // No images left, clear current image
              this.currentImage = null as any;
              this.state.annotations = [];
            }
          }

          alert(`Successfully deleted ${images.length} image(s)`);
        },
        error: (error) => {
          console.error("Error deleting images:", error);
          alert("Failed to delete images: " + error.message);
        },
      });
  }

  /**
   * Select an image to display and annotate
   * @param image The image to select
   */
  selectImage(image: Image): void {
    // Clear previous annotations from display
    this.state.annotations = [];
    this.state.selectedAnnotation = null;
    this.state.selectedAnnotations = [];

    // Update the input property
    this.currentImage = image;

    // Load image file for the new image
    this.loadImageFile(image.id);

    // Reset enhancement settings when switching images
    this.state.enhanceSettings = {
      brightness: 0,
      contrast: 0,
    };

    // Reset annotation history
    this.state.annotationHistory = [];
    this.state.historyIndex = -1;

    // Load annotations for the new image
    this.loadAnnotationsForCurrentImage();
  }

  /**
   * Handle toolbar button click
   * @param buttonId The ID of the clicked button
   */
  onToolbarButtonClick(buttonId: string): void {
    switch (buttonId) {
      case "pan":
        this.togglePanMode();
        break;
      case "boundingBox":
        this.activateBoundingBoxTool();
        break;
      case "smartLabeling":
        this.activateSmartLabelingTool();
        break;
      case "brush":
        this.activateBrushTool();
        break;
      case "polygon":
        this.activatePolygonTool();
        break;
      case "polyline":
        this.activatePolylineTool();
        break;
      case "undo":
        this.undo();
        break;
      case "redo":
        this.redo();
        break;
      case "clear":
        this.clearAllAnnotations();
        break;
      case "zoomIn":
        this.zoomIn();
        break;
      case "zoomOut":
        this.zoomOut();
        break;
      case "holdToHideStart":
        this.hideAnnotations = true;
        break;
      case "holdToHideEnd":
        this.hideAnnotations = false;
        break;
      default:
    }
  }

  /**
   * Check if undo is available
   * @returns True if undo is available
   */
  canUndo(): boolean {
    return this.state.historyIndex > 0;
  }

  /**
   * Check if redo is available
   * @returns True if redo is available
   */
  canRedo(): boolean {
    return this.state.historyIndex < this.state.annotationHistory.length - 1;
  }

  /**
   * Handle class selection
   * @param projectClass The selected class
   */
  onClassSelected(projectClass: ProjectClass): void {
    const previousClass = this.state.selectedClass;
    this.state.selectedClass = projectClass;

    // For Classification projects, handle class change
    if (this.project?.type === "Classification") {
      this.handleClassificationClassChange(previousClass, projectClass);
    }
  }

  /**
   * Compare function for mat-select to properly compare ProjectClass objects
   * @param c1 First class to compare
   * @param c2 Second class to compare
   * @returns True if classes are equal (same id)
   */
  compareClasses(c1: ProjectClass | null, c2: ProjectClass | null): boolean {
    return c1 && c2 ? c1.id === c2.id : c1 === c2;
  }

  /**
   * Update annotation class and color
   * Used when changing the class of an existing annotation
   * @param annotation The annotation to update
   * @param newClass The new class to apply
   */
  updateAnnotationClass(annotation: Annotation, newClass: ProjectClass): void {
    annotation.classId = newClass.id;
    annotation.className = newClass.className;
    annotation.color = newClass.colorCode;
    annotation.label = newClass.className;

    this.addToHistory();

    // Trigger debounced save
    this.saveAnnotation$.next(annotation);
  }

  /**
   * Open class creation dialog
   */
  openClassCreationDialog(): void {
    if (!this.projectId || this.projectId === 0) {
      return;
    }

    const dialogRef = this.dialog.open(ClassCreationDialogComponent, {
      width: "400px",
      data: {
        projectId: this.projectId,
      },
    });

    dialogRef
      .afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result: ProjectClass | undefined) => {
        if (result) {
          this.onClassCreated(result);
        }
      });
  }

  /**
   * Handle class creation
   * @param newClass The newly created class
   */
  onClassCreated(newClass: ProjectClass): void {
    // Add the new class to the classes array
    this.classes.push(newClass);

    // Sort classes by creation time (assuming id is sequential)
    this.classes.sort((a, b) => a.id - b.id);

    // Select the newly created class
    this.state.selectedClass = newClass;
  }

  /**
   * Handle enhance settings change
   * @param settings The new enhance settings
   */
  onEnhanceSettingsChanged(settings: EnhanceSettings): void {
    this.state.enhanceSettings = settings;
    // Implementation to apply settings to image display will be added in subsequent tasks
  }

  /**
   * Handle "No Class" checkbox change from toolbar
   *
   * Business Rules:
   * - When checked (is_no_class=true): Backend deletes all labels, sets is_labeled=true
   * - When unchecked (is_no_class=false): Backend deletes all labels, sets is_labeled=false
   * - Frontend clears ground truth annotations from canvas (keeps predictions for reference)
   *
   * @param checked Whether the checkbox is checked
   */
  onNoObjectToLabelChanged(checked: boolean): void {
    if (!this.currentImage) {
      return;
    }

    // Clear ground truth annotations from UI (keep predictions for reference)
    this.state.annotations = this.state.annotations.filter(
      (annotation) => annotation.isPrediction === true
    );

    // Clear selection (only if selected annotation was ground truth)
    if (
      this.state.selectedAnnotation &&
      !this.state.selectedAnnotation.isPrediction
    ) {
      this.state.selectedAnnotation = null;
    }

    // Clear multi-selection (only ground truth annotations)
    this.state.selectedAnnotations = this.state.selectedAnnotations.filter(
      (annotation) => annotation.isPrediction === true
    );

    if (checked) {
      // Disable bounding box tool and switch to pan mode when marking as no_class
      this.activeTool = "pan";
      this.isPanMode = true;
    } else {
      // When unchecking, allow user to start labeling
      // The toolbar will handle tool activation
    }

    // Update isNoClass flag
    // Backend will automatically:
    // 1. Delete all labels from la_images_label
    // 2. Set is_labeled based on is_no_class value (via trigger)
    this.imageService
      .updateIsNoClass(this.currentImage.id, checked)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedImage) => {
          this.currentImage = updatedImage;
          // Update in images array
          const index = this.images.findIndex(
            (img) => img.id === updatedImage.id
          );
          if (index !== -1) {
            this.images[index] = updatedImage;
          }
        },
        error: (error) => {
          console.error("Error updating isNoClass flag:", error);
        },
      });
  }

  /**
   * Handle image updated event from general block
   * @param updatedImage The updated image
   */
  onImageUpdated(updatedImage: Image): void {
    // Update the image in the images array
    const index = this.images.findIndex((img) => img.id === updatedImage.id);
    if (index !== -1) {
      this.images[index] = updatedImage;
    }

    // Update current image if it's the one that was updated
    if (this.currentImage?.id === updatedImage.id) {
      this.currentImage = updatedImage;
    }
  }

  /**
   * Handle image deleted event from general block
   * @param imageId The ID of the deleted image
   */
  onImageDeleted(imageId: number): void {
    // Remove image from the images array
    this.images = this.images.filter((img) => img.id !== imageId);

    // If the deleted image was the current image, select another one
    if (this.currentImage?.id === imageId) {
      if (this.images.length > 0) {
        this.selectImage(this.images[0]);
      } else {
        this.currentImage = null as any;
        this.state.annotations = [];
      }
    }
  }

  /**
   * Handle image exported event from general block
   * @param image The image to export
   */
  onImageExported(image: Image): void {
    // Implementation will be added in subsequent tasks
    alert(
      `Export functionality for "${image.fileName}" will be implemented in subsequent tasks.`
    );
  }

  /**
   * Handle annotation selection from labels block or canvas
   * @param annotation The selected annotation
   */
  onAnnotationSelected(annotation: Annotation | null): void {
    this.state.selectedAnnotation = annotation;
    // Sync selectedAnnotations array for single selection from canvas
    // This ensures labels-block highlights the correct item
    if (annotation) {
      // Only update if not already in multi-select mode or if selection is different
      if (
        this.state.selectedAnnotations.length <= 1 ||
        !this.state.selectedAnnotations.some((a) => a.id === annotation.id)
      ) {
        this.state.selectedAnnotations = [annotation];
      }
    } else {
      this.state.selectedAnnotations = [];
    }
  }

  /**
   * Handle multiple annotations selection from labels block
   * @param annotations The selected annotations array
   */
  onAnnotationsSelected(annotations: Annotation[]): void {
    this.state.selectedAnnotations = annotations;
  }

  /**
   * Handle Labels visibility change from labels block
   * @param showLabels Whether to show Ground Truth labels on canvas
   */
  onShowLabelsChanged(showLabels: boolean): void {
    this.state.showLabels = showLabels;
  }

  /**
   * Handle Predictions visibility change from labels block
   * @param showPredictions Whether to show Prediction annotations on canvas
   */
  onShowPredictionsChanged(showPredictions: boolean): void {
    this.state.showPredictions = showPredictions;
  }

  /**
   * Handle Label Details visibility change from toolbar
   * @param showLabelDetails Whether to show label class names and confidence rates on canvas
   */
  onShowLabelDetailsChanged(showLabelDetails: boolean): void {
    this.state.showLabelDetails = showLabelDetails;
  }

  /**
   * Handle Label Assist button click from predictions block
   * Opens model selection or triggers prediction generation
   */
  onLabelAssistClicked(): void {
    // Trigger pre-annotation generation
    this.generatePreAnnotations();
  }

  /**
   * Handle annotation created event from canvas
   * @param annotation The newly created annotation
   */
  onAnnotationCreated(annotation: Annotation): void {
    // Apply selected class color to new annotation
    if (this.state.selectedClass) {
      annotation.classId = this.state.selectedClass.id;
      annotation.className = this.state.selectedClass.className;
      annotation.color = this.state.selectedClass.colorCode;
      annotation.label = this.state.selectedClass.className;
    } else {
      // Set default values if no class is selected
      annotation.classId = 0;
      annotation.className = "Unlabeled";
      annotation.color = "#FF0000"; // Default red color
      annotation.label = "Unlabeled";
    }

    // Create a new array reference to trigger Angular change detection
    this.state.annotations = [...this.state.annotations, annotation];
    this.addToHistory();

    // Trigger debounced save
    this.saveAnnotation$.next(annotation);
  }

  /**
   * Handle annotation updated event from canvas
   * @param annotation The updated annotation
   */
  onAnnotationUpdated(annotation: Annotation): void {
    const index = this.state.annotations.findIndex(
      (a) => a.id === annotation.id
    );
    if (index !== -1) {
      this.state.annotations[index] = annotation;
      this.addToHistory();

      // Trigger debounced save
      this.saveAnnotation$.next(annotation);
    }
  }

  /**
   * Handle annotation deleted event from canvas
   * @param annotation The deleted annotation
   */
  onAnnotationDeleted(annotation: Annotation): void {
    this.deleteAnnotation(annotation);
  }

  /**
   * Delete a single annotation
   * @param annotation The annotation to delete
   */
  deleteAnnotation(annotation: Annotation): void {
    // Remove from local state
    this.state.annotations = this.state.annotations.filter(
      (a) => a.id !== annotation.id
    );
    this.addToHistory();

    // Clear selection if the deleted annotation was selected
    if (this.state.selectedAnnotation?.id === annotation.id) {
      this.state.selectedAnnotation = null;
    }

    // Delete from database via API
    if (annotation.id > 0) {
      this.deleteAnnotationFromDatabase(annotation.id);
    }
  }

  /**
   * Generate pre-annotations for the current image
   * Requirements: 9.1, 9.2
   */
  generatePreAnnotations(): void {
    if (!this.currentImage) {
      return;
    }

    if (!this.modelPredictionEnabled) {
      alert(
        "Model prediction is not enabled. Please configure the model prediction service."
      );
      return;
    }

    if (!this.project) {
      return;
    }

    this.isGeneratingPreAnnotations = true;

    this.modelPredictionService
      .generatePreAnnotations(this.currentImage.id, this.project.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (labels) => {
          // Convert ImageLabels to Annotations
          const newPreAnnotations = labels
            .map((label) => this.imageLabelToAnnotation(label))
            .filter((annotation) => annotation !== null) as Annotation[];

          // Add pre-annotations to the display
          this.preAnnotations = newPreAnnotations;

          // Merge with existing annotations for display
          // Pre-annotations are kept separate for accept/reject functionality
          this.state.annotations = [
            ...this.state.annotations.filter((a) => !a.isPrediction),
            ...newPreAnnotations,
          ];

          this.isGeneratingPreAnnotations = false;

          if (newPreAnnotations.length === 0) {
            alert("No predictions were generated for this image.");
          } else {
            alert(
              `Generated ${newPreAnnotations.length} pre-annotation(s). You can now accept or reject them.`
            );
          }
        },
        error: (error) => {
          console.error("Error generating pre-annotations:", error);
          this.isGeneratingPreAnnotations = false;
          alert("Failed to generate pre-annotations: " + error.message);
        },
      });
  }

  /**
   * Accept a pre-annotation, converting it to a manual annotation
   * Requirements: 9.5
   * @param annotation The pre-annotation to accept
   */
  acceptPreAnnotation(annotation: Annotation): void {
    if (!annotation.isPrediction) {
      return;
    }

    // Convert to manual annotation
    annotation.annotationType = "Ground Truth";
    annotation.isPrediction = false;

    // Remove from pre-annotations list
    this.preAnnotations = this.preAnnotations.filter(
      (a) => a.id !== annotation.id
    );

    // Update in database
    this.saveAnnotation$.next(annotation);
  }

  /**
   * Reject a pre-annotation, deleting it
   * Requirements: 9.5
   * @param annotation The pre-annotation to reject
   */
  rejectPreAnnotation(annotation: Annotation): void {
    if (!annotation.isPrediction) {
      return;
    }

    // Remove from pre-annotations list
    this.preAnnotations = this.preAnnotations.filter(
      (a) => a.id !== annotation.id
    );

    // Delete the annotation
    this.deleteAnnotation(annotation);
  }

  /**
   * Accept all pre-annotations for the current image
   * Requirements: 9.5
   */
  acceptAllPreAnnotations(): void {
    if (this.preAnnotations.length === 0) {
      return;
    }

    const confirmed = confirm(
      `Accept all ${this.preAnnotations.length} pre-annotation(s)? They will be converted to manual annotations.`
    );

    if (!confirmed) {
      return;
    }

    // Accept each pre-annotation
    const annotationsToAccept = [...this.preAnnotations];
    annotationsToAccept.forEach((annotation) => {
      this.acceptPreAnnotation(annotation);
    });

    alert(`Accepted ${annotationsToAccept.length} pre-annotation(s)`);
  }

  /**
   * Reject all pre-annotations for the current image
   * Requirements: 9.5
   */
  rejectAllPreAnnotations(): void {
    if (this.preAnnotations.length === 0) {
      return;
    }

    const confirmed = confirm(
      `Reject all ${this.preAnnotations.length} pre-annotation(s)? They will be deleted.`
    );

    if (!confirmed) {
      return;
    }

    // Reject each pre-annotation
    const annotationsToReject = [...this.preAnnotations];
    annotationsToReject.forEach((annotation) => {
      this.rejectPreAnnotation(annotation);
    });

    alert(`Rejected ${annotationsToReject.length} pre-annotation(s)`);
  }

  /**
   * Handle zoom level change from canvas (e.g., mouse wheel)
   * @param newZoomLevel The new zoom level from canvas
   */
  onZoomLevelChanged(newZoomLevel: number): void {
    this.zoomLevel = newZoomLevel;
  }

  /**
   * Trigger canvas resize after sidebar toggle
   * Uses setTimeout to wait for sidebar animation to complete
   */
  private triggerCanvasResize(): void {
    // Wait for sidebar animation to complete (typically 200-300ms)
    setTimeout(() => {
      window.dispatchEvent(new Event("resize"));
    }, 300);
  }

  /**
   * Check if export is complete and trigger download
   * @param completed Number of completed operations
   * @param total Total number of operations
   * @param zip The JSZip instance
   * @param imageCount Number of images being exported
   */
  private checkExportCompletion(
    completed: number,
    total: number,
    zip: any,
    imageCount: number
  ): void {
    if (completed === total) {
      // All operations complete, generate and download ZIP
      zip
        .generateAsync({ type: "blob" })
        .then((blob: Blob) => {
          // Create download link
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = url;
          link.download = `export_${imageCount}_images_${Date.now()}.zip`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);

          alert(
            `Successfully exported ${imageCount} image(s) with annotations`
          );
        })
        .catch((error: Error) => {
          console.error("Error generating ZIP:", error);
          alert("Failed to generate export file: " + error.message);
        });
    }
  }

  /**
   * Handle class change for Classification projects
   * Delete old label and create new one with the selected class
   * Requirements: 20.2, 20.3, 20.4, 20.5
   * @param previousClass The previously selected class
   * @param newClass The newly selected class
   */
  private handleClassificationClassChange(
    previousClass: ProjectClass | null,
    newClass: ProjectClass
  ): void {
    if (!this.currentImage) {
      return;
    }

    // Delete old classification label if exists (Requirement 20.4, 20.5)
    if (previousClass && this.state.annotations.length > 0) {
      const oldAnnotation = this.state.annotations[0];
      this.deleteAnnotation(oldAnnotation);
    }

    // Create new classification label with position=null (Requirement 20.2, 20.3)
    // For Classification projects, we use a special annotation type that will result in position=null
    const classificationAnnotation: Annotation = {
      id: 0, // Will be assigned by database
      classId: newClass.id,
      className: newClass.className,
      color: newClass.colorCode,
      label: newClass.className,
      type: "CLASSIFICATION" as any, // Special type for classification labels
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: [],
      annotationType: "Ground truth",
    };

    // Ensure only one classification label per image (Requirement 20.4, 20.5)
    this.state.annotations = [classificationAnnotation];
    this.addToHistory();

    // Save to database
    this.saveAnnotation$.next(classificationAnnotation);
  }

  /**
   * Delete annotation from database
   * @param annotationId The ID of the annotation to delete
   */
  private deleteAnnotationFromDatabase(annotationId: number): void {
    this.labelService
      .deleteLabel(annotationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.updateSaveStatus("saved", "Deleted");
        },
        error: (error) => {
          console.error("Error deleting annotation:", error);
          this.updateSaveStatus("error", "Delete failed: " + error.message);
        },
      });
  }

  /**
   * Toggle pan mode
   */
  private togglePanMode(): void {
    // If pan is already active, deactivate it
    if (this.isPanMode) {
      this.isPanMode = false;
      this.activeTool = "none";
    } else {
      // Activate pan mode
      this.isPanMode = true;
      this.activeTool = "pan";
    }
  }

  /**
   * Activate bounding box tool
   */
  private activateBoundingBoxTool(): void {
    // If boundingBox is already active, deactivate it
    if (this.activeTool === "boundingBox") {
      this.activeTool = "none";
      return;
    }

    // Deactivate pan mode and activate boundingBox
    this.isPanMode = false;
    this.activeTool = "boundingBox";

    // Warn if no class is selected
    if (!this.state.selectedClass) {
      alert("Please select a class before drawing annotations.");
    }
  }

  /**
   * Activate smart labeling tool
   */
  private activateSmartLabelingTool(): void {
    this.isPanMode = false;
    this.activeTool = "none"; // Smart labeling not yet implemented
    // Implementation will be added in subsequent tasks
  }

  /**
   * Activate brush tool
   */
  private activateBrushTool(): void {
    this.isPanMode = false;
    this.activeTool = "brush";
  }

  /**
   * Activate polygon tool
   */
  private activatePolygonTool(): void {
    this.isPanMode = false;
    this.activeTool = "polygon";
  }

  /**
   * Activate polyline tool
   */
  private activatePolylineTool(): void {
    this.isPanMode = false;
    this.activeTool = "polyline";
  }

  /**
   * Undo last annotation change
   */
  private undo(): void {
    if (this.canUndo()) {
      this.state.historyIndex--;
      this.state.annotations = [
        ...this.state.annotationHistory[this.state.historyIndex],
      ];
    }
  }

  /**
   * Redo last undone annotation change
   */
  private redo(): void {
    if (this.canRedo()) {
      this.state.historyIndex++;
      this.state.annotations = [
        ...this.state.annotationHistory[this.state.historyIndex],
      ];
    }
  }

  /**
   * Clear all annotations for the current image
   */
  private clearAllAnnotations(): void {
    if (this.state.annotations.length === 0) {
      return;
    }

    const confirmed = confirm(
      "Are you sure you want to clear all annotations for this image?"
    );
    if (confirmed) {
      // Store current image ID before clearing
      const currentImageId = this.currentImage?.id;

      // Clear annotations from local state
      this.state.annotations = [];
      this.state.selectedAnnotation = null;
      this.addToHistory();

      // Delete all annotations from database
      if (currentImageId) {
        this.deleteAllAnnotationsFromDatabase(currentImageId);
      }
    }
  }

  /**
   * Delete all annotations for an image from database
   * @param imageId The ID of the image
   */
  private deleteAllAnnotationsFromDatabase(imageId: number): void {
    this.labelService
      .deleteLabelsByImageId(imageId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {},
        error: (error) => {
          console.error("Error deleting all annotations:", error);
          alert("Failed to delete all annotations: " + error.message);
        },
      });
  }

  /**
   * Zoom in
   */
  private zoomIn(): void {
    this.zoomLevel = Math.min(this.zoomLevel + 0.1, 3.0);
    // Implementation will be added in subsequent tasks
  }

  /**
   * Zoom out
   */
  private zoomOut(): void {
    this.zoomLevel = Math.max(this.zoomLevel - 0.1, 0.1);
    // Implementation will be added in subsequent tasks
  }

  /**
   * Add current annotation state to history
   */
  private addToHistory(): void {
    // Remove any future history if we're not at the end
    if (this.state.historyIndex < this.state.annotationHistory.length - 1) {
      this.state.annotationHistory = this.state.annotationHistory.slice(
        0,
        this.state.historyIndex + 1
      );
    }

    // Add current state to history
    this.state.annotationHistory.push([...this.state.annotations]);
    this.state.historyIndex = this.state.annotationHistory.length - 1;

    // Limit history size to prevent memory issues
    const maxHistorySize = 50;
    if (this.state.annotationHistory.length > maxHistorySize) {
      this.state.annotationHistory.shift();
      this.state.historyIndex--;
    }
  }

  /**
   * Setup keyboard event listeners
   */
  private setupKeyboardListeners(): void {
    // Listen for Delete key to delete selected annotation
    document.addEventListener("keydown", (event: KeyboardEvent) => {
      if (event.key === "Delete" || event.key === "Backspace") {
        // Only delete if an annotation is selected and we're not in an input field
        const target = event.target as HTMLElement;
        const isInputField =
          target.tagName === "INPUT" ||
          target.tagName === "TEXTAREA" ||
          target.isContentEditable;

        if (isInputField) {
          return;
        }

        // Handle multi-selection delete first
        if (this.state.selectedAnnotations.length > 0) {
          event.preventDefault();
          // Filter out Prediction annotations (cannot be deleted)
          const deletableAnnotations = this.state.selectedAnnotations.filter(
            (a) => a.annotationType !== "Prediction"
          );

          if (deletableAnnotations.length === 0) {
            return;
          }

          // Delete all selected annotations
          deletableAnnotations.forEach((annotation) => {
            this.deleteAnnotation(annotation);
          });

          // Clear multi-selection
          this.state.selectedAnnotations = [];
          return;
        }

        // Handle single selection delete
        if (this.state.selectedAnnotation) {
          // Prediction annotations cannot be deleted
          if (this.state.selectedAnnotation.annotationType === "Prediction") {
            return;
          }
          event.preventDefault();
          this.deleteAnnotation(this.state.selectedAnnotation);
        }
      }
    });
  }

  /**
   * Load image by ID
   * @param imageId The image ID
   * @param projectId The project ID (kept for backward compatibility but not used)
   */
  private loadImageById(imageId: number, projectId: number): void {
    this.imageService
      .getImageById(imageId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (image) => {
          this.currentImage = image;

          // Load image file for display
          this.loadImageFile(this.currentImage.id);

          // Setup keyboard listeners
          this.setupKeyboardListeners();

          // Setup debounced save
          this.setupDebouncedSave();
        },
        error: (error) => {
          console.error("Error loading image:", error);
          alert(`Failed to load image: ${error.message}`);
        },
      });
  }

  /**
   * Load project information from database
   * @param projectId The project ID
   */
  private loadProject(projectId: number): void {
    this.projectService
      .getProjectById(projectId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (project) => {
          this.project = project;

          // Initialize toolbar buttons based on project type
          this.toolbarButtons = this.getToolbarButtons(project.type);

          // Load images and classes for the project
          // Load classes first, then load annotations after classes are ready
          this.loadImages();
          this.loadClasses(true);

          // Check if model prediction is enabled
          this.checkModelPredictionStatus();
        },
        error: (error) => {
          console.error("Error loading project:", error);
          alert("Failed to load project: " + error.message);
        },
      });
  }

  /**
   * Load images for the current project with pagination context from upload page
   * @param queryParams Query parameters containing pagination and filter info
   */
  private loadImagesWithPagination(queryParams: any): void {
    if (!this.project) {
      return;
    }

    // If no pagination params provided, fall back to loading all images
    if (!queryParams.page && !queryParams.size) {
      this.loadImages();
      return;
    }

    this.isLoadingImages = true;

    // Parse pagination parameters
    const page = queryParams.page ? +queryParams.page : 0;
    const size = queryParams.size ? +queryParams.size : 20;
    const viewMode = queryParams.viewMode || "images";
    const sortBy = queryParams.sortBy || "upload_time_desc";

    // Parse filter parameters
    const filters: any = {};
    if (queryParams.mediaStatus) {
      filters.mediaStatus = queryParams.mediaStatus.split(",");
    }
    if (queryParams.groundTruthLabels) {
      filters.groundTruthLabels = queryParams.groundTruthLabels
        .split(",")
        .map((id: string) => +id);
    }
    if (queryParams.predictionLabels) {
      filters.predictionLabels = queryParams.predictionLabels
        .split(",")
        .map((id: string) => +id);
    }
    if (queryParams.split) {
      filters.split = queryParams.split.split(",");
    }
    if (queryParams.tags) {
      filters.tags = queryParams.tags.split(",").map((id: string) => +id);
    }
    if (queryParams.mediaName) {
      filters.mediaName = queryParams.mediaName;
    }
    if (queryParams.labeler) {
      filters.labeler = queryParams.labeler;
    }
    if (queryParams.mediaId) {
      filters.mediaId = queryParams.mediaId;
    }
    if (queryParams.noClass !== undefined) {
      filters.noClass = queryParams.noClass === "true";
    }
    if (queryParams.predictionNoClass !== undefined) {
      filters.predictionNoClass = queryParams.predictionNoClass === "true";
    }
    if (queryParams.modelId) {
      filters.modelId = +queryParams.modelId;
    }
    if (queryParams.annotationType) {
      filters.annotationType = queryParams.annotationType;
    }

    this.imageService
      .getImagesPageableByProjectId(
        this.project.id,
        page,
        size,
        viewMode,
        filters,
        sortBy,
        false // Don't include thumbnails - labelling page doesn't display them
      )
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.images = response.content;
          this.isLoadingImages = false;
        },
        error: (error) => {
          console.error("Error loading paginated images:", error);
          this.isLoadingImages = false;
          // Fall back to loading all images
          this.loadImages();
        },
      });
  }

  /**
   * Load images for the current project
   */
  private loadImages(): void {
    if (!this.project) {
      return;
    }

    this.isLoadingImages = true;
    this.imageService
      .getImagesByProjectId(this.project.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (images) => {
          this.images = images;
          this.isLoadingImages = false;
        },
        error: (error) => {
          console.error("Error loading images:", error);
          this.isLoadingImages = false;
        },
      });
  }

  /**
   * Load classes for the current project
   * @param loadAnnotationsAfter If true, load annotations after classes are loaded
   */
  private loadClasses(loadAnnotationsAfter: boolean = false): void {
    if (!this.project) {
      return;
    }

    this.isLoadingClasses = true;
    this.projectClassService
      .getClassesByProjectId(this.project.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (classes) => {
          // Check if no classes found - navigate back to upload image page
          if (classes.length === 0) {
            this.isLoadingClasses = false;

            // Show alert to user
            alert("Please setup classes first before labeling images.");

            // Navigate back to upload image page
            this.router.navigate(["/landingai/projects", this.project!.id]);
            return;
          }

          // Create a new array reference to ensure change detection
          this.classes = [...classes];
          this.isLoadingClasses = false;

          // Always select first class by default
          if (this.classes.length > 0 && !this.state.selectedClass) {
            this.state.selectedClass = this.classes[0];
          }

          // Load annotations after classes are loaded to ensure class info is available
          if (loadAnnotationsAfter) {
            this.loadAnnotationsForCurrentImage();
          }
        },
        error: (error) => {
          console.error("Error loading classes:", error);
          this.isLoadingClasses = false;

          // Show alert to user
          alert(
            "Failed to load classes. Please setup classes first before labeling images."
          );

          // Navigate back to upload image page
          if (this.project) {
            this.router.navigate(["/landingai/projects", this.project.id]);
          }
        },
      });
  }

  /**
   * Convert Annotation to ImageLabel format for API
   * @param annotation The annotation to convert
   * @returns ImageLabel object
   */
  private annotationToImageLabel(annotation: Annotation): ImageLabel {
    let position: string | null = null;

    // Serialize position based on annotation type
    // For Classification type, position remains null (Requirement 20.3)
    if (annotation.type === "RECTANGLE") {
      // Convert pixel coordinates to YOLO percentage format (0-1 range)
      // YOLO format: x_center, y_center, width, height (all relative to image dimensions)
      const imgWidth = this.currentImage?.width || 1;
      const imgHeight = this.currentImage?.height || 1;

      // Calculate center point and convert to percentage
      const xCenter = (annotation.x + annotation.width / 2) / imgWidth;
      const yCenter = (annotation.y + annotation.height / 2) / imgHeight;
      const widthPercent = annotation.width / imgWidth;
      const heightPercent = annotation.height / imgHeight;

      position = JSON.stringify({
        type: "rectangle",
        x: xCenter,
        y: yCenter,
        width: widthPercent,
        height: heightPercent,
      });
    } else if (
      annotation.type === "POLYGON" ||
      annotation.type === "BRUSH" ||
      annotation.type === "POLYLINE"
    ) {
      position = JSON.stringify({
        type: annotation.type.toLowerCase(),
        points: annotation.points,
      });
    }
    // For CLASSIFICATION type, position remains null

    return {
      id: annotation.id > 0 ? annotation.id : undefined,
      imageId: this.currentImage!.id,
      classId: annotation.classId,
      position: position,
      annotationType: "Ground truth",
      createdBy: this.currentUser.wbi,
    };
  }

  /**
   * Save annotation to database
   * @param annotation The annotation to save
   */
  private saveAnnotationToDatabase(annotation: Annotation): void {
    if (!this.currentImage) {
      console.error("Cannot save annotation: no current image");
      this.updateSaveStatus("error", "No current image");
      return;
    }

    const imageLabel = this.annotationToImageLabel(annotation);

    // Update save status
    this.updateSaveStatus("saving", "Saving...");

    // Check if this is an existing label (has valid database ID) or a new one
    // Valid database IDs are positive integers typically less than 1 billion
    // Temporary IDs from Date.now() are much larger (> 1 trillion)
    const isExistingLabel =
      imageLabel.id && imageLabel.id > 0 && imageLabel.id < 1000000000;

    if (isExistingLabel) {
      // Update existing label
      this.labelService
        .updateLabel(imageLabel.id!, imageLabel)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (updatedLabel) => {
            this.updateSaveStatus("saved", "Saved");
          },
          error: (error) => {
            console.error("Error saving annotation:", error);
            this.updateSaveStatus("error", "Save failed: " + error.message);
          },
        });
    } else {
      // Create new label
      this.labelService
        .saveLabel(imageLabel)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (createdLabel) => {
            // Update annotation with the ID from the database
            annotation.id = createdLabel.id!;
            this.updateSaveStatus("saved", "Saved");
          },
          error: (error) => {
            console.error("Error creating annotation:", error);
            this.updateSaveStatus("error", "Save failed: " + error.message);
          },
        });
    }
  }

  /**
   * Setup debounced save for annotations
   * Debounces save operations to avoid excessive API calls
   */
  private setupDebouncedSave(): void {
    this.saveAnnotation$
      .pipe(
        debounceTime(500), // Wait 500ms after last change before saving
        takeUntil(this.destroy$)
      )
      .subscribe((annotation) => {
        this.saveAnnotationToDatabase(annotation);
      });
  }

  /**
   * Update save status indicator
   * @param status The save status
   * @param message Optional status message
   */
  private updateSaveStatus(
    status: "idle" | "saving" | "saved" | "error",
    message: string = ""
  ): void {
    this.saveStatus = status;
    this.saveStatusMessage = message;

    // Auto-clear saved status after 2 seconds
    if (status === "saved") {
      setTimeout(() => {
        if (this.saveStatus === "saved") {
          this.saveStatus = "idle";
          this.saveStatusMessage = "";
        }
      }, 2000);
    }
  }

  /**
   * Load annotations for the current image from database
   */
  private loadAnnotationsForCurrentImage(): void {
    if (!this.currentImage) {
      console.error("Cannot load annotations: no current image");
      return;
    }

    this.isLoadingAnnotations = true;

    // Load ground truth labels
    const groundTruth$ = this.labelService.getLabelsByImageId(
      this.currentImage.id
    );

    // If modelId is available, also load prediction labels
    if (this.modelId) {
      forkJoin({
        groundTruth: groundTruth$,
        predictions: this.modelPredictionService.getPredictionsByImageAndModel(
          this.currentImage.id,
          this.modelId
        ),
      })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: ({ groundTruth, predictions }) => {
            // Convert ground truth labels to annotations
            const gtAnnotations = groundTruth
              .map((label) => this.imageLabelToAnnotation(label))
              .filter((a) => a !== null) as Annotation[];

            // Convert prediction labels to annotations
            // Prediction labels from the endpoint already have annotationType="Prediction"
            const predAnnotations = predictions
              .map((label) => this.imageLabelToAnnotation(label))
              .filter((a) => a !== null) as Annotation[];

            // Merge both sets
            this.state.annotations = [...gtAnnotations, ...predAnnotations];

            // Initialize history with loaded annotations
            this.state.annotationHistory = [this.state.annotations];
            this.state.historyIndex = 0;

            // For Classification projects, set selectedClass based on existing label
            if (this.project?.type === "Classification") {
              const gtOnly = gtAnnotations;
              if (gtOnly.length > 0) {
                const existingAnnotation = gtOnly[0];
                const matchingClass = this.classes.find(
                  (c) => c.id === existingAnnotation.classId
                );
                if (matchingClass) {
                  this.state.selectedClass = matchingClass;
                }
              } else {
                this.state.selectedClass = null;
              }
            }

            this.isLoadingAnnotations = false;
          },
          error: (error) => {
            console.error("Error loading annotations:", error);
            this.isLoadingAnnotations = false;
            alert("Failed to load annotations: " + error.message);
          },
        });
    } else {
      // No modelId — only load ground truth
      groundTruth$.pipe(takeUntil(this.destroy$)).subscribe({
        next: (labels) => {
          this.state.annotations = labels
            .map((label) => this.imageLabelToAnnotation(label))
            .filter((annotation) => annotation !== null) as Annotation[];

          this.state.annotationHistory = [this.state.annotations];
          this.state.historyIndex = 0;

          if (this.project?.type === "Classification") {
            if (this.state.annotations.length > 0) {
              const existingAnnotation = this.state.annotations[0];
              const matchingClass = this.classes.find(
                (c) => c.id === existingAnnotation.classId
              );
              if (matchingClass) {
                this.state.selectedClass = matchingClass;
              }
            } else {
              this.state.selectedClass = null;
            }
          }

          this.isLoadingAnnotations = false;
        },
        error: (error) => {
          console.error("Error loading annotations:", error);
          this.isLoadingAnnotations = false;
          alert("Failed to load annotations: " + error.message);
        },
      });
    }
  }

  /**
   * Convert ImageLabel to Annotation format
   * @param label The ImageLabel to convert
   * @returns Annotation object or null if conversion fails
   */
  private imageLabelToAnnotation(label: ImageLabel): Annotation | null {
    try {
      // Find the class for this label
      const projectClass = this.classes.find((c) => c.id === label.classId);

      if (!projectClass) {
        return null;
      }

      // Parse position JSON first to determine the annotation type
      let annotationTypeFromPosition: AnnotationType = AnnotationType.Rectangle;
      let x = 0,
        y = 0,
        width = 0,
        height = 0;
      let points: { x: number; y: number }[] = [];

      if (label.position) {
        try {
          const position = JSON.parse(label.position);

          if (position.type === "rectangle") {
            annotationTypeFromPosition = AnnotationType.Rectangle;
            // Convert YOLO percentage format back to pixel coordinates
            // YOLO format: x_center, y_center, width, height (all relative to image dimensions)
            const imgWidth = this.currentImage?.width || 1;
            const imgHeight = this.currentImage?.height || 1;

            const xCenter = position.x || 0;
            const yCenter = position.y || 0;
            const widthPercent = position.width || 0;
            const heightPercent = position.height || 0;

            // Convert percentage to pixels and calculate top-left corner
            width = widthPercent * imgWidth;
            height = heightPercent * imgHeight;
            x = xCenter * imgWidth - width / 2;
            y = yCenter * imgHeight - height / 2;
          } else if (position.type === "polygon") {
            annotationTypeFromPosition = AnnotationType.Polygon;
            points = position.points || [];
          } else if (position.type === "brush") {
            annotationTypeFromPosition = AnnotationType.Brush;
            points = position.points || [];
          } else if (position.type === "polyline") {
            annotationTypeFromPosition = AnnotationType.Polyline;
            points = position.points || [];
          }
        } catch (jsonError) {
          console.error(
            `Invalid JSON in position field for label ${label.id}:`,
            jsonError
          );
          console.error(`Position value: ${label.position}`);
          // Return null to skip this corrupted annotation
          return null;
        }
      } else {
        // No position means Classification type
        annotationTypeFromPosition = AnnotationType.Classification;
      }

      // Create annotation object
      const annotation: Annotation = {
        id: label.id || 0,
        classId: label.classId,
        className: projectClass.className,
        color: projectClass.colorCode,
        label: projectClass.className,
        type: annotationTypeFromPosition,
        x: x,
        y: y,
        width: width,
        height: height,
        points: points,
        annotationType: label.annotationType, // "Ground Truth" or "Prediction"
        confidenceRate: label.confidenceRate,
        isPrediction: label.annotationType === "Prediction", // Helper flag for UI
      };

      return annotation;
    } catch (error) {
      console.error(`Error converting label ${label.id} to annotation:`, error);
      return null;
    }
  }

  /**
   * Check if model prediction is enabled
   */
  private checkModelPredictionStatus(): void {
    this.modelPredictionService
      .getStatus()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (status) => {
          this.modelPredictionEnabled = status.enabled;
        },
        error: (error) => {
          console.error("Error checking model prediction status:", error);
          this.modelPredictionEnabled = false;
        },
      });
  }

  /**
   * Load model information if modelId is available
   */
  private loadModelInfo(): void {
    if (!this.modelId) {
      return;
    }

    this.modelService
      .getModelById(this.modelId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (model: any) => {
          if (model) {
            this.selectedModelName = model.modelAlias
              ? `${model.modelAlias} (v${model.modelVersion || "?"})`
              : `Model #${model.id}`;
          }
        },
        error: (error) => {
          console.error("Error loading model info:", error);
          this.selectedModelName = `Model #${this.modelId}`;
        },
      });
  }

  /**
   * Load image file from server and create blob URL for display
   * @param imageId The image ID
   */
  private loadImageFile(imageId: number): void {
    if (!this.currentImage) {
      console.error("Cannot load image file: no current image");
      return;
    }

    // Clean up previous image URL if exists
    if (this.currentImageUrl) {
      URL.revokeObjectURL(this.currentImageUrl);
      this.currentImageUrl = null;
    }

    // Fetch image file from server by imageId
    this.imageService
      .getImageFileByImageId(imageId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          // Ensure blob has correct image MIME type for browser decoding
          // The server may return application/octet-stream which browsers can't render as an image
          const fileName = this.currentImage?.fileName || "";
          const ext = fileName.split(".").pop()?.toLowerCase() || "jpeg";
          const mimeMap: Record<string, string> = {
            jpg: "image/jpeg",
            jpeg: "image/jpeg",
            png: "image/png",
            gif: "image/gif",
            bmp: "image/bmp",
            webp: "image/webp",
          };
          const mimeType = mimeMap[ext] || "image/jpeg";
          const imageBlob = new Blob([blob], { type: mimeType });

          // Create blob URL for display
          this.currentImageUrl = URL.createObjectURL(imageBlob);
        },
        error: (error) => {
          console.error(
            `Error loading image file for imageId ${imageId}:`,
            error
          );
          alert(`Failed to load image file: ${error.message}`);
        },
      });
  }
}
