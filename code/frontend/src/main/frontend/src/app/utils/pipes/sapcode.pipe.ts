import { Pipe, PipeTransform } from "@angular/core";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";
import { Location } from "app/models/location";

@Pipe({
  name: "sapCode",
  standalone: false,
})
export class SapCodePipe implements PipeTransform {
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
      this.allLocations.find((loc) => loc.id === locationId)?.sapCode ??
      "Unknown"
    );
  }
}
