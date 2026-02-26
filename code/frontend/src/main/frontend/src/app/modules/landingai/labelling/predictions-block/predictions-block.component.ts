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
 * Component for displaying Prediction annotations grouped by class
 */
@Component({
  selector: "app-predictions-block",
  templateUrl: "./predictions-block.component.html",
  styleUrls: ["./predictions-block.component.scss"],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PredictionsBlockComponent implements OnChanges {
  @Input() annotations: Annotation[] = [];
  @Input() selectedAnnotation: Annotation | null = null;
  @Input() selectedAnnotations: Annotation[] = [];
  @Input() selectedModel: string = "";
  @Output() annotationSelected = new EventEmitter<Annotation>();
  @Output() annotationsSelected = new EventEmitter<Annotation[]>();
  @Output() showPredictionsChanged = new EventEmitter<boolean>();
  @Output() labelAssistClicked = new EventEmitter<void>();

  predictionGroups: AnnotationGroup[] = [];
  showPredictions: boolean = true;
  predictionAnnotations: Annotation[] = [];

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["annotations"]) {
      this.filterPredictions();
    }
    if (changes["selectedAnnotation"] || changes["selectedAnnotations"]) {
      this.cdr.markForCheck();
    }
  }

  /**
   * Handle Predictions visibility toggle
   */
  onShowPredictionsChange(): void {
    this.showPredictionsChanged.emit(this.showPredictions);
  }

  /**
   * Handle Label Assist button click
   */
  onLabelAssistClick(): void {
    this.labelAssistClicked.emit();
  }

  /**
   * Handle annotation selection with multi-select support
   */
  onAnnotationClick(annotation: Annotation, event: MouseEvent): void {
    if (event.ctrlKey || event.metaKey) {
      const index = this.selectedAnnotations.findIndex(
        (a) => a.id === annotation.id
      );
      let newSelection: Annotation[];

      if (index > -1) {
        newSelection = this.selectedAnnotations.filter(
          (a) => a.id !== annotation.id
        );
      } else {
        newSelection = [...this.selectedAnnotations, annotation];
      }

      this.annotationsSelected.emit(newSelection);

      if (newSelection.length > 0) {
        this.annotationSelected.emit(newSelection[newSelection.length - 1]);
      } else {
        this.annotationSelected.emit(null as any);
      }
    } else {
      this.annotationsSelected.emit([annotation]);
      this.annotationSelected.emit(annotation);
    }
  }

  /**
   * Check if an annotation is selected
   */
  isAnnotationSelected(annotation: Annotation): boolean {
    if (this.selectedAnnotations.length > 0) {
      return this.selectedAnnotations.some((a) => a.id === annotation.id);
    }
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
   * Filter Prediction annotations
   */
  private filterPredictions(): void {
    this.predictionAnnotations = this.annotations
      .filter((a) => a.annotationType === "Prediction")
      .sort((a, b) => {
        const aId =
          typeof a.id === "number" ? a.id : parseInt(a.id as string, 10) || 0;
        const bId =
          typeof b.id === "number" ? b.id : parseInt(b.id as string, 10) || 0;
        return aId - bId;
      });
    this.groupPredictionsByClass();
  }

  /**
   * Group prediction annotations by class name
   */
  private groupPredictionsByClass(): void {
    const groupMap = new Map<string, AnnotationGroup>();

    this.predictionAnnotations.forEach((annotation) => {
      const className = annotation.className || "Unclassified";
      const color = annotation.color || "#808080";

      if (!groupMap.has(className)) {
        groupMap.set(className, {
          className: className,
          color: color,
          annotations: [],
          expanded: true,
        });
      }

      groupMap.get(className)!.annotations.push(annotation);
    });

    this.predictionGroups = Array.from(groupMap.values()).sort((a, b) =>
      a.className.localeCompare(b.className)
    );
  }
}
