import { Component, Input, Output, EventEmitter } from "@angular/core";
import { ModelDisplayDto } from "../../../../models/landingai/model";

/**
 * Model Actions Component
 * Implementation requirements 5.1, 5.2: Actions column includes favorite star icon and kebab menu
 */
@Component({
  selector: "app-model-actions",
  templateUrl: "./model-actions.component.html",
  styleUrls: ["./model-actions.component.scss"],
  standalone: false,
})
export class ModelActionsComponent {
  @Input() model!: ModelDisplayDto;
  @Input() showFavoriteButton: boolean = true;
  @Input() showKebabMenu: boolean = true;

  @Output() favoriteToggle = new EventEmitter<number>();
  @Output() actionMenuClick = new EventEmitter<{
    modelId: number;
    action: string;
  }>();

  /**
   * Handle favorite toggle
   * Implementation requirements 3.1, 5.4: When user clicks favorite star icon, toggle the model's Favorite_Status
   */
  onFavoriteToggle(event: Event): void {
    event.stopPropagation(); // Prevent event bubbling
    this.favoriteToggle.emit(this.model.id);
  }

  /**
   * Handle action menu click
   * Implementation requirements 5.2: When user clicks kebab menu, display available model actions
   * Supported actions: copyId, testModel, download, delete
   */
  onActionMenuClick(action: string, event?: Event): void {
    if (event) {
      event.stopPropagation(); // Prevent event bubbling
    }
    this.actionMenuClick.emit({ modelId: this.model.id, action });
  }

  /**
   * Handle kebab menu open
   */
  onKebabMenuClick(event: Event): void {
    event.stopPropagation(); // Prevent event bubbling
  }

  /**
   * Get favorite icon name
   * Implementation requirements 3.3, 3.4: Star icon visual state
   */
  getFavoriteIconName(): string {
    return this.model.isFavorite ? "star" : "star_border";
  }

  /**
   * Get favorite button ARIA label
   * Implementation requirements 6.1: Provide aria-labels for all icon buttons and interactive elements
   */
  getFavoriteAriaLabel(): string {
    const action = this.model.isFavorite
      ? "Remove from favorites"
      : "Add to favorites";
    return `${action} for ${this.model.modelName}`;
  }

  /**
   * Get favorite button tooltip
   * Implementation requirements 6.2: Ensure all actionable icons have descriptive tooltips
   */
  getFavoriteTooltip(): string {
    return this.model.isFavorite ? "Remove from favorites" : "Add to favorites";
  }

  /**
   * Get kebab menu ARIA label
   */
  getKebabAriaLabel(): string {
    return `More actions for ${this.model.modelName}`;
  }

  /**
   * Get kebab menu tooltip
   */
  getKebabTooltip(): string {
    return "More actions";
  }

  /**
   * Check if favorite button should be displayed
   */
  shouldShowFavoriteButton(): boolean {
    return this.showFavoriteButton && !!this.model;
  }

  /**
   * Check if kebab menu should be displayed
   */
  shouldShowKebabMenu(): boolean {
    return this.showKebabMenu && !!this.model;
  }

  /**
   * Get favorite button CSS class
   */
  getFavoriteButtonClass(): string {
    return this.model.isFavorite ? "favorite-active" : "favorite-inactive";
  }

  /**
   * Get actions container CSS class
   */
  getActionsContainerClass(): string {
    const classes = ["actions-container"];

    if (!this.shouldShowFavoriteButton()) {
      classes.push("no-favorite");
    }

    if (!this.shouldShowKebabMenu()) {
      classes.push("no-kebab");
    }

    return classes.join(" ");
  }
}
