import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Routes } from "@angular/router";
import { UserOverviewComponent } from "./user-overview/user-overview.component";
import {
  MAT_TOOLTIP_DEFAULT_OPTIONS,
  MatTooltipDefaultOptions,
  MatTooltipModule,
} from "@angular/material/tooltip";
import { MatIconModule } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { MatPaginatorModule } from "@angular/material/paginator";
import { SharedModule } from "app/shared/shared.module";
import { MatButtonModule } from "@angular/material/button";
import { MatSortModule } from "@angular/material/sort";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatDialogModule } from "@angular/material/dialog";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatStepperModule } from "@angular/material/stepper";
import { MatSelectModule } from "@angular/material/select";
import { UserCrudComponent } from "app/components/crud/user-crud/user-crud.component";
import { GlossaryComponent } from "./glossary/glossary.component";
import { SysAdminToolsComponent } from "./sys-admin-tools/sys-admin-tools.component";
import { MatCardModule } from "@angular/material/card";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { ScheduledJobsComponent } from "./scheduled-jobs/scheduled-jobs.component";
import { MatTabsModule } from "@angular/material/tabs";
import { JobExecutionLogsComponent } from "./scheduled-jobs/job-execution-logs/job-execution-logs.component";
import { HelpScreenComponent } from "./scheduled-jobs/help-screen/help-screen.component";
import { DragDropModule } from "@angular/cdk/drag-drop";
import { NgxMatSelectSearchModule } from "ngx-mat-select-search";
import { TranslateTimezonePipe } from "app/utils/pipes/translate-timezone.pipe";
import { EditJobModalComponent } from "app/modules/settings/scheduled-jobs/edit-job-modal/edit-job-modal.component";
import { EditTriggerModalComponent } from "app/modules/settings/scheduled-jobs/edit-trigger-modal/edit-trigger-modal.component";

const routes: Routes = [
  { path: "user-management", component: UserOverviewComponent },
  { path: "glossary", component: GlossaryComponent },
  { path: "sys-admin-tools", component: SysAdminToolsComponent },
  { path: "scheduled-jobs", component: ScheduledJobsComponent },
];

const tooltipDefaultOptions: MatTooltipDefaultOptions = {
  showDelay: 0,
  hideDelay: 0,
  touchendHideDelay: 0,
  disableTooltipInteractivity: true,
};

@NgModule({
  declarations: [
    UserOverviewComponent,
    UserCrudComponent,
    GlossaryComponent,
    SysAdminToolsComponent,
    ScheduledJobsComponent,
    JobExecutionLogsComponent,
    EditTriggerModalComponent,
    EditJobModalComponent,
    HelpScreenComponent,
    TranslateTimezonePipe,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    SharedModule,
    DragDropModule,
    MatTooltipModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatDialogModule,
    MatToolbarModule,
    MatStepperModule,
    MatSelectModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    NgxMatSelectSearchModule,
  ],
  providers: [
    {
      provide: MAT_TOOLTIP_DEFAULT_OPTIONS,
      useValue: tooltipDefaultOptions,
    },
  ],
})
export class SettingsModule {}
