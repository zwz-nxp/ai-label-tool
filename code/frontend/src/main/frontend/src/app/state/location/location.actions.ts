import { createAction, props } from "@ngrx/store";
import { Location } from "app/models/location";

export const loadAllLocations = createAction("[Location] Load All Locations");

export const loadAllLocationsSuccess = createAction(
  "[Location] Load All Locations Success",
  props<{ allLocations: Location[] }>()
);

export const loadAllLocationsFailure = createAction(
  "[Location] Load All Locations Failure"
);

export const setCurrentLocation = createAction(
  "[Location] Set Current Location",
  props<{ location: Location }>()
);
