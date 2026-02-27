package com.nxp.iemdm.service;

import com.nxp.iemdm.shared.dto.landingai.ModelParamCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ModelParamDTO;
import com.nxp.iemdm.shared.dto.landingai.ModelParamUpdateRequest;
import java.util.List;

/**
 * Service interface for Model Parameter Configuration operations. Validates: Requirements 2.1, 2.3,
 * 2.4, 3.2, 4.2, 5.2, 6.1
 */
public interface ModelParamService {

  /**
   * Get all model parameters for a specific location.
   *
   * @param locationId the location ID
   * @return list of model parameter DTOs
   */
  List<ModelParamDTO> getModelParamsByLocation(Long locationId);

  /**
   * Get model parameters filtered by location and model type.
   *
   * @param locationId the location ID
   * @param modelType the model type (Object Detection, Classification, Segmentation)
   * @return list of model parameter DTOs
   */
  List<ModelParamDTO> getModelParamsByLocationAndType(Long locationId, String modelType);

  /**
   * Search model parameters by name (case-insensitive) for a specific location.
   *
   * @param locationId the location ID
   * @param modelName the model name to search for
   * @return list of model parameter DTOs
   */
  List<ModelParamDTO> searchModelParamsByName(Long locationId, String modelName);

  /**
   * Get a single model parameter by ID.
   *
   * @param id the model parameter ID
   * @return model parameter DTO
   */
  ModelParamDTO getModelParamById(Long id);

  /**
   * Create a new model parameter.
   *
   * @param request the create request containing model name, type, and parameters
   * @param locationId the location ID to associate with the model parameter
   * @param userId the user ID creating the model parameter
   * @return created model parameter DTO
   */
  ModelParamDTO createModelParam(ModelParamCreateRequest request, Long locationId, String userId);

  /**
   * Update an existing model parameter.
   *
   * @param id the model parameter ID to update
   * @param request the update request containing new values
   * @param userId the user ID performing the update
   * @return updated model parameter DTO
   */
  ModelParamDTO updateModelParam(Long id, ModelParamUpdateRequest request, String userId);

  /**
   * Delete a model parameter.
   *
   * @param id the model parameter ID to delete
   * @param userId the user ID performing the deletion
   */
  void deleteModelParam(Long id, String userId);
}
