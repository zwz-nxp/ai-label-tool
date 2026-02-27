package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.rest.landingai.ModelServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelRequest;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelResponse;
import com.nxp.iemdm.shared.dto.landingai.ModelWithMetricsDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST API controller for Landing AI model operations. */
@Slf4j
@RestController
@RequestMapping("/api/landingai/models")
public class ModelApiController {

  private final ModelServiceREST modelServiceREST;

  @Autowired
  public ModelApiController(ModelServiceREST modelServiceREST) {
    this.modelServiceREST = modelServiceREST;
  }

  /**
   * Get all models
   *
   * @return list of all models with metrics
   */
  @MethodLog
  @GetMapping
  public ResponseEntity<List<ModelWithMetricsDto>> getAllModels() {
    log.info("API: Getting all models");
    try {
      List<ModelWithMetricsDto> models = modelServiceREST.getAllModels();
      return ResponseEntity.ok(models);
    } catch (Exception e) {
      log.error("Failed to get all models", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get all models for a specific project
   *
   * @param projectId the project ID
   * @return list of models with metrics
   */
  @MethodLog
  @GetMapping("/project/{projectId}")
  public ResponseEntity<List<ModelWithMetricsDto>> getModelsByProjectId(
      @PathVariable Long projectId) {
    log.info("API: Getting models for project: {}", projectId);
    try {
      List<ModelWithMetricsDto> models = modelServiceREST.getModelsByProjectId(projectId);
      return ResponseEntity.ok(models);
    } catch (Exception e) {
      log.error("Failed to get models for project: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a single model by ID
   *
   * @param id the model ID
   * @return the model with metrics
   */
  @MethodLog
  @GetMapping("/{id}")
  public ResponseEntity<ModelWithMetricsDto> getModelById(@PathVariable Long id) {
    log.info("API: Getting model: {}", id);
    try {
      ModelWithMetricsDto model = modelServiceREST.getModelById(id);
      return ResponseEntity.ok(model);
    } catch (Exception e) {
      log.error("Failed to get model: {}", id, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Toggle favorite status for a model
   *
   * @param id the model ID
   * @return the updated model with metrics
   */
  @MethodLog
  @PutMapping("/{id}/favorite")
  public ResponseEntity<ModelWithMetricsDto> toggleFavorite(@PathVariable Long id) {
    log.info("API: Toggling favorite for model: {}", id);
    try {
      ModelWithMetricsDto model = modelServiceREST.toggleFavorite(id);
      return ResponseEntity.ok(model);
    } catch (Exception e) {
      log.error("Failed to toggle favorite for model: {}", id, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Search models by query and/or favorites filter
   *
   * @param query search query (optional)
   * @param favoritesOnly filter for favorites only (optional)
   * @param projectId filter by project ID (optional)
   * @return list of matching models with metrics
   */
  @MethodLog
  @GetMapping("/search")
  public ResponseEntity<List<ModelWithMetricsDto>> searchModels(
      @RequestParam(required = false) String query,
      @RequestParam(required = false, defaultValue = "false") Boolean favoritesOnly,
      @RequestParam(required = false) Long projectId) {
    log.info(
        "API: Searching models - query: {}, favoritesOnly: {}, projectId: {}",
        query,
        favoritesOnly,
        projectId);
    try {
      List<ModelWithMetricsDto> models =
          modelServiceREST.searchModels(query, favoritesOnly, projectId);
      return ResponseEntity.ok(models);
    } catch (Exception e) {
      log.error("Failed to search models", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a model by ID (soft delete - sets status to INACTIVE)
   *
   * @param id the model ID
   * @return no content on success
   */
  @MethodLog
  @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteModel(@PathVariable Long id) {
    log.info("API: Deleting model: {}", id);
    try {
      modelServiceREST.deleteModel(id);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Failed to delete model: {}", id, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get prediction labels for a model Requirements: 26.5, 31.1, 35.3 回傳所有 prediction
   * labels，frontend 會根據 Image.split 在記憶體中過濾
   *
   * @param modelId the model ID
   * @return list of prediction labels with image information
   */
  @MethodLog
  @GetMapping("/{modelId}/prediction-labels")
  public ResponseEntity<?> getPredictionLabels(@PathVariable Long modelId) {
    log.info("API: Getting all prediction labels for model: {}", modelId);
    try {
      Object labels = modelServiceREST.getPredictionLabels(modelId);
      return ResponseEntity.ok(labels);
    } catch (Exception e) {
      log.error("Failed to get prediction labels for model: {}", modelId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Generate a new model with adjusted confidence threshold Requirements: 31.1, 33.6
   *
   * @param sourceModelId the source model ID
   * @param request the generate model request
   * @return the generated model response
   */
  @MethodLog
  @PostMapping("/{sourceModelId}/generate-with-threshold")
  public ResponseEntity<GenerateModelResponse> generateModelWithNewThreshold(
      @PathVariable Long sourceModelId, @RequestBody GenerateModelRequest request) {
    log.info(
        "API: Generating new model from source: {} with threshold: {}",
        sourceModelId,
        request.getNewThreshold());
    try {
      GenerateModelResponse response =
          modelServiceREST.generateModelWithNewThreshold(sourceModelId, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Failed to generate model from source: {}", sourceModelId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
