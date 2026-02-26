import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSelectModule } from "@angular/material/select";
import { MatTableModule } from "@angular/material/table";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatPaginatorModule } from "@angular/material/paginator";
import { StoreModule } from "@ngrx/store";
import { EffectsModule } from "@ngrx/effects";

import { ModelParamRoutingModule } from "./model-param-routing.module";
import { ModelParamListComponent } from "./model-param-list/model-param-list.component";
import { ModelParamFormDialogComponent } from "./model-param-form-dialog/model-param-form-dialog.component";
import { ModelParamDetailDialogComponent } from "./model-param-detail-dialog/model-param-detail-dialog.component";
import { ModelParamDeleteDialogComponent } from "./model-param-delete-dialog/model-param-delete-dialog.component";
import {
  ModelParamEffects,
  modelParamReducer,
} from "app/state/landingai/model-param";

@NgModule({
  declarations: [
    ModelParamListComponent,
    ModelParamFormDialogComponent,
    ModelParamDetailDialogComponent,
    ModelParamDeleteDialogComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ModelParamRoutingModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTableModule,
    MatTooltipModule,
    MatPaginatorModule,
    StoreModule.forFeature("modelParam", modelParamReducer),
    EffectsModule.forFeature([ModelParamEffects]),
  ],
})
export class ModelParamModule {}
