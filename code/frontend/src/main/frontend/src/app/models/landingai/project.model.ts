export interface Project {
  id: number;
  name: string;
  status: "Upload" | "Label" | "Train" | "Predict";
  type: "Object Detection" | "Segmentation" | "Classification";
  modelName?: string;
  groupName?: string; // WT, FE, BE, QA
  locationId?: number;
  createdAt: Date;
  createdBy: string;
}
