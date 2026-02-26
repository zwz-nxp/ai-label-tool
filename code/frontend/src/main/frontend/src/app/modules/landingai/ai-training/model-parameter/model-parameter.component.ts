import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";

// Angular Material Modules
import {
  MatTreeFlatDataSource,
  MatTreeFlattener,
  MatTreeModule,
} from "@angular/material/tree";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCardModule } from "@angular/material/card";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatMenuModule } from "@angular/material/menu";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { FlatTreeControl } from "@angular/cdk/tree";
import {
  CdkDragDrop,
  DragDropModule,
  moveItemInArray,
} from "@angular/cdk/drag-drop";

// RxJS
import { of, Subject } from "rxjs";
import { catchError, takeUntil } from "rxjs/operators";

// Services
import { ModelParamHttpService } from "app/services/landingai/model-param-http.service";

/**
 * Internal tree node representing a JSON key-value pair.
 * `object` and `array` types have `children`; leaf types store `value` directly.
 */
export interface JsonNode {
  key: string;
  value: any;
  type: "string" | "number" | "boolean" | "object" | "array";
  children?: JsonNode[];
  /** Marks nodes that were loaded from la_model_param — these cannot be deleted */
  isOriginal?: boolean;
}

/**
 * Flattened node used by FlatTreeControl for rendering the tree view.
 */
export interface FlatNode {
  expandable: boolean;
  key: string;
  value: any;
  type: string;
  level: number;
  /** Inherited from JsonNode — original nodes cannot be deleted */
  isOriginal?: boolean;
}

/**
 * ModelParameterComponent
 *
 * Standalone Angular component providing a visual JSON tree editor for model parameters.
 * Uses Angular Material MatTree with FlatTreeControl for hierarchical display,
 * CDK DragDrop for array reordering, and undo/redo history management.
 *
 * Validates: Requirements 1.1, 1.2, 1.4, 13.1, 13.2, 13.3, 13.4, 17.1, 17.2
 */
@Component({
  selector: "app-model-parameter",
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTreeModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    MatSelectModule,
    MatMenuModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatToolbarModule,
    MatProgressSpinnerModule,
    DragDropModule,
  ],
  templateUrl: "./model-parameter.component.html",
  styleUrls: ["./model-parameter.component.scss"],
})
export class ModelParameterComponent implements OnChanges, OnDestroy {
  // ── @Input / @Output ──────────────────────────────────────────────────
  @Input() modelSize: string = "";
  @Input() modelType: string = "";
  @Input() locationId: number = 0;
  @Output() parametersChange = new EventEmitter<string>();
  // ── Tree control (Task 2.2) ───────────────────────────────────────────
  private transformer = (node: JsonNode, level: number): FlatNode => ({
    expandable: !!node.children,
    key: node.key,
    value: node.value,
    type: node.type,
    level,
    isOriginal: node.isOriginal,
  });

  treeFlattener = new MatTreeFlattener(
    this.transformer,
    (node: FlatNode) => node.level,
    (node: FlatNode) => node.expandable,
    (node: JsonNode) => node.children
  );

  treeControl = new FlatTreeControl<FlatNode>(
    (node) => node.level,
    (node) => node.expandable
  );
  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  // ── Component state ───────────────────────────────────────────────────
  root: JsonNode = { key: "root", value: null, type: "object", children: [] };
  isLoading = false;
  historyStack: JsonNode[] = [];
  historyIndex = -1;
  nodeTypes: JsonNode["type"][] = [
    "string",
    "number",
    "boolean",
    "object",
    "array",
  ];
  /** Track last emitted JSON to avoid redundant emissions */
  private lastEmittedJson: string = "";
  // ── Subscription cleanup (Task 2.3) ───────────────────────────────────
  private destroy$ = new Subject<void>();

  // ── Injected services ─────────────────────────────────────────────────
  constructor(
    private modelParamService: ModelParamHttpService,
    private snackBar: MatSnackBar,
    private elementRef: ElementRef
  ) {}

  get jsonString(): string {
    return JSON.stringify(this.treeToJson(this.root), null, 2);
  }

  hasChild = (_: number, node: FlatNode) => node.expandable;

  ngOnChanges(changes: SimpleChanges): void {
    // Only reload when values actually changed, not just object references
    const sizeChange = changes["modelSize"];
    const typeChange = changes["modelType"];
    const locChange = changes["locationId"];
    const sizeActuallyChanged =
      sizeChange && sizeChange.previousValue !== sizeChange.currentValue;
    const typeActuallyChanged =
      typeChange && typeChange.previousValue !== typeChange.currentValue;
    const locActuallyChanged =
      locChange && locChange.previousValue !== locChange.currentValue;

    if (
      (sizeActuallyChanged || typeActuallyChanged || locActuallyChanged) &&
      this.locationId &&
      this.modelType
    ) {
      this.isLoading = true;
      this.modelParamService
        .getModelParamsByType(this.locationId, this.modelType)
        .pipe(
          takeUntil(this.destroy$),
          catchError((err) => {
            this.snackBar.open(err.message || "加载失败", "Close", {
              duration: 3000,
            });
            this.isLoading = false;
            return of([]);
          })
        )
        .subscribe((params) => {
          const matched = params.find((p) => p.modelName === this.modelSize);
          let json: any = {};
          if (matched) {
            try {
              json = JSON.parse(matched.parameters);
            } catch {
              json = {};
            }
          }
          this.root = this.jsonToTree(json);
          this.markAsOriginal(this.root);
          this.rebuildTree(this.root.children || []);
          this.historyStack = [];
          this.historyIndex = -1;
          this.saveHistory(this.root);
          this.isLoading = false;
          this.emitIfChanged();
        });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  jsonToTree(obj: any, key = "root"): JsonNode {
    const type = this.getRealType(obj);
    if (type === "object") {
      const children = Object.keys(obj).map((k) => this.jsonToTree(obj[k], k));
      return { key, value: null, type: "object", children };
    }
    if (type === "array") {
      const children = (obj as any[]).map((item, i) =>
        this.jsonToTree(item, String(i))
      );
      return { key, value: null, type: "array", children };
    }
    return { key, value: obj, type };
  }

  treeToJson(node: JsonNode): any {
    if (node.type === "object") {
      const result: any = {};
      (node.children || []).forEach((child) => {
        result[child.key] = this.treeToJson(child);
      });
      return result;
    }
    if (node.type === "array") {
      return (node.children || []).map((child) => this.treeToJson(child));
    }
    if (node.type === "number") return Number(node.value);
    if (node.type === "boolean")
      return node.value === true || node.value === "true";
    return node.value;
  }

  getRealType(v: any): JsonNode["type"] {
    if (v === null || v === undefined) return "string";
    if (Array.isArray(v)) return "array";
    if (typeof v === "object") return "object";
    if (typeof v === "number") return "number";
    if (typeof v === "boolean") return "boolean";
    return "string";
  }

  // ── Lifecycle ─────────────────────────────────────────────────────────

  findNode(root: JsonNode, target: FlatNode): JsonNode | null {
    if (
      root.key === target.key &&
      root.type === target.type &&
      root.value === target.value
    ) {
      return root;
    }
    for (const child of root.children || []) {
      const found = this.findNode(child, target);
      if (found) return found;
    }
    return null;
  }

  findParent(root: JsonNode, target: FlatNode): JsonNode | null {
    for (const child of root.children || []) {
      if (
        child.key === target.key &&
        child.type === target.type &&
        child.value === target.value
      ) {
        return root;
      }
      const found = this.findParent(child, target);
      if (found) return found;
    }
    return null;
  }

  addChildWithType(parentNode: FlatNode, type: JsonNode["type"]): void {
    const parent = this.findNode(this.root, parentNode);
    if (!parent || !parent.children) return;
    let newChild: JsonNode;
    if (type === "object") {
      newChild = { key: "newKey", value: null, type: "object", children: [] };
    } else if (type === "array") {
      newChild = { key: "newKey", value: null, type: "array", children: [] };
    } else {
      newChild = { key: "newKey", value: "", type };
    }
    parent.children.push(newChild);
    this.treeControl.expand(parentNode);
    this.refresh();

    // Focus the key input of the newly added node after DOM updates
    setTimeout(() => {
      const inputs = this.elementRef.nativeElement.querySelectorAll(
        ".tree-card .node-input"
      );
      if (inputs.length > 0) {
        const lastInput = inputs[inputs.length - 1] as HTMLInputElement;
        // Walk back to find the key input (first input in the last node)
        const allNodes =
          this.elementRef.nativeElement.querySelectorAll("mat-tree-node");
        if (allNodes.length > 0) {
          const lastNode = allNodes[allNodes.length - 1] as HTMLElement;
          const keyInput = lastNode.querySelector(
            ".node-input"
          ) as HTMLInputElement;
          if (keyInput) {
            keyInput.focus();
            keyInput.select();
          }
        }
      }
    });
  }

  // ── JSON ↔ Tree conversion stubs ──────────────────────────────────────

  updateKey(node: FlatNode, newKey: string): void {
    if (node.isOriginal) {
      this.snackBar.open("原始参数的Key不可修改", "Close", { duration: 3000 });
      this.refresh(); // refresh to revert the input field
      return;
    }
    const real = this.findNode(this.root, node);
    if (real) {
      real.key = newKey;
      this.refresh();
    }
  }

  updateValue(node: FlatNode, newVal: string): void {
    const real = this.findNode(this.root, node);
    if (real) {
      if (real.type === "number") {
        real.value = Number(newVal);
      } else if (real.type === "boolean") {
        real.value = newVal === "true";
      } else {
        real.value = newVal;
      }
      this.refresh();
    }
  }

  deleteNode(node: FlatNode): void {
    if (node.isOriginal) {
      this.snackBar.open("原始参数节点不可删除", "Close", { duration: 3000 });
      return;
    }
    const parent = this.findParent(this.root, node);
    if (parent && parent.children) {
      const idx = parent.children.findIndex(
        (c) =>
          c.key === node.key && c.type === node.type && c.value === node.value
      );
      if (idx >= 0) {
        parent.children.splice(idx, 1);
        this.refresh();
      }
    }
  }

  // ── Node lookup stubs ─────────────────────────────────────────────────

  drop(event: CdkDragDrop<JsonNode[]>, parentNode: FlatNode): void {
    const parent = this.findNode(this.root, parentNode);
    if (parent && parent.children && parent.type === "array") {
      moveItemInArray(parent.children, event.previousIndex, event.currentIndex);
      this.refresh();
    }
  }

  copyJson(): void {
    navigator.clipboard
      .writeText(this.jsonString)
      .then(() => {
        this.snackBar.open("已复制", "Close", { duration: 2000 });
      })
      .catch(() => {
        this.snackBar.open("复制失败", "Close", { duration: 2000 });
      });
  }

  downloadJson(): void {
    const blob = new Blob([this.jsonString], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "data.json";
    a.click();
    URL.revokeObjectURL(url);
  }

  // ── JSON string getter stub ───────────────────────────────────────────

  clearAll(): void {
    this.root = this.jsonToTree({});
    this.rebuildTree(this.root.children || []);
    this.saveHistory(this.root);
    this.emitIfChanged();
  }

  // ── CRUD operation stubs ──────────────────────────────────────────────

  saveHistory(root: JsonNode): void {
    const clone = JSON.parse(JSON.stringify(root)) as JsonNode;
    // Truncate any forward history when a new change is made
    this.historyStack = this.historyStack.slice(0, this.historyIndex + 1);
    this.historyStack.push(clone);
    this.historyIndex = this.historyStack.length - 1;
  }

  refresh(): void {
    this.rebuildTree(this.root.children || []);
    this.saveHistory(this.root);
    this.emitIfChanged();
  }

  undo(): void {
    if (this.historyIndex > 0) {
      this.historyIndex--;
      this.root = JSON.parse(
        JSON.stringify(this.historyStack[this.historyIndex])
      );
      this.rebuildTree(this.root.children || []);
      this.emitIfChanged();
    }
  }

  // ── Drag-drop stub ────────────────────────────────────────────────────

  redo(): void {
    if (this.historyIndex < this.historyStack.length - 1) {
      this.historyIndex++;
      this.root = JSON.parse(
        JSON.stringify(this.historyStack[this.historyIndex])
      );
      this.rebuildTree(this.root.children || []);
      this.emitIfChanged();
    }
  }

  // ── Import / Export stubs ─────────────────────────────────────────────

  /** Emit parametersChange only when the JSON string actually changed */
  private emitIfChanged(): void {
    const current = this.jsonString;
    if (current !== this.lastEmittedJson) {
      this.lastEmittedJson = current;
      this.parametersChange.emit(current);
    }
  }

  /** Recursively mark all nodes in the tree as original (loaded from la_model_param) */
  private markAsOriginal(node: JsonNode): void {
    node.isOriginal = true;
    if (node.children) {
      node.children.forEach((child) => this.markAsOriginal(child));
    }
  }

  // ── History management stubs ──────────────────────────────────────────

  /** Reassign dataSource.data while preserving expansion state */
  private rebuildTree(children: JsonNode[]): void {
    const expanded = new Set<string>();
    this.treeControl.dataNodes?.forEach((n) => {
      if (this.treeControl.isExpanded(n)) {
        expanded.add(this.nodeId(n));
      }
    });
    this.dataSource.data = children;
    this.treeControl.dataNodes?.forEach((n) => {
      if (expanded.has(this.nodeId(n))) {
        this.treeControl.expand(n);
      }
    });
  }

  private nodeId(n: FlatNode): string {
    return `${n.level}::${n.key}::${n.type}`;
  }
}
