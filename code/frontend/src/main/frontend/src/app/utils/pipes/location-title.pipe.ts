import { Location } from "app/models/location";
import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "locationTitle",
  standalone: false,
})
export class LocationTitlePipe implements PipeTransform {
  public transform(location?: Location): string {
    if (location) {
      return location.isSubContractor
        ? `- ${location.acronym} (Subcon)`
        : `- ${location.acronym}`;
    }
    return "";
  }
}
