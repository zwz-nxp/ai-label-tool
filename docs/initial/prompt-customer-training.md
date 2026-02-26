# Customer Training 组件开发提示词

## 角色定义

你是一个资深的 Java Spring Boot 微服务架构和 Angular 前端开发专家，精通：
- Angular 18+ 框架开发
- Angular Material UI 组件库
- RxJS 响应式编程
- NgRx 状态管理
- Java Spring Boot 微服务架构
- RESTful API 设计

## 项目背景

本项目是一个 AI 图像训练平台，需要开发一个新的 Customer Training 组件，用于配置和启动 AI 模型训练任务。

## 需求概述

### 1. 组件基本信息

- **组件名称**: `customer-training`
- **组件路径**: `code/frontend/src/main/frontend/src/app/modules/landingai/ai-training`
- **UI 框架**: Angular Material
- **样式风格**: Material Design

### 2. 页面结构 (Page 1 - Set up your data)

#### 2.1 Set up your data 区域

- **下拉框**: 数据版本选择
  - 默认值: `Current version`
  - 选项来源: 从后端获取的 snapshot 列表

#### 2.2 Assign split 按钮

- **按钮文本**: `Assign split to {count} images`
  - `{count}` = 未分配 split 的图片数量
- **Tooltip 提示** (鼠标悬停时显示):
  ```
  Automatically assign images to train, dev, and test splits based on your target distribution.
  Images that already have a split assigned will not be changed.
  ```

#### 2.3 Set your target split distribution 区域

- **下拉选项**: 两个值
  - `All Classes`
  - `Per Class` (可选择具体的 class)
- **滑块控制**: Train / Dev / Test 比例分配
  - 默认比例: 70% / 15% / 15%

#### 2.4 Preview your split 区域

- **标题**: `Preview your split ({count} images)`
  - `{count}` = 所有图片总数
- **切换按钮组**: 
  - `By class` - 显示每种 class 的 train/dev/test 占比色带
  - `By Split` - 显示每种 split 中各 class 的占比色带
- **色带图例**: 不同颜色代表不同的 class

### 3. 页面结构 (Page 2 - Configure your model)

点击 `Next` 按钮后显示此区域。

#### 3.1 Hyperparameters 配置

##### 3.1.1 Epoch 配置
- **输入方式**: 数字输入框 + 滑块
- **默认值**: `40`
- **范围**: 1-100
- **Tooltip 提示**:
  ```
  An epoch is one complete pass through the entire training dataset.
  More epochs can improve model accuracy but may lead to overfitting.
  Recommended: 20-50 for most use cases.
  ```

##### 3.1.2 Model size 配置
- **输入方式**: 下拉选择
- **默认值**: `RepPoints-[37M]`
- **选项列表** (前端固定值):
  - `RepPoints-[37M]`
  - `RepPoints-[50M]`
  - `RepPoints-[101M]`
- **Tooltip 提示**:
  ```
  Model size affects training time and inference speed.
  Larger models may achieve better accuracy but require more resources.
  ```

#### 3.2 Transforms 配置

##### 3.2.1 Rescale with padding
- **默认值**: 空
- **Tooltip 提示**:
  ```
  Rescale images to a fixed size while maintaining aspect ratio.
  Padding is added to fill the remaining space.
  ```
- **添加选项** (点击加号):
  - `Resize` → `Manual resize`
  - `Crop`

##### 3.2.2 Manual resize 弹窗
- **字段**:
  - Width: 数字输入
  - Height: 数字输入
  - Keep aspect ratio: 复选框

##### 3.2.3 Crop 弹窗
- **字段**:
  - X offset: 数字输入
  - Y offset: 数字输入
  - Width: 数字输入
  - Height: 数字输入

##### 3.2.4 Image Size 显示区域
- 显示当前图片尺寸
- 鼠标悬停时右上角显示 `Edit` 按钮
- 点击 Edit 弹出 `Rescale with padding` 配置弹窗

#### 3.3 Augmentations 配置

##### 3.3.1 Horizontal Flip
- 鼠标悬停显示 Edit 按钮
- 点击 Edit 弹出配置弹窗:
  - Probability: 滑块 (0-1)

##### 3.3.2 Random Augment
- 鼠标悬停显示 Edit 按钮
- 点击 Edit 弹出配置弹窗:
  - Number of transforms: 数字输入
  - Magnitude: 滑块

##### 3.3.3 Add Augmentations (点击加号)
可添加的增强选项:
- **Random Brightness**: 
  - Limit: 滑块 (-1 to 1)
- **Blur**:
  - Blur limit: 滑块
- **Motion Blur**:
  - Blur limit: 滑块
- **Gaussian Blur**:
  - Blur limit: 滑块
  - Sigma: 滑块
- **Hue Saturation Value**:
  - Hue shift limit: 滑块
  - Saturation shift limit: 滑块
  - Value shift limit: 滑块
- **Random Contrast**:
  - Limit: 滑块
- **Vertical Flip**:
  - Probability: 滑块
- **Random Rotate**:
  - Limit: 滑块 (角度)
  - Border mode: 下拉选择

### 4. 按钮操作

- **Back**: 返回上一步
- **Next**: 进入下一步配置
- **Start Training**: 开始训练 (最后一步)
- **Cancel**: 取消并返回

---

## 前端文件清单

### 组件文件

```
code/frontend/src/main/frontend/src/app/modules/landingai/ai-training/
├── customer-training/
│   ├── customer-training.component.ts
│   ├── customer-training.component.html
│   ├── customer-training.component.scss
│   └── customer-training.component.spec.ts
├── split-preview/
│   ├── split-preview.component.ts
│   ├── split-preview.component.html
│   ├── split-preview.component.scss
│   └── split-preview.component.spec.ts
├── split-distribution/
│   ├── split-distribution.component.ts
│   ├── split-distribution.component.html
│   ├── split-distribution.component.scss
│   └── split-distribution.component.spec.ts
├── hyperparameters-config/
│   ├── hyperparameters-config.component.ts
│   ├── hyperparameters-config.component.html
│   ├── hyperparameters-config.component.scss
│   └── hyperparameters-config.component.spec.ts
├── transforms-config/
│   ├── transforms-config.component.ts
│   ├── transforms-config.component.html
│   ├── transforms-config.component.scss
│   └── transforms-config.component.spec.ts
├── augmentations-config/
│   ├── augmentations-config.component.ts
│   ├── augmentations-config.component.html
│   ├── augmentations-config.component.scss
│   └── augmentations-config.component.spec.ts
├── dialogs/
│   ├── crop-dialog/
│   │   ├── crop-dialog.component.ts
│   │   ├── crop-dialog.component.html
│   │   └── crop-dialog.component.scss
│   ├── manual-resize-dialog/
│   │   ├── manual-resize-dialog.component.ts
│   │   ├── manual-resize-dialog.component.html
│   │   └── manual-resize-dialog.component.scss
│   ├── rescale-padding-dialog/
│   │   ├── rescale-padding-dialog.component.ts
│   │   ├── rescale-padding-dialog.component.html
│   │   └── rescale-padding-dialog.component.scss
│   ├── horizontal-flip-dialog/
│   │   ├── horizontal-flip-dialog.component.ts
│   │   ├── horizontal-flip-dialog.component.html
│   │   └── horizontal-flip-dialog.component.scss
│   ├── random-augment-dialog/
│   │   ├── random-augment-dialog.component.ts
│   │   ├── random-augment-dialog.component.html
│   │   └── random-augment-dialog.component.scss
│   ├── random-brightness-dialog/
│   │   ├── random-brightness-dialog.component.ts
│   │   ├── random-brightness-dialog.component.html
│   │   └── random-brightness-dialog.component.scss
│   ├── blur-dialog/
│   │   ├── blur-dialog.component.ts
│   │   ├── blur-dialog.component.html
│   │   └── blur-dialog.component.scss
│   ├── motion-blur-dialog/
│   │   ├── motion-blur-dialog.component.ts
│   │   ├── motion-blur-dialog.component.html
│   │   └── motion-blur-dialog.component.scss
│   ├── gaussian-blur-dialog/
│   │   ├── gaussian-blur-dialog.component.ts
│   │   ├── gaussian-blur-dialog.component.html
│   │   └── gaussian-blur-dialog.component.scss
│   ├── hue-saturation-dialog/
│   │   ├── hue-saturation-dialog.component.ts
│   │   ├── hue-saturation-dialog.component.html
│   │   └── hue-saturation-dialog.component.scss
│   ├── random-contrast-dialog/
│   │   ├── random-contrast-dialog.component.ts
│   │   ├── random-contrast-dialog.component.html
│   │   └── random-contrast-dialog.component.scss
│   ├── vertical-flip-dialog/
│   │   ├── vertical-flip-dialog.component.ts
│   │   ├── vertical-flip-dialog.component.html
│   │   └── vertical-flip-dialog.component.scss
│   └── random-rotate-dialog/
│       ├── random-rotate-dialog.component.ts
│       ├── random-rotate-dialog.component.html
│       └── random-rotate-dialog.component.scss
└── ai-training.module.ts
```

### Model 文件

```
code/frontend/src/main/frontend/src/app/models/landingai/
├── training-config.model.ts
├── split-distribution.model.ts
├── hyperparameters.model.ts
├── transform-config.model.ts
└── augmentation-config.model.ts
```

### Service 文件

```
code/frontend/src/main/frontend/src/app/services/landingai/
├── training.service.ts
└── split.service.ts
```

### State 文件 (NgRx)

```
code/frontend/src/main/frontend/src/app/state/landingai/training/
├── training.actions.ts
├── training.effects.ts
├── training.reducer.ts
├── training.selectors.ts
└── training.state.ts
```

---

## 后端文件清单 (仅 TrainingController 相关)

### Controller 文件

```
code/backend/api/src/main/java/com/nxp/iemdm/controller/landingai/
└── TrainingController.java (已存在，需扩展)
```

### Service 文件

```
code/backend/api/src/main/java/com/nxp/iemdm/service/
└── TrainingService.java (已存在，需扩展)

code/backend/api/src/main/java/com/nxp/iemdm/service/rest/landingai/
└── TrainingServiceImpl.java (已存在，需扩展)
```

### DTO 文件

```
code/backend/shared/src/main/java/com/nxp/iemdm/shared/dto/landingai/
├── TrainingRequest.java (已存在，需扩展)
├── TrainingRecordDTO.java (已存在)
├── TrainingStatusDTO.java (已存在)
├── TransformConfigDTO.java (新建)
└── AugmentationConfigDTO.java (新建)
```

### Entity 文件

```
code/backend/shared/src/main/java/com/nxp/iemdm/shared/entity/landingai/
└── TrainingRecord.java (已存在，字段已包含 epochs, modelSize, transformParam, augmentationParam)
```

### Repository 文件

```
code/backend/shared/src/main/java/com/nxp/iemdm/shared/repository/landingai/
└── TrainingRecordRepository.java (已存在)
```

---

## API 接口设计 (仅 Training 相关)

### POST /api/landingai/training/start
启动训练 (已存在，需扩展)

**Request:**
```json
{
  "projectId": 1,
  "snapshotId": null,
  "filePath": "/path/to/training/data",
  "fileName": "training_data.zip",
  "modelConfigs": [
    {
      "modelAlias": "Model 1",
      "epochs": 40,
      "modelSize": "RepPoints-[37M]",
      "transforms": {
        "rescaleWithPadding": {
          "enabled": true,
          "width": 640,
          "height": 640
        },
        "crop": null,
        "manualResize": null
      },
      "augmentations": {
        "horizontalFlip": { "probability": 0.5 },
        "randomAugment": { "numTransforms": 2, "magnitude": 9 },
        "randomBrightness": null,
        "blur": null,
        "motionBlur": null,
        "gaussianBlur": null,
        "hueSaturationValue": null,
        "randomContrast": null,
        "verticalFlip": null,
        "randomRotate": null
      }
    }
  ]
}
```

**说明**: 当 `modelConfigs` 包含多个配置时，后端会为每个配置创建一条独立的 `la_training_record` 记录。

### GET /api/landingai/training/{id}/status
获取训练状态 (已存在)

---

## Backend 需要修改和增加的方法清单

### TrainingController.java (扩展)

| 方法名 | HTTP 方法 | 路径 | 说明 | 状态 |
|--------|----------|------|------|------|
| `startTraining` | POST | `/api/landingai/training/start` | 启动训练，支持多模型配置，每个配置创建独立的 training record | 修改 |
| `getTrainingStatus` | GET | `/api/landingai/training/{id}/status` | 获取训练状态 | 已存在 |

### TrainingService.java / TrainingServiceImpl.java (扩展)

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `startTraining(TrainingRequest request)` | 启动训练，遍历 modelConfigs 为每个配置创建 training record | 修改 |
| `getTrainingStatus(Long trainingId)` | 获取训练状态 | 已存在 |
| `createTrainingRecord(TrainingRequest request, ModelConfigDTO config)` | 创建单条训练记录 | 新增 |

### TrainingRequest.java (扩展)

| 字段名 | 类型 | 说明 | 状态 |
|--------|------|------|------|
| `projectId` | Long | 项目ID | 已存在 |
| `snapshotId` | Long | 快照ID (可选) | 已存在 |
| `filePath` | String | 文件路径 | 新增 |
| `fileName` | String | 文件名称 | 新增 |
| `modelConfigs` | List<ModelConfigDTO> | 模型配置列表，每个配置会创建独立的 training record | 新增 |

### ModelConfigDTO.java (新建)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `modelAlias` | String | 模型别名 |
| `epochs` | Integer | Epoch 值 |
| `modelSize` | String | 模型尺寸 |
| `transforms` | TransformConfigDTO | 变换配置 |
| `augmentations` | AugmentationConfigDTO | 增强配置 |

### TrainingRecord.java (字段映射)

| 字段名 | 类型 | 说明 | 状态 |
|--------|------|------|------|
| `epochs` | Integer | Epoch 值 | 已存在 |
| `modelSize` | String | 模型尺寸 | 已存在 |
| `transformParam` | String | Transforms 配置 (JSON 格式) | 已存在 |
| `augmentationParam` | String | Augmentations 配置 (JSON 格式) | 已存在 |
| `filePath` | String | 文件路径 | 已存在 |
| `fileName` | String | 文件名称 | 已存在 |

---

## Frontend 需要修改和增加的方法清单

### training.service.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `startTraining(request: TrainingRequest): Observable<TrainingResponse>` | 启动训练 | 修改 |
| `getTrainingStatus(trainingId: number): Observable<TrainingStatus>` | 获取训练状态 | 已存在 |

### Model Size 常量 (前端固定值)

```typescript
// training-config.model.ts
export const MODEL_SIZES = [
  { value: 'RepPoints-[37M]', label: 'RepPoints-[37M]' },
  { value: 'RepPoints-[50M]', label: 'RepPoints-[50M]' },
  { value: 'RepPoints-[101M]', label: 'RepPoints-[101M]' }
];
```

### split.service.ts (新建)

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `getSplitPreview(projectId: number): Observable<SplitPreview>` | 获取 split 预览数据 | 新增 |
| `assignSplit(projectId: number, distribution: SplitDistribution): Observable<void>` | 自动分配 split | 新增 |
| `getProjectClasses(projectId: number): Observable<ProjectClass[]>` | 获取项目的所有 class | 新增 |

### customer-training.component.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `ngOnInit()` | 初始化组件，加载数据 | 新增 |
| `loadSnapshots()` | 加载快照列表 | 新增 |
| `loadSplitPreview()` | 加载 split 预览 | 新增 |
| `onSnapshotChange(snapshotId: number)` | 快照选择变更 | 新增 |
| `onAssignSplit()` | 点击分配 split 按钮 | 新增 |
| `onNext()` | 进入下一步 | 新增 |
| `onBack()` | 返回上一步 | 新增 |
| `onStartTraining()` | 开始训练 | 新增 |
| `onCancel()` | 取消操作 | 新增 |
| `addModel()` | 添加新模型配置 | 新增 |
| `removeModel(index: number)` | 移除模型配置 | 新增 |
| `buildTrainingRequest(): TrainingRequest` | 构建训练请求对象 | 新增 |

### split-preview.component.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `ngOnInit()` | 初始化组件 | 新增 |
| `ngOnChanges()` | 输入变更时更新视图 | 新增 |
| `toggleView(view: 'byClass' \| 'bySplit')` | 切换视图模式 | 新增 |
| `calculateBarSegments()` | 计算色带分段 | 新增 |
| `getClassColor(className: string): string` | 获取 class 对应颜色 | 新增 |

### split-distribution.component.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `ngOnInit()` | 初始化组件 | 新增 |
| `onScopeChange(scope: 'all' \| 'perClass')` | 切换分配范围 | 新增 |
| `onClassSelect(classId: number)` | 选择具体 class | 新增 |
| `onDistributionChange(type: string, value: number)` | 分配比例变更 | 新增 |
| `validateDistribution(): boolean` | 验证分配比例总和为100 | 新增 |

### hyperparameters-config.component.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `ngOnInit()` | 初始化组件，加载默认值 | 新增 |
| `onEpochChange(value: number)` | Epoch 值变更 | 新增 |
| `onModelSizeChange(modelSize: string)` | 模型尺寸变更 | 新增 |
| `getModelSizes()` | 获取前端固定的模型尺寸列表 | 新增 |

### transforms-config.component.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `ngOnInit()` | 初始化组件 | 新增 |
| `addTransform(type: string)` | 添加变换配置 | 新增 |
| `removeTransform(type: string)` | 移除变换配置 | 新增 |
| `openRescaleDialog()` | 打开 Rescale 配置弹窗 | 新增 |
| `openCropDialog()` | 打开 Crop 配置弹窗 | 新增 |
| `openManualResizeDialog()` | 打开 Manual Resize 配置弹窗 | 新增 |
| `onTransformConfigChange(type: string, config: any)` | 变换配置变更 | 新增 |

### augmentations-config.component.ts

| 方法名 | 说明 | 状态 |
|--------|------|------|
| `ngOnInit()` | 初始化组件 | 新增 |
| `addAugmentation(type: string)` | 添加增强配置 | 新增 |
| `removeAugmentation(type: string)` | 移除增强配置 | 新增 |
| `openHorizontalFlipDialog()` | 打开 Horizontal Flip 配置弹窗 | 新增 |
| `openRandomAugmentDialog()` | 打开 Random Augment 配置弹窗 | 新增 |
| `openRandomBrightnessDialog()` | 打开 Random Brightness 配置弹窗 | 新增 |
| `openBlurDialog()` | 打开 Blur 配置弹窗 | 新增 |
| `openMotionBlurDialog()` | 打开 Motion Blur 配置弹窗 | 新增 |
| `openGaussianBlurDialog()` | 打开 Gaussian Blur 配置弹窗 | 新增 |
| `openHueSaturationDialog()` | 打开 Hue Saturation 配置弹窗 | 新增 |
| `openRandomContrastDialog()` | 打开 Random Contrast 配置弹窗 | 新增 |
| `openVerticalFlipDialog()` | 打开 Vertical Flip 配置弹窗 | 新增 |
| `openRandomRotateDialog()` | 打开 Random Rotate 配置弹窗 | 新增 |
| `onAugmentationConfigChange(type: string, config: any)` | 增强配置变更 | 新增 |

### NgRx State - training.actions.ts

| Action 名称 | 说明 | 状态 |
|-------------|------|------|
| `loadSnapshots` | 加载快照列表 | 新增 |
| `loadSnapshotsSuccess` | 加载快照成功 | 新增 |
| `loadSnapshotsFailure` | 加载快照失败 | 新增 |
| `loadSplitPreview` | 加载 split 预览 | 新增 |
| `loadSplitPreviewSuccess` | 加载 split 预览成功 | 新增 |
| `loadSplitPreviewFailure` | 加载 split 预览失败 | 新增 |
| `assignSplit` | 分配 split | 新增 |
| `assignSplitSuccess` | 分配 split 成功 | 新增 |
| `assignSplitFailure` | 分配 split 失败 | 新增 |
| `updateDistribution` | 更新分配比例 | 新增 |
| `addModelConfig` | 添加模型配置 | 新增 |
| `removeModelConfig` | 移除模型配置 | 新增 |
| `updateModelConfig` | 更新模型配置 | 新增 |
| `startTraining` | 开始训练 | 新增 |
| `startTrainingSuccess` | 开始训练成功 | 新增 |
| `startTrainingFailure` | 开始训练失败 | 新增 |
| `setCurrentStep` | 设置当前步骤 | 新增 |

### NgRx State - training.selectors.ts

| Selector 名称 | 说明 | 状态 |
|---------------|------|------|
| `selectSnapshots` | 获取快照列表 | 新增 |
| `selectSplitPreview` | 获取 split 预览数据 | 新增 |
| `selectDistribution` | 获取分配比例 | 新增 |
| `selectModelConfigs` | 获取模型配置列表 | 新增 |
| `selectCurrentStep` | 获取当前步骤 | 新增 |
| `selectIsLoading` | 获取加载状态 | 新增 |
| `selectError` | 获取错误信息 | 新增 |
| `selectUnassignedCount` | 获取未分配图片数量 | 新增 |
| `selectTotalImages` | 获取总图片数量 | 新增 |

---

## Material 组件使用规范

### 必须导入的 Material 模块

```typescript
import { MatButtonModule } from "@angular/material/button";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatMenuModule } from "@angular/material/menu";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatSelectModule } from "@angular/material/select";
import { MatSliderModule } from "@angular/material/slider";
import { MatStepperModule } from "@angular/material/stepper";
import { MatTooltipModule } from "@angular/material/tooltip";
```

### 按钮风格统一

```html
<!-- Primary 按钮 -->
<button mat-raised-button color="primary">Next</button>

<!-- Secondary 按钮 -->
<button mat-stroked-button>Back</button>

<!-- Icon 按钮 -->
<button mat-icon-button matTooltip="Edit">
  <mat-icon>edit</mat-icon>
</button>

<!-- FAB 按钮 (添加) -->
<button mat-mini-fab color="primary" matTooltip="Add">
  <mat-icon>add</mat-icon>
</button>
```

### Tooltip 使用规范

```html
<span matTooltip="Tooltip text here" matTooltipPosition="above">
  Hover me
</span>
```

### Dialog 使用规范

```typescript
// 打开 dialog
const dialogRef = this.dialog.open(CropDialogComponent, {
  width: '400px',
  data: { currentConfig: this.cropConfig }
});

dialogRef.afterClosed().subscribe(result => {
  if (result) {
    this.cropConfig = result;
  }
});
```

---

## 样式规范

### 色带组件样式

```scss
.split-bar {
  display: flex;
  height: 24px;
  border-radius: 4px;
  overflow: hidden;
  
  .train-segment {
    background-color: #4caf50;
  }
  
  .dev-segment {
    background-color: #2196f3;
  }
  
  .test-segment {
    background-color: #ff9800;
  }
}

.class-bar {
  display: flex;
  height: 24px;
  border-radius: 4px;
  overflow: hidden;
  
  .class-segment {
    // 动态颜色，根据 class 索引分配
  }
}
```

### 配置卡片样式

```scss
.config-card {
  margin-bottom: 16px;
  
  .config-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.12);
  }
  
  .config-content {
    padding: 16px;
  }
  
  .config-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    
    &:hover .edit-button {
      opacity: 1;
    }
    
    .edit-button {
      opacity: 0;
      transition: opacity 0.2s;
    }
  }
}
```

---

## 开发注意事项

1. **响应式设计**: 组件需要适配不同屏幕尺寸
2. **表单验证**: 所有输入需要进行验证，显示错误提示
3. **加载状态**: API 调用时显示 loading 状态
4. **错误处理**: 统一的错误处理和用户提示
5. **国际化**: 预留 i18n 支持
6. **无障碍**: 确保组件符合 WCAG 2.1 标准
7. **单元测试**: 每个组件需要编写单元测试
8. **多记录创建**: 当有多个 model config 时，后端需要为每个配置创建独立的 training record

---

## 路由配置

需要在 `landingai-routing.module.ts` 中添加:

```typescript
{
  path: "ai-training",
  loadChildren: () =>
    import("./ai-training/ai-training.module").then((m) => m.AiTrainingModule),
}
```

在 `ai-training.module.ts` 中配置子路由:

```typescript
const routes: Routes = [
  {
    path: ":projectId",
    component: CustomerTrainingComponent,
  },
];
```
