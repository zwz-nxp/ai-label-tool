import { Person } from "./person";

export class Notification {
  public id = 0;
  public type = "";
  public severityLevel = "";
  public message = "";
  public title = "";
  public recipient = new Person();
  public sender = new Person();
  public read = false;
  public event = "";
  public timestamp = new Date();
  public errorOccurred?: string;
}

export class HomePageCount {
  public unreadNotificationCount = 0;
  public requestCount = 0;
  public approvalCount = 0;
  public maxNotifications = 0;
}
