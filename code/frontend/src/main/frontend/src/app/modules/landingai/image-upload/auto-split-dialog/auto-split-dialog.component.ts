import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  Inject,
  OnDestroy,
  OnInit,
} from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { AutoSplitService } from "app/services/landingai/auto-split.service";
import {
  AutoSplitRequest,
  ClassStats,
} from "app/models/landingai/auto-split.model";
import noUiSlider, { API } from "nouislider";

export interface AutoSplitDialogData {
  projectId: number;
}

@Component({
  selector: "app-auto-split-dialog",
  standalone: false,
  templateUrl: "./auto-split-dialog.component.html",
  styleUrls: ["./auto-split-dialog.component.scss"],
})
export class AutoSplitDialogComponent
  implements OnInit, AfterViewInit, OnDestroy
{
  currentStep = 0;

  // Step 1: Select Images
  includeAssigned = false;
  totalImagesToSplit = 0;
  classStats: ClassStats[] = [];

  // Step 2: Set Distribution
  adjustAllTogether = true;
  trainRatio = 70;
  devRatio = 20;
  testRatio = 10;

  classRatios: Map<number, { train: number; dev: number; test: number }> =
    new Map();
  classSliders: Map<number, API> = new Map();

  globalSlider?: API;
  loading = false;
  // Calculated preview values
  previewTrainCount = 0;
  previewDevCount = 0;
  previewTestCount = 0;
  averageDeviation = 0;
  private isInitializing = false;

  constructor(
    public dialogRef: MatDialogRef<AutoSplitDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AutoSplitDialogData,
    private snackBar: MatSnackBar,
    private autoSplitService: AutoSplitService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadImageStats();
  }

  ngAfterViewInit(): void {
    // Sliders will be initialized when moving to step 2
  }

  ngOnDestroy(): void {
    // Cleanup sliders
    if (this.globalSlider) {
      this.globalSlider.destroy();
    }
    this.classSliders.forEach((slider) => slider.destroy());
  }

  loadImageStats(): void {
    this.loading = true;

    this.autoSplitService
      .getAutoSplitStats(this.data.projectId, this.includeAssigned)
      .subscribe({
        next: (stats) => {
          this.classStats = stats.classStats;
          this.totalImagesToSplit = stats.totalImagesToSplit;

          // Initialize class ratios
          this.classStats.forEach((cls) => {
            this.classRatios.set(cls.classId, {
              train: this.trainRatio,
              dev: this.devRatio,
              test: this.testRatio,
            });
          });

          this.loading = false;
        },
        error: (error) => {
          console.error("Error loading auto-split stats:", error);
          this.snackBar.open("Failed to load statistics", "Close", {
            duration: 3000,
          });
          this.loading = false;
        },
      });
  }

  onIncludeAssignedChange(): void {
    this.loadImageStats();
  }

  onNext(): void {
    if (this.currentStep === 0) {
      if (this.totalImagesToSplit === 0) {
        this.snackBar.open("No images available for splitting", "Close", {
          duration: 3000,
        });
        return;
      }
      this.currentStep = 1;
      // Initialize sliders after moving to step 2 with longer delay
      setTimeout(() => this.initializeSliders(), 300);
    } else if (this.currentStep === 1) {
      this.assignSplit();
    }
  }

  onBack(): void {
    if (this.currentStep > 0) {
      this.currentStep--;
    }
  }

  onAdjustAllTogetherChange(): void {
    if (this.adjustAllTogether) {
      // Reset all class ratios to global ratio
      this.classStats.forEach((cls) => {
        this.classRatios.set(cls.classId, {
          train: this.trainRatio,
          dev: this.devRatio,
          test: this.testRatio,
        });
      });
    }
    // Reinitialize sliders with longer delay
    setTimeout(() => this.initializeSliders(), 300);
  }

  initializeSliders(): void {
    if (this.adjustAllTogether) {
      this.initializeGlobalSlider();
    } else {
      this.initializeClassSliders();
    }
    // Initialize charts
    this.updateCharts();
  }

  initializeGlobalSlider(): void {
    const sliderEl = document.getElementById("global-slider");
    console.log("Initializing global slider, element:", sliderEl);
    console.log(
      "Initial values:",
      this.trainRatio,
      this.devRatio,
      this.testRatio
    );

    if (!sliderEl) {
      console.error("Global slider element not found!");
      return;
    }

    // Destroy existing slider
    if (this.globalSlider) {
      this.globalSlider.destroy();
    }

    this.isInitializing = true;

    // Create slider with 2 handles (3 segments: train, dev, test)
    noUiSlider.create(sliderEl, {
      start: [this.trainRatio, this.trainRatio + this.devRatio],
      connect: [true, true, true],
      range: {
        min: 0,
        max: 100,
      },
      step: 1,
      tooltips: false,
      format: {
        to: (value) => Math.round(value),
        from: (value) => Number(value),
      },
    });

    console.log("Slider created with values:", [
      this.trainRatio,
      this.trainRatio + this.devRatio,
    ]);

    this.globalSlider = (sliderEl as any).noUiSlider as API;

    // Debug: Check if slider was created correctly
    if (this.globalSlider) {
      const handles = sliderEl.querySelectorAll(".noUi-handle");
      console.log("Number of handles found:", handles.length);
      console.log("Slider get values:", this.globalSlider.get());
    }

    // Update values on slider change (but not during initialization)
    if (this.globalSlider) {
      this.globalSlider.on("update", (values: (string | number)[]) => {
        if (this.isInitializing) {
          this.isInitializing = false;
          return;
        }

        const val1 = Number(values[0]);
        const val2 = Number(values[1]);

        this.trainRatio = Math.round(val1);
        this.devRatio = Math.round(val2 - val1);
        this.testRatio = Math.round(100 - val2);

        console.log(
          "Slider updated:",
          this.trainRatio,
          this.devRatio,
          this.testRatio
        );

        // Update all class ratios if adjusting together
        this.classStats.forEach((cls) => {
          this.classRatios.set(cls.classId, {
            train: this.trainRatio,
            dev: this.devRatio,
            test: this.testRatio,
          });
        });

        // Update charts
        this.updateCharts();

        // Trigger change detection to update the view
        this.cdr.detectChanges();
      });
    }
  }

  initializeClassSliders(): void {
    // Destroy existing sliders
    this.classSliders.forEach((slider) => slider.destroy());
    this.classSliders.clear();

    this.classStats.forEach((cls) => {
      const sliderEl = document.getElementById(`class-slider-${cls.classId}`);
      if (!sliderEl) return;

      const ratio = this.classRatios.get(cls.classId)!;

      noUiSlider.create(sliderEl, {
        start: [ratio.train, ratio.train + ratio.dev],
        connect: [true, true, true],
        range: {
          min: 0,
          max: 100,
        },
        step: 1,
        tooltips: false,
        format: {
          to: (value) => Math.round(value),
          from: (value) => Number(value),
        },
      });

      const slider = (sliderEl as any).noUiSlider as API;
      this.classSliders.set(cls.classId, slider);

      // Update values on slider change
      slider.on("update", (values: (string | number)[]) => {
        const val1 = Number(values[0]);
        const val2 = Number(values[1]);

        this.classRatios.set(cls.classId, {
          train: Math.round(val1),
          dev: Math.round(val2 - val1),
          test: Math.round(100 - val2),
        });

        // Update charts
        this.updateCharts();

        // Trigger change detection to update the view
        this.cdr.detectChanges();
      });
    });
  }

  assignSplit(): void {
    this.loading = true;

    const classRatiosObj: {
      [key: number]: { train: number; dev: number; test: number };
    } = {};
    this.classRatios.forEach((value, key) => {
      classRatiosObj[key] = value;
    });

    const request: AutoSplitRequest = {
      projectId: this.data.projectId,
      includeAssigned: this.includeAssigned,
      adjustAllTogether: this.adjustAllTogether,
      trainRatio: this.trainRatio,
      devRatio: this.devRatio,
      testRatio: this.testRatio,
      classRatios: classRatiosObj,
    };

    this.autoSplitService.assignSplits(request).subscribe({
      next: (updatedCount) => {
        this.loading = false;
        this.snackBar.open(
          `Successfully updated ${updatedCount} image(s) with new split assignments`,
          "Close",
          {
            duration: 5000,
            panelClass: ["success-snackbar"],
          }
        );
        this.dialogRef.close(true);
      },
      error: (error) => {
        console.error("Error assigning splits:", error);
        this.loading = false;
        this.snackBar.open(
          "Failed to assign splits. Please try again.",
          "Close",
          {
            duration: 5000,
            panelClass: ["error-snackbar"],
          }
        );
      },
    });
  }

  onClose(): void {
    this.dialogRef.close();
  }

  getStepLabel(step: number): string {
    switch (step) {
      case 0:
        return "Select images";
      case 1:
        return "Set split distribution";
      case 2:
        return "Done";
      default:
        return "";
    }
  }

  isStepComplete(step: number): boolean {
    return this.currentStep > step;
  }

  isStepActive(step: number): boolean {
    return this.currentStep === step;
  }

  /**
   * Update all charts based on current ratios
   */
  updateCharts(): void {
    // Calculate preview counts based on ratios
    const total = this.totalImagesToSplit;
    this.previewTrainCount = Math.round((total * this.trainRatio) / 100);
    const remaining = total - this.previewTrainCount;
    this.previewDevCount = Math.round(
      (remaining * this.devRatio) / (this.devRatio + this.testRatio)
    );
    this.previewTestCount =
      total - this.previewTrainCount - this.previewDevCount;

    // Calculate average deviation from target
    let totalDeviation = 0;
    this.classStats.forEach((cls) => {
      const ratio = this.classRatios.get(cls.classId);
      if (ratio) {
        totalDeviation += Math.abs(ratio.train - this.trainRatio);
        totalDeviation += Math.abs(ratio.dev - this.devRatio);
        totalDeviation += Math.abs(ratio.test - this.testRatio);
      }
    });
    this.averageDeviation =
      this.classStats.length > 0
        ? totalDeviation / (this.classStats.length * 3)
        : 0;

    this.cdr.detectChanges();
  }

  /**
   * Calculate what percentage of a split comes from a specific class
   */
  getClassPercentInSplit(classId: number, split: string): number {
    const cls = this.classStats.find((c) => c.classId === classId);
    if (!cls) return 0;

    const ratio = this.classRatios.get(classId);
    const classImageCount = cls.imageCount;

    let splitRatio = 0;
    let totalInSplit = 0;

    switch (split) {
      case "train":
        splitRatio = ratio?.train || this.trainRatio;
        totalInSplit = this.previewTrainCount;
        break;
      case "dev":
        splitRatio = ratio?.dev || this.devRatio;
        totalInSplit = this.previewDevCount;
        break;
      case "test":
        splitRatio = ratio?.test || this.testRatio;
        totalInSplit = this.previewTestCount;
        break;
    }

    const classCountInSplit = Math.round((classImageCount * splitRatio) / 100);
    return totalInSplit > 0 ? (classCountInSplit / totalInSplit) * 100 : 0;
  }
}
