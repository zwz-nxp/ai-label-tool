import { Subscription } from "rxjs";
import { Location } from "app/models/location";

export class AppUtils {
  public closeSubscriptions(subscriptions: Array<Subscription>): void {
    subscriptions.forEach((s) => {
      if (s) s.unsubscribe();
    });
  }

  public getSiteAcronym(locations: Location[], siteId?: number): string {
    return (
      locations.find((loc) => loc.id === siteId)?.acronym ??
      `Unknown id (${siteId})`
    );
  }

  public isNotNull(object: unknown): boolean {
    return object !== undefined && object !== null;
  }

  public getLocationByAcronym(
    locations: Location[],
    acronym: string
  ): Location {
    let location = locations.find((e) => e.acronym === acronym);
    if (!location) {
      location = new Location();
      location.acronym = acronym;
      location.sapCode = "Unknown";
    }
    return location;
  }

  public convertBooleanToYesNo(value: boolean): string {
    return value ? "Yes" : "No";
  }

  public convertBooleanToYesNoBoth(value: boolean): string {
    if (value === undefined) {
      return "";
    }
    return value ? "Yes" : "No";
  }
}
