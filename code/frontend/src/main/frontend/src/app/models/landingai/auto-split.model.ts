export interface AutoSplitStats {
  totalImagesToSplit: number;
  classStats: ClassStats[];
}

export interface ClassStats {
  classId: number;
  className: string;
  color: string;
  imageCount: number;
}

export interface AutoSplitRequest {
  projectId: number;
  includeAssigned: boolean;
  adjustAllTogether: boolean;
  trainRatio: number;
  devRatio: number;
  testRatio: number;
  classRatios: { [classId: number]: ClassRatio };
}

export interface ClassRatio {
  train: number;
  dev: number;
  test: number;
}
