import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
} from "@angular/core";
import Konva from "konva";
import {
  Annotation,
  AnnotationType,
  Point,
} from "app/models/landingai/annotation.model";
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
 * Canvas component for image annotation using Konva
 * Supports bounding boxes, polygons, brush, and polyline annotations
 */
@Component({
  selector: "app-annotation-canvas",
  templateUrl: "./annotation-canvas.component.html",
  styleUrls: ["./annotation-canvas.component.scss"],
  standalone: false,
})
export class AnnotationCanvasComponent
  implements OnInit, AfterViewInit, OnChanges, OnDestroy
{
  @ViewChild("canvasContainer", { static: true })
  canvasContainer!: ElementRef<HTMLDivElement>;

  @Input() image: Image | null = null;
  @Input() imageUrl: string | null = null; // Blob URL for image display
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

  private stage: Konva.Stage | null = null;
  private imageLayer: Konva.Layer | null = null;
  private annotationLayer: Konva.Layer | null = null;
  private konvaImage: Konva.Image | null = null;
  private htmlImage: HTMLImageElement | null = null;

  // Drawing state
  private isDrawing = false;
  private currentShape: Konva.Shape | null = null;
  private polygonPoints: Point[] = [];
  private brushPoints: Point[] = [];
  private polylinePoints: Point[] = [];

  // Pan state
  private isPanning = false;
  private lastPanPosition: { x: number; y: number } | null = null;

  ngOnInit(): void {
    // Stage initialization will be done in ngAfterViewInit
    // to ensure the container has proper dimensions
  }

  ngAfterViewInit(): void {
    // Initialize stage after view is fully rendered
    // Use setTimeout to ensure container has proper dimensions
    setTimeout(() => {
      this.initializeStage();

      // If imageUrl is already available, load the image
      if (this.imageUrl && this.image) {
        this.loadImage();
      }
    }, 0);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["image"] && this.image) {
      this.loadImage();
    }

    if (changes["imageUrl"] && this.imageUrl) {
      this.loadImage();
    }

    if (changes["annotations"]) {
      this.renderAnnotations();
    }

    if (changes["selectedAnnotation"]) {
      this.highlightSelectedAnnotation();
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
      this.highlightSelectedAnnotation();
    }

    if (changes["showLabelDetails"]) {
      this.renderAnnotations();
      // Re-apply selection highlight after re-rendering
      this.highlightSelectedAnnotation();
    }
  }

  ngOnDestroy(): void {
    if (this.stage) {
      this.stage.destroy();
    }
  }

  /**
   * Initialize Konva stage
   */
  private initializeStage(): void {
    const container = this.canvasContainer.nativeElement;
    const width = container.clientWidth || 800; // Fallback width
    const height = container.clientHeight || 600; // Fallback height
    if (width === 0 || height === 0) {
    }

    // Destroy existing stage if any
    if (this.stage) {
      this.stage.destroy();
    }

    this.stage = new Konva.Stage({
      container: container,
      width: width,
      height: height,
    });

    // Create layers
    this.imageLayer = new Konva.Layer();
    this.annotationLayer = new Konva.Layer();

    this.stage.add(this.imageLayer);
    this.stage.add(this.annotationLayer);
    // Set up event handlers
    this.setupEventHandlers();
  }

  /**
   * Set up event handlers for the stage
   */
  private setupEventHandlers(): void {
    if (!this.stage) {
      console.error("Cannot setup event handlers: stage is null");
      return;
    }
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
      } else if (this.isDrawing) {
        this.handleMouseMove(e);
      }
    });

    // Mouse up
    this.stage.on("mouseup", (e) => {
      if (this.isPanning) {
        this.endPan();
      } else if (this.isDrawing) {
        this.handleMouseUp(e);
      }
    });

    // Mouse wheel for zoom
    this.stage.on("wheel", (e) => {
      e.evt.preventDefault();
      this.handleMouseWheel(e);
    });

    // Double click for polygon and polyline completion
    this.stage.on("dblclick", () => {
      if (this.activeTool === "polygon" && this.polygonPoints.length >= 3) {
        this.completePolygon();
      } else if (
        this.activeTool === "polyline" &&
        this.polylinePoints.length >= 2
      ) {
        this.completePolyline();
      }
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
  private loadImage(): void {
    // Check if necessary objects are initialized
    if (!this.image || !this.imageLayer) {
      // If imageLayer is not initialized yet, wait and retry
      if (!this.imageLayer) {
        // Use setTimeout to retry, giving initializeStage time to complete
        setTimeout(() => {
          if (this.image && this.imageLayer) {
            this.loadImage();
          } else {
            console.error(
              "Failed to load image: imageLayer still not initialized after retry"
            );
          }
        }, 50); // 50ms delay is enough for setTimeout(0) to complete
      }
      return;
    }

    // Check if we have a valid image URL
    if (!this.imageUrl) {
      return;
    }
    // Clear existing image
    if (this.konvaImage) {
      this.konvaImage.destroy();
    }

    // Reset stage position and scale when loading new image
    if (this.stage) {
      this.stage.position({ x: 0, y: 0 });
      this.stage.scale({ x: 1, y: 1 });
    }

    // Load HTML image
    this.htmlImage = new window.Image();
    this.htmlImage.crossOrigin = "Anonymous";
    this.htmlImage.onload = () => {
      if (!this.htmlImage || !this.imageLayer || !this.stage) {
        console.error("Image loaded but required objects are null");
        return;
      }
      // Create Konva image
      this.konvaImage = new Konva.Image({
        image: this.htmlImage!,
        x: 0,
        y: 0,
      });

      // Scale image to fit canvas while maintaining aspect ratio
      const scaleX = this.stage.width() / this.htmlImage!.width;
      const scaleY = this.stage.height() / this.htmlImage!.height;
      const scale = Math.min(scaleX, scaleY, 1);

      this.konvaImage.scale({ x: scale, y: scale });

      // Center image on canvas
      const x = (this.stage.width() - this.htmlImage!.width * scale) / 2;
      const y = (this.stage.height() - this.htmlImage!.height * scale) / 2;
      this.konvaImage.position({ x, y });
      this.imageLayer.add(this.konvaImage);
      this.imageLayer.batchDraw();
      // Apply enhance settings
      this.applyEnhanceSettings();

      // Render annotations
      this.renderAnnotations();
    };

    this.htmlImage.onerror = (error) => {
      console.error("Error loading image:", error);
      console.error("Image URL:", this.imageUrl);
      console.error("Image object:", this.image);
    };

    // Use the Blob URL provided by the parent component
    this.htmlImage.src = this.imageUrl;
  }

  /**
   * Render all annotations on the canvas
   */
  private renderAnnotations(): void {
    if (!this.annotationLayer) return;

    // Clear existing annotations
    this.annotationLayer.destroyChildren();

    // Filter annotations based on showLabels and showPredictions flags
    let annotationsToRender: Annotation[] = [];

    // Add Ground Truth annotations if showLabels is true
    if (this.showLabels) {
      const groundTruth = this.annotations.filter(
        (a) =>
          a.annotationType === "Ground Truth" ||
          a.annotationType === "Ground truth"
      );
      annotationsToRender = annotationsToRender.concat(groundTruth);
    }

    // Add Prediction annotations if showPredictions is true
    if (this.showPredictions) {
      const predictions = this.annotations.filter(
        (a) => a.annotationType === "Prediction"
      );
      annotationsToRender = annotationsToRender.concat(predictions);
    }

    // Render each annotation
    annotationsToRender.forEach((annotation) => {
      this.renderAnnotation(annotation);
    });

    this.annotationLayer.batchDraw();
  }

  /**
   * Render a single annotation
   */
  private renderAnnotation(annotation: Annotation): void {
    if (!this.annotationLayer) return;

    let shape: Konva.Shape | Konva.Group | null = null;

    switch (annotation.type) {
      case AnnotationType.Rectangle:
        shape = this.createRectangle(annotation);
        break;
      case AnnotationType.Polygon:
        shape = this.createPolygon(annotation);
        break;
      case AnnotationType.Brush:
        shape = this.createBrush(annotation);
        break;
      case AnnotationType.Polyline:
        shape = this.createPolyline(annotation);
        break;
    }

    if (shape) {
      // Store annotation reference
      shape.setAttr("annotation", annotation);

      // Make shape interactive
      shape.on("click", () => {
        this.annotationSelected.emit(annotation);
      });

      this.annotationLayer.add(shape);
    }
  }

  /**
   * Create a rectangle shape for bounding box annotation
   */
  private createRectangle(annotation: Annotation): Konva.Group {
    // Create a group to hold the rectangle, transformer, and label
    const group = new Konva.Group({
      draggable: true,
    });

    const rect = new Konva.Rect({
      x: annotation.x,
      y: annotation.y,
      width: annotation.width,
      height: annotation.height,
      stroke: annotation.color,
      strokeWidth: 2,
      fill: annotation.color + "20", // 20% opacity
    });

    // Add label text
    const labelText = new Konva.Text({
      x: annotation.x,
      y: annotation.y - 22,
      text: annotation.label || annotation.className || "Unlabeled",
      fontSize: 14,
      fontFamily: "Arial",
      fill: "#ffffff", // White text for better contrast
      padding: 4,
    });

    // Add background for label text
    const labelBg = new Konva.Rect({
      x: annotation.x,
      y: annotation.y - 22,
      width: labelText.width(),
      height: labelText.height(),
      fill: annotation.color,
      opacity: 0.9,
      cornerRadius: 3,
    });

    // Calculate anchor size based on current stage scale
    // Inversely scale to keep consistent screen-pixel size at any zoom level
    const stageScale = this.stage?.scaleX() || 1;
    const baseAnchorSize = 10;
    const baseStrokeWidth = 1;
    const anchorSize = baseAnchorSize / stageScale;
    const anchorStrokeWidth = baseStrokeWidth / stageScale;
    const borderStrokeWidth = baseStrokeWidth / stageScale;

    // Add transformer for resizing
    const transformer = new Konva.Transformer({
      nodes: [rect],
      enabledAnchors: [
        "top-left",
        "top-right",
        "bottom-left",
        "bottom-right",
        "top-center",
        "middle-right",
        "middle-left",
        "bottom-center",
      ],
      rotateEnabled: false, // Disable rotation
      anchorSize: anchorSize,
      anchorStrokeWidth: 0,
      borderStrokeWidth: borderStrokeWidth,
      anchorFill: "transparent",
      anchorStroke: "transparent",
      borderStroke: "#1890ff",
      // Increase anchor corner radius for better visual appearance
      anchorCornerRadius: anchorSize / 4,
      // CRITICAL: ignoreStroke ensures the entire anchor area is clickable
      // This fixes the issue where anchors are hard to click at high zoom levels
      ignoreStroke: true,
      boundBoxFunc: (oldBox, newBox) => {
        // Limit resize to minimum size
        if (newBox.width < 10 || newBox.height < 10) {
          return oldBox;
        }
        return newBox;
      },
    });

    // Check if this annotation is currently selected
    const isSelected =
      this.selectedAnnotation?.id === annotation.id ||
      this.selectedAnnotations.some((a) => a.id === annotation.id);
    transformer.visible(isSelected);
    // Always hide anchors — keep logic intact but make them invisible
    transformer
      .find(
        ".top-left, .top-center, .top-right, .middle-left, .middle-right, .bottom-left, .bottom-center, .bottom-right"
      )
      .forEach((anchor: any) => {
        anchor.visible(false);
      });

    group.add(rect);

    // Only add label text and background if showLabelDetails is true
    if (this.showLabelDetails) {
      group.add(labelBg);
      group.add(labelText);
    }

    group.add(transformer);

    // Show transformer when selected (anchors stay hidden)
    group.on("click", () => {
      transformer.visible(true);
      transformer
        .find(
          ".top-left, .top-center, .top-right, .middle-left, .middle-right, .bottom-left, .bottom-center, .bottom-right"
        )
        .forEach((anchor: any) => {
          anchor.visible(false);
        });
      this.annotationLayer?.batchDraw();
    });

    // Enable label editing on double-click (only if labels are shown)
    if (this.showLabelDetails) {
      labelText.on("dblclick", () => {
        this.editAnnotationLabel(annotation, labelText, labelBg);
      });
    }

    // Enable dragging - update annotation coordinates on drag end
    group.on("dragend", () => {
      const groupPos = group.position();

      // Update annotation coordinates based on group position
      annotation.x = rect.x() + groupPos.x;
      annotation.y = rect.y() + groupPos.y;

      // Update rect position to absolute coordinates
      rect.x(annotation.x);
      rect.y(annotation.y);

      // Update label position
      labelText.x(annotation.x);
      labelText.y(annotation.y - 22);
      labelBg.x(annotation.x);
      labelBg.y(annotation.y - 22);

      // Reset group position since we updated the rect coordinates
      group.position({ x: 0, y: 0 });

      this.annotationLayer?.batchDraw();
      this.annotationUpdated.emit(annotation);
    });

    // Enable resizing - update annotation size on transform end
    rect.on("transformend", () => {
      const groupPos = group.position();

      // Get the new dimensions
      const scaleX = rect.scaleX();
      const scaleY = rect.scaleY();

      // Update annotation with new size and position
      annotation.x = rect.x() + groupPos.x;
      annotation.y = rect.y() + groupPos.y;
      annotation.width = rect.width() * scaleX;
      annotation.height = rect.height() * scaleY;

      // Update rect to use absolute coordinates and reset scale
      rect.x(annotation.x);
      rect.y(annotation.y);
      rect.width(annotation.width);
      rect.height(annotation.height);
      rect.scaleX(1);
      rect.scaleY(1);

      // Update label position
      labelText.x(annotation.x);
      labelText.y(annotation.y - 22);
      labelBg.x(annotation.x);
      labelBg.y(annotation.y - 22);
      labelBg.width(labelText.width());
      labelBg.height(labelText.height());

      // Reset group position
      group.position({ x: 0, y: 0 });

      this.annotationLayer?.batchDraw();
      this.annotationUpdated.emit(annotation);
    });

    return group;
  }

  /**
   * Create a polygon shape
   */
  private createPolygon(annotation: Annotation): Konva.Group {
    // Create a group to hold the polygon and vertex handles
    const group = new Konva.Group({
      draggable: true,
    });

    const points: number[] = [];
    annotation.points.forEach((point) => {
      points.push(point.x, point.y);
    });

    const polygon = new Konva.Line({
      points: points,
      stroke: annotation.color,
      strokeWidth: 2,
      fill: annotation.color + "20", // 20% opacity
      closed: true,
    });

    group.add(polygon);

    // Calculate center point for label
    const bounds = polygon.getClientRect();
    const labelX = bounds.x + bounds.width / 2 - 30;
    const labelY = bounds.y - 22;

    // Add label text
    const labelText = new Konva.Text({
      x: labelX,
      y: labelY,
      text: annotation.label || annotation.className || "Unlabeled",
      fontSize: 14,
      fontFamily: "Arial",
      fill: "#ffffff",
      padding: 4,
    });

    // Add background for label text
    const labelBg = new Konva.Rect({
      x: labelX,
      y: labelY,
      width: labelText.width(),
      height: labelText.height(),
      fill: annotation.color,
      opacity: 0.9,
      cornerRadius: 3,
    });

    // Only add label text and background if showLabelDetails is true
    if (this.showLabelDetails) {
      group.add(labelBg);
      group.add(labelText);
    }

    // Add draggable vertex handles
    const vertexHandles: Konva.Circle[] = [];
    annotation.points.forEach((point, index) => {
      const handle = new Konva.Circle({
        x: point.x,
        y: point.y,
        radius: 5,
        fill: annotation.color,
        stroke: "#ffffff",
        strokeWidth: 2,
        draggable: true,
        visible: false, // Hidden by default
      });

      // Update polygon when vertex is dragged
      handle.on("dragmove", () => {
        const newPoints = [...points];
        newPoints[index * 2] = handle.x();
        newPoints[index * 2 + 1] = handle.y();
        polygon.points(newPoints);
        this.annotationLayer?.batchDraw();
      });

      // Update annotation when vertex drag ends
      handle.on("dragend", () => {
        const newPoints: Point[] = [];
        const pointsArray = polygon.points();
        for (let i = 0; i < pointsArray.length; i += 2) {
          newPoints.push({ x: pointsArray[i], y: pointsArray[i + 1] });
        }
        annotation.points = newPoints;
        this.annotationUpdated.emit(annotation);
      });

      vertexHandles.push(handle);
      group.add(handle);
    });

    // Show vertex handles when polygon is selected
    group.on("click", () => {
      vertexHandles.forEach((handle) => handle.visible(true));
      this.annotationLayer?.batchDraw();
    });

    // Enable dragging - update annotation position on drag end
    group.on("dragend", () => {
      const dx = group.x();
      const dy = group.y();

      // Update annotation points based on drag offset
      const newPoints: Point[] = annotation.points.map((point) => ({
        x: point.x + dx,
        y: point.y + dy,
      }));

      annotation.points = newPoints;

      // Update polygon and vertex handles
      const updatedPoints: number[] = [];
      newPoints.forEach((point) => {
        updatedPoints.push(point.x, point.y);
      });
      polygon.points(updatedPoints);

      vertexHandles.forEach((handle, index) => {
        handle.x(newPoints[index].x);
        handle.y(newPoints[index].y);
      });

      // Reset group position since we updated the points
      group.position({ x: 0, y: 0 });

      this.annotationUpdated.emit(annotation);
    });

    return group;
  }

  /**
   * Create a brush stroke
   */
  private createBrush(annotation: Annotation): Konva.Line {
    const points: number[] = [];
    annotation.points.forEach((point) => {
      points.push(point.x, point.y);
    });

    const brush = new Konva.Line({
      points: points,
      stroke: annotation.color,
      strokeWidth: 5,
      lineCap: "round",
      lineJoin: "round",
      draggable: true,
    });

    // Enable dragging - update annotation position on drag end
    brush.on("dragend", () => {
      const dx = brush.x();
      const dy = brush.y();

      // Update annotation points based on drag offset
      const newPoints: Point[] = annotation.points.map((point) => ({
        x: point.x + dx,
        y: point.y + dy,
      }));

      annotation.points = newPoints;

      // Reset brush position since we updated the points
      brush.position({ x: 0, y: 0 });

      this.annotationUpdated.emit(annotation);
    });

    return brush;
  }

  /**
   * Create a polyline
   */
  private createPolyline(annotation: Annotation): Konva.Line {
    const points: number[] = [];
    annotation.points.forEach((point) => {
      points.push(point.x, point.y);
    });

    const polyline = new Konva.Line({
      points: points,
      stroke: annotation.color,
      strokeWidth: 2,
      lineCap: "round",
      lineJoin: "round",
      draggable: true,
    });

    // Enable dragging - update annotation position on drag end
    polyline.on("dragend", () => {
      const dx = polyline.x();
      const dy = polyline.y();

      // Update annotation points based on drag offset
      const newPoints: Point[] = annotation.points.map((point) => ({
        x: point.x + dx,
        y: point.y + dy,
      }));

      annotation.points = newPoints;

      // Reset polyline position since we updated the points
      polyline.position({ x: 0, y: 0 });

      this.annotationUpdated.emit(annotation);
    });

    return polyline;
  }

  /**
   * Get pointer position relative to the stage (accounting for zoom and pan)
   */
  private getRelativePointerPosition(): { x: number; y: number } | null {
    if (!this.stage) return null;

    const transform = this.stage.getAbsoluteTransform().copy();
    transform.invert();

    const pos = this.stage.getPointerPosition();
    if (!pos) return null;

    return transform.point(pos);
  }

  /**
   * Handle mouse down event
   */
  private handleMouseDown(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.stage) {
      console.error("Stage not initialized");
      return;
    }

    if (!this.selectedClass) {
      return;
    }

    const pos = this.getRelativePointerPosition();
    if (!pos) {
      console.error("Could not get pointer position");
      return;
    }
    switch (this.activeTool) {
      case "boundingBox":
        this.startBoundingBox(pos);
        break;
      case "polygon":
        this.addPolygonPoint(pos);
        break;
      case "brush":
        this.startBrush(pos);
        break;
      case "polyline":
        this.addPolylinePoint(pos);
        break;
    }
  }

  /**
   * Handle mouse move event
   */
  private handleMouseMove(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.stage || !this.isDrawing) return;

    const pos = this.getRelativePointerPosition();
    if (!pos) return;

    switch (this.activeTool) {
      case "boundingBox":
        this.updateBoundingBox(pos);
        break;
      case "brush":
        this.updateBrush(pos);
        break;
    }
  }

  /**
   * Handle mouse up event
   */
  private handleMouseUp(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.isDrawing) return;

    switch (this.activeTool) {
      case "boundingBox":
        this.completeBoundingBox();
        break;
      case "brush":
        this.completeBrush();
        break;
    }
  }

  /**
   * Start drawing a bounding box
   */
  private startBoundingBox(pos: { x: number; y: number }): void {
    if (!this.annotationLayer || !this.selectedClass) {
      console.error(
        "Cannot start bounding box: annotationLayer or selectedClass is null"
      );
      return;
    }
    this.isDrawing = true;

    this.currentShape = new Konva.Rect({
      x: pos.x,
      y: pos.y,
      width: 0,
      height: 0,
      stroke: this.selectedClass.colorCode,
      strokeWidth: 2,
      fill: this.selectedClass.colorCode + "20",
    });

    this.annotationLayer.add(this.currentShape);
    this.annotationLayer.batchDraw();
  }

  /**
   * Update bounding box while dragging
   */
  private updateBoundingBox(pos: { x: number; y: number }): void {
    if (!this.currentShape || !(this.currentShape instanceof Konva.Rect)) {
      return;
    }

    const startX = this.currentShape.x();
    const startY = this.currentShape.y();

    const width = pos.x - startX;
    const height = pos.y - startY;

    this.currentShape.width(width);
    this.currentShape.height(height);

    this.annotationLayer?.batchDraw();
  }

  /**
   * Complete bounding box drawing
   */
  private completeBoundingBox(): void {
    if (
      !this.currentShape ||
      !(this.currentShape instanceof Konva.Rect) ||
      !this.selectedClass
    ) {
      this.isDrawing = false;
      return;
    }

    const width = this.currentShape.width();
    const height = this.currentShape.height();
    // Ignore very small boxes (likely accidental clicks)
    if (Math.abs(width) < 10 || Math.abs(height) < 10) {
      this.currentShape.destroy();
      this.annotationLayer?.batchDraw();
      this.isDrawing = false;
      return;
    }

    // Normalize negative dimensions
    let x = this.currentShape.x();
    let y = this.currentShape.y();
    let normalizedWidth = width;
    let normalizedHeight = height;

    if (width < 0) {
      x = x + width;
      normalizedWidth = Math.abs(width);
    }
    if (height < 0) {
      y = y + height;
      normalizedHeight = Math.abs(height);
    }

    // Create annotation object
    const annotation: Annotation = {
      id: 0, // Temporary ID (0 indicates new annotation), will be replaced by backend
      label: this.selectedClass.className,
      type: AnnotationType.Rectangle,
      color: this.selectedClass.colorCode,
      x: x,
      y: y,
      width: normalizedWidth,
      height: normalizedHeight,
      points: [],
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
    };
    // Emit annotation created event
    this.annotationCreated.emit(annotation);

    // Clean up temporary shape
    this.currentShape.destroy();
    this.currentShape = null;
    this.isDrawing = false;
    this.annotationLayer?.batchDraw();
  }

  /**
   * Add a point to the polygon
   */
  private addPolygonPoint(pos: { x: number; y: number }): void {
    this.polygonPoints.push({ x: pos.x, y: pos.y });

    // Draw temporary polygon
    this.drawTemporaryPolygon();
  }

  /**
   * Draw temporary polygon while creating
   */
  private drawTemporaryPolygon(): void {
    if (!this.annotationLayer || !this.selectedClass) return;

    // Remove previous temporary polygon
    if (this.currentShape) {
      this.currentShape.destroy();
    }

    if (this.polygonPoints.length < 2) return;

    const points: number[] = [];
    this.polygonPoints.forEach((point) => {
      points.push(point.x, point.y);
    });

    this.currentShape = new Konva.Line({
      points: points,
      stroke: this.selectedClass.colorCode,
      strokeWidth: 2,
      fill: this.selectedClass.colorCode + "20",
      closed: false,
    });

    this.annotationLayer.add(this.currentShape);
    this.annotationLayer.batchDraw();
  }

  /**
   * Complete polygon drawing
   */
  private completePolygon(): void {
    if (this.polygonPoints.length < 3 || !this.selectedClass) return;

    // Create annotation object
    const annotation: Annotation = {
      id: 0, // Temporary ID (0 indicates new annotation), will be replaced by backend
      label: this.selectedClass.className,
      type: AnnotationType.Polygon,
      color: this.selectedClass.colorCode,
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: [...this.polygonPoints],
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
    };

    // Emit annotation created event
    this.annotationCreated.emit(annotation);

    // Clean up
    if (this.currentShape) {
      this.currentShape.destroy();
      this.currentShape = null;
    }
    this.polygonPoints = [];
    this.annotationLayer?.batchDraw();
  }

  /**
   * Start brush drawing
   */
  private startBrush(pos: { x: number; y: number }): void {
    if (!this.annotationLayer || !this.selectedClass) return;

    this.isDrawing = true;
    this.brushPoints = [{ x: pos.x, y: pos.y }];

    this.currentShape = new Konva.Line({
      points: [pos.x, pos.y],
      stroke: this.selectedClass.colorCode,
      strokeWidth: 5,
      lineCap: "round",
      lineJoin: "round",
    });

    this.annotationLayer.add(this.currentShape);
  }

  /**
   * Update brush while drawing
   */
  private updateBrush(pos: { x: number; y: number }): void {
    if (!this.currentShape || !(this.currentShape instanceof Konva.Line))
      return;

    this.brushPoints.push({ x: pos.x, y: pos.y });

    const points: number[] = [];
    this.brushPoints.forEach((point) => {
      points.push(point.x, point.y);
    });

    this.currentShape.points(points);
    this.annotationLayer?.batchDraw();
  }

  /**
   * Complete brush drawing
   */
  private completeBrush(): void {
    if (!this.selectedClass || this.brushPoints.length < 2) {
      this.isDrawing = false;
      return;
    }

    // Create annotation object
    const annotation: Annotation = {
      id: 0, // Temporary ID (0 indicates new annotation), will be replaced by backend
      label: this.selectedClass.className,
      type: AnnotationType.Brush,
      color: this.selectedClass.colorCode,
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: [...this.brushPoints],
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
    };

    // Emit annotation created event
    this.annotationCreated.emit(annotation);

    this.currentShape = null;
    this.brushPoints = [];
    this.isDrawing = false;
  }

  /**
   * Add a point to the polyline
   */
  private addPolylinePoint(pos: { x: number; y: number }): void {
    this.polylinePoints.push({ x: pos.x, y: pos.y });

    // Draw temporary polyline
    this.drawTemporaryPolyline();
  }

  /**
   * Draw temporary polyline while creating
   */
  private drawTemporaryPolyline(): void {
    if (!this.annotationLayer || !this.selectedClass) return;

    // Remove previous temporary polyline
    if (this.currentShape) {
      this.currentShape.destroy();
    }

    if (this.polylinePoints.length < 2) return;

    const points: number[] = [];
    this.polylinePoints.forEach((point) => {
      points.push(point.x, point.y);
    });

    this.currentShape = new Konva.Line({
      points: points,
      stroke: this.selectedClass.colorCode,
      strokeWidth: 2,
      lineCap: "round",
      lineJoin: "round",
      closed: false,
    });

    this.annotationLayer.add(this.currentShape);
    this.annotationLayer.batchDraw();
  }

  /**
   * Complete polyline drawing (triggered by double-click or Enter key)
   */
  private completePolyline(): void {
    if (this.polylinePoints.length < 2 || !this.selectedClass) return;

    // Create annotation object
    const annotation: Annotation = {
      id: 0, // Temporary ID (0 indicates new annotation), will be replaced by backend
      label: this.selectedClass.className,
      type: AnnotationType.Polyline,
      color: this.selectedClass.colorCode,
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: [...this.polylinePoints],
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
    };

    // Emit annotation created event
    this.annotationCreated.emit(annotation);

    // Clean up
    if (this.currentShape) {
      this.currentShape.destroy();
      this.currentShape = null;
    }
    this.polylinePoints = [];
    this.annotationLayer?.batchDraw();
  }

  /**
   * Start panning
   */
  private startPan(e: Konva.KonvaEventObject<MouseEvent>): void {
    this.isPanning = true;
    const pos = this.stage?.getPointerPosition();
    if (pos) {
      this.lastPanPosition = { x: pos.x, y: pos.y };
    }
  }

  /**
   * Handle panning
   * Moves the stage based on mouse movement
   */
  private handlePan(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.stage || !this.lastPanPosition) return;

    const pos = this.stage.getPointerPosition();
    if (!pos) return;

    const dx = pos.x - this.lastPanPosition.x;
    const dy = pos.y - this.lastPanPosition.y;

    // Update stage position
    this.stage.x(this.stage.x() + dx);
    this.stage.y(this.stage.y() + dy);

    // Update last position for next move
    this.lastPanPosition = { x: pos.x, y: pos.y };

    this.stage.batchDraw();
  }

  /**
   * End panning
   */
  private endPan(): void {
    this.isPanning = false;
    this.lastPanPosition = null;
  }

  /**
   * Handle mouse wheel for zooming
   * Zooms towards the mouse cursor position
   */
  private handleMouseWheel(e: Konva.KonvaEventObject<WheelEvent>): void {
    if (!this.stage) return;

    const oldScale = this.stage.scaleX();
    const pointer = this.stage.getPointerPosition();

    if (!pointer) return;

    // Calculate zoom direction and amount
    const scaleBy = 1.05;
    const direction = e.evt.deltaY > 0 ? -1 : 1;

    // Calculate new scale
    let newScale = direction > 0 ? oldScale * scaleBy : oldScale / scaleBy;

    // Limit zoom range
    newScale = Math.max(0.1, Math.min(3.0, newScale));

    // Calculate mouse point relative to stage
    const mousePointTo = {
      x: (pointer.x - this.stage.x()) / oldScale,
      y: (pointer.y - this.stage.y()) / oldScale,
    };

    // Apply new scale
    this.stage.scale({ x: newScale, y: newScale });

    // Calculate new position to keep mouse point fixed
    const newPos = {
      x: pointer.x - mousePointTo.x * newScale,
      y: pointer.y - mousePointTo.y * newScale,
    };

    this.stage.position(newPos);
    this.stage.batchDraw();

    // Re-render annotations to update transformer hit areas for new zoom level
    // This is critical because Konva Transformer hit detection areas are fixed at creation time
    this.renderAnnotations();

    // Update zoom level in parent component
    this.zoomLevelChanged.emit(newScale);
  }

  /**
   * Highlight the selected annotation
   */
  private highlightSelectedAnnotation(): void {
    // Implementation to highlight selected annotation
  }

  /**
   * Apply zoom level
   * Zooms towards the center of the stage
   */
  private applyZoom(): void {
    if (!this.stage) return;

    // Get the center point of the stage
    const centerX = this.stage.width() / 2;
    const centerY = this.stage.height() / 2;

    // Get the old scale
    const oldScale = this.stage.scaleX();

    // Calculate the new position to keep the center point fixed
    const mousePointTo = {
      x: (centerX - this.stage.x()) / oldScale,
      y: (centerY - this.stage.y()) / oldScale,
    };

    // Apply new scale
    this.stage.scale({ x: this.zoomLevel, y: this.zoomLevel });

    // Calculate new position
    const newPos = {
      x: centerX - mousePointTo.x * this.zoomLevel,
      y: centerY - mousePointTo.y * this.zoomLevel,
    };

    this.stage.position(newPos);
    this.stage.batchDraw();

    // Re-render annotations to update transformer hit areas for new zoom level
    // This is critical because Konva Transformer hit detection areas are fixed at creation time
    this.renderAnnotations();
  }

  /**
   * Toggle annotations visibility
   */
  private toggleAnnotationsVisibility(): void {
    if (!this.annotationLayer) return;

    this.annotationLayer.visible(!this.hideAnnotations);
    this.annotationLayer.batchDraw();
  }

  /**
   * Edit annotation label text
   * @param annotation The annotation to edit
   * @param labelText The Konva text object
   * @param labelBg The label background rect
   */
  private editAnnotationLabel(
    annotation: Annotation,
    labelText: Konva.Text,
    labelBg: Konva.Rect
  ): void {
    // Hide the text temporarily
    labelText.hide();
    labelBg.hide();

    // Create a textarea element for editing
    const textPosition = labelText.getAbsolutePosition();
    const stageBox = this.stage!.container().getBoundingClientRect();

    const textarea = document.createElement("input");
    textarea.type = "text";
    textarea.value = annotation.label;
    textarea.style.position = "absolute";
    textarea.style.top = stageBox.top + textPosition.y + "px";
    textarea.style.left = stageBox.left + textPosition.x + "px";
    textarea.style.width = labelText.width() + "px";
    textarea.style.fontSize = "14px";
    textarea.style.border = "2px solid " + annotation.color;
    textarea.style.padding = "4px";
    textarea.style.margin = "0px";
    textarea.style.overflow = "hidden";
    textarea.style.background = "white";
    textarea.style.outline = "none";
    textarea.style.resize = "none";
    textarea.style.lineHeight = labelText.lineHeight().toString();
    textarea.style.fontFamily = labelText.fontFamily();
    textarea.style.transformOrigin = "left top";
    textarea.style.textAlign = labelText.align();
    textarea.style.color = annotation.color;

    // Apply rotation if any
    const rotation = labelText.rotation();
    let transform = "";
    if (rotation) {
      transform += "rotateZ(" + rotation + "deg)";
    }

    const px = 0;
    const isFirefox = navigator.userAgent.toLowerCase().indexOf("firefox") > -1;
    if (isFirefox) {
      transform += "translateY(-" + px + "px)";
    }

    textarea.style.transform = transform;

    // Add to DOM
    document.body.appendChild(textarea);
    textarea.focus();
    textarea.select();

    // Handle blur and Enter key
    const removeTextarea = () => {
      textarea.parentNode?.removeChild(textarea);
      labelText.show();
      labelBg.show();
      this.annotationLayer?.batchDraw();
    };

    const saveText = () => {
      const newText = textarea.value.trim();
      if (newText && newText !== annotation.label) {
        annotation.label = newText;
        labelText.text(newText);
        labelBg.width(labelText.width());
        this.annotationUpdated.emit(annotation);
      }
      removeTextarea();
    };

    textarea.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        saveText();
      } else if (e.key === "Escape") {
        removeTextarea();
      }
    });

    textarea.addEventListener("blur", () => {
      saveText();
    });
  }

  /**
   * Apply enhance settings (brightness and contrast)
   */
  private applyEnhanceSettings(): void {
    if (!this.konvaImage) return;

    // Apply filters
    this.konvaImage.cache();
    this.konvaImage.filters([Konva.Filters.Brighten, Konva.Filters.Contrast]);

    // Brightness: -100 to +100 -> -1 to +1
    const brightness = this.enhanceSettings.brightness / 100;
    this.konvaImage.brightness(brightness);

    // Contrast: -100 to +100 -> -100 to +100
    this.konvaImage.contrast(this.enhanceSettings.contrast);

    this.imageLayer?.batchDraw();
  }

  /**
   * Update transformer anchor sizes based on current zoom level
   * This ensures resize handles remain usable at any zoom level
   */
  private updateTransformerSizes(): void {
    // Re-render annotations to ensure transformer hit areas match current zoom level
    // This is necessary because Konva Transformer hit detection areas are fixed at creation time
    this.renderAnnotations();
  }
}
