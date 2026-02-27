package com.nxp.iemdm.service;

import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelRequest;
import com.nxp.iemdm.shared.dto.landingai.GenerateModelResponse;
import com.nxp.iemdm.shared.dto.landingai.ModelWithMetricsDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Model operations. Provides business logic layer for model management
 * including CRUD operations, search, and favorite management.
 */
public interface ModelService {

  /**
   * Get all models with evaluation metrics.
   *
   * @return List of models with metrics
   */
  List<ModelWithMetricsDto> getAllModels();

  /**
   * Get paginated models list. Note: This method does not include ConfidentialReport data.
   *
   * @param pageable Pagination parameters
   * @return Page of models
   */
  Page<Model> getModelsPage(Pageable pageable);

  /**
   * Get specific model by ID with evaluation metrics.
   *
   * @param id Model ID
   * @return Model with metrics, or null if not found
   */
  ModelWithMetricsDto getModelById(Long id);

  /**
   * Get models list by project ID with evaluation metrics.
   *
   * @param projectId Project ID
   * @return List of models with metrics for the specified project
   */
  List<ModelWithMetricsDto> getModelsByProjectId(Long projectId);

  /**
   * Toggle model favorite status.
   *
   * @param id Model ID
   * @return Updated model with metrics
   * @throws com.nxp.iemdm.exception.NotFoundException if model not found
   */
  ModelWithMetricsDto toggleFavorite(Long id);

  /**
   * Search models by model alias or creator with evaluation metrics.
   *
   * @param query Search query (optional)
   * @param favoritesOnly Filter for favorites only
   * @param projectId Filter by project ID (optional)
   * @return List of models matching search criteria
   */
  List<ModelWithMetricsDto> searchModels(String query, Boolean favoritesOnly, Long projectId);

  /**
   * Delete model by ID (soft delete - sets status to INACTIVE).
   *
   * @param id Model ID
   * @throws com.nxp.iemdm.exception.NotFoundException if model not found
   */
  void deleteModel(Long id);

  /**
   * Get all prediction labels for a model (包含 Image 的 split 資訊). Frontend 會根據 Image.split 欄位在記憶體中過濾
   * train/dev/test.
   *
   * @param modelId Model ID
   * @return List of prediction labels with image information
   */
  List<?> getPredictionLabels(Long modelId);

  /**
   * Generate a new model with adjusted confidence threshold Requirements: 31.1, 33.6
   *
   * @param sourceModelId Source model ID
   * @param request Generate model request with new threshold and metrics
   * @return Generate model response with new model information
   */
  GenerateModelResponse generateModelWithNewThreshold(
      Long sourceModelId, GenerateModelRequest request);
}
