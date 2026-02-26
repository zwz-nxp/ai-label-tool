export interface ProjectClass {
  id: number;
  className: string;
  colorCode: string;
  description?: string;
  project?: { id: number };
  createdAt?: string;
  createdBy?: string;
  labelCount?: number;
}

// Helper function to get display name
export function getClassName(cls: ProjectClass): string {
  return cls.className;
}

// Helper function to get color
export function getClassColor(cls: ProjectClass): string {
  return cls.colorCode;
}

// Helper function to get project id
export function getClassProjectId(cls: ProjectClass): number {
  return cls.project?.id || 0;
}
