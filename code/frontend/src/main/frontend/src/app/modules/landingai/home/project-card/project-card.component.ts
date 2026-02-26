import { Component, EventEmitter, Input, Output } from "@angular/core";
import { ProjectListItem } from "../../../../models/landingai/project";

@Component({
  selector: "app-project-card",
  standalone: false,
  templateUrl: "./project-card.component.html",
  styleUrls: ["./project-card.component.scss"],
})
export class ProjectCardComponent {
  @Input() project!: ProjectListItem;
  @Output() cardClick = new EventEmitter<number>();
  @Output() editClick = new EventEmitter<ProjectListItem>();
  @Output() deleteClick = new EventEmitter<ProjectListItem>();

  /**
   * Get the thumbnail URL for the project
   * Returns the project's thumbnail URL or falls back to default image
   */
  getThumbnailUrl(): string {
    if (this.project?.thumbnailUrl) {
      return this.project.thumbnailUrl;
    }
    return this.getDefaultImageUrl();
  }

  /**
   * Get the default placeholder image URL
   * Used when project has no images
   */
  getDefaultImageUrl(): string {
    return "assets/factory.svg";
  }

  /**
   * Handle card click event
   * Emits the project ID to parent component
   */
  onCardClick(): void {
    if (this.project?.id) {
      this.cardClick.emit(this.project.id);
    }
  }

  /**
   * Handle menu button click
   * Prevents the card click event from firing
   */
  onMenuClick(event: Event): void {
    event.stopPropagation();
  }

  /**
   * Handle edit button click
   * Emits the project to parent component for editing
   */
  onEditClick(): void {
    this.editClick.emit(this.project);
  }

  /**
   * Handle delete button click
   * Emits the project to parent component for deletion
   */
  onDeleteClick(): void {
    this.deleteClick.emit(this.project);
  }

  /**
   * Get the Material icon name based on project type
   * Returns the appropriate icon for each project type
   */
  getTypeIcon(): string {
    switch (this.project?.type) {
      case "Object Detection":
        return "check_box_outline_blank";
      case "Segmentation":
        return "gradient";
      case "Classification":
        return "category";
      default:
        return "folder";
    }
  }
}
