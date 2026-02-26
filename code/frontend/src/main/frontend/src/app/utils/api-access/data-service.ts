import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Injectable, OnDestroy } from "@angular/core";
import {
  catchError,
  firstValueFrom,
  Observable,
  of,
  Subscription,
  throwError,
} from "rxjs";
import { Configuration } from "../configuration";
import { Person } from "app/models/person";
import { Location, Manufacturer } from "app/models/location";
import { ConfigurationItem } from "app/models/configuration-item";
import { VersionInfo } from "app/models/version-info";
import { GlossaryItem } from "app/models/glossary-item";
import { NxpProductionYear } from "app/models/nxp-production-year";
import { Role } from "app/models/role";
import { RoleAllowed, UserRole } from "app/models/user-role";
import { HomePageCount, Notification } from "app/models/notification";
import { GenericSearchArguments } from "app/models/generic-search";
import { GlobalLookupData, LocalLookupData } from "app/models/lookup-data";
import { PageColumnsConfiguration } from "app/models/page-columns-configuration";
import { PaginatedContent } from "app/models/pagination";
import { SapCode } from "app/models/sap-code";
import {
  JobExecutionLogDto,
  JobExecutionLogRequest,
  JobOverview,
} from "app/models/scheduled-jobs";
import { UserSetting } from "app/models/user-setting";

@Injectable()
export class DataService implements OnDestroy {
  private readonly actionUrl: string;
  private deletedEquipmentSubscription!: Subscription;

  public constructor(
    private http: HttpClient,
    _configuration: Configuration
  ) {
    this.actionUrl = _configuration.ServerWithApiUrl + "";
  }

  public searchUser(wbi: string): Observable<Person[]> {
    return this.http
      .get<Person[]>(this.actionUrl + "users/search?wbi=" + wbi)
      .pipe(catchError(this.handleError<Person[]>(`searchUserUser`)));
  }

  public getAllRoles(): Observable<Role[]> {
    return this.http
      .get<Role[]>(this.actionUrl + "authorization/roles/all")
      .pipe(catchError(this.handleError<Role[]>(`getAllRoles`)));
  }

  public getAllRolesForUser(user: Person): Observable<UserRole[]> {
    return this.http
      .get<UserRole[]>(this.actionUrl + "authorization/" + user.wbi + "/roles")
      .pipe(catchError(this.handleError<UserRole[]>(`getAllRolesForUser`)));
  }

  public getAllRolesAllowed(user: Person): Observable<RoleAllowed[]> {
    return this.http
      .get<
        RoleAllowed[]
      >(this.actionUrl + "authorization/" + user.wbi + "/getAllRolesAllowed")
      .pipe(catchError(this.handleError<RoleAllowed[]>(`getAllRolesAllowed`)));
  }

  public addUserRole(
    user: Person,
    role: Role,
    location: Location
  ): Observable<UserRole> {
    return this.http
      .post<UserRole>(
        `${this.actionUrl}authorization/${user.wbi}/${location.id}/${role.id}`,
        ""
      )
      .pipe(catchError(this.handleServerError<UserRole>(`addNewRoleToUser`)));
  }

  public removeUserRole(
    user: Person,
    role: Role,
    location: Location
  ): Observable<boolean> {
    return this.http
      .delete<boolean>(
        `${this.actionUrl}authorization/${user.wbi}/${location.id}/${role.id}`
      )
      .pipe(catchError(this.handleServerError<boolean>(`deleteUserRole`)));
  }

  public createUser(person: Person): Observable<Person> {
    return this.http
      .post<Person>(this.actionUrl + "users", person)
      .pipe(catchError(this.handleServerError<Person>(`createUser`)));
  }

  public saveUser(person: Person): Observable<Person> {
    return this.http
      .post<Person>(this.actionUrl + "users/saveUser", person)
      .pipe(catchError(this.handleServerError<Person>(`saveUser`)));
  }

  public saveUserSetting(item: UserSetting): Observable<UserSetting> {
    return this.http
      .post<UserSetting>(this.actionUrl + "usersetting", item)
      .pipe(catchError(this.handleError<UserSetting>(`saveUserSetting`)));
  }

  public getAllUserSettings(): Observable<UserSetting[]> {
    return this.http
      .get<UserSetting[]>(this.actionUrl + "usersetting/all")
      .pipe(catchError(this.handleError<UserSetting[]>(`getAllUserSettings`)));
  }

  public getSettingForUser(
    userId: string,
    page: string
  ): Observable<UserSetting> {
    return this.http
      .get<UserSetting>(this.actionUrl + "usersetting/" + userId + "/" + page)
      .pipe(catchError(this.handleError<UserSetting>("getSettingForUser")));
  }

  public getAllGlossaryItems(): Observable<GlossaryItem[]> {
    return this.http
      .get<GlossaryItem[]>(this.actionUrl + "glossary/all", {
        headers: { "Content-Type": "application/json" },
      })
      .pipe(
        catchError(this.handleError<GlossaryItem[]>(`getAllGlossaryItems`))
      );
  }

  public getAllSapCodes(): Observable<SapCode[]> {
    return this.http
      .get<SapCode[]>(this.actionUrl + "sapcode/all")
      .pipe(catchError(this.handleError<SapCode[]>(`getAllSapCodes`)));
  }

  public saveSapCode(sapcode: SapCode): Observable<SapCode> {
    return this.http
      .post<SapCode>(this.actionUrl + "sapcode/save", sapcode)
      .pipe(catchError(this.handleServerError<SapCode>(`saveSapCode`)));
  }

  public deleteSapCode(sapcode: SapCode): Observable<SapCode> {
    return this.http
      .delete<SapCode>(this.actionUrl + "sapcode/" + sapcode.plantCode)
      .pipe(catchError(this.handleServerError<SapCode>(`saveSapCode`)));
  }

  public savePageColumnConfiguration(
    pageColumnsConfiguration: PageColumnsConfiguration
  ): Observable<PageColumnsConfiguration> {
    const savedObject = pageColumnsConfiguration.convertToJsonFormat(
      pageColumnsConfiguration
    );
    delete savedObject.configuration.configurationsByName[""];
    return this.http
      .post<PageColumnsConfiguration>(
        this.actionUrl + "pageColumnsConfiguration",
        savedObject
      )
      .pipe(
        catchError(
          this.handleServerError<PageColumnsConfiguration>(
            `savePageColumnsConfiguration`
          )
        )
      );
  }

  public getConfigurationItemForKey(
    key: string
  ): Observable<ConfigurationItem> {
    return this.http
      .get<ConfigurationItem>(this.actionUrl + "configurationvalue/" + key)
      .pipe(
        catchError(
          this.handleError<ConfigurationItem>(`getConfigurationItemByKey`)
        )
      );
  }

  public getIEMDMVersionInfo(): Observable<VersionInfo[]> {
    return this.http
      .get<VersionInfo[]>(this.actionUrl + "version/info")
      .pipe(catchError(this.handleError<VersionInfo[]>(`getVersionInfo`)));
  }

  public getAllUnreadNotifications(): Observable<Notification[]> {
    return this.http
      .get<Notification[]>(this.actionUrl + "notification/unread")
      .pipe(
        catchError(
          this.handleError<Notification[]>(`getAllUnreadNotifications`)
        )
      );
  }

  public getHomePageCount(): Observable<HomePageCount> {
    return this.http
      .get<HomePageCount>(this.actionUrl + "homepagecount")
      .pipe(catchError(this.handleError<HomePageCount>(`getHomePageCount`)));
  }

  public getAllNotifications(): Observable<Notification[]> {
    return this.http
      .get<Notification[]>(this.actionUrl + "notification/all")
      .pipe(
        catchError(
          this.handleError<Notification[]>(`getAllUnreadNotifications`)
        )
      );
  }

  public submitNotificationRead(
    notification: Notification
  ): Observable<Notification> {
    notification.read = true;
    return this.http.post<Notification>(
      this.actionUrl + "notification/",
      notification
    );
  }

  public acknowledgeAllNotification(
    notifications: Notification[]
  ): Observable<Notification> {
    return this.http.post<Notification>(
      this.actionUrl + "notification/readAll",
      notifications
    );
  }

  public hasNotificationSystemWarnings(): Observable<boolean> {
    return this.http.get<boolean>(
      this.actionUrl + "notification/hasSysWarnings"
    );
  }

  public get3WeeksForLocation(locationId: number): Observable<string> {
    return this.http
      .get(this.actionUrl + "notification/weekly/" + locationId, {
        responseType: "text",
      })
      .pipe(catchError(this.handleError<string>(`get3WeeksForLocation`)));
  }

  public getNxpProductionYearForAllYears(): Observable<NxpProductionYear[]> {
    return this.http
      .get<NxpProductionYear[]>(this.actionUrl + "nxpproductionyear/all")
      .pipe(
        catchError(
          this.handleError<NxpProductionYear[]>(
            `getNxpProductionYearForAllYears`
          )
        )
      );
  }

  public getProdWeekNumbersNXP(year: number): Observable<Array<string[]>> {
    return this.http
      .get<Array<string[]>>(this.actionUrl + "productionweeknumber/" + year)
      .pipe(
        catchError(this.handleError<Array<string[]>>(`getProdWeekNumberNXP`))
      );
  }

  // Year and startdate obligated
  public submitNxpProductionYear(
    nxpProdYear: NxpProductionYear
  ): Observable<NxpProductionYear> {
    return this.http.post<NxpProductionYear>(
      this.actionUrl + "nxpproductionyear",
      JSON.parse(nxpProdYear.toJSON())
    );
  }

  public getLookupTables(): Observable<Map<string, Map<string, string>>> {
    return this.http
      .get<
        Map<string, Map<string, string>>
      >(this.actionUrl + "lookuptables/all")
      .pipe(
        catchError(
          this.handleError<Map<string, Map<string, string>>>(`getLookupTables`)
        )
      );
  }

  public getGlobalLookupData(): Observable<GlobalLookupData> {
    return this.http
      .get<GlobalLookupData>(this.actionUrl + "lookupdata/global")
      .pipe(
        catchError(this.handleError<GlobalLookupData>(`getGlobalLookupData`))
      );
  }

  public getLocalLookupData(siteId: number): Observable<LocalLookupData> {
    return this.http
      .get<LocalLookupData>(this.actionUrl + "lookupdata/local/" + siteId)
      .pipe(
        catchError(this.handleError<LocalLookupData>(`getGlobalLookupData`))
      );
  }

  public downloadUsersExcel(): string {
    return this.actionUrl + "users/downloadExcel";
  }

  public alertIeMdm(msg: string): Observable<void> {
    const url = this.actionUrl + "manualTrigger/iemdmAlert?msg=" + msg;
    return this.http
      .get<void>(url)
      .pipe(catchError(this.handleServerError<void>(`alertIeMdm`)));
  }

  public manualTriggerUpdate(upd: string): Observable<string> {
    const url = this.actionUrl + "manualTrigger/update?update=" + upd;
    return this.http
      .get<string>(url)
      .pipe(catchError(this.handleServerError<string>(`manualTriggerUpdate`)));
  }

  public fetchWorkFlow(
    part12nc?: string | null,
    sapCode?: string,
    wbi?: string
  ): Observable<void> {
    const url = `${this.actionUrl}wsClient/fetchWorkFlow/${part12nc}/${sapCode}/${wbi}`;
    return this.http
      .get<void>(url)
      .pipe(catchError(this.handleServerError<void>(`fetchWorkFlow`)));
  }

  public scheduledJobOverviews(): Observable<JobOverview[]> {
    const url = this.actionUrl + "scheduling/scheduled-job-overviews";
    return this.http
      .get<JobOverview[]>(url)
      .pipe(
        catchError(this.handleError<JobOverview[]>(`scheduledJobOverviews`))
      );
  }

  public scheduledJobOverviewsAudit(
    jobName: string,
    triggerName: string | undefined = undefined
  ): Observable<JobOverview[]> {
    let url: string;
    if (triggerName === undefined) {
      url = this.actionUrl + `scheduling/job-overview-audit/${jobName}`;
    } else {
      url =
        this.actionUrl +
        `scheduling/job-overview-audit/${jobName}/${triggerName}`;
    }

    return this.http
      .get<JobOverview[]>(url)
      .pipe(
        catchError(this.handleError<JobOverview[]>(`scheduledJobOverviews`))
      );
  }

  public scheduledJobExecutionLogs(
    jobExecutionLogRequest: JobExecutionLogRequest
  ): Observable<JobExecutionLogDto[]> {
    const url = this.actionUrl + "scheduling/job-execution-logs";
    return this.http
      .post<JobExecutionLogDto[]>(url, jobExecutionLogRequest)
      .pipe(
        catchError(
          this.handleServerError<JobExecutionLogDto[]>(
            `scheduledJobExecutionLogs`
          )
        )
      );
  }

  public getAvailableJobs(): Observable<JobOverview[]> {
    const url = this.actionUrl + "scheduling/available-jobs";
    return this.http
      .get<JobOverview[]>(url)
      .pipe(
        catchError(this.handleServerError<JobOverview[]>(`getAvailableJobs`))
      );
  }

  public createJob(jobOverview: JobOverview): Observable<string> {
    const url = this.actionUrl + "scheduling/jobDetail";
    return this.http
      .post<string>(url, jobOverview)
      .pipe(catchError(this.handleServerError<string>(`createJobDetail`)));
  }

  public pauseCronTrigger(triggerName: string): Observable<string> {
    const url = this.actionUrl + `scheduling/trigger/pause/${triggerName}`;
    return this.http
      .put<string>(url, null)
      .pipe(catchError(this.handleServerError<string>(`pauseCronTrigger`)));
  }

  public resumeCronTrigger(triggerName: string): Observable<string> {
    const url = this.actionUrl + `scheduling/trigger/resume/${triggerName}`;
    return this.http
      .get<string>(url)
      .pipe(catchError(this.handleServerError<string>(`resumeCronTrigger`)));
  }

  public runJob(jobOverview: JobOverview): Observable<boolean> {
    const url = this.actionUrl + "scheduling/run-job";
    return this.http
      .post<boolean>(url, jobOverview)
      .pipe(catchError(this.handleServerError<boolean>(`runJob`)));
  }

  public deleteTrigger(jobOverview: JobOverview): Observable<boolean> {
    const url =
      this.actionUrl + `scheduling/trigger/${jobOverview.triggerName}`;
    return this.http
      .delete<boolean>(url)
      .pipe(catchError(this.handleServerError<boolean>(`runJob`)));
  }

  public genericSearchPerson(
    searchValues: GenericSearchArguments,
    page: number,
    size: number
  ): Observable<PaginatedContent<Person>> {
    return this.http
      .post<
        PaginatedContent<Person>
      >(this.actionUrl + "genericSearch/person" + "?page=" + page + "&size=" + size, searchValues)
      .pipe(
        catchError(
          this.handleError<PaginatedContent<Person>>(`genericSearchPerson`)
        )
      );
  }

  public postFix(status: string): string {
    const pctStatuses = [
      "c_site_down",
      "c_virt_cap",
      "c_rg_up_site_down",
      "c_manual_reduce_capacity",
    ];

    return pctStatuses.indexOf(status) > -1 ? "%" : "";
  }

  public round(value: number): number {
    return Math.round(value * 10) / 10;
  }

  public async searchLocations(
    searchValues: GenericSearchArguments,
    page: number,
    size: number
  ): Promise<PaginatedContent<Location>> {
    return firstValueFrom(
      this.http.post<PaginatedContent<Location>>(
        this.actionUrl +
          "genericSearch/location" +
          "?page=" +
          page +
          "&size=" +
          size,
        searchValues
      )
    );
  }

  public searchManufacturerCodes(
    searchValues: GenericSearchArguments,
    page: number,
    size: number
  ): Observable<PaginatedContent<Manufacturer>> {
    return this.http
      .post<
        PaginatedContent<Manufacturer>
      >(`${this.actionUrl}genericSearch/manufacturer?page=${page}&size=${size}`, searchValues)
      .pipe(
        catchError(
          this.handleError<PaginatedContent<Manufacturer>>(
            `searchManufacturerCodes`
          )
        )
      );
  }

  public ngOnDestroy(): void {
    if (this.deletedEquipmentSubscription != null) {
      this.deletedEquipmentSubscription.unsubscribe();
    }
  }

  // ---------- private methods ----------------------------------

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param _operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T>(_operation = "operation", result?: T) {
    return (): Observable<T> => {
      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param _operation - name of the operation that failed
   */
  private handleServerError<T>(_operation = "operation") {
    return (error: HttpErrorResponse): Observable<T> => {
      let errMsg = error.error;

      if (typeof errMsg === "object") {
        if (Object.hasOwn(errMsg, "error")) {
          errMsg =
            errMsg.status.toString() +
            ": " +
            errMsg.path.toString() +
            ", " +
            errMsg.error.toString();
        } else {
          errMsg = "Something went wrong";
        }
      }
      // Let the app keep running by returning an empty result.
      return throwError(() => new Error(errMsg));
    };
  }

  private dateWithoutTimeToIsoString(date: Date): string {
    return (
      date.getFullYear() +
      "-" +
      this.pad(date.getMonth() + 1, 2, "0") +
      "-" +
      this.pad(date.getDate(), 2, "0")
    );
  }

  private isoStringWithoutTimeToDate(isoString: string): Date {
    const parts = isoString.split("-");

    return new Date(
      Number(parts[0]),
      Number(parts[1]) - 1,
      Number(parts[2]),
      0,
      0,
      0,
      0
    );
  }

  private pad(num: number, size: number, pad: string): string {
    let s = "" + num;
    while (s.length < size) {
      s = pad + num;
    }
    return s;
  }
}
