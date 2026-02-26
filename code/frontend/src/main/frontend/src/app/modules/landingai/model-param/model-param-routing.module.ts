import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { ModelParamListComponent } from "./model-param-list/model-param-list.component";

const routes: Routes = [
  {
    path: "",
    component: ModelParamListComponent,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ModelParamRoutingModule {}
