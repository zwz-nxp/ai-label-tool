import { createFeatureSelector, createSelector } from "@ngrx/store";
import { HomeState } from "./home.reducer";

export const selectHomeState = createFeatureSelector<HomeState>("landingAI");

export const selectProjects = createSelector(
  selectHomeState,
  (state: HomeState) => state.projects
);

export const selectLoading = createSelector(
  selectHomeState,
  (state: HomeState) => state.loading
);

export const selectError = createSelector(
  selectHomeState,
  (state: HomeState) => state.error
);

export const selectProjectById = (projectId: number) =>
  createSelector(selectProjects, (projects) =>
    projects.find((project) => project.id === projectId)
  );
