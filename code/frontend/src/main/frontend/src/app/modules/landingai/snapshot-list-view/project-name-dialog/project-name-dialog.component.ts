import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

/**
 * Data passed to the ProjectNameDialog
 */
export interface ProjectNameDialogData {
  defaultName?: string;
  existingProjectNames?: string[];
}

/**
 * Result returned from the ProjectNameDialog
 */
export interface ProjectNameDialogResult {
  projectName: string;
}

/**
 * ProjectNameDialogComponent
 * Requirements: 4.2
 *
 * Dialog component for entering a new project name when creating a project from a snapshot.
 * Validates that the project name is non-empty and optionally checks for uniqueness.
 */
@Component({
  selector: "app-project-name-dialog",
  standalone: false,
  templateUrl: "./project-name-dialog.component.html",
  styleUrls: ["./project-name-dialog.component.scss"],
})
export class ProjectNameDialogComponent implements OnInit {
  projectForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ProjectNameDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProjectNameDialogData
  ) {
    this.projectForm = this.fb.group({
      projectName: [
        "",
        [
          Validators.required,
          Validators.minLength(1),
          Validators.maxLength(100),
          this.uniqueNameValidator.bind(this),
        ],
      ],
    });
  }

  ngOnInit(): void {
    // Set default project name if provided
    if (this.data.defaultName) {
      this.projectForm.patchValue({ projectName: this.data.defaultName });
    }
  }

  /**
   * Custom validator to check if project name is unique
   * Requirements: 4.2 - Validate project name (unique)
   */
  private uniqueNameValidator(control: any): { [key: string]: any } | null {
    if (
      !this.data.existingProjectNames ||
      this.data.existingProjectNames.length === 0
    ) {
      return null;
    }

    const value = control.value?.trim().toLowerCase();
    const exists = this.data.existingProjectNames.some(
      (name) => name.toLowerCase() === value
    );

    return exists ? { uniqueName: { value: control.value } } : null;
  }

  /**
   * Handle form submission
   * Requirements: 4.2
   */
  onSubmit(): void {
    if (this.projectForm.valid) {
      const result: ProjectNameDialogResult = {
        projectName: this.projectForm.value.projectName.trim(),
      };
      this.dialogRef.close(result);
    }
  }

  /**
   * Handle cancel
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Get error message for project name field
   */
  getProjectNameError(): string {
    const control = this.projectForm.get("projectName");
    if (control?.hasError("required")) {
      return "Project name is required";
    }
    if (control?.hasError("minlength")) {
      return "Project name must be at least 1 character";
    }
    if (control?.hasError("maxlength")) {
      return "Project name must be 100 characters or less";
    }
    if (control?.hasError("uniqueName")) {
      return "A project with this name already exists";
    }
    return "";
  }
}
