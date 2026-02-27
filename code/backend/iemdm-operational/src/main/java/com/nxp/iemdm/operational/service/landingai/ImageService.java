package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageMetadata;
import com.nxp.iemdm.model.landingai.ImageTag;
import com.nxp.iemdm.model.landingai.ProjectMetadata;
import com.nxp.iemdm.model.landingai.ProjectTag;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageFileRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageMetadataRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImagePredictionLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageTagRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectMetadataRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectTagRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ImageService {

  private final ImageRepository imageRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ImageTagRepository imageTagRepository;
  private final ImageMetadataRepository imageMetadataRepository;
  private final ImageFileRepository imageFileRepository;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final ProjectTagRepository projectTagRepository;
  private final ProjectMetadataRepository projectMetadataRepository;

  public ImageService(
      ImageRepository imageRepository,
      ImageLabelRepository imageLabelRepository,
      ImageTagRepository imageTagRepository,
      ImageMetadataRepository imageMetadataRepository,
      ImageFileRepository imageFileRepository,
      ImagePredictionLabelRepository imagePredictionLabelRepository,
      ProjectTagRepository projectTagRepository,
      ProjectMetadataRepository projectMetadataRepository) {
    this.imageRepository = imageRepository;
    this.imageLabelRepository = imageLabelRepository;
    this.imageTagRepository = imageTagRepository;
    this.imageMetadataRepository = imageMetadataRepository;
    this.imageFileRepository = imageFileRepository;
    this.imagePredictionLabelRepository = imagePredictionLabelRepository;
    this.projectTagRepository = projectTagRepository;
    this.projectMetadataRepository = projectMetadataRepository;
  }

  /**
   * Save a new image
   *
   * @param image the image to save
   * @return the saved image
   */
  @Transactional
  public Image saveImage(Image image) {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null");
    }

    if (image.getProject() == null) {
      throw new IllegalArgumentException("Image must be associated with a project");
    }

    return imageRepository.save(image);
  }

  /**
   * Get an image by ID
   *
   * @param imageId the image ID
   * @return the image
   * @throws NotFoundException if image not found
   */
  @Transactional(readOnly = true)
  public Image getImageById(Long imageId) {
    return imageRepository
        .findById(imageId)
        .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));
  }

  /**
   * Get all images for a specific project
   *
   * @param projectId the project ID
   * @return list of images ordered by creation date descending
   */
  @Transactional(readOnly = true)
  public List<Image> getImagesByProjectId(Long projectId) {
    return imageRepository.findByProject_IdOrderByCreatedAtDesc(projectId);
  }

  /**
   * Delete an image and all associated data (labels, tags, metadata, prediction labels) Note:
   * Physical file is NOT deleted from file system
   *
   * @param imageId the image ID to delete
   * @throws NotFoundException if image not found
   */
  @Transactional
  public void deleteImage(Long imageId) {
    if (!imageRepository.existsById(imageId)) {
      throw new NotFoundException("Image not found with id: " + imageId);
    }

    // Cascade delete associated data
    imageLabelRepository.deleteByImage_Id(imageId);
    imagePredictionLabelRepository.deleteByImage_Id(imageId);
    imageTagRepository.deleteByImage_Id(imageId);
    imageMetadataRepository.deleteByImage_Id(imageId);

    // Delete the image record
    imageRepository.deleteById(imageId);

    log.info("Deleted image {} and all associated data", imageId);
  }

  /**
   * Delete multiple images and all associated data Note: Physical files are NOT deleted from file
   * system
   *
   * @param imageIds the list of image IDs to delete
   */
  @Transactional
  public void deleteImages(List<Long> imageIds) {
    if (imageIds == null || imageIds.isEmpty()) {
      return;
    }

    for (Long imageId : imageIds) {
      try {
        deleteImage(imageId);
      } catch (NotFoundException e) {
        log.warn("Image not found during batch delete: {}", imageId);
        // Continue with other images
      }
    }

    log.info("Batch deleted {} images", imageIds.size());
  }

  /**
   * Update the split value for an image
   *
   * @param imageId the image ID
   * @param split the new split value (Train, Dev, Test, Unassigned)
   * @return the updated image
   * @throws NotFoundException if image not found
   */
  @Transactional
  public Image updateImageSplit(Long imageId, String split) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    // Validate split value
    if (split != null && !split.isEmpty()) {
      String normalizedSplit = split.trim();
      if (!normalizedSplit.equals("Train")
          && !normalizedSplit.equals("Dev")
          && !normalizedSplit.equals("Test")
          && !normalizedSplit.equals("Unassigned")) {
        throw new IllegalArgumentException(
            "Invalid split value. Must be Train, Dev, Test, or Unassigned");
      }
      image.setSplit(normalizedSplit);
    } else {
      image.setSplit("Unassigned");
    }

    return imageRepository.save(image);
  }

  /**
   * Update the isNoClass flag for an image
   *
   * <p>Business Rules: - When setting is_no_class=true: Delete all labels, is_labeled will be set
   * to true by trigger - When setting is_no_class=false: Delete all labels, is_labeled will be set
   * to false by trigger - is_labeled is managed by database triggers and cannot be set manually
   *
   * @param imageId the image ID
   * @param isNoClass the new isNoClass value
   * @return the updated image
   * @throws NotFoundException if image not found
   */
  @Transactional
  public Image updateIsNoClass(Long imageId, boolean isNoClass) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    // Delete all labels for this image when is_no_class changes
    // This ensures consistency: is_no_class images should have no labels
    imageLabelRepository.deleteByImage_Id(imageId);

    log.info("Deleted all labels for image {} due to is_no_class change to {}", imageId, isNoClass);

    // Update is_no_class flag
    // The database trigger tgf_sync_no_class will automatically set is_labeled:
    // - If is_no_class=true: is_labeled=true
    // - If is_no_class=false: is_labeled=false (since we just deleted all labels)
    image.setIsNoClass(isNoClass);

    return imageRepository.save(image);
  }

  /**
   * Get image file content from database by file name
   *
   * @param fileName the file name
   * @return the file content as byte array
   * @throws NotFoundException if file not found
   */
  @Transactional(readOnly = true)
  public byte[] getImageFromFileSystem(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name cannot be null or empty");
    }

    // Find image by fileName
    Image image =
        imageRepository
            .findByFileName(fileName)
            .orElseThrow(() -> new NotFoundException("Image not found with fileName: " + fileName));

    // Get image file stream from database using file_id
    if (image.getFileId() == null) {
      throw new NotFoundException("Image has no file reference for fileName: " + fileName);
    }

    return imageFileRepository
        .findImageFileStreamById(image.getFileId())
        .orElseThrow(() -> new NotFoundException("Image file not found for fileName: " + fileName));
  }

  /**
   * Get image file content from database by image ID
   *
   * @param imageId the image ID
   * @return the file content as byte array
   * @throws NotFoundException if image or file not found
   */
  @Transactional(readOnly = true)
  public byte[] getImageFromFileSystemById(Long imageId) {
    if (imageId == null || imageId <= 0) {
      throw new IllegalArgumentException("Image ID must be a positive number");
    }

    // Get image to retrieve file_id
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    // Get image file stream using file_id
    if (image.getFileId() == null) {
      throw new NotFoundException("Image has no file reference for imageId: " + imageId);
    }

    return imageFileRepository
        .findImageFileStreamById(image.getFileId())
        .orElseThrow(() -> new NotFoundException("Image file not found for imageId: " + imageId));
  }

  // ==================== Image Tags Operations ====================

  /**
   * Get all tags for an image
   *
   * @param imageId the image ID
   * @return list of image tags
   * @throws NotFoundException if image not found
   */
  @Transactional(readOnly = true)
  public List<ImageTag> getImageTags(Long imageId) {
    if (!imageRepository.existsById(imageId)) {
      throw new NotFoundException("Image not found with id: " + imageId);
    }
    return imageTagRepository.findByImage_Id(imageId);
  }

  /**
   * Add a tag to an image
   *
   * @param imageId the image ID
   * @param tagId the project tag ID
   * @param createdBy the user who created the tag
   * @return the created image tag
   * @throws NotFoundException if image or tag not found
   */
  @Transactional
  public ImageTag addImageTag(Long imageId, Long tagId, String createdBy) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    ProjectTag projectTag =
        projectTagRepository
            .findById(tagId)
            .orElseThrow(() -> new NotFoundException("Project tag not found with id: " + tagId));

    // Check if tag already exists for this image
    if (imageTagRepository.existsByImage_IdAndProjectTagId(imageId, tagId)) {
      throw new IllegalArgumentException("Tag already exists for this image");
    }

    ImageTag imageTag = new ImageTag();
    imageTag.setImage(image);
    imageTag.setProjectTag(projectTag);
    imageTag.setCreatedBy(createdBy);

    return imageTagRepository.save(imageTag);
  }

  /**
   * Remove a tag from an image
   *
   * @param imageId the image ID
   * @param tagId the image tag ID
   * @throws NotFoundException if image tag not found
   */
  @Transactional
  public void removeImageTag(Long imageId, Long tagId) {
    ImageTag imageTag =
        imageTagRepository
            .findById(tagId)
            .orElseThrow(() -> new NotFoundException("Image tag not found with id: " + tagId));

    if (!imageTag.getImage().getId().equals(imageId)) {
      throw new IllegalArgumentException("Tag does not belong to this image");
    }

    imageTagRepository.delete(imageTag);
    log.info("Removed tag {} from image {}", tagId, imageId);
  }

  /**
   * Update image tags in batch (replace all tags)
   *
   * @param imageId the image ID
   * @param tagIds list of project tag IDs
   * @param createdBy the user who updated the tags
   * @return list of updated image tags
   * @throws NotFoundException if image not found
   */
  @Transactional
  public List<ImageTag> updateImageTags(Long imageId, List<Long> tagIds, String createdBy) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    // Delete existing tags
    imageTagRepository.deleteByImage_Id(imageId);

    // Add new tags
    if (tagIds != null && !tagIds.isEmpty()) {
      for (Long tagId : tagIds) {
        ProjectTag projectTag =
            projectTagRepository
                .findById(tagId)
                .orElseThrow(
                    () -> new NotFoundException("Project tag not found with id: " + tagId));

        ImageTag imageTag = new ImageTag();
        imageTag.setImage(image);
        imageTag.setProjectTag(projectTag);
        imageTag.setCreatedBy(createdBy);
        imageTagRepository.save(imageTag);
      }
    }

    return imageTagRepository.findByImage_Id(imageId);
  }

  // ==================== Image Metadata Operations ====================

  /**
   * Get all metadata for an image
   *
   * @param imageId the image ID
   * @return list of image metadata
   * @throws NotFoundException if image not found
   */
  @Transactional(readOnly = true)
  public List<ImageMetadata> getImageMetadata(Long imageId) {
    if (!imageRepository.existsById(imageId)) {
      throw new NotFoundException("Image not found with id: " + imageId);
    }
    return imageMetadataRepository.findByImage_Id(imageId);
  }

  /**
   * Add metadata to an image
   *
   * @param imageId the image ID
   * @param metadataId the project metadata ID
   * @param value the metadata value
   * @param createdBy the user who created the metadata
   * @return the created image metadata
   * @throws NotFoundException if image or metadata not found
   */
  @Transactional
  public ImageMetadata addImageMetadata(
      Long imageId, Long metadataId, String value, String createdBy) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    ProjectMetadata projectMetadata =
        projectMetadataRepository
            .findById(metadataId)
            .orElseThrow(
                () -> new NotFoundException("Project metadata not found with id: " + metadataId));

    ImageMetadata imageMetadata = new ImageMetadata();
    imageMetadata.setImage(image);
    imageMetadata.setProjectMetadata(projectMetadata);
    imageMetadata.setValue(value);
    imageMetadata.setCreatedBy(createdBy);

    return imageMetadataRepository.save(imageMetadata);
  }

  /**
   * Update an image metadata value
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @param value the new value
   * @return the updated image metadata
   * @throws NotFoundException if image metadata not found
   */
  @Transactional
  public ImageMetadata updateImageMetadata(Long imageId, Long metadataId, String value) {
    ImageMetadata imageMetadata =
        imageMetadataRepository
            .findById(metadataId)
            .orElseThrow(
                () -> new NotFoundException("Image metadata not found with id: " + metadataId));

    if (!imageMetadata.getImage().getId().equals(imageId)) {
      throw new IllegalArgumentException("Metadata does not belong to this image");
    }

    imageMetadata.setValue(value);
    return imageMetadataRepository.save(imageMetadata);
  }

  /**
   * Remove metadata from an image
   *
   * @param imageId the image ID
   * @param metadataId the image metadata ID
   * @throws NotFoundException if image metadata not found
   */
  @Transactional
  public void removeImageMetadata(Long imageId, Long metadataId) {
    ImageMetadata imageMetadata =
        imageMetadataRepository
            .findById(metadataId)
            .orElseThrow(
                () -> new NotFoundException("Image metadata not found with id: " + metadataId));

    if (!imageMetadata.getImage().getId().equals(imageId)) {
      throw new IllegalArgumentException("Metadata does not belong to this image");
    }

    imageMetadataRepository.delete(imageMetadata);
    log.info("Removed metadata {} from image {}", metadataId, imageId);
  }

  /**
   * Update image metadata in batch (replace all metadata)
   *
   * @param imageId the image ID
   * @param metadataList list of metadata with metadataId and value
   * @param createdBy the user who updated the metadata
   * @return list of updated image metadata
   * @throws NotFoundException if image not found
   */
  @Transactional
  public List<ImageMetadata> updateImageMetadataBatch(
      Long imageId, List<MetadataInput> metadataList, String createdBy) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageId));

    // Delete existing metadata
    imageMetadataRepository.deleteByImage_Id(imageId);

    // Add new metadata
    if (metadataList != null && !metadataList.isEmpty()) {
      for (MetadataInput input : metadataList) {
        ProjectMetadata projectMetadata =
            projectMetadataRepository
                .findById(input.getMetadataId())
                .orElseThrow(
                    () ->
                        new NotFoundException(
                            "Project metadata not found with id: " + input.getMetadataId()));

        ImageMetadata imageMetadata = new ImageMetadata();
        imageMetadata.setImage(image);
        imageMetadata.setProjectMetadata(projectMetadata);
        imageMetadata.setValue(input.getValue());
        imageMetadata.setCreatedBy(createdBy);
        imageMetadataRepository.save(imageMetadata);
      }
    }

    return imageMetadataRepository.findByImage_Id(imageId);
  }

  /** DTO for metadata input */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetadataInput {
    private Long metadataId;
    private String value;
  }
}
