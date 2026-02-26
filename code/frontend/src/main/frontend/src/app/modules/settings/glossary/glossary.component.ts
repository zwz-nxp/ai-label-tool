import { Component, OnDestroy, OnInit } from "@angular/core";
import { DataService } from "app/utils/api-access/data-service";
import { GlossaryItem } from "app/models/glossary-item";
import { Subscription } from "rxjs";
import {
  animate,
  state,
  style,
  transition,
  trigger,
} from "@angular/animations";

@Component({
  selector: "app-glossary",
  templateUrl: "./glossary.component.html",
  styleUrls: ["./glossary.component.scss"],
  standalone: false,
  animations: [
    trigger("detailExpand", [
      state(
        "collapsed",
        style({ height: "0px", minHeight: "0", padding: "0" })
      ),
      state("expanded", style({ height: "*" })),
      transition(
        "expanded <=> collapsed",
        animate("225ms cubic-bezier(0.4, 0.0, 0.2, 1)")
      ),
    ]),
  ],
})
export class GlossaryComponent implements OnInit, OnDestroy {
  public displayedColumnsGlossaryItems = ["term", "description"];
  public glossaryList: Array<GlossaryItem> = [];
  public selectedItem!: GlossaryItem;
  public expandedElement: GlossaryItem | null = null;

  private glossarySubscription!: Subscription;

  public constructor(private dataService: DataService) {}

  public ngOnInit(): void {
    this.glossaryList = new Array<GlossaryItem>();
    this.getGlossaryList();
  }

  public getGlossaryList(): void {
    this.glossarySubscription = this.dataService
      .getAllGlossaryItems()
      .subscribe((items) => {
        this.glossaryList = items;
      });
  }

  public openDescription(): void {
    //
  }

  public ngOnDestroy(): void {
    if (this.glossarySubscription != null) {
      this.glossarySubscription.unsubscribe();
    }
  }
}
