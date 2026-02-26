import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { AppUtils } from "app/utils/app-utils";
import { debounceTime, distinctUntilChanged, Subject } from "rxjs";
import { GenericSearchPopupComponent } from "./generic-search-popup/generic-search-popup.component";
import {
  GenericSearchArg,
  GenericSearchArguments,
  GenericSearchSortField,
  GenericSearchType,
  GenericSearchUtils,
  SEARCH_MAX_DATE,
  SEARCH_MAX_VALUE,
  SEARCH_MIN_DATE,
  SEARCH_MIN_VALUE,
} from "app/models/generic-search";
import { BooleanInput, coerceBooleanProperty } from "@angular/cdk/coercion";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Store } from "@ngrx/store";
import { takeUntil } from "rxjs/operators";
import { DEFAULT_DEBOUNCE_TIME } from "app/state";
import * as LocationSelectors from "app/state/location/location.selectors";
import { Location } from "app/models/location";

@Component({
  selector: "app-generic-search",
  templateUrl: "./generic-search.component.html",
  styleUrls: ["./generic-search.component.scss"],
  standalone: false,
})
export class GenericSearchComponent implements OnInit, OnDestroy {
  @Input() public searchField!: GenericSearchSortField;
  @Input() public enumValues?: [string, string][];
  @Input() public booleanValues?: (boolean | undefined)[];
  @Input() public initialValue?: string | string[] | boolean;

  @Output() public emitSearchTrigger = new EventEmitter<GenericSearchArg>();
  public filterValue: string | string[] | boolean = "";
  public displayFromTo = "...";
  public au: AppUtils = new AppUtils();
  public filter = new Subject<string>();
  public skip = false;
  public searchType = GenericSearchType.DEFAULT;
  public gst: typeof GenericSearchType = GenericSearchType;
  private destroy$ = new Subject<void>();
  private debounceTime = DEFAULT_DEBOUNCE_TIME;
  private location = new Location();

  public constructor(
    public dialogService: MatDialog,
    private dataUpdateService: DataUpdateService,
    private store: Store,
    private genericSearchUtils: GenericSearchUtils
  ) {
    this.store
      .select(LocationSelectors.selectCurrentLocation)
      .pipe(takeUntil(this.destroy$))
      .subscribe((location) => {
        if (location) {
          this.location = location;
        }
      });

    this.store
      .select(CurrentUserSelectors.selectDebounceTime)
      .subscribe((debounceTime) => {
        this.debounceTime = debounceTime;
      });
  }

  private _disabled = false;

  @Input()
  public get disabled(): boolean {
    return this._disabled;
  }

  public set disabled(value: BooleanInput) {
    this._disabled = coerceBooleanProperty(value);
  }

  public ngOnInit(): void {
    this.setupDebounce();
    this.skip = this.searchField.toString().startsWith("skip");
    this.watchForReset();

    this.searchType = this.genericSearchUtils.getSearchType(this.searchField);

    if (this.initialValue !== undefined) {
      this.triggerSearch(this.initialValue);
    }
  }

  public popupDialog(): void {
    const dialogRef = this.dialogService.open(GenericSearchPopupComponent);

    dialogRef.componentInstance.searchType = this.searchType;
    dialogRef.afterClosed().subscribe((value) => {
      this.triggerSearch(value);
    });
  }

  public triggerSearch(filterValue: string | string[] | boolean): void {
    const searchArg = new GenericSearchArg(this.searchField, filterValue);
    this.filterValue = filterValue;
    this.emitSearchTrigger.emit(searchArg);
  }

  public isEmpty(): boolean {
    return this.searchType === this.gst.EMPTY;
  }

  public isFromToSearch(): boolean {
    return (
      this.searchType === this.gst.NUM_FROM_TO ||
      this.searchType === this.gst.DATE_FROM_TO
    );
  }

  public isDropdownMultiSelectSearch(): boolean {
    return this.searchType === this.gst.DROPDOWN_MULTI;
  }

  public isDropdownSingleSelectSearch(): boolean {
    return this.searchType === this.gst.DROPDOWN_SINGLE;
  }

  /**
   * The function is used in the template as a condition for assigning
   * a dynamic css class.
   * @returns 'true' if a search field has value
   */
  public hasValue(): boolean {
    return (
      typeof this.filterValue !== "boolean" &&
      this.filterValue?.length !== 0 &&
      this.filterValue !== undefined
    );
  }

  /**
   * Gets color for component depending on its state: filled, disabled or idle.
   * @returns string containing colorcode.
   */
  public getColor(): "bg-neutral-200" | "bg-amber-100" {
    return this.isSearchFieldDisabled() ? "bg-neutral-200" : "bg-amber-100";
  }

  public getTooltip(): string {
    if (this.filterValue == "") {
      return "no search value";
    } else {
      if (this.isFromToSearch()) {
        const values = (this.filterValue + ";").split(";");
        const s1 =
          values[0] === SEARCH_MIN_DATE || values[0] === SEARCH_MIN_VALUE
            ? "--"
            : values[0];
        const s2 =
          values[1] === SEARCH_MAX_DATE || values[1] === SEARCH_MAX_VALUE
            ? "--"
            : values[1];
        return s1 + ".." + s2;
      } else {
        return `${this.filterValue}`;
      }
    }
  }

  public isSearchFieldDisabled(): boolean {
    if (this.genericSearchUtils.disabled.includes(this.searchField)) {
      return true;
    }
    return this.isDisabledForNotGlobalSite() || this.disabled;
  }

  public isDisabledForNotGlobalSite(): boolean {
    if (this.location.id == 0) {
      return false;
    }
    return (
      this.searchField === GenericSearchSortField.E_SITE_ACRONYM ||
      this.searchField === GenericSearchSortField.PV_LOCATION ||
      this.searchField === GenericSearchSortField.TDP_LOCATION_ACRONYM ||
      this.searchField === GenericSearchSortField.BURN_IN_BOARD_ACRONYM ||
      this.searchField ===
        GenericSearchSortField.UR_ACTIVATION_FLOW_OVERVIEW_SAPLOCATION ||
      this.searchField === GenericSearchSortField.UR_TESTFLOW_SAPCODE ||
      this.searchField === GenericSearchSortField.P_ACRONYM ||
      this.searchField === GenericSearchSortField.UR_PACKINGFLOW_SAP_CODE ||
      this.searchField === GenericSearchSortField.STEP_MAPPING_SITE ||
      this.searchField === GenericSearchSortField.PK_POV_EFFICIENCY_LOCATION ||
      this.searchField === GenericSearchSortField.PK_POV_PARAMS_LOCATION ||
      this.searchField === GenericSearchSortField.PK_MANUAL_FLOW_LOCATION ||
      this.searchField === GenericSearchSortField.MISSING_LINK_LOCATION
    );
  }

  public convertIfNeeded(value: string | string[] | boolean): string {
    if (typeof value === "boolean") {
      return this.au.convertBooleanToYesNoBoth(value);
    }
    return value as string;
  }

  public refreshSearch(): void {
    if (typeof this.filterValue === "string") {
      this.triggerSearch(this.filterValue);
    }
  }

  public getEnumValuesArray(list?: string[] | [string, string][]): string[] {
    return list
      ? list.map((item) => {
          if (item instanceof Array) {
            return item[0];
          }
          return item;
        })
      : [];
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private watchForReset(): void {
    this.dataUpdateService.resetEmitter
      .pipe(takeUntil(this.destroy$))
      .subscribe((data) => {
        const searchArgs = data as GenericSearchArguments;
        const fld = searchArgs.searchArgs.find(
          (e) => e.field === this.searchField
        );

        if (fld?.value) {
          if (this.isDropdownMultiSelectSearch()) {
            this.filterValue = fld.value.split(";");
          } else {
            this.filterValue = fld.value;
          }
        } else {
          if (this.isDropdownMultiSelectSearch()) {
            this.filterValue = [];
          } else {
            this.filterValue = "";
          }
        }
      });
  }

  private setupDebounce(): void {
    this.filter
      .pipe(debounceTime(this.debounceTime), distinctUntilChanged())
      .subscribe((filterValue) => {
        this.triggerSearch(filterValue);
      });
  }
}
