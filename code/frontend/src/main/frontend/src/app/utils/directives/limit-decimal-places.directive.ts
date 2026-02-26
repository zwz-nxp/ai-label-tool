import {
  Directive,
  ElementRef,
  HostListener,
  Input,
  numberAttribute,
} from "@angular/core";

@Directive({
  selector: "[appLimitDecimalPlaces]",
  standalone: false,
})
export class LimitDecimalPlacesDirective {
  @Input({ transform: numberAttribute }) public maxDecimals: number = 2;
  private readonly specialKeys = [
    "Backspace",
    "Tab",
    "End",
    "Home",
    "-",
    "ArrowLeft",
    "ArrowRight",
    "Del",
    "Delete",
    "F1",
    "F2",
    "F3",
    "F4",
    "F5",
    "F6",
    "F7",
    "F8",
    "F9",
    "F10",
    "F11",
    "F12",
  ];

  public constructor(private elementRef: ElementRef) {}

  private get regex(): RegExp {
    return RegExp("^\\d*\\.?\\d{0," + this.maxDecimals + "}$", "g");
  }

  @HostListener("keydown", ["$event"])
  public onKeyDown(event: KeyboardEvent): void {
    // Allow Backspace, tab, end, and home keys
    if (this.specialKeys.includes(event.key)) {
      return;
    }

    const current = this.elementRef.nativeElement.value as string;
    const position = this.elementRef.nativeElement.selectionStart as number;
    const next = `${current.slice(0, position)}${
      event.key === "Decimal" ? "." : event.key
    }${current.slice(position)}`;

    if (next && !next.match(this.regex)) {
      event.preventDefault();
    }
  }
}
