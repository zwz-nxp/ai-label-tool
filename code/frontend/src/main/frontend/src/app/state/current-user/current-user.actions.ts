import { createAction, props } from "@ngrx/store";
import { Person } from "app/models/person";

export const loadCurrentUser = createAction("[Current User] Load");

export const loadCurrentUserSuccess = createAction(
  "[Current User] Load Success",
  props<{ user: Person }>()
);

export const loadCurrentUserFailure = createAction(
  "[Current User] Load Failure"
);

export const loadDebounceTime = createAction(
  "[Current User] Load Debounce Time",
  props<{ user: Person }>()
);

export const loadDebounceTimeSuccess = createAction(
  "[Current User] Load Debounce Time Success",
  props<{ debounceTime: number }>()
);

export const loadDebounceTimeFailure = createAction(
  "[Current User] Load Debounce Time Failure"
);
