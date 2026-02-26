import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";

// Material Modules
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatDialogModule } from "@angular/material/dialog";
import { MatChipsModule } from "@angular/material/chips";
import { MatGridListModule } from "@angular/material/grid-list";
import { MatMenuModule } from "@angular/material/menu";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatTableModule } from "@angular/material/table";
import { MatRadioModule } from "@angular/material/radio";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatSliderModule } from "@angular/material/slider";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatDividerModule } from "@angular/material/divider";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { DragDropModule } from "@angular/cdk/drag-drop";

// Routing
import { LandingAIRoutingModule } from "./landingai-routing.module";

// Modules
import { ModelModule } from "./model/model.module";

// Components
import { ProjectListComponent } from "./home/project-list/project-list.component";
import { ProjectCardComponent } from "./home/project-card/project-card.component";
import { ProjectCreateComponent } from "./home/project-create/project-create.component";
import { ProjectEditDialogComponent } from "./home/project-edit-dialog/project-edit-dialog.component";
import { ProjectDeleteDialogComponent } from "./home/project-delete-dialog/project-delete-dialog.component";
import { ImageUploadPageComponent } from "./image-upload/image-upload-page.component";
import { ImageGridComponent } from "./image-upload/image-grid/image-grid.component";
import { ImageCardComponent } from "./image-upload/image-card/image-card.component";
import { ToolbarComponent } from "./image-upload/toolbar/toolbar.component";
import { FilterPanelComponent } from "./image-upload/filter-panel/filter-panel.component";
import { FilterDialogComponent } from "./image-upload/filter-dialog/filter-dialog.component";
import { UploadDialogComponent } from "./image-upload/upload-dialog/upload-dialog.component";
import { UploadClassifiedDialogComponent } from "./image-upload/upload-classified-dialog/upload-classified-dialog.component";
import { UploadBatchDialogComponent } from "./image-upload/upload-batch-dialog/upload-batch-dialog.component";
import { SnapshotCreateDialogComponent } from "./image-upload/snapshot-create-dialog/snapshot-create-dialog.component";
import { SnapshotListDialogComponent } from "./image-upload/snapshot-list-dialog/snapshot-list-dialog.component";
import { ManageClassesDialogComponent } from "./image-upload/manage-classes-dialog/manage-classes-dialog.component";
import { ManageTagsDialogComponent } from "./image-upload/manage-tags-dialog/manage-tags-dialog.component";
import { ManageMetadataDialogComponent } from "./image-upload/manage-metadata-dialog/manage-metadata-dialog.component";
import { BatchSetMetadataDialogComponent } from "./image-upload/batch-set-metadata-dialog/batch-set-metadata-dialog.component";
import { BatchSetTagsDialogComponent } from "./image-upload/batch-set-tags-dialog/batch-set-tags-dialog.component";
import { BatchSetClassDialogComponent } from "./image-upload/batch-set-class-dialog/batch-set-class-dialog.component";
import { AutoSplitDialogComponent } from "./image-upload/auto-split-dialog/auto-split-dialog.component";
import { DownloadProgressDialogComponent } from "./image-upload/download-progress-dialog/download-progress-dialog.component";

// Snapshot List View Components
import { SnapshotListViewComponent } from "./snapshot-list-view/snapshot-list-view.component";
import { SnapshotToolbarComponent } from "./snapshot-list-view/snapshot-toolbar/snapshot-toolbar.component";
import { SnapshotSidebarComponent } from "./snapshot-list-view/snapshot-sidebar/snapshot-sidebar.component";
import { SnapshotCardComponent } from "./snapshot-list-view/snapshot-card/snapshot-card.component";
import { ProjectNameDialogComponent } from "./snapshot-list-view/project-name-dialog/project-name-dialog.component";

// Shared Module
import { SharedModule } from "app/shared/shared.module";

@NgModule({
  declarations: [
    ProjectListComponent,
    ProjectCardComponent,
    ProjectCreateComponent,
    ProjectEditDialogComponent,
    ProjectDeleteDialogComponent,
    ImageUploadPageComponent,
    ImageGridComponent,
    ImageCardComponent,
    ToolbarComponent,
    FilterPanelComponent,
    FilterDialogComponent,
    UploadDialogComponent,
    UploadClassifiedDialogComponent,
    UploadBatchDialogComponent,
    SnapshotCreateDialogComponent,
    SnapshotListDialogComponent,
    ManageClassesDialogComponent,
    ManageTagsDialogComponent,
    ManageMetadataDialogComponent,
    BatchSetMetadataDialogComponent,
    BatchSetTagsDialogComponent,
    BatchSetClassDialogComponent,
    AutoSplitDialogComponent,
    DownloadProgressDialogComponent,
    SnapshotListViewComponent,
    SnapshotToolbarComponent,
    SnapshotSidebarComponent,
    SnapshotCardComponent,
    ProjectNameDialogComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    LandingAIRoutingModule,
    SharedModule,
    ModelModule,

    // Material Modules
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDialogModule,
    MatChipsModule,
    MatGridListModule,
    MatMenuModule,
    MatCheckboxModule,
    MatTableModule,
    MatRadioModule,
    MatButtonToggleModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatDividerModule,
    MatAutocompleteModule,
    DragDropModule,
  ],
})
export class LandingAIModule {}
