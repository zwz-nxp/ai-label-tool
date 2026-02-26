import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Snapshot } from "../../../../services/landingai/snapshot.service";

/**
 * SnapshotSidebarComponent - Collapsible sidebar displaying snapshot list
 * Requirements: 2.1, 2.3, 2.4, 2.5
 *
 * This component displays all available snapshots for the current project
 * and allows users to select and switch between them.
 * Full implementation will be done in Task 5.
 */
@Component({
  selector: "app-snapshot-sidebar",
  standalone: false,
  templateUrl: "./snapshot-sidebar.component.html",
  styleUrls: ["./snapshot-sidebar.component.scss"],
})
export class SnapshotSidebarComponent {
  @Input() snapshots: Snapshot[] = [];
  @Input() selectedId: number | null = null;
  @Input() collapsed: boolean = false;
  @Input() loading: boolean = false;

  @Output() snapshotSelected = new EventEmitter<number>();
  @Output() toggleCollapse = new EventEmitter<void>();

  /**
   * Handle snapshot selection
   * Requirements: 2.3
   */
  selectSnapshot(snapshotId: number): void {
    this.snapshotSelected.emit(snapshotId);
  }

  /**
   * Toggle sidebar collapsed state
   * Requirements: 2.5
   */
  onToggleCollapse(): void {
    this.toggleCollapse.emit();
  }

  /**
   * Check if a snapshot is currently selected
   * Requirements: 2.4
   */
  isSelected(snapshotId: number): boolean {
    return this.selectedId === snapshotId;
  }
}
