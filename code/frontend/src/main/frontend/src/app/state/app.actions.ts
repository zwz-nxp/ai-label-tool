import { createAction, props } from "@ngrx/store";
import { Update } from "app/models/update";

export const initializeApp = createAction("[App] Initialize");

export const updateData = createAction(
  "[App] Update Data",
  props<{ update: Update }>()
);
