import { Component, EventEmitter, Input, Output } from "@angular/core";
import { Notification } from "app/models/notification";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { Configuration } from "app/utils/configuration";
import { MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: "app-notification-viewer",
  templateUrl: "./notification-viewer.component.html",
  standalone: false,
})
export class NotificationViewerComponent {
  @Input() public selectedNotification!: Notification;
  @Input() public unreadNotifications: Array<Notification> = [];
  @Output() public updateNotificationsEmitter = new EventEmitter<string>();
  @Output() public clearNotificationsEmitter = new EventEmitter<string>();

  public isLoading = false;

  public constructor(
    public dialogRef: MatDialogRef<NotificationViewerComponent>,
    public dataService: DataService,
    public authorizationService: AuthorizationService
  ) {}

  public closeDialog(): void {
    this.dialogRef.close();
  }

  public okAndClose(): void {
    this.dataService.submitNotificationRead(this.selectedNotification);
    this.updateNotifications();
    this.closeDialog();
  }

  public okAndNext(): void {
    if (this.unreadNotifications?.length > 0) {
      this.dataService
        .submitNotificationRead(this.selectedNotification)
        .subscribe((_resp) => {
          const index = this.unreadNotifications.findIndex(
            (x) => x.id == this.selectedNotification.id
          );
          if (index > -1) {
            this.unreadNotifications.splice(index, 1);
          }
          this.updateNotifications();
          if (this.unreadNotifications?.length > 0) {
            this.selectedNotification = this.unreadNotifications[0];
          } else {
            this.closeDialog();
          }
        });
    } else {
      this.closeDialog();
    }
  }

  public okAndAll(): void {
    if (this.unreadNotifications.length != 0) {
      const notifications = this.unreadNotifications;
      notifications.forEach((notification) => (notification.read = true));
      this.isLoading = true;
      this.dataService
        .acknowledgeAllNotification(notifications)
        .subscribe((_resp) => {
          this.isLoading = false;

          Configuration.NotificationCount = 0;
          this.clearNotifications();
          this.closeDialog();
        });
    }
  }

  public clearNotifications(): void {
    this.clearNotificationsEmitter.emit("clear");
  }

  public updateNotifications(): void {
    this.updateNotificationsEmitter.emit("update");
  }

  public isNotificationUnread(): boolean {
    return !this.selectedNotification.read;
  }
}
