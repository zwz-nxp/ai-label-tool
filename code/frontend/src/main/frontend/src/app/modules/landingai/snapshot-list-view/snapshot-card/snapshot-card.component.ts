import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Snapshot } from "../../../../services/landingai/snapshot.service";

/**
 * SnapshotCardComponent - Displays individual snapshot metadata card
 * Requirements: 2.2, 2.4
 *
 * This component displays snapshot information including name, creation date,
 * creator, image count, and class count. It supports selection highlighting.
 */
@Component({
  selector: "app-snapshot-card",
  standalone: false,
  templateUrl: "./snapshot-card.component.html",
  styleUrls: ["./snapshot-card.component.scss"],
})
export class SnapshotCardComponent {
  /**
   * The snapshot data to display
   * Requirements: 2.2
   */
  @Input() snapshot!: Snapshot;

  /**
   * Whether this snapshot is currently selected
   * Requirements: 2.4
   */
  @Input() selected: boolean = false;

  /**
   * Emits the snapshot ID when the card is clicked
   * Requirements: 2.4
   */
  @Output() clicked = new EventEmitter<number>();

  /**
   * Handle card click event
   * Emits the snapshot ID to parent component
   * Requirements: 2.4
   */
  onClick(): void {
    if (this.snapshot?.id) {
      this.clicked.emit(this.snapshot.id);
    }
  }

  /**
   * Handle keyboard navigation (Enter/Space)
   * Requirements: Accessibility
   */
  onKeydown(event: KeyboardEvent): void {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      this.onClick();
    }
  }

  /**
   * Format date for display
   * @param date The date to format
   * @returns Formatted date string
   */
  formatDate(date: Date | string): string {
    if (!date) return "";
    const d = new Date(date);
    return d.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }
}
