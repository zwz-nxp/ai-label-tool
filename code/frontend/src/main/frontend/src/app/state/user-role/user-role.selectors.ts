import { createFeatureSelector, createSelector } from "@ngrx/store";
import { UserRoleState } from "./user-role.reducer";

export const selectUserRoleState =
  createFeatureSelector<UserRoleState>("userRoles");

export const selectUserRoles = createSelector(
  selectUserRoleState,
  (state: UserRoleState) => state.roles
);

export const selectUserRolesLoading = createSelector(
  selectUserRoleState,
  (state: UserRoleState) => state.loading
);

export const selectUserRolesError = createSelector(
  selectUserRoleState,
  (state: UserRoleState) => state.error
);
