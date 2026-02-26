import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";

// Angular Material imports
import { MatTableModule } from "@angular/material/table";
import { MatInputModule } from "@angular/material/input";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatCardModule } from "@angular/material/card";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatMenuModule } from "@angular/material/menu";
import { MatSortModule } from "@angular/material/sort";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatDividerModule } from "@angular/material/divider";

// Components
import { ModelComponent } from "./model.component";
import { ModelSearchComponent } from "./model-search/model-search.component";
import { ModelTableComponent } from "./model-table/model-table.component";
import { ModelActionsComponent } from "./model-actions/model-actions.component";

// Model Detail Module
import { ModelDetailModule } from "./model-detail/model-detail.module";

// Test Model Module
import { TestModelModule } from "../test-model/test-model.module";

@NgModule({
  declarations: [
    ModelComponent,
    ModelSearchComponent,
    ModelTableComponent,
    ModelActionsComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,

    // Angular Material modules
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatCardModule,
    MatToolbarModule,
    MatTooltipModule,
    MatMenuModule,
    MatSortModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatDividerModule,

    // Model Detail Module
    ModelDetailModule,

    // Test Model Module
    TestModelModule,
  ],
})
export class ModelModule {}
