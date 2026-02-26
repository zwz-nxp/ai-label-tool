import { UploadedImage } from "../models/landingai/test-model.model";

/**
 * Utility functions for image upload and processing.
 */

/**
 * Accepted image file formats for model testing.
 */
export const ACCEPTED_IMAGE_FORMATS = [
  "image/jpeg",
  "image/jpg",
  "image/bmp",
  "image/png",
  ".mpo",
];

/**
 * Maximum file size in bytes (10MB).
 */
export const MAX_FILE_SIZE = 10 * 1024 * 1024;

/**
 * Validates if a file is an accepted image format.
 *
 * @param file File to validate
 * @returns true if file format is accepted, false otherwise
 */
export function isValidImageFile(file: File): boolean {
  // Check file type
  const isValidType = ACCEPTED_IMAGE_FORMATS.some((format) => {
    if (format.startsWith(".")) {
      // Check file extension
      return file.name.toLowerCase().endsWith(format);
    } else {
      // Check MIME type
      return file.type === format;
    }
  });

  // Check file size
  const isValidSize = file.size <= MAX_FILE_SIZE;

  return isValidType && isValidSize;
}

/**
 * Validates multiple files and returns validation results.
 *
 * @param files Files to validate
 * @returns Object with valid files and error messages for invalid files
 */
export function validateFiles(files: File[]): {
  validFiles: File[];
  errors: string[];
} {
  const validFiles: File[] = [];
  const errors: string[] = [];

  files.forEach((file) => {
    if (!isValidImageFile(file)) {
      if (file.size > MAX_FILE_SIZE) {
        errors.push(`${file.name}: File size exceeds 10MB limit`);
      } else {
        errors.push(
          `${file.name}: Unsupported file format. Please upload JPEG, BMP, PNG, or MPO images.`
        );
      }
    } else {
      validFiles.push(file);
    }
  });

  return { validFiles, errors };
}

/**
 * Generates a blob URL from a File object for display.
 *
 * @param file File object
 * @returns Blob URL string
 */
export function generateBlobUrl(file: File): string {
  return URL.createObjectURL(file);
}

/**
 * Generates a thumbnail blob URL from a File object.
 * Uses canvas to resize the image to a smaller size.
 *
 * @param file File object
 * @param maxWidth Maximum width for thumbnail (default: 200px)
 * @param maxHeight Maximum height for thumbnail (default: 200px)
 * @returns Promise that resolves to thumbnail blob URL
 */
export function generateThumbnailUrl(
  file: File,
  maxWidth: number = 200,
  maxHeight: number = 200
): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (e: ProgressEvent<FileReader>) => {
      const img = new Image();

      img.onload = () => {
        // Calculate thumbnail dimensions maintaining aspect ratio
        let width = img.width;
        let height = img.height;

        if (width > height) {
          if (width > maxWidth) {
            height = (height * maxWidth) / width;
            width = maxWidth;
          }
        } else {
          if (height > maxHeight) {
            width = (width * maxHeight) / height;
            height = maxHeight;
          }
        }

        // Create canvas and draw resized image
        const canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;

        const ctx = canvas.getContext("2d");
        if (!ctx) {
          reject(new Error("Failed to get canvas context"));
          return;
        }

        ctx.drawImage(img, 0, 0, width, height);

        // Convert canvas to blob URL
        canvas.toBlob((blob) => {
          if (blob) {
            resolve(URL.createObjectURL(blob));
          } else {
            reject(new Error("Failed to create thumbnail blob"));
          }
        }, file.type);
      };

      img.onerror = () => {
        reject(new Error("Failed to load image"));
      };

      img.src = e.target?.result as string;
    };

    reader.onerror = () => {
      reject(new Error("Failed to read file"));
    };

    reader.readAsDataURL(file);
  });
}

/**
 * Creates an UploadedImage object from a File.
 *
 * @param file File object
 * @returns Promise that resolves to UploadedImage object
 */
export async function createUploadedImage(file: File): Promise<UploadedImage> {
  const blobUrl = generateBlobUrl(file);
  const thumbnailUrl = await generateThumbnailUrl(file);

  return {
    id: generateUUID(),
    file,
    blobUrl,
    thumbnailUrl,
    uploadedAt: new Date(),
  };
}

/**
 * Creates multiple UploadedImage objects from Files.
 *
 * @param files Array of File objects
 * @returns Promise that resolves to array of UploadedImage objects
 */
export async function createUploadedImages(
  files: File[]
): Promise<UploadedImage[]> {
  return Promise.all(files.map((file) => createUploadedImage(file)));
}

/**
 * Revokes blob URLs to free memory.
 * Should be called when images are no longer needed.
 *
 * @param images Array of UploadedImage objects
 */
export function revokeBlobUrls(images: UploadedImage[]): void {
  images.forEach((image) => {
    URL.revokeObjectURL(image.blobUrl);
    URL.revokeObjectURL(image.thumbnailUrl);
  });
}

/**
 * Generates a UUID v4.
 *
 * @returns UUID string
 */
function generateUUID(): string {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
