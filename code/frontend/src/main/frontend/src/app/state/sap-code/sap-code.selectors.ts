import { createFeatureSelector, createSelector } from "@ngrx/store";
import { SapCodeState } from "./sap-code.reducer";

export const selectSapCodeState =
  createFeatureSelector<SapCodeState>("sapCodes");

export const selectSapCodes = createSelector(
  selectSapCodeState,
  (state: SapCodeState) => state.sapCodes
);

export const selectSapPlantCodes = createSelector(
  selectSapCodeState,
  (state: SapCodeState) => [
    ...new Set(state.sapCodes.map((sapCode) => sapCode.plantCode)),
  ]
);

export const selectSapCodesLoading = createSelector(
  selectSapCodeState,
  (state: SapCodeState) => state.loading
);

export const selectSapCodesError = createSelector(
  selectSapCodeState,
  (state: SapCodeState) => state.error
);
