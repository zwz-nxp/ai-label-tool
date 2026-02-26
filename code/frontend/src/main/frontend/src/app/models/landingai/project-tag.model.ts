export interface ProjectTag {
  id?: number;
  name: string;
  project?: { id: number };
  createdAt?: string;
  createdBy?: string;
}

// Helper function to get display name
export function getTagName(tag: ProjectTag): string {
  return tag.name;
}

// Helper function to get project id
export function getTagProjectId(tag: ProjectTag): number {
  return tag.project?.id || 0;
}
