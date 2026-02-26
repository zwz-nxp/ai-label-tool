import { Component, HostBinding, Input } from "@angular/core";

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: "button[super-mini-fab-button]",
  templateUrl: "./super-mini-fab-button.component.html",
  standalone: false,
})
export class SuperMiniFabButtonComponent {
  @Input() public matIcon = "";

  @HostBinding("class") public buttonClass =
    "h-7 w-7 rounded-full text-lg enabled:hover:opacity-80 disabled:text-neutral-400 disabled:bg-disabled-button";
}
