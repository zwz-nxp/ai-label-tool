import { Component, Input } from "@angular/core";
import { NgModel } from "@angular/forms";
import { MatPseudoCheckboxState } from "@angular/material/core";

@Component({
  selector: "app-select-check-all",
  templateUrl: "./select-check-all.component.html",
  standalone: false,
})
export class SelectCheckAllComponent {
  @Input() public model: NgModel | undefined;
  @Input() public values?: any[] = [];
  @Input() public text: string = "Select All";

  public getCheckboxState(): MatPseudoCheckboxState {
    return this.isIndeterminate()
      ? "indeterminate"
      : this.model?.value &&
          this.values?.length &&
          this.model.value.length === this.values.length
        ? "checked"
        : "unchecked";
  }

  public isChecked(): MatPseudoCheckboxState {
    return (
      this.model?.value &&
      this.values?.length &&
      this.model.value.length === this.values.length
    );
  }

  public isIndeterminate(): boolean {
    return (
      this.model?.value &&
      this.values?.length &&
      this.model.value.length &&
      this.model.value.length < this.values.length
    );
  }

  public toggleSelection(): void {
    if (this.isChecked()) {
      this.model?.update.emit([]);
    } else {
      this.model?.update.emit(this.values);
    }
  }
}
