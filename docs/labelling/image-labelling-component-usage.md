# Image Labelling Component Usage

## 概述

`ImageLabellingComponent` 已经修改为接收 `currentImage` 作为输入，而不是 `project`。组件会自动通过 `currentImage.projectId` 从数据库加载 project 信息。

## 修改内容

### 1. 输入属性变更

**之前:**
```typescript
@Input() project!: Project;
```

**现在:**
```typescript
@Input() currentImage!: Image;
```

### 2. Project 加载

Project 信息现在通过 `currentImage.projectId` 从数据库动态加载：

```typescript
private loadProject(projectId: number): void {
  this.projectService
    .getProjectById(projectId)
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (project) => {
        this.project = project;
        // 初始化视图模式、工具栏按钮等
        // 加载图片列表和类别
        // 加载当前图片的标注
      },
      error: (error) => {
        console.error("Error loading project:", error);
      },
    });
}
```

### 3. 组件状态简化

`ComponentState` 接口已移除 `currentImage` 字段，因为它现在是组件的输入属性：

```typescript
interface ComponentState {
  annotations: Annotation[];
  selectedAnnotation: Annotation | null;
  selectedClass: ProjectClass | null;
  enhanceSettings: EnhanceSettings;
  annotationHistory: Annotation[][];
  historyIndex: number;
}
```

## 使用方式

### 在父组件中使用

```typescript
// 父组件 TypeScript
export class ParentComponent {
  currentImage: Image;
  
  ngOnInit() {
    // 从某处获取当前图片
    this.imageService.getImageById(imageId).subscribe(image => {
      this.currentImage = image;
    });
  }
}
```

```html
<!-- 父组件模板 -->
<app-image-labelling 
  [currentImage]="currentImage">
</app-image-labelling>
```

### 路由配置

路由已更新为使用 `imageId`：

```typescript
const routes: Routes = [
  { path: "label/:imageId", component: ImageLabellingComponent },
];
```

## 新增服务

### ProjectService

新增了 `ProjectService` 用于管理 project 数据：

```typescript
@Injectable({
  providedIn: "root",
})
export class ProjectService {
  public getProjectById(projectId: number): Observable<Project>
  public getAllProjects(): Observable<Project[]>
  public createProject(project: Partial<Project>): Observable<Project>
  public updateProject(projectId: number, project: Partial<Project>): Observable<Project>
  public deleteProject(projectId: number): Observable<void>
}
```

## 数据流

1. 父组件传入 `currentImage`
2. `ImageLabellingComponent` 在 `ngOnInit` 中检查 `currentImage`
3. 通过 `currentImage.projectId` 调用 `ProjectService.getProjectById()`
4. 加载 project 后，初始化视图模式和工具栏
5. 加载该 project 的所有图片和类别
6. 加载当前图片的标注

## 优势

1. **解耦**: 组件不再依赖外部传入的 project，而是根据 currentImage 自动加载
2. **灵活性**: 可以轻松切换不同的图片，组件会自动加载对应的 project 信息
3. **数据一致性**: 确保 project 信息始终与 currentImage 关联的 project 一致
4. **简化使用**: 父组件只需要提供 currentImage，不需要同时管理 project 和 image

## 注意事项

1. 确保传入的 `currentImage` 包含有效的 `projectId`
2. 后端 API 需要支持 `GET /api/landingai/projects/{projectId}` 端点
3. 组件会在 project 加载完成后才开始加载图片列表和标注
4. 如果 project 加载失败，会显示错误提示
