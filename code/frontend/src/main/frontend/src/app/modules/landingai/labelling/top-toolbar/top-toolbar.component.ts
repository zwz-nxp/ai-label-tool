import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { ProjectClass } from "app/models/landingai/project-class.model";
import { EnhanceSettings } from "app/models/landingai/enhance-settings.model";
import { ToolbarButton } from "../image-labelling/image-labelling.component";
import { ClassCreationDialogComponent } from "../class-creation-dialog/class-creation-dialog.component";
import { EnhanceDialogComponent } from "../enhance-dialog/enhance-dialog.component";

/**
 * Top toolbar component for annotation tools
 * Dynamically renders buttons based on project type
 */
@Component({
  selector: "app-top-toolbar",
  templateUrl: "./top-toolbar.component.html",
  styleUrls: ["./top-toolbar.component.scss"],
  standalone: false,
})
export class TopToolbarComponent implements OnInit, OnDestroy {
  @Input() buttons: ToolbarButton[] = [];
  @Input() classes: ProjectClass[] = [];
  @Input() selectedClass: ProjectClass | null = null;
  @Input() projectId: number = 0;
  @Input() projectName: string = "";
  @Input() projectType: string = "";
  @Input() enhanceSettings: EnhanceSettings = { brightness: 0, contrast: 0 };
  @Input() isPanMode = false;
  @Input() activeTool: string = "none";
  @Input() canUndo = false;
  @Input() canRedo = false;
  @Input() zoomLevel = 1.0;
  @Input() currentImageIndex = 0;
  @Input() totalImages = 0;
  @Input() isNoClass = false;
  @Input() currentImage: any = null;
  @Input() showLabelDetails = true;

  @Output() buttonClick = new EventEmitter<string>();
  @Output() classSelected = new EventEmitter<ProjectClass>();
  @Output() classCreated = new EventEmitter<ProjectClass>();
  @Output() enhanceSettingsChanged = new EventEmitter<EnhanceSettings>();
  @Output() navigateImage = new EventEmitter<"previous" | "next">();
  @Output() navigateToProject = new EventEmitter<void>();
  @Output() noObjectToLabelChanged = new EventEmitter<boolean>();
  @Output() showLabelDetailsChanged = new EventEmitter<boolean>();

  // UI state
  isHoldingHide = false;

  // Destroy subject for cleanup
  private destroy$ = new Subject<void>();

  constructor(private dialog: MatDialog) {}

  ngOnInit(): void {
    // Initialize component
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check if a specific button is present in the toolbar
   * @param buttonId The button ID to check
   * @returns True if the button exists
   */
  hasButton(buttonId: string): boolean {
    return this.buttons.some((btn) => btn.id === buttonId);
  }

  /**
   * Handle button click
   * @param buttonId The ID of the clicked button
   */
  onButtonClick(buttonId: string): void {
    // Handle special buttons locally
    switch (buttonId) {
      case "enhance":
        this.openEnhanceDialog();
        return;
      case "holdToHide":
        // Hold to hide is handled by mouse events
        return;
      default:
        this.buttonClick.emit(buttonId);
    }
  }

  /**
   * Handle class selection
   * @param projectClass The selected class
   */
  onClassSelected(projectClass: ProjectClass): void {
    this.classSelected.emit(projectClass);
  }

  /**
   * Handle "No Class" checkbox change
   * @param checked Whether the checkbox is checked
   */
  onNoClassChange(checked: boolean): void {
    this.noObjectToLabelChanged.emit(checked);

    // If unchecking "No Class", automatically activate bounding box tool
    if (!checked && this.hasButton("boundingBox")) {
      this.buttonClick.emit("boundingBox");
    }
  }

  /**
   * Navigate to previous image
   */
  onPreviousImage(): void {
    if (this.canGoPrevious()) {
      this.navigateImage.emit("previous");
    }
  }

  /**
   * Navigate to next image
   */
  onNextImage(): void {
    if (this.canGoNext()) {
      this.navigateImage.emit("next");
    }
  }

  /**
   * Navigate back to project page
   */
  onNavigateToProject(): void {
    this.navigateToProject.emit();
  }

  /**
   * Check if can navigate to previous image
   */
  canGoPrevious(): boolean {
    return this.currentImageIndex > 0;
  }

  /**
   * Check if can navigate to next image
   */
  canGoNext(): boolean {
    return this.currentImageIndex < this.totalImages - 1;
  }

  /**
   * Open class creation dialog
   */
  openClassCreationDialog(): void {
    if (!this.projectId || this.projectId === 0) {
      console.warn("Cannot create class: project not loaded");
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
          this.classCreated.emit(result);
        }
      });
  }

  /**
   * Open enhance dialog
   */
  openEnhanceDialog(): void {
    const dialogRef = this.dialog.open(EnhanceDialogComponent, {
      width: "400px",
      data: {
        brightness: this.enhanceSettings.brightness,
        contrast: this.enhanceSettings.contrast,
      },
    });

    dialogRef
      .afterClosed()
      .pipe(takeUntil(this.destroy$))
      .subscribe((result: EnhanceSettings | undefined) => {
        if (result) {
          this.enhanceSettingsChanged.emit(result);
        }
      });
  }

  /**
   * Handle hold to hide mouse down
   */
  onHoldToHideMouseDown(): void {
    this.isHoldingHide = true;
    this.buttonClick.emit("holdToHideStart");
  }

  /**
   * Handle hold to hide mouse up
   */
  onHoldToHideMouseUp(): void {
    this.isHoldingHide = false;
    this.buttonClick.emit("holdToHideEnd");
  }

  /**
   * Handle hold to hide mouse leave (in case user drags out)
   */
  onHoldToHideMouseLeave(): void {
    if (this.isHoldingHide) {
      this.isHoldingHide = false;
      this.buttonClick.emit("holdToHideEnd");
    }
  }

  /**
   * Get button by ID
   * @param buttonId The button ID
   * @returns The button configuration or undefined
   */
  getButton(buttonId: string): ToolbarButton | undefined {
    return this.buttons.find((btn) => btn.id === buttonId);
  }

  /**
   * Check if button is active (for toggle buttons like pan)
   * @param buttonId The button ID
   * @returns True if the button is active
   */
  isButtonActive(buttonId: string): boolean {
    if (buttonId === "pan") {
      return this.isPanMode;
    }
    if (buttonId === "boundingBox") {
      return this.activeTool === "boundingBox";
    }
    // Add more active states as needed
    return false;
  }

  /**
   * Get zoom percentage for display
   * @returns Zoom level as percentage string
   */
  getZoomPercentage(): string {
    return Math.round(this.zoomLevel * 100) + "%";
  }

  /**
   * Handle show label details toggle change
   * @param checked The new checked state
   */
  onShowLabelDetailsChange(checked: boolean): void {
    this.showLabelDetailsChanged.emit(checked);
  }
}
