import { Component, Input } from "@angular/core";
import { GenericSearchType } from "app/models/generic-search";
import { acceptedDateTypes, DateUtils } from "app/utils/date-utils";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";

@Component({
  selector: "app-generic-search-popup",
  templateUrl: "./generic-search-popup.component.html",
  styleUrls: ["./generic-search-popup.component.scss"],
  standalone: false,
})
export class GenericSearchPopupComponent extends BaseDialogComponent {
  @Input() public searchType!: GenericSearchType;

  public du = new DateUtils();
  public gst: typeof GenericSearchType = GenericSearchType;

  public valueFrom: acceptedDateTypes;
  public valueTo: acceptedDateTypes;

  public override close(): void {
    const result = this.buildResult();
    this.dialogRef.close(result);
  }

  public buildResult(): string {
    // the backend will handle undefined as a value
    if (this.searchType === GenericSearchType.DATE_FROM_TO) {
      return `${this.du.display(this.valueFrom)};${this.du.display(
        this.valueTo
      )}`;
    } else {
      return `${this.valueFrom};${this.valueTo}`;
    }
  }
}
