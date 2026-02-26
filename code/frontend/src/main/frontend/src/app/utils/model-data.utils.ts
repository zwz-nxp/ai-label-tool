import { Model, ModelDisplayDto } from "../models/landingai/model";

/**
 * Model data formatting utilities
 * Implementation requirements 1.3, 1.4: Format metrics and confidence threshold
 */
export class ModelDataUtils {
  /**
   * Format percentage metrics
   * Implementation requirements 1.3, 7.2: Format numeric metrics as percentage display
   */
  static formatMetricAsPercentage(value: number | null | undefined): string {
    if (value === null || value === undefined) {
      return "--";
    }
    return `${Math.round(value)}%`;
  }

  /**
   * Format confidence threshold
   * Implementation requirements 1.4, 7.4: Format confidence threshold to two decimal places
   */
  static formatConfidenceThreshold(value: number | null | undefined): string {
    if (value === null || value === undefined) {
      return "N/A";
    }

    // Ensure value is within valid range (0-1)
    if (value < 0 || value > 1) {
      console.warn(
        `Invalid confidence threshold value: ${value}. Expected range: 0-1`
      );
      return "Invalid";
    }

    // Format to two decimal places
    return value.toFixed(2);
  }

  /**
   * Format confidence threshold as percentage display (optional)
   * Implementation requirements 1.4, 7.4: Format confidence threshold as percentage form
   */
  static formatConfidenceThresholdAsPercentage(
    value: number | null | undefined
  ): string {
    if (value === null || value === undefined) {
      return "N/A";
    }

    // Ensure value is within valid range (0-1)
    if (value < 0 || value > 1) {
      console.warn(
        `Invalid confidence threshold value: ${value}. Expected range: 0-1`
      );
      return "Invalid";
    }

    // Convert to percentage and format to two decimal places
    return `${(value * 100).toFixed(2)}%`;
  }

  /**
   * Validate metric value range
   */
  static isValidMetric(value: number): boolean {
    return value >= 0 && value <= 100;
  }

  /**
   * Validate confidence threshold range
   */
  static isValidConfidenceThreshold(value: number): boolean {
    return value >= 0 && value <= 1;
  }

  /**
   * Generate sample model data
   * Implementation requirements 1.2: Create sample model data matching the provided dataset
   */
  static generateSampleModels(): Model[] {
    return [
      {
        id: 1,
        projectId: 1,
        modelAlias: "ResNet-50 Image Classifier",
        trackId: "track_001",
        modelVersion: "v1.0.0",
        createdBy: "Alice Chen",
        isFavorite: true,
        createdAt: new Date("2024-01-15T10:30:00Z"),
        trainingF1Rate: 95.2,
        trainingPrecisionRate: 94.2,
        trainingRecallRate: 92.0,
        devF1Rate: 92.8,
        devPrecisionRate: 93.5,
        devRecallRate: 91.2,
        testF1Rate: 91.5,
        testPrecisionRate: 92.1,
        testRecallRate: 90.8,
        imageCount: 10000,
        labelCount: 50,
      },
      {
        id: 2,
        projectId: 1,
        modelAlias: "BERT Text Sentiment Analysis",
        trackId: "track_002",
        modelVersion: "v2.1.0",
        createdBy: "Bob Wang",
        isFavorite: false,
        createdAt: new Date("2024-01-20T14:45:00Z"),
        trainingF1Rate: 88.7,
        trainingPrecisionRate: 89.1,
        trainingRecallRate: 85.4,
        devF1Rate: 86.3,
        devPrecisionRate: 87.2,
        devRecallRate: 84.1,
        testF1Rate: undefined, // Test data not available
        testPrecisionRate: undefined,
        testRecallRate: undefined,
        imageCount: 5000,
        labelCount: 3,
      },
      {
        id: 3,
        projectId: 2,
        modelAlias: "YOLOv8 Object Detection",
        trackId: "track_003",
        modelVersion: "v1.2.0",
        createdBy: "Carol Liu",
        isFavorite: true,
        createdAt: new Date("2024-02-01T09:15:00Z"),
        trainingF1Rate: 92.4,
        trainingPrecisionRate: 91.8,
        trainingRecallRate: 89.2,
        devF1Rate: 89.6,
        devPrecisionRate: 90.3,
        devRecallRate: 87.5,
        testF1Rate: 88.9,
        testPrecisionRate: 89.7,
        testRecallRate: 86.8,
        imageCount: 15000,
        labelCount: 20,
      },
      {
        id: 4,
        projectId: 2,
        modelAlias: "Transformer Language Model",
        trackId: "track_004",
        modelVersion: "v3.0.0",
        createdBy: "David Zhang",
        isFavorite: false,
        createdAt: new Date("2024-02-10T16:20:00Z"),
        trainingF1Rate: 96.8,
        trainingPrecisionRate: 96.3,
        trainingRecallRate: 93.9,
        devF1Rate: 94.2,
        devPrecisionRate: 95.1,
        devRecallRate: 92.5,
        testF1Rate: 93.7,
        testPrecisionRate: 94.5,
        testRecallRate: 91.8,
        imageCount: 8000,
        labelCount: 100,
      },
      {
        id: 5,
        projectId: 3,
        modelAlias: "CNN Feature Extractor",
        trackId: "track_005",
        modelVersion: "v1.1.0",
        createdBy: "Eva Wu",
        isFavorite: true,
        createdAt: new Date("2024-02-15T11:30:00Z"),
        trainingF1Rate: 87.3,
        trainingPrecisionRate: 88.2,
        trainingRecallRate: 83.5,
        devF1Rate: 84.7,
        devPrecisionRate: 86.1,
        devRecallRate: 81.2,
        testF1Rate: undefined, // Test data not available
        testPrecisionRate: undefined,
        testRecallRate: undefined,
        imageCount: 12000,
        labelCount: 30,
      },
      {
        id: 6,
        projectId: 3,
        modelAlias: "Random Forest Classifier",
        trackId: "track_006",
        modelVersion: "v2.0.0",
        createdBy: "Frank Lin",
        isFavorite: false,
        createdAt: new Date("2024-02-20T13:45:00Z"),
        trainingF1Rate: 91.6,
        trainingPrecisionRate: 90.7,
        trainingRecallRate: 88.1,
        devF1Rate: 88.9,
        devPrecisionRate: 89.5,
        devRecallRate: 86.3,
        testF1Rate: 87.2,
        testPrecisionRate: 88.1,
        testRecallRate: 85.4,
        imageCount: 6000,
        labelCount: 15,
      },
    ];
  }

  /**
   * Generate random model data (for testing)
   */
  static generateRandomModel(id: number): Model {
    const creators = [
      "Alice Chen",
      "Bob Wang",
      "Carol Liu",
      "David Zhang",
      "Eva Wu",
      "Frank Lin",
    ];
    const modelTypes = [
      "ResNet",
      "BERT",
      "YOLO",
      "Transformer",
      "CNN",
      "Random Forest",
    ];
    const tasks = ["Classifier", "Detection", "Analysis", "Extractor", "Model"];

    const randomCreator = creators[Math.floor(Math.random() * creators.length)];
    const randomType =
      modelTypes[Math.floor(Math.random() * modelTypes.length)];
    const randomTask = tasks[Math.floor(Math.random() * tasks.length)];

    return {
      id,
      projectId: Math.floor(Math.random() * 5) + 1,
      modelAlias: `${randomType} ${randomTask}`,
      trackId: `track_${id.toString().padStart(3, "0")}`,
      modelVersion: `v${Math.floor(Math.random() * 3) + 1}.${Math.floor(Math.random() * 5)}.0`,
      createdBy: randomCreator,
      isFavorite: Math.random() > 0.7, // 30% chance to be favorite
      createdAt: new Date(
        Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000
      ), // Within past 30 days
      trainingF1Rate: Math.random() * 20 + 80, // 80-100%
      trainingPrecisionRate: Math.random() * 20 + 80,
      trainingRecallRate: Math.random() * 20 + 80,
      devF1Rate: Math.random() * 20 + 75, // 75-95%
      devPrecisionRate: Math.random() * 20 + 75,
      devRecallRate: Math.random() * 20 + 75,
      testF1Rate: Math.random() > 0.2 ? Math.random() * 20 + 70 : undefined, // 80% chance to have test data
      testPrecisionRate:
        Math.random() > 0.2 ? Math.random() * 20 + 70 : undefined,
      testRecallRate: Math.random() > 0.2 ? Math.random() * 20 + 70 : undefined,
      imageCount: Math.floor(Math.random() * 15000) + 1000,
      labelCount: Math.floor(Math.random() * 100) + 5,
    };
  }
}
