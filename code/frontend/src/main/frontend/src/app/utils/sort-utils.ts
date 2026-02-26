import { Person } from "app/models/person";

export class SortUtils {
  /**
   * return the item on which the MatSort should sort. The property may be a nested property like: 'site.acronym'
   * @param item the item coming from sortingDataAccessor
   * @param property the property coming from sortingDataAccessor
   * @param propertyMap a Map<string, string>
   */
  public static sortOnNestedProperty(item: any, property: string) {
    const elems = property.split(".");
    if (elems.length == 1) {
      return item[elems[0]];
    } else if (elems.length == 2) {
      return item[elems[0]][elems[1]];
    } else if (elems.length == 3) {
      return item[elems[0]][elems[1]][elems[2]];
    } else if (elems.length == 4) {
      return item[elems[0]][elems[1]][elems[2]][elems[3]];
    } else {
      return item[property];
    }
  }

  public static compare(
    a: number | string | Date,
    b: number | string | Date,
    isAscending: boolean
  ): number {
    return (a < b ? -1 : 1) * (isAscending ? 1 : -1);
  }

  public static sortUsersByName(users: Person[]) {
    users.sort((a: Person, b: Person) => (a.name > b.name ? 1 : -1));
  }
}
