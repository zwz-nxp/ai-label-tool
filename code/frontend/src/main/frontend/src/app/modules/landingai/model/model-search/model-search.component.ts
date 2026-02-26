import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnDestroy,
} from "@angular/core";
import { Subject } from "rxjs";
import { debounceTime, distinctUntilChanged, takeUntil } from "rxjs/operators";

/**
 * Model Search Component
 * Implementation requirements 2.1, 2.2, 2.5: Search input and debouncing functionality
 */
@Component({
  selector: "app-model-search",
  templateUrl: "./model-search.component.html",
  styleUrls: ["./model-search.component.scss"],
  standalone: false,
})
export class ModelSearchComponent implements OnInit, OnDestroy {
  @Input() searchTerm: string = "";
  @Input() showFavoritesOnly: boolean = false;

  @Output() searchChange = new EventEmitter<string>();
  @Output() favoritesFilterChange = new EventEmitter<boolean>();

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  ngOnInit(): void {
    // Implementation requirements 2.2: Implement 300ms delay debouncing to optimize performance
    this.searchSubject
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((searchTerm) => {
        this.searchChange.emit(searchTerm);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Handle search input change
   * Implementation requirements 2.1: When user types in search input, filter models in real-time by model name or creator
   */
  onSearchInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target?.value || "";
    this.searchTerm = value;
    this.searchSubject.next(value);
  }

  /**
   * Handle favorites filter checkbox change
   * Implementation requirements 2.3: When "Only show favorite models" checkbox is checked, only display models with Favorite_Status as true
   */
  onFavoritesCheckboxChange(checked: boolean): void {
    this.showFavoritesOnly = checked;
    this.favoritesFilterChange.emit(checked);
  }

  /**
   * Clear search input
   */
  clearSearch(): void {
    this.searchTerm = "";
    this.searchSubject.next("");
  }
}
