import { Component, OnDestroy, OnInit } from "@angular/core";
import { Person } from "./models/person";
import { DataService } from "./utils/api-access/data-service";
import { Location } from "./models/location";
import { AuthorizationService } from "./utils/services/authorization.service";
import { AboutComponent } from "./components/about/about.component";
import { SwitchUserDialogComponent } from "./components/switch-user-dialog/switch-user-dialog.component";
import { Router } from "@angular/router";
import { Notification } from "./models/notification";
import { combineLatest, Observable, Subscription } from "rxjs";
import { distinctUntilChanged, filter } from "rxjs/operators";
import { Configuration } from "./utils/configuration";
import { DEFAULT_INTERRUPTSOURCES, Idle } from "@ng-idle/core";
import { Keepalive } from "@ng-idle/keepalive";
import { IdleTimeoutComponent } from "./components/dialogs/idle-timeout/idle-timeout.component";
import { MatDialog, MatDialogRef } from "@angular/material/dialog";
import { IdleService } from "./utils/services/idle.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SnackbarUtils } from "./utils/snackbar-utils";
import { DataUpdateService } from "./utils/services/data-update.service";
import { UpdateType } from "./models/update";
import { RoleEnum } from "./models/role";
import { NotificationsModalComponent } from "./shared/notifications-modal/notifications-modal.component";
import { RxStompService } from "app/utils/services/rx-stomp.service";
import { RxStompState } from "@stomp/rx-stomp";
import { groupBy } from "lodash-es";
import { Store } from "@ngrx/store";
import { initializeApp, selectLoadingCriticalData } from "app/state";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import * as LocationSelectors from "app/state/location/location.selectors";
import * as LocationActions from "app/state/location/location.actions";
import * as SystemSelectors from "app/state/system/system.selectors";
import * as UserRoleSelectors from "app/state/user-role/user-role.selectors";

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  standalone: false,
})
export class AppComponent implements OnInit, OnDestroy {
  public lastPing?: Date;
  public currentUser = new Person();
  public selectedLocation = new Location();
  public locationMenuMap: Record<string, Location[]> = {};
  public notifications: Array<Notification> = [];
  public profilePictureLink: string;

  public isSupportedBrowser = true;

  public idleDialogActive = false;

  public url!: string;

  protected idleSubscription!: Subscription;
  protected initialized = false;
  private locationInitialized = false;
  private unreadNotificationsSubscription!: Subscription;

  private socketDisconnected = false;
  private currentUser$: Observable<Person | null>;
  private allLocations$: Observable<Location[]>;
  private isReadOnlyMode = false;
  private isInitialLocationSetup = true;

  public constructor(
    protected _configuration: Configuration,
    public idleService: IdleService,
    public idle: Idle,
    public keepalive: Keepalive,
    private dataService: DataService,
    private rxStompService: RxStompService,
    private dataUpdateService: DataUpdateService,
    public authorizationService: AuthorizationService,
    public router: Router,
    public dialogService: MatDialog,
    public snackBar: MatSnackBar,
    private store: Store
  ) {
    this.store.dispatch(initializeApp());

    this.currentUser$ = this.store.select(
      CurrentUserSelectors.selectCurrentUser
    );

    this.allLocations$ = this.store.select(
      LocationSelectors.selectAllLocations
    );

    // Rebuild the location menu whenever user, locations, or userRoles change.
    // userRoles loads after currentUser, so we must react to all three streams.
    combineLatest([
      this.currentUser$,
      this.allLocations$,
      this.store.select(UserRoleSelectors.selectUserRoles),
    ])
      .pipe(
        filter(([user, locations]) => !!user?.wbi && locations.length > 0),
        distinctUntilChanged(
          ([prevUser, prevLocs, prevRoles], [nextUser, nextLocs, nextRoles]) =>
            prevUser?.wbi === nextUser?.wbi &&
            prevLocs.length === nextLocs.length &&
            prevRoles.length === nextRoles.length
        )
      )
      .subscribe(([user, locations]) => {
        this.currentUser = user!;
        this.populateLocationMenuMap(locations);
        // Set default selection to primary location only once on first load
        if (!this.locationInitialized) {
          this.locationInitialized = true;
          const primary = this.currentUser.primaryLocation;
          if (primary && primary.id !== 0) {
            // Initial setup - don't navigate, just set the location
            this.selectedLocation = primary;
            this.store.dispatch(
              LocationActions.setCurrentLocation({ location: primary })
            );
            this.isInitialLocationSetup = false;
          }
        }
      });

    this.store
      .select(SystemSelectors.selectIsReadOnlyMode)
      .subscribe((value) => (this.isReadOnlyMode = value));

    this.store.select(selectLoadingCriticalData).subscribe((loading) => {
      if (!this.initialized && !loading) {
        this.initialized = true;
      }
    });

    this.profilePictureLink =
      _configuration.ServerWithApiUrl + "users/profilepicture";
    this.setUpIdleTimeoutWindow();

    this.rxStompService.connectionState$.subscribe((state) => {
      if (state === RxStompState.CLOSED && !this.socketDisconnected) {
        SnackbarUtils.displayErrorMsg(
          this.snackBar,
          "There seems to be a connection issue, please check your network and refresh this page."
        );
        this.socketDisconnected = true;
      } else if (state === RxStompState.OPEN) {
        this.socketDisconnected = false;
      }
    });
  }

  protected get isAdmin(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      RoleEnum.ADMINISTRATOR_SYSTEM
    );
  }

  protected get isUserAdmin(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      0,
      RoleEnum.ADMINISTRATOR_USER
    );
  }

  // Is this the same a 'site planner'
  protected get isSitePlanningManager(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      this.selectedLocation.id,
      RoleEnum.MANAGER_PLANNING_SITE
    );
  }

  protected get isSitePlanningFlagSteward(): boolean {
    return this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
      this.selectedLocation.id,
      RoleEnum.SITE_PLANNING_FLAG_STEWARD
    );
  }

  protected get notificationCount(): string {
    let count = Configuration.NotificationCount.toString();
    if (Configuration.NotificationCount > Configuration.MaxNotifications) {
      count = Configuration.MaxNotifications.toString() + "+";
    }
    return count;
  }

  protected get isProd(): boolean {
    return this.url.startsWith("https://nww.ie-mdm.nxp.com/");
  }

  protected get topTitle(): string {
    let title = "Owl Vision ADC";

    if (this.url.indexOf("localhost") > -1) {
      title += " LH";
    } else if (this.url.indexOf(".dev") > -1) {
      title += " DEV";
    } else if (this.url.indexOf(".qa") > -1) {
      title += " QA";
    }

    if (this.isReadOnlyMode) {
      title += " (read-only)";
    }

    return title;
  }

  protected get showProfileBadge(): boolean {
    return Configuration.NotificationCount < 1;
  }

  public ngOnInit(): void {
    this.setIsSupportedBrowser();
    this.url = window.location.href;
    this.idleSubscription = this.idleService
      .getIdleEventEmitter()
      .subscribe((event) => (this.idleDialogActive = event));
    this.retrieveNotifications();
    Configuration.IsProduction = this.url.startsWith(
      "https://nww.ie-mdm.nxp.com/"
    );
    this.profilePictureLink =
      this._configuration.ServerWithApiUrl + "users/profilepicture";
    this.configurePageBasedOnEnvironment();
    this.watchForUpdates();
  }

  public changeLocation(location: Location): void {
    this.selectedLocation = location;
    this.store.dispatch(LocationActions.setCurrentLocation({ location }));
    // Only navigate if this is not the initial location setup
    if (!this.isInitialLocationSetup) {
      // Navigate to index page after location change
      this.router.navigate(["/"]);
    }
  }

  public openAbout(): void {
    this.dialogService.open(AboutComponent, {
      disableClose: true,
    });
  }

  public openSwitchUserDialog(): void {
    const ref = this.dialogService.open(SwitchUserDialogComponent, {
      width: "360px",
      disableClose: false,
    });
    ref.afterClosed().subscribe((switched: boolean) => {
      if (switched) {
        window.location.reload();
      }
    });
  }

  public ngOnDestroy(): void {
    if (this.unreadNotificationsSubscription != null) {
      this.unreadNotificationsSubscription.unsubscribe();
    }
  }

  public refreshUserRights(): void {
    window.location.href = "/";
  }

  public openNotificationsDialog(): void {
    this.dialogService.open(NotificationsModalComponent);
  }

  private setUpIdleTimeoutWindow(): void {
    // sets an idle trigger of 30 minutes.
    this.idle.setIdle(60 * 30);
    // sets a timeout period of 120 minutes.
    this.idle.setTimeout(60 * 120);
    // sets the default interrupts, in this case, things like clicks, scrolls, touches to the document
    this.idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);

    this.idle.onIdleStart.subscribe(() => {
      if (!this.idleDialogActive) {
        this.openTimeout();
        this.idleDialogActive = true;
      }
    });

    this.idle.onIdleEnd.subscribe(() => {
      this.reset();
    });

    // sets the ping interval to 60 seconds
    this.keepalive.interval(60);
    this.keepalive.onPing.subscribe(() => {
      this.lastPing = new Date();
      this.dataService.getIEMDMVersionInfo().subscribe({
        next: () => {},
        error: () => {
          if (!this.idleDialogActive) {
            this.openTimeout();
            this.idleDialogActive = true;
          }
        },
      });
    });

    this.reset();
  }

  private reset(): void {
    this.idle.watch();
  }

  private setIsSupportedBrowser(): void {
    const browser = window.navigator.userAgent;
    if (browser.indexOf("Edg") > -1) {
      this.isSupportedBrowser = true;
    } else {
      this.isSupportedBrowser = browser.indexOf("Chrome") > -1;
    }
  }

  private watchForUpdates(): void {
    this.dataUpdateService.updateEmitter.subscribe((update) => {
      if (update.updatedType == UpdateType.APPROVAL) {
        this.retrieveNotifications();
      }
    });
  }

  private retrieveNotifications(): void {
    this.unreadNotificationsSubscription = this.dataService
      .getHomePageCount()
      .subscribe((resp) => {
        Configuration.NotificationCount = resp.unreadNotificationCount;
        Configuration.PendingApprovalCount = resp.approvalCount;
        Configuration.RequestCount = resp.requestCount;
        Configuration.MaxNotifications = resp.maxNotifications;
      });
  }

  private openTimeout(): MatDialogRef<IdleTimeoutComponent> {
    return this.dialogService.open(IdleTimeoutComponent, {
      disableClose: true,
    });
  }

  private populateLocationMenuMap(locations: Location[]): void {
    const filtered = this.filterLocationsForCurrentUser(locations);
    this.locationMenuMap = groupBy(filtered, (item) => item.menuGrouping ?? "");
  }

  private filterLocationsForCurrentUser(locations: Location[]): Location[] {
    // Always exclude the ALL SITES (id=0) entry
    const realLocations = locations.filter((l) => l.id !== 0);

    const allowedAcronyms = new Set<string>();
    let showAll = false;

    // Check Administrator_System (global role at location 0)
    const isSystemAdmin =
      this.authorizationService.doesCurrentUserHaveRoleForCurrentLocation(
        0,
        RoleEnum.ADMINISTRATOR_SYSTEM
      );

    if (isSystemAdmin) {
      const adminSites =
        this.authorizationService.getLinkedLocationAcronymsForRole(
          RoleEnum.ADMINISTRATOR_SYSTEM
        );
      if (adminSites.has("*")) {
        showAll = true;
      } else {
        adminSites.forEach((a) => allowedAcronyms.add(a));
      }
    }

    if (!showAll) {
      // ADC_Engineer is a site-specific role (not global), so check via userRoles directly
      const adcSites =
        this.authorizationService.getLinkedLocationAcronymsForRole(
          RoleEnum.ADC_ENGINEER
        );
      if (adcSites.size > 0) {
        if (adcSites.has("*")) {
          showAll = true;
        } else {
          adcSites.forEach((a) => allowedAcronyms.add(a));
        }
      }
    }

    if (showAll) {
      return realLocations;
    }

    if (allowedAcronyms.size === 0) {
      return [];
    }

    return realLocations.filter(
      (l) => allowedAcronyms.has(l.acronym) || allowedAcronyms.has(l.sapCode)
    );
  }

  private configurePageBasedOnEnvironment(): void {
    if (!Configuration.IsProduction) {
      const faviconEl: HTMLLinkElement | null =
        document.querySelector('link[rel="icon"]');

      if (faviconEl) {
        faviconEl.href = "assets/favicon1.ico";
      }

      document.title = this.topTitle;
    }
  }
}
