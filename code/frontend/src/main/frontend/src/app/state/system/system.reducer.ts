import { createReducer, on } from "@ngrx/store";
import * as SystemActions from "./system.actions";
import { ConfigurationItem } from "app/models/configuration-item";
import { GlobalLookupData } from "app/models/lookup-data";

export interface SystemState {
  configurationItems: ConfigurationItem[];
  globalLookupData: GlobalLookupData;
  loading: boolean;
  error: string | null;
}

export const systemInitialState: SystemState = {
  configurationItems: [],
  globalLookupData: new GlobalLookupData(),
  loading: false,
  error: null,
};

export const systemReducer = createReducer(
  systemInitialState,
  on(
    SystemActions.loadConfigurationItems,
    (state): SystemState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    SystemActions.loadConfigurationItemsSuccess,
    (state, { configurationItems }): SystemState => ({
      ...state,
      loading: false,
      configurationItems,
    })
  ),
  on(
    SystemActions.loadConfigurationItemsFailure,
    (state): SystemState => ({
      ...state,
      loading: false,
      error: "Failed loading configuration items",
    })
  ),
  on(
    SystemActions.loadGlobalLookupData,
    (state): SystemState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    SystemActions.loadGlobalLookupDataSuccess,
    (state, { globalLookupData }): SystemState => ({
      ...state,
      loading: false,
      globalLookupData: {
        ...globalLookupData,
        genericSearchMap: Object.fromEntries(
          Object.entries(globalLookupData.genericSearchMap).map(
            ([key, value]) => [value, key]
          )
        ) as Record<string, string>,
      },
    })
  ),
  on(
    SystemActions.loadGlobalLookupDataFailure,
    (state): SystemState => ({
      ...state,
      loading: false,
      error: "Failed loading global lookup data",
    })
  )
);
