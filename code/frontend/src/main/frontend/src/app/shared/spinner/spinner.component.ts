import { Component, Input } from "@angular/core";

@Component({
  selector: "app-spinner",
  templateUrl: "./spinner.component.html",
  standalone: false,
})
export class SpinnerComponent {
  @Input() public color = "primary";
  @Input() public isAbsolute = true;
}
