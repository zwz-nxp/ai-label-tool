import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Routes } from "@angular/router";
import { SiteManagementComponent } from "./site-management/site-management.component";
import { MatIconModule } from "@angular/material/icon";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import { SharedModule } from "app/shared/shared.module";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatSortModule } from "@angular/material/sort";
import { SapCodeManagementComponent } from "./sapcode-management/sapcode-management.component";
import { MatTabsModule } from "@angular/material/tabs";
import { SysAdminComponent } from "./sys-admin/sys-admin.component";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AddNxpProductionYearDialogComponent } from "./sys-admin/add-nxp-production-year-dialog/add-nxp-production-year-dialog.component";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatDialogModule } from "@angular/material/dialog";
import { MatInputModule } from "@angular/material/input";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { SapCodeCrudComponent } from "app/components/crud/sap-code-crud/sap-code-crud.component";
import { LocationCrudComponent } from "app/components/crud/location-crud/site-crud/location-crud.component";
import { ManufacturerCrudComponent } from "app/components/crud/location-crud/manufacturer-crud/manufacturer-crud.component";
import { MatSelectModule } from "@angular/material/select";
import { DragDropModule } from "@angular/cdk/drag-drop";
import { LocationAcronymPipe } from "app/utils/pipes/location-acronym.pipe";
import { NgxMatSelectSearchModule } from "ngx-mat-select-search";

const routes: Routes = [
  { path: "sites", component: SiteManagementComponent },
  {
    path: "plant-codes",
    component: SapCodeManagementComponent,
  },
  {
    path: "production-years",
    component: SysAdminComponent,
  },
];

@NgModule({
  declarations: [
    SiteManagementComponent,
    SapCodeManagementComponent,
    SysAdminComponent,
    AddNxpProductionYearDialogComponent,
    SapCodeCrudComponent,
    LocationCrudComponent,
    LocationAcronymPipe,
    ManufacturerCrudComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    SharedModule,
    DragDropModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatSortModule,
    MatTabsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatToolbarModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    NgxMatSelectSearchModule,
  ],
})
export class AdminModule {}
