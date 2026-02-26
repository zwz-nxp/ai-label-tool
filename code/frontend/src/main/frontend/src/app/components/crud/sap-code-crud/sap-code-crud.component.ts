import { Component, Input, OnInit } from "@angular/core";
import { CRUD_ACTION } from "../crud-action";
import { FormBuilder, Validators } from "@angular/forms";
import { Location } from "app/models/location";
import { AppUtils } from "app/utils/app-utils";
import { DataService } from "app/utils/api-access/data-service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { SapCode } from "app/models/sap-code";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { CrudComponent } from "../CrudComponent";
import { MatDialogRef } from "@angular/material/dialog";
import { firstValueFrom } from "rxjs";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";

@Component({
  selector: "app-sap-code-crud",
  templateUrl: "./sap-code-crud.component.html",
  standalone: false,
})
export class SapCodeCrudComponent extends CrudComponent implements OnInit {
  @Input() public selectedSapCode: SapCode = new SapCode();

  public loading = true;
  public alreadySubmitted = false;

  public au = new AppUtils();
  public allLocations: Location[] = [];
  public allSapCodes: SapCode[] = [];

  public constructor(
    dialogRef: MatDialogRef<CrudComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public formBuilder: FormBuilder,
    public authorizationService: AuthorizationService
  ) {
    super(dialogRef, dataService, snackBar, store);

    this.store
      .select(LocationSelectors.selectAllLocations)
      .subscribe((locations) => {
        this.allLocations = locations.filter((loc) => loc.id !== 0);
      });
  }

  @Input()
  public set inputSapCode(value: unknown) {
    this.selectedSapCode = value ? (value as SapCode) : new SapCode();
  }

  public ngOnInit(): void {
    this.getAllSapCodes();
    this.loading = false;
    this.fillForm();
  }

  public update(): void {
    this.assignValuesToSapCode();
    this.save();
  }

  public save(): void {
    this.loading = true;
    this.alreadySubmitted = true;
    if (this.sapCodeAlreadyExists() && this.crudAction === CRUD_ACTION.CREATE) {
      SnackbarUtils.displayErrorMsg(
        this.snackBar,
        "This SAP Plant Code already exists"
      );
      this.loading = false;
      this.alreadySubmitted = false;
    } else {
      firstValueFrom(this.dataService.saveSapCode(this.selectedSapCode))
        .then(() => {
          SnackbarUtils.displaySuccessMsg(
            this.snackBar,
            "The SAP Plant Code has been successfully saved"
          );
          this.closeDialog();
        })
        .catch((error) => SnackbarUtils.displayErrorMsg(this.snackBar, error))
        .finally(() => (this.loading = false));
    }
  }

  public fillForm(): void {
    this.inputForm = this.formBuilder.group({
      plantCode: ["", Validators.required],
      city: [""],
      country: [""],
      managedBy: [""],
    });

    this.inputForm.controls["plantCode"].setValue(
      this.selectedSapCode.plantCode
    );
    this.inputForm.controls["city"].setValue(this.selectedSapCode.city);
    this.inputForm.controls["country"].setValue(this.selectedSapCode.country);
    this.inputForm.controls["managedBy"].setValue(
      this.selectedSapCode.managedBy
    );

    if (this.crudAction == CRUD_ACTION.DELETE) {
      this.inputForm.controls["plantCode"].disable();
      this.inputForm.controls["city"].disable();
      this.inputForm.controls["country"].disable();
      this.inputForm.controls["managedBy"].disable();
    }
  }

  public submit(): void {
    try {
      switch (this.crudAction) {
        case CRUD_ACTION.CREATE:
          if (this.inputForm.valid) {
            this.create();
          }
          break;
        case CRUD_ACTION.DELETE:
          this.delete();
          break;
        case CRUD_ACTION.UPDATE:
          this.update();
          break;
      }
    } catch (error) {
      SnackbarUtils.displayErrorMsg(this.snackBar, error as string);
    }
  }

  public create(): void {
    this.assignValuesToSapCode();
    this.save();
  }

  public delete(): void {
    this.alreadySubmitted = true;
    firstValueFrom(this.dataService.deleteSapCode(this.selectedSapCode))
      .then(() => {
        SnackbarUtils.displaySuccessMsg(
          this.snackBar,
          "The SAP Plant Code has been successfully deleted"
        );
        this.closeDialog();
      })
      .catch((error) => SnackbarUtils.displayErrorMsg(this.snackBar, error));
  }

  public isDelete(): boolean {
    return this.crudAction === CRUD_ACTION.DELETE;
  }

  public sapCodeAlreadyExists(): SapCode | undefined {
    const acronym = this.selectedSapCode.plantCode.trim().toUpperCase();
    return this.allSapCodes.find((e) => e.plantCode === acronym);
  }

  public compareLocation(loc1: Location, loc2: Location): boolean {
    return loc1.id === loc2.id;
  }

  private getAllSapCodes(): void {
    this.dataService.getAllSapCodes().subscribe((sapCodes) => {
      this.allSapCodes = sapCodes;
      this.allSapCodes.sort((a, b) =>
        a.plantCode > b.plantCode ? 1 : b.plantCode > a.plantCode ? -1 : 0
      );
    });
  }

  private assignValuesToSapCode(): void {
    this.selectedSapCode.plantCode = this.inputForm
      .get(`plantCode`)
      ?.value.toUpperCase()
      .trim();
    this.selectedSapCode.city = this.inputForm.get(`city`)?.value;
    this.selectedSapCode.country = this.inputForm.get(`country`)?.value;
    this.selectedSapCode.managedBy = this.inputForm.get(`managedBy`)?.value;
  }
}
