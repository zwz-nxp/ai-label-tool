export interface ProjectMetadata {
  id?: number;
  project?: { id: number };
  name: string;
  type: string; // TEXT, NUMBER, BOOLEAN
  valueFrom: string; // PREDEFINED, INPUT
  predefinedValues?: string;
  multipleValues: boolean;
  createdAt?: string;
  createdBy?: string;
}
