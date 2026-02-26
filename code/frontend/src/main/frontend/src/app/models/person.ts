import { Location } from "./location";
import { Role } from "./role";

export class Person {
  public wbi = "";
  public name = "";
  public pictureURL = "";
  public email = "";
  public primaryLocation = new Location();
  public roles: Record<number, Role[]> = {};
  public lastLogin = new Date();
  public loginAllowed = false;

  public static clonePerson(source: Person): Person {
    const target = new Person();
    target.wbi = source.wbi;
    target.name = source.name;
    target.pictureURL = source.pictureURL;
    target.email = source.email;
    target.primaryLocation = source.primaryLocation;
    target.roles = source.roles;
    target.lastLogin = source.lastLogin;
    target.loginAllowed = source.loginAllowed;
    return target;
  }
}
