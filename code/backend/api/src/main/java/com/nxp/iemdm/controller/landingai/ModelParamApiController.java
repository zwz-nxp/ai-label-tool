package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.ModelParamService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ModelParamCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ModelParamDTO;
import com.nxp.iemdm.shared.dto.landingai.ModelParamUpdateRequest;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller for Model Parameter Configuration operations. Provides RESTful endpoints for
 * frontend communication. Validates: Requirements 2.1, 2.3, 2.4, 3.2, 4.2, 5.2, 6.1
 */
@RestController
@RequestMapping("/api/landingai/model-params")
@Slf4j
public class ModelParamApiController {

  private final ModelParamService modelParamService;

  @Autowired
  public ModelParamApiController(ModelParamService modelParamService) {
    this.modelParamService = modelParamService;
  }

  /**
   * Get all model parameters for a specific location, optionally filtered by model type
   *
   * @param locationId the location ID (required)
   * @param modelType the model type filter (optional)
   * @return list of model parameter DTOs with HTTP 200 status
   */
  @MethodLog
  @GetMapping(produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ModelParamDTO>> getModelParams(
      @RequestParam("locationId") @NotNull Long locationId,
      @RequestParam(value = "modelType", required = false) String modelType) {
    try {
      log.info(
          "API: Getting model parameters for location: {}, modelType: {}", locationId, modelType);

      List<ModelParamDTO> modelParams;

      if (modelType != null && !modelType.trim().isEmpty()) {
        modelParams = modelParamService.getModelParamsByLocationAndType(locationId, modelType);
      } else {
        modelParams = modelParamService.getModelParamsByLocation(locationId);
      }

      return ResponseEntity.ok(modelParams);
    } catch (Exception e) {
      log.error("Error getting model parameters: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Search model parameters by name for a specific location
   *
   * @param locationId the location ID (required)
   * @param modelName the model name to search for (required)
   * @return list of model parameter DTOs with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ModelParamDTO>> searchModelParams(
      @RequestParam("locationId") @NotNull Long locationId,
      @RequestParam("modelName") @NotNull String modelName) {
    try {
      log.info("API: Searching model parameters for location: {}, name: {}", locationId, modelName);

      List<ModelParamDTO> modelParams =
          modelParamService.searchModelParamsByName(locationId, modelName);

      return ResponseEntity.ok(modelParams);
    } catch (Exception e) {
      log.error("Error searching model parameters: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get a single model parameter by ID
   *
   * @param id the model parameter ID
   * @return model parameter DTO with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ModelParamDTO> getModelParamById(@PathVariable("id") @NotNull Long id) {
    try {
      log.info("API: Getting model parameter by id: {}", id);

      ModelParamDTO modelParam = modelParamService.getModelParamById(id);

      return ResponseEntity.ok(modelParam);
    } catch (Exception e) {
      log.error("Error getting model parameter by id {}: {}", id, e.getMessage());
      throw e;
    }
  }

  /**
   * Create a new model parameter
   *
   * @param request the create request containing model name, type, and parameters
   * @param locationId the location ID to associate with the model parameter
   * @param user the authenticated user
   * @return created model parameter DTO with HTTP 201 status
   */
  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ModelParamDTO> createModelParam(
      @RequestBody @Valid ModelParamCreateRequest request,
      @RequestParam("locationId") @NotNull Long locationId,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      log.info(
          "API: Creating model parameter: name={}, type={}, location={}, user={}",
          request.getModelName(),
          request.getModelType(),
          locationId,
          user.getUsername());

      ModelParamDTO createdModelParam =
          modelParamService.createModelParam(request, locationId, user.getUsername());

      return ResponseEntity.status(HttpStatus.CREATED).body(createdModelParam);
    } catch (Exception e) {
      log.error("Error creating model parameter: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing model parameter
   *
   * @param id the model parameter ID to update
   * @param request the update request containing new values
   * @param user the authenticated user
   * @return updated model parameter DTO with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{id}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ModelParamDTO> updateModelParam(
      @PathVariable("id") @NotNull Long id,
      @RequestBody @Valid ModelParamUpdateRequest request,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      log.info(
          "API: Updating model parameter: id={}, name={}, type={}, user={}",
          id,
          request.getModelName(),
          request.getModelType(),
          user.getUsername());

      ModelParamDTO updatedModelParam =
          modelParamService.updateModelParam(id, request, user.getUsername());

      return ResponseEntity.ok(updatedModelParam);
    } catch (Exception e) {
      log.error("Error updating model parameter {}: {}", id, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a model parameter
   *
   * @param id the model parameter ID to delete
   * @param user the authenticated user
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{id}")
  public ResponseEntity<Void> deleteModelParam(
      @PathVariable("id") @NotNull Long id, @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      log.info("API: Deleting model parameter: id={}, user={}", id, user.getUsername());

      modelParamService.deleteModelParam(id, user.getUsername());

      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting model parameter {}: {}", id, e.getMessage());
      throw e;
    }
  }
}
