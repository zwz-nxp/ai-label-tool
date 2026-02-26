import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Snapshot } from "../../../../services/landingai/snapshot.service";
import { ManageActionType } from "../snapshot-list-view.component";

/**
 * Use action types for snapshot operations
 * Requirements: 4.1, 5.1
 */
export type UseActionType = "create_project" | "revert";

/**
 * Manage action types for snapshot operations
 * Requirements: 6.1, 7.1
 */
export type ManageSnapshotActionType = "download" | "delete";

/**
 * SnapshotToolbarComponent - Toolbar with Use and Manage buttons
 * Requirements: 4.1, 5.1, 6.1, 7.1
 *
 * This component displays the toolbar with action buttons for snapshot operations:
 * - Use button: Create new project from snapshot, Revert to snapshot
 * - Manage button: Download dataset, Delete snapshot
 *
 * Buttons are disabled when no snapshot is selected or when an operation is in progress.
 */
@Component({
  selector: "app-snapshot-toolbar",
  standalone: false,
  templateUrl: "./snapshot-toolbar.component.html",
  styleUrls: ["./snapshot-toolbar.component.scss"],
})
export class SnapshotToolbarComponent {
  /**
   * The currently selected snapshot
   * Used to enable/disable buttons and display snapshot info
   */
  @Input() selectedSnapshot: Snapshot | null = null;

  /**
   * Loading state for operations
   * When true, buttons are disabled to prevent multiple operations
   */
  @Input() loading: boolean = false;

  /**
   * Emits manage action events to parent component
   * Includes both Use and Manage menu actions
   */
  @Output() manageAction = new EventEmitter<ManageActionType>();

  /**
   * Handle Use button dropdown action
   * Requirements: 4.1, 5.1
   * @param action The use action type ('create_project' or 'revert')
   */
  onUseSnapshot(action: UseActionType): void {
    if (this.isDisabled()) {
      return;
    }
    this.manageAction.emit(action);
  }

  /**
   * Handle Manage button dropdown action
   * Requirements: 6.1, 7.1
   * @param action The manage action type ('download' or 'delete')
   */
  onManageSnapshot(action: ManageSnapshotActionType): void {
    if (this.isDisabled()) {
      return;
    }
    this.manageAction.emit(action);
  }

  /**
   * Check if buttons should be disabled
   * Buttons are disabled when:
   * - No snapshot is selected
   * - An operation is currently in progress (loading)
   * @returns true if buttons should be disabled
   */
  isDisabled(): boolean {
    return !this.selectedSnapshot || this.loading;
  }

  /**
   * Get the selected snapshot name for display
   * @returns The snapshot name or empty string if no snapshot selected
   */
  getSelectedSnapshotName(): string {
    return this.selectedSnapshot?.name || "";
  }

  /**
   * Check if a snapshot is currently selected
   * @returns true if a snapshot is selected
   */
  hasSelectedSnapshot(): boolean {
    return this.selectedSnapshot !== null;
  }
}
