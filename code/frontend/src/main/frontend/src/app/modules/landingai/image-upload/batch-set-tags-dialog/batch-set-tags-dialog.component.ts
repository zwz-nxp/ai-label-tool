import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ProjectTag } from "../../../../models/landingai/project-tag.model";

export interface BatchSetTagsDialogData {
  projectId: number;
  selectedImageIds: number[];
  availableTags: ProjectTag[];
}

@Component({
  selector: "app-batch-set-tags-dialog",
  standalone: false,
  templateUrl: "./batch-set-tags-dialog.component.html",
  styleUrls: ["./batch-set-tags-dialog.component.scss"],
})
export class BatchSetTagsDialogComponent implements OnInit {
  availableTags: ProjectTag[] = [];
  filteredTags: ProjectTag[] = [];
  selectedTagIds: Set<number> = new Set();
  selectedImageCount: number = 0;
  searchText: string = "";

  constructor(
    private dialogRef: MatDialogRef<BatchSetTagsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: BatchSetTagsDialogData
  ) {
    this.availableTags = data.availableTags;
    this.filteredTags = [...this.availableTags];
    this.selectedImageCount = data.selectedImageIds.length;
  }

  ngOnInit(): void {}

  /**
   * Handle search input change
   */
  onSearchChange(): void {
    const searchLower = this.searchText.toLowerCase().trim();
    if (!searchLower) {
      this.filteredTags = [...this.availableTags];
    } else {
      this.filteredTags = this.availableTags.filter((tag) =>
        tag.name.toLowerCase().includes(searchLower)
      );
    }
  }

  /**
   * Toggle tag selection
   */
  toggleTag(tagId: number): void {
    if (this.selectedTagIds.has(tagId)) {
      this.selectedTagIds.delete(tagId);
    } else {
      this.selectedTagIds.add(tagId);
    }
  }

  /**
   * Check if tag is selected
   */
  isTagSelected(tagId: number): boolean {
    return this.selectedTagIds.has(tagId);
  }

  /**
   * Handle apply button click
   */
  onApply(): void {
    const tagIds = Array.from(this.selectedTagIds);
    this.dialogRef.close({
      tagIds: tagIds,
      imageIds: this.data.selectedImageIds,
    });
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }

  /**
   * Check if apply button should be enabled
   */
  canApply(): boolean {
    return this.selectedTagIds.size > 0;
  }
}
