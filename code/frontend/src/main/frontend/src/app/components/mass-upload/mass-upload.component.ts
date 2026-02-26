import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from "@angular/core";
import { SnackbarUtils } from "app/utils/snackbar-utils";
import { MassUploadResponse } from "app/models/mass-upload";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MassUploadService } from "app/utils/services/mass-upload.service";
import { MassUploadType } from "app/models/mass-upload-type";
import { RoleEnum } from "app/models/role";
import { AuthorizationService } from "app/utils/services/authorization.service";
import { groupBy } from "lodash-es";
import { HttpErrorResponse } from "@angular/common/http";
import { format } from "date-fns";
import { firstValueFrom, Subscription } from "rxjs";
import { DataUpdateService } from "app/utils/services/data-update.service";
import { Update, UpdateType } from "app/models/update";
import * as CurrentUserSelectors from "app/state/current-user/current-user.selectors";
import { Person } from "app/models/person";
import { Store } from "@ngrx/store";
import * as SystemSelectors from "app/state/system/system.selectors";

@Component({
  selector: "app-mass-upload",
  templateUrl: "./mass-upload.component.html",
  standalone: false,
})
export class MassUploadComponent implements OnInit, OnDestroy {
  @ViewChild("uploadField") public elementRef!: ElementRef;

  public selectedMassUpload?: MassUploadType;
  public massUploadsByCategory?: MassUploadType[][];

  public isUploadDisabled = true;
  public isLoadingResults = false;
  public isUploading = false;
  public sendEmail = false;

  public verifyError: string = "";
  public uploadError: string = "";
  public fileName?: string;
  public fileSize?: string;
  public fileDateModified?: string;
  public recordsMetaData?: string;
  public completeMassUploadResponse: MassUploadResponse | null = null;
  public totalRecords = 0;
  public successCount = 0;
  public errorCount = 0;
  public ignoreCount = 0;
  public progressBarValue = 0;
  public errors: MassUploadResponseFeedback[] = [];
  public unuploadedObjects: MassUploadResponseFeedback[] = [];

  private FORTY_MEGABYTES = 40000000;
  private file?: File;
  private updateSubscription?: Subscription;
  private currentUser!: Person;
  private isReadOnlyMode = false;

  public constructor(
    public snackBar: MatSnackBar,
    public massUploadService: MassUploadService,
    private authorizationService: AuthorizationService,
    private dataUpdateService: DataUpdateService,
    private store: Store
  ) {
    this.store
      .select(CurrentUserSelectors.selectCurrentUser)
      .subscribe((user) => {
        if (user) {
          this.currentUser = user;
        }
      });

    this.store
      .select(SystemSelectors.selectIsReadOnlyMode)
      .subscribe((value) => (this.isReadOnlyMode = value));
  }

  public get downloadTemplateUrl(): string {
    return this.massUploadService.getTemplateUrl(
      this.selectedMassUpload?.type ?? ""
    );
  }

  protected get isChooseFileDisabled(): boolean {
    const globalLocationId = "0";

    if (this.selectedMassUpload?.massUploadName === undefined) {
      return true;
    }

    if (
      this.authorizationService.doesUserHaveRoleForSite(
        RoleEnum.ADMINISTRATOR_SYSTEM,
        globalLocationId
      )
    ) {
      return false;
    }

    return this.isReadOnlyMode;
  }

  public ngOnInit(): void {
    this.selectedMassUpload = new MassUploadType();

    this.massUploadService.metaData.subscribe((data) => {
      if (data) {
        this.massUploadsByCategory = Object.values(
          groupBy(data, (massUploadType) => massUploadType.category)
        );
      }
    });

    this.updateSubscription = this.dataUpdateService.updateEmitter.subscribe(
      (update: Update) => {
        if (
          update.updatedType === UpdateType.MASS_UPLOAD_PROGRESS &&
          update.userWbi === this.currentUser.wbi
        ) {
          this.progressBarValue = update.updateData;
        }
      }
    );
  }

  public ngOnDestroy(): void {
    this.updateSubscription?.unsubscribe();
  }

  public async setFile(event: Event): Promise<void> {
    const target = event.target as HTMLInputElement;
    this.isUploadDisabled = true;
    this.isUploading = false;
    this.resetResults();

    if (target.files && target.files.length > 0) {
      const file = target.files[0];
      if (file.size > this.FORTY_MEGABYTES) {
        this.elementRef.nativeElement.value = "";
        SnackbarUtils.displayErrorMsg(
          this.snackBar,
          "File exceeds maximum limit of 40MB."
        );
      } else {
        this.file = file;
        this.fileName = file.name;
        this.fileSize = file.size / 1000 + " KB";
        this.fileDateModified = format(
          new Date(file.lastModified),
          "yyyy-MM-dd HH:mm"
        );

        const initialMassUploadResponse = await this.verify(this.file);

        if (initialMassUploadResponse) {
          this.recordsMetaData =
            "Record(s) to be loaded: " +
            initialMassUploadResponse?.recordsToBeLoaded +
            ", ignored: " +
            initialMassUploadResponse?.ignoreCount;
          this.totalRecords =
            initialMassUploadResponse?.recordsToBeLoaded +
            initialMassUploadResponse?.ignoreCount;

          if (
            initialMassUploadResponse.errorCount === 0 &&
            initialMassUploadResponse.recordsToBeLoaded > 0
          ) {
            this.isUploadDisabled = false;
          } else {
            this.completeMassUploadResponse = initialMassUploadResponse;
            this.showMassUploadFeedback(this.completeMassUploadResponse);
          }
        }
      }
    }
  }

  public async upload(): Promise<void> {
    if (!this.file) return;
    try {
      this.isUploadDisabled = true;
      this.isUploading = true;

      this.completeMassUploadResponse = await this.load(this.file);
      if (this.completeMassUploadResponse) {
        this.showMassUploadFeedback(this.completeMassUploadResponse);
      }
    } finally {
      this.isUploadDisabled = false;
      this.isUploading = false;
    }
  }

  public resetFields(): void {
    this.elementRef.nativeElement.value = "";
    this.verifyError = "";
    this.uploadError = "";
    this.file = undefined;
    this.fileName = undefined;
    this.fileSize = undefined;
    this.fileDateModified = undefined;
    this.recordsMetaData = undefined;
    this.isUploadDisabled = true;
    this.isUploading = false;
    this.resetResults();
  }

  public resetResults(): void {
    this.completeMassUploadResponse = null;
    this.progressBarValue = 0;
    this.successCount = 0;
    this.errorCount = 0;
    this.ignoreCount = 0;
    this.errors = [];
    this.unuploadedObjects = [];
  }

  public hasDelimiter(error: string): boolean {
    return error.indexOf(";;") > -1 && error.split(";;")[0] != undefined;
  }

  protected downloadMassUploadResults(): void {
    if (!this.completeMassUploadResponse || !this.file) return;

    this.massUploadService
      .downloadResults(
        this.file,
        this.completeMassUploadResponse,
        this.selectedMassUpload?.type ?? ""
      )
      .subscribe({
        next: (blob) => {
          const fileName = "mass-upload-result.xlsx"; // Change as needed
          const blobUrl = window.URL.createObjectURL(blob);
          const anchor = document.createElement("a");
          anchor.href = blobUrl;
          anchor.download = fileName;
          anchor.click();
          window.URL.revokeObjectURL(blobUrl);
        },
        error: () => {
          SnackbarUtils.displayServerErrorMsg(
            this.snackBar,
            "Failed to download mass upload results"
          );
        },
      });
  }

  private async verify(file: File): Promise<MassUploadResponse | null> {
    this.isLoadingResults = true;
    let massUploadResponse: MassUploadResponse | null = null;

    if (file !== undefined) {
      try {
        massUploadResponse = await firstValueFrom(
          this.massUploadService.verify(
            file,
            this.selectedMassUpload?.type ?? ""
          )
        );

        this.verifyError = "";
      } catch (response) {
        if (response instanceof HttpErrorResponse) {
          if (!response.url) {
            this.verifyError = `Could not find the file ${file.name}.'+
            ' Possible causes: the file might have been moved or renamed.`;
          } else if (response.status === 403) {
            this.verifyError = `You are not authorized to use mass upload ${this.selectedMassUpload?.massUploadName}`;
          } else if (response.status === 500) {
            this.verifyError =
              "Something went wrong trying to process the request. Try again.";
          } else {
            this.verifyError = response.error;
          }
        }
      } finally {
        this.isLoadingResults = false;
      }
    }

    return massUploadResponse;
  }

  private async load(file: File): Promise<MassUploadResponse | null> {
    let massUploadResponse: MassUploadResponse | null = null;

    if (file !== undefined) {
      this.isLoadingResults = true;
      this.uploadError = "";
      try {
        massUploadResponse = await firstValueFrom(
          this.massUploadService.load(
            file,
            this.sendEmail,
            this.selectedMassUpload?.type ?? ""
          )
        );
      } catch (error) {
        if (error instanceof HttpErrorResponse) {
          if (!error.url) {
            this.uploadError = `Could not find the file ${this.file?.name}.'+ ' Possible causes: the file might have been moved or renamed.`;
          } else if (error.status === 500) {
            this.uploadError =
              "Something went wrong trying to process the request. Try again.";
          } else {
            this.uploadError = error.message;
          }
        } else {
          this.uploadError = "Unknown error occurred";
        }
      } finally {
        this.isLoadingResults = false;
      }
    }

    return massUploadResponse;
  }

  private showMassUploadFeedback(massUploadResponse: MassUploadResponse): void {
    this.successCount = massUploadResponse.successCount;
    this.errorCount = massUploadResponse.errorCount;
    this.ignoreCount = massUploadResponse.ignoreCount;

    massUploadResponse.errorMessages.forEach((errorMessage) => {
      this.errors.push(
        new MassUploadResponseFeedback(
          this.getRowNumber(errorMessage),
          this.getErrorMessage(errorMessage)
        )
      );
    });

    massUploadResponse.warningMessages.forEach((warningMessage) => {
      this.errors.push(
        new MassUploadResponseFeedback(
          this.getRowNumber(warningMessage),
          this.getErrorMessage(warningMessage)
        )
      );
    });

    massUploadResponse.excludedFromUploadList.forEach((unuploadedObject) => {
      this.unuploadedObjects.push(
        new MassUploadResponseFeedback(
          this.getRowNumber(unuploadedObject),
          this.getErrorMessage(unuploadedObject)
        )
      );
    });
  }

  private getRowNumber(error: string): number {
    if (this.hasDelimiter(error)) {
      return +error.split(";;")[0];
    }

    return -1;
  }

  private getErrorMessage(error: string): string {
    if (this.hasDelimiter(error)) {
      return error.split(";;")[1];
    }

    return error;
  }
}

export class MassUploadResponseFeedback {
  public excelRowNumber: number;
  public message: string;

  public constructor(excelRowNumber: number, message: string) {
    this.excelRowNumber = excelRowNumber;
    this.message = message;
  }
}
