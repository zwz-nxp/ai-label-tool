import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Routes } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatListModule } from "@angular/material/list";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatSelectModule } from "@angular/material/select";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSliderModule } from "@angular/material/slider";
import { MatSlideToggleModule } from "@angular/material/slide-toggle";
import { MatDialogModule } from "@angular/material/dialog";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatChipsModule } from "@angular/material/chips";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { TextFieldModule } from "@angular/cdk/text-field";
import { SharedModule } from "app/shared/shared.module";
import { ImageLabellingComponent } from "./image-labelling/image-labelling.component";
import { LeftToolbarComponent } from "./left-toolbar/left-toolbar.component";
import { GeneralBlockComponent } from "./general-block/general-block.component";
import { TagsBlockComponent } from "./tags-block/tags-block.component";
import { MetadataBlockComponent } from "./metadata-block/metadata-block.component";
import { AddMetadataDialogComponent } from "./add-metadata-dialog/add-metadata-dialog.component";
import { LabelsBlockComponent } from "./labels-block/labels-block.component";
import { PredictionsBlockComponent } from "./predictions-block/predictions-block.component";
import { TopToolbarComponent } from "./top-toolbar/top-toolbar.component";
import { ClassCreationDialogComponent } from "./class-creation-dialog/class-creation-dialog.component";
import { EnhanceDialogComponent } from "./enhance-dialog/enhance-dialog.component";
import { AnnotationCanvasComponent } from "./annotation-canvas/annotation-canvas.component";
import { ObjectDetectionCanvasComponent } from "./annotation-canvas/object-detection-canvas/object-detection-canvas.component";
import { SegmentationCanvasComponent } from "./annotation-canvas/segmentation-canvas/segmentation-canvas.component";
import { ClassificationCanvasComponent } from "./annotation-canvas/classification-canvas/classification-canvas.component";

const routes: Routes = [
  { path: ":projectId/:imageId", component: ImageLabellingComponent },
];

@NgModule({
  declarations: [
    ImageLabellingComponent,
    LeftToolbarComponent,
    GeneralBlockComponent,
    TagsBlockComponent,
    MetadataBlockComponent,
    AddMetadataDialogComponent,
    LabelsBlockComponent,
    PredictionsBlockComponent,
    TopToolbarComponent,
    ClassCreationDialogComponent,
    EnhanceDialogComponent,
    AnnotationCanvasComponent,
    ObjectDetectionCanvasComponent,
    SegmentationCanvasComponent,
    ClassificationCanvasComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
    SharedModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatCardModule,
    MatCheckboxModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatDialogModule,
    MatTooltipModule,
    MatChipsModule,
    MatExpansionModule,
    MatProgressSpinnerModule,
    MatButtonToggleModule,
    MatAutocompleteModule,
    MatSnackBarModule,
    TextFieldModule,
  ],
  exports: [
    ImageLabellingComponent,
    LeftToolbarComponent,
    GeneralBlockComponent,
    TagsBlockComponent,
    MetadataBlockComponent,
    LabelsBlockComponent,
    PredictionsBlockComponent,
    TopToolbarComponent,
    ClassCreationDialogComponent,
    EnhanceDialogComponent,
    AnnotationCanvasComponent,
    ObjectDetectionCanvasComponent,
    SegmentationCanvasComponent,
    ClassificationCanvasComponent,
  ],
})
export class LabellingModule {}
