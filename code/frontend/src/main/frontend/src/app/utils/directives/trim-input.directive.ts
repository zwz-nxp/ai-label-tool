import { Directive, ElementRef, HostListener } from "@angular/core";
import { NgControl } from "@angular/forms";
import { debounceTime, Subject } from "rxjs";

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: "input[trimInput],textarea[trimInput]",
  standalone: false,
})
export class TrimInputDirective {
  private inputSubject = new Subject<string>();
  private readonly debounceTime = 300;

  public constructor(
    private el: ElementRef,
    private ngControl: NgControl
  ) {
    this.inputSubject
      .pipe(debounceTime(this.debounceTime))
      .subscribe((inputValue) => {
        this.handleInput(inputValue);
      });
  }

  @HostListener("blur", ["$event.target.value"])
  public onBlur(value: string): void {
    this.inputSubject.next(value);
  }

  @HostListener("input", ["$event.target.value"])
  public onInput(value: string): void {
    this.inputSubject.next(value);
  }

  private handleInput(value: string): void {
    if (this.el.nativeElement.value) {
      const trimmed = value.trim();
      this.el.nativeElement.value = trimmed;
      this.ngControl.control?.setValue(trimmed);
    }
  }
}
