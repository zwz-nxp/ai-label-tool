import { Component, HostBinding, Input } from "@angular/core";

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: "button[table-icon-button]",
  templateUrl: "./table-icon-button.component.html",
  standalone: false,
})
export class TableIconButtonComponent {
  @Input() public matIcon = "";

  @HostBinding("class") public buttonClass =
    "h-5 w-5 rounded-full text-sm enabled:hover:bg-neutral-300 disabled:text-neutral-400";
}
