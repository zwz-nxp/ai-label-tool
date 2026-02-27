package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ModelParam;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.shared.dto.landingai.ModelParamCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ModelParamDTO;
import com.nxp.iemdm.shared.dto.landingai.ModelParamUpdateRequest;
import com.nxp.iemdm.shared.repository.jpa.LocationRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ModelParamRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operational service for managing model parameter configurations. Implements business logic for
 * CRUD operations on model parameters. Validates: Requirements 2.1, 2.3, 2.4, 3.2, 3.3, 3.4, 3.5,
 * 4.2, 4.3, 5.2, 6.1
 */
@Slf4j
@Service
public class ModelParamOperationalService {

  private final ModelParamRepository modelParamRepository;
  private final LocationRepository locationRepository;

  public ModelParamOperationalService(
      ModelParamRepository modelParamRepository, LocationRepository locationRepository) {
    this.modelParamRepository = modelParamRepository;
    this.locationRepository = locationRepository;
  }

  /**
   * Get all model parameters for a specific location
   *
   * @param locationId the location ID
   * @return list of model parameter DTOs
   * @throws IllegalArgumentException if locationId is null
   */
  @Transactional(readOnly = true)
  public List<ModelParamDTO> getModelParamsByLocation(Long locationId) {
    if (locationId == null) {
      throw new IllegalArgumentException("Location ID cannot be null");
    }

    log.debug("Fetching model parameters for location: {}", locationId);
    List<ModelParam> modelParams =
        modelParamRepository.findByLocation_IdOrderByCreatedAtDesc(locationId);

    return modelParams.stream().map(this::toDTO).collect(Collectors.toList());
  }

  /**
   * Get model parameters filtered by location and model type
   *
   * @param locationId the location ID
   * @param modelType the model type (Object Detection, Classification, Segmentation)
   * @return list of model parameter DTOs
   * @throws IllegalArgumentException if locationId or modelType is null
   */
  @Transactional(readOnly = true)
  public List<ModelParamDTO> getModelParamsByLocationAndType(Long locationId, String modelType) {
    if (locationId == null) {
      throw new IllegalArgumentException("Location ID cannot be null");
    }
    if (modelType == null || modelType.trim().isEmpty()) {
      throw new IllegalArgumentException("Model type cannot be null or empty");
    }

    log.debug("Fetching model parameters for location: {} and type: {}", locationId, modelType);
    List<ModelParam> modelParams =
        modelParamRepository.findByLocation_IdAndModelTypeOrderByCreatedAtDesc(
            locationId, modelType);

    return modelParams.stream().map(this::toDTO).collect(Collectors.toList());
  }

  /**
   * Search model parameters by name (case-insensitive) for a specific location
   *
   * @param locationId the location ID
   * @param modelName the model name to search for
   * @return list of model parameter DTOs
   * @throws IllegalArgumentException if locationId or modelName is null
   */
  @Transactional(readOnly = true)
  public List<ModelParamDTO> searchModelParamsByName(Long locationId, String modelName) {
    if (locationId == null) {
      throw new IllegalArgumentException("Location ID cannot be null");
    }
    if (modelName == null || modelName.trim().isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }

    log.debug("Searching model parameters for location: {} with name: {}", locationId, modelName);
    List<ModelParam> allMatches = modelParamRepository.searchByModelName(modelName);

    // Filter by location
    return allMatches.stream()
        .filter(mp -> mp.getLocation().getId().equals(locationId.intValue()))
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get a single model parameter by ID
   *
   * @param id the model parameter ID
   * @return model parameter DTO
   * @throws NotFoundException if model parameter not found
   * @throws IllegalArgumentException if id is null
   */
  @Transactional(readOnly = true)
  public ModelParamDTO getModelParamById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Model parameter ID cannot be null");
    }

    log.debug("Fetching model parameter with id: {}", id);
    ModelParam modelParam =
        modelParamRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Model parameter not found with id: " + id));

    return toDTO(modelParam);
  }

  /**
   * Create a new model parameter with automatic field setting
   *
   * @param request the create request containing model name, type, and parameters
   * @param locationId the location ID to associate with the model parameter
   * @param userId the user ID creating the model parameter
   * @return created model parameter DTO
   * @throws IllegalArgumentException if validation fails
   * @throws NotFoundException if location not found
   */
  @Transactional
  public ModelParamDTO createModelParam(
      ModelParamCreateRequest request, Long locationId, String userId) {
    if (request == null) {
      throw new IllegalArgumentException("Create request cannot be null");
    }
    if (locationId == null) {
      throw new IllegalArgumentException("Location ID cannot be null");
    }
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    log.debug("Creating model parameter for location: {} by user: {}", locationId, userId);

    // Fetch the location
    Location location =
        locationRepository
            .findById(locationId.intValue())
            .orElseThrow(() -> new NotFoundException("Location not found with id: " + locationId));

    // Create new model parameter entity
    ModelParam modelParam = new ModelParam();
    modelParam.setLocation(location);
    modelParam.setModelName(request.getModelName());
    modelParam.setModelType(request.getModelType());
    modelParam.setParameters(request.getParameters());
    modelParam.setCreatedBy(userId);
    // createdAt is automatically set by @CreationTimestamp

    ModelParam saved = modelParamRepository.save(modelParam);
    log.info("Created model parameter with id: {} for location: {}", saved.getId(), locationId);

    return toDTO(saved);
  }

  /**
   * Update an existing model parameter, preserving audit fields
   *
   * @param id the model parameter ID to update
   * @param request the update request containing new values
   * @param userId the user ID performing the update (for logging purposes)
   * @return updated model parameter DTO
   * @throws NotFoundException if model parameter not found
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ModelParamDTO updateModelParam(Long id, ModelParamUpdateRequest request, String userId) {
    if (id == null) {
      throw new IllegalArgumentException("Model parameter ID cannot be null");
    }
    if (request == null) {
      throw new IllegalArgumentException("Update request cannot be null");
    }
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    log.debug("Updating model parameter with id: {} by user: {}", id, userId);

    // Fetch existing model parameter
    ModelParam existingModelParam =
        modelParamRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Model parameter not found with id: " + id));

    // Update fields (preserving location, createdAt, and createdBy)
    existingModelParam.setModelName(request.getModelName());
    existingModelParam.setModelType(request.getModelType());
    existingModelParam.setParameters(request.getParameters());
    // Note: createdAt and createdBy are NOT updated (preserved)

    ModelParam updated = modelParamRepository.save(existingModelParam);
    log.info("Updated model parameter with id: {}", id);

    return toDTO(updated);
  }

  /**
   * Delete a model parameter
   *
   * @param id the model parameter ID to delete
   * @param userId the user ID performing the deletion (for logging purposes)
   * @throws NotFoundException if model parameter not found
   * @throws IllegalArgumentException if id is null
   */
  @Transactional
  public void deleteModelParam(Long id, String userId) {
    if (id == null) {
      throw new IllegalArgumentException("Model parameter ID cannot be null");
    }
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    log.debug("Deleting model parameter with id: {} by user: {}", id, userId);

    if (!modelParamRepository.existsById(id)) {
      throw new NotFoundException("Model parameter not found with id: " + id);
    }

    modelParamRepository.deleteById(id);
    log.info("Deleted model parameter with id: {}", id);
  }

  // -------------------- Private Helper Methods --------------------

  /**
   * Convert ModelParam entity to DTO
   *
   * @param modelParam the entity to convert
   * @return the DTO
   */
  private ModelParamDTO toDTO(ModelParam modelParam) {
    ModelParamDTO dto = new ModelParamDTO();
    dto.setId(modelParam.getId());
    dto.setLocationId(modelParam.getLocation().getId().longValue());
    dto.setLocationName(modelParam.getLocation().getAcronym());
    dto.setModelName(modelParam.getModelName());
    dto.setModelType(modelParam.getModelType());
    dto.setParameters(modelParam.getParameters());
    dto.setCreatedAt(modelParam.getCreatedAt());
    dto.setCreatedBy(modelParam.getCreatedBy());
    return dto;
  }
}
