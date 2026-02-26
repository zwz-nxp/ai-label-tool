import { createFeatureSelector, createSelector } from "@ngrx/store";
import { CurrentUserState } from "./current-user.reducer";

export const selectCurrentUserState =
  createFeatureSelector<CurrentUserState>("currentUser");

export const selectCurrentUser = createSelector(
  selectCurrentUserState,
  (state: CurrentUserState) => state.currentUser
);

export const selectDebounceTime = createSelector(
  selectCurrentUserState,
  (state: CurrentUserState) => state.debounceTime
);

export const selectCurrentUserLoading = createSelector(
  selectCurrentUserState,
  (state: CurrentUserState) => state.loading
);

export const selectCurrentUserError = createSelector(
  selectCurrentUserState,
  (state: CurrentUserState) => state.error
);
