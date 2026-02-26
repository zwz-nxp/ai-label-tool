# Image Labelling Component 重构总结

## 修改概述

将 `ImageLabellingComponent` 从接收 `@Input() project` 改为接收 `@Input() currentImage`，project 信息通过 `currentImage.projectId` 从数据库动态加载。

## 修改的文件

### 1. 新增文件

#### `code/frontend/src/main/frontend/src/app/services/landingai/project.service.ts`
- 新增 `ProjectService` 用于管理 project 数据
- 提供 CRUD 操作：
  - `getProjectById(projectId: number): Observable<Project>`
  - `getAllProjects(): Observable<Project[]>`
  - `createProject(project: Partial<Project>): Observable<Project>`
  - `updateProject(projectId: number, project: Partial<Project>): Observable<Project>`
  - `deleteProject(projectId: number): Observable<void>`

### 2. 修改的核心文件

#### `code/frontend/src/main/frontend/src/app/modules/landingai/labelling/image-labelling/image-labelling.component.ts`

**主要变更：**

1. **输入属性变更**
   ```typescript
   // 之前
   @Input() project!: Project;
   
   // 现在
   @Input() currentImage!: Image;
   project: Project | null = null;
   ```

2. **ComponentState 简化**
   - 移除了 `currentImage` 字段（现在是 @Input 属性）
   
3. **新增 loadProject 方法**
   ```typescript
   private loadProject(projectId: number): void {
     this.projectService.getProjectById(projectId)
       .subscribe({
         next: (project) => {
           this.project = project;
           // 初始化视图模式、工具栏
           // 加载图片列表和类别
           // 加载当前图片的标注
         }
       });
   }
   ```

4. **修改所有 `state.currentImage` 引用为 `currentImage`**
   - `selectImage()` 方法
   - `onImageUpdated()` 方法
   - `onImageDeleted()` 方法
   - `generatePreAnnotations()` 方法
   - `handleClassificationClassChange()` 方法
   - `clearAllAnnotations()` 方法
   - `annotationToImageLabel()` 方法
   - `saveAnnotationToDatabase()` 方法
   - `loadAnnotationsForCurrentImage()` 方法

5. **修改 loadImages 方法**
   - 移除了自动选择第一张图片的逻辑
   - 添加了 project 存在性检查

#### `code/frontend/src/main/frontend/src/app/modules/landingai/labelling/image-labelling/image-labelling.component.html`

**变更：**
- 所有 `state.currentImage` 改为 `currentImage`
- `[projectId]="project.id"` 改为 `[projectId]="project?.id || 0"`

#### `code/frontend/src/main/frontend/src/app/modules/landingai/labelling/labelling.module.ts`

**路由变更：**
```typescript
// 之前
{ path: "label/:projectId", component: ImageLabellingComponent }

// 现在
{ path: "label/:imageId", component: ImageLabellingComponent }
```

### 3. 修改的子组件

#### `code/frontend/src/main/frontend/src/app/modules/landingai/labelling/left-toolbar/left-toolbar.component.ts`

**变更：**
```typescript
// 之前
@Input() project!: Project;

// 现在
@Input() project: Project | null = null;
```

#### `code/frontend/src/main/frontend/src/app/modules/landingai/labelling/top-toolbar/top-toolbar.component.ts`

**变更：**
```typescript
// 之前
@Input() projectId!: number;

// 现在
@Input() projectId: number = 0;
```

并在 `openClassCreationDialog()` 中添加了 projectId 有效性检查。

### 4. 服务注册

#### `code/frontend/src/main/frontend/src/app/services/landingai/image.service.ts`

**新增方法：**
```typescript
public getImageById(imageId: number): Observable<Image>
```

#### `code/frontend/src/main/frontend/src/app/app.module.ts`

**变更：**
- 导入 `ProjectService`
- 在 providers 中注册 `ProjectService`

## 数据流变化

### 之前的数据流
```
父组件 → [project] → ImageLabellingComponent
                   ↓
              加载 images
              加载 classes
              加载 annotations
```

### 现在的数据流
```
父组件 → [currentImage] → ImageLabellingComponent
                        ↓
                   通过 currentImage.projectId
                        ↓
                   加载 project (ProjectService)
                        ↓
                   初始化视图模式和工具栏
                        ↓
                   加载 images (ImageService)
                   加载 classes (ProjectClassService)
                   加载 annotations (LabelService)
```

## 优势

1. **单一数据源**: 只需传入 `currentImage`，组件自动加载相关的 project 信息
2. **数据一致性**: 确保 project 信息始终与 currentImage 关联
3. **解耦**: 父组件不需要同时管理 project 和 image
4. **灵活性**: 可以轻松切换不同图片，组件会自动处理 project 加载

## 后端 API 要求

需要确保以下 API 端点可用：

1. `GET /api/landingai/projects/{projectId}` - 获取单个 project
2. `GET /api/landingai/images/{imageId}` - 获取单个 image
3. `GET /api/landingai/images/project/{projectId}` - 获取 project 的所有 images
4. `GET /api/landingai/classes/project/{projectId}` - 获取 project 的所有 classes
5. `GET /api/landingai/labels/image/{imageId}` - 获取 image 的所有 labels

## 使用示例

```typescript
// 父组件
export class ParentComponent {
  currentImage: Image;
  
  ngOnInit() {
    const imageId = 123;
    this.imageService.getImageById(imageId).subscribe(image => {
      this.currentImage = image;
    });
  }
}
```

```html
<!-- 父组件模板 -->
<app-image-labelling [currentImage]="currentImage"></app-image-labelling>
```

## 注意事项

1. 确保 `currentImage` 包含有效的 `projectId`
2. Project 加载是异步的，在 project 加载完成前，某些功能可能不可用
3. 所有子组件都已更新以处理 `project` 可能为 `null` 的情况
4. 路由已更新为使用 `imageId` 而不是 `projectId`

## 测试建议

1. 测试 project 加载失败的情况
2. 测试 currentImage 为 null 的情况
3. 测试切换不同 project 的图片
4. 测试所有标注功能在 project 加载后是否正常工作
5. 测试批量操作（导出、删除）是否正常
