import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from "@angular/core";
import { Image } from "app/models/landingai/image.model";
import { Project } from "app/models/landingai/project";
import { ImageService } from "app/services/landingai/image.service";

/**
 * Component for displaying and managing general image information
 * Includes export, delete, split assignment, and no-object flag
 */
@Component({
  selector: "app-general-block",
  templateUrl: "./general-block.component.html",
  styleUrls: ["./general-block.component.scss"],
  standalone: false,
})
export class GeneralBlockComponent implements OnChanges {
  @Input() currentImage: Image | null = null;
  @Input() project: Project | null = null;
  @Output() imageUpdated = new EventEmitter<Image>();
  @Output() imageDeleted = new EventEmitter<number>();
  @Output() imageExported = new EventEmitter<Image>();

  selectedSplit: "Unassigned" | "Train" | "Dev" | "Test" = "Unassigned";
  isNoClass = false;
  isUpdating = false;

  splitOptions: Array<"Unassigned" | "Train" | "Dev" | "Test"> = [
    "Unassigned",
    "Train",
    "Dev",
    "Test",
  ];

  splitTooltip =
    "Split assignment determines how the image is used in model training. " +
    "Train: Used for training the model. " +
    "Dev: Used for validation during training. " +
    "Test: Used for final model evaluation. " +
    "Unassigned: Not yet assigned to a split.";

  constructor(private imageService: ImageService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["currentImage"] && this.currentImage) {
      // Normalize split value to match dropdown options
      const split = this.currentImage.split;
      if (split) {
        // Handle case variations: "training" -> "Train", "dev" -> "Dev", etc.
        const normalized =
          split.charAt(0).toUpperCase() + split.slice(1).toLowerCase();
        // Map "Training" to "Train"
        if (normalized === "Training") {
          this.selectedSplit = "Train";
        } else if (
          ["Train", "Dev", "Test", "Unassigned"].includes(normalized as any)
        ) {
          this.selectedSplit = normalized as
            | "Unassigned"
            | "Train"
            | "Dev"
            | "Test";
        } else {
          this.selectedSplit = "Unassigned";
        }
      } else {
        this.selectedSplit = "Unassigned";
      }
      this.isNoClass = this.currentImage.isNoClass || false;
    }
  }

  /**
   * Get labeler names as comma-separated string
   */
  getLabelerNames(): string {
    if (
      !this.currentImage?.labelers ||
      this.currentImage.labelers.length === 0
    ) {
      return "-";
    }
    return this.currentImage.labelers.map((l) => l.name).join(", ");
  }

  /**
   * Get time since last label as human-readable string
   */
  getTimeSinceLastLabel(): string {
    if (!this.currentImage?.lastLabeledAt) {
      return "-";
    }

    const lastLabeledAt = new Date(this.currentImage.lastLabeledAt);
    const now = new Date();
    const diffMs = now.getTime() - lastLabeledAt.getTime();

    const seconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    const months = Math.floor(days / 30);
    const years = Math.floor(days / 365);

    if (years > 0) {
      return years === 1 ? "1 year ago" : `${years} years ago`;
    }
    if (months > 0) {
      return months === 1 ? "1 month ago" : `${months} months ago`;
    }
    if (days > 0) {
      return days === 1 ? "1 day ago" : `${days} days ago`;
    }
    if (hours > 0) {
      return hours === 1 ? "1 hour ago" : `${hours} hours ago`;
    }
    if (minutes > 0) {
      return minutes === 1 ? "1 minute ago" : `${minutes} minutes ago`;
    }
    return "Just now";
  }

  /**
   * Handle split dropdown change
   */
  onSplitChange(split: "Unassigned" | "Train" | "Dev" | "Test"): void {
    if (!this.currentImage || this.isUpdating) {
      return;
    }

    this.isUpdating = true;
    this.imageService.updateSplit(this.currentImage.id, split).subscribe({
      next: (updatedImage) => {
        this.selectedSplit = split;
        this.imageUpdated.emit(updatedImage);
        this.isUpdating = false;
      },
      error: (error) => {
        console.error("Error updating split:", error);
        // Revert to previous value
        this.selectedSplit = this.currentImage!.split || "Unassigned";
        this.isUpdating = false;
      },
    });
  }

  /**
   * Handle "No Object to Label" checkbox change
   */
  onNoClassChange(checked: boolean): void {
    if (!this.currentImage || this.isUpdating) {
      return;
    }

    this.isUpdating = true;
    this.imageService.updateIsNoClass(this.currentImage.id, checked).subscribe({
      next: (updatedImage) => {
        this.isNoClass = checked;
        this.imageUpdated.emit(updatedImage);
        this.isUpdating = false;
      },
      error: (error) => {
        console.error("Error updating isNoClass:", error);
        // Revert to previous value
        this.isNoClass = this.currentImage!.isNoClass || false;
        this.isUpdating = false;
      },
    });
  }

  /**
   * Handle download button click - downloads the image file directly
   */
  onDownload(): void {
    if (!this.currentImage) {
      return;
    }

    // Fetch the image file and trigger download
    this.imageService.getImageFileByImageId(this.currentImage.id).subscribe({
      next: (blob) => {
        // Create a download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = this.currentImage!.fileName;
        document.body.appendChild(link);
        link.click();

        // Cleanup
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error("Error downloading image:", error);
        alert("Failed to download image. Please try again.");
      },
    });
  }

  /**
   * Handle delete button click
   */
  onDelete(): void {
    if (this.currentImage) {
      if (
        confirm(
          `Are you sure you want to delete "${this.currentImage.fileName}"? This action cannot be undone.`
        )
      ) {
        this.imageService.deleteImage(this.currentImage.id).subscribe({
          next: () => {
            this.imageDeleted.emit(this.currentImage!.id);
          },
          error: (error) => {
            console.error("Error deleting image:", error);
            alert("Failed to delete image. Please try again.");
          },
        });
      }
    }
  }

  /**
   * Copy file name to clipboard
   */
  copyFileName(): void {
    if (this.currentImage?.fileName) {
      navigator.clipboard.writeText(this.currentImage.fileName).then(
        () => {
          // You could add a snackbar notification here if desired
        },
        (error) => {
          console.error("Failed to copy file name:", error);
          alert("Failed to copy file name to clipboard.");
        }
      );
    }
  }
}
