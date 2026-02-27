package com.nxp.iemdm.operational.service.rest.landingai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nxp.iemdm.model.landingai.*;
import com.nxp.iemdm.shared.dto.landingai.CreateProjectFromSnapshotRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.SnapshotDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotPreviewStatsDTO;
import com.nxp.iemdm.shared.exception.landingai.DuplicateSnapshotNameException;
import com.nxp.iemdm.shared.exception.landingai.ProjectCreationException;
import com.nxp.iemdm.shared.exception.landingai.SnapshotNotFoundException;
import com.nxp.iemdm.shared.exception.landingai.SnapshotRevertException;
import com.nxp.iemdm.shared.repository.jpa.landingai.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operational/landingai/snapshots")
@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotService {

  private final EntityManager entityManager;
  private final SnapshotRepository snapshotRepository;
  private final ProjectRepository projectRepository;
  private final ImageRepository imageRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ImageTagRepository imageTagRepository;
  private final ImageMetadataRepository imageMetadataRepository;
  private final ImageFileRepository imageFileRepository;
  private final ProjectClassRepository projectClassRepository;
  private final ProjectTagRepository projectTagRepository;
  private final ProjectSplitRepository projectSplitRepository;
  private final ProjectMetadataRepository projectMetadataRepository;

  // Snapshot data repositories
  private final SnapshotImageRepository snapshotImageRepository;
  private final SnapshotImageLabelRepository snapshotImageLabelRepository;
  private final SnapshotImageTagRepository snapshotImageTagRepository;
  private final SnapshotImageMetadataRepository snapshotImageMetadataRepository;
  private final SnapshotProjectClassRepository snapshotProjectClassRepository;
  private final SnapshotProjectTagRepository snapshotProjectTagRepository;
  private final SnapshotProjectSplitRepository snapshotProjectSplitRepository;
  private final SnapshotProjectMetadataRepository snapshotProjectMetadataRepository;

  // Export service
  private final DatasetExportService datasetExportService;

  /**
   * Create a new snapshot for a project. Note: The database trigger tgf_do_snapshot automatically
   * copies all project data to the _ss tables when a snapshot record is inserted.
   */
  @PostMapping
  @Transactional
  public SnapshotDTO createSnapshot(
      @RequestBody SnapshotCreateRequest request, @RequestParam("userId") String userId) {
    log.info(
        "Creating snapshot '{}' for project ID: {}",
        request.getSnapshotName(),
        request.getProjectId());

    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Project not found with ID: " + request.getProjectId()));

    if (snapshotRepository.existsByProjectIdAndSnapshotName(
        request.getProjectId(), request.getSnapshotName())) {
      throw new DuplicateSnapshotNameException(
          "Snapshot name '" + request.getSnapshotName() + "' already exists");
    }

    Snapshot snapshot = new Snapshot();
    snapshot.setProject(project);
    snapshot.setSnapshotName(request.getSnapshotName());
    snapshot.setDescription(request.getDescription());
    snapshot.setCreatedBy(userId);

    // When saved, the database trigger will automatically copy project data to _ss tables
    Snapshot savedSnapshot = snapshotRepository.save(snapshot);
    log.info("Snapshot created with ID: {}", savedSnapshot.getId());
    return convertToDTO(savedSnapshot);
  }

  @GetMapping("/project/{projectId}")
  @Transactional(readOnly = true)
  public List<SnapshotDTO> getSnapshotsForProject(@PathVariable("projectId") Long projectId) {
    log.info("Retrieving snapshots for project ID: {}", projectId);
    List<Snapshot> snapshots = snapshotRepository.findByProjectId(projectId);
    return snapshots.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public SnapshotDTO getSnapshotById(@PathVariable("id") Long snapshotId) {
    log.info("Retrieving snapshot with ID: {}", snapshotId);
    Snapshot snapshot =
        snapshotRepository
            .findById(snapshotId)
            .orElseThrow(
                () -> new EntityNotFoundException("Snapshot not found with ID: " + snapshotId));
    return convertToDTO(snapshot);
  }

  /** Delete a snapshot and all its associated data from _ss tables. Requirements: 7.3 */
  @DeleteMapping("/{id}")
  @Transactional
  public void deleteSnapshot(
      @PathVariable("id") Long snapshotId, @RequestParam("userId") String userId) {
    log.info("Deleting snapshot with ID: {} by user: {}", snapshotId, userId);
    if (!snapshotRepository.existsById(snapshotId)) {
      throw new SnapshotNotFoundException(snapshotId);
    }
    // Delete snapshot data from _ss tables first
    deleteSnapshotData(snapshotId);
    // Delete the snapshot record
    snapshotRepository.deleteById(snapshotId);
    log.info("Snapshot deleted with ID: {}", snapshotId);
  }

  @GetMapping("/project/{projectId}/preview-stats")
  @Transactional(readOnly = true)
  public SnapshotPreviewStatsDTO getSnapshotPreviewStats(
      @PathVariable("projectId") Long projectId) {
    log.info("Getting snapshot preview stats for project ID: {}", projectId);
    long labeledCount =
        imageRepository.countByProject_IdAndIsLabeledTrueAndIsNoClassFalse(projectId);
    long unlabeledCount = imageRepository.countByProject_IdAndIsLabeledFalse(projectId);
    long noClassCount =
        imageRepository.countByProject_IdAndIsLabeledTrueAndIsNoClassTrue(projectId);
    long trainCount = imageRepository.countByProject_IdAndSplitIgnoreCase(projectId, "TRAINING");
    long devCount = imageRepository.countByProject_IdAndSplitIgnoreCase(projectId, "DEV");
    long testCount = imageRepository.countByProject_IdAndSplitIgnoreCase(projectId, "TEST");
    long unassignedCount =
        imageRepository.countByProject_IdAndSplitIsNullOrSplitEquals(projectId, "");

    SnapshotPreviewStatsDTO stats = new SnapshotPreviewStatsDTO();
    stats.setLabeled((int) labeledCount);
    stats.setUnlabeled((int) unlabeledCount);
    stats.setNoClass((int) noClassCount);
    stats.setTrainCount((int) trainCount);
    stats.setDevCount((int) devCount);
    stats.setTestCount((int) testCount);
    stats.setUnassignedCount((int) unassignedCount);
    return stats;
  }

  /**
   * Get paginated images for a snapshot with optional sorting and filtering. Requirements: 1.1,
   * 3.1, 3.2
   *
   * @param snapshotId the snapshot ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param sortBy the sort method
   * @param filterRequest the filter criteria (optional)
   * @return paginated response with image list items
   */
  @GetMapping("/{snapshotId}/images")
  @Transactional(readOnly = true)
  public Map<String, Object> getSnapshotImages(
      @PathVariable("snapshotId") Long snapshotId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "sortBy", defaultValue = "upload_time_desc") String sortBy,
      @RequestBody(required = false)
          com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filterRequest) {
    return getSnapshotImagesInternal(snapshotId, page, size, sortBy, filterRequest);
  }

  /**
   * Get paginated images for a snapshot with optional filtering and sorting (POST version).
   * Requirements: 1.1, 3.1, 3.2
   *
   * <p>This POST endpoint is preferred for complex filter requests as it properly handles request
   * body data.
   *
   * @param snapshotId the snapshot ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param sortBy the sort method
   * @param filterRequest the filter criteria (optional, sent in request body)
   * @return paginated response with image list items
   */
  @PostMapping("/{snapshotId}/images/search")
  @Transactional(readOnly = true)
  public Map<String, Object> getSnapshotImagesPost(
      @PathVariable("snapshotId") Long snapshotId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "sortBy", defaultValue = "upload_time_desc") String sortBy,
      @RequestBody(required = false)
          com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filterRequest) {
    return getSnapshotImagesInternal(snapshotId, page, size, sortBy, filterRequest);
  }

  /**
   * Internal method to get snapshot images with filtering and sorting. Shared by both GET and POST
   * endpoints.
   */
  private Map<String, Object> getSnapshotImagesInternal(
      Long snapshotId,
      int page,
      int size,
      String sortBy,
      com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filterRequest) {
    log.info(
        "Getting images for snapshot ID: {} (page: {}, size: {}, sortBy: {}, filters: {})",
        snapshotId,
        page,
        size,
        sortBy,
        filterRequest);

    // Validate snapshot exists
    if (!snapshotRepository.existsById(snapshotId)) {
      throw new SnapshotNotFoundException(snapshotId);
    }

    // Get all snapshot images from _ss table
    List<SnapshotImage> snapshotImages = snapshotImageRepository.findBySnapshotId(snapshotId);
    log.info("Found {} images for snapshot ID: {}", snapshotImages.size(), snapshotId);

    // Load all labels for this snapshot once (performance optimization)
    List<SnapshotImageLabel> allSnapshotLabels =
        snapshotImageLabelRepository.findBySnapshotId(snapshotId);
    log.info("Found {} labels for snapshot ID: {}", allSnapshotLabels.size(), snapshotId);

    // Load all tags for this snapshot once
    List<SnapshotImageTag> allSnapshotTags =
        snapshotImageTagRepository.findBySnapshotId(snapshotId);
    log.info("Found {} image tags for snapshot ID: {}", allSnapshotTags.size(), snapshotId);

    // Load all metadata for this snapshot once
    List<SnapshotImageMetadata> allSnapshotMetadata =
        snapshotImageMetadataRepository.findBySnapshotId(snapshotId);
    log.info("Found {} image metadata for snapshot ID: {}", allSnapshotMetadata.size(), snapshotId);

    // Load all classes for this snapshot to enrich labels with class info
    List<SnapshotProjectClass> allSnapshotClasses =
        snapshotProjectClassRepository.findBySnapshotId(snapshotId);
    log.info("Found {} classes for snapshot ID: {}", allSnapshotClasses.size(), snapshotId);

    // Group labels, tags, and metadata by image ID for efficient lookup
    Map<Long, List<SnapshotImageLabel>> labelsByImageId =
        allSnapshotLabels.stream().collect(Collectors.groupingBy(SnapshotImageLabel::getImageId));
    Map<Long, List<SnapshotImageTag>> tagsByImageId =
        allSnapshotTags.stream().collect(Collectors.groupingBy(SnapshotImageTag::getImageId));
    Map<Long, List<SnapshotImageMetadata>> metadataByImageId =
        allSnapshotMetadata.stream()
            .collect(Collectors.groupingBy(SnapshotImageMetadata::getImageId));

    // Create a map of class ID to class info for efficient lookup
    Map<Long, SnapshotProjectClass> classesById =
        allSnapshotClasses.stream().collect(Collectors.toMap(SnapshotProjectClass::getId, c -> c));

    // Convert to DTOs with thumbnail, labels, tags, and metadata
    List<Map<String, Object>> imageDTOs = new ArrayList<>();
    for (SnapshotImage snapshotImage : snapshotImages) {
      Map<String, Object> imageDTO = new HashMap<>();
      imageDTO.put("id", snapshotImage.getId());
      imageDTO.put("fileName", snapshotImage.getFileName());
      imageDTO.put("fileSize", snapshotImage.getFileSize());
      imageDTO.put("width", snapshotImage.getWidth());
      imageDTO.put("height", snapshotImage.getHeight());
      imageDTO.put("split", snapshotImage.getSplit());
      imageDTO.put("isLabeled", snapshotImage.getIsLabeled());
      imageDTO.put("isNoClass", snapshotImage.getIsNoClass());
      imageDTO.put("createdAt", snapshotImage.getCreatedAt());

      // Include thumbnail image data
      imageDTO.put("thumbnailImage", snapshotImage.getThumbnailImage());
      imageDTO.put("thumbnailWidthRatio", snapshotImage.getThumbnailWidthRatio());
      imageDTO.put("thumbnailHeightRatio", snapshotImage.getThumbnailHeightRatio());

      // Get labels for this specific image from the grouped map
      List<SnapshotImageLabel> snapshotLabels =
          labelsByImageId.getOrDefault(snapshotImage.getId(), Collections.emptyList());

      List<Map<String, Object>> labels = new ArrayList<>();
      for (SnapshotImageLabel label : snapshotLabels) {
        Map<String, Object> labelDTO = new HashMap<>();
        labelDTO.put("id", label.getId());
        labelDTO.put("classId", label.getClassId());
        labelDTO.put("position", label.getPosition());

        // Enrich with class information from snapshot classes
        SnapshotProjectClass projectClass = classesById.get(label.getClassId());
        if (projectClass != null) {
          labelDTO.put("className", projectClass.getClassName());
          labelDTO.put("colorCode", projectClass.getColorCode());
        }

        labels.add(labelDTO);
      }
      imageDTO.put("labels", labels);
      imageDTO.put("labelCount", labels.size());

      // Get tags for this specific image
      List<SnapshotImageTag> snapshotImageTags =
          tagsByImageId.getOrDefault(snapshotImage.getId(), Collections.emptyList());

      List<Map<String, Object>> tags = new ArrayList<>();
      for (SnapshotImageTag tag : snapshotImageTags) {
        Map<String, Object> tagDTO = new HashMap<>();
        tagDTO.put("id", tag.getId());
        tagDTO.put("tagId", tag.getTagId());
        tags.add(tagDTO);
      }
      imageDTO.put("tags", tags);

      // Get metadata for this specific image
      List<SnapshotImageMetadata> snapshotImageMetadata =
          metadataByImageId.getOrDefault(snapshotImage.getId(), Collections.emptyList());

      List<Map<String, Object>> metadata = new ArrayList<>();
      for (SnapshotImageMetadata meta : snapshotImageMetadata) {
        Map<String, Object> metaDTO = new HashMap<>();
        metaDTO.put("id", meta.getId());
        metaDTO.put("metadataId", meta.getMetadataId());
        metaDTO.put("value", meta.getValue());
        metadata.add(metaDTO);
      }
      imageDTO.put("metadata", metadata);

      imageDTOs.add(imageDTO);
    }

    // Apply filters if provided
    if (filterRequest != null && filterRequest.hasFilters()) {
      imageDTOs = applyFilters(imageDTOs, filterRequest);
      log.info("After filtering: {} images remain", imageDTOs.size());
    }

    // Apply sorting
    imageDTOs.sort(
        (a, b) -> {
          switch (sortBy) {
            case "upload_time_desc":
              return ((java.time.Instant) b.get("createdAt"))
                  .compareTo((java.time.Instant) a.get("createdAt"));
            case "upload_time_asc":
              return ((java.time.Instant) a.get("createdAt"))
                  .compareTo((java.time.Instant) b.get("createdAt"));
            case "label_time_desc":
              // TODO: Implement label time sorting when labels are loaded
              return ((java.time.Instant) b.get("createdAt"))
                  .compareTo((java.time.Instant) a.get("createdAt"));
            case "label_time_asc":
              // TODO: Implement label time sorting when labels are loaded
              return ((java.time.Instant) a.get("createdAt"))
                  .compareTo((java.time.Instant) b.get("createdAt"));
            case "name_asc":
              return ((String) a.get("fileName")).compareToIgnoreCase((String) b.get("fileName"));
            case "name_desc":
              return ((String) b.get("fileName")).compareToIgnoreCase((String) a.get("fileName"));
            default:
              return ((java.time.Instant) b.get("createdAt"))
                  .compareTo((java.time.Instant) a.get("createdAt"));
          }
        });

    // Apply pagination
    int totalElements = imageDTOs.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    int startIndex = page * size;
    int endIndex = Math.min(startIndex + size, totalElements);

    List<Map<String, Object>> pageContent = imageDTOs.subList(startIndex, endIndex);

    // Build paginated response
    Map<String, Object> response = new HashMap<>();
    response.put("content", pageContent);
    response.put("page", page);
    response.put("size", size);
    response.put("totalElements", totalElements);
    response.put("totalPages", totalPages);
    response.put("first", page == 0);
    response.put("last", page >= totalPages - 1);

    return response;
  }

  /**
   * Apply filters to snapshot image DTOs Filters images based on various criteria like media
   * status, labels, split, tags, etc.
   *
   * @param imageDTOs the list of image DTOs to filter
   * @param filterRequest the filter criteria
   * @return filtered list of image DTOs
   */
  private List<Map<String, Object>> applyFilters(
      List<Map<String, Object>> imageDTOs,
      com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filterRequest) {

    return imageDTOs.stream()
        .filter(
            imageDTO -> {
              // Filter by media status (labeled/unlabeled)
              if (filterRequest.getMediaStatus() != null
                  && !filterRequest.getMediaStatus().isEmpty()) {
                Boolean isLabeled = (Boolean) imageDTO.get("isLabeled");
                // Handle null values - treat null as false
                boolean isLabeledValue = isLabeled != null && isLabeled;
                boolean matchesStatus = false;
                for (String status : filterRequest.getMediaStatus()) {
                  if ("labeled".equalsIgnoreCase(status) && isLabeledValue) {
                    matchesStatus = true;
                    break;
                  } else if ("unlabeled".equalsIgnoreCase(status) && !isLabeledValue) {
                    matchesStatus = true;
                    break;
                  }
                }
                if (!matchesStatus) {
                  return false;
                }
              }

              // Filter by ground truth labels (class IDs)
              if (filterRequest.getGroundTruthLabels() != null
                  && !filterRequest.getGroundTruthLabels().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> labels =
                    (List<Map<String, Object>>) imageDTO.get("labels");
                boolean hasMatchingLabel =
                    labels.stream()
                        .anyMatch(
                            label ->
                                filterRequest
                                    .getGroundTruthLabels()
                                    .contains(((Number) label.get("classId")).longValue()));
                if (!hasMatchingLabel) {
                  return false;
                }
              }

              // Filter by split (training/dev/test)
              if (filterRequest.getSplit() != null && !filterRequest.getSplit().isEmpty()) {
                String imageSplit = (String) imageDTO.get("split");
                boolean matchesSplit =
                    filterRequest.getSplit().stream()
                        .anyMatch(
                            split -> {
                              if (split == null || split.isEmpty()) {
                                return imageSplit == null || imageSplit.isEmpty();
                              }
                              return split.equalsIgnoreCase(imageSplit);
                            });
                if (!matchesSplit) {
                  return false;
                }
              }

              // Filter by tags
              if (filterRequest.getTags() != null && !filterRequest.getTags().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> imageTags =
                    (List<Map<String, Object>>) imageDTO.get("tags");
                boolean hasMatchingTag =
                    imageTags.stream()
                        .anyMatch(
                            tag ->
                                filterRequest
                                    .getTags()
                                    .contains(((Number) tag.get("tagId")).longValue()));
                if (!hasMatchingTag) {
                  return false;
                }
              }

              // Filter by media name (partial match, case-insensitive)
              if (filterRequest.getMediaName() != null && !filterRequest.getMediaName().isEmpty()) {
                String fileName = (String) imageDTO.get("fileName");
                if (fileName == null
                    || !fileName
                        .toLowerCase()
                        .contains(filterRequest.getMediaName().toLowerCase())) {
                  return false;
                }
              }

              // Filter by media ID (exact match)
              if (filterRequest.getMediaId() != null && !filterRequest.getMediaId().isEmpty()) {
                Long imageId = ((Number) imageDTO.get("id")).longValue();
                try {
                  Long filterMediaId = Long.parseLong(filterRequest.getMediaId());
                  if (!imageId.equals(filterMediaId)) {
                    return false;
                  }
                } catch (NumberFormatException e) {
                  // If media ID is not a valid number, no match
                  return false;
                }
              }

              // Filter by No Class (is_labeled=true AND is_no_class=true)
              if (filterRequest.getNoClass() != null && filterRequest.getNoClass()) {
                Boolean isLabeled = (Boolean) imageDTO.get("isLabeled");
                Boolean isNoClass = (Boolean) imageDTO.get("isNoClass");
                // Handle null values - treat null as false
                boolean isLabeledValue = isLabeled != null && isLabeled;
                boolean isNoClassValue = isNoClass != null && isNoClass;
                if (!(isLabeledValue && isNoClassValue)) {
                  return false;
                }
              }

              // Filter by metadata (key-value pairs)
              if (filterRequest.getMetadata() != null && !filterRequest.getMetadata().isEmpty()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> imageMetadata =
                    (List<Map<String, Object>>) imageDTO.get("metadata");

                // Check if all requested metadata filters match
                for (Map.Entry<String, String> metadataFilter :
                    filterRequest.getMetadata().entrySet()) {
                  Long metadataId = Long.parseLong(metadataFilter.getKey());
                  String expectedValue = metadataFilter.getValue();

                  boolean hasMatchingMetadata =
                      imageMetadata.stream()
                          .anyMatch(
                              meta -> {
                                Long metaId = ((Number) meta.get("metadataId")).longValue();
                                String metaValue = (String) meta.get("value");
                                return metaId.equals(metadataId) && expectedValue.equals(metaValue);
                              });

                  if (!hasMatchingMetadata) {
                    return false;
                  }
                }
              }

              // All filters passed
              return true;
            })
        .collect(Collectors.toList());
  }

  /**
   * Get project classes from snapshot
   *
   * @param snapshotId the snapshot ID
   * @return list of project classes from snapshot
   */
  @GetMapping("/{snapshotId}/classes")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getSnapshotClasses(@PathVariable("snapshotId") Long snapshotId) {
    log.info("Getting classes for snapshot ID: {}", snapshotId);

    if (!snapshotRepository.existsById(snapshotId)) {
      throw new SnapshotNotFoundException(snapshotId);
    }

    List<SnapshotProjectClass> classes =
        snapshotProjectClassRepository.findBySnapshotId(snapshotId);
    return classes.stream()
        .map(
            c -> {
              Map<String, Object> classDTO = new HashMap<>();
              classDTO.put("id", c.getId());
              classDTO.put("className", c.getClassName());
              classDTO.put("description", c.getDescription());
              classDTO.put("colorCode", c.getColorCode());
              classDTO.put("sequence", c.getSequence());
              classDTO.put("createdBy", c.getCreatedBy());
              return classDTO;
            })
        .collect(Collectors.toList());
  }

  /**
   * Get project tags from snapshot
   *
   * @param snapshotId the snapshot ID
   * @return list of project tags from snapshot
   */
  @GetMapping("/{snapshotId}/tags")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getSnapshotTags(@PathVariable("snapshotId") Long snapshotId) {
    log.info("Getting tags for snapshot ID: {}", snapshotId);

    if (!snapshotRepository.existsById(snapshotId)) {
      throw new SnapshotNotFoundException(snapshotId);
    }

    List<SnapshotProjectTag> tags = snapshotProjectTagRepository.findBySnapshotId(snapshotId);
    return tags.stream()
        .map(
            t -> {
              Map<String, Object> tagDTO = new HashMap<>();
              tagDTO.put("id", t.getId());
              tagDTO.put("name", t.getName());
              tagDTO.put("createdBy", t.getCreatedBy());
              return tagDTO;
            })
        .collect(Collectors.toList());
  }

  /**
   * Get project metadata from snapshot
   *
   * @param snapshotId the snapshot ID
   * @return list of project metadata from snapshot
   */
  @GetMapping("/{snapshotId}/metadata")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getSnapshotMetadata(
      @PathVariable("snapshotId") Long snapshotId) {
    log.info("Getting metadata for snapshot ID: {}", snapshotId);

    if (!snapshotRepository.existsById(snapshotId)) {
      throw new SnapshotNotFoundException(snapshotId);
    }

    List<SnapshotProjectMetadata> metadata =
        snapshotProjectMetadataRepository.findBySnapshotId(snapshotId);
    return metadata.stream()
        .map(
            m -> {
              Map<String, Object> metadataDTO = new HashMap<>();
              metadataDTO.put("id", m.getId());
              metadataDTO.put("name", m.getName());
              metadataDTO.put("type", m.getType());
              metadataDTO.put("valueFrom", m.getValueFrom());
              metadataDTO.put("predefinedValues", m.getPredefinedValues());
              metadataDTO.put("multipleValues", m.getMultipleValues());
              metadataDTO.put("createdBy", m.getCreatedBy());
              return metadataDTO;
            })
        .collect(Collectors.toList());
  }

  /**
   * Get project splits from snapshot
   *
   * @param snapshotId the snapshot ID
   * @return list of project splits from snapshot
   */
  @GetMapping("/{snapshotId}/splits")
  @Transactional(readOnly = true)
  public List<Map<String, Object>> getSnapshotSplits(@PathVariable("snapshotId") Long snapshotId) {
    log.info("Getting splits for snapshot ID: {}", snapshotId);

    if (!snapshotRepository.existsById(snapshotId)) {
      throw new SnapshotNotFoundException(snapshotId);
    }

    List<SnapshotProjectSplit> splits = snapshotProjectSplitRepository.findBySnapshotId(snapshotId);
    return splits.stream()
        .map(
            s -> {
              Map<String, Object> splitDTO = new HashMap<>();
              splitDTO.put("id", s.getId());
              splitDTO.put("classId", s.getClassId());
              splitDTO.put("trainRatio", s.getTrainRatio());
              splitDTO.put("devRatio", s.getDevRatio());
              splitDTO.put("testRatio", s.getTestRatio());
              return splitDTO;
            })
        .collect(Collectors.toList());
  }

  /**
   * Create a new project from a snapshot. Reads data from _ss tables and copies to new project
   * tables with new IDs. Requirements: 4.3, 4.4
   *
   * @param snapshotId the snapshot ID to create project from
   * @param request the request containing the new project name
   * @param userId the user creating the project
   * @return the created project DTO
   */
  @PostMapping("/{snapshotId}/create-project")
  @Transactional
  public ProjectDTO createProjectFromSnapshot(
      @PathVariable("snapshotId") Long snapshotId,
      @RequestBody CreateProjectFromSnapshotRequest request,
      @RequestParam("userId") String userId) {
    log.info(
        "Creating project '{}' from snapshot ID: {} by user: {}",
        request.getProjectName(),
        snapshotId,
        userId);

    try {
      // 1. Validate snapshot exists
      Snapshot snapshot =
          snapshotRepository
              .findById(snapshotId)
              .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

      Project sourceProject = snapshot.getProject();

      // 2. Create new project entity with properties from source project
      Project newProject = new Project();
      newProject.setName(request.getProjectName());
      newProject.setStatus(sourceProject.getStatus());
      newProject.setType(sourceProject.getType());
      newProject.setModelName(null); // New project starts without a model
      newProject.setGroupName(sourceProject.getGroupName());
      newProject.setLocation(sourceProject.getLocation());
      newProject.setCreatedBy(userId);
      newProject = projectRepository.save(newProject);
      log.info("Created new project with ID: {}", newProject.getId());

      // 3. Copy data from _ss tables to new project tables with ID mappings
      Map<Long, Long> classIdMapping = copySnapshotProjectClasses(snapshotId, newProject, userId);
      Map<Long, Long> tagIdMapping = copySnapshotProjectTags(snapshotId, newProject, userId);
      Map<Long, Long> metadataIdMapping =
          copySnapshotProjectMetadata(snapshotId, newProject, userId);
      copySnapshotProjectSplits(snapshotId, newProject, classIdMapping, userId);
      Map<Long, Long> imageIdMapping = copySnapshotImages(snapshotId, newProject, userId);
      copySnapshotImageLabels(snapshotId, imageIdMapping, classIdMapping, userId);
      copySnapshotImageTags(snapshotId, imageIdMapping, tagIdMapping, userId);
      copySnapshotImageMetadata(snapshotId, imageIdMapping, metadataIdMapping, userId);

      log.info(
          "Successfully created project '{}' from snapshot ID: {}",
          request.getProjectName(),
          snapshotId);
      return convertProjectToDTO(newProject);

    } catch (SnapshotNotFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to create project from snapshot: {}", e.getMessage(), e);
      throw new ProjectCreationException(
          "Failed to create project from snapshot: " + e.getMessage(), e);
    }
  }

  /**
   * Revert a project to a snapshot state. Creates a backup snapshot first (trigger auto-captures
   * current data), then deletes current project data and restores from snapshot _ss tables.
   * Requirements: 5.3, 5.4, 5.5, 5.6, 5.8
   *
   * @param snapshotId the snapshot ID to revert to
   * @param projectId the project ID to revert
   * @param userId the user performing the revert
   */
  @PostMapping("/{snapshotId}/revert")
  @Transactional
  public void revertProjectToSnapshot(
      @PathVariable("snapshotId") Long snapshotId,
      @RequestParam("projectId") Long projectId,
      @RequestParam("userId") String userId) {
    log.info(
        "Reverting project ID: {} to snapshot ID: {} by user: {}", projectId, snapshotId, userId);

    try {
      // Validate snapshot exists
      Snapshot snapshot =
          snapshotRepository
              .findById(snapshotId)
              .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

      // Validate project exists
      Project project =
          projectRepository
              .findById(projectId)
              .orElseThrow(
                  () -> new EntityNotFoundException("Project not found with ID: " + projectId));

      // Step 1: Create backup snapshot of current state
      // The database trigger will automatically capture current data
      Snapshot backupSnapshot = createBackupSnapshot(project, userId);
      log.info("Created backup snapshot with ID: {} before revert", backupSnapshot.getId());

      // CRITICAL: Flush to database to trigger the snapshot capture BEFORE deleting data
      // PostgreSQL triggers execute synchronously, so flush() blocks until trigger completes
      entityManager.flush();
      log.info("Flushed backup snapshot to database - trigger has captured current data");

      // Verify backup snapshot captured data (safety check)
      long backupImageCount = snapshotImageRepository.countBySnapshotId(backupSnapshot.getId());
      long currentImageCount = imageRepository.countByProjectId(projectId);
      log.info(
          "Backup snapshot verification: captured {} images (current project has {} images)",
          backupImageCount,
          currentImageCount);

      if (currentImageCount > 0 && backupImageCount == 0) {
        throw new SnapshotRevertException(
            "Backup snapshot failed to capture data. Aborting revert to prevent data loss.");
      }

      // Step 2: Delete current project data (respecting FK constraints)
      deleteProjectData(projectId);
      log.info("Deleted current project data for project ID: {}", projectId);

      // Step 3: Restore snapshot data from _ss tables to project tables
      restoreSnapshotData(snapshotId, project, userId);
      log.info("Restored snapshot data to project ID: {}", projectId);

      log.info("Successfully reverted project ID: {} to snapshot ID: {}", projectId, snapshotId);

    } catch (SnapshotNotFoundException | EntityNotFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to revert project to snapshot: {}", e.getMessage(), e);
      throw new SnapshotRevertException("Failed to revert to snapshot: " + e.getMessage(), e);
    }
  }

  /**
   * Download snapshot dataset as a ZIP archive. Queries snapshot data from _ss tables and creates a
   * ZIP with images and JSON metadata. Requirements: 6.3
   *
   * @param snapshotId the snapshot ID to download
   * @return byte array containing the ZIP archive
   */
  @GetMapping("/{snapshotId}/download")
  @Transactional(readOnly = true)
  public byte[] downloadSnapshotDataset(@PathVariable("snapshotId") Long snapshotId) {
    log.info("Downloading snapshot dataset for snapshot ID: {}", snapshotId);

    // Validate snapshot exists
    Snapshot snapshot =
        snapshotRepository
            .findById(snapshotId)
            .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(baos);

      // Get snapshot data
      List<SnapshotImage> images = snapshotImageRepository.findBySnapshotId(snapshotId);
      List<SnapshotImageLabel> labels = snapshotImageLabelRepository.findBySnapshotId(snapshotId);
      List<SnapshotImageTag> imageTags = snapshotImageTagRepository.findBySnapshotId(snapshotId);
      List<SnapshotImageMetadata> imageMetadata =
          snapshotImageMetadataRepository.findBySnapshotId(snapshotId);
      List<SnapshotProjectClass> classes =
          snapshotProjectClassRepository.findBySnapshotId(snapshotId);
      List<SnapshotProjectTag> tags = snapshotProjectTagRepository.findBySnapshotId(snapshotId);
      List<SnapshotProjectMetadata> metadata =
          snapshotProjectMetadataRepository.findBySnapshotId(snapshotId);

      // Create JSON metadata
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

      // Add metadata.json
      Map<String, Object> metadataJson = new HashMap<>();
      metadataJson.put("snapshotId", snapshotId);
      metadataJson.put("snapshotName", snapshot.getSnapshotName());
      metadataJson.put("description", snapshot.getDescription());
      metadataJson.put("createdAt", snapshot.getCreatedAt());
      metadataJson.put("createdBy", snapshot.getCreatedBy());
      metadataJson.put("projectId", snapshot.getProject().getId());
      metadataJson.put("projectName", snapshot.getProject().getName());
      metadataJson.put("projectType", snapshot.getProject().getType());
      metadataJson.put("imageCount", images.size());
      metadataJson.put("classCount", classes.size());

      ZipEntry metadataEntry = new ZipEntry("metadata.json");
      zos.putNextEntry(metadataEntry);
      zos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(metadataJson));
      zos.closeEntry();

      // Add classes.json
      ZipEntry classesEntry = new ZipEntry("classes.json");
      zos.putNextEntry(classesEntry);
      zos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(classes));
      zos.closeEntry();

      // Add tags.json
      ZipEntry tagsEntry = new ZipEntry("tags.json");
      zos.putNextEntry(tagsEntry);
      zos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(tags));
      zos.closeEntry();

      // Add project_metadata.json
      ZipEntry projectMetadataEntry = new ZipEntry("project_metadata.json");
      zos.putNextEntry(projectMetadataEntry);
      zos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(metadata));
      zos.closeEntry();

      // Add images with their labels and metadata
      List<Map<String, Object>> imageAnnotations = new ArrayList<>();
      for (SnapshotImage image : images) {
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("id", image.getId());
        imageData.put("fileName", image.getFileName());
        imageData.put("fileSize", image.getFileSize());
        imageData.put("width", image.getWidth());
        imageData.put("height", image.getHeight());
        imageData.put("split", image.getSplit());
        imageData.put("isNoClass", image.getIsNoClass());
        imageData.put("isLabeled", image.getIsLabeled());

        // Get labels for this image
        List<SnapshotImageLabel> imageLabels =
            labels.stream()
                .filter(l -> l.getImageId().equals(image.getId()))
                .collect(Collectors.toList());
        imageData.put("labels", imageLabels);

        // Get tags for this image
        List<SnapshotImageTag> imageTagList =
            imageTags.stream()
                .filter(t -> t.getImageId().equals(image.getId()))
                .collect(Collectors.toList());
        imageData.put("tags", imageTagList);

        // Get metadata for this image
        List<SnapshotImageMetadata> imageMetadataList =
            imageMetadata.stream()
                .filter(m -> m.getImageId().equals(image.getId()))
                .collect(Collectors.toList());
        imageData.put("metadata", imageMetadataList);

        imageAnnotations.add(imageData);

        // Add image file if available (using file_id)
        if (image.getFileId() != null) {
          Optional<byte[]> imageFileStream =
              imageFileRepository.findImageFileStreamById(image.getFileId());
          if (imageFileStream.isPresent()) {
            ZipEntry imageEntry = new ZipEntry("images/" + image.getFileName());
            zos.putNextEntry(imageEntry);
            zos.write(imageFileStream.get());
            zos.closeEntry();
          }
        }
      }

      // Add annotations.json
      ZipEntry annotationsEntry = new ZipEntry("annotations.json");
      zos.putNextEntry(annotationsEntry);
      zos.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(imageAnnotations));
      zos.closeEntry();

      zos.close();
      log.info("Successfully created snapshot dataset ZIP for snapshot ID: {}", snapshotId);
      return baos.toByteArray();

    } catch (IOException e) {
      log.error("Failed to create snapshot dataset ZIP: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create snapshot dataset: " + e.getMessage(), e);
    }
  }

  // ==================== Helper Methods ====================

  /** Delete all snapshot data from _ss tables. */
  private void deleteSnapshotData(Long snapshotId) {
    log.debug("Deleting snapshot data for snapshot ID: {}", snapshotId);
    // Delete in order to avoid FK constraint issues (if any)
    snapshotImageMetadataRepository.deleteBySnapshotId(snapshotId);
    snapshotImageTagRepository.deleteBySnapshotId(snapshotId);
    snapshotImageLabelRepository.deleteBySnapshotId(snapshotId);
    snapshotImageRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectSplitRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectMetadataRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectTagRepository.deleteBySnapshotId(snapshotId);
    snapshotProjectClassRepository.deleteBySnapshotId(snapshotId);
  }

  /**
   * Create a backup snapshot before revert operation. The database trigger will automatically
   * capture current project data.
   */
  private Snapshot createBackupSnapshot(Project project, String userId) {
    // Format: "Backup before revert - yyyy-MM-dd HH:mm:ss" (now fits in 100 char limit)
    String backupName =
        "Backup before revert - "
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    Snapshot backup = new Snapshot();
    backup.setProject(project);
    backup.setSnapshotName(backupName);
    backup.setDescription("Automatic backup before revert operation");
    backup.setCreatedBy(userId);

    // When saved, the database trigger will automatically copy project data to _ss tables
    return snapshotRepository.save(backup);
  }

  /** Delete all project data in correct order to respect FK constraints. */
  private void deleteProjectData(Long projectId) {
    log.debug("Deleting project data for project ID: {}", projectId);
    // Delete in order to respect FK constraints
    imageMetadataRepository.deleteByImageProjectId(projectId);
    imageTagRepository.deleteByImageProjectId(projectId);
    imageLabelRepository.deleteByImageProjectId(projectId);
    imageRepository.deleteByProjectId(projectId);
    projectSplitRepository.deleteByProjectId(projectId);
    projectMetadataRepository.deleteByProjectId(projectId);
    projectTagRepository.deleteByProjectId(projectId);
    projectClassRepository.deleteByProjectId(projectId);
  }

  /**
   * Restore snapshot data from _ss tables to project tables using PostgreSQL stored procedure.
   * IMPORTANT: This method preserves original IDs to maintain links to la_images_file.
   */
  private void restoreSnapshotData(Long snapshotId, Project project, String userId) {
    log.info(
        "Restoring snapshot data for snapshot ID: {} to project ID: {} using stored procedure",
        snapshotId,
        project.getId());

    try {
      // Call PostgreSQL stored procedure to restore all data with preserved IDs
      String sql = "SELECT restore_snapshot_data(:snapshotId, :projectId)";
      String resultJson =
          (String)
              entityManager
                  .createNativeQuery(sql)
                  .setParameter("snapshotId", snapshotId)
                  .setParameter("projectId", project.getId())
                  .getSingleResult();

      log.info("Snapshot restoration completed. Result: {}", resultJson);

      // Parse the JSON result to log statistics
      try {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
            new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode stats = mapper.readTree(resultJson);

        log.info("Restoration statistics:");
        log.info("  - Classes restored: {}", stats.get("classes_restored").asInt());
        log.info("  - Tags restored: {}", stats.get("tags_restored").asInt());
        log.info("  - Metadata restored: {}", stats.get("metadata_restored").asInt());
        log.info("  - Splits restored: {}", stats.get("splits_restored").asInt());
        log.info("  - Images restored: {}", stats.get("images_restored").asInt());
        log.info("  - Labels restored: {}", stats.get("labels_restored").asInt());
        log.info("  - Image tags restored: {}", stats.get("image_tags_restored").asInt());
        log.info("  - Image metadata restored: {}", stats.get("image_metadata_restored").asInt());
      } catch (Exception e) {
        log.warn("Could not parse restoration statistics: {}", e.getMessage());
      }

    } catch (Exception e) {
      log.error("Failed to restore snapshot data using stored procedure: {}", e.getMessage(), e);
      throw new SnapshotRevertException("Failed to restore snapshot data: " + e.getMessage(), e);
    }
  }

  // Note: Individual restore methods removed - now using PostgreSQL stored procedure
  // restore_snapshot_data() which preserves IDs and handles all restoration in a single transaction

  /**
   * Copy project classes from snapshot _ss table to project table. Returns mapping of old class ID
   * to new class ID.
   */
  private Map<Long, Long> copySnapshotProjectClasses(
      Long snapshotId, Project project, String userId) {
    Map<Long, Long> idMapping = new HashMap<>();
    List<SnapshotProjectClass> snapshotClasses =
        snapshotProjectClassRepository.findBySnapshotId(snapshotId);

    for (SnapshotProjectClass sc : snapshotClasses) {
      ProjectClass newClass = new ProjectClass();
      newClass.setProject(project);
      newClass.setClassName(sc.getClassName());
      newClass.setDescription(sc.getDescription());
      newClass.setColorCode(sc.getColorCode());
      newClass.setCreatedBy(userId);
      newClass = projectClassRepository.save(newClass);
      idMapping.put(sc.getId(), newClass.getId());
    }

    log.debug("Copied {} project classes from snapshot", snapshotClasses.size());
    return idMapping;
  }

  /**
   * Copy project tags from snapshot _ss table to project table. Returns mapping of old tag ID to
   * new tag ID.
   */
  private Map<Long, Long> copySnapshotProjectTags(Long snapshotId, Project project, String userId) {
    Map<Long, Long> idMapping = new HashMap<>();
    List<SnapshotProjectTag> snapshotTags =
        snapshotProjectTagRepository.findBySnapshotId(snapshotId);

    for (SnapshotProjectTag st : snapshotTags) {
      ProjectTag newTag = new ProjectTag();
      newTag.setProject(project);
      newTag.setName(st.getName());
      newTag.setCreatedBy(userId);
      newTag = projectTagRepository.save(newTag);
      idMapping.put(st.getId(), newTag.getId());
    }

    log.debug("Copied {} project tags from snapshot", snapshotTags.size());
    return idMapping;
  }

  /**
   * Copy project metadata from snapshot _ss table to project table. Returns mapping of old metadata
   * ID to new metadata ID.
   */
  private Map<Long, Long> copySnapshotProjectMetadata(
      Long snapshotId, Project project, String userId) {
    Map<Long, Long> idMapping = new HashMap<>();
    List<SnapshotProjectMetadata> snapshotMetadata =
        snapshotProjectMetadataRepository.findBySnapshotId(snapshotId);

    for (SnapshotProjectMetadata sm : snapshotMetadata) {
      ProjectMetadata newMetadata = new ProjectMetadata();
      newMetadata.setProject(project);
      newMetadata.setName(sm.getName());
      newMetadata.setType(sm.getType());
      newMetadata.setValueFrom(sm.getValueFrom());
      newMetadata.setPredefinedValues(sm.getPredefinedValues());
      newMetadata.setMultipleValues(sm.getMultipleValues());
      newMetadata.setCreatedBy(userId);
      newMetadata = projectMetadataRepository.save(newMetadata);
      idMapping.put(sm.getId(), newMetadata.getId());
    }

    log.debug("Copied {} project metadata from snapshot", snapshotMetadata.size());
    return idMapping;
  }

  /**
   * Copy project splits from snapshot _ss table to project table. Uses class ID mapping to update
   * foreign key references.
   */
  private void copySnapshotProjectSplits(
      Long snapshotId, Project project, Map<Long, Long> classIdMapping, String userId) {
    List<SnapshotProjectSplit> snapshotSplits =
        snapshotProjectSplitRepository.findBySnapshotId(snapshotId);

    for (SnapshotProjectSplit ss : snapshotSplits) {
      ProjectSplit newSplit = new ProjectSplit();
      newSplit.setProject(project);
      newSplit.setTrainRatio(ss.getTrainRatio());
      newSplit.setDevRatio(ss.getDevRatio());
      newSplit.setTestRatio(ss.getTestRatio());
      newSplit.setCreatedBy(userId);

      // Map class ID if present
      if (ss.getClassId() != null && classIdMapping.containsKey(ss.getClassId())) {
        Long newClassId = classIdMapping.get(ss.getClassId());
        ProjectClass projectClass = projectClassRepository.findById(newClassId).orElse(null);
        newSplit.setProjectClass(projectClass);
      }

      projectSplitRepository.save(newSplit);
    }

    log.debug("Copied {} project splits from snapshot", snapshotSplits.size());
  }

  /**
   * Copy images from snapshot _ss table to project table. Returns mapping of old image ID to new
   * image ID. IMPORTANT: Preserves file_id to share image files across projects.
   */
  private Map<Long, Long> copySnapshotImages(Long snapshotId, Project project, String userId) {
    Map<Long, Long> idMapping = new HashMap<>();
    List<SnapshotImage> snapshotImages = snapshotImageRepository.findBySnapshotId(snapshotId);

    for (SnapshotImage si : snapshotImages) {
      Image newImage = new Image();
      newImage.setProject(project);
      newImage.setFileName(si.getFileName());
      newImage.setFileSize(si.getFileSize());
      newImage.setWidth(si.getWidth());
      newImage.setHeight(si.getHeight());
      newImage.setSplit(si.getSplit());
      newImage.setIsNoClass(si.getIsNoClass());
      // Note: is_labeled is NOT set here - it will be managed by database triggers
      // based on is_no_class flag and label existence after labels are copied
      newImage.setThumbnailImage(si.getThumbnailImage());
      newImage.setThumbnailWidthRatio(si.getThumbnailWidthRatio());
      newImage.setThumbnailHeightRatio(si.getThumbnailHeightRatio());
      newImage.setFileId(si.getFileId()); // Share the same image file
      newImage.setCreatedBy(userId);
      newImage = imageRepository.save(newImage);
      idMapping.put(si.getId(), newImage.getId());
    }

    log.debug("Copied {} images from snapshot (sharing image files)", snapshotImages.size());
    return idMapping;
  }

  /**
   * Copy image labels from snapshot _ss table to project table. Uses image ID and class ID mappings
   * to update foreign key references.
   */
  private void copySnapshotImageLabels(
      Long snapshotId,
      Map<Long, Long> imageIdMapping,
      Map<Long, Long> classIdMapping,
      String userId) {
    List<SnapshotImageLabel> snapshotLabels =
        snapshotImageLabelRepository.findBySnapshotId(snapshotId);

    for (SnapshotImageLabel sl : snapshotLabels) {
      // Skip if image or class mapping not found
      if (!imageIdMapping.containsKey(sl.getImageId())
          || !classIdMapping.containsKey(sl.getClassId())) {
        log.warn(
            "Skipping label - missing mapping for image {} or class {}",
            sl.getImageId(),
            sl.getClassId());
        continue;
      }

      Long newImageId = imageIdMapping.get(sl.getImageId());
      Long newClassId = classIdMapping.get(sl.getClassId());

      Image image = imageRepository.findById(newImageId).orElse(null);
      ProjectClass projectClass = projectClassRepository.findById(newClassId).orElse(null);

      if (image == null || projectClass == null) {
        log.warn("Skipping label - image or class not found");
        continue;
      }

      ImageLabel newLabel = new ImageLabel();
      newLabel.setImage(image);
      newLabel.setProjectClass(projectClass);
      newLabel.setPosition(sl.getPosition());
      // Note: confidenceRate and annotationType are not set as ImageLabel no longer has these
      // fields
      // Copied labels are always ground truth
      newLabel.setCreatedBy(userId);
      imageLabelRepository.save(newLabel);
    }

    log.debug("Copied {} image labels from snapshot", snapshotLabels.size());
  }

  /**
   * Copy image tags from snapshot _ss table to project table. Uses image ID and tag ID mappings to
   * update foreign key references.
   */
  private void copySnapshotImageTags(
      Long snapshotId,
      Map<Long, Long> imageIdMapping,
      Map<Long, Long> tagIdMapping,
      String userId) {
    List<SnapshotImageTag> snapshotTags = snapshotImageTagRepository.findBySnapshotId(snapshotId);

    for (SnapshotImageTag st : snapshotTags) {
      // Skip if image or tag mapping not found
      if (!imageIdMapping.containsKey(st.getImageId())
          || !tagIdMapping.containsKey(st.getTagId())) {
        log.warn(
            "Skipping image tag - missing mapping for image {} or tag {}",
            st.getImageId(),
            st.getTagId());
        continue;
      }

      Long newImageId = imageIdMapping.get(st.getImageId());
      Long newTagId = tagIdMapping.get(st.getTagId());

      Image image = imageRepository.findById(newImageId).orElse(null);
      ProjectTag projectTag = projectTagRepository.findById(newTagId).orElse(null);

      if (image == null || projectTag == null) {
        log.warn("Skipping image tag - image or tag not found");
        continue;
      }

      ImageTag newImageTag = new ImageTag();
      newImageTag.setImage(image);
      newImageTag.setProjectTag(projectTag);
      newImageTag.setCreatedBy(userId);
      imageTagRepository.save(newImageTag);
    }

    log.debug("Copied {} image tags from snapshot", snapshotTags.size());
  }

  /**
   * Copy image metadata from snapshot _ss table to project table. Uses image ID and metadata ID
   * mappings to update foreign key references.
   */
  private void copySnapshotImageMetadata(
      Long snapshotId,
      Map<Long, Long> imageIdMapping,
      Map<Long, Long> metadataIdMapping,
      String userId) {
    List<SnapshotImageMetadata> snapshotMetadata =
        snapshotImageMetadataRepository.findBySnapshotId(snapshotId);

    for (SnapshotImageMetadata sm : snapshotMetadata) {
      // Skip if image or metadata mapping not found
      if (!imageIdMapping.containsKey(sm.getImageId())
          || !metadataIdMapping.containsKey(sm.getMetadataId())) {
        log.warn(
            "Skipping image metadata - missing mapping for image {} or metadata {}",
            sm.getImageId(),
            sm.getMetadataId());
        continue;
      }

      Long newImageId = imageIdMapping.get(sm.getImageId());
      Long newMetadataId = metadataIdMapping.get(sm.getMetadataId());

      Image image = imageRepository.findById(newImageId).orElse(null);
      ProjectMetadata projectMetadata =
          projectMetadataRepository.findById(newMetadataId).orElse(null);

      if (image == null || projectMetadata == null) {
        log.warn("Skipping image metadata - image or metadata not found");
        continue;
      }

      ImageMetadata newImageMetadata = new ImageMetadata();
      newImageMetadata.setImage(image);
      newImageMetadata.setProjectMetadata(projectMetadata);
      newImageMetadata.setValue(sm.getValue());
      newImageMetadata.setCreatedBy(userId);
      imageMetadataRepository.save(newImageMetadata);
    }

    log.debug("Copied {} image metadata from snapshot", snapshotMetadata.size());
  }

  // ==================== DTO Conversion Methods ====================

  private SnapshotDTO convertToDTO(Snapshot snapshot) {
    SnapshotDTO dto = new SnapshotDTO();
    dto.setId(snapshot.getId());
    dto.setProjectId(snapshot.getProject().getId());
    dto.setName(snapshot.getSnapshotName());
    dto.setDescription(snapshot.getDescription());
    dto.setCreatedAt(snapshot.getCreatedAt());
    dto.setCreatedBy(snapshot.getCreatedBy());

    // Get counts from _ss tables
    dto.setImageCount((int) snapshotImageRepository.countBySnapshotId(snapshot.getId()));
    dto.setClassCount((int) snapshotProjectClassRepository.countBySnapshotId(snapshot.getId()));

    return dto;
  }

  /**
   * Export snapshot dataset as ZIP file for training.
   *
   * @param snapshotId the snapshot ID
   * @return byte array containing the ZIP file
   */
  @GetMapping("/{snapshotId}/export-dataset")
  public byte[] exportSnapshotTrainingDataset(@PathVariable("snapshotId") Long snapshotId) {
    log.info("Exporting training dataset for snapshot ID: {}", snapshotId);

    try {
      java.io.File zipFile = datasetExportService.exportSnapshotDataset(snapshotId);
      byte[] data = java.nio.file.Files.readAllBytes(zipFile.toPath());

      // Clean up temp file
      zipFile.delete();

      return data;
    } catch (Exception e) {
      log.error("Failed to export snapshot training dataset: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to export snapshot training dataset", e);
    }
  }

  private ProjectDTO convertProjectToDTO(Project project) {
    return new ProjectDTO(
        project.getId(),
        project.getName(),
        project.getStatus(),
        project.getType(),
        project.getModelName(),
        project.getGroupName(),
        project.getLocation() != null ? project.getLocation().getId().longValue() : null,
        project.getCreatedAt(),
        project.getCreatedBy());
  }
}
