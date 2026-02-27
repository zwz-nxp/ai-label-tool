export interface Point {
  x: number;
  y: number;
}

export enum AnnotationType {
  Rectangle = "RECTANGLE",
  OBB = "OBB",
  Polygon = "POLYGON",
  Ellipse = "ELLIPSE",
  Brush = "BRUSH",
  Polyline = "POLYLINE",
  Classification = "CLASSIFICATION",
}

/**
 * OBB (Oriented Bounding Box) corner points in Point-based format.
 * Four corners ordered sequentially (clockwise or counter-clockwise).
 * Coordinates are stored in pixel space; normalized to 0-1 for YOLO export.
 */
export interface OBBPoints {
  x1: number;
  y1: number;
  x2: number;
  y2: number;
  x3: number;
  y3: number;
  x4: number;
  y4: number;
}

export interface Annotation {
  id: number;
  label: string;
  type: AnnotationType;
  color: string;
  x: number;
  y: number;
  width: number;
  height: number;
  points: Point[];
  classId: number;
  className: string;
  annotationType?: string; // "Ground Truth" or "Prediction"
  confidenceRate?: number; // Confidence percentage for predictions
  isPrediction?: boolean; // Helper flag for UI
  obbPoints?: OBBPoints; // OBB corner points (Point-based format)
}
