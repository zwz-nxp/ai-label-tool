import { createReducer, on } from "@ngrx/store";
import * as UserRoleActions from "./user-role.actions";
import { RoleAllowed } from "app/models/user-role";

export interface UserRoleState {
  roles: RoleAllowed[];
  loading: boolean;
  error: string | null;
}

export const userRoleInitialState: UserRoleState = {
  roles: [],
  loading: false,
  error: null,
};

export const userRoleReducer = createReducer(
  userRoleInitialState,
  on(
    UserRoleActions.loadUserRoles,
    (state): UserRoleState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    UserRoleActions.loadUserRolesSuccess,
    (state, { roles }): UserRoleState => ({
      ...state,
      loading: false,
      roles,
    })
  ),
  on(
    UserRoleActions.loadUserRolesFailure,
    (state): UserRoleState => ({
      ...state,
      loading: false,
      error: "Failed loading user roles",
    })
  )
);
