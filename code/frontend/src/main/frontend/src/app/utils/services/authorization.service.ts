import { Injectable } from "@angular/core";
import { RoleEnum } from "app/models/role";
import { Person } from "app/models/person";
import { RoleAllowed } from "app/models/user-role";
import { Store } from "@ngrx/store";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import * as UserRoleSelectors from "app/state/user-role/user-role.selectors";

@Injectable()
export class AuthorizationService {
  private currentUser!: Person;
  private userRoles: RoleAllowed[] = [];

  public constructor(private store: Store) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        this.currentUser = user ?? new Person();
      });

    this.store.select(UserRoleSelectors.selectUserRoles).subscribe((roles) => {
      this.userRoles = roles;
    });
  }

  public doesCurrentUserHaveRoleForCurrentLocation(
    locationId: number,
    roleId: string
  ): boolean {
    if (this.currentUser.wbi) {
      if (locationId === 0) {
        return this.doesCurrentUserHaveGlobalRole(this.currentUser, roleId);
      } else {
        return (
          this.currentUser.roles[0]?.some((role) => role.id === roleId) ||
          this.currentUser.roles[locationId]?.some((role) => role.id === roleId)
        );
      }
    } else {
      return false;
    }
  }

  /*
    This uses the sapCode stored in currentUserRolesAllowed to check if the currentUser is granted,
    if this user does not have a global role.
   */
  public doesUserHaveRoleForSite(roleId: string, site: string): boolean {
    const hasGlobalRole = this.userRoles.some(
      (role) => role.role.id === roleId && role.allSites
    );

    if (hasGlobalRole) {
      return true;
    } else {
      return this.userRoles.some(
        (e) =>
          e.role.id === roleId &&
          (e.site === site || e.sapCodes.some((f) => f === site))
      );
    }
  }

  public getLinkedLocationAcronymsForRole(roleId: string): Set<string> {
    const result = new Set<string>();
    for (const ra of this.userRoles) {
      if (ra.role.id === roleId) {
        if (ra.allSites) {
          // Signal to caller that all sites are allowed
          result.add("*");
          return result;
        }
        if (ra.site) {
          result.add(ra.site);
        }
        ra.sapCodes.forEach((code) => result.add(code));
      }
    }
    return result;
  }

  private doesCurrentUserHaveGlobalRole(
    person: Person,
    roleId: string
  ): boolean {
    if (person.wbi) {
      return person.roles[0]?.some((role) => role.id === roleId);
    } else {
      return false;
    }
  }
}
