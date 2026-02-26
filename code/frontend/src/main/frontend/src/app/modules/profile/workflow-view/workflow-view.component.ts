import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatSort } from "@angular/material/sort";
import { MatTable, MatTableDataSource } from "@angular/material/table";
import { ActivatedRoute } from "@angular/router";
import { DataService } from "app/utils/api-access/data-service";
import {
  GenericSearchArg,
  GenericSearchArguments,
  GenericSearchSortField,
  GenericSearchUtils,
} from "app/models/generic-search";
import { UpdateType } from "app/models/update";
import { Notification } from "app/models/notification";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { NotificationViewerComponent } from "app/shared/notifications-modal/notification-viewer/notification-viewer.component";
import { Store } from "@ngrx/store";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Person } from "app/models/person";

/**
 * Displays the notifications, the pending approval requests that a user might have,
 * and the approvals that the user might need to attend to.
 */
@Component({
  selector: "app-workflow-view",
  templateUrl: "./workflow-view.component.html",
  standalone: false,
})
export class WorkflowViewComponent implements OnInit, AfterViewInit {
  @ViewChild("NotificationsTable") public notificationsTable!: MatTable<
    MatTableDataSource<Notification>
  >;
  @ViewChild("NotificationsPaginator")
  public notificationPaginator!: MatPaginator;
  @ViewChild(MatSort) public notificationSort!: MatSort;

  /**
   * Displayed for notifications of workflow view component
   */
  public displayedColumnsNotifications = [
    "timestamp",
    "severityLevel",
    "message",
    "read",
  ];
  public notificationsDataSource = new MatTableDataSource<Notification>();
  public notifications: Notification[] = [];
  public notificationSearchArguments = new GenericSearchArguments();
  public notificationSearchColumns = [
    GenericSearchSortField.NOTIFICATION_TIMESTAMP,
    GenericSearchSortField.NOTIFICATION_SEVERITYLEVEL,
    GenericSearchSortField.NOTIFICATION_TITLE,
    GenericSearchSortField.NOTIFICATION_READ,
  ];

  public deepLinkRoute?: string;

  public filterValues = {};
  public isLoading = false;

  public activeTab = 0;

  public viewRequestCountForLabel = 0;
  public approveRequestCountForLabel = 0;
  private currentUser!: Person;

  public constructor(
    private route: ActivatedRoute,
    private dialogService: MatDialog,
    public dataService: DataService,
    public snackbar: MatSnackBar,
    private dataUpdateService: DataUpdateService,
    private store: Store,
    private genericSearchUtils: GenericSearchUtils
  ) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        if (user) {
          this.currentUser = user;
        }
      });
  }

  public ngOnInit(): void {
    this.getAllNotifications();

    this.route.url.subscribe((url) => {
      url.forEach((segment) => {
        if (
          segment.path.toLowerCase() === "request" ||
          segment.path.toLowerCase() === "approval"
        ) {
          this.deepLinkRoute = segment.path.toLowerCase();
        }
      });
    });

    this.watchForUpdates();
  }

  public ngAfterViewInit(): void {}

  public watchForUpdates(): void {
    this.dataUpdateService.updateEmitter.subscribe((update) => {
      if (
        update.updatedType == UpdateType.PLANNING ||
        update.updatedType == UpdateType.APPROVAL
      ) {
        this.getAllNotifications();
      }
    });
  }

  /**
   * Gets notifications assigns them to data source.
   */
  public getAllNotifications(): void {
    this.dataService.getAllNotifications().subscribe((notifications) => {
      this.notifications = notifications;
      this.notificationsDataSource = new MatTableDataSource(this.notifications);
      this.notificationsDataSource.paginator = this.notificationPaginator;
      this.notificationsDataSource.sort = this.notificationSort;
      this.isLoading = false;
    });
  }

  /**
   * Opens notification viewer dialog with the selected notification
   * being visible first.
   * The notification viewer is used to acknowledge notifications.
   * @param id id of the selected notification
   */
  public openNotification(id: number): void {
    const selectedNotification = this.notifications.find(
      (notification) => notification.id === id
    );

    if (selectedNotification) {
      const dialogRef = this.dialogService.open(NotificationViewerComponent);

      dialogRef.componentInstance.selectedNotification = selectedNotification;
      dialogRef.componentInstance.unreadNotifications = this.notifications;

      const sub =
        dialogRef.componentInstance.updateNotificationsEmitter.subscribe(() =>
          this.getAllNotifications()
        );

      dialogRef.afterClosed().subscribe(() => {
        sub.unsubscribe();
      });
    }
  }

  /**
   * Gets level of notification.
   * @param element
   * @returns string containing the level of the notification
   */
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

  /**
   * Gets title of notification
   * @param element
   * @returns title of notification.
   */
  public getTitle(element: Notification): string {
    if (typeof element.title !== "undefined") {
      return element.title;
    } else if (typeof element.message !== "undefined") {
      return element.message;
    }
    return "";
  }

  /**
   * Converts read boolean from notification entity into readable value for the table
   * @param read
   * @returns context-adequate string
   */
  public isRead(read: boolean): "Closed" | "Open" {
    if (read) {
      return "Closed";
    } else {
      return "Open";
    }
  }

  /**
   * Function that triggers when the notification search field is used.
   * It resets the paginator and applies HTTP filtering to the datasource to look for what was inputted.
   * @param triggeredSearchArg arguments being used to search
   */
  public notificationSearchFieldTriggered(
    triggeredSearchArg: GenericSearchArg
  ): void {
    //Creates a reference in memory to the search arguments instance
    const searchArg = this.notificationSearchArguments.getSearchField(
      triggeredSearchArg.field
    );
    if (searchArg) {
      //If the search field already existed, assign it a new search value.
      searchArg.value = triggeredSearchArg.value;
    } else {
      //If not, first push the search field into the possible search arguments then assign it the new value.
      this.notificationSearchArguments.searchArgs.push(triggeredSearchArg);
    }
    this.notificationPaginator.firstPage();
    this.applyFilterToNotificationDataSource();
  }

  /**
   * Applies filter to the data source of the notification table
   */
  public applyFilterToNotificationDataSource(): void {
    const searchArgs = this.notificationSearchArguments;

    this.notificationsDataSource.filterPredicate = (
      notification: Notification
    ): boolean => {
      return (
        this.genericSearchUtils.isBetween(
          notification.timestamp,
          searchArgs,
          GenericSearchSortField.NOTIFICATION_TIMESTAMP
        ) &&
        this.genericSearchUtils.isLike(
          this.isRead(notification.read),
          searchArgs,
          GenericSearchSortField.NOTIFICATION_READ
        ) &&
        this.genericSearchUtils.isLike(
          notification.title,
          searchArgs,
          GenericSearchSortField.NOTIFICATION_TITLE
        ) &&
        this.genericSearchUtils.isLike(
          notification.severityLevel,
          searchArgs,
          GenericSearchSortField.NOTIFICATION_SEVERITYLEVEL
        )
      );
    };

    this.notificationsDataSource.filter = JSON.stringify(this.filterValues);
  }

  public updateTableStickyStyles(): void {
    this.notificationsTable.updateStickyColumnStyles();
    this.notificationsTable.updateStickyHeaderRowStyles();
    this.notificationsTable.updateStickyFooterRowStyles();
  }
}
