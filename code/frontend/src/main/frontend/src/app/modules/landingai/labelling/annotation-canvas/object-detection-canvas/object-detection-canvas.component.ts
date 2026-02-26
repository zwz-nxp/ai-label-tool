import { Component, ElementRef, ViewChild } from "@angular/core";
import Konva from "konva";
import {
  Annotation,
  AnnotationType,
} from "app/models/landingai/annotation.model";
import { BaseAnnotationCanvasComponent } from "../base-annotation-canvas";

/**
 * Canvas component for Object Detection projects
 * Supports bounding box annotations
 */
@Component({
  selector: "app-object-detection-canvas",
  templateUrl: "./object-detection-canvas.component.html",
  styleUrls: ["./object-detection-canvas.component.scss"],
  standalone: false,
})
export class ObjectDetectionCanvasComponent extends BaseAnnotationCanvasComponent {
  @ViewChild("canvasContainer", { static: true })
  canvasContainer!: ElementRef<HTMLDivElement>;

  private isDrawing = false;
  private currentShape: Konva.Rect | null = null;

  protected override setupEventHandlers(): void {
    super.setupEventHandlers();

    // Double click to delete annotation (only for Ground Truth annotations)
    if (this.stage) {
      this.stage.on("dblclick", (e) => {
        const target = e.target;
        if (target && target !== this.stage) {
          const annotation = target.getAttr("annotation") as Annotation;
          // Only allow deletion for Ground Truth annotations
          if (annotation && annotation.annotationType !== "Prediction") {
            this.annotationDeleted.emit(annotation);
          }
        }
      });
    }
  }

  protected handleMouseDown(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (this.activeTool !== "boundingBox" || !this.selectedClass) return;

    const pos = this.getRelativePointerPosition();
    if (!pos || !this.annotationLayer) return;

    this.isDrawing = true;

    const stageScale = this.stage?.scaleX() || 1;
    this.currentShape = new Konva.Rect({
      x: pos.x,
      y: pos.y,
      width: 0,
      height: 0,
      stroke: this.selectedClass.colorCode,
      strokeWidth: 2 / stageScale,
      fill: this.selectedClass.colorCode + "20",
    });

    this.annotationLayer.add(this.currentShape);
    this.annotationLayer.batchDraw();
  }

  protected handleMouseMove(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.isDrawing || !this.currentShape) return;

    const pos = this.getRelativePointerPosition();
    if (!pos) return;

    const startX = this.currentShape.x();
    const startY = this.currentShape.y();

    this.currentShape.width(pos.x - startX);
    this.currentShape.height(pos.y - startY);

    this.annotationLayer?.batchDraw();
  }

  protected handleMouseUp(e: Konva.KonvaEventObject<MouseEvent>): void {
    if (!this.isDrawing || !this.currentShape || !this.selectedClass) {
      this.isDrawing = false;
      return;
    }

    const width = this.currentShape.width();
    const height = this.currentShape.height();

    // Ignore very small boxes
    if (Math.abs(width) < 10 || Math.abs(height) < 10) {
      this.currentShape.destroy();
      this.annotationLayer?.batchDraw();
      this.isDrawing = false;
      return;
    }

    // Normalize negative dimensions
    let canvasX = this.currentShape.x();
    let canvasY = this.currentShape.y();
    let normalizedWidth = width;
    let normalizedHeight = height;

    if (width < 0) {
      canvasX = canvasX + width;
      normalizedWidth = Math.abs(width);
    }
    if (height < 0) {
      canvasY = canvasY + height;
      normalizedHeight = Math.abs(height);
    }

    // Convert canvas coordinates to image coordinates for storage
    const imageCoords = this.canvasToImageCoords(canvasX, canvasY);

    const annotation: Annotation = {
      id: 0,
      label: this.selectedClass.className,
      type: AnnotationType.Rectangle,
      color: this.selectedClass.colorCode,
      x: imageCoords.x,
      y: imageCoords.y,
      width: this.scaleToImage(normalizedWidth),
      height: this.scaleToImage(normalizedHeight),
      points: [],
      classId: this.selectedClass.id,
      className: this.selectedClass.className,
      annotationType: "Ground truth",
    };

    this.annotationCreated.emit(annotation);

    this.currentShape.destroy();
    this.currentShape = null;
    this.isDrawing = false;
    this.annotationLayer?.batchDraw();
  }

  protected renderAnnotations(): void {
    if (!this.annotationLayer) return;

    this.annotationLayer.destroyChildren();

    this.getFilteredAnnotations().forEach((annotation) => {
      if (annotation.type === AnnotationType.Rectangle) {
        this.renderRectangle(annotation);
      }
    });

    this.annotationLayer.batchDraw();
  }

  protected override highlightSelectedAnnotation(): void {
    if (!this.annotationLayer || !this.stage) return;

    const stageScale = this.stage.scaleX();
    const baseAnchorSize = 10;
    const baseStrokeWidth = 1;
    // Inversely scale to keep consistent screen-pixel size
    const anchorSize = baseAnchorSize / stageScale;

    this.annotationLayer.children.forEach((child) => {
      if (child instanceof Konva.Group) {
        const transformer = child.findOne("Transformer") as Konva.Transformer;
        const rect = child.findOne("Rect") as Konva.Rect;
        const annotation = child.getAttr("annotation") as Annotation;
        if (annotation) {
          const isSelected = this.selectedAnnotation?.id === annotation.id;
          const isPrediction = annotation.annotationType === "Prediction";
          // Only show transformer for Ground Truth annotations
          if (transformer && !isPrediction) {
            transformer.visible(isSelected);
            // Update transformer sizes for current zoom level
            if (isSelected) {
              transformer.anchorSize(anchorSize);
              transformer.anchorStrokeWidth(0);
              transformer.borderStrokeWidth(baseStrokeWidth / stageScale);
              transformer.anchorCornerRadius(anchorSize / 4);
              // Keep anchors hidden
              transformer
                .find(
                  ".top-left, .top-center, .top-right, .middle-left, .middle-right, .bottom-left, .bottom-center, .bottom-right"
                )
                .forEach((anchor: any) => {
                  anchor.visible(false);
                });
              transformer.forceUpdate();
            }
          }
          // Apply highlight effect for single selection with zoom-aware stroke width
          if (rect) {
            const rectBaseStroke = isSelected ? 3 : 2;
            rect.strokeWidth(rectBaseStroke / stageScale);
          }
          // Show label when selected, even if showLabelDetails is off
          const labelBg = child.findOne(".labelBg");
          const labelText = child.findOne(".labelText");
          if (labelBg) labelBg.visible(this.showLabelDetails || isSelected);
          if (labelText) labelText.visible(this.showLabelDetails || isSelected);
        }
      }
    });

    this.annotationLayer.batchDraw();
  }

  protected override highlightSelectedAnnotations(): void {
    if (!this.annotationLayer || !this.stage) return;

    const selectedIds = this.selectedAnnotations.map((a) => a.id);
    const stageScale = this.stage.scaleX();
    const baseAnchorSize = 10;
    const baseStrokeWidth = 1;
    // Inversely scale to keep consistent screen-pixel size
    const anchorSize = baseAnchorSize / stageScale;

    this.annotationLayer.children.forEach((child) => {
      if (child instanceof Konva.Group) {
        const transformer = child.findOne("Transformer") as Konva.Transformer;
        const rect = child.findOne("Rect") as Konva.Rect;
        const annotation = child.getAttr("annotation") as Annotation;
        if (annotation) {
          const isSelected = selectedIds.includes(annotation.id);
          const isPrediction = annotation.annotationType === "Prediction";
          // Only show transformer for Ground Truth annotations
          if (transformer && !isPrediction) {
            transformer.visible(isSelected);
            // Update transformer sizes for current zoom level
            if (isSelected) {
              transformer.anchorSize(anchorSize);
              transformer.anchorStrokeWidth(0);
              transformer.borderStrokeWidth(baseStrokeWidth / stageScale);
              transformer.anchorCornerRadius(anchorSize / 4);
              // Keep anchors hidden
              transformer
                .find(
                  ".top-left, .top-center, .top-right, .middle-left, .middle-right, .bottom-left, .bottom-center, .bottom-right"
                )
                .forEach((anchor: any) => {
                  anchor.visible(false);
                });
              transformer.forceUpdate();
            }
          }
          // Apply highlight effect: thicker border for selected with zoom-aware stroke width
          // Keep dashed style for Prediction annotations
          if (rect) {
            const rectBaseStroke = isSelected ? 3 : 2;
            rect.strokeWidth(rectBaseStroke / stageScale);
            // Only apply selection dash for Ground Truth, Prediction keeps its dashed style
            if (!isPrediction) {
              const dashSize = 8 / stageScale;
              const dashGap = 4 / stageScale;
              rect.dash(isSelected ? [dashSize, dashGap] : []);
            }
            rect.shadowEnabled(isSelected);
            if (isSelected) {
              rect.shadowColor("#ffffff");
              rect.shadowBlur(10 / stageScale);
              rect.shadowOpacity(0.8);
            }
          }
          // Show label when selected, even if showLabelDetails is off
          const labelBg = child.findOne(".labelBg");
          const labelText = child.findOne(".labelText");
          if (labelBg) labelBg.visible(this.showLabelDetails || isSelected);
          if (labelText) labelText.visible(this.showLabelDetails || isSelected);
        }
      }
    });

    this.annotationLayer.batchDraw();
  }

  /**
   * Update transformer anchor sizes based on current zoom level
   * This ensures resize handles remain usable at any zoom level
   */
  protected override updateTransformerSizes(): void {
    // Re-render annotations to ensure transformer hit areas match current zoom level
    // This is necessary because Konva Transformer hit detection areas are fixed at creation time
    // and don't automatically update when the stage is scaled
    this.renderAnnotations();
    // Re-apply selection highlight after re-rendering
    this.highlightSelectedAnnotations();
  }

  private renderRectangle(annotation: Annotation): void {
    if (!this.annotationLayer) return;

    // Convert image coordinates to canvas coordinates for display
    const canvasPos = this.imageToCanvasCoords(annotation.x, annotation.y);
    const canvasWidth = this.scaleToCanvas(annotation.width);
    const canvasHeight = this.scaleToCanvas(annotation.height);

    // Check if this is a Prediction annotation (not editable, dashed border)
    const isPrediction = annotation.annotationType === "Prediction";

    // Prediction annotations are not draggable but can be selected
    const group = new Konva.Group({
      draggable: !isPrediction,
      // Constrain dragging within image bounds
      // Must dynamically get bounds to account for stage zoom/pan
      dragBoundFunc: !isPrediction
        ? (pos) => {
            // Get current image bounds in canvas coordinates (without stage transform)
            const imageBounds = this.getImageBoundsInCanvas();

            // Get current rect dimensions (may have been scaled by transformer)
            const rectScaleX = rect.scaleX();
            const rectScaleY = rect.scaleY();
            const rectWidth = rect.width() * rectScaleX;
            const rectHeight = rect.height() * rectScaleY;

            // Get stage transform to convert between screen and canvas coordinates
            const stageScale = this.stage?.scaleX() || 1;
            const stagePos = this.stage?.position() || { x: 0, y: 0 };

            // Convert pos (screen coords) to canvas coords
            const canvasPosX = (pos.x - stagePos.x) / stageScale;
            const canvasPosY = (pos.y - stagePos.y) / stageScale;

            // Calculate the allowed range for the group position in canvas coords
            // The rect's position is relative to the group, so we need to account for that
            const minX = imageBounds.x - rect.x();
            const maxX =
              imageBounds.x + imageBounds.width - rectWidth - rect.x();
            const minY = imageBounds.y - rect.y();
            const maxY =
              imageBounds.y + imageBounds.height - rectHeight - rect.y();

            // Clamp to bounds in canvas coords
            const clampedCanvasX = Math.max(minX, Math.min(maxX, canvasPosX));
            const clampedCanvasY = Math.max(minY, Math.min(maxY, canvasPosY));

            // Convert back to screen coords
            return {
              x: clampedCanvasX * stageScale + stagePos.x,
              y: clampedCanvasY * stageScale + stagePos.y,
            };
          }
        : undefined,
    });

    // Calculate stroke width based on current stage scale for consistent visual appearance
    // Divide by stageScale to keep visual size consistent when zoomed
    const stageScale = this.stage?.scaleX() || 1;
    const baseStrokeWidth = 2;
    const strokeWidth = baseStrokeWidth / stageScale;
    const dashSize = 8 / stageScale;
    const dashGap = 4 / stageScale;

    const rect = new Konva.Rect({
      x: canvasPos.x,
      y: canvasPos.y,
      width: canvasWidth,
      height: canvasHeight,
      stroke: annotation.color,
      strokeWidth: strokeWidth,
      fill: annotation.color + "20",
      // Dashed border for Prediction, solid for Ground Truth
      dash: isPrediction ? [dashSize, dashGap] : [],
      dashEnabled: isPrediction,
    });

    // Build label text
    let labelContent = annotation.label || annotation.className || "Unlabeled";
    // Include confidenceRate for Prediction annotations (display value directly without %)
    if (isPrediction && annotation.confidenceRate !== undefined) {
      labelContent += ` (${annotation.confidenceRate})`;
    }

    // Label text
    const labelText = new Konva.Text({
      text: labelContent,
      fontSize: 14,
      fontFamily: "Arial",
      fill: "#ffffff",
      padding: 4,
    });

    // Position label based on annotation type:
    // - Ground Truth: top-left corner (above the box)
    // - Prediction: bottom-right corner (below the box)
    let labelX: number;
    let labelY: number;

    if (isPrediction) {
      // Bottom-right corner for Prediction
      labelX = canvasPos.x + canvasWidth - labelText.width();
      labelY = canvasPos.y + canvasHeight + 2;
    } else {
      // Top-left corner for Ground Truth
      labelX = canvasPos.x;
      labelY = canvasPos.y - labelText.height() - 2;
    }

    labelText.x(labelX);
    labelText.y(labelY);

    const labelBg = new Konva.Rect({
      x: labelX,
      y: labelY,
      width: labelText.width(),
      height: labelText.height(),
      fill: annotation.color,
      opacity: 0.9,
      cornerRadius: 3,
    });

    group.add(rect);

    // Always add label elements to group, control visibility based on showLabelDetails and selection state
    const isSelected =
      this.selectedAnnotation?.id === annotation.id ||
      this.selectedAnnotations.some((a) => a.id === annotation.id);
    labelBg.name("labelBg");
    labelText.name("labelText");
    labelBg.visible(this.showLabelDetails || isSelected);
    labelText.visible(this.showLabelDetails || isSelected);
    group.add(labelBg);
    group.add(labelText);

    // Only add transformer and interaction handlers for Ground Truth annotations
    if (!isPrediction) {
      // Calculate anchor size based on current stage scale
      // Konva Transformer anchors are drawn in the layer's coordinate space (canvas coords),
      // and the stage transform scales them to screen pixels.
      // To keep anchors at a consistent screen-pixel size regardless of zoom,
      // we need to INVERSELY scale the anchor size by the stage scale.
      // e.g., at 2x zoom: anchorSize = 10/2 = 5 canvas px → 5*2 = 10 screen px
      const stageScale = this.stage?.scaleX() || 1;
      const baseAnchorSize = 10;
      const baseStrokeWidth = 1;
      // Divide by stageScale to keep consistent screen-pixel size at any zoom level
      const anchorSize = baseAnchorSize / stageScale;
      const anchorStrokeWidth = baseStrokeWidth / stageScale;
      const borderStrokeWidth = baseStrokeWidth / stageScale;

      // Transformer for resizing with boundary constraints
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
        rotateEnabled: false,
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
          // Minimum size constraint
          if (newBox.width < 10 || newBox.height < 10) return oldBox;

          // Get current image bounds in canvas coordinates
          const imageBounds = this.getImageBoundsInCanvas();

          // Get current group position
          const groupPos = group.position();

          // Calculate the actual position of the new box in canvas coordinates
          const actualX = newBox.x + groupPos.x;
          const actualY = newBox.y + groupPos.y;

          // Check if the new box is within image bounds
          if (
            actualX < imageBounds.x ||
            actualY < imageBounds.y ||
            actualX + newBox.width > imageBounds.x + imageBounds.width ||
            actualY + newBox.height > imageBounds.y + imageBounds.height
          ) {
            return oldBox;
          }

          return newBox;
        },
      });
      // Check if this annotation is currently selected
      const isSelected =
        this.selectedAnnotation?.id === annotation.id ||
        this.selectedAnnotations.some((a) => a.id === annotation.id);
      // Transformer is always added but anchors are hidden by default.
      // Anchors only appear on hover over the rect border in label mode.
      transformer.visible(isSelected);
      // Hide anchors by default — they show on border hover
      transformer
        .find(
          ".top-left, .top-center, .top-right, .middle-left, .middle-right, .bottom-left, .bottom-center, .bottom-right"
        )
        .forEach((anchor: any) => {
          anchor.visible(false);
        });
      group.add(transformer);

      // Anchors are always hidden visually but logic is preserved.
      // mouseenter still triggers batchDraw for cursor/hover effects.
      rect.on("mouseenter", () => {
        if (this.isPanMode || this.isDrawing) return;
        this.annotationLayer?.batchDraw();
      });

      // Hide anchors when mouse leaves the rect
      rect.on("mouseleave", () => {
        const tr = group.findOne("Transformer") as Konva.Transformer;
        if (tr) {
          tr.find(
            ".top-left, .top-center, .top-right, .middle-left, .middle-right, .bottom-left, .bottom-center, .bottom-right"
          ).forEach((anchor: any) => {
            anchor.visible(false);
          });
          this.annotationLayer?.batchDraw();
        }
      });

      group.on("click", () => {
        transformer.visible(true);
        this.annotationLayer?.batchDraw();
        this.annotationSelected.emit(annotation);
      });

      group.on("dragend", () => {
        const groupPos = group.position();
        const newCanvasX = rect.x() + groupPos.x;
        const newCanvasY = rect.y() + groupPos.y;

        // Convert back to image coordinates
        const imageCoords = this.canvasToImageCoords(newCanvasX, newCanvasY);
        annotation.x = imageCoords.x;
        annotation.y = imageCoords.y;

        rect.x(newCanvasX);
        rect.y(newCanvasY);

        // Update label position to top-left corner for Ground Truth
        const newLabelX = newCanvasX;
        const newLabelY = newCanvasY - labelText.height() - 2;
        labelText.x(newLabelX);
        labelText.y(newLabelY);
        labelBg.x(newLabelX);
        labelBg.y(newLabelY);

        group.position({ x: 0, y: 0 });
        this.annotationLayer?.batchDraw();
        this.annotationUpdated.emit(annotation);
      });

      rect.on("transformend", () => {
        const groupPos = group.position();
        const scaleX = rect.scaleX();
        const scaleY = rect.scaleY();

        const newCanvasX = rect.x() + groupPos.x;
        const newCanvasY = rect.y() + groupPos.y;
        const newCanvasWidth = rect.width() * scaleX;
        const newCanvasHeight = rect.height() * scaleY;

        // Convert back to image coordinates
        const imageCoords = this.canvasToImageCoords(newCanvasX, newCanvasY);
        annotation.x = imageCoords.x;
        annotation.y = imageCoords.y;
        annotation.width = this.scaleToImage(newCanvasWidth);
        annotation.height = this.scaleToImage(newCanvasHeight);

        rect.x(newCanvasX);
        rect.y(newCanvasY);
        rect.width(newCanvasWidth);
        rect.height(newCanvasHeight);
        rect.scaleX(1);
        rect.scaleY(1);

        // Update label position to top-left corner for Ground Truth
        const newLabelX = newCanvasX;
        const newLabelY = newCanvasY - labelText.height() - 2;
        labelText.x(newLabelX);
        labelText.y(newLabelY);
        labelBg.x(newLabelX);
        labelBg.y(newLabelY);

        group.position({ x: 0, y: 0 });
        this.annotationLayer?.batchDraw();
        this.annotationUpdated.emit(annotation);
      });
    } else {
      // For Prediction annotations, allow selection for viewing but no editing/deletion
      group.on("click", () => {
        this.annotationSelected.emit(annotation);
      });
    }

    group.setAttr("annotation", annotation);

    this.annotationLayer.add(group);
  }

  /**
   * Get image bounds in canvas coordinates (without stage transform)
   * Used for drag boundary calculations
   */
  private getImageBoundsInCanvas(): {
    x: number;
    y: number;
    width: number;
    height: number;
  } {
    if (!this.htmlImage) {
      return { x: 0, y: 0, width: 0, height: 0 };
    }

    return {
      x: this.imageOffsetX,
      y: this.imageOffsetY,
      width: this.htmlImage.width * this.imageScale,
      height: this.htmlImage.height * this.imageScale,
    };
  }

  /**
   * Get image bounds in stage coordinates (accounting for stage transform)
   * Used for drag boundary calculations when stage is zoomed/panned
   */
  private getImageBoundsInStageCoords(): {
    x: number;
    y: number;
    width: number;
    height: number;
  } {
    if (!this.htmlImage || !this.stage) {
      return { x: 0, y: 0, width: 0, height: 0 };
    }

    const stageScale = this.stage.scaleX();
    const stagePos = this.stage.position();

    // Transform image bounds from canvas coords to stage (screen) coords
    return {
      x: this.imageOffsetX * stageScale + stagePos.x,
      y: this.imageOffsetY * stageScale + stagePos.y,
      width: this.htmlImage.width * this.imageScale * stageScale,
      height: this.htmlImage.height * this.imageScale * stageScale,
    };
  }
}
