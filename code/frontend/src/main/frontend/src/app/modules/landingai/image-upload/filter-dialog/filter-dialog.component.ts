import { Component, Inject } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { FilterState } from "../../../../state/landingai/image-upload/image-upload.actions";
import { ProjectClass } from "../../../../models/landingai/project-class.model";
import { ProjectTag } from "../../../../models/landingai/project-tag.model";
import { ProjectMetadata } from "../../../../models/landingai/project-metadata.model";

export interface FilterDialogData {
  activeFilters: FilterState;
  availableClasses: ProjectClass[];
  availableTags: ProjectTag[];
  availableMetadata: ProjectMetadata[];
  selectedModelId: number | undefined;
  hidePredictionLabels?: boolean; // Optional flag to hide prediction labels (for snapshots)
}

export interface FilterDialogResult {
  filters?: FilterState;
  cleared?: boolean;
}

@Component({
  selector: "app-filter-dialog",
  standalone: false,
  templateUrl: "./filter-dialog.component.html",
  styleUrls: ["./filter-dialog.component.scss"],
})
export class FilterDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<FilterDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FilterDialogData
  ) {}

  /**
   * Handle filter change from filter panel
   */
  onFilterChange(filters: FilterState): void {
    console.log("FilterDialog: onFilterChange called with filters =", filters);
    console.log("FilterDialog: closing dialog with result =", { filters });
    this.dialogRef.close({ filters });
  }

  /**
   * Handle filter clear from filter panel
   */
  onFilterClear(): void {
    console.log("FilterDialog: onFilterClear called");
    this.dialogRef.close({ cleared: true });
  }

  /**
   * Handle close panel event
   */
  onClosePanel(): void {
    console.log("FilterDialog: onClosePanel called");
    this.dialogRef.close();
  }
}
