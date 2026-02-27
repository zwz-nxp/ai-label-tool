package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.model.landingai.ImagePredictionLabel;
import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.operational.service.landingai.ModelPredictionService;
import com.nxp.iemdm.shared.dto.landingai.ImageLabelDTO;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImagePredictionLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ModelRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for model prediction operations */
@RestController
@RequestMapping("/operational/landingai/predictions")
@Slf4j
public class ModelPredictionController {

  private final ModelPredictionService modelPredictionService;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final ModelRepository modelRepository;

  public ModelPredictionController(
      ModelPredictionService modelPredictionService,
      ImagePredictionLabelRepository imagePredictionLabelRepository,
      ModelRepository modelRepository) {
    this.modelPredictionService = modelPredictionService;
    this.imagePredictionLabelRepository = imagePredictionLabelRepository;
    this.modelRepository = modelRepository;
  }

  /**
   * Generate predictions for an image
   *
   * @param imageId the image ID
   * @param projectId the project ID
   * @param modelId the model ID
   * @return list of prediction labels
   */
  @PostMapping("/generate")
  public ResponseEntity<List<ImagePredictionLabel>> generatePredictions(
      @RequestParam Long imageId, @RequestParam Long projectId, @RequestParam Long modelId) {
    try {
      log.info(
          "Generating predictions for image {} in project {} with model {}",
          imageId,
          projectId,
          modelId);
      List<ImagePredictionLabel> predictions =
          modelPredictionService.generatePredictions(imageId, projectId, modelId);
      return ResponseEntity.ok(predictions);
    } catch (IllegalArgumentException e) {
      log.error("Invalid request: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error generating predictions: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get prediction labels for an image and model
   *
   * @param imageId the image ID
   * @param modelId the model ID
   * @return list of prediction labels as DTOs
   */
  @GetMapping("/image/{imageId}/model/{modelId}")
  public ResponseEntity<List<ImageLabelDTO>> getPredictionsByImageAndModel(
      @PathVariable Long imageId, @PathVariable Long modelId) {
    try {
      List<ImagePredictionLabel> predictions =
          imagePredictionLabelRepository.findByImage_IdAndModel_Id(imageId, modelId);

      List<ImageLabelDTO> dtos =
          predictions.stream()
              .map(
                  p ->
                      ImageLabelDTO.builder()
                          .id(p.getId())
                          .imageId(imageId)
                          .classId(p.getProjectClass() != null ? p.getProjectClass().getId() : null)
                          .position(p.getPosition())
                          .confidenceRate(p.getConfidenceRate())
                          .annotationType("Prediction")
                          .createdAt(p.getCreatedAt())
                          .createdBy(p.getCreatedBy())
                          .build())
              .collect(Collectors.toList());

      return ResponseEntity.ok(dtos);
    } catch (Exception e) {
      log.error(
          "Error getting predictions for image {} model {}: {}",
          imageId,
          modelId,
          e.getMessage(),
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get model info by ID
   *
   * @param modelId the model ID
   * @return the model entity
   */
  @GetMapping("/model/{modelId}")
  public ResponseEntity<Model> getModelById(@PathVariable Long modelId) {
    return modelRepository
        .findById(modelId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Check if model prediction is enabled
   *
   * @return status response
   */
  @GetMapping("/status")
  public ResponseEntity<ModelPredictionStatus> getStatus() {
    boolean enabled = modelPredictionService.isModelPredictionEnabled();
    return ResponseEntity.ok(new ModelPredictionStatus(enabled));
  }

  /** Status response class */
  public static class ModelPredictionStatus {
    private final boolean enabled;

    public ModelPredictionStatus(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isEnabled() {
      return enabled;
    }
  }
}
