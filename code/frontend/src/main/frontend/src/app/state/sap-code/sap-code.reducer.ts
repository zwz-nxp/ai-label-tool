import { createReducer, on } from "@ngrx/store";
import * as SapCodeActions from "./sap-code.actions";
import { SapCode } from "app/models/sap-code";

export interface SapCodeState {
  sapCodes: SapCode[];
  loading: boolean;
  error: string | null;
}

export const sapCodeInitialState: SapCodeState = {
  sapCodes: [],
  loading: false,
  error: null,
};

export const sapCodeReducer = createReducer(
  sapCodeInitialState,
  on(
    SapCodeActions.loadSapCodes,
    (state): SapCodeState => ({
      ...state,
      loading: true,
    })
  ),
  on(
    SapCodeActions.loadSapCodesSuccess,
    (state, { sapCodes }): SapCodeState => ({
      ...state,
      loading: false,
      sapCodes: sapCodes.toSorted((a, b) =>
        a.plantCode.localeCompare(b.plantCode)
      ),
    })
  ),
  on(
    SapCodeActions.loadSapCodesFailure,
    (state): SapCodeState => ({
      ...state,
      loading: false,
      error: "Failed loading sap codes",
    })
  )
);
