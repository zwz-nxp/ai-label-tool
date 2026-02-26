import { Pipe, PipeTransform } from "@angular/core";
import { RoleEnum } from "app/models/role";
import { AuthorizationService } from "app/utils/services/authorization.service";

/**
 * This pipe will check if a user is authorized for a particular role in a particular location or not.
 */
@Pipe({
  name: "authorization",
  standalone: false,
})
export class AuthorizationPipe implements PipeTransform {
  public constructor(private authorizationService: AuthorizationService) {}

  /**
   * Pipe main entry point. Checks if a user has this role for a specific location.
   * @param location location to check for, can be location: id, acronym or sap code
   * @param role role to check, use roleEnum
   * @param negation used for disabling buttons and/or links. The disabled tag activates on true,
   *  negating it will deactivate the disabled tag and enable the button/link use this pipe for.
   * @returns if a user is authorized, it will return true, else false. When negated, it will return
   * the opposite. Use that for html disabled tags.
   */
  public transform(
    location: string | number,
    role: RoleEnum,
    negation = false
  ): boolean {
    let result: boolean;
    if (typeof location === "number") {
      result =
        this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
          location,
          role
        );
    } else {
      result = this.authorizationService.doesUserHaveRoleForSite(
        role,
        location
      );
    }
    return negation ? !result : result;
  }
}
