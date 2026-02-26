import { createAction, props } from "@ngrx/store";
import { SapCode } from "app/models/sap-code";

export const loadSapCodes = createAction("[Sap Codes] Load");

export const loadSapCodesSuccess = createAction(
  "[Sap Codes] Load Success",
  props<{ sapCodes: SapCode[] }>()
);

export const loadSapCodesFailure = createAction("[Sap Codes] Load Failure");
