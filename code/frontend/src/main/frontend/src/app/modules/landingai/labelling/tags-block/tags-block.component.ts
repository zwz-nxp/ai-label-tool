import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild,
} from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import {
  MatAutocompleteTrigger,
  MatAutocompleteSelectedEvent,
} from "@angular/material/autocomplete";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { Image } from "app/models/landingai/image.model";
import { Project } from "app/models/landingai/project.model";
import {
  ImageTag,
  ProjectTag as ServiceProjectTag,
  TagService,
} from "app/services/landingai/tag.service";
import { ProjectTagService } from "app/services/landingai/project-tag.service";
import { ProjectTag as ModelProjectTag } from "app/models/landingai/project-tag.model";
import { DEFAULT_PROJECT_TAGS } from "../default-project-config";
import { ManageTagsDialogComponent } from "app/modules/landingai/image-upload/manage-tags-dialog/manage-tags-dialog.component";

/**
 * Component for managing image tags
 * Allows adding, displaying, and deleting tags for the current image
 */
@Component({
  selector: "app-tags-block",
  templateUrl: "./tags-block.component.html",
  styleUrls: ["./tags-block.component.scss"],
  standalone: false,
})
export class TagsBlockComponent implements OnChanges, OnDestroy {
  @Input() currentImage: Image | null = null;
  @Input() project: Project | null = null;
  @ViewChild(MatAutocompleteTrigger)
  autocompleteTrigger!: MatAutocompleteTrigger;

  tags: ImageTag[] = [];
  projectTags: ServiceProjectTag[] = [];
  filteredTags: ServiceProjectTag[] = [];
  isLoadingTags = false;
  isLoadingProjectTags = false;
  isAddingTag = false;
  tagInputValue = "";

  private destroy$ = new Subject<void>();

  constructor(
    private tagService: TagService,
    private projectTagService: ProjectTagService,
    private dialog: MatDialog
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["project"] && this.project) {
      this.loadProjectTags();
    }

    if (changes["currentImage"]) {
      if (this.currentImage) {
        this.loadTags();
      } else {
        this.tags = [];
      }
      // Reset input state when image changes
      this.tagInputValue = "";
      this.updateFilteredTags();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Update filtered tags when input changes
   */
  onInputChange(): void {
    this.updateFilteredTags();
  }

  /**
   * Update the filtered tags list based on input value
   */
  private updateFilteredTags(): void {
    const availableTags = this.getAvailableProjectTags();

    if (!this.tagInputValue || this.tagInputValue.trim() === "") {
      this.filteredTags = [...availableTags]; // Create new array reference
    } else {
      const searchValue = this.tagInputValue.toLowerCase();
      this.filteredTags = availableTags.filter((tag) =>
        tag.name.toLowerCase().includes(searchValue)
      );
    }
  }

  /**
   * Get available project tags that are not already added to the image
   */
  getAvailableProjectTags(): ServiceProjectTag[] {
    const addedTagIds = new Set(this.tags.map((t) => t.projectTag.id));
    return this.projectTags.filter((pt) => !addedTagIds.has(pt.id));
  }

  /**
   * Handle tag selection from autocomplete
   */
  onTagSelected(event: MatAutocompleteSelectedEvent): void {
    this.tagInputValue = event.option.value;
    this.onAddTag();
  }

  /**
   * Add a tag when user presses Enter
   * If tag exists in project tags, just add to image
   * If tag doesn't exist, create project tag first, then add to image
   */
  onAddTag(): void {
    if (!this.currentImage || !this.tagInputValue.trim() || this.isAddingTag) {
      return;
    }

    const tagName = this.tagInputValue.trim();

    // Check if tag already exists in project tags
    const existingProjectTag = this.projectTags.find(
      (pt) => pt.name.toLowerCase() === tagName.toLowerCase()
    );

    if (existingProjectTag) {
      // Check if this tag is already added to the current image
      const isAlreadyAdded = this.tags.some(
        (t) => t.projectTag.id === existingProjectTag.id
      );

      if (isAlreadyAdded) {
        alert(`Tag "${tagName}" is already added to this image.`);
        this.tagInputValue = "";
        return;
      }

      // Tag exists and not added yet, add to image
      if (existingProjectTag.id) {
        this.addImageTag(existingProjectTag.id);
      } else {
        console.error("Existing tag has no ID");
      }
    } else {
      // Tag doesn't exist, create project tag first
      this.createProjectTagAndAddToImage(tagName);
    }
  }

  /**
   * Create a new project tag and then add it to the image
   */
  private createProjectTagAndAddToImage(tagName: string): void {
    if (!this.project || !this.currentImage) {
      console.error("Missing project or currentImage");
      return;
    }

    // Case-insensitive duplicate check against existing project tags
    const duplicate = this.projectTags.find(
      (pt) => pt.name.toLowerCase() === tagName.toLowerCase()
    );
    if (duplicate) {
      alert(
        `Tag "${duplicate.name}" already exists in this project. Please select it from the list.`
      );
      this.tagInputValue = "";
      return;
    }

    this.isAddingTag = true;

    // Create tag object without id (as per Omit<ProjectTag, "id">)
    const newTag: ModelProjectTag = {
      name: tagName,
    };

    this.projectTagService
      .createTag(this.project.id, newTag)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (newProjectTag) => {
          // Convert ModelProjectTag to ServiceProjectTag and add to the list
          const serviceTag: ServiceProjectTag = {
            id: newProjectTag.id!,
            name: newProjectTag.name,
            createdAt: newProjectTag.createdAt,
            createdBy: newProjectTag.createdBy,
          };
          this.projectTags.push(serviceTag);

          // Update filtered tags to include the new tag in autocomplete
          this.updateFilteredTags();

          // Now add to image
          if (newProjectTag.id) {
            this.addImageTag(newProjectTag.id);
          } else {
            console.error("Created tag has no ID");
            this.isAddingTag = false;
          }
        },
        error: (error) => {
          console.error("Error creating project tag:", error);
          // Check if it's a duplicate tag error
          if (
            error.status === 400 &&
            error.error?.message?.includes("already exists")
          ) {
            alert(`Tag "${tagName}" already exists in this project.`);
          } else {
            alert("Failed to create tag. Please try again.");
          }
          this.isAddingTag = false;
        },
      });
  }

  /**
   * Add an existing project tag to the current image
   */
  private addImageTag(projectTagId: number): void {
    if (!this.currentImage) {
      console.error("No current image");
      return;
    }

    this.isAddingTag = true;

    this.tagService
      .addImageTag(this.currentImage.id, projectTagId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (createdTag) => {
          this.tags.push(createdTag);
          this.tagInputValue = "";
          this.isAddingTag = false;

          // Update filtered tags to remove the newly added tag from autocomplete
          this.updateFilteredTags();

          // Close autocomplete panel if open
          if (this.autocompleteTrigger && this.autocompleteTrigger.panelOpen) {
            this.autocompleteTrigger.closePanel();
          }
        },
        error: (error) => {
          console.error("Error adding tag to image:", error);
          // Check if it's a duplicate tag error
          if (
            error.status === 400 &&
            error.error?.message?.includes("already exists")
          ) {
            alert("This tag is already added to the image.");
          } else {
            alert("Failed to add tag to image. Please try again.");
          }
          this.isAddingTag = false;
        },
      });
  }

  /**
   * Delete a tag
   */
  onDeleteTag(tag: ImageTag): void {
    if (!this.currentImage || !tag.id) {
      return;
    }

    if (
      confirm(
        `Are you sure you want to delete the tag "${tag.projectTag.name}"?`
      )
    ) {
      this.tagService
        .removeImageTag(this.currentImage.id, tag.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.tags = this.tags.filter((t) => t.id !== tag.id);
            this.updateFilteredTags();
          },
          error: (error) => {
            console.error("Error deleting tag:", error);
            alert("Failed to delete tag. Please try again.");
          },
        });
    }
  }

  /**
   * Load project tag definitions
   */
  private loadProjectTags(): void {
    if (!this.project) {
      return;
    }

    this.isLoadingProjectTags = true;
    this.tagService
      .getTagsByProjectId(this.project.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tags) => {
          this.projectTags = tags;
          this.isLoadingProjectTags = false;
          this.updateFilteredTags();
        },
        error: (error) => {
          console.error("Error loading project tags:", error);
          this.projectTags = [];
          this.isLoadingProjectTags = false;
          this.updateFilteredTags();
        },
      });
  }

  /**
   * Load tags for the current image
   */
  private loadTags(): void {
    if (!this.currentImage) {
      return;
    }

    this.isLoadingTags = true;
    this.tagService
      .getTagsByImageId(this.currentImage.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tags) => {
          this.tags = tags;
          this.isLoadingTags = false;
          this.updateFilteredTags();
        },
        error: (error) => {
          console.error("Error loading tags:", error);
          this.tags = [];
          this.isLoadingTags = false;
          this.updateFilteredTags();
        },
      });
  }
}
