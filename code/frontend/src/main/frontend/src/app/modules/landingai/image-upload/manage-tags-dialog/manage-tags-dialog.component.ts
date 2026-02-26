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
import { ProjectTagService } from "../../../../services/landingai/project-tag.service";
import { ProjectTag } from "../../../../models/landingai/project-tag.model";
import { MatSnackBar } from "@angular/material/snack-bar";

export interface ManageTagsDialogData {
  projectId: number;
}

@Component({
  selector: "app-manage-tags-dialog",
  standalone: false,
  templateUrl: "./manage-tags-dialog.component.html",
  styleUrls: ["./manage-tags-dialog.component.scss"],
})
export class ManageTagsDialogComponent implements OnInit {
  @ViewChild("tagDialog") tagDialogTemplate!: TemplateRef<any>;

  tags: ProjectTag[] = [];
  filteredTags: ProjectTag[] = [];
  loading = false;
  searchQuery = "";
  editingTagName = "";
  editingTag: ProjectTag | null = null;
  isEditMode = false;
  tagDialogRef: MatDialogRef<any> | null = null;
  hasChanges = false;

  constructor(
    public dialogRef: MatDialogRef<ManageTagsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ManageTagsDialogData,
    private projectTagService: ProjectTagService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadTags();
  }

  loadTags(): void {
    this.loading = true;
    this.projectTagService.getTagsByProjectId(this.data.projectId).subscribe({
      next: (tags) => {
        this.tags = tags;
        this.filteredTags = tags;
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading tags:", error);
        this.snackBar.open("Failed to load tags", "Close", {
          duration: 3000,
        });
        this.loading = false;
      },
    });
  }

  onSearchChange(): void {
    const query = this.searchQuery.toLowerCase().trim();
    if (!query) {
      this.filteredTags = this.tags;
    } else {
      this.filteredTags = this.tags.filter((tag) =>
        tag.name.toLowerCase().includes(query)
      );
    }
  }

  onAddNewTag(): void {
    this.isEditMode = false;
    this.editingTag = null;
    this.editingTagName = "";
    this.openTagDialog();
  }

  onRenameTag(tag: ProjectTag): void {
    this.isEditMode = true;
    this.editingTag = tag;
    this.editingTagName = tag.name;
    this.openTagDialog();
  }

  openTagDialog(): void {
    this.tagDialogRef = this.dialog.open(this.tagDialogTemplate, {
      width: "500px",
      disableClose: false,
    });

    this.tagDialogRef.afterClosed().subscribe(() => {
      this.editingTag = null;
      this.editingTagName = "";
      this.tagDialogRef = null;
    });
  }

  onSaveTag(): void {
    const trimmedName = this.editingTagName.trim();
    if (!trimmedName) {
      this.snackBar.open("Tag name is required", "Close", { duration: 3000 });
      return;
    }

    // Case-insensitive duplicate check (exclude current tag when editing)
    const duplicate = this.tags.find(
      (t) =>
        t.name.toLowerCase() === trimmedName.toLowerCase() &&
        (!this.isEditMode || t.id !== this.editingTag?.id)
    );
    if (duplicate) {
      this.snackBar.open(
        `Tag "${duplicate.name}" already exists in this project`,
        "Close",
        { duration: 3000 }
      );
      return;
    }

    if (this.isEditMode && this.editingTag) {
      // Update existing tag
      this.loading = true;
      this.projectTagService
        .updateTag(this.editingTag.id!, { name: trimmedName })
        .subscribe({
          next: (updated) => {
            const index = this.tags.findIndex((t) => t.id === updated.id);
            if (index !== -1) {
              this.tags[index] = updated;
            }
            this.onSearchChange();
            this.hasChanges = true;
            this.snackBar.open("Tag updated successfully", "Close", {
              duration: 3000,
            });
            this.loading = false;
            // Close only the inner tag dialog, not the main manage tags dialog
            if (this.tagDialogRef) {
              this.tagDialogRef.close();
            }
          },
          error: (error) => {
            console.error("Error updating tag:", error);
            const message = error.error?.message?.includes("already exists")
              ? error.error.message
              : "Failed to update tag";
            this.snackBar.open(message, "Close", { duration: 3000 });
            this.loading = false;
          },
        });
    } else {
      // Create new tag
      const newTag: Omit<ProjectTag, "id"> = {
        name: trimmedName,
      };

      this.loading = true;
      this.projectTagService.createTag(this.data.projectId, newTag).subscribe({
        next: (created) => {
          this.tags.push(created);
          this.onSearchChange();
          this.hasChanges = true;
          this.snackBar.open("Tag created successfully", "Close", {
            duration: 3000,
          });
          this.loading = false;
          // Close only the inner tag dialog, not the main manage tags dialog
          if (this.tagDialogRef) {
            this.tagDialogRef.close();
          }
        },
        error: (error) => {
          console.error("Error creating tag:", error);
          const message = error.error?.message?.includes("already exists")
            ? error.error.message
            : "Failed to create tag";
          this.snackBar.open(message, "Close", { duration: 3000 });
          this.loading = false;
        },
      });
    }
  }

  onDeleteTag(tag: ProjectTag): void {
    if (!confirm(`Are you sure you want to delete the tag "${tag.name}"?`)) {
      return;
    }

    this.loading = true;
    this.projectTagService.deleteTag(tag.id!).subscribe({
      next: () => {
        this.tags = this.tags.filter((t) => t.id !== tag.id);
        this.onSearchChange();
        this.hasChanges = true;
        this.snackBar.open("Tag deleted successfully", "Close", {
          duration: 3000,
        });
        this.loading = false;
      },
      error: (error) => {
        console.error("Error deleting tag:", error);
        const message =
          error.status === 409
            ? "Cannot delete tag: it is being used by images"
            : "Failed to delete tag";
        this.snackBar.open(message, "Close", { duration: 3000 });
        this.loading = false;
      },
    });
  }

  onClose(): void {
    this.dialogRef.close({ hasChanges: this.hasChanges });
  }
}
