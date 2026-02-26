import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
import { AppComponent } from "./app.component";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { DataService } from "./utils/api-access/data-service";
import {
  HTTP_INTERCEPTORS,
  HttpClient,
  HttpClientModule,
} from "@angular/common/http";
import { Configuration } from "./utils/configuration";
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
} from "@angular/material/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthorizationService } from "./utils/services/authorization.service";
import { AboutComponent } from "./components/about/about.component";
import { SwitchUserDialogComponent } from "./components/switch-user-dialog/switch-user-dialog.component";
import { DragDropModule } from "@angular/cdk/drag-drop";
import { ConfirmDialogComponent } from "./components/dialogs/confirm-dialog/confirm-dialog.component";
import { MassUploadResponseComponent } from "./components/dialogs/mass-upload-response/mass-upload-response.component";
import { ConfirmDialogService } from "./utils/services/confirm-dialog.service";
import { HomeDashboardComponent } from "./components/home-dashboard/home-dashboard.component";
import { NgIdleKeepaliveModule } from "@ng-idle/keepalive";
import { IdleTimeoutComponent } from "./components/dialogs/idle-timeout/idle-timeout.component";
import { LayoutModule } from "@angular/cdk/layout";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { IdleService } from "./utils/services/idle.service";
import { DataUpdateService } from "./utils/services/data-update.service";
import { MatButtonModule } from "@angular/material/button";
import { MatInputModule } from "@angular/material/input";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatMenuModule } from "@angular/material/menu";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatCardModule } from "@angular/material/card";
import { MatIconModule } from "@angular/material/icon";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatBadgeModule } from "@angular/material/badge";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatTableModule } from "@angular/material/table";
import { MatTabsModule } from "@angular/material/tabs";
import { MatSortModule } from "@angular/material/sort";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MassUploadComponent } from "./components/mass-upload/mass-upload.component";
import { FilterMassUploadTypesPipe } from "./utils/pipes/filter-mass-upload-types.pipe";
import { SharedModule } from "app/shared/shared.module";
import { NgxMatSelectSearchModule } from "ngx-mat-select-search";
import { CommonModule, NgOptimizedImage } from "@angular/common";
import { MatProgressBar } from "@angular/material/progress-bar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { enGB } from "date-fns/locale";
import { DATE_FORMATS } from "app/utils/date-configuration";
import { RxStompService } from "app/utils/services/rx-stomp.service";
import { rxStompServiceFactory } from "app/utils/rx-stomp-service-factory";
import { ApiErrorInterceptor } from "app/utils/interceptors/api-error.interceptor";
import { CredentialsInterceptor } from "app/utils/interceptors/credentials.interceptor";
import { StoreModule } from "@ngrx/store";
import { EffectsModule } from "@ngrx/effects";
import {
  AppState,
  CurrentUserEffects,
  SapCodeEffects,
  UserRoleEffects,
} from "app/state";
import { StoreDevtoolsModule } from "@ngrx/store-devtools";
import { UserService } from "app/services/user.service";
import { UserRoleService } from "app/services/user-role.service";
import { LocationService } from "app/services/location.service";
import { LocationEffects } from "app/state/location";
import { SapCodeService } from "app/services/sap-code.service";
import { SystemEffects } from "app/state/system";
import { SystemService } from "app/services/system.service";
import { HomeEffects } from "app/state/landingai/home";
import { ModelEffects } from "app/state/landingai/model/model.effects";
import { ModelDetailEffects } from "app/state/landingai/model/model-detail/model-detail.effects";
import { ImageUploadEffects } from "app/state/landingai/image-upload";
import { SnapshotListEffects } from "app/state/landingai/snapshot-list";
import { ProjectService } from "app/services/landingai/project.service";
import { ImageService } from "app/services/landingai/image.service";
import { environment } from "environments/environment";
import { GenericSearchUtils } from "app/models/generic-search";
import { AppUtils } from "app/utils/app-utils";
import { DateFnsAdapter } from "@angular/material-date-fns-adapter";
import { DateSerializationInterceptor } from "app/utils/interceptors/date-serialization.interceptor";

const appRoutes: Routes = [
  { path: "", component: HomeDashboardComponent },
  { path: "mass-upload", component: MassUploadComponent },
  {
    path: "admin",
    loadChildren: () =>
      import("./modules/admin/admin.module").then((m) => m.AdminModule),
  },
  {
    path: "settings",
    loadChildren: () =>
      import("./modules/settings/settings.module").then(
        (m) => m.SettingsModule
      ),
  },
  {
    path: "profile",
    loadChildren: () =>
      import("./modules/profile/profile.module").then((m) => m.ProfileModule),
  },
  {
    path: "landingai",
    loadChildren: () =>
      import("./modules/landingai/landingai.module").then(
        (m) => m.LandingAIModule
      ),
  },
  {
    path: "labeling",
    loadChildren: () =>
      import("./modules/landingai/labelling/labelling.module").then(
        (m) => m.LabellingModule
      ),
  },
];

@NgModule({
  declarations: [
    AppComponent,
    HomeDashboardComponent,
    AboutComponent,
    ConfirmDialogComponent,
    IdleTimeoutComponent,
    MassUploadComponent,
    MassUploadResponseComponent,
    FilterMassUploadTypesPipe,
    SwitchUserDialogComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(
      appRoutes,
      { enableTracing: false } // <-- debugging purposes only
    ),
    BrowserModule,
    HttpClientModule,
    DragDropModule,
    NgIdleKeepaliveModule.forRoot(),
    LayoutModule,
    BrowserAnimationsModule,
    NgxMatSelectSearchModule,
    SharedModule,
    NgOptimizedImage,
    MatTooltipModule,
    MatButtonModule,
    MatBadgeModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatMenuModule,
    MatToolbarModule,
    MatCardModule,
    MatIconModule,
    MatDialogModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatPaginatorModule,
    MatTableModule,
    MatTabsModule,
    MatSortModule,
    MatProgressBar,
    MatProgressSpinnerModule,
    StoreModule.forRoot(AppState),
    EffectsModule.forRoot([
      CurrentUserEffects,
      LocationEffects,
      SapCodeEffects,
      SystemEffects,
      UserRoleEffects,
      HomeEffects,
      ModelEffects,
      ModelDetailEffects,
      ImageUploadEffects,
      SnapshotListEffects,
    ]),
    !environment.production
      ? StoreDevtoolsModule.instrument({ maxAge: 25 })
      : [],
  ],
  providers: [
    DataService,
    UserService,
    LocationService,
    SapCodeService,
    SystemService,
    UserRoleService,
    ProjectService,
    ImageService,
    HttpClient,
    Configuration,
    DataUpdateService,
    AuthorizationService,
    ConfirmDialogService,
    IdleService,
    AppUtils,
    GenericSearchUtils,
    {
      provide: MatDialogRef,
      useValue: {},
    },
    {
      provide: DateAdapter,
      useClass: DateFnsAdapter,
      deps: [MAT_DATE_LOCALE],
    },
    { provide: MAT_DATE_FORMATS, useValue: DATE_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: enGB },
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CredentialsInterceptor,
      multi: true,
    },
    { provide: HTTP_INTERCEPTORS, useClass: ApiErrorInterceptor, multi: true },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: DateSerializationInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
