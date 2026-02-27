package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.operational.service.landingai.ImageLabelService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ImageLabelDTO;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Image Label operations in the Operational Layer. Provides internal endpoints
 * for label CRUD operations.
 *
 * <p>Requirements: 25.1, 25.4, 25.5
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/labels")
public class ImageLabelController {

  private final ImageLabelService imageLabelService;
  private final ImageRepository imageRepository;
  private final ProjectClassRepository projectClassRepository;

  public ImageLabelController(
      ImageLabelService imageLabelService,
      ImageRepository imageRepository,
      ProjectClassRepository projectClassRepository) {
    this.imageLabelService = imageLabelService;
    this.imageRepository = imageRepository;
    this.projectClassRepository = projectClassRepository;
  }

  /**
   * Create a new label
   *
   * @param labelDTO the label DTO to create
   * @return the created label DTO
   */
  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageLabelDTO> createLabel(@RequestBody ImageLabelDTO labelDTO) {
    try {
      // Convert DTO to Entity
      ImageLabel label = convertToEntity(labelDTO);

      // Save the label
      ImageLabel savedLabel = imageLabelService.saveLabel(label);

      // Convert back to DTO
      ImageLabelDTO responseDTO = convertToDTO(savedLabel);

      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    } catch (IllegalArgumentException e) {
      log.error("Validation error creating label: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating label", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create multiple labels in batch
   *
   * @param labelDTOs the list of label DTOs to create
   * @return the list of created label DTOs
   */
  @MethodLog
  @PostMapping(
      path = "/batch",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageLabelDTO>> createLabelsBatch(
      @RequestBody List<ImageLabelDTO> labelDTOs) {
    try {
      // Convert DTOs to Entities
      List<ImageLabel> labels =
          labelDTOs.stream().map(this::convertToEntity).collect(Collectors.toList());

      // Save the labels
      List<ImageLabel> savedLabels = imageLabelService.saveBatch(labels);

      // Convert back to DTOs
      List<ImageLabelDTO> responseDTOs =
          savedLabels.stream().map(this::convertToDTO).collect(Collectors.toList());

      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTOs);
    } catch (IllegalArgumentException e) {
      log.error("Validation error creating labels batch: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating labels batch", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get all labels for a specific image
   *
   * @param imageId the image ID
   * @return list of label DTOs
   */
  @MethodLog
  @GetMapping(path = "/image/{imageId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageLabelDTO>> getLabelsByImageId(
      @PathVariable("imageId") Long imageId) {
    try {
      List<ImageLabel> labels = imageLabelService.getLabelsByImageId(imageId);

      // Convert to DTOs
      List<ImageLabelDTO> labelDTOs =
          labels.stream().map(this::convertToDTO).collect(Collectors.toList());

      return ResponseEntity.ok(labelDTOs);
    } catch (Exception e) {
      log.error("Error retrieving labels for image {}", imageId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update an existing label
   *
   * @param labelId the label ID to update
   * @param labelDTO the updated label DTO data
   * @return the updated label DTO
   */
  @MethodLog
  @PutMapping(
      path = "/{labelId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageLabelDTO> updateLabel(
      @PathVariable("labelId") Long labelId, @RequestBody ImageLabelDTO labelDTO) {
    try {
      // Convert DTO to Entity
      ImageLabel label = convertToEntity(labelDTO);

      // Update the label
      ImageLabel updatedLabel = imageLabelService.updateLabel(labelId, label);

      // Convert back to DTO
      ImageLabelDTO responseDTO = convertToDTO(updatedLabel);

      return ResponseEntity.ok(responseDTO);
    } catch (NotFoundException e) {
      log.error("Label not found: {}", labelId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error updating label: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error updating label {}", labelId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a specific label
   *
   * @param labelId the label ID to delete
   * @return no content on success
   */
  @MethodLog
  @DeleteMapping(path = "/{labelId}")
  public ResponseEntity<Void> deleteLabel(@PathVariable("labelId") Long labelId) {
    try {
      imageLabelService.deleteLabel(labelId);
      return ResponseEntity.noContent().build();
    } catch (NotFoundException e) {
      log.error("Label not found: {}", labelId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error deleting label {}", labelId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete all labels for a specific image
   *
   * @param imageId the image ID
   * @return no content on success
   */
  @MethodLog
  @DeleteMapping(path = "/image/{imageId}")
  public ResponseEntity<Void> deleteLabelsByImageId(@PathVariable("imageId") Long imageId) {
    try {
      imageLabelService.deleteLabelsByImageId(imageId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting labels for image {}", imageId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // -------------------- Helper Methods --------------------

  /**
   * Convert ImageLabelDTO to ImageLabel entity
   *
   * @param dto the DTO to convert
   * @return the entity
   */
  private ImageLabel convertToEntity(ImageLabelDTO dto) {
    ImageLabel label = new ImageLabel();

    if (dto.getId() != null) {
      label.setId(dto.getId());
    }

    // Load Image entity
    Image image =
        imageRepository
            .findById(dto.getImageId())
            .orElseThrow(
                () -> new IllegalArgumentException("Image not found with id: " + dto.getImageId()));
    label.setImage(image);

    // Load ProjectClass entity
    ProjectClass projectClass =
        projectClassRepository
            .findById(dto.getClassId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Project class not found with id: " + dto.getClassId()));
    label.setProjectClass(projectClass);

    label.setPosition(dto.getPosition());
    // Note: confidenceRate and annotationType are not set as ImageLabel no longer has these fields
    // Ground truth labels don't have confidence rate or annotation type
    label.setCreatedBy(dto.getCreatedBy());

    return label;
  }

  /**
   * Convert ImageLabel entity to ImageLabelDTO
   *
   * @param label the entity to convert
   * @return the DTO
   */
  private ImageLabelDTO convertToDTO(ImageLabel label) {
    return ImageLabelDTO.builder()
        .id(label.getId())
        .imageId(label.getImage().getId())
        .classId(label.getProjectClass().getId())
        .position(label.getPosition())
        .confidenceRate(null) // Ground truth labels don't have confidence rate
        .annotationType("Ground Truth") // All ImageLabel records are ground truth
        .createdAt(label.getCreatedAt())
        .createdBy(label.getCreatedBy())
        .build();
  }
}
