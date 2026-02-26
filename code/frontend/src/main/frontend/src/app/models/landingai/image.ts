import { Project, ProjectClass, ProjectMetadata, ProjectTag } from "./project";

export interface Image {
  id: number;
  projectId: number;
  project?: Project;
  fileName: string;
  filePath: string;
  fileUrl: string;
  fileSize: number;
  width: number;
  height: number;
  split: "Unassigned" | "Train" | "Dev" | "Test";
  isNoClass: boolean;
  thumbnailImage?: string; // Base64 encoded or URL
  createdAt: Date;
  createdBy: string;

  // For instances view
  instanceLabelId?: number; // ID of the label this instance represents (null for images view)
  focusedLabel?: ImageLabel; // The specific label being focused on in instances view

  // Related objects (optional, loaded when needed)
  labels?: ImageLabel[];
  tags?: ImageTag[];
  metadata?: ImageMetadata[];
}

export interface ImageLabel {
  id: number;
  imageId: number;
  image?: Image;
  classId: number;
  projectClass?: ProjectClass;
  position: string; // JSON string (Yolo/Coco format)
  confidenceRate: number;
  annotationType: string; // Ground Truth, Prediction
  createdAt: Date;
  createdBy: string;
}

export interface ImageTag {
  id: number;
  imageId: number;
  image?: Image;
  tagId: number;
  projectTag?: ProjectTag;
  createdAt: Date;
  createdBy: string;
}

export interface ImageMetadata {
  id: number;
  imageId: number;
  image?: Image;
  metadataId: number;
  projectMetadata?: ProjectMetadata;
  value: string;
  createdAt: Date;
  createdBy: string;
}

export interface ImageUploadResult {
  id: number;
  fileName: string;
  fileSize: number;
  width: number;
  height: number;
  success: boolean;
  errorMessage?: string;
}
