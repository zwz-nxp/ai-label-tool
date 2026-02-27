package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.rest.landingai.ImageLabelServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ImageLabelDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller for Image Label operations. Provides RESTful endpoints for frontend communication.
 *
 * <p>Requirements: 24.1, 24.4, 24.5, 24.6
 */
@RestController
@RequestMapping("/api/landingai/labels")
@Slf4j
public class ImageLabelApiController {

  private final ImageLabelServiceREST imageLabelServiceREST;

  @Autowired
  public ImageLabelApiController(ImageLabelServiceREST imageLabelServiceREST) {
    this.imageLabelServiceREST = imageLabelServiceREST;
  }

  /**
   * Create a new label
   *
   * @param labelDTO the label DTO to create
   * @return the created label DTO with HTTP 201 status
   */
  @MethodLog
  @PostMapping(
      path = "/create",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageLabelDTO> createLabel(@RequestBody @Valid ImageLabelDTO labelDTO) {
    try {
      ImageLabelDTO createdLabel = imageLabelServiceREST.saveLabel(labelDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdLabel);
    } catch (Exception e) {
      log.error("Error creating label: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Create multiple labels in batch
   *
   * @param labelDTOs the list of label DTOs to create
   * @return the list of created label DTOs with HTTP 201 status
   */
  @MethodLog
  @PostMapping(
      path = "/batch",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageLabelDTO>> createLabelsBatch(
      @RequestBody @Valid List<ImageLabelDTO> labelDTOs) {
    try {
      if (labelDTOs == null || labelDTOs.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      List<ImageLabelDTO> createdLabels = imageLabelServiceREST.saveBatch(labelDTOs);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdLabels);
    } catch (Exception e) {
      log.error("Error creating labels batch: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get all labels for a specific image
   *
   * @param imageId the image ID
   * @return list of label DTOs with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/image/{imageId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageLabelDTO>> getLabelsByImageId(
      @PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<ImageLabelDTO> labels = imageLabelServiceREST.getLabelsByImageId(imageId);
      return ResponseEntity.ok(labels);
    } catch (Exception e) {
      log.error("Error getting labels for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing label
   *
   * @param labelId the label ID to update
   * @param labelDTO the updated label DTO data
   * @return the updated label DTO with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{labelId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageLabelDTO> updateLabel(
      @PathVariable("labelId") Long labelId, @RequestBody @Valid ImageLabelDTO labelDTO) {
    try {
      if (labelId == null || labelId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      ImageLabelDTO updatedLabel = imageLabelServiceREST.updateLabel(labelId, labelDTO);
      return ResponseEntity.ok(updatedLabel);
    } catch (Exception e) {
      log.error("Error updating label {}: {}", labelId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a specific label
   *
   * @param labelId the label ID to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{labelId}")
  public ResponseEntity<Void> deleteLabel(@PathVariable("labelId") Long labelId) {
    try {
      if (labelId == null || labelId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      imageLabelServiceREST.deleteLabel(labelId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting label {}: {}", labelId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete all labels for a specific image
   *
   * @param imageId the image ID
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/image/{imageId}")
  public ResponseEntity<Void> deleteLabelsByImageId(@PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      imageLabelServiceREST.deleteLabelsByImageId(imageId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting labels for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }
}
