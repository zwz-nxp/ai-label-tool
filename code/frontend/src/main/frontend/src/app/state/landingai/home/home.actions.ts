import { createAction, props } from "@ngrx/store";
import {
  Project,
  ProjectCreateRequest,
  ProjectListItem,
} from "app/models/landingai/project";
import { ImageUploadResult } from "app/models/landingai/image";

// Load Projects Actions
export const loadProjects = createAction(
  "[LandingAI] Load Projects",
  props<{ viewAll: boolean }>()
);

export const loadProjectsSuccess = createAction(
  "[LandingAI] Load Projects Success",
  props<{ projects: ProjectListItem[] }>()
);

export const loadProjectsFailure = createAction(
  "[LandingAI] Load Projects Failure",
  props<{ error: string }>()
);

// Create Project Actions
export const createProject = createAction(
  "[LandingAI] Create Project",
  props<{ request: ProjectCreateRequest }>()
);

export const createProjectSuccess = createAction(
  "[LandingAI] Create Project Success",
  props<{ project: Project }>()
);

export const createProjectFailure = createAction(
  "[LandingAI] Create Project Failure",
  props<{ error: string }>()
);

// Upload Images Actions
export const uploadImages = createAction(
  "[LandingAI] Upload Images",
  props<{ files: File[]; projectId: number }>()
);

export const uploadImagesSuccess = createAction(
  "[LandingAI] Upload Images Success",
  props<{ results: ImageUploadResult[] }>()
);

export const uploadImagesFailure = createAction(
  "[LandingAI] Upload Images Failure",
  props<{ error: string }>()
);

// Update Project Actions
export const updateProject = createAction(
  "[LandingAI] Update Project",
  props<{ id: number; name: string; modelName: string; groupName?: string }>()
);

export const updateProjectSuccess = createAction(
  "[LandingAI] Update Project Success",
  props<{ project: Project }>()
);

export const updateProjectFailure = createAction(
  "[LandingAI] Update Project Failure",
  props<{ error: string }>()
);

// Delete Project Actions
export const deleteProject = createAction(
  "[LandingAI] Delete Project",
  props<{ id: number }>()
);

export const deleteProjectSuccess = createAction(
  "[LandingAI] Delete Project Success",
  props<{ id: number }>()
);

export const deleteProjectFailure = createAction(
  "[LandingAI] Delete Project Failure",
  props<{ error: string }>()
);
