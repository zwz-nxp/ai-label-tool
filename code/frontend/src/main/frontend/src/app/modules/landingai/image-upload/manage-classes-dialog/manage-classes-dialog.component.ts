import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectClassService } from "../../../../services/landingai/project-class.service";
import { ProjectClass } from "../../../../models/landingai/project-class.model";
import { MatSnackBar } from "@angular/material/snack-bar";

export interface ManageClassesDialogData {
  projectId: number;
}

@Component({
  selector: "app-manage-classes-dialog",
  standalone: false,
  templateUrl: "./manage-classes-dialog.component.html",
  styleUrls: ["./manage-classes-dialog.component.scss"],
})
export class ManageClassesDialogComponent implements OnInit {
  classes: ProjectClass[] = [];
  loading = false;
  selectedClass: ProjectClass | null = null;
  isCreating = false;
  editingClassName = "";
  editingColorCode = "";

  // Predefined color palette
  colorPalette = [
    "#9C27B0",
    "#FFEB3B",
    "#00BCD4",
    "#E91E63",
    "#2196F3",
    "#FF9800",
    "#8B4513",
    "#4E342E",
    "#CDDC39",
    "#8BC34A",
    "#26A69A",
    "#B0BEC5",
    "#F06292",
    "#673AB7",
    "#3F51B5",
  ];
  hasChanges = false;

  constructor(
    public dialogRef: MatDialogRef<ManageClassesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ManageClassesDialogData,
    private projectClassService: ProjectClassService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadClasses();
  }

  loadClasses(): void {
    this.loading = true;
    console.log("Loading classes for project:", this.data.projectId);
    this.projectClassService
      .getClassesByProjectId(this.data.projectId)
      .subscribe({
        next: (classes) => {
          console.log("Classes loaded:", classes);
          this.classes = classes;
          this.loading = false;
        },
        error: (error) => {
          console.error("Error loading classes:", error);
          console.error("Error status:", error.status);
          console.error("Error message:", error.message);
          console.error("Error body:", error.error);
          console.error("Response headers:", error.headers);
          this.snackBar.open("Failed to load classes", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
      });
  }

  onClassClick(projectClass: ProjectClass): void {
    if (this.isCreating) {
      return;
    }
    this.selectedClass = projectClass;
    this.editingClassName = projectClass.className;
    this.editingColorCode = projectClass.colorCode;
  }

  onEditClick(projectClass: ProjectClass, event: Event): void {
    event.stopPropagation();
    this.onClassClick(projectClass);
  }

  onDeleteClick(projectClass: ProjectClass, event: Event): void {
    event.stopPropagation();

    if (
      !confirm(
        `Are you sure you want to delete the class "${projectClass.className}"?`
      )
    ) {
      return;
    }

    this.loading = true;
    this.projectClassService.deleteClass(projectClass.id!).subscribe({
      next: () => {
        this.classes = this.classes.filter((c) => c.id !== projectClass.id);
        this.hasChanges = true;
        this.snackBar.open("Class deleted successfully", "Close", {
          duration: 3000,
        });
        this.loading = false;
      },
      error: (error) => {
        console.error("Error deleting class:", error);
        const message =
          error.status === 409
            ? "Cannot delete class: it is being used by labels"
            : "Failed to delete class";
        this.snackBar.open(message, "Close", { duration: 3000 });
        this.loading = false;
      },
    });
  }

  onBackToCardView(): void {
    this.selectedClass = null;
    this.isCreating = false;
  }

  onCreateClass(): void {
    this.isCreating = true;
    this.selectedClass = null;
    this.editingClassName = "";
    this.editingColorCode = this.colorPalette[0];
  }

  onColorSelect(color: string): void {
    this.editingColorCode = color;
  }

  onSaveNewClass(): void {
    if (!this.editingClassName.trim()) {
      this.snackBar.open("Class name is required", "Close", { duration: 3000 });
      return;
    }

    const newClass: Omit<ProjectClass, "id" | "project"> = {
      className: this.editingClassName.trim(),
      colorCode: this.editingColorCode,
    };

    this.loading = true;
    this.projectClassService
      .createClass(this.data.projectId, newClass)
      .subscribe({
        next: (created) => {
          this.classes.push(created);
          this.isCreating = false;
          this.hasChanges = true;
          this.snackBar.open("Class created successfully", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
        error: (error) => {
          console.error("Error creating class:", error);
          this.snackBar.open("Failed to create class", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
      });
  }

  onSaveEdit(): void {
    if (!this.selectedClass) {
      return;
    }

    if (!this.editingClassName.trim()) {
      this.snackBar.open("Class name is required", "Close", { duration: 3000 });
      return;
    }

    const updates: Partial<ProjectClass> = {
      className: this.editingClassName.trim(),
      colorCode: this.editingColorCode,
    };

    this.loading = true;
    this.projectClassService
      .updateClass(this.selectedClass.id!, updates)
      .subscribe({
        next: (updated) => {
          const index = this.classes.findIndex((c) => c.id === updated.id);
          if (index !== -1) {
            this.classes[index] = updated;
          }
          this.selectedClass = null;
          this.hasChanges = true;
          this.snackBar.open("Class updated successfully", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
        error: (error) => {
          console.error("Error updating class:", error);
          this.snackBar.open("Failed to update class", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
      });
  }

  onDeleteClass(): void {
    if (!this.selectedClass) {
      return;
    }

    if (
      !confirm(
        `Are you sure you want to delete the class "${this.selectedClass.className}"?`
      )
    ) {
      return;
    }

    this.loading = true;
    this.projectClassService.deleteClass(this.selectedClass.id!).subscribe({
      next: () => {
        this.classes = this.classes.filter(
          (c) => c.id !== this.selectedClass!.id
        );
        this.selectedClass = null;
        this.hasChanges = true;
        this.snackBar.open("Class deleted successfully", "Close", {
          duration: 3000,
        });
        this.loading = false;
      },
      error: (error) => {
        console.error("Error deleting class:", error);
        const message =
          error.status === 409
            ? "Cannot delete class: it is being used by labels"
            : "Failed to delete class";
        this.snackBar.open(message, "Close", { duration: 3000 });
        this.loading = false;
      },
    });
  }

  onCancelCreate(): void {
    this.isCreating = false;
  }

  onClose(): void {
    this.dialogRef.close({ hasChanges: this.hasChanges });
  }
}
