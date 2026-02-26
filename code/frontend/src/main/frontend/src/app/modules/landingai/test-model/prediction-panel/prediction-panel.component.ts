import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from "@angular/core";
import { Subject } from "rxjs";
import { debounceTime, distinctUntilChanged } from "rxjs/operators";
import {
  PredictionResult,
  GroupedPrediction,
  Label,
} from "../../../../models/landingai/test-model.model";
import { ProjectClass } from "../../../../models/landingai/project-class.model";
import { ProjectClassService } from "../../../../services/landingai/project-class.service";

/**
 * Component for displaying prediction results and controls.
 */
@Component({
  selector: "app-prediction-panel",
  standalone: false,
  templateUrl: "./prediction-panel.component.html",
  styleUrls: ["./prediction-panel.component.scss"],
})
export class PredictionPanelComponent implements OnChanges, OnInit {
  @Input() predictions!: PredictionResult;
  @Input() confidenceThreshold!: number;
  @Input() projectId!: number;
  @Output() thresholdChanged = new EventEmitter<number>();

  activeTab: "prediction" | "json" = "prediction";
  selectedTabIndex: number = 0;
  groupedPredictions: GroupedPrediction[] = [];
  jsonOutput: string = "";
  projectClasses: ProjectClass[] = [];

  private thresholdSubject = new Subject<number>();

  constructor(private projectClassService: ProjectClassService) {
    // Debounce threshold changes for slider
    this.thresholdSubject
      .pipe(debounceTime(500), distinctUntilChanged())
      .subscribe((value) => {
        this.thresholdChanged.emit(value);
      });
  }

  ngOnInit(): void {
    // 載入 project classes
    if (this.projectId) {
      this.loadProjectClasses();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["predictions"]) {
      this.updatePredictions();
    }

    if (changes["projectId"] && this.projectId) {
      this.loadProjectClasses();
    }
  }

  /**
   * 載入 project classes
   */
  private loadProjectClasses(): void {
    console.log(
      "PredictionPanel: Loading project classes for projectId:",
      this.projectId
    );
    this.projectClassService.getClassesByProjectId(this.projectId).subscribe({
      next: (classes: ProjectClass[]) => {
        // 按 ID 排序 (假設 ID 順序對應 sequence)
        this.projectClasses = classes.sort(
          (a: ProjectClass, b: ProjectClass) => a.id - b.id
        );

        console.log(
          "PredictionPanel: Project classes loaded:",
          this.projectClasses.length
        );

        // 重新更新 predictions (如果已經有資料)
        if (this.predictions) {
          this.updatePredictions();
        }
      },
      error: (error: any) => {
        console.error(
          "PredictionPanel: Failed to load project classes:",
          error
        );
      },
    });
  }

  /**
   * Updates grouped predictions and JSON output.
   */
  private updatePredictions(): void {
    console.log("PredictionPanel: updatePredictions called");
    console.log("PredictionPanel: predictions object:", this.predictions);

    if (!this.predictions) {
      console.log("PredictionPanel: No predictions object");
      this.groupedPredictions = [];
      this.jsonOutput = "";
      return;
    }

    console.log(
      "PredictionPanel: predictions.predictions:",
      this.predictions.predictions
    );

    if (!this.predictions.predictions) {
      console.log("PredictionPanel: No predictions.predictions array");
      this.groupedPredictions = [];
      this.jsonOutput = "";
      return;
    }

    console.log(
      "PredictionPanel: predictions.predictions length:",
      this.predictions.predictions.length
    );

    // Group predictions by class
    this.groupedPredictions = this.groupPredictionsByClass();

    // Generate JSON output
    this.jsonOutput = JSON.stringify(this.predictions.predictions, null, 2);
  }

  /**
   * Groups predictions by class ID.
   */
  private groupPredictionsByClass(): GroupedPrediction[] {
    const groups = new Map<number, GroupedPrediction>();

    this.predictions.predictions.forEach((label: Label, index: number) => {
      // 支援 snake_case 和 camelCase
      const classId =
        label.classId !== undefined
          ? label.classId
          : label.class_id !== undefined
            ? label.class_id
            : -1;

      // 根據 classId 從 projectClasses 取得 className 和 color
      let className = label.className || label.class_name;
      let color = "#FF5C9A"; // 預設顏色

      if (
        !className &&
        this.projectClasses.length > 0 &&
        classId >= 0 &&
        classId < this.projectClasses.length
      ) {
        className = this.projectClasses[classId].className;
        color = this.projectClasses[classId].colorCode;
      } else if (!className) {
        className = `Class ${classId}`;
      } else if (
        this.projectClasses.length > 0 &&
        classId >= 0 &&
        classId < this.projectClasses.length
      ) {
        // className 已存在,但仍需取得 color
        color = this.projectClasses[classId].colorCode;
      }

      if (!groups.has(classId)) {
        groups.set(classId, {
          classId,
          className: className,
          color: color,
          count: 0,
          instances: [],
        });
      }

      const group = groups.get(classId)!;
      group.count++;

      // bbox 格式: [xcenter, ycenter, width, height]
      const [xcenter, ycenter, width, height] = label.bbox;

      group.instances.push({
        index: group.instances.length + 1,
        x: Math.round(xcenter * 1000),
        y: Math.round(ycenter * 1000),
        width: Math.round(width * 1000),
        height: Math.round(height * 1000),
        confidence: label.confidence,
      });
    });

    return Array.from(groups.values());
  }

  /**
   * Handles tab switching.
   */
  switchTab(tab: "prediction" | "json"): void {
    this.activeTab = tab;
  }

  /**
   * Handles tab index change.
   */
  onTabChange(index: number): void {
    this.selectedTabIndex = index;
    this.activeTab = index === 0 ? "prediction" : "json";
  }

  /**
   * Handles slider threshold change (debounced).
   */
  onSliderChange(value: string | number): void {
    const numValue = typeof value === "string" ? parseFloat(value) : value;
    this.confidenceThreshold = numValue;
    this.thresholdSubject.next(numValue);
  }

  /**
   * Handles input threshold change (immediate on blur).
   */
  onInputChange(value: string | number): void {
    const numValue = typeof value === "string" ? parseFloat(value) : value;
    this.confidenceThreshold = numValue;
    this.thresholdChanged.emit(numValue);
  }
}
