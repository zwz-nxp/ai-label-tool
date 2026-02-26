import { createFeatureSelector, createSelector } from "@ngrx/store";
import { LocationState } from "./location.reducer";

export const selectLocationState =
  createFeatureSelector<LocationState>("location");

export const selectAllLocations = createSelector(
  selectLocationState,
  (state: LocationState) => state.allLocations
);

export const selectCurrentLocation = createSelector(
  selectLocationState,
  (state: LocationState) => state.currentLocation
);

export const selectLocationLoading = createSelector(
  selectLocationState,
  (state: LocationState) => state.loading
);

export const selectLocationError = createSelector(
  selectLocationState,
  (state: LocationState) => state.error
);
