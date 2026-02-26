import {
  AfterViewInit,
  Directive,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
} from "@angular/core";
import Konva from "konva";
import { Annotation } from "app/models/landingai/annotation.model";
import { Image } from "app/models/landingai/image.model";
import { ProjectClass } from "app/models/landingai/project-class.model";
import { EnhanceSettings } from "app/models/landingai/enhance-settings.model";

/**
 * Annotation tool types
 */
export type AnnotationTool =
  | "pan"
  | "boundingBox"
  | "polygon"
  | "brush"
  | "polyline"
  | "none";

/**
 * Base class for annotation canvas components
 * Contains shared inputs, outputs, and common functionality
 */
@Directive()
export abstract class BaseAnnotationCanvasComponent
  implements AfterViewInit, OnChanges, OnDestroy
{
  @Input() image: Image | null = null;
  @Input() imageUrl: string | null = null;
  @Input() annotations: Annotation[] = [];
  @Input() selectedAnnotation: Annotation | null = null;
  @Input() selectedAnnotations: Annotation[] = [];
  @Input() selectedClass: ProjectClass | null = null;
  @Input() activeTool: AnnotationTool = "none";
  @Input() zoomLevel = 1.0;
  @Input() isPanMode = false;
  @Input() hideAnnotations = false;
  @Input() enhanceSettings: EnhanceSettings = { brightness: 0, contrast: 0 };
  @Input() showLabels = true;
  @Input() showPredictions = true;
  @Input() showLabelDetails = true;

  @Output() annotationCreated = new EventEmitter<Annotation>();
  @Output() annotationUpdated = new EventEmitter<Annotation>();
  @Output() annotationDeleted = new EventEmitter<Annotation>();
  @Output() annotationSelected = new EventEmitter<Annotation | null>();
  @Output() zoomLevelChanged = new EventEmitter<number>();

  protected stage: Konva.Stage | null = null;
  protected imageLayer: Konva.Layer | null = null;
  protected annotationLayer: Konva.Layer | null = null;
  protected konvaImage: Konva.Image | null = null;
  protected htmlImage: HTMLImageElement | null = null;

  // Image transform info for coordinate conversion
  protected imageScale = 1;
  protected imageOffsetX = 0;
  protected imageOffsetY = 0;

  // Pan state
  protected isPanning = false;
  protected lastPanPosition: { x: number; y: number } | null = null;

  protected abstract canvasContainer: ElementRef<HTMLDivElement>;
  // Guard to prevent concurrent/duplicate image loads
  private _loadingImageUrl: string | null = null;

  @HostListener("window:resize")
  onWindowResize(): void {
    this.resizeStage();
  }

  ngAfterViewInit(): void {
    // Use a longer timeout to ensure the container has proper dimensions
    // after Angular's change detection and layout calculations
    setTimeout(() => {
      this.initializeStage();
      if (this.imageUrl && this.image) {
        this.loadImage();
      }
    }, 100);
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Only load image when imageUrl actually changes (it's the blob URL that matters)
    // Avoid double-loading when both image and imageUrl change in the same cycle
    if (changes["imageUrl"] && this.imageUrl && this.image) {
      if (this._loadingImageUrl !== this.imageUrl) {
        this._loadingImageUrl = this.imageUrl;
        this.loadImage();
      }
    } else if (
      changes["image"] &&
      this.image &&
      this.imageUrl &&
      !changes["imageUrl"]
    ) {
      // Only reload for image-only changes (e.g. metadata update, not a new file)
      this.loadImage();
    }

    if (changes["annotations"]) {
      this.renderAnnotations();
    }

    if (changes["selectedAnnotation"]) {
      this.highlightSelectedAnnotation();
    }

    if (changes["selectedAnnotations"]) {
      this.highlightSelectedAnnotations();
    }

    if (changes["zoomLevel"]) {
      this.applyZoom();
    }

    if (changes["hideAnnotations"]) {
      this.toggleAnnotationsVisibility();
    }

    if (changes["enhanceSettings"]) {
      this.applyEnhanceSettings();
    }

    if (changes["showLabels"] || changes["showPredictions"]) {
      this.renderAnnotations();
      // Re-apply selection highlight after re-rendering
      this.highlightSelectedAnnotations();
    }

    if (changes["showLabelDetails"]) {
      this.renderAnnotations();
      // Re-apply selection highlight after re-rendering
      this.highlightSelectedAnnotations();
    }
  }

  ngOnDestroy(): void {
    // Clean up image resources to free memory
    if (this.konvaImage) {
      this.konvaImage.destroy();
      this.konvaImage = null;
    }
    if (this.htmlImage) {
      this.htmlImage.onload = null;
      this.htmlImage.onerror = null;
      this.htmlImage.src = "";
      this.htmlImage = null;
    }
    if (this.stage) {
      this.stage.destroy();
      this.stage = null;
    }
    this.imageLayer = null;
    this.annotationLayer = null;
  }

  /**
   * Initialize Konva stage
   */
  protected initializeStage(): void {
    const container = this.canvasContainer.nativeElement;
    const width = container.clientWidth || 800;
    const height = container.clientHeight || 600;

    if (this.stage) {
      this.stage.destroy();
    }

    this.stage = new Konva.Stage({
      container: container,
      width: width,
      height: height,
    });

    this.imageLayer = new Konva.Layer();
    this.annotationLayer = new Konva.Layer();

    this.stage.add(this.imageLayer);
    this.stage.add(this.annotationLayer);

    this.setupEventHandlers();
  }

  /**
   * Resize stage to match container dimensions
   * Preserves current zoom level and pan position
   */
  protected resizeStage(): void {
    if (!this.stage || !this.canvasContainer) return;

    const container = this.canvasContainer.nativeElement;
    const width = container.clientWidth || 800;
    const height = container.clientHeight || 600;

    // Save current zoom and position
    const currentScale = this.stage.scaleX();
    const currentPosition = this.stage.position();

    this.stage.width(width);
    this.stage.height(height);

    // Restore zoom and position to maintain view
    this.stage.scale({ x: currentScale, y: currentScale });
    this.stage.position(currentPosition);
    this.stage.batchDraw();

    // Update transformer sizes to match current zoom level
    this.updateTransformerSizes();
  }

  /**
   * Set up event handlers - can be overridden by subclasses
   */
  protected setupEventHandlers(): void {
    if (!this.stage) return;

    // Mouse down
    this.stage.on("mousedown", (e) => {
      if (this.isPanMode) {
        this.startPan(e);
      } else {
        this.handleMouseDown(e);
      }
    });

    // Mouse move
    this.stage.on("mousemove", (e) => {
      if (this.isPanning) {
        this.handlePan(e);
      } else {
        this.handleMouseMove(e);
      }
    });

    // Mouse up
    this.stage.on("mouseup", (e) => {
      if (this.isPanning) {
        this.endPan();
      } else {
        this.handleMouseUp(e);
      }
    });

    // Mouse wheel for zoom
    this.stage.on("wheel", (e) => {
      e.evt.preventDefault();
      this.handleMouseWheel(e);
    });

    // Click on empty area to deselect
    this.stage.on("click", (e) => {
      if (e.target === this.stage) {
        this.annotationSelected.emit(null);
      }
    });
  }

  /**
   * Load and display the image
   */
  protected loadImage(): void {
    if (!this.image || !this.imageLayer) {
      if (!this.imageLayer) {
        setTimeout(() => {
          if (this.image && this.imageLayer) {
            this.loadImage();
          }
        }, 50);
      }
      return;
    }

    if (!this.imageUrl) return;

    // Clean up previous image to free memory before loading new one
    if (this.konvaImage) {
      this.konvaImage.destroy();
      this.konvaImage = null;
    }
    if (this.htmlImage) {
      this.htmlImage.onload = null;
      this.htmlImage.onerror = null;
      this.htmlImage.src = "";
      this.htmlImage = null;
    }

    if (this.stage) {
      this.stage.position({ x: 0, y: 0 });
      this.stage.scale({ x: 1, y: 1 });
    }

    this.htmlImage = new window.Image();
    this.htmlImage.crossOrigin = "Anonymous";
    this.htmlImage.onload = () => {
      if (!this.htmlImage || !this.imageLayer || !this.stage) return;

      this.konvaImage = new Konva.Image({
        image: this.htmlImage!,
        x: 0,
        y: 0,
      });

      const scaleX = this.stage.width() / this.htmlImage!.width;
      const scaleY = this.stage.height() / this.htmlImage!.height;
      const scale = Math.min(scaleX, scaleY, 1);

      // Store image transform info for coordinate conversion
      this.imageScale = scale;
      this.imageOffsetX =
        (this.stage.width() - this.htmlImage!.width * scale) / 2;
      this.imageOffsetY =
        (this.stage.height() - this.htmlImage!.height * scale) / 2;

      this.konvaImage.scale({ x: scale, y: scale });
      this.konvaImage.position({ x: this.imageOffsetX, y: this.imageOffsetY });

      this.imageLayer.add(this.konvaImage);
      this.imageLayer.batchDraw();

      this.applyEnhanceSettings();
      this.renderAnnotations();
    };

    this.htmlImage.onerror = (error) => {
      console.error("Error loading image:", error);
    };

    this.htmlImage.src = this.imageUrl;
  }

  /**
   * Convert image coordinates to canvas coordinates
   */
  protected imageToCanvasCoords(
    x: number,
    y: number
  ): { x: number; y: number } {
    return {
      x: x * this.imageScale + this.imageOffsetX,
      y: y * this.imageScale + this.imageOffsetY,
    };
  }

  /**
   * Convert canvas coordinates to image coordinates
   */
  protected canvasToImageCoords(
    x: number,
    y: number
  ): { x: number; y: number } {
    return {
      x: (x - this.imageOffsetX) / this.imageScale,
      y: (y - this.imageOffsetY) / this.imageScale,
    };
  }

  /**
   * Scale a dimension from image space to canvas space
   */
  protected scaleToCanvas(value: number): number {
    return value * this.imageScale;
  }

  /**
   * Scale a dimension from canvas space to image space
   */
  protected scaleToImage(value: number): number {
    return value / this.imageScale;
  }

  /**
   * Get pointer position relative to the stage
   */
  protected getRelativePointerPosition(): { x: number; y: number } | null {
    if (!this.stage) return null;

    const transform = this.stage.getAbsoluteTransform().copy();
    transform.invert();

    const pos = this.stage.getPointerPosition();
    if (!pos) return null;

    return transform.point(pos);
  }

  // Pan functionality
  protected startPan(e: Konva.KonvaEventObject<MouseEvent>): void {
    this.isPanning = true;
    const pos = this.stage?.getPointerPosition();
    if (pos) {
      this.lastPanPosition = { x: pos.x, y: pos.y };
    }
  }

  protected handlePan(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.isPanning || !this.lastPanPosition || !this.stage) return;

    const pos = this.stage.getPointerPosition();
    if (!pos) return;

    const dx = pos.x - this.lastPanPosition.x;
    const dy = pos.y - this.lastPanPosition.y;

    const currentPos = this.stage.position();
    this.stage.position({
      x: currentPos.x + dx,
      y: currentPos.y + dy,
    });

    this.lastPanPosition = { x: pos.x, y: pos.y };
    this.stage.batchDraw();
  }

  protected endPan(): void {
    this.isPanning = false;
    this.lastPanPosition = null;
  }

  protected handleMouseWheel(e: Konva.KonvaEventObject<WheelEvent>): void {
    if (!this.stage) return;

    const oldScale = this.stage.scaleX();
    const pointer = this.stage.getPointerPosition();
    if (!pointer) return;

    const scaleBy = 1.1;
    const newScale = e.evt.deltaY < 0 ? oldScale * scaleBy : oldScale / scaleBy;

    // Limit zoom range
    if (newScale < 0.1 || newScale > 10) return;

    const mousePointTo = {
      x: (pointer.x - this.stage.x()) / oldScale,
      y: (pointer.y - this.stage.y()) / oldScale,
    };

    this.stage.scale({ x: newScale, y: newScale });

    const newPos = {
      x: pointer.x - mousePointTo.x * newScale,
      y: pointer.y - mousePointTo.y * newScale,
    };

    this.stage.position(newPos);
    this.stage.batchDraw();

    // Re-render annotations to update transformer hit areas for new zoom level
    // This is critical because Konva Transformer hit detection areas are fixed at creation time
    this.renderAnnotations();
    // Re-apply selection highlight after re-rendering
    this.highlightSelectedAnnotations();

    this.zoomLevelChanged.emit(newScale);
  }

  // Abstract methods to be implemented by subclasses
  protected abstract handleMouseDown(
    e: Konva.KonvaEventObject<MouseEvent>
  ): void;

  protected abstract handleMouseMove(
    e: Konva.KonvaEventObject<MouseEvent>
  ): void;

  protected abstract handleMouseUp(e: Konva.KonvaEventObject<MouseEvent>): void;

  protected abstract renderAnnotations(): void;

  /**
   * Get filtered annotations based on showLabels and showPredictions flags
   */
  protected getFilteredAnnotations(): Annotation[] {
    let filtered: Annotation[] = [];

    // Add Ground Truth annotations if showLabels is true
    if (this.showLabels) {
      const groundTruth = this.annotations.filter(
        (a) =>
          a.annotationType === "Ground Truth" ||
          a.annotationType === "Ground truth"
      );
      filtered = filtered.concat(groundTruth);
    }

    // Add Prediction annotations if showPredictions is true
    if (this.showPredictions) {
      const predictions = this.annotations.filter(
        (a) => a.annotationType === "Prediction"
      );
      filtered = filtered.concat(predictions);
    }

    return filtered;
  }

  // Common methods that can be overridden
  protected highlightSelectedAnnotation(): void {
    // Default implementation - can be overridden
  }

  protected highlightSelectedAnnotations(): void {
    // Default implementation - can be overridden by subclasses
  }

  protected applyZoom(): void {
    if (!this.stage) return;
    this.stage.scale({ x: this.zoomLevel, y: this.zoomLevel });
    this.stage.batchDraw();
    // Re-render annotations to update transformer hit areas for new zoom level
    this.renderAnnotations();
    // Re-apply selection highlight after re-rendering
    this.highlightSelectedAnnotations();
  }

  /**
   * Update transformer anchor sizes based on current zoom level
   * Should be overridden by subclasses that use transformers
   */
  protected updateTransformerSizes(): void {
    // Re-render annotations to ensure transformer hit areas match current zoom level
    // This is necessary because Konva Transformer hit detection areas are fixed at creation time
    this.renderAnnotations();
    // Re-apply selection highlight after re-rendering
    this.highlightSelectedAnnotations();
  }

  protected toggleAnnotationsVisibility(): void {
    if (!this.annotationLayer) return;
    this.annotationLayer.visible(!this.hideAnnotations);
    this.annotationLayer.batchDraw();
  }

  protected applyEnhanceSettings(): void {
    if (!this.konvaImage) return;

    // Only apply filters if settings are non-default to avoid unnecessary caching
    if (
      this.enhanceSettings.brightness === 0 &&
      this.enhanceSettings.contrast === 0
    ) {
      this.konvaImage.clearCache();
      this.konvaImage.filters([]);
      this.imageLayer?.batchDraw();
      return;
    }

    this.konvaImage.cache();
    this.konvaImage.filters([Konva.Filters.Brighten, Konva.Filters.Contrast]);
    this.konvaImage.brightness(this.enhanceSettings.brightness / 100);
    this.konvaImage.contrast(this.enhanceSettings.contrast);

    this.imageLayer?.batchDraw();
  }
}
