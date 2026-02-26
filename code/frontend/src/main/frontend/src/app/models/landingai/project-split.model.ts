export interface ProjectSplit {
  id?: number;
  project?: { id: number };
  splitName: string;
  splitRatio: number;
  createdAt?: string;
  createdBy?: string;
}
