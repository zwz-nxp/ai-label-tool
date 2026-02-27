package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.ImageLabel;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.model.landingai.ProjectSplit;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitRequestDTO;
import com.nxp.iemdm.shared.dto.landingai.AutoSplitStatsDTO;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectSplitRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoSplitService {

  private final ImageRepository imageRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ProjectClassRepository projectClassRepository;
  private final ProjectSplitRepository projectSplitRepository;
  private final ProjectRepository projectRepository;

  /**
   * Get statistics for auto-split feature
   *
   * @param projectId the project ID
   * @param includeAssigned whether to include images that already have split assigned
   * @return statistics DTO
   */
  @Transactional(readOnly = true)
  public AutoSplitStatsDTO getAutoSplitStats(Long projectId, Boolean includeAssigned) {
    log.info(
        "Getting auto-split stats for project {} (includeAssigned: {})",
        projectId,
        includeAssigned);

    // Count total images to split directly from ImageLabel table
    long totalImagesToSplit;
    if (includeAssigned) {
      totalImagesToSplit = imageLabelRepository.countDistinctImagesWithLabels(projectId);
    } else {
      totalImagesToSplit = imageLabelRepository.countDistinctImagesWithLabelsNoSplit(projectId);
    }
    log.info("Total images to split: {}", totalImagesToSplit);

    // Get all classes for the project
    List<ProjectClass> classes = projectClassRepository.findByProject_IdOrderByCreatedAt(projectId);
    log.info("Found {} classes for project {}", classes.size(), projectId);

    // Count images per class directly from ImageLabel table
    List<AutoSplitStatsDTO.ClassStatsDTO> classStats = new ArrayList<>();
    for (ProjectClass projectClass : classes) {
      long imageCount;
      if (includeAssigned) {
        imageCount =
            imageLabelRepository.countDistinctImagesByClass(projectId, projectClass.getId());
      } else {
        imageCount =
            imageLabelRepository.countDistinctImagesByClassNoSplit(projectId, projectClass.getId());
      }
      log.info(
          "Class {} ({}) has {} images",
          projectClass.getId(),
          projectClass.getClassName(),
          imageCount);

      classStats.add(
          new AutoSplitStatsDTO.ClassStatsDTO(
              projectClass.getId(),
              projectClass.getClassName(),
              projectClass.getColorCode(),
              imageCount));
    }

    return new AutoSplitStatsDTO(totalImagesToSplit, classStats);
  }

  /**
   * Assign splits to images based on the provided ratios
   *
   * @param request the auto-split request
   * @return number of images updated
   */
  @Transactional
  public Integer assignSplits(AutoSplitRequestDTO request) {
    log.info(
        "Assigning splits for project {} with request: includeAssigned={}, adjustAllTogether={}, trainRatio={}, devRatio={}, testRatio={}",
        request.getProjectId(),
        request.getIncludeAssigned(),
        request.getAdjustAllTogether(),
        request.getTrainRatio(),
        request.getDevRatio(),
        request.getTestRatio());

    // Get project
    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(
                () -> new RuntimeException("Project not found: " + request.getProjectId()));

    // Get all images for the project
    List<Image> allImages = imageRepository.findByProject_Id(request.getProjectId());
    log.info("Found {} total images for project {}", allImages.size(), request.getProjectId());

    // Filter images that have labels (from ImageLabel table) and optionally no split assigned
    List<Image> images = new ArrayList<>();
    for (Image image : allImages) {
      // Check if image has labels in ImageLabel table
      long labelCount = imageLabelRepository.countByImage_Id(image.getId());
      boolean hasLabels = labelCount > 0;

      log.debug(
          "Image {} - hasLabels={}, currentSplit={}", image.getId(), hasLabels, image.getSplit());

      if (hasLabels) {
        if (Boolean.TRUE.equals(request.getIncludeAssigned())) {
          // Include all labeled images
          images.add(image);
          log.debug("Image {} added (includeAssigned=true)", image.getId());
        } else {
          // Only include images with no split assigned
          if (image.getSplit() == null || image.getSplit().isEmpty()) {
            images.add(image);
            log.debug("Image {} added (no split assigned)", image.getId());
          } else {
            log.debug("Image {} skipped (already has split: {})", image.getId(), image.getSplit());
          }
        }
      }
    }

    log.info(
        "Filtered to {} images to split (includeAssigned={})",
        images.size(),
        request.getIncludeAssigned());

    if (images.isEmpty()) {
      log.warn("No images to split for project {}", request.getProjectId());
      return 0;
    }

    // 1. Assign splits to images
    if (request.getAdjustAllTogether()) {
      // Apply global ratio to all images
      assignSplitsGlobally(
          images, request.getTrainRatio(), request.getDevRatio(), request.getTestRatio());
    } else {
      // Apply per-class ratios
      assignSplitsPerClass(images, request);
    }

    // 2. Save split configuration to la_project_split table
    saveSplitConfiguration(project, request);

    return images.size();
  }

  /** Save split configuration to la_project_split table */
  private void saveSplitConfiguration(Project project, AutoSplitRequestDTO request) {
    if (request.getAdjustAllTogether()) {
      // Save single global split (no class_id)
      Optional<ProjectSplit> existingSplit =
          projectSplitRepository.findByProject_IdAndProjectClassIsNull(project.getId());

      ProjectSplit split;
      if (existingSplit.isPresent()) {
        split = existingSplit.get();
      } else {
        split = new ProjectSplit();
        split.setProject(project);
        split.setProjectClass(null);
      }
      split.setTrainRatio(request.getTrainRatio());
      split.setDevRatio(request.getDevRatio());
      split.setTestRatio(request.getTestRatio());
      projectSplitRepository.save(split);
      log.info(
          "Saved global split configuration: train={}, dev={}, test={}",
          request.getTrainRatio(),
          request.getDevRatio(),
          request.getTestRatio());
    } else {
      // Save per-class splits
      Map<Long, AutoSplitRequestDTO.ClassRatioDTO> classRatios = request.getClassRatios();
      if (classRatios != null) {
        for (Map.Entry<Long, AutoSplitRequestDTO.ClassRatioDTO> entry : classRatios.entrySet()) {
          Long classId = entry.getKey();
          AutoSplitRequestDTO.ClassRatioDTO ratio = entry.getValue();

          ProjectClass projectClass = projectClassRepository.findById(classId).orElse(null);
          if (projectClass == null) {
            log.warn("ProjectClass not found for id: {}", classId);
            continue;
          }

          Optional<ProjectSplit> existingSplit =
              projectSplitRepository.findByProject_IdAndProjectClassId(project.getId(), classId);

          ProjectSplit split;
          if (existingSplit.isPresent()) {
            split = existingSplit.get();
          } else {
            split = new ProjectSplit();
            split.setProject(project);
            split.setProjectClass(projectClass);
          }
          split.setTrainRatio(ratio.getTrain());
          split.setDevRatio(ratio.getDev());
          split.setTestRatio(ratio.getTest());
          projectSplitRepository.save(split);
          log.info(
              "Saved split configuration for class {}: train={}, dev={}, test={}",
              projectClass.getClassName(),
              ratio.getTrain(),
              ratio.getDev(),
              ratio.getTest());
        }
      }
    }
  }

  /** Assign splits globally (same ratio for all images) */
  private void assignSplitsGlobally(
      List<Image> images, Integer trainRatio, Integer devRatio, Integer testRatio) {
    // Shuffle images for random distribution
    Collections.shuffle(images);

    int totalImages = images.size();

    // Calculate train count first (round to nearest)
    int trainCount = (int) Math.round(totalImages * trainRatio / 100.0);
    // Ensure trainCount doesn't exceed total
    trainCount = Math.min(trainCount, totalImages);

    // Calculate dev count from remaining images
    int remaining = totalImages - trainCount;
    int devCount = (int) Math.round(remaining * devRatio / (double) (devRatio + testRatio));
    // Ensure devCount doesn't exceed remaining
    devCount = Math.min(devCount, remaining);

    // Test gets whatever is left (can be 0)
    int testCount = totalImages - trainCount - devCount;

    log.info(
        "Assigning splits globally: train={}, dev={}, test={} (total={})",
        trainCount,
        devCount,
        testCount,
        totalImages);

    for (int i = 0; i < images.size(); i++) {
      Image image = images.get(i);
      if (i < trainCount) {
        image.setSplit("training");
      } else if (i < trainCount + devCount) {
        image.setSplit("dev");
      } else {
        image.setSplit("test");
      }
      imageRepository.save(image);
    }
  }

  /** Assign splits per class (different ratio for each class) */
  private void assignSplitsPerClass(List<Image> images, AutoSplitRequestDTO request) {
    // Group images by their primary class (first label's class)
    Map<Long, List<Image>> imagesByClass = new HashMap<>();

    for (Image image : images) {
      // Get the first label's class for this image (any annotation type)
      List<ImageLabel> labels = imageLabelRepository.findByImage_Id(image.getId());

      if (!labels.isEmpty()) {
        Long classId = labels.get(0).getProjectClass().getId();
        imagesByClass.computeIfAbsent(classId, k -> new ArrayList<>()).add(image);
      }
    }

    // Assign splits for each class
    for (Map.Entry<Long, List<Image>> entry : imagesByClass.entrySet()) {
      Long classId = entry.getKey();
      List<Image> classImages = entry.getValue();

      AutoSplitRequestDTO.ClassRatioDTO ratio = request.getClassRatios().get(classId);
      if (ratio == null) {
        // Use global ratio if class ratio not specified
        ratio =
            new AutoSplitRequestDTO.ClassRatioDTO(
                request.getTrainRatio(), request.getDevRatio(), request.getTestRatio());
      }

      assignSplitsGlobally(classImages, ratio.getTrain(), ratio.getDev(), ratio.getTest());
    }
  }
}
