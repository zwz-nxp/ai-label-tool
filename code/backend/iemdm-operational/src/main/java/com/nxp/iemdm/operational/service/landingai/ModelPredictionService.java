package com.nxp.iemdm.operational.service.landingai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImagePredictionLabel;
import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageFileRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImagePredictionLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ModelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/** Service for calling ML models to generate predictions */
@Slf4j
@Service
public class ModelPredictionService {

  private final RestTemplate restTemplate;
  private final ImageRepository imageRepository;
  private final ProjectClassRepository projectClassRepository;
  private final ObjectMapper objectMapper;
  private final ImageFileRepository imageFileRepository;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final ModelRepository modelRepository;

  @Value("${model.prediction.url:http://localhost:5000/predict}")
  private String modelPredictionUrl;

  @Value("${model.prediction.enabled:false}")
  private boolean modelPredictionEnabled;

  public ModelPredictionService(
      RestTemplate restTemplate,
      ImageRepository imageRepository,
      ProjectClassRepository projectClassRepository,
      ImageFileRepository imageFileRepository,
      ImagePredictionLabelRepository imagePredictionLabelRepository,
      ModelRepository modelRepository) {
    this.restTemplate = restTemplate;
    this.imageRepository = imageRepository;
    this.projectClassRepository = projectClassRepository;
    this.imageFileRepository = imageFileRepository;
    this.imagePredictionLabelRepository = imagePredictionLabelRepository;
    this.modelRepository = modelRepository;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Generate predictions for an image using a ML model
   *
   * @param imageId the image ID
   * @param projectId the project ID
   * @param modelId the model ID
   * @return list of prediction labels
   */
  public List<ImagePredictionLabel> generatePredictions(
      Long imageId, Long projectId, Long modelId) {
    if (!modelPredictionEnabled) {
      log.warn("Model prediction is disabled. Enable it by setting model.prediction.enabled=true");
      return new ArrayList<>();
    }

    try {
      // Get image and project
      Image image =
          imageRepository
              .findById(imageId)
              .orElseThrow(
                  () -> new IllegalArgumentException("Image not found with id: " + imageId));

      Project project = image.getProject();
      if (project == null) {
        throw new IllegalArgumentException("Image is not associated with a project");
      }

      // Get model
      Model model =
          modelRepository
              .findById(modelId)
              .orElseThrow(
                  () -> new IllegalArgumentException("Model not found with id: " + modelId));

      // Get project classes
      List<ProjectClass> projectClasses =
          projectClassRepository.findByProject_IdOrderByCreatedAt(projectId);
      if (projectClasses.isEmpty()) {
        log.warn("No classes defined for project {}. Cannot generate predictions.", projectId);
        return new ArrayList<>();
      }

      // Call model prediction API
      String predictions = callModelPredictionAPI(image, project);

      // Parse predictions and create ImagePredictionLabel objects
      return parsePredictions(predictions, image, model, projectClasses);

    } catch (Exception e) {
      log.error("Error generating predictions for image {}: {}", imageId, e.getMessage(), e);
      throw new RuntimeException("Failed to generate predictions: " + e.getMessage(), e);
    }
  }

  /**
   * Call the model prediction API
   *
   * @param image the image to predict
   * @param project the project
   * @return JSON string with predictions
   */
  private String callModelPredictionAPI(Image image, Project project) {
    try {
      // Prepare request
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

      // Add image file
      File imageFile =
          new File("C:\\NXP\\c-Drive\\030-landingAI\\20220901_000028_TJMEA2V5PS00_6089_FH4061");

      body.add("image", new org.springframework.core.io.FileSystemResource(imageFile));

      // Add project type
      body.add("project_type", project.getType());

      // Add model name if specified
      if (project.getModelName() != null && !project.getModelName().isEmpty()) {
        body.add("model_name", project.getModelName());
      }

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      // Call API
      ResponseEntity<String> response =
          restTemplate.exchange(modelPredictionUrl, HttpMethod.POST, requestEntity, String.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return response.getBody();
      } else {
        throw new RuntimeException(
            "Model prediction API returned status: " + response.getStatusCode());
      }

    } catch (Exception e) {
      log.error("Error calling model prediction API: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to call model prediction API: " + e.getMessage(), e);
    }
  }

  /**
   * Parse prediction results and create ImagePredictionLabel objects
   *
   * @param predictionsJson JSON string with predictions
   * @param image the image
   * @param model the model that generated predictions
   * @param projectClasses available project classes
   * @return list of ImagePredictionLabel objects
   */
  private List<ImagePredictionLabel> parsePredictions(
      String predictionsJson, Image image, Model model, List<ProjectClass> projectClasses) {

    List<ImagePredictionLabel> predictions = new ArrayList<>();

    try {
      JsonNode root = objectMapper.readTree(predictionsJson);
      JsonNode predictionsNode = root.get("predictions");

      if (predictionsNode == null || !predictionsNode.isArray()) {
        log.warn("No predictions found in response");
        return predictions;
      }

      for (JsonNode prediction : predictionsNode) {
        try {
          ImagePredictionLabel label = new ImagePredictionLabel();
          label.setImage(image);
          label.setModel(model);

          // Get class name from prediction
          String className = prediction.get("class").asText();

          // Find matching project class
          ProjectClass matchingClass =
              projectClasses.stream()
                  .filter(pc -> pc.getClassName().equalsIgnoreCase(className))
                  .findFirst()
                  .orElse(projectClasses.get(0)); // Default to first class if not found

          label.setProjectClass(matchingClass);

          // Get confidence rate
          if (prediction.has("confidence")) {
            int confidence = (int) (prediction.get("confidence").asDouble() * 100);
            label.setConfidenceRate(confidence);
          }

          // Parse position based on annotation type
          JsonNode bbox = prediction.get("bbox");
          if (bbox != null && bbox.isObject()) {
            // Bounding box format
            String position = objectMapper.writeValueAsString(bbox);
            label.setPosition(position);
          } else if (prediction.has("polygon")) {
            // Polygon format
            JsonNode polygon = prediction.get("polygon");
            String position = objectMapper.writeValueAsString(polygon);
            label.setPosition(position);
          } else if (prediction.has("mask")) {
            // Segmentation mask format
            JsonNode mask = prediction.get("mask");
            String position = objectMapper.writeValueAsString(mask);
            label.setPosition(position);
          }

          predictions.add(label);

        } catch (Exception e) {
          log.error("Error parsing individual prediction: {}", e.getMessage(), e);
          // Continue with next prediction
        }
      }

    } catch (Exception e) {
      log.error("Error parsing predictions JSON: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to parse predictions: " + e.getMessage(), e);
    }

    return predictions;
  }

  /**
   * Check if model prediction is enabled
   *
   * @return true if enabled
   */
  public boolean isModelPredictionEnabled() {
    return modelPredictionEnabled;
  }
}
