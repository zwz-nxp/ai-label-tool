package com.nxp.iemdm.operational.service.landingai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ImageLabelService {

  private final ImageLabelRepository imageLabelRepository;
  private final ProjectClassRepository projectClassRepository;
  private final ProjectRepository projectRepository;
  private final ObjectMapper objectMapper;

  public ImageLabelService(
      ImageLabelRepository imageLabelRepository,
      ProjectClassRepository projectClassRepository,
      ProjectRepository projectRepository) {
    this.imageLabelRepository = imageLabelRepository;
    this.projectClassRepository = projectClassRepository;
    this.projectRepository = projectRepository;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Save a single label with validation
   *
   * @param label the label to save
   * @return the saved label
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ImageLabel saveLabel(ImageLabel label) {
    validateLabel(label);

    // For Classification type: ensure only one label per image
    if (isClassificationProject(label)) {
      // Delete existing labels for this image
      imageLabelRepository.deleteByImage_Id(label.getImage().getId());
      // Ensure position is null for classification
      label.setPosition(null);
    }

    return imageLabelRepository.save(label);
  }

  /**
   * Save multiple labels in batch
   *
   * @param labels the list of labels to save
   * @return the list of saved labels
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public List<ImageLabel> saveBatch(List<ImageLabel> labels) {
    // Validate all labels first
    for (ImageLabel label : labels) {
      validateLabel(label);
    }

    return imageLabelRepository.saveAll(labels);
  }

  /**
   * Get all labels for a specific image
   *
   * @param imageId the image ID
   * @return list of labels
   */
  @Transactional(readOnly = true)
  public List<ImageLabel> getLabelsByImageId(Long imageId) {
    return imageLabelRepository.findByImage_Id(imageId);
  }

  /**
   * Delete a specific label
   *
   * @param labelId the label ID to delete
   * @throws NotFoundException if label not found
   */
  @Transactional
  public void deleteLabel(Long labelId) {
    if (!imageLabelRepository.existsById(labelId)) {
      throw new NotFoundException("Label not found with id: " + labelId);
    }
    imageLabelRepository.deleteById(labelId);
  }

  /**
   * Delete all labels for a specific image
   *
   * @param imageId the image ID
   */
  @Transactional
  public void deleteLabelsByImageId(Long imageId) {
    imageLabelRepository.deleteByImage_Id(imageId);
  }

  /**
   * Update an existing label
   *
   * @param labelId the label ID to update
   * @param updatedLabel the updated label data
   * @return the updated label
   * @throws NotFoundException if label not found
   */
  @Transactional
  public ImageLabel updateLabel(Long labelId, ImageLabel updatedLabel) {
    ImageLabel existingLabel =
        imageLabelRepository
            .findById(labelId)
            .orElseThrow(() -> new NotFoundException("Label not found with id: " + labelId));

    // Validate the updated label
    validateLabel(updatedLabel);

    // Update fields (ground truth labels don't have confidenceRate or annotationType)
    if (updatedLabel.getProjectClass() != null) {
      existingLabel.setProjectClass(updatedLabel.getProjectClass());
    }
    if (updatedLabel.getPosition() != null) {
      existingLabel.setPosition(updatedLabel.getPosition());
    }

    return imageLabelRepository.save(existingLabel);
  }

  // -------------------- Private Helper Methods --------------------

  /**
   * Validate label data
   *
   * @param label the label to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateLabel(ImageLabel label) {
    if (label == null) {
      throw new IllegalArgumentException("Label cannot be null");
    }

    if (label.getImage() == null) {
      throw new IllegalArgumentException("Label must be associated with an image");
    }

    if (label.getProjectClass() == null) {
      throw new IllegalArgumentException("Label must be associated with a class");
    }

    // Verify that the class exists
    if (!projectClassRepository.existsById(label.getProjectClass().getId())) {
      throw new IllegalArgumentException(
          "Project class not found with id: " + label.getProjectClass().getId());
    }

    // Validate position JSON format if not null
    if (label.getPosition() != null && !label.getPosition().trim().isEmpty()) {
      validatePositionJson(label.getPosition());
    }
  }

  /**
   * Validate position JSON format
   *
   * @param position the JSON string to validate
   * @throws IllegalArgumentException if JSON is invalid
   */
  private void validatePositionJson(String position) {
    try {
      JsonNode jsonNode = objectMapper.readTree(position);

      // Check if it's a valid JSON object
      if (!jsonNode.isObject()) {
        throw new IllegalArgumentException("Position must be a valid JSON object");
      }

      // Validate that coordinates are non-negative if present
      if (jsonNode.has("x") && jsonNode.get("x").asInt() < 0) {
        throw new IllegalArgumentException("Position x coordinate must be non-negative");
      }
      if (jsonNode.has("y") && jsonNode.get("y").asInt() < 0) {
        throw new IllegalArgumentException("Position y coordinate must be non-negative");
      }
      if (jsonNode.has("width") && jsonNode.get("width").asInt() < 0) {
        throw new IllegalArgumentException("Position width must be non-negative");
      }
      if (jsonNode.has("height") && jsonNode.get("height").asInt() < 0) {
        throw new IllegalArgumentException("Position height must be non-negative");
      }

    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid position JSON format: " + e.getMessage(), e);
    }
  }

  /**
   * Check if the label belongs to a Classification project
   *
   * @param label the label to check
   * @return true if it's a classification project
   */
  private boolean isClassificationProject(ImageLabel label) {
    if (label.getImage() == null || label.getImage().getProject() == null) {
      return false;
    }

    Long projectId = label.getImage().getProject().getId();
    Project project = projectRepository.findById(projectId).orElse(null);

    return project != null && "Classification".equalsIgnoreCase(project.getType());
  }
}
