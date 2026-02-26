import { AfterContentInit, Component, ViewChild } from "@angular/core";
import { MatTableDataSource } from "@angular/material/table";
import { Notification } from "app/models/notification";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { Configuration } from "app/utils/configuration";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { NotificationViewerComponent } from "./notification-viewer/notification-viewer.component";
import { MatSnackBar } from "@angular/material/snack-bar";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { MatSort } from "@angular/material/sort";
import { MatPaginator } from "@angular/material/paginator";
import { Store } from "@ngrx/store";

@Component({
  selector: "app-notifications-modal",
  templateUrl: "./notifications-modal.component.html",
  standalone: false,
})
export class NotificationsModalComponent
  extends BaseDialogComponent
  implements AfterContentInit
{
  @ViewChild(MatPaginator) public paginator!: MatPaginator;
  @ViewChild(MatSort) public sort!: MatSort;

  public selectedNotification?: Notification;
  public displayedColumnsNotifications = [
    "timestamp",
    "severityLevel",
    "message",
  ];
  public unreadNotifications: Array<Notification> = [];

  public unreadNotificationDataSource = new MatTableDataSource<Notification>();

  public constructor(
    dialogRef: MatDialogRef<NotificationsModalComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public dialogService: MatDialog,
    public authorizationService: AuthorizationService
  ) {
    super(dialogRef, dataService, snackBar, store);
  }

  public ngAfterContentInit(): void {
    this.getNotifications();
  }

  public openNotification(id: string, _type: string): void {
    this.unreadNotifications.forEach((notification) => {
      if (notification.id.toString() == id) {
        this.selectedNotification = notification;
      }
    });

    const dialogRef = this.dialogService.open(NotificationViewerComponent, {
      disableClose: true,
    });

    const sub =
      dialogRef.componentInstance.updateNotificationsEmitter.subscribe(() =>
        this._updateNotificationsDataSource()
      );
    const clear =
      dialogRef.componentInstance.clearNotificationsEmitter.subscribe(() =>
        this._clearNotificationsDataSource()
      );
    dialogRef.componentInstance.selectedNotification =
      this.selectedNotification ?? new Notification();
    dialogRef.componentInstance.unreadNotifications = this.unreadNotifications;

    dialogRef.afterClosed().subscribe(() => {
      sub.unsubscribe();
      clear.unsubscribe();
    });
  }

  public setNotificationColor(status: string): string {
    return status === "SYSTEM_WARNING" ? "!bg-red-400" : "";
  }

  public getTitle(element: Notification): string {
    if (typeof element.title !== "undefined") {
      return element.title;
    } else if (typeof element.message !== "undefined") {
      return element.message;
    }
    return "";
  }

  public getLevel(element: Notification): string {
    if (typeof element.severityLevel !== "undefined") {
      return element.severityLevel.replace("_", " ");
    } else {
      if (typeof element.errorOccurred !== "undefined") {
        if (element.errorOccurred) {
          return "WARNING";
        }
      }
    }
    return "INFORMATION";
  }

  private getNotifications(): void {
    this.isLoading = true;
    this.dataService.getAllUnreadNotifications().subscribe({
      next: (data) => {
        this.isLoading = false;
        this.sort.sortChange.subscribe(() => this.paginator.firstPage());
        this.unreadNotifications = data;
        this.unreadNotificationDataSource = new MatTableDataSource(
          this.unreadNotifications
        );
        this.unreadNotificationDataSource.sort = this.sort;
        this.unreadNotificationDataSource.paginator = this.paginator;
      },
      error: (_err) => {
        this.isLoading = false;
      },
    });
  }

  private _updateNotificationsDataSource(): void {
    const index = this.unreadNotifications.findIndex(
      (x) => x.id == this.selectedNotification?.id
    );
    if (index > -1) {
      this.unreadNotifications.splice(index, 1);
    }
    this.setDataSource();
  }

  private _clearNotificationsDataSource(): void {
    this.unreadNotifications = [];
    this.setDataSource();
  }

  private setDataSource(): void {
    this.unreadNotificationDataSource = new MatTableDataSource(
      this.unreadNotifications
    );
    this.unreadNotificationDataSource.sort = this.sort;
    this.unreadNotificationDataSource.paginator = this.paginator;
    Configuration.NotificationCount = this.unreadNotifications.length;
  }
}
