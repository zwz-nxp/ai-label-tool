import { createAction, props } from "@ngrx/store";
import { RoleAllowed } from "app/models/user-role";
import { Person } from "app/models/person";

export const loadUserRoles = createAction(
  "[User Roles] Load",
  props<{ user: Person }>()
);

export const loadUserRolesSuccess = createAction(
  "[User Roles] Load Success",
  props<{ roles: RoleAllowed[] }>()
);

export const loadUserRolesFailure = createAction("[User Roles] Load Failure");
