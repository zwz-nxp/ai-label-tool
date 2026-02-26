import { createAction, props } from "@ngrx/store";
import { ConfigurationItem } from "app/models/configuration-item";
import { GlobalLookupData } from "app/models/lookup-data";

export const loadConfigurationItems = createAction(
  "[System] Load Configuration Items"
);

export const loadConfigurationItemsSuccess = createAction(
  "[System] Load Configuration Items Success",
  props<{ configurationItems: ConfigurationItem[] }>()
);

export const loadConfigurationItemsFailure = createAction(
  "[System] Load Configuration Items Failure"
);

export const loadGlobalLookupData = createAction(
  "[System] Load Global Lookup Data"
);

export const loadGlobalLookupDataSuccess = createAction(
  "[System] Load Global Lookup Data Success",
  props<{ globalLookupData: GlobalLookupData }>()
);

export const loadGlobalLookupDataFailure = createAction(
  "[System] Load Global Lookup Data Failure"
);
