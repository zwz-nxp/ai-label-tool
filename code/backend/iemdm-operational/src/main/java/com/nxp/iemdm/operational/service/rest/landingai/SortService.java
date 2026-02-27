package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.shared.dto.landingai.ImageListItemDTO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for sorting images by various criteria. Supports sorting by upload time and label time.
 */
@Slf4j
@Service
public class SortService {

  /**
   * Sort images by the specified method.
   *
   * @param images the list of images to sort
   * @param sortMethod the sort method (upload_time_desc, upload_time_asc, label_time_desc,
   *     label_time_asc, name_asc, name_desc)
   * @return sorted list of images
   */
  public List<ImageListItemDTO> sortImages(List<ImageListItemDTO> images, String sortMethod) {
    if (images == null || images.isEmpty()) {
      return images;
    }

    log.debug("Sorting {} images by method: {}", images.size(), sortMethod);

    List<ImageListItemDTO> sortedImages = new ArrayList<>(images);

    switch (sortMethod) {
      case "upload_time_desc":
        sortedImages = sortByUploadTime(sortedImages, false);
        break;
      case "upload_time_asc":
        sortedImages = sortByUploadTime(sortedImages, true);
        break;
      case "label_time_desc":
        sortedImages = sortByLabelTime(sortedImages, false);
        break;
      case "label_time_asc":
        sortedImages = sortByLabelTime(sortedImages, true);
        break;
      case "name_asc":
        sortedImages = sortByName(sortedImages, true);
        break;
      case "name_desc":
        sortedImages = sortByName(sortedImages, false);
        break;
      default:
        log.warn("Unknown sort method: {}, defaulting to upload_time_desc", sortMethod);
        sortedImages = sortByUploadTime(sortedImages, false);
    }

    log.debug("Sorted {} images by method: {}", sortedImages.size(), sortMethod);
    return sortedImages;
  }

  /**
   * Sort images by upload time (created_at timestamp). For images with the same timestamp, sorts by
   * ID (descending) as a tiebreaker for deterministic ordering - higher IDs appear first.
   *
   * @param images the list of images to sort
   * @param ascending true for ascending order (oldest first), false for descending (newest first)
   * @return sorted list of images
   */
  public List<ImageListItemDTO> sortByUploadTime(List<ImageListItemDTO> images, boolean ascending) {
    Comparator<ImageListItemDTO> comparator;

    if (ascending) {
      // Ascending: oldest first (created_at ASC), then by ID DESC (higher IDs first)
      comparator =
          Comparator.comparing(
                  ImageListItemDTO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
              .thenComparing(ImageListItemDTO::getId, Comparator.reverseOrder());
    } else {
      // Descending: newest first (created_at DESC), then by ID DESC (higher IDs first)
      comparator =
          Comparator.comparing(
                  ImageListItemDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
              .thenComparing(ImageListItemDTO::getId, Comparator.reverseOrder());
    }

    return images.stream().sorted(comparator).collect(Collectors.toList());
  }

  /**
   * Sort images by last labeled time (most recent label timestamp). Images without labels are
   * treated as having no label time and placed at the end for descending (newest first) or at the
   * beginning for ascending (oldest first).
   *
   * @param images the list of images to sort
   * @param ascending true for ascending order (oldest first), false for descending (newest first)
   * @return sorted list of images
   */
  public List<ImageListItemDTO> sortByLabelTime(List<ImageListItemDTO> images, boolean ascending) {
    // Separate images with labels and without labels
    List<ImageListItemDTO> withLabels = new ArrayList<>();
    List<ImageListItemDTO> withoutLabels = new ArrayList<>();

    for (ImageListItemDTO image : images) {
      Instant labelTime = getLastLabelTime(image);
      if (labelTime != null) {
        withLabels.add(image);
        log.info("Image {} has labels, last label time: {}", image.getId(), labelTime);
      } else {
        withoutLabels.add(image);
        log.info(
            "Image {} has no labels (labels list size: {})",
            image.getId(),
            image.getLabels() != null ? image.getLabels().size() : 0);
      }
    }

    log.info(
        "Sorting by label time (ascending={}): {} with labels, {} without labels",
        ascending,
        withLabels.size(),
        withoutLabels.size());

    // Sort images with labels by label time, then by ID DESC (higher IDs first)
    Comparator<ImageListItemDTO> comparator;

    if (ascending) {
      // Ascending: oldest label first, then by ID DESC (higher IDs first)
      comparator =
          Comparator.comparing(this::getLastLabelTime, Comparator.naturalOrder())
              .thenComparing(ImageListItemDTO::getId, Comparator.reverseOrder());
    } else {
      // Descending: newest label first, then by ID DESC (higher IDs first)
      comparator =
          Comparator.comparing(this::getLastLabelTime, Comparator.reverseOrder())
              .thenComparing(ImageListItemDTO::getId, Comparator.reverseOrder());
    }

    List<ImageListItemDTO> sortedWithLabels =
        withLabels.stream().sorted(comparator).collect(Collectors.toList());

    // Sort unlabeled images by ID DESC (higher IDs first) for consistent order
    List<ImageListItemDTO> sortedWithoutLabels =
        withoutLabels.stream()
            .sorted(Comparator.comparing(ImageListItemDTO::getId, Comparator.reverseOrder()))
            .collect(Collectors.toList());

    // Combine based on sort direction:
    // - Descending (newest first): labeled images first, then unlabeled at the end
    // - Ascending (oldest first): unlabeled images first, then labeled images
    List<ImageListItemDTO> result = new ArrayList<>();
    if (ascending) {
      // Oldest first: unlabeled (no label time = oldest) first, then labeled sorted oldest to
      // newest
      result.addAll(sortedWithoutLabels);
      result.addAll(sortedWithLabels);
    } else {
      // Newest first: labeled sorted newest to oldest, then unlabeled at the end
      result.addAll(sortedWithLabels);
      result.addAll(sortedWithoutLabels);
    }

    log.info(
        "Sort result order (first 5): {}",
        result.stream()
            .limit(5)
            .map(
                img ->
                    "id="
                        + img.getId()
                        + ",labels="
                        + (img.getLabels() != null ? img.getLabels().size() : 0))
            .collect(Collectors.toList()));

    return result;
  }

  /**
   * Get the last label time for an image (most recent label created_at timestamp). Returns null if
   * the image has no labels.
   *
   * @param imageDTO the image DTO
   * @return the last label time, or null if no labels
   */
  private Instant getLastLabelTime(ImageListItemDTO imageDTO) {
    if (imageDTO.getLabels() == null || imageDTO.getLabels().isEmpty()) {
      return null;
    }

    // Find the most recent label timestamp
    return imageDTO.getLabels().stream()
        .map(label -> label.getCreatedAt())
        .filter(createdAt -> createdAt != null)
        .max(Comparator.naturalOrder())
        .orElse(null);
  }

  /**
   * Sort images by file name (case-insensitive). For images with the same name, sorts by ID
   * (descending) as a tiebreaker for deterministic ordering - higher IDs appear first.
   *
   * @param images the list of images to sort
   * @param ascending true for ascending order (A-Z), false for descending (Z-A)
   * @return sorted list of images
   */
  public List<ImageListItemDTO> sortByName(List<ImageListItemDTO> images, boolean ascending) {
    Comparator<ImageListItemDTO> comparator;

    if (ascending) {
      // Ascending: A-Z, then by ID DESC (higher IDs first)
      comparator =
          Comparator.comparing(
                  ImageListItemDTO::getFileName,
                  Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
              .thenComparing(ImageListItemDTO::getId, Comparator.reverseOrder());
    } else {
      // Descending: Z-A, then by ID DESC (higher IDs first)
      comparator =
          Comparator.comparing(
                  ImageListItemDTO::getFileName,
                  Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER.reversed()))
              .thenComparing(ImageListItemDTO::getId, Comparator.reverseOrder());
    }

    return images.stream().sorted(comparator).collect(Collectors.toList());
  }
}
