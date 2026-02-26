import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
} from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";
import { Image } from "app/models/landingai/image.model";
import { Project } from "app/models/landingai/project.model";
import {
  ImageMetadata,
  MetadataService,
  ProjectMetadata,
} from "app/services/landingai/metadata.service";
import { DEFAULT_PROJECT_METADATA } from "../default-project-config";
import {
  AddMetadataDialogComponent,
  AddMetadataDialogData,
  AddMetadataDialogResult,
} from "../add-metadata-dialog/add-metadata-dialog.component";

export interface DisplayedMetadataField {
  id: number;
  name: string;
  type: string;
  valueFrom?: string;
  predefinedValues?: string;
  multipleValues?: boolean;
  showTextInput: boolean;
  showNumberInput: boolean;
  showDropdown: boolean;
  isMultiple: boolean;
  cachedValue: string;
  cachedValueArray: string[];
  cachedPredefinedValues: string[];
}

@Component({
  selector: "app-metadata-block",
  templateUrl: "./metadata-block.component.html",
  styleUrls: ["./metadata-block.component.scss"],
  standalone: false,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MetadataBlockComponent implements OnChanges, OnDestroy {
  @Input() currentImage: Image | null = null;
  @Input() project: Project | null = null;

  metadataFields: ProjectMetadata[] = [];
  displayedMetadataFields: DisplayedMetadataField[] = [];
  metadataValues: Map<number, ImageMetadata> = new Map();
  isLoadingFields = false;
  isLoadingValues = false;
  isSaving = false;
  hasAvailableFields = false;

  private hasLoadedValuesForCurrentImage = false;
  private destroy$ = new Subject<void>();

  constructor(
    private metadataService: MetadataService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["project"] && this.project) {
      this.loadMetadataFields();
    }

    if (changes["currentImage"]) {
      this.hasLoadedValuesForCurrentImage = false;
      if (this.currentImage) {
        if (this.metadataFields.length > 0) {
          this.loadMetadataValues();
        }
      } else {
        this.metadataValues.clear();
        this.displayedMetadataFields = [];
        this.updateAvailableCount();
      }
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private updateAvailableCount(): void {
    const displayedIds = new Set(this.displayedMetadataFields.map((f) => f.id));
    this.hasAvailableFields = this.metadataFields.some(
      (f) => !displayedIds.has(f.id)
    );
  }

  onAddMetadata(): void {
    const displayedIds = new Set(this.displayedMetadataFields.map((f) => f.id));
    const available = this.metadataFields.filter(
      (f) => !displayedIds.has(f.id)
    );
    if (available.length === 0) return;

    const dialogRef = this.dialog.open(AddMetadataDialogComponent, {
      width: "500px",
      data: { availableMetadata: available } as AddMetadataDialogData,
    });

    dialogRef.afterClosed().subscribe((result: AddMetadataDialogResult) => {
      if (
        result &&
        result.metadata &&
        result.value !== undefined &&
        result.value !== null
      ) {
        const preparedField = this.buildDisplayField(
          result.metadata,
          result.value
        );
        this.displayedMetadataFields = [
          ...this.displayedMetadataFields,
          preparedField,
        ];
        this.updateAvailableCount();
        this.cdr.markForCheck();

        if (this.currentImage && result.metadata.id) {
          this.isSaving = true;
          this.cdr.markForCheck();

          this.metadataService
            .addImageMetadata(
              this.currentImage.id,
              result.metadata.id,
              result.value
            )
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (savedMetadata) => {
                const key =
                  savedMetadata.projectMetadata?.id || result.metadata.id;
                this.metadataValues.set(key, savedMetadata);
                this.refreshFieldCache(key);
                this.isSaving = false;
                this.snackBar.open("Metadata saved successfully", "OK", {
                  duration: 2000,
                });
                this.cdr.markForCheck();
              },
              error: (error) => {
                console.error("Error saving metadata value:", error);
                alert("Failed to save metadata value.");
                this.isSaving = false;
                this.displayedMetadataFields =
                  this.displayedMetadataFields.filter(
                    (f) => f.id !== result.metadata.id
                  );
                this.updateAvailableCount();
                this.cdr.markForCheck();
              },
            });
        }
      }
    });
  }

  onRemoveMetadata(field: DisplayedMetadataField): void {
    this.displayedMetadataFields = this.displayedMetadataFields.filter(
      (f) => f.id !== field.id
    );
    this.updateAvailableCount();
    this.cdr.markForCheck();

    const metadata = this.metadataValues.get(field.id);
    if (metadata?.id && this.currentImage) {
      if (confirm(`Remove "${field.name}" value from this image?`)) {
        this.metadataService
          .deleteImageMetadata(this.currentImage.id, metadata.id)
          .pipe(takeUntil(this.destroy$))
          .subscribe({
            next: () => {
              this.metadataValues.delete(field.id);
              this.cdr.markForCheck();
            },
            error: (error) => console.error("Error deleting metadata:", error),
          });
      }
    }
  }

  onMetadataValueChange(field: DisplayedMetadataField, value: string): void {
    if (!this.currentImage || !field.id || this.isSaving) return;

    const existingMetadata = this.metadataValues.get(field.id);
    this.isSaving = true;
    this.cdr.markForCheck();

    const operation = existingMetadata?.id
      ? this.metadataService.updateImageMetadata(
          this.currentImage.id,
          existingMetadata.id,
          value
        )
      : this.metadataService.addImageMetadata(
          this.currentImage.id,
          field.id,
          value
        );

    operation.pipe(takeUntil(this.destroy$)).subscribe({
      next: (savedMetadata) => {
        this.metadataValues.set(field.id, savedMetadata);
        this.refreshFieldCache(field.id);
        this.isSaving = false;
        this.snackBar.open("Metadata saved successfully", "OK", {
          duration: 2000,
        });
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error("Error saving metadata value:", error);
        alert("Failed to save metadata value.");
        this.isSaving = false;
        this.cdr.markForCheck();
      },
    });
  }

  private buildDisplayField(
    field: ProjectMetadata,
    value?: string
  ): DisplayedMetadataField {
    const isPredefined = field.valueFrom?.toLowerCase() === "predefined";
    const type = (field.type || "").toLowerCase();
    const val = value || "";
    const predefinedValues = field.predefinedValues
      ? field.predefinedValues.split(",").map((v) => v.trim())
      : [];

    return {
      id: field.id,
      name: field.name,
      type: field.type,
      valueFrom: field.valueFrom,
      predefinedValues: field.predefinedValues,
      multipleValues: field.multipleValues,
      showTextInput:
        !isPredefined &&
        (type === "text" ||
          (!type && type !== "number" && type !== "dropdown") ||
          (type !== "number" && type !== "dropdown")),
      showNumberInput: !isPredefined && type === "number",
      showDropdown: type === "dropdown" || isPredefined,
      isMultiple: field.multipleValues === true,
      cachedValue: val,
      cachedValueArray: val
        ? val
            .split(",")
            .map((v) => v.trim())
            .filter((v) => v.length > 0)
        : [],
      cachedPredefinedValues: predefinedValues,
    };
  }

  private refreshFieldCache(fieldId: number): void {
    const idx = this.displayedMetadataFields.findIndex((f) => f.id === fieldId);
    if (idx === -1) return;

    const metadata = this.metadataValues.get(fieldId);
    const val = metadata?.value || "";

    // Mutate in place - no new object creation
    this.displayedMetadataFields[idx].cachedValue = val;
    this.displayedMetadataFields[idx].cachedValueArray = val
      ? val
          .split(",")
          .map((v) => v.trim())
          .filter((v) => v.length > 0)
      : [];
  }

  private loadMetadataFields(): void {
    if (!this.project) return;

    this.isLoadingFields = true;
    this.cdr.markForCheck();

    this.metadataService
      .getMetadataByProjectId(this.project.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (fields) => {
          this.metadataFields =
            fields.length === 0 ? DEFAULT_PROJECT_METADATA : fields;
          this.isLoadingFields = false;
          this.updateAvailableCount();

          if (this.currentImage && !this.hasLoadedValuesForCurrentImage) {
            this.loadMetadataValues();
          }
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error("Error loading metadata fields:", error);
          this.metadataFields = DEFAULT_PROJECT_METADATA;
          this.isLoadingFields = false;
          this.updateAvailableCount();
          this.cdr.markForCheck();
        },
      });
  }

  private loadMetadataValues(): void {
    if (!this.currentImage || this.hasLoadedValuesForCurrentImage) return;

    this.hasLoadedValuesForCurrentImage = true;
    this.isLoadingValues = true;
    this.cdr.markForCheck();

    this.metadataService
      .getMetadataByImageId(this.currentImage.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (values) => {
          this.metadataValues.clear();
          const newDisplayed: DisplayedMetadataField[] = [];

          values.forEach((metadata) => {
            const key = metadata.projectMetadata.id;
            this.metadataValues.set(key, metadata);

            const field = this.metadataFields.find((f) => f.id === key);
            if (field && !newDisplayed.find((f) => f.id === field.id)) {
              newDisplayed.push(this.buildDisplayField(field, metadata.value));
            }
          });

          this.displayedMetadataFields = newDisplayed;
          this.updateAvailableCount();
          this.isLoadingValues = false;
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error("Error loading metadata values:", error);
          this.metadataValues.clear();
          this.displayedMetadataFields = [];
          this.isLoadingValues = false;
          this.hasLoadedValuesForCurrentImage = false;
          this.updateAvailableCount();
          this.cdr.markForCheck();
        },
      });
  }
}
