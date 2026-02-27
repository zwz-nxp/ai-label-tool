package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.*;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelRequest;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelResponse;
import com.nxp.iemdm.shared.dto.landingai.ModelWithMetricsDto;
import com.nxp.iemdm.shared.dto.landingai.RecalculatedMetrics;
import com.nxp.iemdm.shared.repository.jpa.landingai.*;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for Model operations in the operational layer. Provides internal endpoints that
 * are called by the API service layer. Includes all business logic for model management.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/models")
@RequiredArgsConstructor
public class ModelServiceImpl {

  private final ModelRepository modelRepository;
  private final TrainingRecordRepository trainingRecordRepository;
  private final ConfidentialReportRepository confidentialReportRepository;
  private final ImageRepository imageRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final SnapshotImageRepository snapshotImageRepository;
  private final SnapshotImageLabelRepository snapshotImageLabelRepository;
  private final LossChartRepository lossChartRepository;
  private final ValidationChartRepository validationChartRepository;
  private final EntityManager entityManager;

  /**
   * Get all models with evaluation metrics. Uses DTO to avoid JPA association issues.
   *
   * @return List of models with metrics
   */
  @MethodLog
  @GetMapping(produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ResponseEntity<List<ModelWithMetricsDto>> getAllModels() {
    log.debug("Operational REST: Fetching all models with metrics");
    List<ModelWithMetricsDto> models = modelRepository.findAllModelsWithMetrics();
    return ResponseEntity.ok(models);
  }

  /**
   * Get paginated models list. Note: This method does not include ConfidentialReport data.
   *
   * @param pageable Pagination parameters
   * @return Page of models
   */
  @MethodLog
  @GetMapping(path = "/page", produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ResponseEntity<Page<Model>> getModelsPage(Pageable pageable) {
    log.debug("Operational REST: Fetching models page: {}", pageable);
    Page<Model> models = modelRepository.findAll(pageable);
    return ResponseEntity.ok(models);
  }

  /**
   * Get specific model by ID with evaluation metrics.
   *
   * @param id Model ID
   * @return Model with metrics, or null if not found
   */
  @MethodLog
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ResponseEntity<ModelWithMetricsDto> getModelById(@PathVariable Long id) {
    log.debug("Operational REST: Fetching model by ID: {}", id);
    ModelWithMetricsDto model = modelRepository.findModelWithMetricsById(id);
    if (model != null) {
      return ResponseEntity.ok(model);
    }
    return ResponseEntity.notFound().build();
  }

  /**
   * Get models list by project ID with evaluation metrics.
   *
   * @param projectId Project ID
   * @return List of models with metrics for the specified project
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ResponseEntity<List<ModelWithMetricsDto>> getModelsByProjectId(
      @PathVariable Long projectId) {
    log.debug("Operational REST: Fetching models for project ID: {}", projectId);
    List<ModelWithMetricsDto> models = modelRepository.findModelsWithMetricsByProjectId(projectId);
    return ResponseEntity.ok(models);
  }

  /**
   * Toggle model favorite status. Uses Native SQL to update directly, avoiding loading associated
   * entities.
   *
   * @param id Model ID
   * @return Updated model with metrics
   * @throws NotFoundException if model not found
   */
  @MethodLog
  @PutMapping(path = "/{id}/favorite", produces = MediaType.APPLICATION_JSON)
  @Transactional
  public ResponseEntity<ModelWithMetricsDto> toggleFavorite(@PathVariable Long id) {
    log.info("Operational REST: Toggling favorite status for model ID: {}", id);

    // Query current favorite status (use DTO to avoid loading entire entity)
    ModelWithMetricsDto currentModel = modelRepository.findModelWithMetricsById(id);
    if (currentModel == null) {
      log.error("Model not found with ID: {}", id);
      return ResponseEntity.notFound().build();
    }

    // Calculate new favorite status
    Boolean currentFavoriteStatus = currentModel.getIsFavorite();
    Boolean newFavoriteStatus = (currentFavoriteStatus != null) ? !currentFavoriteStatus : true;

    try {
      // Use Native SQL to update directly, avoiding loading associated entities
      modelRepository.updateFavoriteStatus(id, newFavoriteStatus);

      // Force flush and clear EntityManager cache to ensure fresh data
      entityManager.flush();
      entityManager.clear();

      // Return updated DTO
      ModelWithMetricsDto updatedModel = modelRepository.findModelWithMetricsById(id);

      log.info(
          "Successfully toggled favorite for model {}: {} -> {}",
          id,
          currentFavoriteStatus,
          newFavoriteStatus);
      log.debug("Updated model isFavorite: {}", updatedModel.getIsFavorite());

      return ResponseEntity.ok(updatedModel);
    } catch (Exception e) {
      log.error("Error toggling favorite for model {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Search models by model alias or creator with evaluation metrics.
   *
   * @param query Search query (optional)
   * @param favoritesOnly Filter for favorites only
   * @param projectId Filter by project ID (optional)
   * @return List of models matching search criteria
   */
  @MethodLog
  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ResponseEntity<List<ModelWithMetricsDto>> searchModels(
      @RequestParam(required = false) String query,
      @RequestParam(required = false, defaultValue = "false") Boolean favoritesOnly,
      @RequestParam(required = false) Long projectId) {
    log.debug(
        "Operational REST: Searching models - query: {}, favoritesOnly: {}, projectId: {}",
        query,
        favoritesOnly,
        projectId);

    List<ModelWithMetricsDto> models;

    if (Boolean.TRUE.equals(favoritesOnly)) {
      if (query != null && !query.trim().isEmpty()) {
        // Search favorite models with query condition
        models = modelRepository.searchFavoriteModelsWithMetrics(query);
        log.debug("Found {} favorite models matching query: {}", models.size(), query);
      } else {
        // Get all favorite models
        models = modelRepository.findFavoriteModelsWithMetrics();
        log.debug("Found {} favorite models", models.size());
      }
    } else {
      if (query != null && !query.trim().isEmpty()) {
        // Search all models with query condition
        models = modelRepository.searchModelsWithMetrics(query);
        log.debug("Found {} models matching query: {}", models.size(), query);
      } else {
        // Get all models
        models = modelRepository.findAllModelsWithMetrics();
        log.debug("Found {} total models", models.size());
      }
    }

    // Filter by projectId if provided
    if (projectId != null) {
      models =
          models.stream()
              .filter(model -> projectId.equals(model.getProjectId()))
              .collect(java.util.stream.Collectors.toList());
      log.debug("Filtered to {} models for project {}", models.size(), projectId);
    }

    return ResponseEntity.ok(models);
  }

  /**
   * Delete model by setting status to INACTIVE (soft delete).
   *
   * @param id Model ID
   * @return No content on success, not found if model doesn't exist
   */
  @MethodLog
  @org.springframework.web.bind.annotation.DeleteMapping(path = "/{id}")
  @Transactional
  public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
    log.info("Operational REST: Deleting model ID: {}", id);

    // Check if model exists
    ModelWithMetricsDto model = modelRepository.findModelWithMetricsById(id);
    if (model == null) {
      log.error("Model not found with ID: {}", id);
      return ResponseEntity.notFound().build();
    }

    try {
      // Update status to INACTIVE (soft delete)
      modelRepository.updateStatus(id, "INACTIVE");

      // Force flush to ensure update is persisted
      entityManager.flush();
      entityManager.clear();

      log.info("Successfully deleted model ID: {}", id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting model {}: {}", id, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * ?��??��? Confidence Threshold ?��??��? Model
   *
   * <p>此方法�?複製來�? Model ?�其?�?�相?��??�表，並套用?��? confidence threshold ?��??��?算�? metrics??
   *
   * @param sourceModelId 來�? Model ID
   * @param request ?�含?��? threshold ?��??��?算�? metrics
   * @return ?�產?��? Model 資�?
   */
  @MethodLog
  @PostMapping(
      path = "/{sourceModelId}/generate-with-threshold",
      produces = MediaType.APPLICATION_JSON)
  @Transactional
  public ResponseEntity<GenerateModelResponse> generateModelWithNewThreshold(
      @PathVariable Long sourceModelId, @RequestBody GenerateModelRequest request) {
    log.info(
        "Operational REST: Generating new model from source ID: {} with threshold: {}",
        sourceModelId,
        request.getNewThreshold());

    try {
      // 1. 載入來�? Model
      Model sourceModel =
          modelRepository
              .findById(sourceModelId)
              .orElseThrow(
                  () -> new NotFoundException("Source model not found with ID: " + sourceModelId));

      // 2. 建�??��? Model（�?製�?位�??��? ID?�threshold?�metrics�?
      Model newModel = new Model();
      newModel.setProjectId(sourceModel.getProjectId());
      newModel.setModelAlias(sourceModel.getModelAlias());
      newModel.setTrackId(sourceModel.getTrackId());
      newModel.setModelVersion(sourceModel.getModelVersion());
      newModel.setStatus(sourceModel.getStatus());
      newModel.setImageCount(sourceModel.getImageCount());
      newModel.setLabelCount(sourceModel.getLabelCount());
      newModel.setIsFavorite(false); // ?�模?��?設�??��???
      newModel.setCreatedBy(sourceModel.getCreatedBy());

      // 設定新的 metrics（從 request 中取得重新計算的值）
      // 前端傳來的值是百分比格式 (0-100)，直接儲存到資料庫（與舊 model 格式一致）
      RecalculatedMetrics metrics = request.getRecalculatedMetrics();

      // Log 前端傳來的 metrics 數值（用於 debug）
      log.info("Recalculated metrics from frontend:");
      log.info(
          "  Train - F1: {}, Precision: {}, Recall: {}",
          metrics.getTrainF1(),
          metrics.getTrainPrecision(),
          metrics.getTrainRecall());
      log.info(
          "  Dev - F1: {}, Precision: {}, Recall: {}",
          metrics.getDevF1(),
          metrics.getDevPrecision(),
          metrics.getDevRecall());
      log.info(
          "  Test - F1: {}, Precision: {}, Recall: {}",
          metrics.getTestF1(),
          metrics.getTestPrecision(),
          metrics.getTestRecall());

      // 直接使用前端傳來的百分比格式數值 (0-100)，與舊 model 的格式保持一致
      newModel.setTrainingF1Rate(metrics.getTrainF1());
      newModel.setTrainingPrecisionRate(metrics.getTrainPrecision());
      newModel.setTrainingRecallRate(metrics.getTrainRecall());
      newModel.setDevF1Rate(metrics.getDevF1());
      newModel.setDevPrecisionRate(metrics.getDevPrecision());
      newModel.setDevRecallRate(metrics.getDevRecall());
      newModel.setTestF1Rate(metrics.getTestF1());
      newModel.setTestPrecisionRate(metrics.getTestPrecision());
      newModel.setTestRecallRate(metrics.getTestRecall());

      // 3. 複製 TrainingRecord（建立新??ID�?
      TrainingRecord sourceTrainingRecord = sourceModel.getTrainingRecord();
      TrainingRecord newTrainingRecord = new TrainingRecord();
      newTrainingRecord.setProject(sourceTrainingRecord.getProject());
      newTrainingRecord.setStatus(sourceTrainingRecord.getStatus());
      newTrainingRecord.setModelAlias(sourceTrainingRecord.getModelAlias());
      newTrainingRecord.setTrackId(sourceTrainingRecord.getTrackId());
      newTrainingRecord.setEpochs(sourceTrainingRecord.getEpochs());
      newTrainingRecord.setModelSize(sourceTrainingRecord.getModelSize());
      newTrainingRecord.setTransformParam(sourceTrainingRecord.getTransformParam());
      newTrainingRecord.setModelParam(sourceTrainingRecord.getModelParam());
      newTrainingRecord.setCreditConsumption(sourceTrainingRecord.getCreditConsumption());
      newTrainingRecord.setTrainingCount(sourceTrainingRecord.getTrainingCount());
      newTrainingRecord.setDevCount(sourceTrainingRecord.getDevCount());
      newTrainingRecord.setTestCount(sourceTrainingRecord.getTestCount());
      newTrainingRecord.setStartedAt(sourceTrainingRecord.getStartedAt());
      newTrainingRecord.setCompletedAt(sourceTrainingRecord.getCompletedAt());
      newTrainingRecord.setCreatedBy(sourceTrainingRecord.getCreatedBy());
      newTrainingRecord.setSnapshotId(sourceTrainingRecord.getSnapshotId());
      newTrainingRecord.setModelTrackKey(sourceTrainingRecord.getModelTrackKey());

      // ?��??��? TrainingRecord
      TrainingRecord savedTrainingRecord = trainingRecordRepository.save(newTrainingRecord);
      log.debug("Created new TrainingRecord with ID: {}", savedTrainingRecord.getId());

      // 設�? Model ??TrainingRecord ?�聯
      newModel.setTrainingRecord(savedTrainingRecord);

      // ?��??��? Model
      Model savedModel = modelRepository.save(newModel);
      log.info("Created new Model with ID: {}", savedModel.getId());

      // 4. 複製 la_images_label records（ground truth labels）
      // 注意：新的 Model 必須有完整且獨立的資料，包含 ground truth labels
      // 這樣即使原始 Model 被刪除，新 Model 仍然可以正常使用

      // 檢查 TrainingRecord 是否有 snapshotId
      Long snapshotId = savedTrainingRecord.getSnapshotId();
      int copiedGroundTruthCount = 0;

      if (snapshotId != null) {
        // 4.1 從 snapshot tables 複製 ground truth labels
        log.info("Copying ground truth labels from snapshot ID: {}", snapshotId);

        // 4.1.1 查詢 snapshot 的所有 image IDs
        List<Long> snapshotImageIds = snapshotImageRepository.findImageIdsBySnapshotId(snapshotId);

        // 4.1.2 查詢 snapshot 的所有 ground truth labels
        List<SnapshotImageLabel> snapshotLabels =
            snapshotImageLabelRepository.findBySnapshotIdAndImageIdIn(snapshotId, snapshotImageIds);

        // 4.1.3 將 SnapshotImageLabel 轉換為 ImageLabel 並儲存
        for (SnapshotImageLabel snapshotLabel : snapshotLabels) {
          // 查詢對應的 Image 實體（使用 snapshot 中的 image_id）
          Image image = imageRepository.findById(snapshotLabel.getImageId()).orElse(null);
          if (image == null) {
            log.warn(
                "Image not found for snapshot label, image_id: {}", snapshotLabel.getImageId());
            continue;
          }

          // 查詢對應的 ProjectClass 實體
          ProjectClass projectClass =
              entityManager.find(ProjectClass.class, snapshotLabel.getClassId());
          if (projectClass == null) {
            log.warn(
                "ProjectClass not found for snapshot label, class_id: {}",
                snapshotLabel.getClassId());
            continue;
          }

          // 建立新的 ImageLabel
          ImageLabel newLabel = new ImageLabel();
          newLabel.setImage(image);
          newLabel.setProjectClass(projectClass);
          newLabel.setPosition(snapshotLabel.getPosition());
          newLabel.setCreatedBy(snapshotLabel.getCreatedBy());
          imageLabelRepository.save(newLabel);
          copiedGroundTruthCount++;
        }
        log.info("Copied {} ground truth labels from snapshot", copiedGroundTruthCount);

      } else {
        // 4.2 從當前 images 複製 ground truth labels
        log.info(
            "Copying ground truth labels from current images for project ID: {}",
            sourceModel.getProjectId());

        // 4.2.1 查詢來源 model 的所有 images（包含 train/dev/test 三個 splits）
        List<Image> projectImages = new java.util.ArrayList<>();
        projectImages.addAll(
            imageRepository.findByProject_IdAndSplit(sourceModel.getProjectId(), "train"));
        projectImages.addAll(
            imageRepository.findByProject_IdAndSplit(sourceModel.getProjectId(), "dev"));
        projectImages.addAll(
            imageRepository.findByProject_IdAndSplit(sourceModel.getProjectId(), "test"));

        // 4.2.2 提取所有 image IDs
        List<Long> imageIds =
            projectImages.stream().map(Image::getId).collect(java.util.stream.Collectors.toList());

        // 4.2.3 查詢這些 images 的所有 ground truth labels
        List<ImageLabel> sourceGroundTruthLabels = imageLabelRepository.findByImageIds(imageIds);

        // 4.2.4 複製 ground truth labels（產生新的 label IDs）
        for (ImageLabel sourceLabel : sourceGroundTruthLabels) {
          ImageLabel newLabel = new ImageLabel();
          newLabel.setImage(sourceLabel.getImage()); // 關聯到相同的 image
          newLabel.setProjectClass(sourceLabel.getProjectClass()); // 關聯到相同的 class
          newLabel.setPosition(sourceLabel.getPosition()); // 複製 position
          newLabel.setCreatedBy(sourceLabel.getCreatedBy()); // 複製 created_by
          imageLabelRepository.save(newLabel);
          copiedGroundTruthCount++;
        }
        log.info("Copied {} ground truth labels from current images", copiedGroundTruthCount);
      }

      // 5. 複製 la_images_prediction_label records（新 IDs + model association）
      // 注意：需要複製所有 evaluation sets 的 prediction labels
      // 但只有 confidence rate >= 新 threshold 的 predictions 會被保留
      // 這樣新 Model 才能在所有 evaluation sets 中正確顯示混淆矩陣
      List<ImagePredictionLabel> sourcePredictionLabels =
          imagePredictionLabelRepository.findByModelId(sourceModelId);

      // 計算 threshold（從 0.0-0.99 轉換為 0-99）
      int thresholdInt = (int) Math.round(request.getNewThreshold() * 100);

      int copiedPredictionCount = 0;
      for (ImagePredictionLabel sourcePredictionLabel : sourcePredictionLabels) {
        // 只複製 confidence rate >= threshold 的 prediction labels
        if (sourcePredictionLabel.getConfidenceRate() >= thresholdInt) {
          ImagePredictionLabel newPredictionLabel = new ImagePredictionLabel();
          newPredictionLabel.setImage(sourcePredictionLabel.getImage());
          newPredictionLabel.setProjectClass(sourcePredictionLabel.getProjectClass());
          newPredictionLabel.setModel(savedModel); // 關聯到新的 Model
          newPredictionLabel.setPosition(sourcePredictionLabel.getPosition());
          newPredictionLabel.setConfidenceRate(sourcePredictionLabel.getConfidenceRate());
          newPredictionLabel.setCreatedBy(sourcePredictionLabel.getCreatedBy());
          imagePredictionLabelRepository.save(newPredictionLabel);
          copiedPredictionCount++;
        }
      }
      log.info(
          "Copied {} ImagePredictionLabel records out of {} (filtered by threshold {})",
          copiedPredictionCount,
          sourcePredictionLabels.size(),
          request.getNewThreshold());

      // 6. 複製 la_loss_chart records（新 IDs + model association）
      List<LossChart> sourceLossCharts =
          lossChartRepository.findByModelIdOrderByCreatedAtAsc(sourceModelId);
      for (LossChart sourceLossChart : sourceLossCharts) {
        LossChart newLossChart = new LossChart();
        newLossChart.setModel(savedModel); // 關聯到新的 Model
        newLossChart.setLoss(sourceLossChart.getLoss());
        newLossChart.setCreatedAt(sourceLossChart.getCreatedAt());
        newLossChart.setCreatedBy(sourceLossChart.getCreatedBy());
        lossChartRepository.save(newLossChart);
      }
      log.debug("Copied {} LossChart records", sourceLossCharts.size());

      // 7. 複製 la_validation_chart records（新 IDs + model association）
      List<ValidationChart> sourceValidationCharts =
          validationChartRepository.findByModelIdOrderByCreatedAtAsc(sourceModelId);
      for (ValidationChart sourceValidationChart : sourceValidationCharts) {
        ValidationChart newValidationChart = new ValidationChart();
        newValidationChart.setModel(savedModel); // 關聯到新的 Model
        newValidationChart.setMap(sourceValidationChart.getMap());
        newValidationChart.setCreatedAt(sourceValidationChart.getCreatedAt());
        newValidationChart.setCreatedBy(sourceValidationChart.getCreatedBy());
        validationChartRepository.save(newValidationChart);
      }
      log.debug("Copied {} ValidationChart records", sourceValidationCharts.size());

      // 8. 建立新的 la_confidential_report（包含重新計算的 metrics + 新的 threshold）
      ConfidentialReport newConfidentialReport = new ConfidentialReport();
      newConfidentialReport.setModel(savedModel);
      // 從 Double threshold (0.00-0.99) 轉換為 Integer (0-99)
      newConfidentialReport.setConfidenceThreshold(
          (int) Math.round(request.getNewThreshold() * 100));

      // 設定 correct rate：前端傳來的 F1 rate 已經是百分比格式 (0-100)
      // 因為 correct rate 是前端 Model List 顯示的主要指標
      // 直接使用前端計算的值，不需要再乘以 100
      if (metrics.getTrainF1() != null) {
        newConfidentialReport.setTrainingCorrectRate((int) Math.round(metrics.getTrainF1()));
      }
      if (metrics.getDevF1() != null) {
        newConfidentialReport.setDevCorrectRate((int) Math.round(metrics.getDevF1()));
      }
      if (metrics.getTestF1() != null) {
        newConfidentialReport.setTestCorrectRate((int) Math.round(metrics.getTestF1()));
      }

      newConfidentialReport.setCreatedBy(sourceModel.getCreatedBy());
      confidentialReportRepository.save(newConfidentialReport);
      log.debug("Created new ConfidentialReport with ID: {}", newConfidentialReport.getId());

      // 9. Force flush and clear EntityManager cache
      entityManager.flush();
      entityManager.clear();

      // 10. 建立 Response
      GenerateModelResponse response = new GenerateModelResponse();
      response.setNewModelId(savedModel.getId());
      response.setModelAlias(savedModel.getModelAlias());
      response.setConfidenceThreshold(request.getNewThreshold());

      log.info(
          "Successfully generated new model ID: {} from source ID: {} with threshold: {}",
          savedModel.getId(),
          sourceModelId,
          request.getNewThreshold());

      return ResponseEntity.ok(response);

    } catch (NotFoundException e) {
      log.error("Model not found: {}", e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error generating model from source {}: {}", sourceModelId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Get all prediction labels for a model (包含 Image 的 split 資訊). Frontend 可根據 Image.split 欄位進行過濾中選擇
   * train/dev/test. Requirements: 26.5, 31.1, 35.3
   *
   * @param modelId Model ID
   * @return List of prediction labels with image information
   */
  @MethodLog
  @GetMapping(path = "/{modelId}/prediction-labels", produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ResponseEntity<List<ImagePredictionLabel>> getPredictionLabels(
      @PathVariable Long modelId) {
    log.info("Operational REST: Getting all prediction labels for model ID: {}", modelId);

    try {
      // 使用 Repository 查詢該模型的所有 prediction labels
      List<ImagePredictionLabel> labels = imagePredictionLabelRepository.findByModelId(modelId);

      log.info("Found {} prediction labels for model ID: {}", labels.size(), modelId);

      return ResponseEntity.ok(labels);
    } catch (Exception e) {
      log.error("Error getting prediction labels for model {}: {}", modelId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
