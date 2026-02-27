package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.exception.landingai.ImageProcessingException;
import com.nxp.iemdm.exception.landingai.InvalidImageFormatException;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageMetadata;
import com.nxp.iemdm.model.landingai.ImageTag;
import com.nxp.iemdm.service.ImageService;
import com.nxp.iemdm.service.rest.landingai.ImageServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ImageUploadResponse;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * API Controller for Image operations. Provides RESTful endpoints for frontend communication.
 *
 * <p>Requirements: 24.2, 24.4, 24.5, 24.6
 */
@RestController
@RequestMapping("/api/landingai/images")
@Slf4j
public class ImageApiController {

  private final ImageServiceREST imageServiceREST;
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  private final ImageService imageService;

  @Autowired
  public ImageApiController(ImageServiceREST imageServiceREST, ImageService imageService) {
    this.imageServiceREST = imageServiceREST;
    this.imageService = imageService;
  }

  /**
   * Get an image by ID
   *
   * @param imageId the image ID
   * @return the image with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/{imageId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<Image> getImageById(@PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      Image image = imageServiceREST.getImageById(imageId);
      return ResponseEntity.ok(image);
    } catch (Exception e) {
      log.error("Error getting image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Get paginated images for a project with label information and optional filters. This replaces
   * the old getImagesByProjectId endpoint to support pagination.
   *
   * @param projectId the project ID
   * @return paginated response with image list items
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<Image>> getImagesByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<Image> images = imageServiceREST.getImagesByProjectId(projectId);
      return ResponseEntity.ok(images);
    } catch (Exception e) {
      log.error("Error getting images for project {}: {}", projectId, e.getMessage());
      throw e;
    }
  }

  /**
   * Get paginated images for a project with label information and optional filters. This replaces
   * the old getImagesByProjectId endpoint to support pagination.
   *
   * @param projectId the project ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param viewMode the view mode (images or instances)
   * @param sortBy the sort method
   * @param mediaStatus filter by media status (comma-separated: labeled,unlabeled,no_class)
   * @param groundTruthLabels filter by ground truth label class IDs (comma-separated)
   * @param predictionLabels filter by prediction label class IDs (comma-separated)
   * @param annotationType filter by annotation type (Ground truth or Prediction)
   * @param split filter by split (comma-separated: training,dev,test)
   * @param tags filter by tag IDs (comma-separated)
   * @param mediaName filter by media name (partial match)
   * @param labeler filter by labeler username
   * @param mediaId filter by media ID
   * @param user the authenticated user
   * @return paginated response with image list items
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}/pageable", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<
          com.nxp.iemdm.shared.dto.landingai.PaginatedResponse<
              com.nxp.iemdm.shared.dto.landingai.ImageListItemDTO>>
      getImagesForProject(
          @PathVariable("projectId") @NotNull Long projectId,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "viewMode", defaultValue = "images") String viewMode,
          @RequestParam(value = "sortBy", defaultValue = "upload_time_desc") String sortBy,
          @RequestParam(value = "mediaStatus", required = false) String mediaStatus,
          @RequestParam(value = "groundTruthLabels", required = false) String groundTruthLabels,
          @RequestParam(value = "predictionLabels", required = false) String predictionLabels,
          @RequestParam(value = "annotationType", required = false) String annotationType,
          @RequestParam(value = "modelId", required = false) Long modelId,
          @RequestParam(value = "split", required = false) String split,
          @RequestParam(value = "tags", required = false) String tags,
          @RequestParam(value = "mediaName", required = false) String mediaName,
          @RequestParam(value = "labeler", required = false) String labeler,
          @RequestParam(value = "mediaId", required = false) String mediaId,
          @RequestParam(value = "noClass", required = false) Boolean noClass,
          @RequestParam(value = "predictionNoClass", required = false) Boolean predictionNoClass,
          @RequestParam(value = "includeThumbnails", defaultValue = "true")
              boolean includeThumbnails,
          @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Getting images for project: {}, page: {}, size: {}, viewMode: {}, sortBy: {}, annotationType: {} by user: {}",
        projectId,
        page,
        size,
        viewMode,
        sortBy,
        annotationType,
        user.getUsername());

    // Build filter request from query parameters
    com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filters =
        new com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest();

    if (mediaStatus != null && !mediaStatus.isEmpty()) {
      filters.setMediaStatus(java.util.Arrays.asList(mediaStatus.split(",")));
    }
    if (groundTruthLabels != null && !groundTruthLabels.isEmpty()) {
      filters.setGroundTruthLabels(
          java.util.Arrays.stream(groundTruthLabels.split(","))
              .map(Long::parseLong)
              .collect(java.util.stream.Collectors.toList()));
    }
    if (predictionLabels != null && !predictionLabels.isEmpty()) {
      filters.setPredictionLabels(
          java.util.Arrays.stream(predictionLabels.split(","))
              .map(Long::parseLong)
              .collect(java.util.stream.Collectors.toList()));
    }
    if (annotationType != null && !annotationType.isEmpty()) {
      filters.setAnnotationType(annotationType);
    }
    if (modelId != null) {
      filters.setModelId(modelId);
    }
    if (split != null && !split.isEmpty()) {
      filters.setSplit(java.util.Arrays.asList(split.split(",")));
    }
    if (tags != null && !tags.isEmpty()) {
      filters.setTags(
          java.util.Arrays.stream(tags.split(","))
              .map(Long::parseLong)
              .collect(java.util.stream.Collectors.toList()));
    }
    if (mediaName != null && !mediaName.isEmpty()) {
      filters.setMediaName(mediaName);
    }
    if (labeler != null && !labeler.isEmpty()) {
      filters.setLabeler(labeler);
    }
    if (mediaId != null && !mediaId.isEmpty()) {
      filters.setMediaId(mediaId);
    }
    if (noClass != null && noClass) {
      filters.setNoClass(true);
    }
    if (predictionNoClass != null && predictionNoClass) {
      filters.setPredictionNoClass(true);
    }

    log.info("Filters: {}", filters);

    com.nxp.iemdm.shared.dto.landingai.PaginatedResponse<
            com.nxp.iemdm.shared.dto.landingai.ImageListItemDTO>
        response =
            imageService.getImagesForProject(
                projectId, page, size, viewMode, sortBy, filters, includeThumbnails);

    return ResponseEntity.ok(response);
  }

  /**
   * Get image file content from file system
   *
   * @param fileName the file name
   * @return the image file as byte array with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/file/{fileName}", produces = "image/jpeg")
  public ResponseEntity<ByteArrayResource> getImageFile(@PathVariable("fileName") String fileName) {
    try {
      if (fileName == null || fileName.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      byte[] imageData = imageServiceREST.getImageFile(fileName);
      ByteArrayResource resource = new ByteArrayResource(imageData);
      return ResponseEntity.ok()
          .contentLength(imageData.length)
          .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
          .body(resource);
    } catch (Exception e) {
      log.error("Error getting image file {}: {}", fileName, e.getMessage());
      throw e;
    }
  }

  /**
   * Get image file content from file system by image ID
   *
   * @param imageId the image ID
   * @return the image file as byte array with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/{imageId}/file")
  public ResponseEntity<byte[]> getImageFileById(@PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      byte[] imageData = imageService.getImageFileById(imageId);

      if (imageData == null || imageData.length == 0) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok()
          .contentLength(imageData.length)
          .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
          .body(imageData);
    } catch (Exception e) {
      log.error("Error getting image file by imageId {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Determine content type from file name extension
   *
   * @param fileName the file name
   * @return the MIME type string
   */
  private String getContentTypeFromFileName(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "image/jpeg"; // default
    }

    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    switch (extension) {
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "gif":
        return "image/gif";
      case "bmp":
        return "image/bmp";
      case "webp":
        return "image/webp";
      case "svg":
        return "image/svg+xml";
      default:
        return "image/jpeg"; // default fallback
    }
  }

  /**
   * Update the split value for an image
   *
   * @param imageId the image ID
   * @param requestBody map containing the split value
   * @return the updated image with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/split",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<Image> updateImageSplit(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, String> requestBody) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      String split = requestBody.get("split");
      if (split == null || split.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      Image updatedImage = imageServiceREST.updateImageSplit(imageId, split);
      return ResponseEntity.ok(updatedImage);
    } catch (Exception e) {
      log.error("Error updating split for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update the isNoClass flag for an image
   *
   * @param imageId the image ID
   * @param requestBody map containing the isNoClass flag
   * @return the updated image with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/is-no-class",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<Image> updateIsNoClass(
      @PathVariable("imageId") Long imageId, @RequestBody Map<String, Boolean> requestBody) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      Boolean isNoClass = requestBody.get("isNoClass");
      if (isNoClass == null) {
        return ResponseEntity.badRequest().build();
      }
      Image updatedImage = imageServiceREST.updateIsNoClass(imageId, isNoClass);
      return ResponseEntity.ok(updatedImage);
    } catch (Exception e) {
      log.error("Error updating isNoClass for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete an image and all associated data
   *
   * @param imageId the image ID to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{imageId}")
  public ResponseEntity<Void> deleteImage(@PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      imageServiceREST.deleteImage(imageId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete multiple images in batch
   *
   * @param requestBody map containing the list of image IDs to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @PostMapping(path = "/delete-batch", consumes = MediaType.APPLICATION_JSON)
  public ResponseEntity<Void> deleteImagesBatch(@RequestBody Map<String, List<Long>> requestBody) {
    try {
      List<Long> imageIds = requestBody.get("imageIds");
      if (imageIds == null || imageIds.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }
      imageServiceREST.deleteImages(imageIds);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting images batch: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Upload images to a project.
   *
   * @param files the image files to upload
   * @param projectId the project ID
   * @param user the authenticated user
   * @return list of upload responses
   */
  @MethodLog
  @PostMapping(
      path = "/upload",
      consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<ImageUploadResponse>> uploadImages(
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Uploading {} images to project: {} by user: {}",
        files.size(),
        projectId,
        user.getUsername());

    // Validate file sizes
    for (MultipartFile file : files) {
      if (file.getSize() > MAX_FILE_SIZE) {
        throw new ImageProcessingException(
            String.format(
                "File size exceeds maximum allowed size of %d MB for file '%s'",
                MAX_FILE_SIZE / (1024 * 1024), file.getOriginalFilename()));
      }
    }

    List<ImageUploadResponse> responses =
        imageService.uploadImages(files, projectId, user.getUsername());

    return ResponseEntity.ok(responses);
  }

  /**
   * Upload a ZIP file containing classified images. The ZIP should contain folders named by class,
   * with images inside each folder. New classes will be created automatically if they don't exist.
   *
   * @param file the ZIP file to upload
   * @param projectId the project ID
   * @param user the authenticated user
   * @return upload result with statistics
   */
  @MethodLog
  @PostMapping(
      path = "/upload-classified",
      consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadClassifiedImages(
      @RequestParam("file") MultipartFile file,
      @RequestParam("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Uploading classified images ZIP to project: {} by user: {}",
        projectId,
        user.getUsername());

    // Validate file type
    String filename = file.getOriginalFilename();
    if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
      throw new InvalidImageFormatException("Only ZIP files are supported");
    }

    // Validate file size (max 2048MB)
    long maxZipSize = 2048L * 1024 * 1024;
    if (file.getSize() > maxZipSize) {
      throw new ImageProcessingException(
          String.format(
              "File size exceeds maximum allowed size of %d MB", maxZipSize / (1024 * 1024)));
    }

    Map<String, Object> result =
        imageService.uploadClassifiedImagesZip(file, projectId, user.getUsername());

    return ResponseEntity.ok(result);
  }

  /**
   * Upload a ZIP file containing batch images. The ZIP should contain images directly at the root
   * level (no class folders). This is for Object Detection and Segmentation projects.
   *
   * @param file the ZIP file to upload
   * @param projectId the project ID
   * @param user the authenticated user
   * @return upload result with statistics
   */
  @MethodLog
  @PostMapping(
      path = "/upload-batch",
      consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadBatchImages(
      @RequestParam("file") MultipartFile file,
      @RequestParam("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Uploading batch images ZIP to project: {} by user: {}", projectId, user.getUsername());

    // Validate file type
    String filename = file.getOriginalFilename();
    if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
      throw new InvalidImageFormatException("Only ZIP files are supported");
    }

    // Validate file size (max 2048MB)
    long maxZipSize = 2048L * 1024 * 1024;
    if (file.getSize() > maxZipSize) {
      throw new ImageProcessingException(
          String.format(
              "File size exceeds maximum allowed size of %d MB", maxZipSize / (1024 * 1024)));
    }

    Map<String, Object> result =
        imageService.uploadBatchImagesZip(file, projectId, user.getUsername());

    return ResponseEntity.ok(result);
  }

  /**
   * Get thumbnail for an image.
   *
   * @param id the image ID
   * @param user the authenticated user
   * @return the thumbnail image as byte array
   */
  @MethodLog
  @GetMapping(
      path = "/{id}/thumbnail",
      produces = org.springframework.http.MediaType.IMAGE_JPEG_VALUE)
  public ResponseEntity<byte[]> getThumbnail(
      @PathVariable("id") @NotNull Long id, @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting thumbnail for image: {} by user: {}", id, user.getUsername());

    byte[] thumbnail = imageService.getThumbnail(id);

    if (thumbnail == null || thumbnail.length == 0) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
        .body(thumbnail);
  }

  /**
   * Exception handler for InvalidImageFormatException.
   *
   * @param ex the exception
   * @return error response with 400 Bad Request status
   */
  @ExceptionHandler(InvalidImageFormatException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidImageFormat(
      InvalidImageFormatException ex) {
    log.error("Invalid image format error: {}", ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
    errorResponse.put("error", "Bad Request");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Exception handler for ImageProcessingException.
   *
   * @param ex the exception
   * @return error response with 500 Internal Server Error status
   */
  @ExceptionHandler(ImageProcessingException.class)
  public ResponseEntity<Map<String, Object>> handleImageProcessing(ImageProcessingException ex) {
    log.error("Image processing error: {}", ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    errorResponse.put("error", "Internal Server Error");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Exception handler for EntityNotFoundException.
   *
   * @param ex the exception
   * @return error response with 404 Not Found status
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
    log.error("Entity not found error: {}", ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
    errorResponse.put("error", "Not Found");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Exception handler for general exceptions.
   *
   * @param ex the exception
   * @return error response with 500 Internal Server Error status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
    log.error("Internal server error: {}", ex.getMessage(), ex);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    errorResponse.put("error", "Internal Server Error");
    errorResponse.put("message", "An unexpected error occurred");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  // ==================== Image Tags API Endpoints ====================

  /**
   * Get all tags for an image
   *
   * @param imageId the image ID
   * @return list of image tags with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/{imageId}/tags", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageTag>> getImageTags(@PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<ImageTag> tags = imageServiceREST.getImageTags(imageId);
      return ResponseEntity.ok(tags);
    } catch (Exception e) {
      log.error("Error getting tags for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Add a tag to an image
   *
   * @param imageId the image ID
   * @param requestBody map containing tagId
   * @param user the authenticated user
   * @return the created image tag with HTTP 201 status
   */
  @MethodLog
  @PostMapping(
      path = "/{imageId}/tags",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageTag> addImageTag(
      @PathVariable("imageId") Long imageId,
      @RequestBody Map<String, Long> requestBody,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      Long tagId = requestBody.get("tagId");
      if (tagId == null) {
        return ResponseEntity.badRequest().build();
      }
      ImageTag imageTag = imageServiceREST.addImageTag(imageId, tagId, user.getUsername());
      return ResponseEntity.status(HttpStatus.CREATED).body(imageTag);
    } catch (Exception e) {
      log.error("Error adding tag to image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Remove a tag from an image
   *
   * @param imageId the image ID
   * @param tagId the image tag ID
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{imageId}/tags/{tagId}")
  public ResponseEntity<Void> removeImageTag(
      @PathVariable("imageId") Long imageId, @PathVariable("tagId") Long tagId) {
    try {
      if (imageId == null || imageId <= 0 || tagId == null || tagId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      imageServiceREST.removeImageTag(imageId, tagId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error removing tag {} from image {}: {}", tagId, imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update image tags in batch (replace all tags)
   *
   * @param imageId the image ID
   * @param requestBody map containing list of tagIds
   * @param user the authenticated user
   * @return list of updated image tags with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/tags",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageTag>> updateImageTags(
      @PathVariable("imageId") Long imageId,
      @RequestBody Map<String, List<Long>> requestBody,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<Long> tagIds = requestBody.get("tagIds");
      List<ImageTag> tags = imageServiceREST.updateImageTags(imageId, tagIds, user.getUsername());
      return ResponseEntity.ok(tags);
    } catch (Exception e) {
      log.error("Error updating tags for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  // ==================== Image Metadata API Endpoints ====================

  /**
   * Get all metadata for an image
   *
   * @param imageId the image ID
   * @return list of image metadata with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/{imageId}/metadata", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageMetadata>> getImageMetadata(
      @PathVariable("imageId") Long imageId) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<ImageMetadata> metadata = imageServiceREST.getImageMetadata(imageId);
      return ResponseEntity.ok(metadata);
    } catch (Exception e) {
      log.error("Error getting metadata for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Add metadata to an image
   *
   * @param imageId the image ID
   * @param requestBody map containing metadataId and value
   * @param user the authenticated user
   * @return the created image metadata with HTTP 201 status
   */
  @MethodLog
  @PostMapping(
      path = "/{imageId}/metadata",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageMetadata> addImageMetadata(
      @PathVariable("imageId") Long imageId,
      @RequestBody Map<String, Object> requestBody,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      Long metadataId = Long.valueOf(requestBody.get("metadataId").toString());
      String value = (String) requestBody.get("value");
      ImageMetadata imageMetadata =
          imageServiceREST.addImageMetadata(imageId, metadataId, value, user.getUsername());
      return ResponseEntity.status(HttpStatus.CREATED).body(imageMetadata);
    } catch (Exception e) {
      log.error("Error adding metadata to image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update an image metadata value
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @param requestBody map containing the new value
   * @return the updated image metadata with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/metadata/{metadataId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ImageMetadata> updateImageMetadata(
      @PathVariable("imageId") Long imageId,
      @PathVariable("metadataId") Long metadataId,
      @RequestBody Map<String, String> requestBody) {
    try {
      if (imageId == null || imageId <= 0 || metadataId == null || metadataId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      String value = requestBody.get("value");
      ImageMetadata imageMetadata =
          imageServiceREST.updateImageMetadata(imageId, metadataId, value);
      return ResponseEntity.ok(imageMetadata);
    } catch (Exception e) {
      log.error("Error updating metadata {} for image {}: {}", metadataId, imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Remove metadata from an image
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{imageId}/metadata/{metadataId}")
  public ResponseEntity<Void> removeImageMetadata(
      @PathVariable("imageId") Long imageId, @PathVariable("metadataId") Long metadataId) {
    try {
      if (imageId == null || imageId <= 0 || metadataId == null || metadataId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      imageServiceREST.removeImageMetadata(imageId, metadataId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(
          "Error removing metadata {} from image {}: {}", metadataId, imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update image metadata in batch (replace all metadata)
   *
   * @param imageId the image ID
   * @param requestBody map containing metadataList (with metadataId and value)
   * @param user the authenticated user
   * @return list of updated image metadata with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{imageId}/metadata",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ImageMetadata>> updateImageMetadataBatch(
      @PathVariable("imageId") Long imageId,
      @RequestBody Map<String, List<Map<String, Object>>> requestBody,
      @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      if (imageId == null || imageId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<Map<String, Object>> metadataList = requestBody.get("metadataList");
      List<ImageMetadata> metadata =
          imageServiceREST.updateImageMetadataBatch(imageId, metadataList, user.getUsername());
      return ResponseEntity.ok(metadata);
    } catch (Exception e) {
      log.error("Error updating metadata for image {}: {}", imageId, e.getMessage());
      throw e;
    }
  }

  /**
   * Batch set metadata for multiple images
   *
   * @param requestBody map containing imageIds and metadata key-value pairs
   * @param user the authenticated user
   * @return HTTP 200 OK status on success
   */
  @MethodLog
  @PostMapping(
      path = "/batch-set-metadata",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<Map<String, Object>> batchSetMetadata(
      @RequestBody Map<String, Object> requestBody, @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      // JSON deserialization produces List<Integer> for numeric arrays, need to convert to Long
      @SuppressWarnings("unchecked")
      List<?> imageIdsRaw = (List<?>) requestBody.get("imageIds");
      List<Long> imageIds =
          imageIdsRaw.stream()
              .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
              .collect(java.util.stream.Collectors.toList());

      @SuppressWarnings("unchecked")
      Map<String, String> metadata = (Map<String, String>) requestBody.get("metadata");

      if (imageIds == null || imageIds.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "imageIds cannot be empty"));
      }
      if (metadata == null || metadata.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "metadata cannot be empty"));
      }

      imageServiceREST.batchSetMetadata(imageIds, metadata, user.getUsername());

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("updatedCount", imageIds.size());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error batch setting metadata: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Batch set tags for multiple images
   *
   * @param requestBody map containing imageIds and tagIds arrays
   * @param user the authenticated user
   * @return HTTP 200 OK status on success
   */
  @MethodLog
  @PostMapping(
      path = "/batch-set-tags",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<Map<String, Object>> batchSetTags(
      @RequestBody Map<String, Object> requestBody, @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      // JSON deserialization produces List<Integer> for numeric arrays, need to convert to Long
      @SuppressWarnings("unchecked")
      List<?> imageIdsRaw = (List<?>) requestBody.get("imageIds");
      List<Long> imageIds =
          imageIdsRaw.stream()
              .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
              .collect(java.util.stream.Collectors.toList());

      @SuppressWarnings("unchecked")
      List<?> tagIdsRaw = (List<?>) requestBody.get("tagIds");
      List<Long> tagIds =
          tagIdsRaw.stream()
              .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
              .collect(java.util.stream.Collectors.toList());

      if (imageIds == null || imageIds.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "imageIds cannot be empty"));
      }
      if (tagIds == null || tagIds.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "tagIds cannot be empty"));
      }

      imageServiceREST.batchSetTags(imageIds, tagIds, user.getUsername());

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("updatedCount", imageIds.size());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error batch setting tags: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Batch set class for multiple images (Classification projects)
   *
   * @param requestBody map containing imageIds and classId
   * @param user the authenticated user
   * @return HTTP 200 OK status on success
   */
  @MethodLog
  @PostMapping(
      path = "/batch-set-class",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<Map<String, Object>> batchSetClass(
      @RequestBody Map<String, Object> requestBody, @AuthenticationPrincipal IEMDMPrincipal user) {
    try {
      // JSON deserialization produces List<Integer> for numeric arrays, need to convert to Long
      @SuppressWarnings("unchecked")
      List<?> imageIdsRaw = (List<?>) requestBody.get("imageIds");
      List<Long> imageIds =
          imageIdsRaw.stream()
              .map(id -> id instanceof Integer ? ((Integer) id).longValue() : (Long) id)
              .collect(java.util.stream.Collectors.toList());

      Object classIdRaw = requestBody.get("classId");
      Long classId =
          classIdRaw instanceof Integer ? ((Integer) classIdRaw).longValue() : (Long) classIdRaw;

      if (imageIds == null || imageIds.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "imageIds cannot be empty"));
      }
      if (classId == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "classId cannot be null"));
      }

      imageServiceREST.batchSetClass(imageIds, classId, user.getUsername());

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("updatedCount", imageIds.size());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error batch setting class: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Export dataset as ZIP file for training Supports both Classification and Object Detection
   * project types
   *
   * @param projectId the project ID
   * @param imageIds optional list of image IDs to export (if null, exports all images)
   * @return ZIP file containing the dataset
   */
  @MethodLog
  @PostMapping(path = "/project/{projectId}/export-dataset")
  public ResponseEntity<org.springframework.core.io.Resource> exportDataset(
      @PathVariable("projectId") Long projectId,
      @RequestBody(required = false) List<Long> imageIds) {
    try {
      log.info(
          "Exporting dataset for project: {}. Images: {}",
          projectId,
          imageIds != null ? imageIds.size() + " selected" : "all");

      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }

      // Call the export service
      java.io.File zipFile = imageServiceREST.exportDataset(projectId, imageIds);

      // Read file into byte array
      byte[] data = java.nio.file.Files.readAllBytes(zipFile.toPath());
      ByteArrayResource resource = new ByteArrayResource(data);

      // Clean up temp file
      zipFile.delete();

      // Generate filename
      String filename =
          String.format(
              "dataset-project-%d-%s.zip",
              projectId,
              java.time.LocalDateTime.now()
                  .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));

      return ResponseEntity.ok()
          .header(
              org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + filename + "\"")
          .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
          .contentLength(data.length)
          .body(resource);

    } catch (Exception e) {
      log.error("Error exporting dataset for project {}: {}", projectId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
