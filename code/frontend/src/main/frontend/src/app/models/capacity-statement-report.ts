import { Person } from "./person";

export class CapacityStatementReport {
  public id = 0;
  public user = new Person();
  public message = "";
  public timestamp = "";
  public errorOccurred = false;
  public read = false;
  public system = "";
  public fileLocation = "";
}
