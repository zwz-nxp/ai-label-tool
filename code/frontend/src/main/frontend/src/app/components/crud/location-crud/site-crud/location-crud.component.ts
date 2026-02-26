import { Component, Input, OnInit } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { MatSelectChange } from "@angular/material/select";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ConfirmDialogService } from "app/utils/services/confirm-dialog.service";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { DataService } from "app/utils/api-access/data-service";
import { Location, SaveLocation } from "app/models/location";
import { CRUD_ACTION } from "../../crud-action";
import { LocationMaintenanceService } from "app/utils/services/location-maintenance.service";
import { CrudComponent } from "../../CrudComponent";
import { MatDialogRef } from "@angular/material/dialog";
import { HttpErrorResponse } from "@angular/common/http";
import { firstValueFrom } from "rxjs";
import { Store } from "@ngrx/store";
import * as SapCodeSelectors from "app/state/sap-code/sap-code.selectors";
import * as SapCodeActions from "app/state/sap-code/sap-code.actions";

@Component({
  selector: "app-location-crud",
  templateUrl: "./location-crud.template.html",
  standalone: false,
})
export class LocationCrudComponent extends CrudComponent implements OnInit {
  @Input() public selectedLocation: SaveLocation = new SaveLocation();

  public sapPlantCodes: Array<string> = [];

  public constructor(
    dialogRef: MatDialogRef<CrudComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private locationMaintenance: LocationMaintenanceService
  ) {
    super(dialogRef, dataService, snackBar, store);

    this.store.dispatch(SapCodeActions.loadSapCodes());

    this.store
      .select(SapCodeSelectors.selectSapPlantCodes)
      .subscribe((value) => (this.sapPlantCodes = value));
  }

  public ngOnInit(): void {
    this.fillForm();
  }

  public create(): void {
    this.assignValuesToSite();
    this.save();
  }

  public delete(): void {
    if (this.selectedLocation.id) {
      this.isLoading = true;
      this.locationMaintenance
        .deleteLocation(this.selectedLocation.id)
        .then((_) => this.saveConfirm("is successfully deleted"))
        .catch((error) => this.saveFailed(error))
        .finally(() => (this.isLoading = false));
    }
  }

  public update(): void {
    this.assignValuesToSite();
    this.save();
  }

  public fillForm(): void {
    this.inputForm = this.formBuilder.group({
      acronym: ["", Validators.required],
      planningEngine: [""],
      country: [""],
      city: [""],
      sapCode: [""],
      extendedSuffix: [""],
      tmdbCode: [""],
      menuGrouping: [""],
      isSubContractor: [false, Validators.required],
      vendorCode: [""],
    });

    this.inputForm.get("isSubContractor")?.valueChanges.subscribe((value) => {
      this.configureVendorCodeInputField(value);
    });

    /**
     * If the action is not a create action:
     *  Disable editing of site name
     *  Fill in form with to-be edited or deleted object
     */
    if (this.crudAction === CRUD_ACTION.UPDATE) {
      this.setValuesToExistingLocation();
    } else if (this.crudAction == CRUD_ACTION.DELETE) {
      /**
       * If the action is a delete disable the whole form.
       */
      this.setValuesToExistingLocation();

      this.inputForm.controls["acronym"].disable();
      this.inputForm.controls["tmdbCode"].disable();
      this.inputForm.controls["city"].disable();
      this.inputForm.controls["sapCode"].disable();
      this.inputForm.controls["planningEngine"].disable();
      this.inputForm.controls["country"].disable();
      this.inputForm.controls["extendedSuffix"].disable();
      this.inputForm.controls["menuGrouping"].disable();
      this.inputForm.controls["isSubContractor"].disable();
      this.inputForm.controls["vendorCode"].disable();
    }
  }

  public handleSubConSelectionChange(event: MatSelectChange): void {
    if (event.value) {
      const message =
        "You are about to create a Subcon site. Capacity will be handled differently. " +
        "Are you sure you want to create a <strong>Subcon Site</strong>?";
      const dialog = this.confirmDialogService.openDialog("Warning!", message);
      firstValueFrom(dialog.afterClosed()).then((result) => {
        this.inputForm.controls["isSubContractor"].setValue(result);
      });
    }
  }

  public submit(): void {
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
        if (this.inputForm.valid) {
          this.update();
        }
        break;
    }
  }

  public isConditionalRequiredForVendorCode(): boolean {
    const isSubContractor = this.inputForm.get("isSubContractor");
    return !!(
      isSubContractor && this.inputForm.get("vendorCode")?.hasError("required")
    );
  }

  private save(): void {
    this.isLoading = true;
    this.locationMaintenance
      .saveLocation(this.selectedLocation as Location)
      .then((_) => this.saveConfirm("is successfully saved"))
      .catch((error) => this.saveFailed(error))
      .finally(() => (this.isLoading = false));
  }

  private saveConfirm(message: string): void {
    SnackbarUtils.displaySuccessMsg(
      this.snackBar,
      `Location ${this.selectedLocation.acronym} ${message}`
    );

    this.closeDialog();
  }

  private saveFailed(error: HttpErrorResponse): void {
    SnackbarUtils.displayServerErrorMsg(this.snackBar, error.error);
  }

  private configureVendorCodeInputField(
    locationIsSubcontractor: boolean
  ): void {
    if (locationIsSubcontractor) {
      this.inputForm.controls["vendorCode"].enable();
      this.inputForm.controls["vendorCode"].setValidators(Validators.required);
    } else {
      this.inputForm.controls["vendorCode"].setValue(null);
      this.inputForm.controls["vendorCode"].disable();
      this.inputForm.controls["vendorCode"].setValidators(null);
    }
    // required call when changing validators
    this.inputForm.controls["vendorCode"].updateValueAndValidity();
  }

  /**
   * Assigns input form values to the to-be created site.
   */
  private assignValuesToSite(): void {
    this.selectedLocation.acronym = this.inputForm
      .get("acronym")
      ?.value?.trim();
    this.selectedLocation.tmdbCode = this.inputForm
      .get("tmdbCode")
      ?.value?.trim();
    this.selectedLocation.city = this.inputForm.get("city")?.value?.trim();
    this.selectedLocation.sapCode = this.inputForm.get("sapCode")?.value;
    this.selectedLocation.planningEngine =
      this.inputForm.get("planningEngine")?.value;
    this.selectedLocation.country = this.inputForm
      .get("country")
      ?.value?.trim();
    this.selectedLocation.extendedSuffix = this.inputForm
      .get("extendedSuffix")
      ?.value?.trim();
    this.selectedLocation.menuGrouping = this.inputForm
      .get("menuGrouping")
      ?.value?.trim()
      .toUpperCase();
    this.selectedLocation.isSubContractor =
      this.inputForm.get("isSubContractor")?.value;
    this.selectedLocation.vendorCode = this.inputForm.get("vendorCode")?.value;
  }

  /**
   * Set values to the copy of an existing location when updating or deleting.
   */
  private setValuesToExistingLocation(): void {
    this.inputForm.controls["acronym"].setValue(this.selectedLocation.acronym);
    this.inputForm.controls["tmdbCode"].setValue(
      this.selectedLocation.tmdbCode
    );
    this.inputForm.controls["city"].setValue(this.selectedLocation.city);
    this.inputForm.controls["sapCode"].setValue(this.selectedLocation.sapCode);
    this.inputForm.controls["planningEngine"].setValue(
      this.selectedLocation.planningEngine
    );
    this.inputForm.controls["country"].setValue(this.selectedLocation.country);
    this.inputForm.controls["extendedSuffix"].setValue(
      this.selectedLocation.extendedSuffix
    );
    this.inputForm.controls["menuGrouping"].setValue(
      this.selectedLocation.menuGrouping?.toUpperCase()
    );
    this.inputForm.controls["isSubContractor"].setValue(
      this.selectedLocation.isSubContractor
    );
    // According to a business rule, the field cannot be edited once it is set at the moment of creating a site
    this.inputForm.controls["isSubContractor"].disable();
    this.inputForm.controls["vendorCode"].setValue(
      this.selectedLocation.vendorCode
    );
    if (this.selectedLocation.isSubContractor) {
      this.inputForm.controls["vendorCode"].setValidators(Validators.required);
    } else {
      this.inputForm.controls["vendorCode"].disable();
    }
  }
}
