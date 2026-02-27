package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.rest.landingai.ConfusionMatrixServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.CellDetailResponse;
import com.nxp.iemdm.shared.dto.landingai.ConfusionMatrixResponse;
import com.nxp.iemdm.shared.dto.landingai.ImageWithLabelsDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for confusion matrix operations (Port 8080). Delegates to
 * ConfusionMatrixServiceREST which calls operational layer.
 */
@Slf4j
@RestController
@RequestMapping("/api/landingai/confusion-matrix")
@RequiredArgsConstructor
public class ConfusionMatrixApiController {

  private final ConfusionMatrixServiceREST confusionMatrixServiceREST;

  /**
   * Calculate confusion matrix for a model and evaluation set.
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return Confusion matrix response
   */
  @MethodLog
  @GetMapping("/{modelId}/{evaluationSet}")
  public ResponseEntity<ConfusionMatrixResponse> getConfusionMatrix(
      @PathVariable Long modelId, @PathVariable String evaluationSet) {
    log.info(
        "API: Getting confusion matrix for modelId={}, evaluationSet={}", modelId, evaluationSet);

    try {
      ConfusionMatrixResponse response =
          confusionMatrixServiceREST.calculateConfusionMatrix(modelId, evaluationSet);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.error("Invalid parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Failed to calculate confusion matrix", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get detail for a specific cell (GT×Pred combination).
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @param gtClassId Ground truth class ID
   * @param predClassId Prediction class ID
   * @return Cell detail response with images
   */
  @MethodLog
  @GetMapping("/{modelId}/{evaluationSet}/cell")
  public ResponseEntity<CellDetailResponse> getCellDetail(
      @PathVariable Long modelId,
      @PathVariable String evaluationSet,
      @RequestParam Long gtClassId,
      @RequestParam Long predClassId) {
    log.info(
        "API: Getting cell detail for modelId={}, evaluationSet={}, gtClassId={}, predClassId={}",
        modelId,
        evaluationSet,
        gtClassId,
        predClassId);

    try {
      CellDetailResponse response =
          confusionMatrixServiceREST.getCellDetail(modelId, evaluationSet, gtClassId, predClassId);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.error("Invalid parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Failed to get cell detail", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get all images in evaluation set with correctness indicators.
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of images with ground truth and prediction labels
   */
  @MethodLog
  @GetMapping("/{modelId}/{evaluationSet}/all-images")
  public ResponseEntity<List<ImageWithLabelsDTO>> getAllImages(
      @PathVariable Long modelId, @PathVariable String evaluationSet) {
    log.info("API: Getting all images for modelId={}, evaluationSet={}", modelId, evaluationSet);

    try {
      List<ImageWithLabelsDTO> images =
          confusionMatrixServiceREST.getAllImages(modelId, evaluationSet);
      return ResponseEntity.ok(images);
    } catch (IllegalArgumentException e) {
      log.error("Invalid parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Failed to get all images", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get prediction labels for a model filtered by evaluation set. Backend 根據 evaluationSet 參數過濾資料，
   * 只回傳指定 evaluation set 的資料。 Requirements: 26.5, 31.1, 35.3
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of prediction labels for the specified evaluation set
   */
  @MethodLog
  @GetMapping("/{modelId}/{evaluationSet}/prediction-labels")
  public ResponseEntity<List<?>> getPredictionLabels(
      @PathVariable Long modelId, @PathVariable String evaluationSet) {
    log.info(
        "API: Getting prediction labels for modelId={}, evaluationSet={}", modelId, evaluationSet);

    try {
      List<?> labels = confusionMatrixServiceREST.getPredictionLabels(modelId, evaluationSet);
      return ResponseEntity.ok(labels);
    } catch (Exception e) {
      log.error(
          "Failed to get prediction labels for model {}, evaluationSet {}",
          modelId,
          evaluationSet,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get ground truth labels for a model's project filtered by evaluation set. Backend 根據
   * evaluationSet 參數過濾資料，只回傳指定 evaluation set 的資料。 Requirements: 26.5, 31.1, 35.3
   *
   * @param modelId Model ID (用來找到對應的 snapshot)
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of ground truth labels for the specified evaluation set
   */
  @MethodLog
  @GetMapping("/{modelId}/{evaluationSet}/ground-truth-labels")
  public ResponseEntity<List<?>> getGroundTruthLabels(
      @PathVariable Long modelId, @PathVariable String evaluationSet) {
    log.info(
        "API: Getting ground truth labels for modelId={}, evaluationSet={}",
        modelId,
        evaluationSet);

    try {
      List<?> labels = confusionMatrixServiceREST.getGroundTruthLabels(modelId, evaluationSet);
      return ResponseEntity.ok(labels);
    } catch (Exception e) {
      log.error(
          "Failed to get ground truth labels for model {}, evaluationSet {}",
          modelId,
          evaluationSet,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
