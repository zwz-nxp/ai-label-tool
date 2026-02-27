package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.service.rest.landingai.ModelPredictionServiceREST;
import com.nxp.iemdm.shared.dto.landingai.ImageLabelDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** API Controller for model prediction operations */
@RestController
@RequestMapping("/api/landingai/predictions")
@Slf4j
public class ModelPredictionApiController {

  private final ModelPredictionServiceREST modelPredictionServiceREST;

  public ModelPredictionApiController(ModelPredictionServiceREST modelPredictionServiceREST) {
    this.modelPredictionServiceREST = modelPredictionServiceREST;
  }

  /**
   * Generate pre-annotations for an image
   *
   * @param imageId the image ID
   * @param projectId the project ID
   * @return list of pre-annotation labels
   */
  @PostMapping("/generate")
  public ResponseEntity<List<ImageLabel>> generatePreAnnotations(
      @RequestParam Long imageId, @RequestParam Long projectId) {
    try {
      log.info("API: Generating pre-annotations for image {} in project {}", imageId, projectId);
      List<ImageLabel> preAnnotations =
          modelPredictionServiceREST.generatePreAnnotations(imageId, projectId);
      return ResponseEntity.ok(preAnnotations);
    } catch (IllegalArgumentException e) {
      log.error("Invalid request: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error generating pre-annotations: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get prediction labels for an image and model
   *
   * @param imageId the image ID
   * @param modelId the model ID
   * @return list of prediction label DTOs
   */
  @GetMapping("/image/{imageId}/model/{modelId}")
  public ResponseEntity<List<ImageLabelDTO>> getPredictionsByImageAndModel(
      @PathVariable Long imageId, @PathVariable Long modelId) {
    try {
      List<ImageLabelDTO> predictions =
          modelPredictionServiceREST.getPredictionsByImageAndModel(imageId, modelId);
      return ResponseEntity.ok(predictions);
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
   * Check if model prediction is enabled
   *
   * @return status response
   */
  @GetMapping("/status")
  public ResponseEntity<ModelPredictionStatus> getStatus() {
    boolean enabled = modelPredictionServiceREST.isModelPredictionEnabled();
    return ResponseEntity.ok(new ModelPredictionStatus(enabled));
  }

  /** Status response class */
  public static class ModelPredictionStatus {
    private boolean enabled;

    public ModelPredictionStatus() {}

    public ModelPredictionStatus(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
