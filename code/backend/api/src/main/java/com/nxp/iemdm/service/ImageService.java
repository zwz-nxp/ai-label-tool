package com.nxp.iemdm.service;

import com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest;
import com.nxp.iemdm.shared.dto.landingai.ImageListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ImageUploadResponse;
import com.nxp.iemdm.shared.dto.landingai.PaginatedResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/** Service interface for Landing AI image operations. */
public interface ImageService {

  /**
   * Upload images to a project.
   *
   * @param files the image files to upload
   * @param projectId the project ID
   * @param userId the user identifier
   * @return list of upload responses
   */
  List<ImageUploadResponse> uploadImages(List<MultipartFile> files, Long projectId, String userId);

  /**
   * Upload a ZIP file containing classified images. The ZIP should contain folders named by class,
   * with images inside each folder. New classes will be created automatically if they don't exist.
   *
   * @param file the ZIP file to upload
   * @param projectId the project ID
   * @param userId the user identifier
   * @return map containing upload statistics (success, totalImages, classesCreated, classesReused,
   *     errors)
   */
  Map<String, Object> uploadClassifiedImagesZip(MultipartFile file, Long projectId, String userId);

  /**
   * Upload a ZIP file containing batch images. The ZIP should contain images directly at the root
   * level (no class folders). This is for Object Detection and Segmentation projects.
   *
   * @param file the ZIP file to upload
   * @param projectId the project ID
   * @param userId the user identifier
   * @return map containing upload statistics (success, totalImages, errors)
   */
  Map<String, Object> uploadBatchImagesZip(MultipartFile file, Long projectId, String userId);

  /**
   * Get thumbnail for an image.
   *
   * @param id the image ID
   * @return the thumbnail image as byte array
   */
  byte[] getThumbnail(Long id);

  /**
   * Get full image file for an image.
   *
   * @param id the image ID
   * @return the full image file as byte array
   */
  byte[] getImageFileById(Long id);

  /**
   * Get paginated images for a project with label information and optional filters.
   *
   * @param projectId the project ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param viewMode the view mode (images or instances)
   * @param sortBy the sort method
   * @param filters the filter criteria (optional)
   * @return paginated response with image list items
   */
  PaginatedResponse<ImageListItemDTO> getImagesForProject(
      Long projectId,
      int page,
      int size,
      String viewMode,
      String sortBy,
      ImageFilterRequest filters,
      boolean includeThumbnails);
}
