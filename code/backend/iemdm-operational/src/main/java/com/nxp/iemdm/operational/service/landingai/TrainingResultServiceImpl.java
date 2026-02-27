package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.model.landingai.*;
import com.nxp.iemdm.shared.dto.landingai.*;
import com.nxp.iemdm.shared.intf.operational.landingai.TrainingResultService;
import com.nxp.iemdm.shared.repository.jpa.landingai.*;
import jakarta.persistence.EntityManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for processing training results from Databricks API. Orchestrates the
 * entire flow from querying pending records to persisting results.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingResultServiceImpl implements TrainingResultService {

  // Repositories
  private final TrainingRecordRepository trainingRecordRepository;
  private final ModelRepository modelRepository;
  private final ConfidentialReportRepository confidentialReportRepository;
  private final LossChartRepository lossChartRepository;
  private final ValidationChartRepository validationChartRepository;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final SnapshotProjectClassRepository snapshotProjectClassRepository;
  private final ImageRepository imageRepository;
  private final ProjectClassRepository projectClassRepository;

  // Helper components
  private final DatabricksApiClient databricksApiClient;
  private final PredictionFileProcessor predictionFileProcessor;
  private final ClassIdMapper classIdMapper;

  private final EntityManager entityManager;

  @Override
  @Transactional
  public ProcessingResult processWaitingTrainingRecords() {
    log.info("Starting processing of training records with status WAITFORRESULT");

    // Query training records with status WAITFORRESULT
    List<TrainingRecord> waitingRecords =
        trainingRecordRepository.findByStatusOrderByStartedAtDesc("WAITFORRESULT");

    int totalRecords = waitingRecords.size();
    log.info("Found {} training records with status WAITFORRESULT", totalRecords);

    ProcessingResult result = new ProcessingResult();
    result.setTotalRecords(totalRecords);

    // Process each record
    for (TrainingRecord record : waitingRecords) {
      try {
        boolean success = processSingleTrainingRecord(record.getId());
        if (success) {
          result.incrementSuccess();
        } else {
          result.incrementFailure();
          result.addError("Failed to process training record ID: " + record.getId());
        }
      } catch (Exception e) {
        log.error("Error processing training record ID {}: {}", record.getId(), e.getMessage(), e);
        result.incrementFailure();
        result.addError(
            "Error processing training record ID " + record.getId() + ": " + e.getMessage());
      }
    }

    log.info(
        "Completed processing. Total: {}, Success: {}, Failure: {}",
        totalRecords,
        result.getSuccessCount(),
        result.getFailureCount());

    return result;
  }

  @Override
  public boolean processSingleTrainingRecord(Long trainingRecordId) {
    log.debug("Processing training record ID: {}", trainingRecordId);

    // Load training record
    TrainingRecord trainingRecord =
        trainingRecordRepository
            .findById(trainingRecordId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Training record not found: " + trainingRecordId));

    // Extract required data
    String trackId = trainingRecord.getTrackId();
    String modelAlias = trainingRecord.getModelAlias();
    Long projectId = trainingRecord.getProject().getId();
    Long snapshotId = trainingRecord.getSnapshotId();

    log.debug(
        "Processing trackId: {}, projectId: {}, snapshotId: {}", trackId, projectId, snapshotId);

    // Check if model exists with same track_id
    Model existingModel = modelRepository.findByTrackId(trackId);

    if (existingModel != null) {
      // Model exists
      if ("COMPLETED".equalsIgnoreCase(existingModel.getStatus())) {
        // Scenario A: Model exists with status COMPLETED
        log.info(
            "Model already completed for trackId: {}. Updating training record status.", trackId);

        // Update training record status
        trainingRecord.setStatus("COMPLETED");
        if (trainingRecord.getCompletedAt() == null) {
          trainingRecord.setCompletedAt(Instant.now());
        }
        trainingRecordRepository.save(trainingRecord);

        return true; // Skip API processing

      } else {
        // Scenario B: Model exists but not completed
        log.debug(
            "Model exists but not completed for trackId: {}. Continuing processing.", trackId);
        // Continue to API processing (don't create new model)
      }
    } else {
      // Scenario C: Model does not exist - create initial model
      log.debug("Creating initial model for trackId: {}", trackId);

      existingModel = new Model();
      existingModel.setTrackId(trackId);
      existingModel.setModelAlias(modelAlias);
      existingModel.setProjectId(projectId);
      existingModel.setTrainingRecord(trainingRecord);
      existingModel.setStatus("WAITFORRESULT");
      existingModel.setCreatedBy(trainingRecord.getCreatedBy());
      existingModel.setIsFavorite(false);

      existingModel = modelRepository.save(existingModel);
      log.debug("Created initial model with ID: {}", existingModel.getId());
    }

    // Continue with API processing
    return processTrainingResults(trainingRecord, existingModel);
  }

  /**
   * Process training results by calling API and persisting data.
   *
   * @param trainingRecord Training record
   * @param model Model entity
   * @return true if successful, false otherwise
   */
  private boolean processTrainingResults(TrainingRecord trainingRecord, Model model) {
    String trackId = trainingRecord.getTrackId();
    Path zipPath = null;
    Path csvPath = null;
    Path extractDir = null;

    try {
      // Step 1: Call Databricks API
      log.debug("Calling Databricks API for trackId: {}", trackId);
      String modelFullName = trainingRecord.getModelAlias(); // TODO: Build proper modelFullName
      TrainingResultResponse apiResponse =
          databricksApiClient.getTrainingResults(modelFullName, trackId);

      if (apiResponse == null) {
        log.error("Failed to get API response for trackId: {}", trackId);
        return false;
      }

      // Validate response
      if (!databricksApiClient.validateResponse(apiResponse)) {
        log.error("API response validation failed for trackId: {}", trackId);
        return false;
      }

      // Step 2: Extract chart data from API response
      // Note: prediction_file is no longer used, data comes directly from API response
      List<EpochMetrics> epochMetricsList = new ArrayList<>();

      // Convert loss chart data
      if (apiResponse.getLossChart() != null && !apiResponse.getLossChart().isEmpty()) {
        for (TrainingResultResponse.LossChartPoint point : apiResponse.getLossChart()) {
          EpochMetrics metrics = new EpochMetrics();
          metrics.setTrainBoxLoss(parseDouble(point.getLoss()));
          metrics.setEpoch(parseInteger(point.getEpoch()));
          epochMetricsList.add(metrics);
        }
        log.debug("Converted {} loss chart points", epochMetricsList.size());
      }

      // Convert validation chart data
      if (apiResponse.getValidationChart() != null && !apiResponse.getValidationChart().isEmpty()) {
        int index = 0;
        for (TrainingResultResponse.ValidationChartPoint point : apiResponse.getValidationChart()) {
          if (index < epochMetricsList.size()) {
            epochMetricsList.get(index).setMetricsMAP50B(parseDouble(point.getMap()));
          } else {
            EpochMetrics metrics = new EpochMetrics();
            metrics.setMetricsMAP50B(parseDouble(point.getMap()));
            metrics.setEpoch(parseInteger(point.getEpoch()));
            epochMetricsList.add(metrics);
          }
          index++;
        }
        log.debug("Converted {} validation chart points", apiResponse.getValidationChart().size());
      }

      // Step 3: Build class ID mapping
      Map<Integer, Long> classIdMapping =
          classIdMapper.buildSequenceToClassIdMap(
              trainingRecord.getProject().getId(), trainingRecord.getSnapshotId());

      // Step 4: Update model with training results
      updateModelWithResults(model, apiResponse);

      // Step 5: Create confidential report
      createConfidentialReport(model, apiResponse);

      // Step 6: Create loss charts
      createLossCharts(model, epochMetricsList);

      // Step 7: Create validation charts
      createValidationCharts(model, epochMetricsList);

      // Step 8: Create prediction labels from prediction_images
      createPredictionLabelsFromImages(
          model, apiResponse.getPredictionImages(), trainingRecord.getProject().getId());

      // Step 9: Update training record
      trainingRecord.setStatus("COMPLETED");
      trainingRecord.setCompletedAt(Instant.now());
      trainingRecordRepository.save(trainingRecord);

      // Flush and clear to ensure all changes are persisted
      entityManager.flush();
      entityManager.clear();

      log.info("Successfully processed training record for trackId: {}", trackId);
      return true;

    } catch (Exception e) {
      log.error("Error processing training results for trackId {}: {}", trackId, e.getMessage(), e);
      return false;

    } finally {
      // Clean up temporary files
      if (zipPath != null || csvPath != null || extractDir != null) {
        predictionFileProcessor.cleanupTempFiles(zipPath, csvPath, extractDir);
      }
    }
  }

  /** Update model with training results from API response. */
  private void updateModelWithResults(Model model, TrainingResultResponse apiResponse) {
    log.debug("Updating model ID {} with training results", model.getId());

    // Parse and set training metrics (乘以 100 轉換為百分比格式)
    model.setTrainingF1Rate(parseDoubleAsPercentage(apiResponse.getTrainingF1Rate()));
    model.setTrainingPrecisionRate(parseDoubleAsPercentage(apiResponse.getTrainingPrecisionRate()));
    model.setTrainingRecallRate(parseDoubleAsPercentage(apiResponse.getTrainingRecallRate()));

    // Parse and set dev metrics (乘以 100 轉換為百分比格式)
    model.setDevF1Rate(parseDoubleAsPercentage(apiResponse.getDevF1Rate()));
    model.setDevPrecisionRate(parseDoubleAsPercentage(apiResponse.getDevPrecisionRate()));
    model.setDevRecallRate(parseDoubleAsPercentage(apiResponse.getDevRecallRate()));

    // Parse and set test metrics (乘以 100 轉換為百分比格式)
    model.setTestF1Rate(parseDoubleAsPercentage(apiResponse.getTestF1Rate()));
    model.setTestPrecisionRate(parseDoubleAsPercentage(apiResponse.getTestPrecisionRate()));
    model.setTestRecallRate(parseDoubleAsPercentage(apiResponse.getTestRecallRate()));

    // Set model version
    model.setModelVersion(apiResponse.getModelVersion());

    // Note: model_alias is set from training_record.model_alias, not from API response
    // The API response contains model_full_name which is different from model_alias

    // Set status to COMPLETED
    model.setStatus("COMPLETED");

    modelRepository.save(model);
    log.debug("Model ID {} updated successfully", model.getId());
  }

  /** Create confidential report from API response. */
  private void createConfidentialReport(Model model, TrainingResultResponse apiResponse) {
    log.debug("Creating confidential report for model ID {}", model.getId());

    ConfidentialReport report = new ConfidentialReport();
    report.setModel(model);
    report.setTrainingCorrectRate(parsePercentageToInteger(apiResponse.getTrainingCorrectRate()));
    report.setDevCorrectRate(parsePercentageToInteger(apiResponse.getDevCorrectRate()));
    report.setTestCorrectRate(parsePercentageToInteger(apiResponse.getTestCorrectRate()));
    report.setConfidenceThreshold(parsePercentageToInteger(apiResponse.getConfidenceThreshhold()));
    report.setCreatedBy("SYSTEM");

    confidentialReportRepository.save(report);
    log.debug("Confidential report created for model ID {}", model.getId());
  }

  /** Create loss chart records from epoch metrics. */
  private void createLossCharts(Model model, List<EpochMetrics> epochMetricsList) {
    if (epochMetricsList.isEmpty()) {
      log.debug("No epoch metrics available for loss charts");
      return;
    }

    log.debug(
        "Creating {} loss chart records for model ID {}", epochMetricsList.size(), model.getId());

    List<LossChart> lossCharts = new ArrayList<>();
    for (EpochMetrics metrics : epochMetricsList) {
      LossChart chart = new LossChart();
      chart.setModel(model);
      // 直接使用 BigDecimal 儲存小數值 (0.52, 0.46 等)
      chart.setLoss(
          metrics.getTrainBoxLoss() != null
              ? java.math.BigDecimal.valueOf(metrics.getTrainBoxLoss())
              : null);
      chart.setCreatedBy("SYSTEM");
      chart.setCreatedAt(Instant.now());
      lossCharts.add(chart);
    }

    lossChartRepository.saveAll(lossCharts);
    log.debug("Created {} loss chart records", lossCharts.size());
  }

  /** Create validation chart records from epoch metrics. */
  private void createValidationCharts(Model model, List<EpochMetrics> epochMetricsList) {
    if (epochMetricsList.isEmpty()) {
      log.debug("No epoch metrics available for validation charts");
      return;
    }

    log.debug(
        "Creating {} validation chart records for model ID {}",
        epochMetricsList.size(),
        model.getId());

    List<ValidationChart> validationCharts = new ArrayList<>();
    for (EpochMetrics metrics : epochMetricsList) {
      ValidationChart chart = new ValidationChart();
      chart.setModel(model);
      chart.setMap(
          metrics.getMetricsMAP50B() != null
              ? java.math.BigDecimal.valueOf(metrics.getMetricsMAP50B())
              : null);
      chart.setCreatedBy("SYSTEM");
      chart.setCreatedAt(Instant.now());
      validationCharts.add(chart);
    }

    validationChartRepository.saveAll(validationCharts);
    log.debug("Created {} validation chart records", validationCharts.size());
  }

  /**
   * 從 prediction_images 建立 prediction label 記錄。
   *
   * @param model Model entity
   * @param predictionImages API response 中的 prediction_images 列表
   * @param projectId 專案 ID
   */
  private void createPredictionLabelsFromImages(
      Model model, List<TrainingResultResponse.PredictionImage> predictionImages, Long projectId) {
    if (predictionImages == null || predictionImages.isEmpty()) {
      log.debug("No prediction images available");
      return;
    }

    log.debug(
        "Creating prediction labels for model ID {} from {} prediction images",
        model.getId(),
        predictionImages.size());

    List<ImagePredictionLabel> imagePredictionLabels = new ArrayList<>();
    int totalPredictions = 0;
    int skippedCount = 0;

    // 處理每個 prediction image
    for (TrainingResultResponse.PredictionImage predictionImage : predictionImages) {
      String imageName = predictionImage.getImage();
      List<TrainingResultResponse.Prediction> predictions = predictionImage.getPredictions();

      if (predictions == null || predictions.isEmpty()) {
        log.debug("No predictions for image: {}", imageName);
        continue;
      }

      // 根據 image 名稱查詢 Image entity
      // 使用模糊匹配,因為檔名可能包含前綴
      Image image = findImageByName(imageName, projectId);
      if (image == null) {
        log.warn(
            "Image not found for name: {}, skipping {} predictions", imageName, predictions.size());
        skippedCount += predictions.size();
        continue;
      }

      // 處理每個 prediction
      for (TrainingResultResponse.Prediction prediction : predictions) {
        totalPredictions++;

        // 根據 class_id (實際上是 sequence) 查詢 ProjectClass
        ProjectClass projectClass = findProjectClassBySequence(prediction.getClassId(), projectId);
        if (projectClass == null) {
          log.warn(
              "ProjectClass not found for sequence: {}, image: {}, skipping prediction",
              prediction.getClassId(),
              imageName);
          skippedCount++;
          continue;
        }

        // 建立 ImagePredictionLabel
        ImagePredictionLabel imagePredictionLabel = new ImagePredictionLabel();
        imagePredictionLabel.setImage(image);
        imagePredictionLabel.setProjectClass(projectClass);
        imagePredictionLabel.setModel(model);

        // 將 bbox (List<Double>) 轉換為 JSON 字串
        imagePredictionLabel.setPosition(convertBboxToJson(prediction.getBbox()));

        // 將 confidence (0.82) 轉換為 confidenceRate (82)
        imagePredictionLabel.setConfidenceRate((int) Math.round(prediction.getConfidence() * 100));

        imagePredictionLabel.setCreatedBy("SYSTEM");

        imagePredictionLabels.add(imagePredictionLabel);
      }
    }

    // 批次儲存
    if (!imagePredictionLabels.isEmpty()) {
      imagePredictionLabelRepository.saveAll(imagePredictionLabels);
      log.info(
          "Created {} prediction label records from {} total predictions (skipped: {})",
          imagePredictionLabels.size(),
          totalPredictions,
          skippedCount);
    } else {
      log.warn(
          "No prediction labels created. Total predictions: {}, skipped: {}",
          totalPredictions,
          skippedCount);
    }
  }

  /**
   * 根據 image 名稱查詢 Image entity。 使用模糊匹配,因為檔名可能包含前綴。
   *
   * @param imageName image 名稱
   * @param projectId 專案 ID
   * @return Image entity 或 null
   */
  private Image findImageByName(String imageName, Long projectId) {
    // 先嘗試精確匹配
    java.util.Optional<Image> exactMatch = imageRepository.findByFileName(imageName);
    if (exactMatch.isPresent() && exactMatch.get().getProject().getId().equals(projectId)) {
      return exactMatch.get();
    }

    // 如果精確匹配失敗,嘗試模糊匹配 (檔名可能有前綴)
    // 取得專案的所有 images,然後比對檔名結尾
    List<Image> projectImages = imageRepository.findByProject_Id(projectId);
    for (Image image : projectImages) {
      if (image.getFileName() != null && image.getFileName().endsWith(imageName)) {
        log.debug("Found image by suffix match: {} -> {}", imageName, image.getFileName());
        return image;
      }
      // 也嘗試反向匹配 (imageName 可能包含前綴)
      if (imageName.endsWith(image.getFileName())) {
        log.debug("Found image by reverse suffix match: {} -> {}", imageName, image.getFileName());
        return image;
      }
    }

    return null;
  }

  /**
   * 根據 sequence (class_id) 查詢 ProjectClass entity。
   *
   * @param sequence class sequence (API 中的 class_id)
   * @param projectId 專案 ID
   * @return ProjectClass entity 或 null
   */
  private ProjectClass findProjectClassBySequence(int sequence, Long projectId) {
    // 取得專案的所有 classes,按 ID 排序
    List<ProjectClass> projectClasses =
        projectClassRepository.findByProjectIdOrderByIdAsc(projectId);

    // sequence 是從 0 開始的索引
    if (sequence >= 0 && sequence < projectClasses.size()) {
      return projectClasses.get(sequence);
    }

    log.warn(
        "Invalid sequence {} for project {}, total classes: {}",
        sequence,
        projectId,
        projectClasses.size());
    return null;
  }

  /**
   * 將 bbox (List<Double>) 轉換為 JSON 字串。 格式: [xcenter, ycenter, width, height]
   *
   * @param bbox bbox 列表
   * @return JSON 字串
   */
  private String convertBboxToJson(List<Double> bbox) {
    if (bbox == null || bbox.isEmpty()) {
      return "[]";
    }

    // 使用簡單的字串拼接建立 JSON 陣列
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < bbox.size(); i++) {
      if (i > 0) {
        json.append(",");
      }
      json.append(bbox.get(i));
    }
    json.append("]");

    return json.toString();
  }

  /** Parse string to Double, return null if parsing fails. */
  private Double parseDouble(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      log.warn("Failed to parse double value: {}", value);
      return null;
    }
  }

  /**
   * Parse string to Double and multiply by 100 for percentage display. 將小數格式 (0.85) 轉換為百分比格式 (85.0)
   */
  private Double parseDoubleAsPercentage(String value) {
    Double doubleValue = parseDouble(value);
    return doubleValue != null ? doubleValue * 100 : null;
  }

  /** Parse string to Integer, return null if parsing fails. */
  private Integer parseInteger(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      log.warn("Failed to parse integer value: {}", value);
      return null;
    }
  }

  /**
   * Parse percentage string to Integer (0.89 -> 89, "0.5" -> 50). Handles both decimal (0.89) and
   * percentage (89) formats.
   */
  private Integer parsePercentageToInteger(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      double doubleValue = Double.parseDouble(value.trim());
      // 如果值小於等於 1.0,視為小數格式 (0.89),需要乘以 100
      if (doubleValue <= 1.0) {
        return (int) Math.round(doubleValue * 100);
      }
      // 如果值大於 1.0,視為已經是百分比格式 (89)
      return (int) Math.round(doubleValue);
    } catch (NumberFormatException e) {
      log.warn("Failed to parse percentage value: {}", value);
      return null;
    }
  }
}
