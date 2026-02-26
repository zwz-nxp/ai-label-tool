import { Person } from "./person";
import { Role } from "./role";
import { Location } from "./location";

export class UserRole {
  user = new Person();
  location = new Location();
  role = new Role();
}

export class RoleAllowed {
  role = new Role();
  allSites = false;
  site = "";
  sapCodes: Array<string> = [];
}
