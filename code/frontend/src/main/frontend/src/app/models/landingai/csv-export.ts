/**
 * CSV Export Data Model
 * Represents data structure for CSV export functionality
 * Requirements: 20.4, 20.5, 20.6, 20.7, 20.8, 20.9, 20.10, 20.11, 20.12
 */

import { Image } from "./image";
import { Model } from "./model";

export interface CsvExportData {
  imageId: number;
  imageName: string;
  imagePath: string;
  modelId: number;
  modelName: string;
  gtClass: string; // Ground truth class (TBD)
  predClass: string; // Predicted class (TBD)
  gtPredJson: string; // Ground truth and prediction JSON (TBD)
}

/**
 * Generate CSV data from images and model
 * Requirements: 20.4, 20.5, 20.6, 20.7, 20.8, 20.9, 20.10, 20.11, 20.12
 * @param images Array of images
 * @param model Model data
 * @param split Split type (train/dev/test)
 * @returns Array of CSV export data
 */
export function generateCsvData(
  images: Image[],
  model: Model,
  split: string
): string {
  // If no images, create a single row with TBD values
  if (!images || images.length === 0) {
    const csvData: CsvExportData[] = [
      {
        imageId: 0,
        imageName: "TBD",
        imagePath: "TBD",
        modelId: model.id,
        modelName: model.modelAlias || "",
        gtClass: "TBD",
        predClass: "TBD",
        gtPredJson: "TBD",
      },
    ];
    return convertToCsv(csvData);
  }

  const csvData: CsvExportData[] = images.map((image) => ({
    imageId: image.id,
    imageName: image.fileName,
    imagePath: `project_${image.projectId}/images/${image.fileName}`,
    modelId: model.id,
    modelName: model.modelAlias || "",
    gtClass: "TBD", // To be implemented
    predClass: "TBD", // To be implemented
    gtPredJson: "{}", // To be implemented
  }));

  return convertToCsv(csvData);
}

/**
 * Generate CSV filename
 * Requirements: 20.14
 * @param modelId Model ID
 * @param splitType Split type (train/dev/test)
 * @returns Formatted filename
 */
export function generateCsvFilename(
  modelId: number,
  splitType: string
): string {
  const timestamp = new Date().toISOString().replace(/[:.]/g, "-");
  return `model_${modelId}_${splitType}_${timestamp}.csv`;
}

/**
 * Convert data array to CSV string
 * Requirements: 20.4, 20.13
 * @param data Array of CSV export data
 * @returns CSV string
 */
export function convertToCsv(data: CsvExportData[]): string {
  if (data.length === 0) {
    return "";
  }

  // CSV header
  const headers = [
    "Image ID",
    "Image Name",
    "Image Path",
    "Model ID",
    "Model Name",
    "GT_Class",
    "PRED_Class",
    "GT-PRED JSON",
  ];

  // CSV rows
  const rows = data.map((row) => [
    row.imageId,
    row.imageName,
    row.imagePath,
    row.modelId,
    row.modelName,
    row.gtClass,
    row.predClass,
    row.gtPredJson,
  ]);

  // Combine header and rows
  const csvContent = [headers, ...rows]
    .map((row) => row.map((cell) => `"${cell}"`).join(","))
    .join("\n");

  return csvContent;
}

/**
 * Trigger browser download of CSV file
 * Requirements: 20.14
 * @param csvContent CSV string content
 * @param filename Filename for download
 */
export function downloadCsv(csvContent: string, filename: string): void {
  const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
  const link = document.createElement("a");
  const url = URL.createObjectURL(blob);

  link.setAttribute("href", url);
  link.setAttribute("download", filename);
  link.style.visibility = "hidden";

  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  URL.revokeObjectURL(url);
}
