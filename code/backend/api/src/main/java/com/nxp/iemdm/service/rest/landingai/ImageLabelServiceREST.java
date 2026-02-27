package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ImageLabelDTO;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * REST Service for Image Label operations in the API Layer. Uses RestTemplate to call Operational
 * Layer endpoints.
 *
 * <p>Requirements: 23.1, 23.5
 */
@Service
@Slf4j
public class ImageLabelServiceREST {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ImageLabelServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  /**
   * Create a new label
   *
   * @param labelDTO the label DTO to create
   * @return the created label DTO
   */
  @MethodLog
  public ImageLabelDTO saveLabel(ImageLabelDTO labelDTO) {
    try {
      ResponseEntity<ImageLabelDTO> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/labels",
              labelDTO,
              ImageLabelDTO.class);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to save label: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Create multiple labels in batch
   *
   * @param labelDTOs the list of label DTOs to create
   * @return the list of created label DTOs
   */
  @MethodLog
  public List<ImageLabelDTO> saveBatch(List<ImageLabelDTO> labelDTOs) {
    try {
      ResponseEntity<ImageLabelDTO[]> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/labels/batch",
              labelDTOs,
              ImageLabelDTO[].class);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to save labels batch: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get all labels for a specific image
   *
   * @param imageId the image ID
   * @return list of label DTOs
   */
  @MethodLog
  public List<ImageLabelDTO> getLabelsByImageId(Long imageId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      ResponseEntity<ImageLabelDTO[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/labels/image/{imageId}",
              ImageLabelDTO[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get labels for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
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
  public ImageLabelDTO updateLabel(Long labelId, ImageLabelDTO labelDTO) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("labelId", labelId);

      ResponseEntity<ImageLabelDTO> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/labels/{labelId}",
              HttpMethod.PUT,
              new org.springframework.http.HttpEntity<>(labelDTO),
              ImageLabelDTO.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to update label {}: {}", labelId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a specific label
   *
   * @param labelId the label ID to delete
   */
  @MethodLog
  public void deleteLabel(Long labelId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("labelId", labelId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/labels/{labelId}", params);
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to delete label {}: {}", labelId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete all labels for a specific image
   *
   * @param imageId the image ID
   */
  @MethodLog
  public void deleteLabelsByImageId(Long imageId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/labels/image/{imageId}", params);
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to delete labels for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }
}
