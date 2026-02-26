import { createReducer, on } from "@ngrx/store";
import { ProjectListItem } from "app/models/landingai/project";
import * as HomeActions from "./home.actions";

export interface HomeState {
  projects: ProjectListItem[];
  loading: boolean;
  error: string | null;
}

export const homeInitialState: HomeState = {
  projects: [],
  loading: false,
  error: null,
};

export const homeReducer = createReducer(
  homeInitialState,

  // Load Projects
  on(
    HomeActions.loadProjects,
    (state): HomeState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),
  on(
    HomeActions.loadProjectsSuccess,
    (state, { projects }): HomeState => ({
      ...state,
      loading: false,
      projects,
      error: null,
    })
  ),
  on(
    HomeActions.loadProjectsFailure,
    (state, { error }): HomeState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Create Project
  on(
    HomeActions.createProject,
    (state): HomeState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),
  on(
    HomeActions.createProjectSuccess,
    (state): HomeState => ({
      ...state,
      loading: false,
      error: null,
    })
  ),
  on(
    HomeActions.createProjectFailure,
    (state, { error }): HomeState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Upload Images
  on(
    HomeActions.uploadImages,
    (state): HomeState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),
  on(
    HomeActions.uploadImagesSuccess,
    (state): HomeState => ({
      ...state,
      loading: false,
      error: null,
    })
  ),
  on(
    HomeActions.uploadImagesFailure,
    (state, { error }): HomeState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Update Project
  on(
    HomeActions.updateProject,
    (state): HomeState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),
  on(
    HomeActions.updateProjectSuccess,
    (state, { project }): HomeState => ({
      ...state,
      loading: false,
      projects: state.projects.map((p) =>
        p.id === project.id
          ? {
              ...p,
              name: project.name,
              type: project.type,
              modelName: project.modelName,
              groupName: project.groupName,
            }
          : p
      ),
      error: null,
    })
  ),
  on(
    HomeActions.updateProjectFailure,
    (state, { error }): HomeState => ({
      ...state,
      loading: false,
      error,
    })
  ),

  // Delete Project
  on(
    HomeActions.deleteProject,
    (state): HomeState => ({
      ...state,
      loading: true,
      error: null,
    })
  ),
  on(
    HomeActions.deleteProjectSuccess,
    (state, { id }): HomeState => ({
      ...state,
      loading: false,
      projects: state.projects.filter((p) => p.id !== id),
      error: null,
    })
  ),
  on(
    HomeActions.deleteProjectFailure,
    (state, { error }): HomeState => ({
      ...state,
      loading: false,
      error,
    })
  )
);
