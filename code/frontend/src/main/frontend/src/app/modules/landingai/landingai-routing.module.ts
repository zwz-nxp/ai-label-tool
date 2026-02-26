import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { ProjectListComponent } from "./home/project-list/project-list.component";
import { ProjectCreateComponent } from "./home/project-create/project-create.component";
import { ModelComponent } from "./model/model.component";
import { ImageUploadPageComponent } from "./image-upload/image-upload-page.component";
import { SnapshotListViewComponent } from "./snapshot-list-view/snapshot-list-view.component";
import { LandingAiAuthGuard } from "./landingai-auth.guard";

const routes: Routes = [
  {
    path: "",
    redirectTo: "projects",
    pathMatch: "full",
  },
  {
    path: "projects",
    component: ProjectListComponent,
    canActivate: [LandingAiAuthGuard],
  },
  {
    path: "projects/create",
    component: ProjectCreateComponent,
    canActivate: [LandingAiAuthGuard],
  },
  {
    path: "projects/:id",
    component: ImageUploadPageComponent,
    canActivate: [LandingAiAuthGuard],
  },
  {
    path: "projects/:id/snapshots",
    component: SnapshotListViewComponent,
    canActivate: [LandingAiAuthGuard],
  },
  {
    path: "model-params",
    canActivate: [LandingAiAuthGuard],
    loadChildren: () =>
      import("./model-param/model-param.module").then(
        (m) => m.ModelParamModule
      ),
  },
  {
    path: "labelling",
    canActivate: [LandingAiAuthGuard],
    loadChildren: () =>
      import("./labelling/labelling.module").then((m) => m.LabellingModule),
  },
  {
    path: "ai-training",
    canActivate: [LandingAiAuthGuard],
    loadChildren: () =>
      import("./ai-training/ai-training.module").then(
        (m) => m.AiTrainingModule
      ),
  },
  {
    path: "model/:projectId",
    component: ModelComponent,
    canActivate: [LandingAiAuthGuard],
  },
  {
    path: "model/project/:projectId",
    component: ModelComponent,
    canActivate: [LandingAiAuthGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LandingAIRoutingModule {}
