package com.nxp.iemdm.service.rest.landingai;

import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageMetadata;
import com.nxp.iemdm.model.landingai.ImageTag;
import com.nxp.iemdm.service.ImageService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest;
import com.nxp.iemdm.shared.dto.landingai.ImageListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ImageUploadResponse;
import com.nxp.iemdm.shared.dto.landingai.PaginatedResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Unified REST implementation of ImageService that calls the operational service layer. Combines
 * functionality from both ImageServiceREST and ImageApiServiceREST.
 */
@Slf4j
@Service
public class ImageServiceREST implements ImageService {

  private final RestTemplate restTemplate;
  private final String operationalServiceURI;

  @Autowired
  public ImageServiceREST(
      RestTemplate restTemplate,
      @Value("${rest.iemdm-services.uri}") String operationalServiceURI) {
    this.restTemplate = restTemplate;
    this.operationalServiceURI = operationalServiceURI;
  }

  @Override
  public List<ImageUploadResponse> uploadImages(
      List<MultipartFile> files, Long projectId, String userId) {

    log.info(
        "REST Service: Uploading {} images to project: {} by user: {}",
        files.size(),
        projectId,
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI + "/operational/landingai/images/upload")
            .queryParam("projectId", projectId)
            .queryParam("userId", userId)
            .toUriString();

    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

      for (MultipartFile file : files) {
        ByteArrayResource resource =
            new ByteArrayResource(file.getBytes()) {
              @Override
              public String getFilename() {
                return file.getOriginalFilename();
              }
            };
        body.add("files", resource);
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<ImageUploadResponse[]> responseEntity =
          restTemplate.postForEntity(url, requestEntity, ImageUploadResponse[].class);

      return Arrays.asList(responseEntity.getBody());
    } catch (Exception e) {
      log.error("Error uploading images: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload images", e);
    }
  }

  @Override
  public Map<String, Object> uploadClassifiedImagesZip(
      MultipartFile file, Long projectId, String userId) {

    log.info(
        "REST Service: Uploading classified images ZIP to project: {} by user: {}",
        projectId,
        userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI + "/operational/landingai/images/upload-classified")
            .queryParam("projectId", projectId)
            .queryParam("userId", userId)
            .toUriString();

    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

      ByteArrayResource resource =
          new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
              return file.getOriginalFilename();
            }
          };
      body.add("file", resource);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<Map> responseEntity =
          restTemplate.postForEntity(url, requestEntity, Map.class);

      return responseEntity.getBody();
    } catch (Exception e) {
      log.error("Error uploading classified images ZIP: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload classified images ZIP", e);
    }
  }

  @Override
  public Map<String, Object> uploadBatchImagesZip(
      MultipartFile file, Long projectId, String userId) {

    log.info(
        "REST Service: Uploading batch images ZIP to project: {} by user: {}", projectId, userId);

    String url =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI + "/operational/landingai/images/upload-batch")
            .queryParam("projectId", projectId)
            .queryParam("userId", userId)
            .toUriString();

    try {
      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

      ByteArrayResource resource =
          new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
              return file.getOriginalFilename();
            }
          };
      body.add("file", resource);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

      ResponseEntity<Map> responseEntity =
          restTemplate.postForEntity(url, requestEntity, Map.class);

      return responseEntity.getBody();
    } catch (Exception e) {
      log.error("Error uploading batch images ZIP: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload batch images ZIP", e);
    }
  }

  @Override
  public byte[] getThumbnail(Long id) {

    log.info("REST Service: Getting thumbnail for image: {}", id);

    String url = operationalServiceURI + "/operational/landingai/images/" + id + "/thumbnail";

    ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);

    return responseEntity.getBody();
  }

  @Override
  public byte[] getImageFileById(Long id) {

    log.info("REST Service: Getting full image file for image: {}", id);

    String url = operationalServiceURI + "/operational/landingai/images/" + id + "/file";

    ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(url, byte[].class);

    return responseEntity.getBody();
  }

  @Override
  public PaginatedResponse<ImageListItemDTO> getImagesForProject(
      Long projectId,
      int page,
      int size,
      String viewMode,
      String sortBy,
      ImageFilterRequest filters,
      boolean includeThumbnails) {

    log.info(
        "REST Service: Getting images for project: {}, page: {}, size: {}, viewMode: {}, sortBy: {}, filters: {}",
        projectId,
        page,
        size,
        viewMode,
        sortBy,
        filters);

    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(
                operationalServiceURI + "/operational/landingai/images/project/" + projectId)
            .queryParam("page", page)
            .queryParam("size", size)
            .queryParam("viewMode", viewMode)
            .queryParam("sortBy", sortBy)
            .queryParam("includeThumbnails", includeThumbnails);

    // Add filter parameters if provided
    if (filters != null) {
      if (filters.getMediaStatus() != null && !filters.getMediaStatus().isEmpty()) {
        builder.queryParam("mediaStatus", String.join(",", filters.getMediaStatus()));
      }
      if (filters.getGroundTruthLabels() != null && !filters.getGroundTruthLabels().isEmpty()) {
        builder.queryParam(
            "groundTruthLabels",
            filters.getGroundTruthLabels().stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
      }
      if (filters.getPredictionLabels() != null && !filters.getPredictionLabels().isEmpty()) {
        builder.queryParam(
            "predictionLabels",
            filters.getPredictionLabels().stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
      }
      if (filters.getAnnotationType() != null && !filters.getAnnotationType().isEmpty()) {
        builder.queryParam("annotationType", filters.getAnnotationType());
      }
      if (filters.getModelId() != null) {
        builder.queryParam("modelId", filters.getModelId());
      }
      if (filters.getSplit() != null && !filters.getSplit().isEmpty()) {
        builder.queryParam("split", String.join(",", filters.getSplit()));
      }
      if (filters.getTags() != null && !filters.getTags().isEmpty()) {
        builder.queryParam(
            "tags",
            filters.getTags().stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
      }
      if (filters.getMediaName() != null && !filters.getMediaName().isEmpty()) {
        builder.queryParam("mediaName", filters.getMediaName());
      }
      if (filters.getLabeler() != null && !filters.getLabeler().isEmpty()) {
        builder.queryParam("labeler", filters.getLabeler());
      }
      if (filters.getMediaId() != null && !filters.getMediaId().isEmpty()) {
        builder.queryParam("mediaId", filters.getMediaId());
      }
      if (filters.getNoClass() != null && filters.getNoClass()) {
        builder.queryParam("noClass", true);
      }
      if (filters.getPredictionNoClass() != null && filters.getPredictionNoClass()) {
        builder.queryParam("predictionNoClass", true);
      }
      if (filters.getMetadata() != null && !filters.getMetadata().isEmpty()) {
        for (Map.Entry<String, String> entry : filters.getMetadata().entrySet()) {
          builder.queryParam("metadata." + entry.getKey(), entry.getValue());
        }
      }
    }

    java.net.URI uri = builder.build().encode().toUri();

    log.info("Calling operational layer URI: {}", uri);

    ResponseEntity<PaginatedResponse<ImageListItemDTO>> responseEntity =
        restTemplate.exchange(
            uri,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PaginatedResponse<ImageListItemDTO>>() {});

    return responseEntity.getBody();
  }

  /**
   * Batch set metadata for multiple images by calling the operational layer.
   *
   * @param imageIds list of image IDs to update
   * @param metadata map of metadata key-value pairs
   * @param userId the user performing the operation
   */
  @MethodLog
  public void batchSetMetadata(List<Long> imageIds, Map<String, String> metadata, String userId) {
    log.info("Batch setting metadata for {} images by user: {}", imageIds.size(), userId);

    String url = operationalServiceURI + "/operational/landingai/images/batch-set-metadata";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("imageIds", imageIds);
    requestBody.put("metadata", metadata);
    requestBody.put("userId", userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

    log.info("Calling operational layer URL: {}", url);

    restTemplate.exchange(
        url,
        HttpMethod.POST,
        requestEntity,
        new ParameterizedTypeReference<Map<String, Object>>() {});

    log.info("Successfully batch set metadata for {} images", imageIds.size());
  }

  /**
   * Batch set tags for multiple images by calling the operational layer.
   *
   * @param imageIds list of image IDs to update
   * @param tagIds list of tag IDs to assign
   * @param userId the user performing the operation
   */
  @MethodLog
  public void batchSetTags(List<Long> imageIds, List<Long> tagIds, String userId) {
    log.info("Batch setting tags for {} images by user: {}", imageIds.size(), userId);

    String url = operationalServiceURI + "/operational/landingai/images/batch-set-tags";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("imageIds", imageIds);
    requestBody.put("tagIds", tagIds);
    requestBody.put("userId", userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

    log.info("Calling operational layer URL: {}", url);

    restTemplate.exchange(
        url,
        HttpMethod.POST,
        requestEntity,
        new ParameterizedTypeReference<Map<String, Object>>() {});

    log.info("Successfully batch set tags for {} images", imageIds.size());
  }

  /**
   * Batch set class for multiple images by calling the operational layer.
   *
   * @param imageIds list of image IDs to update
   * @param classId the class ID to assign
   * @param userId the user performing the operation
   */
  @MethodLog
  public void batchSetClass(List<Long> imageIds, Long classId, String userId) {
    log.info("Batch setting class for {} images by user: {}", imageIds.size(), userId);

    String url = operationalServiceURI + "/operational/landingai/images/batch-set-class";

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("imageIds", imageIds);
    requestBody.put("classId", classId);
    requestBody.put("userId", userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

    log.info("Calling operational layer URL: {}", url);

    restTemplate.exchange(
        url,
        HttpMethod.POST,
        requestEntity,
        new ParameterizedTypeReference<Map<String, Object>>() {});

    log.info("Successfully batch set class for {} images", imageIds.size());
  }

  // ==================== CRUD Operations (from ImageApiServiceREST) ====================

  /**
   * Create a new image record
   *
   * @param image the image to create
   * @return the created image
   */
  @MethodLog
  public Image saveImage(Image image) {
    try {
      ResponseEntity<Image> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/images", image, Image.class);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to save image: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get an image by ID
   *
   * @param imageId the image ID
   * @return the image
   */
  @MethodLog
  public Image getImageById(Long imageId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      ResponseEntity<Image> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/images/{imageId}",
              Image.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to get image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Get all images for a specific project. This method fetches all pages from the paginated
   * endpoint and returns a complete list. Note: This converts ImageListItemDTO to Image, but some
   * fields may not be available in the DTO.
   *
   * @param projectId the project ID
   * @return list of images
   */
  @MethodLog
  public List<Image> getImagesByProjectId(Long projectId) {
    try {
      List<Image> allImages = new java.util.ArrayList<>();
      int page = 0;
      int size = 1000; // Large page size to minimize requests
      boolean hasMore = true;

      while (hasMore) {
        String url =
            UriComponentsBuilder.fromHttpUrl(
                    operationalServiceURI + "/operational/landingai/images/project/{projectId}")
                .queryParam("page", page)
                .queryParam("size", size)
                .buildAndExpand(projectId)
                .toUriString();

        ResponseEntity<PaginatedResponse<ImageListItemDTO>> response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponse<ImageListItemDTO>>() {});

        PaginatedResponse<ImageListItemDTO> paginatedResponse = response.getBody();
        if (paginatedResponse != null && paginatedResponse.getContent() != null) {
          // Convert ImageListItemDTO to Image
          for (ImageListItemDTO dto : paginatedResponse.getContent()) {
            Image image = new Image();
            image.setId(dto.getId());
            // Note: Project object is not set here, only basic fields from DTO
            // fileId is not available in ImageListItemDTO
            image.setFileName(dto.getFileName());
            image.setFileSize(dto.getFileSize());
            image.setWidth(dto.getWidth());
            image.setHeight(dto.getHeight());
            image.setIsLabeled(dto.getIsLabeled());
            image.setIsNoClass(dto.getIsNoClass());
            image.setSplit(dto.getSplit());
            image.setCreatedAt(dto.getCreatedAt());
            image.setThumbnailImage(dto.getThumbnailImage());
            image.setThumbnailWidthRatio(dto.getThumbnailWidthRatio());
            image.setThumbnailHeightRatio(dto.getThumbnailHeightRatio());
            allImages.add(image);
          }

          // Check if there are more pages
          hasMore = page < paginatedResponse.getTotalPages() - 1;
          page++;
        } else {
          hasMore = false;
        }
      }

      log.info("Fetched {} total images for project {}", allImages.size(), projectId);
      return allImages;
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get images for project {}: {}",
          projectId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Get images for a specific project and split
   *
   * @param projectId the project ID
   * @param split the split value (train, dev, test, or unassigned)
   * @return list of images
   */
  @MethodLog
  public List<Image> getImagesByProjectAndSplit(Long projectId, String split) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);
      params.put("split", split);

      ResponseEntity<Image[]> response =
          restTemplate.getForEntity(
              operationalServiceURI
                  + "/operational/landingai/images/project/{projectId}/split/{split}",
              Image[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get images for project {} and split {}: {}",
          projectId,
          split,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Get image file content from file system
   *
   * @param fileName the file name
   * @return the file content as byte array
   */
  @MethodLog
  public byte[] getImageFile(String fileName) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("fileName", fileName);

      ResponseEntity<byte[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/images/file/{fileName}",
              byte[].class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to get file {}: {}", fileName, e.getMessage());
      throw e;
    }
  }

  /**
   * Get image file content from file system by image ID
   *
   * @param imageId the image ID
   * @return the file content as byte array
   */
  /**
   * Update the split value for an image
   *
   * @param imageId the image ID
   * @param split the split value (Unassigned, Train, Dev, Test)
   * @return the updated image
   */
  @MethodLog
  public Image updateImageSplit(Long imageId, String split) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("split", split);

      ResponseEntity<Image> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/images/{imageId}/split",
              HttpMethod.PUT,
              new HttpEntity<>(requestBody),
              Image.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to update split for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update the isNoClass flag for an image
   *
   * @param imageId the image ID
   * @param isNoClass the isNoClass flag value
   * @return the updated image
   */
  @MethodLog
  public Image updateIsNoClass(Long imageId, Boolean isNoClass) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      Map<String, Boolean> requestBody = new HashMap<>();
      requestBody.put("isNoClass", isNoClass);

      ResponseEntity<Image> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/images/{imageId}/is-no-class",
              HttpMethod.PUT,
              new HttpEntity<>(requestBody),
              Image.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to update isNoClass for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Delete an image and all associated data
   *
   * @param imageId the image ID to delete
   */
  @MethodLog
  public void deleteImage(Long imageId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/images/{imageId}", params);
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to delete image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete multiple images in batch
   *
   * @param imageIds the list of image IDs to delete
   */
  @MethodLog
  public void deleteImages(List<Long> imageIds) {
    try {
      Map<String, List<Long>> requestBody = new HashMap<>();
      requestBody.put("imageIds", imageIds);

      restTemplate.postForEntity(
          operationalServiceURI + "/operational/landingai/images/delete-batch",
          requestBody,
          Void.class);
    } catch (HttpClientErrorException e) {
      log.error("Error calling operational layer to delete images batch: {}", e.getMessage());
      throw e;
    }
  }

  // ==================== Image Tags Operations ====================

  /**
   * Get all tags for an image
   *
   * @param imageId the image ID
   * @return list of image tags
   */
  @MethodLog
  public List<ImageTag> getImageTags(Long imageId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      ResponseEntity<ImageTag[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/images/{imageId}/tags",
              ImageTag[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get tags for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Add a tag to an image
   *
   * @param imageId the image ID
   * @param tagId the project tag ID
   * @param createdBy the user who created the tag
   * @return the created image tag
   */
  @MethodLog
  public ImageTag addImageTag(Long imageId, Long tagId, String createdBy) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("tagId", tagId);
      requestBody.put("createdBy", createdBy);

      ResponseEntity<ImageTag> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/images/{imageId}/tags",
              requestBody,
              ImageTag.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to add tag to image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Remove a tag from an image
   *
   * @param imageId the image ID
   * @param tagId the image tag ID
   */
  @MethodLog
  public void removeImageTag(Long imageId, Long tagId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);
      params.put("tagId", tagId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/images/{imageId}/tags/{tagId}", params);
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to remove tag {} from image {}: {}",
          tagId,
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update image tags in batch (replace all tags)
   *
   * @param imageId the image ID
   * @param tagIds list of project tag IDs
   * @param createdBy the user who updated the tags
   * @return list of updated image tags
   */
  @MethodLog
  public List<ImageTag> updateImageTags(Long imageId, List<Long> tagIds, String createdBy) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("tagIds", tagIds);
      requestBody.put("createdBy", createdBy);

      ResponseEntity<ImageTag[]> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/images/{imageId}/tags",
              HttpMethod.PUT,
              new HttpEntity<>(requestBody),
              ImageTag[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to update tags for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }

  // ==================== Image Metadata Operations ====================

  /**
   * Get all metadata for an image
   *
   * @param imageId the image ID
   * @return list of image metadata
   */
  @MethodLog
  public List<ImageMetadata> getImageMetadata(Long imageId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      ResponseEntity<ImageMetadata[]> response =
          restTemplate.getForEntity(
              operationalServiceURI + "/operational/landingai/images/{imageId}/metadata",
              ImageMetadata[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to get metadata for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Add metadata to an image
   *
   * @param imageId the image ID
   * @param metadataId the project metadata ID
   * @param value the metadata value
   * @param createdBy the user who created the metadata
   * @return the created image metadata
   */
  @MethodLog
  public ImageMetadata addImageMetadata(
      Long imageId, Long metadataId, String value, String createdBy) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("metadataId", metadataId);
      requestBody.put("value", value);
      requestBody.put("createdBy", createdBy);

      ResponseEntity<ImageMetadata> response =
          restTemplate.postForEntity(
              operationalServiceURI + "/operational/landingai/images/{imageId}/metadata",
              requestBody,
              ImageMetadata.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to add metadata to image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update an image metadata value
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @param value the new value
   * @return the updated image metadata
   */
  @MethodLog
  public ImageMetadata updateImageMetadata(Long imageId, Long metadataId, String value) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);
      params.put("metadataId", metadataId);

      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("value", value);

      ResponseEntity<ImageMetadata> response =
          restTemplate.exchange(
              operationalServiceURI
                  + "/operational/landingai/images/{imageId}/metadata/{metadataId}",
              HttpMethod.PUT,
              new HttpEntity<>(requestBody),
              ImageMetadata.class,
              params);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to update metadata {} for image {}: {}",
          metadataId,
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Remove metadata from an image
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   */
  @MethodLog
  public void removeImageMetadata(Long imageId, Long metadataId) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);
      params.put("metadataId", metadataId);

      restTemplate.delete(
          operationalServiceURI + "/operational/landingai/images/{imageId}/metadata/{metadataId}",
          params);
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to remove metadata {} from image {}: {}",
          metadataId,
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Update image metadata in batch (replace all metadata)
   *
   * @param imageId the image ID
   * @param metadataList list of metadata with metadataId and value
   * @param createdBy the user who updated the metadata
   * @return list of updated image metadata
   */
  @MethodLog
  public List<ImageMetadata> updateImageMetadataBatch(
      Long imageId, List<Map<String, Object>> metadataList, String createdBy) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("imageId", imageId);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("metadataList", metadataList);
      requestBody.put("createdBy", createdBy);

      ResponseEntity<ImageMetadata[]> response =
          restTemplate.exchange(
              operationalServiceURI + "/operational/landingai/images/{imageId}/metadata",
              HttpMethod.PUT,
              new HttpEntity<>(requestBody),
              ImageMetadata[].class,
              params);
      return Arrays.asList(response.getBody());
    } catch (HttpClientErrorException e) {
      log.error(
          "Error calling operational layer to update metadata for image {}: {}",
          imageId,
          e.getMessage());
      throw e;
    }
  }

  /**
   * Export dataset as ZIP file for training
   *
   * @param projectId the project ID
   * @param imageIds optional list of image IDs to export (if null, exports all images)
   * @return File containing the exported dataset
   */
  @MethodLog
  public java.io.File exportDataset(Long projectId, List<Long> imageIds) {
    try {
      log.info(
          "Calling operational layer to export dataset for project: {}. Images: {}",
          projectId,
          imageIds != null ? imageIds.size() + " selected" : "all");

      Map<String, Object> params = new HashMap<>();
      params.put("projectId", projectId);

      // Set headers for JSON content type
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<List<Long>> requestEntity = new HttpEntity<>(imageIds, headers);

      ResponseEntity<byte[]> response =
          restTemplate.postForEntity(
              operationalServiceURI
                  + "/operational/landingai/images/project/{projectId}/export-dataset",
              requestEntity,
              byte[].class,
              params);

      // Write response to temporary file
      java.io.File tempFile = java.io.File.createTempFile("dataset-export-", ".zip");
      java.nio.file.Files.write(tempFile.toPath(), response.getBody());

      return tempFile;
    } catch (Exception e) {
      log.error(
          "Error calling operational layer to export dataset for project {}: {}",
          projectId,
          e.getMessage());
      throw new RuntimeException("Failed to export dataset", e);
    }
  }
}
