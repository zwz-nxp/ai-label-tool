import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { TableIconButtonComponent } from "app/shared/table-icon-button/table-icon-button.component";
import { SuperMiniFabButtonComponent } from "app/shared/super-mini-fab-button/super-mini-fab-button.component";
import { SpinnerComponent } from "app/shared/spinner/spinner.component";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { DualListBoxComponent } from "./dual-list-box/dual-list-box.component";
import { MatButtonModule } from "@angular/material/button";
import { TrimInputDirective } from "app/utils/directives/trim-input.directive";
import { SelectCheckAllComponent } from "./select-check-all/select-check-all.component";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatNativeDateModule, MatPseudoCheckbox } from "@angular/material/core";
import { GenericSearchComponent } from "./generic-search/generic-search.component";
import { GenericSearchPopupComponent } from "./generic-search/generic-search-popup/generic-search-popup.component";
import { GenericSearchMultiSelectPipe } from "app/utils/pipes/generic-search-multi-select.pipe";
import { GenericSearchBooleanSelectPipe } from "app/utils/pipes/generic-search-boolean-select.pipe";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatDialogModule } from "@angular/material/dialog";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AuthorizationPipe } from "app/utils/pipes/authorization.pipe";
import { BooleanInterpreterPipe } from "app/utils/pipes/boolean-interpreter.pipe";
import { UserNamePipe } from "app/utils/pipes/user-name.pipe";
import { DateFormatterPipe } from "app/utils/pipes/date-formatter.pipe";
import { MatCardModule } from "@angular/material/card";
import {
  CalendarModule,
  DateAdapter as CalendarDateAdapter,
} from "angular-calendar";
import { adapterFactory } from "angular-calendar/date-adapters/date-fns";
import { ActionInterpreterPipe } from "app/utils/pipes/action-interpreter.pipe";
import { OrDashesPipe } from "app/utils/pipes/dashes.pipe";
import { RoundFivePipe } from "app/utils/pipes/round5.pipe";
import { PercentagePipe } from "app/utils/pipes/percentage.pipe";
import { SearchableSelectComponent } from "./searchable-select/searchable-select.component";
import { NotificationsModalComponent } from "./notifications-modal/notifications-modal.component";
import { NotificationViewerComponent } from "./notifications-modal/notification-viewer/notification-viewer.component";
import { MatTableModule } from "@angular/material/table";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatSortModule } from "@angular/material/sort";
import { PageTitleComponent } from "./page-title/page-title.component";
import { LocationTitlePipe } from "app/utils/pipes/location-title.pipe";
import { SapCodePipe } from "app/utils/pipes/sapcode.pipe";
import { NgxMatSelectSearchModule } from "ngx-mat-select-search";
import { CdkDrag, CdkDragHandle } from "@angular/cdk/drag-drop";

@NgModule({
  declarations: [
    TableIconButtonComponent,
    SpinnerComponent,
    DualListBoxComponent,
    SuperMiniFabButtonComponent,
    SelectCheckAllComponent,
    GenericSearchComponent,
    GenericSearchPopupComponent,
    SearchableSelectComponent,
    NotificationsModalComponent,
    NotificationViewerComponent,
    PageTitleComponent,
    TrimInputDirective,
    GenericSearchMultiSelectPipe,
    GenericSearchBooleanSelectPipe,
    AuthorizationPipe,
    BooleanInterpreterPipe,
    UserNamePipe,
    DateFormatterPipe,
    ActionInterpreterPipe,
    OrDashesPipe,
    RoundFivePipe,
    PercentagePipe,
    LocationTitlePipe,
    SapCodePipe,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    CalendarModule.forRoot({
      provide: CalendarDateAdapter,
      useFactory: adapterFactory,
    }),
    MatIconModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatCheckboxModule,
    MatPseudoCheckbox,
    MatTooltipModule,
    MatInputModule,
    MatSelectModule,
    MatToolbarModule,
    MatDialogModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatTooltipModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    NgxMatSelectSearchModule,
    CdkDrag,
    CdkDragHandle,
  ],
  exports: [
    TableIconButtonComponent,
    SpinnerComponent,
    DualListBoxComponent,
    SuperMiniFabButtonComponent,
    SelectCheckAllComponent,
    GenericSearchComponent,
    GenericSearchPopupComponent,
    SearchableSelectComponent,
    NotificationsModalComponent,
    NotificationViewerComponent,
    PageTitleComponent,
    TrimInputDirective,
    GenericSearchMultiSelectPipe,
    GenericSearchBooleanSelectPipe,
    AuthorizationPipe,
    BooleanInterpreterPipe,
    UserNamePipe,
    DateFormatterPipe,
    ActionInterpreterPipe,
    OrDashesPipe,
    RoundFivePipe,
    PercentagePipe,
    LocationTitlePipe,
    SapCodePipe,
  ],
})
export class SharedModule {}
