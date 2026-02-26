import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatSliderModule } from "@angular/material/slider";
import { MatTabsModule } from "@angular/material/tabs";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatSnackBarModule } from "@angular/material/snack-bar";

import { TestModelDialogComponent } from "./test-model-dialog.component";
import { TestModelUploadZoneComponent } from "./test-model-upload-zone/test-model-upload-zone.component";
import { FeaturedImageComponent } from "./featured-image/featured-image.component";
import { PredictionPanelComponent } from "./prediction-panel/prediction-panel.component";
import { ImageThumbnailListComponent } from "./image-thumbnail-list/image-thumbnail-list.component";

@NgModule({
  declarations: [
    TestModelDialogComponent,
    TestModelUploadZoneComponent,
    FeaturedImageComponent,
    PredictionPanelComponent,
    ImageThumbnailListComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSliderModule,
    MatTabsModule,
    MatExpansionModule,
    MatTooltipModule,
    MatSnackBarModule,
  ],
  exports: [TestModelDialogComponent],
})
export class TestModelModule {}
