import { Component, OnInit } from "@angular/core";
import { NxpProductionYear } from "app/models/nxp-production-year";
import { BaseDialogComponent } from "app/components/dialogs/BaseDialogComponent";
import { DateUtils } from "app/utils/date-utils";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { HttpErrorResponse } from "@angular/common/http";
import { firstValueFrom } from "rxjs";

@Component({
  selector: "app-add-nxp-production-year-dialog",
  templateUrl: "./add-nxp-production-year-dialog.component.html",
  standalone: false,
})
export class AddNxpProductionYearDialogComponent
  extends BaseDialogComponent
  implements OnInit
{
  public nxpProdYear = new NxpProductionYear();
  public du = new DateUtils();
  public datepickerStartDate = new Date();
  public nxpProdWeeks: NxpProductionYear[] = [];

  public ngOnInit(): void {
    this.getAllNxpProdWeeks();
  }

  public submitNewYear(year: NxpProductionYear): void {
    if (year.startDate) {
      firstValueFrom(this.dataService.submitNxpProductionYear(year))
        .then((data) => this.saveOk(data))
        .catch((err) => this.saveFailed(err))
        .finally(() => {
          this.getAllNxpProdWeeks();
          this.close();
        });
    }
  }

  public getAllNxpProdWeeks(): void {
    this.dataService.getNxpProductionYearForAllYears().subscribe((years) => {
      this.nxpProdWeeks = years;
      this.nxpProdYear.startDate = new Date();
      this.nxpProdWeeks.sort((a, b) => b.year - a.year);
      this.nxpProdYear.year = this.nxpProdWeeks[0].year + 1;
      this.datepickerStartDate = new Date(this.nxpProdYear.year, 0, 1);
      this.nxpProdYear.startDate = this.datepickerStartDate;
    });
  }

  private saveOk(_data: NxpProductionYear): void {
    SnackbarUtils.displaySuccessMsg(
      this.snackBar,
      "Successfully saved Production year"
    );
    this.getAllNxpProdWeeks();
  }

  private saveFailed(err: HttpErrorResponse): void {
    SnackbarUtils.displayServerErrorMsg(this.snackBar, err.error);
  }
}
