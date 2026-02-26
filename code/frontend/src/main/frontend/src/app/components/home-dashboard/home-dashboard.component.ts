import { Component, OnDestroy, OnInit } from "@angular/core";
import { DataService } from "app/utils/api-access/data-service";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { Location } from "app/models/location";
import { Subscription } from "rxjs";
import { Configuration } from "app/utils/configuration";
import { AppUtils } from "app/utils/app-utils";
import { MatDialog } from "@angular/material/dialog";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { NotificationsModalComponent } from "app/shared/notifications-modal/notifications-modal.component";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";

/**
 * Component for the Home Dashboard. Displays Notification,
 * Pending Approval Requests and Approval counts, a site
 * calendar and a weekly notification overview.
 */
@Component({
  selector: "app-home-dashboard",
  templateUrl: "./home-dashboard.component.html",
  styleUrls: ["./home-dashboard.component.scss"],
  standalone: false,
})
export class HomeDashboardComponent implements OnInit, OnDestroy {
  public location = new Location();
  public threeWeeksHTML = "";
  public hasSysWarnings = false;

  public displayColumns = ["roleName", "forSites", "forSapCodes"];
  public currentMonthDate!: Date;
  public nextMonthDate!: Date;
  public au = new AppUtils();

  public isLoading = false;

  private currentUserSubscription!: Subscription;
  private threeWeeksSubscription!: Subscription;
  private requestsSubscription!: Subscription;

  private currentDate = new Date();

  public constructor(
    public dialogService: MatDialog,
    private dataService: DataService,
    public authorizationService: AuthorizationService,
    private dataUpdateService: DataUpdateService,
    public config: Configuration,
    public snackBar: MatSnackBar,
    private store: Store
  ) {
    this.store
      .select(LocationSelectors.selectCurrentLocation)
      .subscribe((location) => {
        if (location) {
          this.location = location;
          this.getThreeWeeksHTML();
        }
      });
  }

  public ngOnInit(): void {
    this.setMonths();
    this.notificationSysWarnCheck();
  }

  /**
   * Gets the HTML for the weekly overview of the user.
   */
  public getThreeWeeksHTML(): void {
    this.threeWeeksSubscription = this.dataService
      .get3WeeksForLocation(this.location.id)
      .subscribe((html) => {
        if (html.length) {
          this.threeWeeksHTML = html;
        } else {
          this.threeWeeksHTML = "There are no weekly updates available.";
        }
      });
  }

  public notificationSysWarnCheck(): void {
    this.dataService.hasNotificationSystemWarnings().subscribe((value) => {
      return (this.hasSysWarnings = value);
    });
  }

  /**
   * Opens the notifications modal.
   */
  public openNotificationsModal(): void {
    this.dialogService.open(NotificationsModalComponent);
  }

  /**
   * Returns the notification count.
   */
  public notificationCount(): number {
    let countToReturn: number = Configuration.NotificationCount;
    if (this.isNotificationCountOverLimit()) {
      countToReturn = Configuration.MaxNotifications;
    }
    return countToReturn;
  }

  /**
   * Returns True if the number of unread notifications is over the limit.
   * Used locally and in the template
   */
  public isNotificationCountOverLimit(): boolean {
    return Configuration.NotificationCount > Configuration.MaxNotifications;
  }

  /**
   * Returns the approvals count.
   */
  public approvalCount(): number {
    return Configuration.PendingApprovalCount;
  }

  public ngOnDestroy(): void {
    this.au.closeSubscriptions([
      this.currentUserSubscription,
      this.threeWeeksSubscription,
      this.requestsSubscription,
    ]);
  }

  private setMonths(): void {
    const yearCurrentMonth = this.currentDate.getUTCFullYear();
    const currentMonth = this.currentDate.getUTCMonth();
    this.currentMonthDate = new Date(yearCurrentMonth, currentMonth, 7);

    let yearNextMonth = yearCurrentMonth;
    let nextMonth = currentMonth + 1;
    // in case the nextMonth is 13 (the month after December), we reassign the variables to the following year
    if (nextMonth > 11) {
      yearNextMonth++;
      nextMonth = 0;
    }
    this.nextMonthDate = new Date(yearNextMonth, nextMonth, 7);
  }
}
