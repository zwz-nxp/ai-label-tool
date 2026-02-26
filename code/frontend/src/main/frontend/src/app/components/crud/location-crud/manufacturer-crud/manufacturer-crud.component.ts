import { Component, Input, OnInit } from "@angular/core";
import { CrudComponent } from "../../CrudComponent";
import { FormBuilder, FormControl, Validators } from "@angular/forms";
import { MatDialogRef } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { DataService } from "app/utils/api-access/data-service";
import { Location, Manufacturer } from "app/models/location";
import { CRUD_ACTION } from "../../crud-action";
import { LocationMaintenanceService } from "app/utils/services/location-maintenance.service";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { HttpErrorResponse } from "@angular/common/http";
import { Store } from "@ngrx/store";
import * as LocationSelectors from "app/state/location/location.selectors";

@Component({
  selector: "app-manufacturer-crud",
  templateUrl: "./manufacturer-crud.component.html",
  standalone: false,
})
export class ManufacturerCrudComponent extends CrudComponent implements OnInit {
  @Input() public selectedManufacturer: Manufacturer = new Manufacturer();

  public siteFilterControl = new FormControl("");
  public filteredLocations: Location[] = [];
  private locations: Location[] = [];

  public constructor(
    dialogRef: MatDialogRef<CrudComponent>,
    dataService: DataService,
    snackBar: MatSnackBar,
    store: Store,
    public formBuilder: FormBuilder,
    private locationMaintenance: LocationMaintenanceService
  ) {
    super(dialogRef, dataService, snackBar, store);

    this.store
      .select(LocationSelectors.selectAllLocations)
      .subscribe((locations) => {
        this.locations = locations;
      });
  }

  public ngOnInit(): void {
    this.fillForm();
    this.filteredLocations = this.locations;

    this.siteFilterControl.valueChanges.subscribe((value) => {
      this.filterLocations(value);
    });
  }

  public fillForm(): void {
    this.inputForm = this.formBuilder.group({
      manufacturerCode: ["", Validators.required],
      siteFilter: "",
      location: [null, Validators.required],
    });

    /**
     * If the action is not a create action:
     *  Disable editing of manufacturer code name
     *  Fill in form with to-be edited or deleted object
     */
    if (this.crudAction === CRUD_ACTION.UPDATE) {
      this.setValuesToExistingManufacturerCode();
      this.inputForm.get("manufacturerCode")?.disable();
    } else if (this.crudAction == CRUD_ACTION.DELETE) {
      /**
       * If the action is a delete disable the whole form.
       */
      this.setValuesToExistingManufacturerCode();

      this.inputForm.get("manufacturerCode")?.disable();
      this.inputForm.get("location")?.disable();
    }
  }

  public override create(): void {
    this.assignValuesToManufacturerCode();
    this.save();
  }

  public override delete(): void {
    if (this.selectedManufacturer.manufacturerCode) {
      this.isLoading = true;
      this.locationMaintenance
        .deleteManufacturerCode(this.selectedManufacturer.manufacturerCode)
        .then((_) => this.saveConfirm("is successfully deleted"))
        .catch((error) => this.saveFailed(error))
        .finally(() => (this.isLoading = false));
    }
  }

  public override update(): void {
    this.assignValuesToManufacturerCode();
    this.save();
  }

  public override submit(): void {
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

  private setValuesToExistingManufacturerCode(): void {
    this.inputForm
      .get("manufacturerCode")
      ?.setValue(this.selectedManufacturer.manufacturerCode);
    this.inputForm
      .get("location")
      ?.setValue(
        this.locations.find(
          (location) => this.selectedManufacturer.locationId === location.id
        )
      );
  }

  private save(): void {
    this.isLoading = true;
    this.locationMaintenance
      .saveManufacturerCode(this.selectedManufacturer)
      .then((_) => this.saveConfirm("is successfully saved"))
      .catch((error) => this.saveFailed(error))
      .finally(() => (this.isLoading = false));
  }

  private saveConfirm(message: string): void {
    SnackbarUtils.displaySuccessMsg(
      this.snackBar,
      `Manufacturer Code ${this.selectedManufacturer.manufacturerCode} ${message}`
    );

    this.closeDialog();
  }

  private saveFailed(error: HttpErrorResponse): void {
    SnackbarUtils.displayServerErrorMsg(this.snackBar, error.error);
  }

  private assignValuesToManufacturerCode(): void {
    this.selectedManufacturer.manufacturerCode = this.inputForm
      .get("manufacturerCode")
      ?.value?.trim();
    this.selectedManufacturer.locationId =
      this.inputForm.get("location")?.value?.id;
  }

  private filterLocations(search: string | null): void {
    if (!this.locations) {
      return;
    }

    if (!search) {
      this.filteredLocations = this.locations;
    } else {
      this.filteredLocations = this.locations.filter(
        (location) =>
          location.acronym.toLowerCase().indexOf(search.toLowerCase()) > -1
      );
    }
  }
}
