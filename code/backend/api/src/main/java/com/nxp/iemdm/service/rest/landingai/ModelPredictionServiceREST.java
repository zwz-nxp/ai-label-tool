package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.shared.dto.landingai.ImageLabelDTO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** REST service for calling Operational Layer model prediction endpoints */
@Service
@Slf4j
public class ModelPredictionServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ModelPredictionServiceREST(
      RestTemplate restTemplate, @Value("${rest.operational.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  /**
   * Generate pre-annotations for an image
   *
   * @param imageId the image ID
   * @param projectId the project ID
   * @return list of pre-annotation labels
   */
  public List<ImageLabel> generatePreAnnotations(Long imageId, Long projectId) {
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(
                  operationalServiceURI + "/operational/landingai/predictions/generate")
              .queryParam("imageId", imageId)
              .queryParam("projectId", projectId)
              .toUriString();

      ResponseEntity<List<ImageLabel>> response =
          restTemplate.exchange(
              url, HttpMethod.POST, null, new ParameterizedTypeReference<List<ImageLabel>>() {});

      return response.getBody();
    } catch (Exception e) {
      log.error("Error calling operational layer for pre-annotations: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate pre-annotations: " + e.getMessage(), e);
    }
  }

  /**
   * Get prediction labels for an image and model
   *
   * @param imageId the image ID
   * @param modelId the model ID
   * @return list of prediction label DTOs
   */
  public List<ImageLabelDTO> getPredictionsByImageAndModel(Long imageId, Long modelId) {
    try {
      String url =
          operationalServiceURI
              + "/operational/landingai/predictions/image/"
              + imageId
              + "/model/"
              + modelId;

      ResponseEntity<ImageLabelDTO[]> response =
          restTemplate.getForEntity(url, ImageLabelDTO[].class);

      return response.getBody() != null
          ? Arrays.asList(response.getBody())
          : Collections.emptyList();
    } catch (Exception e) {
      log.error(
          "Error getting predictions for image {} model {}: {}",
          imageId,
          modelId,
          e.getMessage(),
          e);
      return Collections.emptyList();
    }
  }

  /**
   * Get model info by ID
   *
   * @param modelId the model ID
   * @return the Model entity or null
   */
  public Model getModelByIdFromOperational(Long modelId) {
    try {
      String url = operationalServiceURI + "/operational/landingai/predictions/model/" + modelId;

      ResponseEntity<Model> response = restTemplate.getForEntity(url, Model.class);
      return response.getBody();
    } catch (Exception e) {
      log.error("Error getting model {}: {}", modelId, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Check if model prediction is enabled
   *
   * @return true if enabled
   */
  public boolean isModelPredictionEnabled() {
    try {
      String url = operationalServiceURI + "/operational/landingai/predictions/status";
      ResponseEntity<ModelPredictionStatus> response =
          restTemplate.getForEntity(url, ModelPredictionStatus.class);

      return response.getBody() != null && response.getBody().isEnabled();
    } catch (Exception e) {
      log.error("Error checking model prediction status: {}", e.getMessage(), e);
      return false;
    }
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
