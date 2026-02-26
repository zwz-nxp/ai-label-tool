import {
  Component,
  Inject,
  OnInit,
  TemplateRef,
  ViewChild,
} from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from "@angular/material/dialog";
import { ProjectMetadataService } from "../../../../services/landingai/project-metadata.service";
import { ProjectMetadata } from "../../../../models/landingai/project-metadata.model";
import { MatSnackBar } from "@angular/material/snack-bar";

export interface ManageMetadataDialogData {
  projectId: number;
}

@Component({
  selector: "app-manage-metadata-dialog",
  standalone: false,
  templateUrl: "./manage-metadata-dialog.component.html",
  styleUrls: ["./manage-metadata-dialog.component.scss"],
})
export class ManageMetadataDialogComponent implements OnInit {
  @ViewChild("metadataDialog") metadataDialogTemplate!: TemplateRef<any>;

  metadata: ProjectMetadata[] = [];
  filteredMetadata: ProjectMetadata[] = [];
  loading = false;
  searchQuery = "";

  // For create/edit dialog
  editingMetadata: ProjectMetadata | null = null;
  isEditMode = false;
  metadataDialogRef: MatDialogRef<any> | null = null;

  // Form fields
  metadataName = "";
  metadataType = "TEXT";
  metadataValueFrom = "INPUT";
  metadataPredefinedValues = "";
  metadataMultipleValues = false;
  hasChanges = false;

  typeOptions = ["TEXT", "NUMBER", "BOOLEAN"];
  valueFromOptions = ["PREDEFINED", "INPUT"];

  constructor(
    public dialogRef: MatDialogRef<ManageMetadataDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ManageMetadataDialogData,
    private projectMetadataService: ProjectMetadataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadMetadata();
  }

  loadMetadata(): void {
    this.loading = true;
    this.projectMetadataService
      .getMetadataByProjectId(this.data.projectId)
      .subscribe({
        next: (metadata) => {
          this.metadata = metadata;
          this.filteredMetadata = metadata;
          this.loading = false;
        },
        error: (error) => {
          console.error("Error loading metadata:", error);
          this.snackBar.open("Failed to load metadata", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
      });
  }

  onSearchChange(): void {
    const query = this.searchQuery.toLowerCase().trim();
    if (!query) {
      this.filteredMetadata = this.metadata;
    } else {
      this.filteredMetadata = this.metadata.filter((meta) =>
        meta.name.toLowerCase().includes(query)
      );
    }
  }

  onAddNewMetadata(): void {
    this.isEditMode = false;
    this.editingMetadata = null;
    this.resetForm();
    this.openMetadataDialog();
  }

  onEditMetadata(metadata: ProjectMetadata): void {
    this.isEditMode = true;
    this.editingMetadata = metadata;
    this.metadataName = metadata.name;
    this.metadataType = metadata.type;
    this.metadataValueFrom = metadata.valueFrom;
    this.metadataPredefinedValues = metadata.predefinedValues || "";
    this.metadataMultipleValues = metadata.multipleValues;
    this.openMetadataDialog();
  }

  resetForm(): void {
    this.metadataName = "";
    this.metadataType = "TEXT";
    this.metadataValueFrom = "INPUT";
    this.metadataPredefinedValues = "";
    this.metadataMultipleValues = false;
  }

  openMetadataDialog(): void {
    this.metadataDialogRef = this.dialog.open(this.metadataDialogTemplate, {
      width: "600px",
      disableClose: false,
    });

    this.metadataDialogRef.afterClosed().subscribe(() => {
      this.editingMetadata = null;
      this.resetForm();
      this.metadataDialogRef = null;
    });
  }

  onSaveMetadata(): void {
    if (!this.isFormValid()) {
      if (!this.metadataName.trim()) {
        this.snackBar.open("Metadata name is required", "Close", {
          duration: 3000,
        });
      } else if (
        this.metadataValueFrom === "PREDEFINED" &&
        !this.metadataPredefinedValues.trim()
      ) {
        this.snackBar.open(
          "Predefined values are required when Value From is PREDEFINED",
          "Close",
          { duration: 3000 }
        );
      }
      return;
    }

    if (this.isEditMode && this.editingMetadata) {
      // Update existing metadata
      this.loading = true;
      const update: Partial<ProjectMetadata> = {
        name: this.metadataName.trim(),
        type: this.metadataType,
        valueFrom: this.metadataValueFrom,
        predefinedValues: this.metadataPredefinedValues.trim() || undefined,
        multipleValues: this.metadataMultipleValues,
      };

      this.projectMetadataService
        .updateMetadata(this.editingMetadata.id!, update)
        .subscribe({
          next: (updated) => {
            const index = this.metadata.findIndex((m) => m.id === updated.id);
            if (index !== -1) {
              this.metadata[index] = updated;
            }
            this.onSearchChange();
            this.hasChanges = true;
            this.snackBar.open("Metadata updated successfully", "Close", {
              duration: 3000,
            });
            this.loading = false;
            if (this.metadataDialogRef) {
              this.metadataDialogRef.close();
            }
          },
          error: (error) => {
            console.error("Error updating metadata:", error);
            this.snackBar.open("Failed to update metadata", "Close", {
              duration: 3000,
            });
            this.loading = false;
          },
        });
    } else {
      // Create new metadata
      const newMetadata: Omit<ProjectMetadata, "id" | "project"> = {
        name: this.metadataName.trim(),
        type: this.metadataType,
        valueFrom: this.metadataValueFrom,
        predefinedValues: this.metadataPredefinedValues.trim() || undefined,
        multipleValues: this.metadataMultipleValues,
      };

      this.loading = true;
      this.projectMetadataService
        .createMetadata(this.data.projectId, newMetadata)
        .subscribe({
          next: (created) => {
            this.metadata.push(created);
            this.onSearchChange();
            this.hasChanges = true;
            this.snackBar.open("Metadata created successfully", "Close", {
              duration: 3000,
            });
            this.loading = false;
            if (this.metadataDialogRef) {
              this.metadataDialogRef.close();
            }
          },
          error: (error) => {
            console.error("Error creating metadata:", error);
            this.snackBar.open("Failed to create metadata", "Close", {
              duration: 3000,
            });
            this.loading = false;
          },
        });
    }
  }

  onDeleteMetadata(metadata: ProjectMetadata): void {
    if (
      !confirm(
        `Are you sure you want to delete the metadata "${metadata.name}"?`
      )
    ) {
      return;
    }

    this.loading = true;
    this.projectMetadataService.deleteMetadata(metadata.id!).subscribe({
      next: () => {
        this.metadata = this.metadata.filter((m) => m.id !== metadata.id);
        this.onSearchChange();
        this.hasChanges = true;
        this.snackBar.open("Metadata deleted successfully", "Close", {
          duration: 3000,
        });
        this.loading = false;
      },
      error: (error) => {
        console.error("Error deleting metadata:", error);
        const message =
          error.status === 409
            ? "Cannot delete metadata: it is being used by images"
            : "Failed to delete metadata";
        this.snackBar.open(message, "Close", { duration: 3000 });
        this.loading = false;
      },
    });
  }

  onClose(): void {
    this.dialogRef.close({ hasChanges: this.hasChanges });
  }

  isFormValid(): boolean {
    // Name is required
    if (!this.metadataName.trim()) {
      return false;
    }

    // If Value From is PREDEFINED, predefined values are required
    if (
      this.metadataValueFrom === "PREDEFINED" &&
      !this.metadataPredefinedValues.trim()
    ) {
      return false;
    }

    return true;
  }
}
