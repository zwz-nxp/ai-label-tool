package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.model.landingai.ProjectSplit;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO.ClassSplitDataDTO;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO.SplitClassCountDTO;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO.SplitClassDataDTO;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectSplitRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for ProjectSplit operations in the Operational Layer. Handles business logic for project
 * split configuration management.
 *
 * <p>Requirements: 25.7, 25.4, 25.5
 */
@Slf4j
@Service
public class ProjectSplitService {

  private final ProjectSplitRepository projectSplitRepository;
  private final ImageRepository imageRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ProjectClassRepository projectClassRepository;

  public ProjectSplitService(
      ProjectSplitRepository projectSplitRepository,
      ImageRepository imageRepository,
      ImageLabelRepository imageLabelRepository,
      ProjectClassRepository projectClassRepository) {
    this.projectSplitRepository = projectSplitRepository;
    this.imageRepository = imageRepository;
    this.imageLabelRepository = imageLabelRepository;
    this.projectClassRepository = projectClassRepository;
  }

  /**
   * Get split preview data for a project. Aggregates image split distribution by class and by split
   * type.
   *
   * @param projectId the project ID
   * @return split preview data, or empty SplitPreviewDTO if no split configuration exists
   */
  @Transactional(readOnly = true)
  public SplitPreviewDTO getSplitPreview(Long projectId) {
    log.debug("Getting split preview for project ID: {}", projectId);

    // Check if project has any split configuration
    List<ProjectSplit> projectSplits = projectSplitRepository.findByProject_Id(projectId);
    if (projectSplits == null || projectSplits.isEmpty()) {
      log.debug(
          "No split configuration found for project ID: {}, returning empty preview", projectId);
      return SplitPreviewDTO.builder()
          .totalImages(0)
          .unassignedCount(0)
          .byClass(Collections.emptyList())
          .bySplit(Collections.emptyList())
          .build();
    }

    // Get all project classes
    List<ProjectClass> projectClasses = projectClassRepository.findByProject_Id(projectId);

    // Get split counts by class using native query
    List<Object[]> splitCountsByClass =
        imageLabelRepository.countSplitsByClassForProject(projectId);

    // Build byClass data
    Map<Long, ClassSplitDataDTO> classDataMap = new HashMap<>();
    for (ProjectClass pc : projectClasses) {
      classDataMap.put(
          pc.getId(),
          ClassSplitDataDTO.builder()
              .className(pc.getClassName())
              .classColor(pc.getColorCode() != null ? pc.getColorCode() : "#808080")
              .train(0)
              .dev(0)
              .test(0)
              .unassigned(0)
              .build());
    }

    // Populate counts from query results
    int totalImages = 0;
    int unassignedCount = 0;
    for (Object[] row : splitCountsByClass) {
      Long classId = ((Number) row[0]).longValue();
      String split = (String) row[1];
      int count = ((Number) row[2]).intValue();

      ClassSplitDataDTO classData = classDataMap.get(classId);
      if (classData != null) {
        if (split == null || split.isEmpty()) {
          classData.setUnassigned(classData.getUnassigned() + count);
          unassignedCount += count;
        } else if ("training".equalsIgnoreCase(split)) {
          classData.setTrain(classData.getTrain() + count);
        } else if ("dev".equalsIgnoreCase(split)) {
          classData.setDev(classData.getDev() + count);
        } else if ("test".equalsIgnoreCase(split)) {
          classData.setTest(classData.getTest() + count);
        }
        totalImages += count;
      }
    }

    List<ClassSplitDataDTO> byClass = new ArrayList<>(classDataMap.values());

    // Build bySplit data
    List<SplitClassDataDTO> bySplit = buildBySplitData(byClass);

    return SplitPreviewDTO.builder()
        .totalImages(totalImages)
        .unassignedCount(unassignedCount)
        .byClass(byClass)
        .bySplit(bySplit)
        .build();
  }

  /** Build bySplit data from byClass data. */
  private List<SplitClassDataDTO> buildBySplitData(List<ClassSplitDataDTO> byClass) {
    List<SplitClassDataDTO> bySplit = new ArrayList<>();

    // Train split
    List<SplitClassCountDTO> trainClasses =
        byClass.stream()
            .filter(c -> c.getTrain() > 0)
            .map(
                c ->
                    SplitClassCountDTO.builder()
                        .className(c.getClassName())
                        .classColor(c.getClassColor())
                        .count(c.getTrain())
                        .build())
            .collect(Collectors.toList());
    bySplit.add(SplitClassDataDTO.builder().splitType("train").classes(trainClasses).build());

    // Dev split
    List<SplitClassCountDTO> devClasses =
        byClass.stream()
            .filter(c -> c.getDev() > 0)
            .map(
                c ->
                    SplitClassCountDTO.builder()
                        .className(c.getClassName())
                        .classColor(c.getClassColor())
                        .count(c.getDev())
                        .build())
            .collect(Collectors.toList());
    bySplit.add(SplitClassDataDTO.builder().splitType("dev").classes(devClasses).build());

    // Test split
    List<SplitClassCountDTO> testClasses =
        byClass.stream()
            .filter(c -> c.getTest() > 0)
            .map(
                c ->
                    SplitClassCountDTO.builder()
                        .className(c.getClassName())
                        .classColor(c.getClassColor())
                        .count(c.getTest())
                        .build())
            .collect(Collectors.toList());
    bySplit.add(SplitClassDataDTO.builder().splitType("test").classes(testClasses).build());

    // Unassigned split
    List<SplitClassCountDTO> unassignedClasses =
        byClass.stream()
            .filter(c -> c.getUnassigned() > 0)
            .map(
                c ->
                    SplitClassCountDTO.builder()
                        .className(c.getClassName())
                        .classColor(c.getClassColor())
                        .count(c.getUnassigned())
                        .build())
            .collect(Collectors.toList());
    bySplit.add(
        SplitClassDataDTO.builder().splitType("unassigned").classes(unassignedClasses).build());

    return bySplit;
  }

  /**
   * Create a new project split configuration
   *
   * @param projectSplit the project split to create
   * @return the created project split
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProjectSplit createProjectSplit(ProjectSplit projectSplit) {
    log.info("Creating new project split for project ID: {}", projectSplit.getProject().getId());

    // Validate ratios
    validateSplitRatios(projectSplit);

    // Set default values if not provided
    if (projectSplit.getTrainRatio() == null) {
      projectSplit.setTrainRatio(70);
    }
    if (projectSplit.getDevRatio() == null) {
      projectSplit.setDevRatio(20);
    }
    if (projectSplit.getTestRatio() == null) {
      projectSplit.setTestRatio(10);
    }

    return projectSplitRepository.save(projectSplit);
  }

  /**
   * Get a project split by ID
   *
   * @param splitId the project split ID
   * @return the project split
   * @throws NotFoundException if project split not found
   */
  @Transactional(readOnly = true)
  public ProjectSplit getProjectSplitById(Long splitId) {
    log.debug("Retrieving project split with ID: {}", splitId);
    return projectSplitRepository
        .findById(splitId)
        .orElseThrow(() -> new NotFoundException("Project split not found with ID: " + splitId));
  }

  /**
   * Get all project splits for a specific project
   *
   * @param projectId the project ID
   * @return list of project splits
   */
  @Transactional(readOnly = true)
  public List<ProjectSplit> getProjectSplitsByProjectId(Long projectId) {
    log.debug("Retrieving project splits for project ID: {}", projectId);
    return projectSplitRepository.findByProject_Id(projectId);
  }

  /**
   * Get project split for a specific project and class
   *
   * @param projectId the project ID
   * @param classId the class ID (null for default split)
   * @return the project split if found
   */
  @Transactional(readOnly = true)
  public Optional<ProjectSplit> getProjectSplitByProjectAndClass(Long projectId, Long classId) {
    log.debug("Retrieving project split for project {} and class {}", projectId, classId);
    if (classId == null) {
      return projectSplitRepository.findByProject_IdAndProjectClassIsNull(projectId);
    }
    return projectSplitRepository.findByProject_IdAndProjectClassId(projectId, classId);
  }

  /**
   * Update an existing project split
   *
   * @param splitId the project split ID to update
   * @param projectSplitUpdate the updated project split data
   * @return the updated project split
   * @throws NotFoundException if project split not found
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProjectSplit updateProjectSplit(Long splitId, ProjectSplit projectSplitUpdate) {
    log.info("Updating project split with ID: {}", splitId);

    ProjectSplit existingSplit = getProjectSplitById(splitId);

    // Update ratios if provided
    if (projectSplitUpdate.getTrainRatio() != null) {
      existingSplit.setTrainRatio(projectSplitUpdate.getTrainRatio());
    }
    if (projectSplitUpdate.getDevRatio() != null) {
      existingSplit.setDevRatio(projectSplitUpdate.getDevRatio());
    }
    if (projectSplitUpdate.getTestRatio() != null) {
      existingSplit.setTestRatio(projectSplitUpdate.getTestRatio());
    }

    // Validate updated ratios
    validateSplitRatios(existingSplit);

    if (projectSplitUpdate.getProjectClass() != null) {
      existingSplit.setProjectClass(projectSplitUpdate.getProjectClass());
    }

    return projectSplitRepository.save(existingSplit);
  }

  /**
   * Delete a project split
   *
   * @param splitId the project split ID to delete
   * @throws NotFoundException if project split not found
   */
  @Transactional
  public void deleteProjectSplit(Long splitId) {
    log.info("Deleting project split with ID: {}", splitId);

    if (!projectSplitRepository.existsById(splitId)) {
      throw new NotFoundException("Project split not found with ID: " + splitId);
    }

    projectSplitRepository.deleteById(splitId);
  }

  /**
   * Validate that split ratios sum to 100
   *
   * @param projectSplit the project split to validate
   * @throws IllegalArgumentException if ratios are invalid
   */
  private void validateSplitRatios(ProjectSplit projectSplit) {
    int trainRatio = projectSplit.getTrainRatio() != null ? projectSplit.getTrainRatio() : 0;
    int devRatio = projectSplit.getDevRatio() != null ? projectSplit.getDevRatio() : 0;
    int testRatio = projectSplit.getTestRatio() != null ? projectSplit.getTestRatio() : 0;

    if (trainRatio < 0 || devRatio < 0 || testRatio < 0) {
      throw new IllegalArgumentException("Split ratios cannot be negative");
    }

    int total = trainRatio + devRatio + testRatio;
    if (total != 100) {
      throw new IllegalArgumentException("Split ratios must sum to 100. Current sum: " + total);
    }
  }
}
