# 图像标注系统开发需求文档

## 项目概述

实现一个专业的图像标注系统，用于目标检测和实例分割任务。

### 技术栈
- **Backend**: Java 21, Spring Boot
- **Frontend**: Angular 19, Angular Material, ngx-image-annotation
- **Database**: PostgreSQL
- **图片存储**: 本地文件系统 `\image\2026`

### 重要说明
**本项目使用已存在的Entity和数据库表结构，无需重新创建。**

所有Entity类位于：`code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/`

---

## 核心功能需求

### 0. Project Type 支持
系统支持三种项目类型，每种类型有不同的标注工具和界面：

#### Object Detection
- **说明**: Label with bounding boxes. Use to identify one or more objects in an image.
- **标注工具**: Bounding Box（矩形框）
- **输出**: 目标的位置和类别
- **应用场景**: 识别图片中的一个或多个对象

#### Segmentation
- **说明**: Label with precision tools. Use when pixel-level precision is required. Output from the model is a mask of the pixels.
- **标注工具**: Smart Labeling, Brush, Polygon, Polyline
- **输出**: 像素级别的mask
- **应用场景**: 需要精确到像素级别的分割任务

#### Classification
- **说明**: Each image itself is a label. Use to classify the contents of an image as a whole to distinguish.
- **标注工具**: Classes下拉选项（无需绘制工具）
- **输出**: 图片的分类标签（无坐标信息）
- **应用场景**: 对整张图片进行分类
- **特性**: 
  - 选择class时自动创建label
  - label的position字段为空或null
  - 一张图片只能有一个classification label

### 1. 多形状标注支持（Object Detection & Segmentation）
- 支持矩形（Rectangle）、多边形（Polygon）、椭圆（Ellipse）等多种标注形状
- 适配目标检测（Object Detection）和实例分割（Instance Segmentation）任务
- 标注数据包含坐标、标签、形状类型等信息

### 2. 标注管理功能
- **增加**: 点击"新增标注"按钮，在图片上绘制标注框
- **删除**: 
  - 单个删除：选中标注后点击删除按钮或按 Delete 键
  - 批量删除：点击扫帚图标删除当前图片所有标注
- **编辑**: 
  - 拖拽标注框调整位置
  - 拖拽边框调整大小
  - 双击标注框修改标签
  - 多边形顶点可拖拽微调
- **缩放与拖拽**: 
  - 图片支持 Zoom In/Out
  - 标注框自动跟随图片缩放
  - Pan 模式下可拖拽图片

### 3. 数据持久化

#### 3.1 标注数据存储
- **表名**: `la_images_label`
- **自动保存**: 标注变更时自动保存到数据库
- **自动导入**: 切换图片时自动加载该图片的标注数据
- **格式校验**: 导入时进行 JSON 格式校验，损坏文件会提示错误

#### 3.2 类别（Class）存储
- **表名**: `la_project_class`
- **功能**: 存储项目的类别定义
- **特性**: 不同类别在标注时使用不同颜色的边框

### 4. 多边形轮廓标注操作流程
1. 在下拉框选择「多边形（轮廓分割）」
2. 选择标签类别（Class）
3. 点击「新增标注」按钮
4. 在图片上依次点击描绘轮廓（每点击一次添加一个顶点）
5. 双击鼠标自动闭合形成完整轮廓
6. 拖拽顶点可微调位置
7. 右键或按 Delete 键删除整个轮廓

### 5. 双标注格式切换
- **矩形框**: 用于目标检测（Object Detection），边框颜色为红色
- **多边形**: 用于实例分割（Instance Segmentation），边框颜色为青色
- 可在两种标注模式间自由切换

### 6. 便捷操作功能
- **新增标注按钮**: 快速添加新标注
- **删除选中按钮**: 删除选中的标注
- **双击修改标签**: 双击标注框可修改标签名称
- **Undo/Redo**: 支持撤销和重做操作
- **清空标注**: 扫帚图标一键清空当前图片所有标注

### 7. 图片管理功能
- **图片存储**: 所有图片存储在服务器端 `\image\2026` 文件夹
- **图片切换**: 点击左侧图片列表切换图片
- **自动清空**: 切换图片时自动清空旧标注，避免混乱
- **批量操作**: 
  - 批量导出选中图片和标注
  - 批量删除选中图片和标注
- **Checkbox 选择**: 每个图片名称前有 checkbox，用于批量操作

### 8. 预标注功能
- **模型调用**: 支持调用本地模型或云端模型
- **自动标注**: 根据模型返回的坐标文档，在图片上自动显示预标注框
- **人工校正**: 用户可在预标注基础上进行调整和修正

### 9. 类别（Class）管理
- **存储位置**: `la_project_class` 表
- **颜色定义**: 每个类别在 `color_code` 字段中定义颜色（十六进制格式，如 "#FF0000"）
- **颜色应用**: 
  - 选择类别后，新创建的标注框自动使用该类别的颜色
  - 前端根据标注的颜色值绘制边框
- **下拉选择**: 在 Classes 下拉框中选择类别
- **标注关联**: 标注时自动关联选中的类别和颜色
- **创建Class**: 在Classes下拉框中点击加号（+），弹出创建界面
- **Class设置界面**:
  - Class Name: 输入类别名称
  - Color: 选择类别颜色
  - Description: 可选的类别描述
- **参考图片**: 
  - `docs/labelling/chooseClass.png` - 选择Class界面
  - `docs/labelling/createClass.png` - 创建Class界面

### 10. 图片增强功能
- **Brightness调节**: 通过滑块调节图片亮度（-100 到 +100）
- **Contrast调节**: 通过滑块调节图片对比度（-100 到 +100）
- **实时预览**: 调节时实时显示效果
- **重置功能**: 一键恢复原始显示效果
- **注意**: 仅影响显示，不修改原始图片文件

---

## GUI 界面设计

### 左侧工具栏

#### 上半部分：Project 详情块
- **Project Status 显示**:
  - 显示当前状态和下一个状态
  - 状态流程: Upload → Label → Train → Predict
  - 参考图片: `docs\labelling\projectStatus.png`

#### 下半部分：Image Name List
- **图片列表**:
  - 显示所有图片名称
  - 点击图片名称切换图片
  - 每个图片名称前有 checkbox，用于批量操作
- **批量操作按钮**:
  - 批量导出图片和标注
  - 批量删除图片和标注
  - 导出和下载选中的图片

### 右侧工具栏

#### 1. General 块（图片基本信息）
- **图片名称**: 显示在最上方
- **操作按钮**:
  - 导出当前图片和标注
  - 删除当前图片和所有标注
- **Split 选项**:
  - 默认值: Unassigned
  - 可选值: Train, Dev, Test
  - Tooltip 提示: "Landing automatically splits your images during Model Training. If you prefer, you can manually split images, and your manual splits will be respected in the next training session"
- **No Object to Label Checkbox**:
  - 如果图片上没有任何可标注对象，勾选此框
  - 勾选后图片状态标记为已标注

#### 2. Tags 块
- **功能**: 为图片添加多个标签（Tag）
- **Add Tag 按钮**: 点击弹出 Tag 添加组件
- **操作流程**:
  1. 点击 Add Tag 按钮
  2. 输入 Tag 名称
  3. 保存 Tag
  4. Tag 显示在 Tags 块下方
- **参考图片**: 
  - `docs\labelling\tags.png`
  - `docs\labelling\tags1.png`

#### 3. Metadata 块
- **功能**: 选择已创建的 Metadata 并输入数值
- **显示**: 数值显示在 Metadata 块中
- **参考图片**:
  - `docs\labelling\metadata1.png`
  - `docs\labelling\metadata2.png`
  - `docs\labelling\metadata3.png`
  - `docs\labelling\metadata4.png`

#### 4. Labels 块（标注列表）
- **显示方式**: 按 Class 名称分组显示标注信息
- **标注格式**: 显示标注的坐标信息
- **折叠功能**: Class 下的 Label 信息可折叠
- **展开操作**: 点击 Class 名称展开显示 Label 列表
- **选中功能**: 点击任意 Label 自动选中图片预览区中相应的标注框
- **参考图片**: `docs\labelling\labels.png`

### 中间区域：图片预览与标注区

#### 整体设计
- **背景色**: 灰色
- **显示内容**: 待标注的图片
- **参考图片**: `docs/labelling/labelling.png`

#### 顶部工具栏（根据Project Type动态显示）

##### 1. Object Detection 类型
**说明**: Label with bounding boxes. Use to identify one or more objects in an image.

**工具栏按钮**（从左到右）:

1. **Pan 图标**
   - 功能: 退出labeling模式，进入拖拽模式
   - 操作: 点击后可拖拽图片

2. **Bounding Box Tool 图标**
   - 功能: 用于labelling，绘制矩形标注框
   - 操作: 点击后进入标注模式

3. **Undo/Redo 图标**
   - 功能: 撤销和重做操作
   - 操作: 点击实现undo, redo

4. **扫帚图标**
   - 功能: 删除当前image的所有label
   - 操作: 点击后清空所有标注框

5. **Classes 下拉选项**
   - 功能: 选择class进行标注
   - 特性: 不同的class标注时的边框颜色不一样
   - 创建Class: 点击下拉框中的加号（+），弹出project class设置界面
   - 参考图片: 
     - `docs/labelling/chooseClass.png` - 选择Class界面
     - `docs/labelling/createClass.png` - 创建Class界面

6. **Zoom In/Out 和 Enhance 图标**
   - **Zoom In/Out 图标**: 放大和缩小图片
     - 功能: 放大和缩小图片显示
     - 操作: 点击放大或缩小图片
     - 参考图片: `docs/labelling/zoominout.png`
   - **Enhance 图标**: 调节图片的显示效果
     - 功能: 调节图片的显示效果
     - 操作: 点击后弹出Enhance界面
     - **Enhance界面内容**:
       - **Brightness 滑块**: 调节图片亮度
       - **Contrast 滑块**: 调节图片对比度
       - 实时预览调节效果
     - 说明: 仅影响显示效果，不修改原始图片

7. **Hold to Hide 图标**
   - 功能: 按住时隐藏所有标注框，松开后恢复显示
   - 操作: 按住图标时隐藏标注框，方便查看原图
   - 参考图片: `docs/labelling/holdtohide.png`, `docs/labelling/segmentation1.png`

##### 2. Segmentation 类型
**说明**: Label with precision tools. Use when pixel-level precision is required. Output from the model is a mask of the pixels.

**工具栏按钮**（从左到右）:

1. **Pan 图标**
   - 功能: 退出labeling模式，进入拖拽模式
   - 操作: 点击后可拖拽图片

2. **Smart Labeling 工具组**
   - **Smart Labeling 图标**: 智能标注工具
   - **Brush 图标**: 画笔工具，用于精细标注
   - **Polygon 图标**: 多边形工具，点击顶点绘制多边形
   - **Polyline 图标**: 折线工具，绘制折线标注
   - 参考图片: `docs/labelling/segmentation.png`

3. **Undo/Redo 图标**
   - 功能: 撤销和重做操作
   - 操作: 点击实现undo, redo

4. **扫帚图标**
   - 功能: 删除当前image的所有label
   - 操作: 点击后清空所有标注框

5. **Classes 下拉选项**
   - 功能: 选择class进行标注
   - 特性: 不同的class标注时的边框颜色不一样
   - 创建Class: 点击下拉框中的加号（+），弹出project class设置界面
   - 参考图片: 
     - `docs/labelling/chooseClass.png` - 选择Class界面
     - `docs/labelling/createClass.png` - 创建Class界面

6. **Zoom In/Out 和 Enhance 图标**
   - **Zoom In/Out 图标**: 放大和缩小图片
     - 功能: 放大和缩小图片显示
     - 操作: 点击放大或缩小图片
     - 参考图片: `docs/labelling/zoominout.png`
   - **Enhance 图标**: 调节图片的显示效果
     - 功能: 调节图片的显示效果
     - 操作: 点击后弹出Enhance界面
     - **Enhance界面内容**:
       - **Brightness 滑块**: 调节图片亮度
       - **Contrast 滑块**: 调节图片对比度
       - 实时预览调节效果
     - 说明: 仅影响显示效果，不修改原始图片

7. **Hold to Hide 图标**
   - 功能: 按住时隐藏所有标注框，松开后恢复显示
   - 操作: 按住图标时隐藏标注框，方便查看原图
   - 参考图片: `docs/labelling/holdtohide.png`, `docs/labelling/segmentation1.png`

##### 3. Classification 类型
**说明**: Each image itself is a label. Use to classify the contents of an image as a whole to distinguish.

**工具栏按钮**（从左到右）:

1. **Zoom In/Out 图标**
   - 功能: 放大和缩小图片
   - 操作: 点击放大或缩小图片显示
   - 参考图片: `docs/labelling/zoominout.png`

2. **Enhance 图标**
   - 功能: 调节图片的显示效果
   - 操作: 点击后弹出Enhance界面
   - **Enhance界面内容**:
     - **Brightness 滑块**: 调节图片亮度
     - **Contrast 滑块**: 调节图片对比度
     - 实时预览调节效果
   - 说明: 仅影响显示效果，不修改原始图片

3. **Classes 下拉选项**
   - 功能: 选择class对整张图片进行分类
   - 特性: 
     - 选择class时自动创建label
     - Classification类型的label没有坐标信息（position字段为空或null）
     - 一张图片只能有一个classification label
   - 创建Class: 点击下拉框中的加号（+），弹出project class设置界面
   - 参考图片: 
     - `docs/labelling/chooseClass.png` - 选择Class界面
     - `docs/labelling/createClass.png` - 创建Class界面
     - `docs/labelling/classification.png` - Classification界面

**注意**: Classification类型不需要标注工具，整个图片本身就是一个标签。选择class后自动为图片创建分类标签。

---

## 技术实现要求

### 前端实现

#### 1. 创建新的 Label Component
- **组件路径**: `code/frontend/src/main/frontend/src/app/modules/labelling/`
- **组件名称**: `image-labelling.component.ts`
- **模板文件**: `image-labelling.component.html`
- **样式文件**: `image-labelling.component.scss`
- **输入参数**: 
  - `@Input() project: Project` - 从父组件（项目界面）传入的项目信息
  - 组件根据传入的 project 自动加载图片列表和类别信息
- **使用方式**: `<app-image-labelling [project]="currentProject"></app-image-labelling>`
- **核心功能**:
  - 根据 `project.type` 动态显示不同的工具栏
  - Object Detection: 显示 Pan, Bounding Box, Undo/Redo, 清空, Classes, Zoom In/Out, Enhance, Hold to Hide
  - Segmentation: 显示 Pan, Smart Labeling/Brush/Polygon/Polyline, Undo/Redo, 清空, Classes, Zoom In/Out, Enhance, Hold to Hide
  - Classification: 显示 Zoom In/Out, Enhance, Classes
  - **Classification特殊处理**:
    - 选择class时自动调用API创建label
    - label的position字段设置为null
    - 切换class时删除旧label，创建新label
    - 一张图片只保留一个classification label

#### 2. 前端服务（Services）

##### ImageService
- **路径**: `code/frontend/src/main/frontend/src/app/services/landingai/image.service.ts`
- **功能**:
  - 获取图片列表
  - 上传图片
  - 删除图片
  - 批量操作图片
  - 获取图片详情

##### LabelService
- **路径**: `code/frontend/src/main/frontend/src/app/services/landingai/label.service.ts`
- **功能**:
  - 保存标注数据
  - 加载标注数据
  - 删除标注
  - 批量操作标注

##### ProjectClassService
- **路径**: `code/frontend/src/main/frontend/src/app/services/landingai/project-class.service.ts`
- **功能**:
  - 获取项目类别列表
  - 创建类别
  - 更新类别
  - 删除类别

##### TagService
- **路径**: `code/frontend/src/main/frontend/src/app/services/landingai/tag.service.ts`
- **功能**:
  - 添加 Tag
  - 删除 Tag
  - 获取 Tag 列表

##### MetadataService
- **路径**: `code/frontend/src/main/frontend/src/app/services/landingai/metadata.service.ts`
- **功能**:
  - 获取 Metadata 列表
  - 保存 Metadata 值
  - 更新 Metadata

#### 3. 前端模型（Models）

##### Image Model
- **路径**: `code/frontend/src/main/frontend/src/app/models/landingai/image.model.ts`
- **属性**:
  - id: number
  - projectId: number
  - fileName: string
  - filePath: string
  - fileSize: number
  - width: number
  - height: number
  - split: string (Train/Dev/Test/Unassigned)
  - isNoClass: boolean
  - createdAt: Date
  - createdBy: string
  - annotations?: Annotation[]

##### Annotation Model
- **路径**: `code/frontend/src/main/frontend/src/app/models/landingai/annotation.model.ts`
- **属性**:
  - id: number
  - label: string
  - type: AnnotationType (Rectangle/Polygon/Ellipse)
  - color: string
  - x: number
  - y: number
  - width: number
  - height: number
  - points: Point[] (多边形顶点)
  - classId: number
  - className: string

##### Project Model
- **路径**: `code/frontend/src/main/frontend/src/app/models/landingai/project.model.ts`
- **属性**:
  - id: number
  - name: string
  - status: string (Upload/Label/Train/Predict)
  - type: string (Object Detection/Segmentation/Classification)
  - modelName: string
  - createdAt: Date
  - createdBy: string

##### ProjectClass Model
- **路径**: `code/frontend/src/main/frontend/src/app/models/landingai/project-class.model.ts`
- **属性**:
  - id: number
  - name: string
  - color: string
  - projectId: number

##### EnhanceSettings Model
- **路径**: `code/frontend/src/main/frontend/src/app/models/landingai/enhance-settings.model.ts`
- **属性**:
  - brightness: number (-100 到 +100)
  - contrast: number (-100 到 +100)

#### 4. 依赖库
- **ngx-image-annotation**: 图像标注核心库
- **Angular Material**: UI 组件库

#### 5. 样式要求
- 使用 Angular Material 风格
- 按钮统一使用 MatButtonModule
- 整体风格保持一致
- 响应式设计，适配不同屏幕尺寸

---

## 后端实现

### 1. 使用现有 Entity 类

项目已存在以下 Entity 类，直接使用：

#### Image Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/Image.java`
- **表名**: `la_images`
- **关键字段**: 
  - id (Long)
  - project (Project)
  - fileName
  - filePath
  - fileUrl
  - fileSize
  - width
  - height
  - split
  - isNoClass
  - thumbnailImage
  - createdAt
  - createdBy
- **关联关系**: 
  - ManyToOne: Project
  - OneToMany: ImageLabel, ImageTag, ImageMetadata

#### ImageLabel Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/ImageLabel.java`
- **表名**: `la_images_label`
- **关键字段**: 
  - id (Long)
  - image (Image)
  - classId (Long)
  - position (String/JSON)
  - confidenceRate
  - annotationType
  - createdAt
  - createdBy
- **关联关系**: ManyToOne: Image

#### Project Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/Project.java`
- **表名**: `la_projects`
- **关键字段**: 
  - id (Long)
  - name
  - status
  - type
  - modelName
  - createdAt
  - createdBy
- **关联关系**: OneToMany: ProjectClass, ProjectTag, ProjectMetadata, Image, TrainingRecord

#### ProjectClass Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/ProjectClass.java`
- **表名**: `la_project_class`
- **关键字段**: 
  - id (Long)
  - project (Project)
  - className
  - description
  - colorCode
  - createdAt
  - createdBy
- **关联关系**: ManyToOne: Project

#### ProjectTag Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/ProjectTag.java`
- **表名**: `la_project_tag`
- **关键字段**: 
  - id (Long)
  - project (Project)
  - name
  - createdAt
  - createdBy
- **关联关系**: ManyToOne: Project

#### ProjectMetadata Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/ProjectMetadata.java`
- **表名**: `la_project_metadata`
- **关键字段**: 
  - id (Long)
  - project (Project)
  - name
  - type
  - valueFrom
  - predefinedValues
  - multipleValues
  - createdAt
  - createdBy
- **关联关系**: ManyToOne: Project

#### ImageTag Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/ImageTag.java`
- **表名**: `la_images_tag`
- **用途**: 存储图片的Tag值

#### ImageMetadata Entity
- **路径**: `code/backend/data-model/src/main/java/com/nxp/iemdm/model/landingai/ImageMetadata.java`
- **表名**: `la_images_metadata`
- **用途**: 存储图片的Metadata值

### 2. 需要创建的 Repository 接口

所有Repository需要在 `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/` 目录下创建。

#### ImageLabelRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ImageLabelRepository.java`
- **继承**: JpaRepository<ImageLabel, Long>
- **必要查询方法**:
  ```java
  // 根据图片ID查询所有标注
  List<ImageLabel> findByImageId(Long imageId);
  
  // 根据类别ID查询所有标注
  List<ImageLabel> findByClassId(Long classId);
  
  // 删除图片的所有标注
  void deleteByImageId(Long imageId);
  
  // 根据多个图片ID查询标注
  List<ImageLabel> findByImageIdIn(List<Long> imageIds);
  
  // 统计图片的标注数量
  long countByImageId(Long imageId);
  ```

#### ImageRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ImageRepository.java`
- **继承**: JpaRepository<Image, Long>
- **必要查询方法**:
  ```java
  // 根据项目ID查询所有图片
  List<Image> findByProjectIdOrderByCreatedAtDesc(Long projectId);
  
  // 根据文件名查询图片
  Optional<Image> findByFileName(String fileName);
  
  // 根据项目ID和Split查询图片
  List<Image> findByProjectIdAndSplit(Long projectId, String split);
  
  // 查询项目中无类别的图片
  List<Image> findByProjectIdAndIsNoClassTrue(Long projectId);
  ```

#### ProjectClassRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ProjectClassRepository.java`
- **继承**: JpaRepository<ProjectClass, Long>
- **必要查询方法**:
  ```java
  // 根据项目ID查询所有类别
  List<ProjectClass> findByProjectIdOrderByCreatedAt(Long projectId);
  
  // 根据项目ID和类别名称查询
  Optional<ProjectClass> findByProjectIdAndClassName(Long projectId, String className);
  ```

#### ProjectRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ProjectRepository.java`
- **继承**: JpaRepository<Project, Long>
- **必要查询方法**:
  ```java
  // 根据项目名称查询
  Optional<Project> findByName(String name);
  
  // 根据状态查询项目
  List<Project> findByStatusOrderByCreatedAtDesc(String status);
  
  // 根据创建者查询项目
  List<Project> findByCreatedByOrderByCreatedAtDesc(String createdBy);
  
  // 查询所有项目按创建时间排序
  List<Project> findAllByOrderByCreatedAtDesc();
  ```

#### ProjectTagRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ProjectTagRepository.java`
- **继承**: JpaRepository<ProjectTag, Long>
- **必要查询方法**:
  ```java
  // 根据项目ID查询所有Tag
  List<ProjectTag> findByProjectIdOrderByCreatedAt(Long projectId);
  
  // 根据项目ID和Tag名称查询
  Optional<ProjectTag> findByProjectIdAndName(Long projectId, String name);
  ```

#### ProjectMetadataRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ProjectMetadataRepository.java`
- **继承**: JpaRepository<ProjectMetadata, Long>
- **必要查询方法**:
  ```java
  // 根据项目ID查询所有Metadata
  List<ProjectMetadata> findByProjectIdOrderByCreatedAt(Long projectId);
  
  // 根据项目ID和Metadata名称查询
  Optional<ProjectMetadata> findByProjectIdAndName(Long projectId, String name);
  ```

#### ImageTagRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ImageTagRepository.java`
- **继承**: JpaRepository<ImageTag, Long>
- **必要查询方法**:
  ```java
  // 根据图片ID查询所有Tag
  List<ImageTag> findByImageId(Long imageId);
  
  // 删除图片的所有Tag
  void deleteByImageId(Long imageId);
  ```

#### ImageMetadataRepository
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/repository/jpa/landingai/ImageMetadataRepository.java`
- **继承**: JpaRepository<ImageMetadata, Long>
- **必要查询方法**:
  ```java
  // 根据图片ID查询所有Metadata
  List<ImageMetadata> findByImageId(Long imageId);
  
  // 删除图片的所有Metadata
  void deleteByImageId(Long imageId);
  ```

### 3. Service 层（业务逻辑）

#### Operational Service（实际业务逻辑）
需要在 `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/service/landingai/` 创建：

#### ImageLabelService
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/service/landingai/ImageLabelService.java`
- **主要方法**:
  - saveLabel(ImageLabel label): ImageLabel - 保存单个标注
  - saveBatch(List<ImageLabel> labels): List<ImageLabel> - 批量保存标注
  - getLabelsByImageId(Long imageId): List<ImageLabel> - 获取图片的所有标注
  - deleteLabel(Long labelId): void - 删除单个标注
  - deleteLabelsByImageId(Long imageId): void - 删除图片的所有标注
  - updateLabel(Long labelId, ImageLabel label): ImageLabel - 更新标注

#### ImageService
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/service/landingai/ImageService.java`
- **主要方法**:
  - saveImage(Image image): Image - 保存图片信息
  - getImageById(Long imageId): Image - 获取图片详情
  - getImagesByProjectId(Long projectId): List<Image> - 获取项目的所有图片
  - deleteImage(Long imageId): void - 删除图片
  - deleteImages(List<Long> imageIds): void - 批量删除图片
  - updateImageSplit(Long imageId, String split): Image - 更新 Split 状态
  - updateIsNoClass(Long imageId, boolean isNoClass): Image - 更新 isNoClass 标志
  - getImageFromFileSystem(String fileName): byte[] - 从文件系统读取图片

#### ProjectClassService
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/service/landingai/ProjectClassService.java`
- **主要方法**:
  - createClass(ProjectClass projectClass): ProjectClass - 创建类别
  - getClassesByProjectId(Long projectId): List<ProjectClass> - 获取项目的所有类别
  - updateClass(Long classId, ProjectClass projectClass): ProjectClass - 更新类别
  - deleteClass(Long classId): void - 删除类别

#### FileStorageService
- **路径**: `code/backend/iemdm-operational/src/main/java/com/nxp/iemdm/operational/service/landingai/FileStorageService.java`
- **主要方法**:
  - storeFile(MultipartFile file): String - 存储文件到 \image\2026
  - loadFile(String fileName): Resource - 从文件系统加载文件
  - deleteFile(String fileName): void - 删除文件
  - getFileList(): List<String> - 获取文件列表

#### API Service REST（RestTemplate调用层）
需要在 `code/backend/api/src/main/java/com/nxp/iemdm/service/rest/landingai/` 创建：

##### ImageLabelServiceREST
- 通过RestTemplate调用Operational Layer的标注服务
- 参考实现: `PersonServiceREST.java`

##### ImageServiceREST
- 通过RestTemplate调用Operational Layer的图片服务

##### ProjectClassServiceREST
- 通过RestTemplate调用Operational Layer的类别服务

**说明**: API Layer的Service通过RestTemplate调用Operational Layer的Controller端点。

**关键代码模式**:
```java
// Service REST 层使用 RestTemplate
@Service
public class ImageLabelServiceREST implements ImageLabelService {
    private final RestTemplate restTemplate;
    private final String operationalServiceURI;
    
    @Autowired
    public ImageLabelServiceREST(
        RestTemplate restTemplate, 
        @Value("${rest.operational.uri}") String operationalServiceURI) {
        this.restTemplate = restTemplate;
        this.operationalServiceURI = operationalServiceURI;
    }
    
    public ImageLabel saveLabel(ImageLabel label) {
        ResponseEntity<ImageLabel> response = 
            restTemplate.postForEntity(
                operationalServiceURI + "/landingai/labels", 
                label, 
                ImageLabel.class);
        return response.getBody();
    }
}
```

### 4. Controller 层（API 端点）

#### 层间交互方式
**API Layer 和 Operational Layer 之间通过 `org.springframework.web.client.RestTemplate` 进行交互**

**实现模式**:
1. **API Layer (Controller)**: 接收前端请求，调用 Service REST 层
2. **Service REST 层**: 使用 RestTemplate 调用 Operational Layer 的 REST 端点
3. **Operational Layer (Controller)**: 处理实际业务逻辑，返回结果

**参考实现**:
- API Controller 示例: `code/backend/api/src/main/java/com/nxp/iemdm/controller/UserController.java`
- Service REST 示例: `code/backend/api/src/main/java/com/nxp/iemdm/service/rest/PersonServiceREST.java`

#### API Layer Controllers (与前端交互)

##### ImageLabelApiController
- **路径**: `code/backend/api/src/main/java/com/nxp/iemdm/controller/landingai/ImageLabelApiController.java`
- **端点**:
  - POST /api/landingai/labels - 保存标注
  - POST /api/landingai/labels/batch - 批量保存标注
  - GET /api/landingai/labels/image/{imageId} - 获取图片的所有标注
  - PUT /api/landingai/labels/{labelId} - 更新标注
  - DELETE /api/landingai/labels/{labelId} - 删除标注
  - DELETE /api/landingai/labels/image/{imageId} - 删除图片的所有标注

##### ImageApiController
- **路径**: `code/backend/api/src/main/java/com/nxp/iemdm/controller/landingai/ImageApiController.java`
- **端点**:
  - POST /api/landingai/images/upload - 上传图片
  - GET /api/landingai/images/{imageId} - 获取图片详情
  - GET /api/landingai/images/project/{projectId} - 获取项目的所有图片
  - GET /api/landingai/images/file/{fileName} - 获取图片文件
  - PUT /api/landingai/images/{imageId}/split - 更新 Split 状态
  - PUT /api/landingai/images/{imageId}/is-no-class - 更新 isNoClass 标志
  - DELETE /api/landingai/images/{imageId} - 删除图片
  - POST /api/landingai/images/delete-batch - 批量删除图片

##### ProjectClassApiController
- **路径**: `code/backend/api/src/main/java/com/nxp/iemdm/controller/landingai/ProjectClassApiController.java`
- **端点**:
  - POST /api/landingai/project-classes - 创建类别
  - GET /api/landingai/project-classes/project/{projectId} - 获取项目的所有类别
  - PUT /api/landingai/project-classes/{classId} - 更新类别
  - DELETE /api/landingai/project-classes/{classId} - 删除类别

#### Operational Layer Controllers (内部调用)
- ImageLabelController: /operational/landingai/labels/*
- ImageController: /operational/landingai/images/*
- ProjectClassController: /operational/landingai/project-classes/*

---
## 实现步骤建议

### 阶段 1：后端基础设施
1. 创建所有 Repository 接口（添加必要的查询方法）
2. 实现 Operational Service 层基础方法
3. 实现 Operational Controller 层基础端点
4. **实现 API Service REST 层**（使用RestTemplate调用Operational Layer）
5. 实现 API Controller 层（调用Service REST）
6. 实现 FileStorageService
7. 配置 RestTemplate 和服务URI（application.properties）

### 阶段 2：前端基础框架
1. 安装依赖（ngx-image-annotation）
2. 创建 Models（Image, Annotation, ProjectClass）
3. 创建 Services（ImageService, LabelService, ProjectClassService）
4. 创建 Label Component 基础结构

### 阶段 3：标注功能实现
1. 集成 ngx-image-annotation 组件
2. **实现动态工具栏**:
   - 根据project.type显示不同的工具按钮
   - Object Detection: 实现Bounding Box工具
   - Segmentation: 实现Smart Labeling, Brush, Polygon, Polyline工具
   - Classification: 实现Zoom、Enhance和Classes下拉选项
3. 实现标注编辑功能（拖拽、缩放、删除）
4. 实现 Undo/Redo 功能
5. 实现类别选择和颜色区分
6. 实现 Class 创建界面（弹出对话框）
7. **实现Classification自动标注**:
   - 选择class时自动创建label
   - Classification类型的label不包含坐标信息
   - 一张图片只能有一个classification label
8. 实现图片增强功能（Brightness/Contrast调节）

### 阶段 4：数据持久化
1. 实现标注自动保存到数据库
2. 实现标注自动加载
3. 实现 JSON 格式校验
4. 实现图片切换时的数据同步

### 阶段 5: GUI 完善
1. 实现左侧工具栏（Project 详情 + 图片列表）
2. 实现右侧工具栏（General + Tags + Metadata + Labels）
3. 实现顶部标注工具栏
4. 实现批量操作功能
5. 优化样式和交互

### 阶段 6: 高级功能
1. 实现预标注功能（调用模型）
2. 实现 Split 管理
3. 实现 Tags 管理
4. 实现 Metadata 管理
5. 实现 No Object to Label 功能

### 阶段 7: 测试和优化
1. 单元测试
2. 集成测试
3. 性能优化
4. 用户体验优化

---

## 参考资料

### 界面参考图片
- **标注界面**: `docs/labelling/labelling.png`
- **顶部工具栏**: `docs/labelling/topLabelToolBar.png`
- **右侧工具栏**: `docs/labelling/rightSideBar.png`
- **项目状态**: `docs/labelling/projectStatus.png`
- **标签块**: `docs/labelling/labels.png`
- **Tags 块**: `docs/labelling/tags.png`, `docs/labelling/tags1.png`
- **Metadata 块**: `docs/labelling/metadata1.png` ~ `metadata4.png`
- **Split 选项**: `docs/labelling/splitOption.png`
- **选择Class**: `docs/labelling/chooseClass.png`
- **创建Class**: `docs/labelling/createClass.png`
- **Segmentation工具**: `docs/labelling/segmentation.png`
- **Segmentation界面**: `docs/labelling/segmentation1.png`
- **Zoom In/Out**: `docs/labelling/zoominout.png`
- **Hold to Hide**: `docs/labelling/holdtohide.png`
- **Classification**: `docs/labelling/classification.png`

---

## 注意事项

1. **使用现有Entity**: 不要重新创建Entity类，直接使用data-model模块中的Entity
2. **层间交互**: API Layer通过RestTemplate调用Operational Layer，参考UserController和PersonServiceREST的实现模式
3. **配置管理**: 在application.properties中配置Operational Layer的服务URI（如：rest.operational.uri）
4. **Project Type判断**: 根据project.type字段动态显示不同的工具栏和功能
5. **工具栏动态切换**: 
   - Object Detection: 显示矩形标注工具
   - Segmentation: 显示精细标注工具（Brush, Polygon等）
   - Classification: 仅显示图片查看和增强工具
6. **Class管理**: 支持在标注过程中动态创建新的Class
7. **Enhance功能**: 仅用于显示调节，不修改原始图片文件
8. **Classification特殊处理**: 选择class时自动创建label，position字段为null，一张图片只保留一个classification label
9. **坐标精度**: 确保导出时使用图片真实宽高，避免坐标偏差
10. **异步处理**: ZIP 打包使用 async/await，避免阻塞
11. **格式校验**: 导入 JSON 时进行格式校验，防止数据损坏
12. **自动保存**: 标注变更时自动保存，避免数据丢失
13. **颜色管理**: 不同类别使用不同颜色，提升可视化效果
14. **批量操作**: 支持批量导出和删除，提升效率
15. **响应式设计**: 确保在不同屏幕尺寸下正常显示
16. **错误处理**: 完善的错误提示和异常处理
17. **性能优化**: 大量标注时的性能优化
18. **安全性**: 文件上传和下载的安全性检查

---

## 开发规范

### 代码风格
- **前端**: 遵循 Angular 官方风格指南
- **后端**: 遵循 Java 编码规范
- **命名**: 使用有意义的变量和方法名
- **注释**: 关键逻辑添加注释说明

### Git 提交
- 每个功能模块独立提交
- 提交信息清晰明确
- 代码审查后再合并

### 测试要求
- 单元测试覆盖率 > 80%
- 集成测试覆盖主要业务流程
- 手动测试验证用户体验

---

## 预期交付物

1. 完整的前端 Label Component
2. 完整的后端 API 接口
3. 用户操作手册
4. API 文档
5. 测试报告

---

**文档版本**: 2.1 (Summary + Integrated)  
**创建日期**: 2026-01-13  
**最后更新**: 2026-01-13  
**基于原文档**: prompt-ngx-image-annotation.md v1.8

## 文档说明

本文档是 `prompt-ngx-image-annotation.md` 的精简版本，并整合了 `prompt-ngx-image-annotation-simplified.md` 的内容，主要变更：

1. **移除内容**:
   - 数据库表结构设计（DDL语句）
   - 完整的代码实现（Entity、Service、Controller 代码）
   - 详细的代码示例

2. **保留内容**:
   - 功能需求说明
   - GUI 界面设计
   - 技术实现要求
   - 文件路径和结构
   - 实现步骤建议

3. **新增内容**:
   - 明确使用项目现有的 Entity 类
   - 列出需要创建的 Repository 接口及必要查询方法
   - 简化的 Service 和 Controller 层说明
   - **Project Type 支持**（Object Detection/Segmentation/Classification）
   - **详细的工具栏设计**（根据不同类型动态显示）
   - **图片增强功能**（Brightness/Contrast调节）
   - **Classification特殊处理**（自动创建label，无坐标信息）
   - **层间交互方式**（RestTemplate调用模式）
   - **更详细的参考图片说明**

4. **整合内容**:
   - 从 simplified 文档整合了 Project Type 的详细说明
   - 整合了三种类型的工具栏设计
   - 整合了 Class 创建界面说明
   - 整合了图片增强功能说明
   - 整合了 RestTemplate 层间交互说明
   - 整合了更完整的参考图片列表
