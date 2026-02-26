import { createReducer, on } from "@ngrx/store";
import * as LocationActions from "./location.actions";
import { Location } from "app/models/location";

export interface LocationState {
  allLocations: Location[];
  currentLocation: Location | null;
  loading: boolean;
  error: string | null;
}

export const locationInitialState: LocationState = {
  allLocations: [],
  currentLocation: null,
  loading: false,
  error: null,
};

export const locationReducer = createReducer(
  locationInitialState,
  on(
    LocationActions.loadAllLocations,
    (state): LocationState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    LocationActions.loadAllLocationsSuccess,
    (state, { allLocations }): LocationState => ({
      ...state,
      loading: false,
      allLocations: sortLocations(allLocations),
    })
  ),
  on(
    LocationActions.loadAllLocationsFailure,
    (state): LocationState => ({
      ...state,
      loading: false,
      error: "Failed loading all locations",
    })
  ),
  on(
    LocationActions.setCurrentLocation,
    (state, { location }): LocationState => ({
      ...state,
      currentLocation: location,
    })
  )
);

function sortLocations(locations: Array<Location>): Location[] {
  return locations.toSorted((a, b) => a.acronym.localeCompare(b.acronym));
}
