import { Image } from "app/models/landingai/image";
import { Snapshot } from "app/services/landingai/snapshot.service";

/**
 * Filter state for snapshot images
 * Reuses the same filter structure as image-upload for consistency
 */
export interface SnapshotFilterState {
  mediaStatus?: string[]; // labeled, unlabeled
  groundTruthLabels?: number[]; // class IDs
  split?: string[]; // training, dev, test
  tags?: number[]; // tag IDs
  mediaName?: string;
  labeler?: string;
  mediaId?: string;
  metadata?: { [key: string]: string };
  noClass?: boolean;
}

/**
 * Sort criteria for snapshot images
 */
export type SnapshotSortMethod =
  | "upload_time_desc"
  | "upload_time_asc"
  | "label_time_desc"
  | "label_time_asc"
  | "name_asc"
  | "name_desc";

/**
 * Pagination state for snapshot images
 */
export interface PaginationState {
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
}

/**
 * Loading state for different operations
 */
export interface LoadingState {
  snapshots: boolean;
  images: boolean;
  operation: boolean;
}

/**
 * Main state interface for snapshot list view
 * Requirements: 1.1, 2.1, 2.3, 3.1, 3.2
 */
export interface SnapshotListState {
  // Snapshot data
  snapshots: Snapshot[];
  selectedSnapshotId: number | null;

  // Image data for selected snapshot
  images: Image[];

  // Pagination
  pagination: PaginationState;

  // Filtering and sorting
  filters: SnapshotFilterState;
  sortCriteria: SnapshotSortMethod;

  // Loading states
  loading: LoadingState;

  // Error state
  error: string | null;

  // UI state
  sidebarCollapsed: boolean;

  // Project context
  projectId: number | null;
}

/**
 * Initial state for snapshot list
 */
export const snapshotListInitialState: SnapshotListState = {
  snapshots: [],
  selectedSnapshotId: null,
  images: [],
  pagination: {
    currentPage: 0, // 0-indexed for backend compatibility
    pageSize: 20,
    totalItems: 0,
    totalPages: 0,
  },
  filters: {},
  sortCriteria: "upload_time_desc",
  loading: {
    snapshots: false,
    images: false,
    operation: false,
  },
  error: null,
  sidebarCollapsed: false,
  projectId: null,
};
