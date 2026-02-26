import { Pipe, PipeTransform } from "@angular/core";
import { Location } from "app/models/location";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";

@Pipe({
  name: "locationAcronym",
  standalone: false,
})
export class LocationAcronymPipe implements PipeTransform {
  private allLocations: Location[] = [];

  public constructor(private store: Store) {
    this.store
      .select(LocationSelectors.selectAllLocations)
      .subscribe((locations) => {
        this.allLocations = locations;
      });
  }

  public transform(locationId: number): string {
    return (
      this.allLocations.find((location) => locationId === location.id)
        ?.acronym ?? "Unknown"
    );
  }
}
