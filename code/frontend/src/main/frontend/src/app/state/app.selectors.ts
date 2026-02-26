import { createSelector } from "@ngrx/store";
import { selectCurrentUserLoading } from "app/state/current-user";
import { selectUserRolesLoading } from "app/state/user-role";
import { selectLocationLoading } from "app/state/location";
import { selectSystemLoading } from "app/state/system";

export const selectLoadingCriticalData = createSelector(
  selectCurrentUserLoading,
  selectUserRolesLoading,
  selectLocationLoading,
  selectSystemLoading,
  (loadingUser, loadingRoles, loadingLocations, loadingSystem) =>
    loadingUser || loadingRoles || loadingLocations || loadingSystem
);
