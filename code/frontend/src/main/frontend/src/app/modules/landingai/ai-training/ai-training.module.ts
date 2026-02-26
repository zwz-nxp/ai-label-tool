import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule, Routes } from "@angular/router";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";

// NgRx
import { StoreModule } from "@ngrx/store";
import { EffectsModule } from "@ngrx/effects";

// Angular Material Modules
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatCardModule } from "@angular/material/card";
import { MatSelectModule } from "@angular/material/select";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSliderModule } from "@angular/material/slider";
import { MatDialogModule } from "@angular/material/dialog";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatMenuModule } from "@angular/material/menu";
import { MatSnackBarModule } from "@angular/material/snack-bar";

// Shared Module
import { SharedModule } from "app/shared/shared.module";

// State Management
import {
  TRAINING_FEATURE_KEY,
  TrainingEffects,
  trainingReducer,
} from "app/state/landingai/ai-training";

// Components - will be added as they are created
// import { CustomerTrainingComponent } from './customer-training/customer-training.component';

/**
 * AI Training Module
 *
 * This module provides the Customer Training functionality for configuring
 * and starting AI model training tasks. It includes:
 * - Data setup page (split configuration, preview)
 * - Model configuration page (hyperparameters, transforms, augmentations)
 * - Training launch functionality
 *
 * Routes:
 * - :projectId - Main training configuration page for a specific project
 */

const routes: Routes = [
  {
    path: ":projectId",
    // component: CustomerTrainingComponent, // Will be uncommented when component is created
    loadComponent: () =>
      import("./customer-training/customer-training.component").then(
        (m) => m.CustomerTrainingComponent
      ),
  },
];

@NgModule({
  declarations: [
    // Components will be added here as they are created
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    SharedModule,
    // NgRx State Management
    StoreModule.forFeature(TRAINING_FEATURE_KEY, trainingReducer),
    EffectsModule.forFeature([TrainingEffects]),
    // Angular Material Modules
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatSliderModule,
    MatDialogModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatButtonToggleModule,
    MatCheckboxModule,
    MatMenuModule,
    MatSnackBarModule,
  ],
  exports: [
    // Components will be exported here as they are created
  ],
})
export class AiTrainingModule {}
