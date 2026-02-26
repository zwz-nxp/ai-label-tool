import { Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";
import { Store } from "@ngrx/store";
import { combineLatest, Observable } from "rxjs";
import { filter, map, take } from "rxjs/operators";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { RoleEnum } from "app/models/role";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import * as LocationSelectors from "app/state/location/location.selectors";
import * as UserRoleSelectors from "app/state/user-role/user-role.selectors";

@Injectable({ providedIn: "root" })
export class LandingAiAuthGuard implements CanActivate {
  constructor(
    private authorizationService: AuthorizationService,
    private store: Store,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    return combineLatest([
      this.store.select(CurrentUserSelectors.selectCurrentUser),
      this.store.select(LocationSelectors.selectCurrentLocation),
      this.store.select(UserRoleSelectors.selectUserRoles),
    ]).pipe(
      // Wait for all critical data to be loaded
      filter(
        ([user, location, userRoles]) =>
          !!user?.wbi && !!location && userRoles.length > 0
      ),
      take(1),
      map(([, location]) => {
        const locationId = location!.id;

        // 1. Check Administrator_System (global role at location 0)
        const isSystemAdmin =
          this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
            0,
            RoleEnum.ADMINISTRATOR_SYSTEM
          );
        if (isSystemAdmin) {
          return true;
        }

        // 2. Check ADC_Engineer for the current location via userRoles (site-specific)
        const adcSites =
          this.authorizationService.getLinkedLocationAcronymsForRole(
            RoleEnum.ADC_ENGINEER
          );
        if (
          adcSites.has("*") ||
          adcSites.has(location!.acronym) ||
          adcSites.has(location!.sapCode)
        ) {
          return true;
        }

        // 3. Unauthorized — redirect to home
        this.router.navigate(["/"]);
        return false;
      })
    );
  }
}
