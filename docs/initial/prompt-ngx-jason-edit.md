# Prompt: 在 AI Training 模块中新增 Model Parameter JSON 树编辑器组件

## 角色设定

你是一位资深的 Java Spring Boot 微服务架构和 Angular 前端开发专家。你精通 Angular Material、NgRx 状态管理、RxJS 响应式编程，以及 Spring Boot RESTful API 设计。你对本项目的代码结构、组件模式和状态管理模式非常熟悉。

## 任务目标

在 `code/frontend/src/main/frontend/src/app/modules/landingai/ai-training/` 目录下新增一个名为 `model-parameter` 的 Angular 组件。该组件用于在训练配置页面（Step 2）中，显示在 "Data Augmentations" 区域的下方，提供一个基于 `MatTreeModule` + `CdkDragDrop` 的完整 JSON 树编辑器，用于输入和编辑模型参数 JSON 字符串。

## 核心需求

### 1. 组件位置与布局
- 组件位于 `ai-training/model-parameter/` 目录下
- 在 `customer-training.component.html` 的 Step 2 中，紧跟在 `<app-augmentations-config>` 所在的 `augmentations-section` div 之后渲染
- 组件选择器命名为 `app-model-parameter`

### 2. 数据来源
- 默认数据从 `la_model_param` 表读取（通过已有的 `ModelParamHttpService` 服务）
- 读取条件为：`location`（当前用户所在 location）+ `model_name`（对应 hyperparameters 中 "Model Size" 下拉框所选的值）+ `model_type`（当前项目的模型类型）
- `model_name` 的值与 `HyperparametersConfigComponent` 中 `modelSize` 选择的值联动（即 `RepPoints-[37M]`、`RepPoints-[50M]`、`RepPoints-[101M]` 之一）

### 3. JSON 树编辑器功能（完整实现，参照参考文件）

采用 `MatTreeModule`（`FlatTreeControl` + `MatTreeFlatDataSource` + `MatTreeFlattener`）和 `@angular/cdk/drag-drop`（`CdkDragDrop` + `moveItemInArray`）实现完整的 JSON 树编辑器。功能清单如下：

#### 3.1 数据结构定义
```typescript
/** JSON 树节点 */
interface JsonNode {
  key: string;
  value: any;
  type: 'string' | 'number' | 'boolean' | 'object' | 'array';
  children?: JsonNode[];
}

/** 扁平化节点（用于 FlatTreeControl） */
interface FlatNode {
  expandable: boolean;
  key: string;
  value: any;
  type: string;
  level: number;
}
```

#### 3.2 JSON ↔ Tree 双向转换
- `jsonToTree(obj: any, key = 'root'): JsonNode` — 将任意 JSON 对象递归转换为 JsonNode 树
- `treeToJson(node: JsonNode): any` — 将 JsonNode 树递归还原为 JSON 对象
- `getRealType(v: any): JsonNode['type']` — 自动检测值类型（string/number/boolean/object/array）
- `get jsonString(): string` — 实时计算属性，返回格式化的 JSON 字符串（`JSON.stringify(..., null, 2)`）

#### 3.3 树节点展开/收缩
- 使用 `FlatTreeControl` 管理展开状态
- object/array 类型节点可展开/收缩，显示 `expand_more` / `chevron_right` 图标
- 叶子节点（string/number/boolean）不可展开

#### 3.4 增删改 key-value
- **添加子节点** `addChild(parentNode: FlatNode)`：在 object/array 节点下添加新的 key-value 对
  - 提供 key 输入框、value 输入框、类型选择下拉框（string/number/boolean/object/array）
  - 添加后自动展开父节点
- **修改 key** `updateKey(node: FlatNode, newKey: string)`：直接在树节点上编辑 key（input 绑定 + blur 事件）
- **修改 value** `updateValue(node: FlatNode, newVal: string)`：直接在树节点上编辑 value（input 绑定 + blur 事件）
- **删除节点** `deleteNode(node: FlatNode)`：删除指定节点（从父节点的 children 中移除）

#### 3.5 数组拖拽排序
- 使用 `cdkDropList` + `cdkDragHandle` 实现数组子元素的拖拽排序
- `drop(event: CdkDragDrop<JsonNode[]>, parentNode: FlatNode)` — 调用 `moveItemInArray` 重排数组元素

#### 3.6 导入 JSON
- 提供一个 textarea 区域用于粘贴 JSON 文本
- `importJson()` — 解析粘贴的 JSON 文本，转换为树结构并加载
- 解析失败时通过 `MatSnackBar` 提示 "JSON格式错误"

#### 3.7 复制 JSON
- `copyJson()` — 将当前树结构转换为格式化 JSON 字符串，复制到剪贴板（`navigator.clipboard.writeText`）
- 复制成功后 `MatSnackBar` 提示 "已复制"

#### 3.8 下载 JSON 文件
- `downloadJson()` — 将当前 JSON 字符串生成 Blob，触发浏览器下载为 `data.json` 文件

#### 3.9 清空
- `clearAll()` — 将树重置为空对象 `{}`

#### 3.10 撤销/重做
- 维护 `historyStack: JsonNode[]` 和 `historyIndex: number`
- `saveHistory(root: JsonNode)` — 每次修改后深拷贝当前树状态入栈
- `refresh()` — 刷新 dataSource 并保存历史
- `undo()` — 回退到上一个历史状态
- `redo()` — 前进到下一个历史状态

#### 3.11 实时 JSON 预览
- 在树编辑器下方显示实时格式化的 JSON 预览（`<pre>{{ jsonString }}</pre>`）
- 每次树结构变化时自动更新

### 4. 与父组件的交互
- `@Input() modelSize: string` — 接收当前选中的 model size
- `@Input() modelType: string` — 接收当前项目的 model type（Object Detection / Classification / Segmentation）
- `@Input() locationId: number` — 接收当前 location ID
- `@Output() parametersChange = new EventEmitter<string>()` — 当 JSON 参数变更时（每次 `refresh()`/`importJson()`/`clearAll()`/`undo()`/`redo()` 后）通知父组件，发射 `jsonString`

### 5. 数据加载逻辑
- 当 `modelSize`、`modelType` 或 `locationId` 任一输入变化时（通过 `OnChanges`），调用 `ModelParamHttpService` 查询匹配的 `la_model_param` 记录
- 查询条件：`locationId` + `modelName`（= modelSize 的值）+ `modelType`
- 如果查到记录，将其 `parameters` 字段（JSON 字符串）解析后调用 `jsonToTree()` 加载到树编辑器
- 如果未查到记录，加载空对象 `{}` 到树编辑器
- 加载过程中显示 loading 状态

### 6. 数据持久化 — 存储到 TrainingRecord.augmentation_param

编辑后的 JSON 字符串最终需要存储到数据库 `la_training_record` 表的 `augmentation_param` 字段。完整数据流如下：

#### 6.1 数据流全链路
```
[model-parameter 组件]  ──@Output() parametersChange──>
[customer-training 父组件]  ──dispatch updateModelConfig──>
[NgRx Store: ModelConfig.augmentationParam]  ──buildTrainingRequest──>
[TrainingRequest.modelConfigs[i].augmentationParams]  ──HTTP POST /api/landingai/training/start──>
[后端 ModelConfigDTO.augmentations / TrainingRequest.augmentationParams]  ──TrainingService──>
[TrainingRecord.setAugmentationParam(jsonString)]  ──JPA save──>
[la_training_record.augmentation_param 列]
```

#### 6.2 前端修改：ModelConfig 接口扩展

在 `code/frontend/src/main/frontend/src/app/models/landingai/training-config.model.ts` 中，`ModelConfig` 接口需要新增 `augmentationParam` 字段，用于存储树编辑器输出的原始 JSON 字符串：

```typescript
export interface ModelConfig {
  modelAlias: string;
  status?: string;
  epochs: number;
  modelSize: string;
  transforms: TransformConfig;
  augmentations: AugmentationConfig;
  augmentationParam?: string;  // ← 新增：来自 model-parameter 树编辑器的 JSON 字符串
}
```

#### 6.3 前端修改：NgRx State 与 Reducer

在 `training.reducer.ts` 中，`updateModelConfig` action 已支持 partial config 更新，新增的 `augmentationParam` 字段会自动通过 spread operator 合并，无需额外修改 reducer 逻辑。

#### 6.4 前端修改：customer-training 父组件

`onModelParametersChange` 方法将树编辑器输出的 JSON 字符串存入 `augmentationParam`：

```typescript
onModelParametersChange(params: string, modelIndex: number): void {
  this.store.dispatch(
    updateModelConfig({
      index: modelIndex,
      config: { augmentationParam: params },
    })
  );
}
```

`buildTrainingRequest` 方法需要将 `augmentationParam` 传递到请求体中：

```typescript
private buildTrainingRequest(...): TrainingRequest | null {
  return {
    projectId,
    snapshotId: snapshotId ?? undefined,
    modelConfigs: modelConfigs.map((config, index) => ({
      ...config,
      modelAlias: this.getModelAlias(index),
      status: 'PENDING',
      epochs: config.epochs,
      modelSize: config.modelSize,
      transforms: config.transforms || {},
      augmentations: config.augmentations || {},
      // ★ 将树编辑器的 JSON 字符串作为 augmentationParams 传递
      augmentationParams: config.augmentationParam
        ? JSON.parse(config.augmentationParam)
        : null,
    })),
  };
}
```

#### 6.5 后端现有逻辑（无需修改）

后端 `TrainingService` 已有完整的处理链路，无需修改：

1. `TrainingRequest.java` 已有 `augmentationParams` 字段（`Map<String, Object>`）
2. `ModelConfigDTO.java` 已有 `augmentations` 字段（`AugmentationConfigDTO`）
3. `TrainingService.createTrainingRecord()` 已实现序列化逻辑：
   ```java
   // 已有代码 — 处理 legacy 单模型模式
   if (request.getAugmentationParams() != null && !request.getAugmentationParams().isEmpty()) {
     trainingRecord.setAugmentationParam(
       objectMapper.writeValueAsString(request.getAugmentationParams()));
   }

   // 已有代码 — 处理多模型模式
   if (modelConfig.getAugmentations() != null) {
     String augmentationJson = serializeAugmentationConfig(modelConfig.getAugmentations());
     trainingRecord.setAugmentationParam(augmentationJson);
   }
   ```
4. `TrainingRecord.java` 实体已有 `augmentationParam` 字段：
   ```java
   @Column(name = "augmentation_param", length = 500)
   private String augmentationParam; // JSON
   ```

#### 6.6 数据库字段

`la_training_record.augmentation_param` 列（`VARCHAR(500)`）存储最终的 JSON 字符串。注意：如果树编辑器产生的 JSON 较大，可能需要将列类型从 `VARCHAR(500)` 扩展为 `TEXT`。

#### 6.7 注意：augmentationParam 与 augmentations 的关系

当前系统中 `augmentations`（来自 AugmentationsConfigComponent 的结构化增强配置）和新增的 `augmentationParam`（来自 model-parameter 树编辑器的自由 JSON）最终都写入同一个 `TrainingRecord.augmentation_param` 字段。需要明确以下策略（二选一）：

- **策略 A：覆盖模式** — 如果用户在树编辑器中编辑了 JSON，则以树编辑器的值为准，覆盖 AugmentationsConfigComponent 的结构化配置。在 `buildTrainingRequest` 中优先使用 `augmentationParam`。
- **策略 B：合并模式** — 将 AugmentationsConfigComponent 的结构化配置与树编辑器的 JSON 合并后存储。在 `buildTrainingRequest` 中将两者 merge。

建议采用**策略 A（覆盖模式）**，逻辑更清晰：
```typescript
// buildTrainingRequest 中的优先级逻辑
augmentationParams: config.augmentationParam
  ? JSON.parse(config.augmentationParam)           // 树编辑器有值时优先使用
  : (config.augmentations || {}),                   // 否则使用结构化配置
```

## 项目上下文

### 目录结构
```
ai-training/
├── ai-training.module.ts
├── augmentations-config/          # 数据增强配置（本组件显示在其下方）
├── customer-training/             # 父容器组件（Step 1 & Step 2 向导）
├── hyperparameters-config/        # 超参数配置（包含 Model Size 下拉框）
├── model-parameter/               # ← 新增组件目录
│   ├── model-parameter.component.ts
│   ├── model-parameter.component.html
│   └── model-parameter.component.scss
├── split-distribution/
├── split-preview/
└── transforms-config/
```

### 关键依赖与模式

1. **Standalone Component 模式**：本项目所有新组件均使用 Angular standalone component（`standalone: true`），不在 NgModule 的 declarations 中注册，而是在父组件的 `imports` 数组中直接导入。参考 `AugmentationsConfigComponent` 的写法。

2. **Angular Material 模块**（在 standalone component 的 imports 中按需引入）：
   - `MatTreeModule`（`@angular/material/tree`）— 树组件
   - `MatButtonModule`（`@angular/material/button`）— 按钮
   - `MatIconModule`（`@angular/material/icon`）— 图标
   - `MatCardModule`（`@angular/material/card`）— 卡片容器
   - `MatInputModule`（`@angular/material/input`）— 输入框
   - `MatSelectModule`（`@angular/material/select`）— 下拉选择
   - `MatTooltipModule`（`@angular/material/tooltip`）— 工具提示
   - `MatSnackBarModule`（`@angular/material/snack-bar`）— 消息提示
   - `MatToolbarModule`（`@angular/material/toolbar`）— 工具栏
   - `DragDropModule`（`@angular/cdk/drag-drop`）— 拖拽排序

3. **已有服务**：
   - `ModelParamHttpService`（路径：`app/services/landingai/model-param-http.service.ts`）
     - `getModelParams(locationId: number): Observable<ModelParam[]>` — 获取指定 location 的所有模型参数
     - `getModelParamsByType(locationId: number, modelType: string): Observable<ModelParam[]>` — 按类型过滤
   - `ModelParam` 接口（路径：`app/models/landingai/model-param.model.ts`）：
     ```typescript
     interface ModelParam {
       id: number;
       locationId: number;
       locationName?: string;
       modelName: string;        // 对应 model size 的值，如 "RepPoints-[37M]"
       modelType: ModelType;     // "Object Detection" | "Classification" | "Segmentation"
       parameters: string;       // JSON 字符串，即要加载到树编辑器的默认值
       createdAt: Date;
       createdBy: string;
     }
     ```

4. **Location 状态**：通过 NgRx store 获取当前 location：
   ```typescript
   import * as LocationSelectors from 'app/state/location/location.selectors';
   this.store.select(LocationSelectors.selectCurrentLocation)
   ```

5. **Model Size 选项**（来自 `training-config.model.ts`）：
   ```typescript
   export const MODEL_SIZES = [
     { value: 'RepPoints-[37M]', label: 'RepPoints-[37M]' },
     { value: 'RepPoints-[50M]', label: 'RepPoints-[50M]' },
     { value: 'RepPoints-[101M]', label: 'RepPoints-[101M]' },
   ] as const;
   ```

6. **父组件 customer-training.component.html Step 2 结构**（需要修改的位置）：
   ```html
   <!-- Step 2: Model Configuration -->
   <div *ngIf="state.step === 2" class="step-content">
     <ng-container *ngIf="modelConfigs$ | async as modelConfigs">
       <div *ngFor="let modelConfig of modelConfigs; let i = index" class="model-config-card">
         <!-- ... Model Header ... -->
         <!-- Hyperparameters Section -->
         <div class="hyperparameters-section">
           <app-hyperparameters-config
             (configChange)="onHyperparametersChange($event, i)"
             [config]="{ epochs: modelConfig.epochs, modelSize: modelConfig.modelSize }">
           </app-hyperparameters-config>
         </div>
         <!-- Transforms Section -->
         <div class="transforms-section">
           <app-transforms-config ...></app-transforms-config>
         </div>
         <!-- Augmentations Section -->
         <div class="augmentations-section">
           <app-augmentations-config
             (configChange)="onAugmentationsChange($event, i)"
             [config]="modelConfig.augmentations">
           </app-augmentations-config>
         </div>
         <!-- ★ 在此处新增 Model Parameter Section ★ -->
       </div>
     </ng-container>
   </div>
   ```

## 参考实现（完整代码，来自 doubaoJasonEdit_0218.txt）

以下是参考文件中的完整实现，需要将其适配为本项目的 standalone component 模式，并集成 `ModelParamHttpService` 数据加载逻辑。

### 参考 TypeScript（核心逻辑）
```typescript
import { Component } from '@angular/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

interface JsonNode {
  key: string;
  value: any;
  type: 'string' | 'number' | 'boolean' | 'object' | 'array';
  children?: JsonNode[];
}

interface FlatNode {
  expandable: boolean;
  key: string;
  value: any;
  type: string;
  level: number;
}

// transformer: 将 JsonNode 转为 FlatNode
private transformer = (node: JsonNode, level: number) => ({
  expandable: !!node.children && node.children.length > 0,
  key: node.key,
  value: node.value,
  type: node.type,
  level: level
});

// FlatTreeControl + MatTreeFlattener + MatTreeFlatDataSource
treeControl = new FlatTreeControl<FlatNode>(
  node => node.level,
  node => node.expandable
);
treeFlattener = new MatTreeFlattener(
  this.transformer,
  node => node.level,
  node => node.expandable,
  node => node.children
);
dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);

// 历史记录
historyStack: JsonNode[] = [];
historyIndex = -1;

// JSON ↔ Tree 转换
jsonToTree(obj: any, key = 'root'): JsonNode { /* 递归转换 */ }
treeToJson(node: JsonNode): any { /* 递归还原 */ }
getRealType(v: any): JsonNode['type'] { /* 类型检测 */ }
get jsonString(): string { /* JSON.stringify(this.treeToJson(...), null, 2) */ }

// 查找
findNode(root: JsonNode, target: FlatNode): JsonNode | null { /* 递归查找 */ }
findParent(root: JsonNode, target: FlatNode): JsonNode | null { /* 递归查找父节点 */ }

// 增删改
addChild(parentNode: FlatNode) { /* 添加子节点 */ }
updateKey(node: FlatNode, newKey: string) { /* 修改 key */ }
updateValue(node: FlatNode, newVal: string) { /* 修改 value */ }
deleteNode(node: FlatNode) { /* 删除节点 */ }

// 数组拖拽排序
drop(event: CdkDragDrop<JsonNode[]>, parentNode: FlatNode) {
  moveItemInArray(parent.children, event.previousIndex, event.currentIndex);
}

// 导入/导出/复制/清空
importJson() { /* JSON.parse + jsonToTree */ }
copyJson() { /* navigator.clipboard.writeText */ }
downloadJson() { /* Blob + download */ }
clearAll() { /* 重置为 {} */ }

// 撤销/重做
saveHistory(root: JsonNode) { /* 深拷贝入栈 */ }
refresh() { /* 刷新 dataSource + saveHistory */ }
undo() { /* historyIndex-- */ }
redo() { /* historyIndex++ */ }

hasChild = (_: number, node: FlatNode) => node.expandable;
```

### 参考 HTML（树编辑器模板）
```html
<!-- 工具栏 -->
<mat-toolbar color="primary" class="toolbar">
  <span>Model Parameters</span>
  <div class="actions">
    <button mat-raised-button (click)="undo()" matTooltip="Undo">↩ Undo</button>
    <button mat-raised-button (click)="redo()" matTooltip="Redo">↪ Redo</button>
    <button mat-raised-button color="warn" (click)="clearAll()">Clear</button>
  </div>
</mat-toolbar>

<!-- 导入 JSON -->
<mat-card class="box">
  <h3>Import JSON</h3>
  <textarea rows="6" [(ngModel)]="importJsonText" class="full-width" placeholder="Paste JSON here"></textarea>
  <button mat-raised-button color="primary" (click)="importJson()" class="mt-2">Load</button>
</mat-card>

<!-- JSON 树编辑 -->
<mat-card class="box tree-box">
  <h3>JSON Tree Editor</h3>
  <mat-tree [dataSource]="dataSource" [treeControl]="treeControl">
    <!-- 叶子节点 -->
    <mat-tree-node *matTreeNodeDef="let node" matTreeNodePadding>
      <button mat-icon-button disabled></button>
      <input [(ngModel)]="node.key" (blur)="updateKey(node, node.key)" class="key-input">
      <span>:</span>
      <input [(ngModel)]="node.value" (blur)="updateValue(node, node.value)" class="val-input">
      <button mat-icon-button color="warn" (click)="deleteNode(node)" matTooltip="Delete">
        <mat-icon>delete</mat-icon>
      </button>
    </mat-tree-node>

    <!-- 对象/数组节点 -->
    <mat-tree-node *matTreeNodeDef="let node; when: hasChild" matTreeNodePadding
      cdkDropList [cdkDropListData]="findNode(dataSource.data[0], node)?.children"
      (cdkDropListDropped)="drop($event, node)">
      <button mat-icon-button matTreeNodeToggle>
        <mat-icon>{{ treeControl.isExpanded(node) ? 'expand_more' : 'chevron_right' }}</mat-icon>
      </button>
      <input [(ngModel)]="node.key" (blur)="updateKey(node, node.key)" class="key-input">
      <span>: {{ node.type }}</span>
      <div class="add-row" cdkDragHandle>
        <input [(ngModel)]="newKey" placeholder="key" class="mini-input">
        <input [(ngModel)]="newValue" placeholder="value" class="mini-input">
        <mat-select [(value)]="newType" class="mini-select">
          <mat-option value="string">string</mat-option>
          <mat-option value="number">number</mat-option>
          <mat-option value="boolean">bool</mat-option>
          <mat-option value="object">object</mat-option>
          <mat-option value="array">array</mat-option>
        </mat-select>
        <button mat-raised-button color="accent" (click)="addChild(node)">Add</button>
      </div>
    </mat-tree-node>
  </mat-tree>
</mat-card>

<!-- 实时 JSON 预览 -->
<mat-card class="box">
  <div class="json-header">
    <h3>Live JSON Preview</h3>
    <div>
      <button mat-raised-button color="accent" (click)="copyJson()" class="mr-2">Copy</button>
      <button mat-raised-button color="primary" (click)="downloadJson()">Download</button>
    </div>
  </div>
  <pre class="json-preview">{{ jsonString }}</pre>
</mat-card>
```

### 参考 SCSS
```scss
.toolbar {
  display: flex;
  justify-content: space-between;
  padding: 0 16px;
}
.actions { display: flex; gap: 8px; }
.box { margin-bottom: 20px; padding: 16px; }
.full-width { width: 100%; box-sizing: border-box; padding: 8px; font-family: monospace; }
.mt-2 { margin-top: 8px; }
.json-header { display: flex; justify-content: space-between; align-items: center; }
.json-preview { background: #f5f5f5; padding: 12px; border-radius: 4px; white-space: pre-wrap; }
.key-input { width: 120px; margin-right: 4px; font-weight: 500; }
.val-input { width: 180px; margin: 0 8px; }
.add-row { display: flex; gap: 6px; align-items: center; margin-left: 12px; }
.mini-input { width: 90px; padding: 4px; }
.mini-select { width: 100px; }
.tree-box { overflow: auto; }
mat-tree-node { min-height: 44px !important; }
.mr-2 { margin-right: 8px; }
```

## 适配要求（参考代码 → 本项目）

将上述参考代码适配到本项目时，需要做以下调整：

### 1. Standalone Component 改造
参考代码使用 NgModule 模式，需改为 standalone component：
```typescript
@Component({
  selector: 'app-model-parameter',
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
    MatTooltipModule,
    MatSnackBarModule,
    MatToolbarModule,
    DragDropModule,
  ],
  templateUrl: './model-parameter.component.html',
  styleUrls: ['./model-parameter.component.scss'],
})
```

### 2. 添加 @Input/@Output 与数据加载
- 替换参考代码中的 `initialJson` 硬编码数据，改为通过 `@Input()` 接收 `modelSize`、`modelType`、`locationId`
- 在 `ngOnChanges` 中监听输入变化，调用 `ModelParamHttpService.getModelParamsByType(locationId, modelType)` 获取数据
- 从返回的 `ModelParam[]` 中找到 `modelName === modelSize` 的记录，取其 `parameters` 字段
- 将 `parameters` JSON 字符串解析后调用 `jsonToTree()` 加载到树编辑器
- 每次树结构变化时（`refresh()`/`importJson()`/`clearAll()`/`undo()`/`redo()`），通过 `@Output() parametersChange` 发射 `jsonString`

### 3. 订阅清理
- 添加 `private destroy$ = new Subject<void>()`
- 在 `ngOnDestroy` 中 `this.destroy$.next(); this.destroy$.complete()`
- HTTP 请求使用 `takeUntil(this.destroy$)`

### 4. Loading 状态
- 添加 `isLoading = false` 标志
- HTTP 请求前设为 `true`，完成后设为 `false`
- 模板中加载时显示 `<mat-spinner>`

### 5. 错误处理
- HTTP 请求失败时通过 `MatSnackBar` 显示错误信息
- JSON 解析失败时显示 "JSON格式错误" 提示

## 父组件修改

### customer-training.component.ts
```typescript
// 1. 新增 import
import { ModelParameterComponent } from '../model-parameter/model-parameter.component';

// 2. 在 @Component imports 数组中添加：
imports: [
  // ... 现有 imports ...
  ModelParameterComponent,
],

// 3. 注入 Location 状态（如尚未注入）
// 在 constructor 或 ngOnInit 中订阅 currentLocation：
currentLocationId: number | null = null;

// ngOnInit 中：
this.store.select(LocationSelectors.selectCurrentLocation)
  .pipe(takeUntil(this.destroy$))
  .subscribe(location => {
    this.currentLocationId = location?.id ?? null;
  });

// 4. 添加处理方法 — 将树编辑器 JSON 存入 augmentationParam
/**
 * Handle model parameters change from the JSON tree editor.
 * Stores the edited JSON string into ModelConfig.augmentationParam,
 * which will ultimately be persisted to TrainingRecord.augmentation_param.
 */
onModelParametersChange(params: string, modelIndex: number): void {
  this.store.dispatch(
    updateModelConfig({
      index: modelIndex,
      config: { augmentationParam: params },
    })
  );
}

// 5. 修改 buildTrainingRequest — augmentationParam 优先级高于 augmentations
private buildTrainingRequest(
  projectId: number | null,
  snapshotId: number | null,
  modelConfigs: ModelConfig[]
): TrainingRequest | null {
  // ... 现有校验逻辑 ...
  return {
    projectId,
    snapshotId: snapshotId ?? undefined,
    modelConfigs: modelConfigs.map((config, index) => ({
      ...config,
      modelAlias: this.getModelAlias(index),
      status: 'PENDING',
      epochs: config.epochs,
      modelSize: config.modelSize,
      transforms: config.transforms || {},
      // ★ 树编辑器的 augmentationParam 优先，否则使用结构化 augmentations
      augmentations: config.augmentationParam
        ? JSON.parse(config.augmentationParam)
        : (config.augmentations || {}),
    })),
  };
}
```

### customer-training.component.html
在 `augmentations-section` 之后添加：
```html
<!-- Model Parameter Section — JSON 树编辑器 -->
<!-- 编辑后的 JSON 存储到 TrainingRecord.augmentation_param -->
<div class="model-parameter-section">
  <app-model-parameter
    [modelSize]="modelConfig.modelSize"
    [modelType]="'Object Detection'"
    [locationId]="currentLocationId"
    (parametersChange)="onModelParametersChange($event, i)">
  </app-model-parameter>
</div>
```

## 完整功能清单

- ✅ 展开/折叠 JSON 树（MatTreeModule + FlatTreeControl）
- ✅ 增、删、改 key & value（inline input 编辑）
- ✅ 支持 string/number/boolean/object/array 五种类型
- ✅ 数组拖拽排序（CdkDragDrop + moveItemInArray）
- ✅ 撤销/重做（historyStack + historyIndex）
- ✅ 导入 JSON（textarea + JSON.parse）
- ✅ 复制 JSON（navigator.clipboard.writeText）
- ✅ 下载 JSON 文件（Blob + download）
- ✅ 一键清空（重置为 `{}`）
- ✅ 实时格式化 JSON 预览（`<pre>{{ jsonString }}</pre>`）
- ✅ 从 la_model_param 表按 location + model_name + model_type 加载默认值
- ✅ 与父组件双向数据流（@Input 接收条件，@Output 发射 JSON 字符串）
- ✅ 编辑后的 JSON 存储到 TrainingRecord.augmentation_param（经由 NgRx → TrainingRequest → 后端 TrainingService → JPA）
- ✅ augmentationParam（树编辑器）优先级高于 augmentations（结构化配置）
- ✅ 完整 Angular Material 界面
- ✅ Standalone Component 模式

## 注意事项

1. 组件必须是 `standalone: true`
2. 使用 `OnChanges` 生命周期钩子监听 `@Input()` 变化并重新加载数据
3. HTTP 请求需要处理错误情况，使用 `MatSnackBar` 显示错误信息
4. 所有按钮使用 `MatTooltip` 提供操作提示
5. 加载数据时显示 loading 状态（`MatProgressSpinnerModule`）
6. 组件需要在 `ngOnDestroy` 中清理订阅（使用 `Subject` + `takeUntil` 模式）
7. 遵循项目现有的代码风格：JSDoc 注释、`data-testid` 属性、Requirement 引用注释
8. 树编辑器区域需要 `overflow: auto` 以支持大型 JSON 结构的滚动
9. 每次树结构变化后都要通过 `@Output()` 通知父组件最新的 JSON 字符串
10. `ModelConfig` 接口需新增 `augmentationParam?: string` 字段
11. `buildTrainingRequest` 中 `augmentationParam`（树编辑器 JSON）优先于 `augmentations`（结构化配置）
12. 后端 `TrainingRecord.augmentation_param` 列当前为 `VARCHAR(500)`，如果 JSON 较大需扩展为 `TEXT`
13. 后端 `TrainingService` 已有 `augmentationParam` 的序列化和存储逻辑，无需修改后端代码
