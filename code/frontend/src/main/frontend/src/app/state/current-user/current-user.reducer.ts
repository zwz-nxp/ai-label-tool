import { Person } from "app/models/person";
import { createReducer, on } from "@ngrx/store";
import * as CurrentUserActions from "./current-user.actions";

export const DEFAULT_DEBOUNCE_TIME = 600;

export interface CurrentUserState {
  currentUser: Person | null;
  debounceTime: number;
  loading: boolean;
  error: string | null;
}

export const currentUserInitialState: CurrentUserState = {
  currentUser: null,
  debounceTime: DEFAULT_DEBOUNCE_TIME,
  loading: false,
  error: null,
};

export const currentUserReducer = createReducer(
  currentUserInitialState,
  on(
    CurrentUserActions.loadCurrentUser,
    (state): CurrentUserState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    CurrentUserActions.loadCurrentUserSuccess,
    (state, { user }): CurrentUserState => ({
      ...state,
      loading: false,
      currentUser: user,
    })
  ),
  on(
    CurrentUserActions.loadCurrentUserFailure,
    (state): CurrentUserState => ({
      ...state,
      loading: false,
      error: "Failed loading current user",
    })
  ),
  on(
    CurrentUserActions.loadDebounceTime,
    (state): CurrentUserState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    CurrentUserActions.loadDebounceTimeSuccess,
    (state, { debounceTime }): CurrentUserState => ({
      ...state,
      loading: false,
      debounceTime,
    })
  ),
  on(
    CurrentUserActions.loadDebounceTimeFailure,
    (state): CurrentUserState => ({
      ...state,
      loading: false,
      error: "Failed loading debounce time",
    })
  )
);
