export type ProjectType =
  | "Object Detection"
  | "Segmentation"
  | "Classification";

export type ProjectGroupName = "WT" | "FE" | "BE" | "QA" | "AT";

export interface Project {
  id: number;
  name: string;
  status: "Upload" | "Label" | "Train" | "Predict";
  type: ProjectType;
  modelName: string;
  groupName?: ProjectGroupName;
  locationId: number;
  location?: Location;
  createdAt: Date;
  createdBy: string;

  // Related objects (optional, loaded when needed)
  projectClasses?: ProjectClass[];
  projectTags?: ProjectTag[];
  projectMetadata?: ProjectMetadata[];
}

export interface Location {
  id: number;
  name: string;
  code: string;
}

export interface ProjectListItem {
  id: number;
  name: string;
  type: ProjectType;
  modelName: string;
  groupName?: ProjectGroupName;
  createdBy: string;
  createdAt: Date;
  imageCount: number;
  labelCount: number;
  modelCount: number;
  thumbnailUrl: string;
}

export interface ProjectCreateRequest {
  name: string;
  type: ProjectType;
  groupName?: ProjectGroupName;
}

export interface ProjectClass {
  id: number;
  projectId: number;
  project?: Project;
  className: string;
  description: string;
  colorCode: string;
  createdAt: Date;
  createdBy: string;
}

export interface ProjectTag {
  id: number;
  projectId: number;
  project?: Project;
  name: string;
  createdAt: Date;
  createdBy: string;
}

export interface ProjectMetadata {
  id: number;
  projectId: number;
  project?: Project;
  name: string;
  type: string; // TEXT, NUMBER, BOOLEAN
  valueFrom: string; // PREDEFINED, INPUT
  predefinedValues: string;
  multipleValues: boolean;
  createdAt: Date;
  createdBy: string;
}

export interface ProjectSplit {
  id: number;
  projectId: number;
  project?: Project;
  trainRatio: number;
  devRatio: number;
  testRatio: number;
  classId?: number;
  projectClass?: ProjectClass;
  createdAt: Date;
  createdBy: string;
}
