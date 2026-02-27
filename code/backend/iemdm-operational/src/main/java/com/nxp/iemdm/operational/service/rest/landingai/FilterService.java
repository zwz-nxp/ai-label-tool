package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for filtering images based on various criteria. Supports multiple simultaneous filters.
 */
@Slf4j
@Service
public class FilterService {

  /**
   * Apply filters to a list of images. All filter criteria are applied with AND logic.
   *
   * @param images List of images to filter
   * @param filters Filter criteria
   * @return Filtered list of images
   */
  public List<Image> applyFilters(List<Image> images, ImageFilterRequest filters) {
    if (filters == null || !filters.hasFilters()) {
      return images;
    }

    log.info("Applying filters to {} images: {}", images.size(), filters);
    log.info("Split filter values: {}", filters.getSplit());

    List<Image> result =
        images.stream()
            .filter(image -> matchesAllFilters(image, filters))
            .collect(Collectors.toList());

    log.info("After filtering: {} images remain", result.size());
    return result;
  }

  /**
   * Check if an image matches all filter criteria.
   *
   * @param image Image to check
   * @param filters Filter criteria
   * @return true if image matches all filters
   */
  private boolean matchesAllFilters(Image image, ImageFilterRequest filters) {
    boolean matchesMediaStatus = matchesMediaStatus(image, filters.getMediaStatus());
    boolean matchesNoClass = matchesNoClass(image, filters.getNoClass());
    boolean matchesGroundTruth = matchesGroundTruthLabels(image, filters.getGroundTruthLabels());
    boolean matchesPrediction = matchesPredictionLabels(image, filters.getPredictionLabels());
    boolean matchesAnnotation = matchesAnnotationType(image, filters.getAnnotationType());
    boolean matchesSplitResult = matchesSplit(image, filters.getSplit());
    boolean matchesTagsResult = matchesTags(image, filters.getTags());
    boolean matchesMediaNameResult = matchesMediaName(image, filters.getMediaName());
    boolean matchesLabelerResult = matchesLabeler(image, filters.getLabeler());
    boolean matchesMediaIdResult = matchesMediaId(image, filters.getMediaId());
    boolean matchesMetadataResult = matchesMetadata(image, filters.getMetadata());

    if (filters.getSplit() != null && !filters.getSplit().isEmpty()) {
      log.debug(
          "Image {} split filter result: {} (image split: '{}')",
          image.getId(),
          matchesSplitResult,
          image.getSplit());
    }

    return matchesMediaStatus
        && matchesNoClass
        && matchesGroundTruth
        && matchesPrediction
        && matchesAnnotation
        && matchesSplitResult
        && matchesTagsResult
        && matchesMediaNameResult
        && matchesLabelerResult
        && matchesMediaIdResult
        && matchesMetadataResult;
  }

  /**
   * Check if image matches media status filter. Media status can be: "labeled", "unlabeled"
   *
   * <p>Logic: - Labeled: is_labeled=true - Unlabeled: is_labeled=false
   *
   * @param image Image to check
   * @param statuses List of acceptable statuses
   * @return true if image matches any of the statuses
   */
  public boolean matchesMediaStatus(Image image, List<String> statuses) {
    if (statuses == null || statuses.isEmpty()) {
      return true;
    }

    Boolean isLabeled = image.getIsLabeled();

    // Default to false if null
    boolean labeled = Boolean.TRUE.equals(isLabeled);

    for (String status : statuses) {
      switch (status.toLowerCase()) {
        case "labeled":
          // Labeled: is_labeled=true
          if (labeled) {
            return true;
          }
          break;
        case "unlabeled":
          // Unlabeled: is_labeled=false
          if (!labeled) {
            return true;
          }
          break;
      }
    }
    return false;
  }

  /**
   * Check if image matches No Class filter. No Class: is_labeled=true AND is_no_class=true
   *
   * @param image Image to check
   * @param noClass Whether to filter for No Class images
   * @return true if filter is not active or image matches No Class criteria
   */
  public boolean matchesNoClass(Image image, Boolean noClass) {
    if (noClass == null || !noClass) {
      return true;
    }

    Boolean isLabeled = image.getIsLabeled();
    Boolean isNoClassFlag = image.getIsNoClass();

    // No Class: is_labeled=true AND is_no_class=true
    return Boolean.TRUE.equals(isLabeled) && Boolean.TRUE.equals(isNoClassFlag);
  }

  /**
   * Check if image has ground truth labels matching the filter. Note: This requires the image to
   * have labels loaded (fetch join).
   *
   * @param image Image to check
   * @param classIds List of class IDs to match
   * @return true if image has any of the specified ground truth labels
   */
  public boolean matchesGroundTruthLabels(Image image, List<Long> classIds) {
    if (classIds == null || classIds.isEmpty()) {
      return true;
    }

    // This would require labels to be loaded
    // For now, we'll return true and handle this in the repository query
    log.debug("Ground truth label filtering should be handled at repository level");
    return true;
  }

  /**
   * Check if image has prediction labels matching the filter. Note: This requires the image to have
   * labels loaded (fetch join).
   *
   * @param image Image to check
   * @param classIds List of class IDs to match
   * @return true if image has any of the specified prediction labels
   */
  public boolean matchesPredictionLabels(Image image, List<Long> classIds) {
    if (classIds == null || classIds.isEmpty()) {
      return true;
    }

    // This would require labels to be loaded
    // For now, we'll return true and handle this in the repository query
    log.debug("Prediction label filtering should be handled at repository level");
    return true;
  }

  /**
   * Check if image has labels matching the annotation type filter. Annotation type can be: "Ground
   * truth", "Prediction" Note: This requires the image to have labels loaded (fetch join).
   *
   * @param image Image to check
   * @param annotationType Annotation type to match
   * @return true if image has labels of the specified type
   */
  public boolean matchesAnnotationType(Image image, String annotationType) {
    if (annotationType == null || annotationType.isEmpty()) {
      return true;
    }

    // This would require labels to be loaded
    // For now, we'll return true and handle this in the repository query
    log.debug("Annotation type filtering should be handled at repository level");
    return true;
  }

  /**
   * Check if image matches split filter. NOTE: Split filtering is now done at the database level in
   * ImageServiceImpl. This method is kept for backwards compatibility but always returns true.
   *
   * @param image Image to check
   * @param splits List of acceptable splits (unassigned, training, dev, test)
   * @return true (split filtering is done at DB level)
   */
  public boolean matchesSplit(Image image, List<String> splits) {
    // Split filtering is now done at the database level for better performance
    // This method always returns true to avoid double-filtering
    return true;
  }

  /**
   * Check if image has tags matching the filter. Note: This requires the image to have tags loaded
   * (fetch join).
   *
   * @param image Image to check
   * @param tagIds List of tag IDs to match
   * @return true if image has any of the specified tags
   */
  public boolean matchesTags(Image image, List<Long> tagIds) {
    if (tagIds == null || tagIds.isEmpty()) {
      return true;
    }

    // This would require tags to be loaded
    // For now, we'll return true and handle this in the repository query
    log.debug("Tag filtering should be handled at repository level");
    return true;
  }

  /**
   * Check if image file name matches the filter (partial match).
   *
   * @param image Image to check
   * @param mediaName Name to search for
   * @return true if image file name contains the search string
   */
  public boolean matchesMediaName(Image image, String mediaName) {
    if (mediaName == null || mediaName.isEmpty()) {
      return true;
    }

    return image.getFileName() != null
        && image.getFileName().toLowerCase().contains(mediaName.toLowerCase());
  }

  /**
   * Check if image was labeled by the specified user. Note: This requires checking the labels'
   * createdBy field.
   *
   * @param image Image to check
   * @param labeler Username to match
   * @return true if image has labels created by the specified user
   */
  public boolean matchesLabeler(Image image, String labeler) {
    if (labeler == null || labeler.isEmpty()) {
      return true;
    }

    // This would require labels to be loaded
    // For now, we'll return true and handle this in the repository query
    log.debug("Labeler filtering should be handled at repository level");
    return true;
  }

  /**
   * Check if image ID matches the filter.
   *
   * @param image Image to check
   * @param mediaId ID to match
   * @return true if image ID matches
   */
  public boolean matchesMediaId(Image image, String mediaId) {
    if (mediaId == null || mediaId.isEmpty()) {
      return true;
    }

    return image.getId() != null && image.getId().toString().equals(mediaId);
  }

  /**
   * Check if image metadata matches the filter. Note: This requires the image to have metadata
   * loaded (fetch join).
   *
   * @param image Image to check
   * @param metadata Map of metadata key-value pairs to match
   * @return true if image has all specified metadata values
   */
  public boolean matchesMetadata(Image image, Map<String, String> metadata) {
    if (metadata == null || metadata.isEmpty()) {
      return true;
    }

    // This would require metadata to be loaded
    // For now, we'll return true and handle this in the repository query
    log.debug("Metadata filtering should be handled at repository level");
    return true;
  }
}
