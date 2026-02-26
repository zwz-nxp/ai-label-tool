import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { Store } from "@ngrx/store";
import { Observable, Subject, takeUntil } from "rxjs";
import { ProjectGroupName, ProjectType } from "app/models/landingai/project";
import * as HomeActions from "app/state/landingai/home/home.actions";
import * as HomeSelectors from "app/state/landingai/home/home.selectors";
import { Actions, ofType } from "@ngrx/effects";
import { MatSnackBar } from "@angular/material/snack-bar";

interface ProjectTypeOption {
  type: ProjectType;
  icon: string;
  description: string;
  enabled: boolean;
}

@Component({
  selector: "app-project-create",
  standalone: false,
  templateUrl: "./project-create.component.html",
  styleUrls: ["./project-create.component.scss"],
})
export class ProjectCreateComponent implements OnInit, OnDestroy {
  projectForm: FormGroup;
  selectedFiles: File[] = [];
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  fileValidationError: string | null = null;
  // Available model names
  availableModels: string[] = [
    "RtmDet-[9M]",
    "RepPoints-[20M]",
    "RepPoints-[37M]",
    "ODEmbedded-[23M]",
  ];
  // Available group names
  availableGroups: ProjectGroupName[] = ["WT", "FE", "BE", "QA", "AT"];
  projectTypes: ProjectTypeOption[] = [
    {
      type: "Object Detection",
      icon: "check_box_outline_blank",
      description: "Identify and locate objects within images",
      enabled: true,
    },
    {
      type: "Classification",
      icon: "category",
      description: "Categorize entire images into classes",
      enabled: true,
    },
    {
      type: "Segmentation",
      icon: "gradient",
      description: "Pixel-level classification of image regions",
      enabled: false,
    },
  ];
  private destroy$ = new Subject<void>();
  private createdProjectId: number | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private store: Store,
    private router: Router,
    private actions$: Actions,
    private snackBar: MatSnackBar
  ) {
    this.projectForm = this.formBuilder.group({
      name: ["", [Validators.required, Validators.minLength(1)]],
      type: ["Object Detection", Validators.required], // Default to Object Detection
      modelName: [""], // Optional field
      groupName: [""], // Optional field
    });

    this.loading$ = this.store.select(HomeSelectors.selectLoading);
    this.error$ = this.store.select(HomeSelectors.selectError);
  }

  ngOnInit(): void {
    // Listen for successful project creation
    this.actions$
      .pipe(ofType(HomeActions.createProjectSuccess), takeUntil(this.destroy$))
      .subscribe(({ project }) => {
        this.createdProjectId = project.id;

        // If there are files to upload, dispatch upload action
        if (this.selectedFiles.length > 0) {
          this.store.dispatch(
            HomeActions.uploadImages({
              files: this.selectedFiles,
              projectId: project.id,
            })
          );
        } else {
          // No files to upload, navigate immediately
          this.router.navigate(["/landingai/projects", project.id]);
        }
      });

    // Listen for successful image upload
    this.actions$
      .pipe(ofType(HomeActions.uploadImagesSuccess), takeUntil(this.destroy$))
      .subscribe(() => {
        // Navigate to project detail after successful upload
        if (this.createdProjectId) {
          this.router.navigate(["/landingai/projects", this.createdProjectId]);
        }
      });

    // Listen for image upload failure
    this.actions$
      .pipe(ofType(HomeActions.uploadImagesFailure), takeUntil(this.destroy$))
      .subscribe(() => {
        // Even on upload failure, navigate to project detail
        // User can see the error and retry from there
        if (this.createdProjectId) {
          this.router.navigate(["/landingai/projects", this.createdProjectId]);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle project type selection
   * @param type Selected project type
   */
  onTypeSelect(type: ProjectType): void {
    const typeOption = this.projectTypes.find((t) => t.type === type);
    if (typeOption && typeOption.enabled) {
      this.projectForm.patchValue({ type });
    }
  }

  /**
   * Check if a project type is selected
   * @param type Project type to check
   */
  isTypeSelected(type: ProjectType): boolean {
    return this.projectForm.get("type")?.value === type;
  }

  /**
   * Handle file selection from input
   * @param event File input change event
   */
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const newFiles = Array.from(input.files);

      // Validate file types and sizes
      const validFiles: File[] = [];
      const invalidFiles: string[] = [];
      const oversizedFiles: string[] = [];
      const maxFileSize = 10 * 1024 * 1024; // 10MB

      newFiles.forEach((file) => {
        const extension = file.name.toLowerCase().split(".").pop();
        const isValidType =
          extension === "png" || extension === "jpg" || extension === "jpeg";
        const isValidSize = file.size <= maxFileSize;

        if (!isValidType) {
          invalidFiles.push(file.name);
        } else if (!isValidSize) {
          oversizedFiles.push(file.name);
        } else {
          validFiles.push(file);
        }
      });

      // Display validation errors
      if (invalidFiles.length > 0) {
        this.fileValidationError = `${invalidFiles.length} file(s) rejected: Invalid format. Only PNG, JPG, and JPEG are supported.`;
        this.snackBar.open(this.fileValidationError, "Close", {
          duration: 5000,
          verticalPosition: "top",
          panelClass: "snackbar-warning",
        });
      }

      if (oversizedFiles.length > 0) {
        this.fileValidationError = `${oversizedFiles.length} file(s) rejected: File size exceeds 10MB limit.`;
        this.snackBar.open(this.fileValidationError, "Close", {
          duration: 5000,
          verticalPosition: "top",
          panelClass: "snackbar-warning",
        });
      }

      // Add valid files to the list
      if (validFiles.length > 0) {
        this.selectedFiles = [...this.selectedFiles, ...validFiles];
        this.fileValidationError = null;
      }

      // Reset input to allow selecting the same file again
      input.value = "";
    }
  }

  /**
   * Handle file drop
   * @param event Drag and drop event
   */
  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();

    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      const newFiles = Array.from(event.dataTransfer.files);

      // Validate file types and sizes
      const validFiles: File[] = [];
      const invalidFiles: string[] = [];
      const oversizedFiles: string[] = [];
      const maxFileSize = 10 * 1024 * 1024; // 10MB

      newFiles.forEach((file) => {
        const extension = file.name.toLowerCase().split(".").pop();
        const isValidType =
          extension === "png" || extension === "jpg" || extension === "jpeg";
        const isValidSize = file.size <= maxFileSize;

        if (!isValidType) {
          invalidFiles.push(file.name);
        } else if (!isValidSize) {
          oversizedFiles.push(file.name);
        } else {
          validFiles.push(file);
        }
      });

      // Display validation errors
      if (invalidFiles.length > 0) {
        this.fileValidationError = `${invalidFiles.length} file(s) rejected: Invalid format. Only PNG, JPG, and JPEG are supported.`;
        this.snackBar.open(this.fileValidationError, "Close", {
          duration: 5000,
          verticalPosition: "top",
          panelClass: "snackbar-warning",
        });
      }

      if (oversizedFiles.length > 0) {
        this.fileValidationError = `${oversizedFiles.length} file(s) rejected: File size exceeds 10MB limit.`;
        this.snackBar.open(this.fileValidationError, "Close", {
          duration: 5000,
          verticalPosition: "top",
          panelClass: "snackbar-warning",
        });
      }

      // Add valid files to the list
      if (validFiles.length > 0) {
        this.selectedFiles = [...this.selectedFiles, ...validFiles];
        this.fileValidationError = null;
      }
    }
  }

  /**
   * Prevent default drag over behavior
   * @param event Drag event
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * Remove a file from the selected files list
   * @param index Index of file to remove
   */
  removeFile(index: number): void {
    this.selectedFiles = this.selectedFiles.filter((_, i) => i !== index);
  }

  /**
   * Format file size for display
   * @param bytes File size in bytes
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  }

  /**
   * Create the project and upload images
   */
  createProject(): void {
    if (this.projectForm.valid) {
      const request = {
        name: this.projectForm.get("name")?.value,
        type: this.projectForm.get("type")?.value,
        modelName: this.projectForm.get("modelName")?.value || undefined,
        groupName: this.projectForm.get("groupName")?.value || undefined,
      };

      this.store.dispatch(HomeActions.createProject({ request }));
    }
  }

  /**
   * Cancel project creation and navigate back
   */
  cancel(): void {
    this.router.navigate(["/landingai/projects"]);
  }
}
