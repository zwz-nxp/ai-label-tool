export interface Point {
  x: number;
  y: number;
}

export enum AnnotationType {
  Rectangle = "RECTANGLE",
  Polygon = "POLYGON",
  Ellipse = "ELLIPSE",
  Brush = "BRUSH",
  Polyline = "POLYLINE",
  Classification = "CLASSIFICATION",
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
}
