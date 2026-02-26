import { Component, ElementRef, ViewChild } from "@angular/core";
import Konva from "konva";
import {
  Annotation,
  AnnotationType,
  Point,
} from "app/models/landingai/annotation.model";
import { BaseAnnotationCanvasComponent } from "../base-annotation-canvas";

/**
 * Canvas component for Segmentation projects
 * Supports polygon, brush, and polyline annotations
 */
@Component({
  selector: "app-segmentation-canvas",
  templateUrl: "./segmentation-canvas.component.html",
  styleUrls: ["./segmentation-canvas.component.scss"],
  standalone: false,
})
export class SegmentationCanvasComponent extends BaseAnnotationCanvasComponent {
  @ViewChild("canvasContainer", { static: true })
  canvasContainer!: ElementRef<HTMLDivElement>;

  private isDrawing = false;
  private currentShape: Konva.Shape | null = null;
  private polygonPoints: Point[] = [];
  private brushPoints: Point[] = [];
  private polylinePoints: Point[] = [];

  protected override setupEventHandlers(): void {
    super.setupEventHandlers();

    if (this.stage) {
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
    }
  }

  protected handleMouseDown(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.selectedClass) return;

    const pos = this.getRelativePointerPosition();
    if (!pos) return;

    switch (this.activeTool) {
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

  protected handleMouseMove(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.isDrawing) return;

    const pos = this.getRelativePointerPosition();
    if (!pos) return;

    if (this.activeTool === "brush") {
      this.updateBrush(pos);
    }
  }

  protected handleMouseUp(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (this.activeTool === "brush" && this.isDrawing) {
      this.completeBrush();
    }
  }

  protected renderAnnotations(): void {
    if (!this.annotationLayer) return;

    this.annotationLayer.destroyChildren();

    this.getFilteredAnnotations().forEach((annotation) => {
      switch (annotation.type) {
        case AnnotationType.Polygon:
          this.renderPolygon(annotation);
          break;
        case AnnotationType.Brush:
          this.renderBrush(annotation);
          break;
        case AnnotationType.Polyline:
          this.renderPolyline(annotation);
          break;
      }
    });

    this.annotationLayer.batchDraw();
  }

  // Polygon methods
  private addPolygonPoint(pos: { x: number; y: number }): void {
    this.polygonPoints.push({ x: pos.x, y: pos.y });
    this.drawTemporaryPolygon();
  }

  private drawTemporaryPolygon(): void {
    if (!this.annotationLayer || !this.selectedClass) return;

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

  private completePolygon(): void {
    if (this.polygonPoints.length < 3 || !this.selectedClass) return;

    // Convert canvas coordinates to image coordinates for storage
    const imagePoints = this.polygonPoints.map((point) =>
      this.canvasToImageCoords(point.x, point.y)
    );

    const annotation: Annotation = {
      id: 0,
      label: this.selectedClass.className,
      type: AnnotationType.Polygon,
      color: this.selectedClass.colorCode,
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: imagePoints,
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
      annotationType: "Ground truth",
    };

    this.annotationCreated.emit(annotation);

    if (this.currentShape) {
      this.currentShape.destroy();
      this.currentShape = null;
    }
    this.polygonPoints = [];
    this.annotationLayer?.batchDraw();
  }

  // Brush methods
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

  private completeBrush(): void {
    if (this.brushPoints.length < 2 || !this.selectedClass) {
      if (this.currentShape) {
        this.currentShape.destroy();
        this.currentShape = null;
      }
      this.isDrawing = false;
      return;
    }

    // Convert canvas coordinates to image coordinates for storage
    const imagePoints = this.brushPoints.map((point) =>
      this.canvasToImageCoords(point.x, point.y)
    );

    const annotation: Annotation = {
      id: 0,
      label: this.selectedClass.className,
      type: AnnotationType.Brush,
      color: this.selectedClass.colorCode,
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: imagePoints,
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
      annotationType: "Ground truth",
    };

    this.annotationCreated.emit(annotation);

    if (this.currentShape) {
      this.currentShape.destroy();
      this.currentShape = null;
    }
    this.brushPoints = [];
    this.isDrawing = false;
    this.annotationLayer?.batchDraw();
  }

  // Polyline methods
  private addPolylinePoint(pos: { x: number; y: number }): void {
    this.polylinePoints.push({ x: pos.x, y: pos.y });
    this.drawTemporaryPolyline();
  }

  private drawTemporaryPolyline(): void {
    if (!this.annotationLayer || !this.selectedClass) return;

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
    });

    this.annotationLayer.add(this.currentShape);
    this.annotationLayer.batchDraw();
  }

  private completePolyline(): void {
    if (this.polylinePoints.length < 2 || !this.selectedClass) return;

    // Convert canvas coordinates to image coordinates for storage
    const imagePoints = this.polylinePoints.map((point) =>
      this.canvasToImageCoords(point.x, point.y)
    );

    const annotation: Annotation = {
      id: 0,
      label: this.selectedClass.className,
      type: AnnotationType.Polyline,
      color: this.selectedClass.colorCode,
      x: 0,
      y: 0,
      width: 0,
      height: 0,
      points: imagePoints,
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
      annotationType: "Ground truth",
    };

    this.annotationCreated.emit(annotation);

    if (this.currentShape) {
      this.currentShape.destroy();
      this.currentShape = null;
    }
    this.polylinePoints = [];
    this.annotationLayer?.batchDraw();
  }

  private renderPolygon(annotation: Annotation): void {
    if (!this.annotationLayer) return;

    const group = new Konva.Group({ draggable: true });

    // Convert image coordinates to canvas coordinates for display
    const canvasPoints: number[] = [];
    annotation.points.forEach((point) => {
      const canvasCoord = this.imageToCanvasCoords(point.x, point.y);
      canvasPoints.push(canvasCoord.x, canvasCoord.y);
    });

    const polygon = new Konva.Line({
      points: canvasPoints,
      stroke: annotation.color,
      strokeWidth: 2,
      fill: annotation.color + "20",
      closed: true,
    });

    group.add(polygon);

    // Label
    const bounds = polygon.getClientRect();
    const labelX = bounds.x + bounds.width / 2 - 30;
    const labelY = bounds.y - 22;

    const labelText = new Konva.Text({
      x: labelX,
      y: labelY,
      text: annotation.label || annotation.className || "Unlabeled",
      fontSize: 14,
      fontFamily: "Arial",
      fill: "#ffffff",
      padding: 4,
    });

    const labelBg = new Konva.Rect({
      x: labelX,
      y: labelY,
      width: labelText.width(),
      height: labelText.height(),
      fill: annotation.color,
      opacity: 0.9,
      cornerRadius: 3,
    });

    group.add(labelBg);
    group.add(labelText);

    // Vertex handles
    const vertexHandles: Konva.Circle[] = [];
    annotation.points.forEach((point, index) => {
      const canvasCoord = this.imageToCanvasCoords(point.x, point.y);
      const handle = new Konva.Circle({
        x: canvasCoord.x,
        y: canvasCoord.y,
        radius: 5,
        fill: annotation.color,
        stroke: "#ffffff",
        strokeWidth: 2,
        draggable: true,
        visible: false,
      });

      handle.on("dragmove", () => {
        const newPoints = [...canvasPoints];
        newPoints[index * 2] = handle.x();
        newPoints[index * 2 + 1] = handle.y();
        polygon.points(newPoints);
        this.annotationLayer?.batchDraw();
      });

      handle.on("dragend", () => {
        const newPoints: Point[] = [];
        const pointsArray = polygon.points();
        for (let i = 0; i < pointsArray.length; i += 2) {
          // Convert back to image coordinates
          const imageCoord = this.canvasToImageCoords(
            pointsArray[i],
            pointsArray[i + 1]
          );
          newPoints.push(imageCoord);
        }
        annotation.points = newPoints;
        this.annotationUpdated.emit(annotation);
      });

      vertexHandles.push(handle);
      group.add(handle);
    });

    group.setAttr("annotation", annotation);

    group.on("click", () => {
      vertexHandles.forEach((handle) => handle.visible(true));
      this.annotationLayer?.batchDraw();
      this.annotationSelected.emit(annotation);
    });

    group.on("dragend", () => {
      const dx = group.x();
      const dy = group.y();

      // Convert delta to image space
      const imageDx = this.scaleToImage(dx);
      const imageDy = this.scaleToImage(dy);

      const newPoints: Point[] = annotation.points.map((point) => ({
        x: point.x + imageDx,
        y: point.y + imageDy,
      }));

      annotation.points = newPoints;

      // Update canvas points
      const updatedCanvasPoints: number[] = [];
      newPoints.forEach((point) => {
        const canvasCoord = this.imageToCanvasCoords(point.x, point.y);
        updatedCanvasPoints.push(canvasCoord.x, canvasCoord.y);
      });
      polygon.points(updatedCanvasPoints);

      vertexHandles.forEach((handle, index) => {
        const canvasCoord = this.imageToCanvasCoords(
          newPoints[index].x,
          newPoints[index].y
        );
        handle.x(canvasCoord.x);
        handle.y(canvasCoord.y);
      });

      group.position({ x: 0, y: 0 });
      this.annotationUpdated.emit(annotation);
    });

    this.annotationLayer.add(group);
  }

  private renderBrush(annotation: Annotation): void {
    if (!this.annotationLayer) return;

    // Convert image coordinates to canvas coordinates for display
    const canvasPoints: number[] = [];
    annotation.points.forEach((point) => {
      const canvasCoord = this.imageToCanvasCoords(point.x, point.y);
      canvasPoints.push(canvasCoord.x, canvasCoord.y);
    });

    const brush = new Konva.Line({
      points: canvasPoints,
      stroke: annotation.color,
      strokeWidth: 5,
      lineCap: "round",
      lineJoin: "round",
      draggable: true,
    });

    brush.setAttr("annotation", annotation);

    brush.on("click", () => {
      this.annotationSelected.emit(annotation);
    });

    brush.on("dragend", () => {
      const dx = brush.x();
      const dy = brush.y();

      // Convert delta to image space
      const imageDx = this.scaleToImage(dx);
      const imageDy = this.scaleToImage(dy);

      const newPoints: Point[] = annotation.points.map((point) => ({
        x: point.x + imageDx,
        y: point.y + imageDy,
      }));

      annotation.points = newPoints;
      brush.position({ x: 0, y: 0 });
      this.annotationUpdated.emit(annotation);
    });

    this.annotationLayer.add(brush);
  }

  private renderPolyline(annotation: Annotation): void {
    if (!this.annotationLayer) return;

    // Convert image coordinates to canvas coordinates for display
    const canvasPoints: number[] = [];
    annotation.points.forEach((point) => {
      const canvasCoord = this.imageToCanvasCoords(point.x, point.y);
      canvasPoints.push(canvasCoord.x, canvasCoord.y);
    });

    const polyline = new Konva.Line({
      points: canvasPoints,
      stroke: annotation.color,
      strokeWidth: 2,
      lineCap: "round",
      lineJoin: "round",
      draggable: true,
    });

    polyline.setAttr("annotation", annotation);

    polyline.on("click", () => {
      this.annotationSelected.emit(annotation);
    });

    polyline.on("dragend", () => {
      const dx = polyline.x();
      const dy = polyline.y();

      // Convert delta to image space
      const imageDx = this.scaleToImage(dx);
      const imageDy = this.scaleToImage(dy);

      const newPoints: Point[] = annotation.points.map((point) => ({
        x: point.x + imageDx,
        y: point.y + imageDy,
      }));

      annotation.points = newPoints;
      polyline.position({ x: 0, y: 0 });
      this.annotationUpdated.emit(annotation);
    });

    this.annotationLayer.add(polyline);
  }
}
