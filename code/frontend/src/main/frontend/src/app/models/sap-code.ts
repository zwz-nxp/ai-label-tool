import { Location } from "./location";

export class SapCode {
  plantCode = "";
  enoviaAcronym = "";
  city = "";
  country = "";
  state = "";
  managedBy = new Location();
  lastUpdated = new Date();
  updatedBy = "";
}
