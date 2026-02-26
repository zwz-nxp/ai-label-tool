import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from "@angular/core";
import { ReplaySubject } from "rxjs";
import { FormControl } from "@angular/forms";
import { BooleanInput } from "@angular/cdk/coercion";
import { SubscriptSizing } from "@angular/material/form-field";

@Component({
  selector: "app-searchable-select",
  templateUrl: "./searchable-select.component.html",
  standalone: false,
})
export class SearchableSelectComponent<T> implements OnInit, OnChanges {
  @Input() public inputList: T[] = [];
  @Input() public defaultValue: T | null = null;
  @Input() public label = "Select an option";
  @Input() public required: BooleanInput = false;
  @Input() public panelWidth: string | number | null = "auto";
  @Input() public disabled: boolean = false;
  @Input() public subscriptSizing: SubscriptSizing = "fixed";
  @Output() public itemSelected = new EventEmitter<T>();

  public itemCtrl = new FormControl<T | null>(null);
  public itemFilterCtrl = new FormControl<string>("");
  public filteredItems = new ReplaySubject<T[]>(1);

  @Input() public compareFn = (a: T, b: T): boolean => {
    return a === b;
  };

  @Input() public filterFn = (item: T, search: string): boolean => {
    return (
      this.displayFn(item).toLowerCase().indexOf(search.toLowerCase()) > -1
    );
  };

  @Input() public displayFn = (item: T): string => {
    return String(item);
  };

  public ngOnChanges(changes: SimpleChanges): void {
    if (changes["defaultValue"]) {
      this.itemCtrl.setValue(this.defaultValue);
    }

    if (changes["inputList"]) {
      this.filteredItems.next(this.inputList.slice());
    }

    if (changes["disabled"]) {
      if (this.disabled) this.itemCtrl.disable();
      else this.itemCtrl.enable();
    }
  }

  public ngOnInit(): void {
    this.itemCtrl.setValue(this.defaultValue);

    this.filteredItems.next(this.inputList.slice());

    this.itemFilterCtrl.valueChanges.subscribe(() => {
      this.filterItems();
    });

    if (this.disabled) {
      this.itemCtrl.disable();
    }
  }

  protected filterItems(): void {
    if (!this.inputList) {
      return;
    }

    const search = (this.itemFilterCtrl.value ?? "").toLowerCase();

    if (!search) {
      this.filteredItems.next(this.inputList.slice());
      return;
    }

    this.filteredItems.next(
      this.inputList.filter((item) => this.filterFn(item, search))
    );
  }
}
