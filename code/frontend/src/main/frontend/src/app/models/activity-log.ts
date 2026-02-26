import { Person } from "./person";
import { Location } from "./location";

export class ActivityLog {
  public user: Person = new Person();
  public location: Location = new Location();
  public timestamp = new Date();
  public action = "";
}
