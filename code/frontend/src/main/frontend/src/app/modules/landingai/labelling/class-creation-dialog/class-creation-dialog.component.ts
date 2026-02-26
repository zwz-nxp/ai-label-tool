import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectClassService } from "app/services/landingai/project-class.service";
import { ProjectClass } from "app/models/landingai/project-class.model";

export interface ClassCreationDialogData {
  projectId: number;
}

/**
 * Dialog component for creating a new class
 */
@Component({
  selector: "app-class-creation-dialog",
  templateUrl: "./class-creation-dialog.component.html",
  styleUrls: ["./class-creation-dialog.component.scss"],
  standalone: false,
})
export class ClassCreationDialogComponent implements OnInit {
  className = "";
  colorCode = "#FF0000"; // Default red color
  description = "";
  isCreating = false;
  errorMessage = "";

  // Predefined color palette
  colorPalette = [
    "#FF0000", // Red
    "#00FF00", // Green
    "#0000FF", // Blue
    "#FFFF00", // Yellow
    "#FF00FF", // Magenta
    "#00FFFF", // Cyan
    "#FFA500", // Orange
    "#800080", // Purple
    "#FFC0CB", // Pink
    "#A52A2A", // Brown
    "#808080", // Gray
    "#000000", // Black
  ];

  constructor(
    public dialogRef: MatDialogRef<ClassCreationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ClassCreationDialogData,
    private projectClassService: ProjectClassService
  ) {}

  ngOnInit(): void {
    // Initialize component
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    // Validate inputs
    if (!this.className.trim()) {
      this.errorMessage = "Class name is required";
      return;
    }

    if (!this.isValidHexColor(this.colorCode)) {
      this.errorMessage =
        "Invalid color code. Please use hex format (e.g., #FF0000)";
      return;
    }

    this.errorMessage = "";
    this.isCreating = true;

    const newClass: Omit<ProjectClass, "id" | "project"> = {
      className: this.className.trim(),
      colorCode: this.colorCode.toUpperCase(),
      description: this.description.trim() || undefined,
    };

    this.projectClassService
      .createClass(this.data.projectId, newClass)
      .subscribe({
        next: (createdClass) => {
          this.isCreating = false;
          this.dialogRef.close(createdClass);
        },
        error: (error) => {
          this.isCreating = false;
          this.errorMessage = error.message || "Failed to create class";
          console.error("Error creating class:", error);
        },
      });
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Select a color from the palette
   * @param color The color to select
   */
  selectColor(color: string): void {
    this.colorCode = color;
  }

  /**
   * Validate hex color format
   * @param color The color code to validate
   * @returns True if valid hex color
   */
  private isValidHexColor(color: string): boolean {
    const hexColorRegex = /^#[0-9A-Fa-f]{6}$/;
    return hexColorRegex.test(color);
  }
}
