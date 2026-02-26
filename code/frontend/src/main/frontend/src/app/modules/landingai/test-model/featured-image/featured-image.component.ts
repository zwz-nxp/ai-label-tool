import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from "@angular/core";
import {
  PredictionResult,
  BoundingBox,
  Label,
} from "../../../../models/landingai/test-model.model";
import { ProjectClass } from "../../../../models/landingai/project-class.model";
import { ProjectClassService } from "../../../../services/landingai/project-class.service";

/**
 * Component for displaying the featured image with bounding box overlays.
 * 顯示圖片並繪製 prediction labels
 */
@Component({
  selector: "app-featured-image",
  standalone: false,
  templateUrl: "./featured-image.component.html",
  styleUrls: ["./featured-image.component.scss"],
})
export class FeaturedImageComponent implements OnChanges, OnInit {
  @Input() imageUrl!: string;
  @Input() predictions!: PredictionResult;
  @Input() projectId!: number;

  boundingBoxes: BoundingBox[] = [];
  imageWidth: number = 0;
  imageHeight: number = 0;
  imageLoaded: boolean = false;
  projectClasses: ProjectClass[] = [];
  zoomLevel: number = 1; // 預設縮放等級

  // 拖拉相關屬性
  isDragging: boolean = false;
  dragStartX: number = 0;
  dragStartY: number = 0;
  scrollLeft: number = 0;
  scrollTop: number = 0;

  constructor(private projectClassService: ProjectClassService) {}

  ngOnInit(): void {
    // 載入 project classes
    if (this.projectId) {
      this.loadProjectClasses();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["predictions"] || changes["imageUrl"]) {
      this.imageLoaded = false;
    }

    if (changes["projectId"] && this.projectId) {
      this.loadProjectClasses();
    }
  }

  /**
   * 載入 project classes
   */
  private loadProjectClasses(): void {
    console.log("=== Loading project classes ===");
    console.log("ProjectId:", this.projectId);

    this.projectClassService.getClassesByProjectId(this.projectId).subscribe({
      next: (classes: ProjectClass[]) => {
        // 按 ID 排序 (假設 ID 順序對應 sequence)
        this.projectClasses = classes.sort(
          (a: ProjectClass, b: ProjectClass) => a.id - b.id
        );

        console.log("=== Project Classes Loaded ===");
        console.log("Total classes:", this.projectClasses.length);
        console.log("Classes detail:");
        this.projectClasses.forEach((cls, index) => {
          console.log(
            `  [${index}] ID: ${cls.id}, Name: ${cls.className}, Color: ${cls.colorCode}`
          );
        });

        // 重新計算 bounding boxes (如果圖片已載入)
        if (this.imageLoaded) {
          console.log("Image already loaded, recalculating bounding boxes...");
          this.calculateBoundingBoxes();
        }
      },
      error: (error: any) => {
        console.error("=== Failed to load project classes ===");
        console.error("Error:", error);
      },
    });
  }

  /**
   * Handles image load event to get dimensions.
   */
  onImageLoad(event: Event): void {
    const img = event.target as HTMLImageElement;
    this.imageWidth = img.naturalWidth;
    this.imageHeight = img.naturalHeight;
    this.imageLoaded = true;

    // Calculate bounding boxes
    this.calculateBoundingBoxes();
  }

  /**
   * Calculates pixel coordinates for bounding boxes from normalized coordinates.
   */
  private calculateBoundingBoxes(): void {
    if (!this.predictions || !this.predictions.predictions) {
      this.boundingBoxes = [];
      return;
    }

    console.log("=== Calculating Bounding Boxes ===");
    console.log("Predictions count:", this.predictions.predictions.length);
    console.log("Project classes available:", this.projectClasses.length);

    this.boundingBoxes = this.predictions.predictions.map(
      (label: Label, index: number) => {
        // bbox 格式: [xcenter, ycenter, width, height] - 已經是正規化座標 (0-1)
        const [xcenter, ycenter, width, height] = label.bbox;

        // Convert normalized coordinates (0-1) to pixel coordinates
        const pixelX = (xcenter - width / 2) * this.imageWidth;
        const pixelY = (ycenter - height / 2) * this.imageHeight;
        const pixelWidth = width * this.imageWidth;
        const pixelHeight = height * this.imageHeight;

        // 根據 classId (sequence) 從 projectClasses 取得 className
        // classId 是從 0 開始的索引
        // 注意: API 可能使用 snake_case (class_id) 或 camelCase (classId)
        let className = label.className || label.class_name;
        let classId =
          label.classId !== undefined ? label.classId : label.class_id;

        // 確保 classId 有預設值
        if (classId === undefined) {
          classId = -1;
        }

        console.log(`\n--- Prediction ${index + 1} ---`);
        console.log("Label from API (stringified):", JSON.stringify(label));
        console.log("Label keys:", Object.keys(label));
        console.log("ClassId (from classId):", label.classId);
        console.log("ClassId (from class_id):", label.class_id);
        console.log("Final ClassId:", classId);
        console.log("ClassName from API:", className);

        if (!className && this.projectClasses.length > 0 && classId >= 0) {
          console.log("Mapping classId to projectClasses[" + classId + "]");

          if (classId < this.projectClasses.length) {
            className = this.projectClasses[classId].className;
            console.log("✅ Mapped to className:", className);
          } else {
            className = `Class ${classId}`;
            console.log("⚠️ ClassId out of range, using fallback:", className);
          }
        } else if (!className && classId >= 0) {
          className = `Class ${classId}`;
          console.log(
            "⚠️ No projectClasses available, using fallback:",
            className
          );
        } else if (!className) {
          className = "Unknown";
          console.log("⚠️ No classId available, using fallback: Unknown");
        } else {
          console.log("✅ Using className from API:", className);
        }

        // 確保 className 有值
        if (!className) {
          className = "Unknown";
        }

        // 根據 classId 從 projectClasses 取得顏色
        let color = "#FF5C9A"; // 預設顏色
        if (
          this.projectClasses.length > 0 &&
          classId >= 0 &&
          classId < this.projectClasses.length
        ) {
          color = this.projectClasses[classId].colorCode;
          console.log(`✅ Mapped classId ${classId} to color:`, color);
        } else {
          console.log(`⚠️ Using default color for classId ${classId}`);
        }

        // 建立 label 文字 (格式: ClassName(confidence%))
        const labelText = `${className}(${(label.confidence * 100).toFixed(0)}%)`;
        console.log("Final labelText:", labelText);

        // 使用固定字體大小 40px (在 SVG viewBox 座標系統中,這會根據圖片縮放)
        const fontSize = 40;

        // 計算 label 尺寸
        const labelHeight = fontSize + 16; // 增加 padding
        const labelWidth = this.estimateTextWidth(labelText, fontSize) + 16; // 增加 padding

        // Label 位置: 在 box 左上角外側 (上方)
        const labelX = pixelX;
        const labelY = pixelY - labelHeight;

        return {
          x: pixelX,
          y: pixelY,
          width: pixelWidth,
          height: pixelHeight,
          classId: classId,
          className: className,
          confidence: label.confidence,
          color: color,
          strokeWidth: 3, // 增加邊框寬度
          fillOpacity: 0.2,
          labelText: labelText,
          labelX: labelX,
          labelY: labelY,
          labelWidth: labelWidth,
          labelHeight: labelHeight,
          fontSize: fontSize,
        };
      }
    );

    console.log("\n=== Bounding Boxes Calculated ===");
    console.log("Total boxes:", this.boundingBoxes.length);
  }

  /**
   * 估計文字寬度 (粗略計算)
   */
  private estimateTextWidth(text: string, fontSize: number): number {
    // 粗略估計: 每個字元約佔 fontSize * 0.6 的寬度
    return text.length * fontSize * 0.6;
  }

  /**
   * 放大圖片
   */
  zoomIn(): void {
    if (this.zoomLevel < 3) {
      this.zoomLevel = Math.min(this.zoomLevel + 0.25, 3);
    }
  }

  /**
   * 縮小圖片
   */
  zoomOut(): void {
    if (this.zoomLevel > 0.5) {
      this.zoomLevel = Math.max(this.zoomLevel - 0.25, 0.5);
    }
  }

  /**
   * 重置縮放
   */
  resetZoom(): void {
    this.zoomLevel = 1;
  }

  /**
   * 滑鼠滾輪縮放
   */
  onMouseWheel(event: WheelEvent): void {
    event.preventDefault();

    if (event.deltaY < 0) {
      // 向上滾動 = 放大
      this.zoomIn();
    } else {
      // 向下滾動 = 縮小
      this.zoomOut();
    }
  }

  /**
   * 開始拖拉
   */
  onMouseDown(event: MouseEvent, viewport: HTMLElement): void {
    this.isDragging = true;
    this.dragStartX = event.clientX;
    this.dragStartY = event.clientY;
    this.scrollLeft = viewport.scrollLeft;
    this.scrollTop = viewport.scrollTop;
    event.preventDefault();
  }

  /**
   * 拖拉中
   */
  onMouseMove(event: MouseEvent, viewport: HTMLElement): void {
    if (!this.isDragging) return;

    event.preventDefault();
    const deltaX = event.clientX - this.dragStartX;
    const deltaY = event.clientY - this.dragStartY;

    viewport.scrollLeft = this.scrollLeft - deltaX;
    viewport.scrollTop = this.scrollTop - deltaY;
  }

  /**
   * 結束拖拉
   */
  onMouseUp(): void {
    this.isDragging = false;
  }
}
