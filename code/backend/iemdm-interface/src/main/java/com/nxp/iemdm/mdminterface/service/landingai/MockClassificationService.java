package com.nxp.iemdm.mdminterface.service.landingai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.mdminterface.configuration.landingai.DatabricksConfig.MockConfig;
import com.nxp.iemdm.mdminterface.dto.request.ClassificationTrainingRequest;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationResultsResponse;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationResultsResponse.*;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationTrainingResponse;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of ClassificationService for development and testing. Processes zip files
 * containing classification datasets (class folders with images).
 */
@Service
@ConditionalOnProperty(name = "databricks.mode", havingValue = "mock", matchIfMissing = true)
@Slf4j
public class MockClassificationService implements ClassificationService {

  private final Random random = new Random();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockConfig mockConfig;

  private static final String TEMP_SUBDIR = "temp";
  private static final String RESULTS_SUBDIR = "results";
  private static final String[] IMAGE_EXTENSIONS = {
    ".jpg", ".jpeg", ".png", ".JPG", ".JPEG", ".PNG", ".bmp", ".BMP"
  };

  @Override
  public ClassificationTrainingResponse submitTraining(ClassificationTrainingRequest request) {
    log.info(
        "[MOCK-CLS] Submitting classification training - trackId: {}, zipFilenames: {}, zipPath: {}",
        request.getTrackId(),
        request.getZipFilenames(),
        request.getZipPath());

    try {
      String firstZipFilename = request.getFirstZipFilename();
      if (firstZipFilename == null) {
        log.error("[MOCK-CLS] No zip files provided");
        return new ClassificationTrainingResponse("No zip files provided", request.getTrackId());
      }

      String zipFullPath = request.getZipPath() + firstZipFilename;
      Path zipFile = Paths.get(zipFullPath);

      if (!Files.exists(zipFile)) {
        log.error("[MOCK-CLS] Zip file not found: {}", zipFullPath);
        return new ClassificationTrainingResponse(
            "Zip file not found: " + zipFullPath, request.getTrackId());
      }

      // Create temp directory
      Path tempBaseDir = Paths.get(mockConfig.getBaseDir(), TEMP_SUBDIR);
      Files.createDirectories(tempBaseDir);
      Path tempDir =
          tempBaseDir.resolve("cls_" + request.getTrackId() + "_" + System.currentTimeMillis());
      Files.createDirectories(tempDir);
      log.info("[MOCK-CLS] Extracting zip to: {}", tempDir.toAbsolutePath());

      unzip(zipFile, tempDir);

      // Find dataset root (handles wrapper folders)
      Path datasetRoot = findClassificationDatasetRoot(tempDir);
      log.info("[MOCK-CLS] Dataset root detected at: {}", datasetRoot.toAbsolutePath());

      // Discover classes from folder structure
      Map<String, String> classNames = discoverClasses(datasetRoot);
      log.info("[MOCK-CLS] Discovered {} classes: {}", classNames.size(), classNames.values());

      // Process images from all splits
      List<ImagePrediction> predictions = new ArrayList<>();
      String[] splits = {"train", "val", "test"};

      int correctCount = 0;
      int totalCount = 0;

      for (String split : splits) {
        Path splitDir = datasetRoot.resolve(split);
        if (!Files.exists(splitDir) || !Files.isDirectory(splitDir)) {
          log.info("[MOCK-CLS] No {} split found", split);
          continue;
        }

        // Iterate through class folders
        try (DirectoryStream<Path> classStream = Files.newDirectoryStream(splitDir)) {
          for (Path classDir : classStream) {
            if (!Files.isDirectory(classDir)) continue;

            String className = classDir.getFileName().toString();
            String classIndex = getClassIndex(classNames, className);
            if (classIndex == null) {
              log.warn("[MOCK-CLS] Unknown class folder: {}", className);
              continue;
            }

            // Process images in class folder
            try (DirectoryStream<Path> imageStream = Files.newDirectoryStream(classDir)) {
              for (Path imageFile : imageStream) {
                if (!Files.isRegularFile(imageFile)) continue;
                if (!isImageFile(imageFile.getFileName().toString())) continue;

                totalCount++;
                String relativePath =
                    split + "/" + className + "/" + imageFile.getFileName().toString();

                // Generate mock prediction (~75% accuracy)
                String predictedClass;
                double confidence;
                if (random.nextDouble() < 0.75) {
                  // Correct prediction
                  predictedClass = classIndex;
                  confidence = 0.70 + random.nextDouble() * 0.29; // 0.70-0.99
                  correctCount++;
                } else {
                  // Wrong prediction - pick random different class
                  predictedClass = getRandomDifferentClass(classNames, classIndex);
                  confidence = 0.10 + random.nextDouble() * 0.50; // 0.10-0.60 (lower confidence)
                }

                predictions.add(
                    new ImagePrediction(
                        relativePath,
                        classIndex,
                        predictedClass,
                        Math.round(confidence * 100000.0) / 100000.0));
              }
            }
          }
        }
      }

      log.info(
          "[MOCK-CLS] Processed {} images, mock accuracy: {:.2f}%",
          totalCount, (totalCount > 0 ? (correctCount * 100.0 / totalCount) : 0));

      // Build response
      ClassificationResultsResponse results =
          buildResultsResponse(
              request.getTrackId(), classNames, predictions, correctCount, totalCount);

      // Save results
      Path resultsDir = Paths.get(mockConfig.getBaseDir(), RESULTS_SUBDIR, request.getTrackId());
      Files.createDirectories(resultsDir);
      Path resultsFile = resultsDir.resolve("classification_results.json");
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(resultsFile.toFile(), results);
      log.info("[MOCK-CLS] Saved classification results to: {}", resultsFile.toAbsolutePath());

      // Cleanup
      if (mockConfig.isCleanupTemp()) {
        deleteDirectory(tempDir);
        log.info("[MOCK-CLS] Cleaned up temp directory");
      }

      return new ClassificationTrainingResponse("", request.getTrackId());

    } catch (Exception e) {
      log.error("[MOCK-CLS] Error processing classification training", e);
      return new ClassificationTrainingResponse("Error: " + e.getMessage(), request.getTrackId());
    }
  }

  @Override
  public ClassificationResultsResponse getTrainingResults(String trackId) {
    log.info("[MOCK-CLS] Retrieving classification results for trackId: {}", trackId);

    try {
      Path resultsFile =
          Paths.get(
              mockConfig.getBaseDir(), RESULTS_SUBDIR, trackId, "classification_results.json");

      if (!Files.exists(resultsFile)) {
        log.error("[MOCK-CLS] Classification results not found for trackId: {}", trackId);
        throw new RuntimeException("Classification results not found for trackId: " + trackId);
      }

      return objectMapper.readValue(resultsFile.toFile(), ClassificationResultsResponse.class);

    } catch (IOException e) {
      log.error("[MOCK-CLS] Error reading classification results", e);
      throw new RuntimeException("Error reading classification results: " + e.getMessage(), e);
    }
  }

  @Override
  public ClassificationResultsResponse testModel(ClassificationTrainingRequest request) {
    log.info("[MOCK-CLS] Testing classification model - trackId: {}", request.getTrackId());
    // For mock, test model works same as training - processes zip and generates predictions
    ClassificationTrainingResponse trainingResponse = submitTraining(request);
    if (trainingResponse.getError() != null && !trainingResponse.getError().isEmpty()) {
      throw new RuntimeException(trainingResponse.getError());
    }
    return getTrainingResults(request.getTrackId());
  }

  // ============ Helper Methods ============

  /** Find classification dataset root. Looks for train/, val/, or test/ folders. */
  private Path findClassificationDatasetRoot(Path extractedDir) throws IOException {
    // Check if train/val/test exists directly
    if (Files.exists(extractedDir.resolve("train"))
        || Files.exists(extractedDir.resolve("val"))
        || Files.exists(extractedDir.resolve("test"))) {
      return extractedDir;
    }

    // Search one level deep
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(extractedDir)) {
      for (Path subDir : stream) {
        if (Files.isDirectory(subDir)) {
          if (Files.exists(subDir.resolve("train"))
              || Files.exists(subDir.resolve("val"))
              || Files.exists(subDir.resolve("test"))) {
            log.info("[MOCK-CLS] Found dataset in wrapper folder: {}", subDir.getFileName());
            return subDir;
          }
        }
      }
    }

    log.warn("[MOCK-CLS] Could not find train/val/test folders, using extracted root");
    return extractedDir;
  }

  /** Discover class names from folder structure. Returns map of index -> class name. */
  private Map<String, String> discoverClasses(Path datasetRoot) throws IOException {
    Set<String> classNamesSet = new TreeSet<>(); // TreeSet for consistent ordering

    String[] splits = {"train", "val", "test"};
    for (String split : splits) {
      Path splitDir = datasetRoot.resolve(split);
      if (Files.exists(splitDir) && Files.isDirectory(splitDir)) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(splitDir)) {
          for (Path classDir : stream) {
            if (Files.isDirectory(classDir)) {
              classNamesSet.add(classDir.getFileName().toString());
            }
          }
        }
      }
    }

    // Convert to index map
    Map<String, String> classNames = new LinkedHashMap<>();
    int index = 0;
    for (String name : classNamesSet) {
      classNames.put(String.valueOf(index), name);
      index++;
    }
    return classNames;
  }

  private String getClassIndex(Map<String, String> classNames, String className) {
    for (Map.Entry<String, String> entry : classNames.entrySet()) {
      if (entry.getValue().equals(className)) {
        return entry.getKey();
      }
    }
    return null;
  }

  private String getRandomDifferentClass(Map<String, String> classNames, String excludeIndex) {
    List<String> indices = new ArrayList<>(classNames.keySet());
    indices.remove(excludeIndex);
    if (indices.isEmpty()) return excludeIndex;
    return indices.get(random.nextInt(indices.size()));
  }

  private boolean isImageFile(String filename) {
    for (String ext : IMAGE_EXTENSIONS) {
      if (filename.endsWith(ext)) return true;
    }
    return false;
  }

  private ClassificationResultsResponse buildResultsResponse(
      String trackId,
      Map<String, String> classNames,
      List<ImagePrediction> predictions,
      int correctCount,
      int totalCount) {

    ClassificationResultsResponse response = new ClassificationResultsResponse();
    response.setTrackId(trackId);
    response.setModelFullName("YOLO-v8-cls");
    response.setModelVersion("1");
    response.setConfidenceThreshold("0.5");
    response.setClassNames(classNames);
    response.setPredictions(predictions);

    // Calculate actual accuracy from predictions
    double actualAccuracy = totalCount > 0 ? (correctCount * 1.0 / totalCount) : 0.80;

    // Generate metrics based on actual accuracy with small variations
    response.setTrainingAccuracy(generateMetricAround(actualAccuracy, 0.03));
    response.setTrainingF1Rate(generateMetricAround(actualAccuracy - 0.02, 0.03));
    response.setTrainingPrecisionRate(generateMetricAround(actualAccuracy, 0.03));
    response.setTrainingRecallRate(generateMetricAround(actualAccuracy - 0.01, 0.03));

    response.setDevAccuracy(generateMetricAround(actualAccuracy - 0.02, 0.03));
    response.setDevF1Rate(generateMetricAround(actualAccuracy - 0.04, 0.03));
    response.setDevPrecisionRate(generateMetricAround(actualAccuracy - 0.02, 0.03));
    response.setDevRecallRate(generateMetricAround(actualAccuracy - 0.03, 0.03));

    response.setTestAccuracy(generateMetricAround(actualAccuracy - 0.03, 0.03));
    response.setTestF1Rate(generateMetricAround(actualAccuracy - 0.05, 0.03));
    response.setTestPrecisionRate(generateMetricAround(actualAccuracy - 0.03, 0.03));
    response.setTestRecallRate(generateMetricAround(actualAccuracy - 0.04, 0.03));

    // Generate loss chart (decreasing)
    int lossPoints = 10 + random.nextInt(11);
    List<LossChartPoint> lossChart = new ArrayList<>();
    for (int i = 0; i < lossPoints; i++) {
      double progress = (double) i / (lossPoints - 1);
      double loss = 0.5 - (0.4 * progress) + (random.nextDouble() * 0.05 - 0.025);
      loss = Math.max(0.05, Math.min(0.55, loss));
      lossChart.add(new LossChartPoint(String.format("%.4f", loss), String.valueOf(i)));
    }
    response.setLossChart(lossChart);

    // Generate accuracy chart (increasing)
    int accPoints = 10 + random.nextInt(11);
    List<AccuracyChartPoint> accuracyChart = new ArrayList<>();
    for (int i = 0; i < accPoints; i++) {
      double progress = (double) i / (accPoints - 1);
      double acc = 0.5 + (actualAccuracy - 0.5) * progress + (random.nextDouble() * 0.03 - 0.015);
      acc = Math.max(0.45, Math.min(0.99, acc));
      accuracyChart.add(new AccuracyChartPoint(String.format("%.4f", acc), String.valueOf(i)));
    }
    response.setAccuracyChart(accuracyChart);

    return response;
  }

  private String generateMetricAround(double center, double variance) {
    double value = center + (random.nextDouble() * 2 - 1) * variance;
    value = Math.max(0.0, Math.min(1.0, value));
    return String.format("%.4f", value);
  }

  private void unzip(Path zipFile, Path destDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName().replace("\\", "/");
        Path filePath = destDir.resolve(entryName).normalize();

        if (!filePath.startsWith(destDir.normalize())) {
          log.warn("[MOCK-CLS] Skipping unsafe zip entry: {}", entryName);
          zis.closeEntry();
          continue;
        }

        if (entry.isDirectory()) {
          Files.createDirectories(filePath);
        } else {
          Files.createDirectories(filePath.getParent());
          Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        zis.closeEntry();
      }
    }
  }

  private void deleteDirectory(Path dir) throws IOException {
    if (Files.exists(dir)) {
      Files.walk(dir)
          .sorted(Comparator.reverseOrder())
          .forEach(
              path -> {
                try {
                  Files.delete(path);
                } catch (IOException e) {
                  log.warn("[MOCK-CLS] Failed to delete: {}", path);
                }
              });
    }
  }
}
