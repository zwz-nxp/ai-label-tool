import { createFeatureSelector, createSelector } from "@ngrx/store";
import { SystemState } from "./system.reducer";
import { ConfigurationValueType } from "app/models/configuration-item";

export const selectSystemState = createFeatureSelector<SystemState>("system");

export const selectConfigurationItems = createSelector(
  selectSystemState,
  (state: SystemState) => state.configurationItems
);

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export const selectConfigurationItem = (key?: string) =>
  createSelector(
    selectSystemState,
    (state: SystemState) =>
      state.configurationItems.find((e) => e.configurationKey === key)
        ?.configurationValue ?? ""
  );

export const selectIsReadOnlyMode = createSelector(
  selectSystemState,
  (state: SystemState) =>
    state.configurationItems.find(
      (e) => e.configurationKey === ConfigurationValueType.READ_ONLY_MODE
    )?.configurationValue === "true"
);

export const selectGlobalLookupData = createSelector(
  selectSystemState,
  (state: SystemState) => state.globalLookupData
);

export const selectGenericSearchMap = createSelector(
  selectSystemState,
  (state: SystemState) => state.globalLookupData.genericSearchMap
);

export const selectUserNames = createSelector(
  selectSystemState,
  (state: SystemState) => state.globalLookupData.userNames
);

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export const selectUserName = (wbi: string) =>
  createSelector(
    selectSystemState,
    (state: SystemState) => state.globalLookupData.userNames[wbi]
  );

export const selectSystemLoading = createSelector(
  selectSystemState,
  (state: SystemState) => state.loading
);

export const selectSystemError = createSelector(
  selectSystemState,
  (state: SystemState) => state.error
);
