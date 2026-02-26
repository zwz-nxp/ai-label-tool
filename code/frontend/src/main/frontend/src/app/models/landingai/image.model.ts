import { Annotation } from "./annotation.model";

export interface ImageLabeler {
  personId: number;
  name: string;
  labeledAt: Date;
}

export interface Image {
  id: number;
  projectId: number;
  fileName: string;
  filePath: string;
  fileSize: number;
  width: number;
  height: number;
  split: "Unassigned" | "Train" | "Dev" | "Test";
  isNoClass: boolean;
  createdAt: Date;
  createdBy: string;
  annotations?: Annotation[];
  // Additional fields for General block display
  labelers?: ImageLabeler[];
  lastLabeledAt?: Date;
}
