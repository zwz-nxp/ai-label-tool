import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from "@angular/core";
import { Annotation } from "app/models/landingai/annotation.model";

/**
 * Interface for grouped annotations by class
 */
interface AnnotationGroup {
  className: string;
  color: string;
  annotations: Annotation[];
  expanded: boolean;
}

/**
 * Component for displaying annotations grouped by class
 * Shows coordinate information and allows selection
 * Supports multi-selection with Ctrl + Click
 */
@Component({
  selector: "app-labels-block",
  templateUrl: "./labels-block.component.html",
  styleUrls: ["./labels-block.component.scss"],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LabelsBlockComponent implements OnChanges {
  @Input() annotations: Annotation[] = [];
  @Input() selectedAnnotation: Annotation | null = null;
  @Input() selectedAnnotations: Annotation[] = [];
  @Output() annotationSelected = new EventEmitter<Annotation>();
  @Output() annotationsSelected = new EventEmitter<Annotation[]>();
  @Output() showLabelsChanged = new EventEmitter<boolean>();

  annotationGroups: AnnotationGroup[] = [];
  showLabels: boolean = true;
  filteredAnnotations: Annotation[] = [];

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["annotations"]) {
      this.applyFilter();
    }
    // Trigger change detection when selection changes from canvas
    if (changes["selectedAnnotation"] || changes["selectedAnnotations"]) {
      this.cdr.markForCheck();
    }
  }

  /**
   * Handle Labels visibility toggle
   */
  onShowLabelsChange(): void {
    this.showLabelsChanged.emit(this.showLabels);
  }

  /**
   * Toggle the expanded state of a class group
   */
  toggleGroup(group: AnnotationGroup): void {
    group.expanded = !group.expanded;
  }

  /**
   * Handle annotation selection with multi-select support
   * Ctrl + Click to add/remove from selection
   * Normal click to select single annotation
   */
  onAnnotationClick(annotation: Annotation, event: MouseEvent): void {
    if (event.ctrlKey || event.metaKey) {
      // Multi-select mode: toggle annotation in selection
      const index = this.selectedAnnotations.findIndex(
        (a) => a.id === annotation.id
      );
      let newSelection: Annotation[];

      if (index > -1) {
        // Remove from selection
        newSelection = this.selectedAnnotations.filter(
          (a) => a.id !== annotation.id
        );
      } else {
        // Add to selection
        newSelection = [...this.selectedAnnotations, annotation];
      }

      this.annotationsSelected.emit(newSelection);

      // Also emit single selection for the last clicked item
      if (newSelection.length > 0) {
        this.annotationSelected.emit(newSelection[newSelection.length - 1]);
      } else {
        this.annotationSelected.emit(null as any);
      }
    } else {
      // Single select mode: clear selection and select only this one
      this.annotationsSelected.emit([annotation]);
      this.annotationSelected.emit(annotation);
    }
  }

  /**
   * Check if an annotation is selected (supports both single and multi-select)
   */
  isAnnotationSelected(annotation: Annotation): boolean {
    // Check in multi-selection array first
    if (this.selectedAnnotations.length > 0) {
      return this.selectedAnnotations.some((a) => a.id === annotation.id);
    }
    // Fall back to single selection
    return (
      this.selectedAnnotation !== null &&
      this.selectedAnnotation.id === annotation.id
    );
  }

  /**
   * Get coordinate information for an annotation
   */
  getCoordinateInfo(annotation: Annotation): string {
    switch (annotation.type) {
      case "RECTANGLE":
        return `x: ${Math.round(annotation.x)}, y: ${Math.round(annotation.y)}, w: ${Math.round(annotation.width)}, h: ${Math.round(annotation.height)}`;

      case "POLYGON":
      case "POLYLINE":
        if (annotation.points && annotation.points.length > 0) {
          return `${annotation.points.length} points`;
        }
        return "No points";

      case "ELLIPSE":
        return `x: ${Math.round(annotation.x)}, y: ${Math.round(annotation.y)}, rx: ${Math.round(annotation.width / 2)}, ry: ${Math.round(annotation.height / 2)}`;

      case "BRUSH":
        return "Brush annotation";

      default:
        return "Unknown type";
    }
  }

  /**
   * Filter Ground Truth annotations
   * Sort by label id
   */
  private applyFilter(): void {
    // Filter Ground Truth annotations for Labels block
    this.filteredAnnotations = this.annotations
      .filter(
        (a) =>
          a.annotationType === "Ground Truth" ||
          a.annotationType === "Ground truth"
      )
      .sort((a, b) => {
        // Sort by id (numeric comparison if possible)
        const aId =
          typeof a.id === "number" ? a.id : parseInt(a.id as string, 10) || 0;
        const bId =
          typeof b.id === "number" ? b.id : parseInt(b.id as string, 10) || 0;
        return aId - bId;
      });

    this.groupAnnotationsByClass();
  }

  /**
   * Group annotations by class name
   */
  private groupAnnotationsByClass(): void {
    const groupMap = new Map<string, AnnotationGroup>();

    // Group filtered annotations by class name
    this.filteredAnnotations.forEach((annotation) => {
      const className = annotation.className || "Unclassified";
      const color = annotation.color || "#808080";

      if (!groupMap.has(className)) {
        groupMap.set(className, {
          className: className,
          color: color,
          annotations: [],
          expanded: true, // Expand all groups by default
        });
      }

      groupMap.get(className)!.annotations.push(annotation);
    });

    // Convert map to array and sort by class name
    this.annotationGroups = Array.from(groupMap.values()).sort((a, b) =>
      a.className.localeCompare(b.className)
    );
  }
}
