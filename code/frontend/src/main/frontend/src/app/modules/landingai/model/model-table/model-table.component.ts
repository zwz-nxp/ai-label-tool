import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  AfterViewInit,
  ViewChild,
} from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";
import {
  ModelDisplayDto,
  TableColumn,
} from "../../../../models/landingai/model";

/**
 * Event emitted when user clicks on a model row cell
 */
export interface ModelRowClickEvent {
  modelId: number;
  clickedColumn: string;
}

/**
 * Model Table Component
 * Implementation requirements 1.1, 1.5, 7.5: Data table structure and accessibility features
 */
@Component({
  selector: "app-model-table",
  templateUrl: "./model-table.component.html",
  styleUrls: ["./model-table.component.scss"],
  standalone: false,
})
export class ModelTableComponent implements OnInit, OnChanges, AfterViewInit {
  @Input() models: ModelDisplayDto[] = [];
  @Input() loading: boolean = false;

  @Output() favoriteToggle = new EventEmitter<number>();
  @Output() actionMenuClick = new EventEmitter<{
    modelId: number;
    action: string;
  }>();
  @Output() rowClick = new EventEmitter<ModelRowClickEvent>();

  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<ModelDisplayDto>();

  // Implementation requirements 1.1: Table column definitions - Model, Train, Dev, Test, More evaluation set, Confidence Threshold, Actions
  // 注意：moreEvaluationSet 已暫時隱藏（保留程式碼以便未來重新啟用）
  displayedColumns: string[] = [
    "modelName",
    "trainMetric",
    "devMetric",
    "testMetric",
    // "moreEvaluationSet", // 暫時隱藏
    "confidenceThreshold",
    "actions",
  ];

  // Table column configuration - Requirements 1.5, 7.5: Appropriate column widths and alignment
  // 注意：moreEvaluationSet 已暫時隱藏（保留程式碼以便未來重新啟用）
  tableColumns: TableColumn[] = [
    {
      key: "modelName",
      label: "Model",
      sortable: false,
      width: "25%",
      align: "left",
    },
    {
      key: "trainMetric",
      label: "Train",
      sortable: false,
      width: "10%",
      align: "center",
    },
    {
      key: "devMetric",
      label: "Dev",
      sortable: false,
      width: "10%",
      align: "center",
    },
    {
      key: "testMetric",
      label: "Test",
      sortable: false,
      width: "10%",
      align: "center",
    },
    // 暫時隱藏 moreEvaluationSet 欄位
    // {
    //   key: "moreEvaluationSet" as any,
    //   label: "More evaluation set",
    //   sortable: false,
    //   width: "15%",
    //   align: "center",
    // },
    {
      key: "confidenceThreshold",
      label: "Confidence Threshold",
      sortable: false,
      width: "15%",
      align: "center",
    },
    {
      key: "actions" as keyof ModelDisplayDto,
      label: "Actions",
      sortable: false,
      width: "5%",
      align: "left",
    },
  ];

  ngOnInit(): void {
    this.updateDataSource();
  }

  ngOnChanges(): void {
    this.updateDataSource();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  /**
   * Update data source
   */
  private updateDataSource(): void {
    this.dataSource.data = this.models;
  }

  /**
   * Format metric as percentage display
   * Implementation requirements 1.3, 7.2: Consistently format percentage values
   */
  formatMetricAsPercentage(value: number | null): string {
    if (value === null || value === undefined) {
      return "--";
    }
    return `${Math.round(value)}%`;
  }

  /**
   * Format confidence threshold
   * Implementation requirements 1.4, 7.4: Display confidence threshold value accurate to two decimal places
   * Backend sends 0-100 range, convert to 0.00-1.00 for display
   */
  formatConfidenceThreshold(value: number): string {
    if (value === null || value === undefined) {
      return "N/A";
    }
    // Convert from 0-100 to 0.00-1.00
    return (value / 100).toFixed(2);
  }

  /**
   * Handle favorite toggle
   * Implementation requirements 3.1: When user clicks favorite star icon, toggle the model's Favorite_Status
   */
  onFavoriteToggle(modelId: number): void {
    this.favoriteToggle.emit(modelId);
  }

  /**
   * Handle action menu click
   * Implementation requirements 5.2: When user clicks kebab menu, display available model actions
   */
  onActionMenuClick(event: { modelId: number; action: string }): void {
    this.actionMenuClick.emit(event);
  }

  /**
   * Get table column style class
   */
  getColumnClass(column: TableColumn): string {
    return `column-${column.key} text-${column.align}`;
  }

  /**
   * Get table column style
   */
  getColumnStyle(column: TableColumn): any {
    return {
      width: column.width,
      "text-align": column.align,
    };
  }

  /**
   * Check if column is sortable
   */
  isColumnSortable(columnKey: string): boolean {
    const column = this.tableColumns.find((col) => col.key === columnKey);
    return column ? column.sortable : false;
  }

  /**
   * Get sort direction ARIA attribute value
   * Accessibility feature: Provide sort state information for screen readers
   */
  getSortDirection(columnKey: string): string {
    if (!this.sort || !this.isColumnSortable(columnKey)) {
      return "none";
    }

    if (this.sort.active === columnKey) {
      return this.sort.direction === "asc"
        ? "ascending"
        : this.sort.direction === "desc"
          ? "descending"
          : "none";
    }

    return "none";
  }

  /**
   * Handle model name click
   * Implementation requirements 10.1: When user clicks on model name, open Model Detail Panel with Training tab
   */
  onModelNameClick(modelId: number): void {
    this.rowClick.emit({ modelId, clickedColumn: "modelName" });
  }

  /**
   * Handle metric click
   * Implementation requirements 10.2: When user clicks on Train/Dev/Test metric, open Model Detail Panel with Performance tab
   */
  onMetricClick(modelId: number, metricType: string): void {
    this.rowClick.emit({ modelId, clickedColumn: metricType });
  }

  /**
   * Handle confidence threshold click
   * Implementation requirements 10.3: When user clicks on Confidence Threshold, open Model Detail Panel with Performance tab
   */
  onConfidenceThresholdClick(modelId: number): void {
    this.rowClick.emit({ modelId, clickedColumn: "confidenceThreshold" });
  }

  /**
   * Handle add evaluation set click
   */
  onAddEvaluationSet(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    // TODO: Implement add evaluation set functionality
    console.log("Add evaluation set clicked");
  }
}
