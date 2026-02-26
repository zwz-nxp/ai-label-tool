import { Component, Input } from "@angular/core";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { CRUD_ACTION } from "./crud-action";
import { FormGroup } from "@angular/forms";

@Component({ template: "" })
export abstract class CrudComponent extends BaseDialogComponent {
  @Input() public crudAction: CRUD_ACTION = CRUD_ACTION.CREATE;
  public inputForm!: FormGroup;

  public closeDialog(): void {
    super.close();
  }

  public crudActionToText(): string {
    switch (this.crudAction) {
      case CRUD_ACTION.CREATE:
        return "Add";
      case CRUD_ACTION.DELETE:
        return "Delete";
      case CRUD_ACTION.UPDATE:
        return "Edit";
    }
  }

  public getSubmitLabel(): string {
    return this.crudAction === CRUD_ACTION.DELETE ? "Delete" : "Save";
  }

  public abstract create(): void;

  public abstract delete(): void;

  public abstract update(): void;

  public abstract fillForm(): void;

  public abstract submit(): void;
}
