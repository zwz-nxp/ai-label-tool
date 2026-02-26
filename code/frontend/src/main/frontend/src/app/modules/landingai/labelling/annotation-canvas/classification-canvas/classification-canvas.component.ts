import { Component, ElementRef, ViewChild } from "@angular/core";
import Konva from "konva";
import { Annotation } from "app/models/landingai/annotation.model";
import { BaseAnnotationCanvasComponent } from "../base-annotation-canvas";

/**
 * Canvas component for Classification projects
 * Only displays the image with zoom/pan capabilities
 * No annotation drawing - classification is done via class selection
 */
@Component({
  selector: "app-classification-canvas",
  templateUrl: "./classification-canvas.component.html",
  styleUrls: ["./classification-canvas.component.scss"],
  standalone: false,
})
export class ClassificationCanvasComponent extends BaseAnnotationCanvasComponent {
  @ViewChild("canvasContainer", { static: true })
  canvasContainer!: ElementRef<HTMLDivElement>;

  protected handleMouseDown(e: Konva.KonvaEventObject<MouseEvent>): void {
    // Classification doesn't support drawing annotations
    // Only pan mode is available
  }

  protected handleMouseMove(e: Konva.KonvaEventObject<MouseEvent>): void {
    // No drawing in classification mode
  }

  protected handleMouseUp(e: Konva.KonvaEventObject<MouseEvent>): void {
    // No drawing in classification mode
  }

  protected renderAnnotations(): void {
    if (!this.annotationLayer) return;

    this.annotationLayer.destroyChildren();

    // For classification projects, we don't display the class label on canvas
    // The class is shown in the toolbar dropdown instead
    // No need to render anything on the canvas for classification

    this.annotationLayer.batchDraw();
  }

  protected override setupEventHandlers(): void {
    super.setupEventHandlers();

    // Classification canvas is primarily for viewing
    // Override cursor to default since no drawing is supported
    if (this.canvasContainer) {
      this.canvasContainer.nativeElement.style.cursor = "default";
    }
  }

  private renderClassificationLabel(annotation: Annotation): void {
    if (!this.annotationLayer || !this.stage) return;

    // Display classification label at the top of the image
    const labelText = new Konva.Text({
      x: 10,
      y: 10,
      text: `Class: ${annotation.className || annotation.label || "Unlabeled"}`,
      fontSize: 18,
      fontFamily: "Arial",
      fill: "#ffffff",
      padding: 8,
    });

    const labelBg = new Konva.Rect({
      x: 10,
      y: 10,
      width: labelText.width(),
      height: labelText.height(),
      fill: annotation.color || "#2196F3",
      opacity: 0.9,
      cornerRadius: 5,
    });

    labelBg.setAttr("annotation", annotation);
    labelText.setAttr("annotation", annotation);

    this.annotationLayer.add(labelBg);
    this.annotationLayer.add(labelText);
  }
}
