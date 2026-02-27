package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ImagePredictionLabel;
import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.model.landingai.SnapshotImage;
import com.nxp.iemdm.model.landingai.SnapshotImageLabel;
import com.nxp.iemdm.model.landingai.SnapshotProjectClass;
import com.nxp.iemdm.shared.dto.landingai.CellDetailResponse;
import com.nxp.iemdm.shared.dto.landingai.ClassInfoDTO;
import com.nxp.iemdm.shared.dto.landingai.ClassMetricsDTO;
import com.nxp.iemdm.shared.dto.landingai.ConfusionMatrixResponse;
import com.nxp.iemdm.shared.dto.landingai.GroundTruthLabelDTO;
import com.nxp.iemdm.shared.dto.landingai.ImageWithLabelsDTO;
import com.nxp.iemdm.shared.dto.landingai.LabelInfoDTO;
import com.nxp.iemdm.shared.dto.landingai.MatrixCellDTO;
import com.nxp.iemdm.shared.dto.landingai.PredictionLabelDTO;
import com.nxp.iemdm.shared.intf.operational.landingai.ConfusionMatrixService;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImagePredictionLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ModelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.SnapshotProjectClassRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for confusion matrix operations. Implements business logic for calculating
 * confusion matrix, cell details, and image analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfusionMatrixServiceImpl implements ConfusionMatrixService {

  private final SnapshotImageLabelRepository snapshotImageLabelRepository;
  private final ImagePredictionLabelRepository imagePredictionLabelRepository;
  private final SnapshotProjectClassRepository snapshotProjectClassRepository;
  private final SnapshotImageRepository snapshotImageRepository;
  private final ModelRepository modelRepository;
  private final EntityManager entityManager;

  @Override
  @Transactional(readOnly = true)
  public ConfusionMatrixResponse calculateConfusionMatrix(Long modelId, String evaluationSet) {
    log.info(
        "Calculating confusion matrix for modelId={}, evaluationSet={}", modelId, evaluationSet);

    // 1. Get model and snapshot ID
    Model model =
        modelRepository
            .findById(modelId)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + modelId));

    // Get snapshot ID from training record
    Long snapshotId = model.getTrainingRecord().getSnapshotId();
    if (snapshotId == null) {
      log.error("Model {} has no snapshot_id in training record", modelId);
      throw new IllegalStateException("Model has no associated snapshot");
    }

    log.info("Using snapshot_id={} for model={}", snapshotId, modelId);

    // 2. Query all classes for the snapshot
    List<SnapshotProjectClass> classes =
        snapshotProjectClassRepository.findBySnapshotIdOrderBySequence(snapshotId);
    if (classes.isEmpty()) {
      log.warn("No classes found for snapshotId={}", snapshotId);
      return buildEmptyResponse();
    }

    // 3. Build class info DTOs
    List<ClassInfoDTO> classInfoList =
        classes.stream()
            .map(
                c ->
                    ClassInfoDTO.builder()
                        .id(c.getId())
                        .name(c.getClassName())
                        .color(c.getColorCode())
                        .build())
            .collect(Collectors.toList());

    // Add "No label" to Ground Truth axis (for images with predictions but no GT)
    List<ClassInfoDTO> gtClassInfoList = new ArrayList<>(classInfoList);
    gtClassInfoList.add(
        ClassInfoDTO.builder()
            .id(-2L)
            .name("No label")
            .color("#DDDDDD") // Light gray color for "No label"
            .build());

    // Build prediction classes list (includes "No prediction" at the end)
    List<ClassInfoDTO> predictionClassInfoList = new ArrayList<>(classInfoList);
    predictionClassInfoList.add(
        ClassInfoDTO.builder()
            .id(-1L)
            .name("No prediction")
            .color("#CCCCCC") // Gray color for "No prediction"
            .build());

    // 4. Query images for evaluation set from snapshot
    String splitValue = convertEvaluationSetToSplit(evaluationSet);
    List<SnapshotImage> images =
        snapshotImageRepository.findBySnapshotIdAndSplit(snapshotId, splitValue);
    if (images.isEmpty()) {
      log.warn("No images found for snapshotId={}, split={}", snapshotId, splitValue);
      return buildEmptyResponseWithClasses(classInfoList);
    }

    List<Long> imageIds = images.stream().map(SnapshotImage::getId).collect(Collectors.toList());

    // 5. Query ground truth labels and predictions
    List<SnapshotImageLabel> gtLabels =
        snapshotImageLabelRepository.findBySnapshotIdAndImageIdIn(snapshotId, imageIds);
    List<ImagePredictionLabel> predLabels =
        imagePredictionLabelRepository.findByModelId(modelId).stream()
            .filter(p -> imageIds.contains(p.getImage().getId()))
            .collect(Collectors.toList());

    // 6. Build GT and Pred maps: (imageId + position) -> label
    // Use position-based matching to handle multiple labels per image
    Map<String, SnapshotImageLabel> gtLabelMap = new HashMap<>();
    for (SnapshotImageLabel label : gtLabels) {
      String key = label.getImageId() + "_" + label.getPosition();
      gtLabelMap.put(key, label);
    }

    Map<String, ImagePredictionLabel> predLabelMap = new HashMap<>();
    for (ImagePredictionLabel label : predLabels) {
      String key = label.getImage().getId() + "_" + label.getPosition();
      predLabelMap.put(key, label);
    }

    // 7. Calculate confusion matrix counts (per-label matching)
    Map<String, Integer> confusionCounts = new HashMap<>();
    Map<Long, Integer> noPredictionCounts =
        new HashMap<>(); // Track GT labels with no prediction per GT class
    Map<Long, Integer> noLabelCounts =
        new HashMap<>(); // Track predictions with no GT per Pred class
    int maxCount = 0;

    // Process all GT labels
    for (SnapshotImageLabel gtLabel : gtLabels) {
      String key = gtLabel.getImageId() + "_" + gtLabel.getPosition();
      ImagePredictionLabel predLabel = predLabelMap.get(key);
      Long gtClassId = gtLabel.getClassId();

      if (predLabel != null) {
        // Has matching prediction
        Long predClassId = predLabel.getProjectClass().getId();
        String countKey = gtClassId + "_" + predClassId;
        int count = confusionCounts.getOrDefault(countKey, 0) + 1;
        confusionCounts.put(countKey, count);
        maxCount = Math.max(maxCount, count);
      } else {
        // Has GT but no matching prediction
        int count = noPredictionCounts.getOrDefault(gtClassId, 0) + 1;
        noPredictionCounts.put(gtClassId, count);
        maxCount = Math.max(maxCount, count);
      }
    }

    // Process predictions without GT (False Positives)
    for (ImagePredictionLabel predLabel : predLabels) {
      String key = predLabel.getImage().getId() + "_" + predLabel.getPosition();
      if (!gtLabelMap.containsKey(key)) {
        // Has prediction but no GT (No label case)
        Long predClassId = predLabel.getProjectClass().getId();
        int count = noLabelCounts.getOrDefault(predClassId, 0) + 1;
        noLabelCounts.put(predClassId, count);
        maxCount = Math.max(maxCount, count);
      }
    }

    // 8. Build matrix cells (including "No prediction" column)
    List<List<MatrixCellDTO>> matrix = new ArrayList<>();
    for (SnapshotProjectClass gtClass : classes) {
      List<MatrixCellDTO> row = new ArrayList<>();

      // Add cells for each prediction class
      for (SnapshotProjectClass predClass : classes) {
        String key = gtClass.getId() + "_" + predClass.getId();
        int count = confusionCounts.getOrDefault(key, 0);
        boolean isDiagonal = gtClass.getId().equals(predClass.getId());

        row.add(
            MatrixCellDTO.builder()
                .groundTruthClassId(gtClass.getId())
                .predictionClassId(predClass.getId())
                .count(count)
                .isDiagonal(isDiagonal)
                .build());
      }

      // Add "No prediction" cell at the end of each row
      int noPredCount = noPredictionCounts.getOrDefault(gtClass.getId(), 0);
      row.add(
          MatrixCellDTO.builder()
              .groundTruthClassId(gtClass.getId())
              .predictionClassId(-1L) // Special ID for "No prediction"
              .count(noPredCount)
              .isDiagonal(false)
              .build());

      matrix.add(row);
    }

    // Add "No label" row at the end
    List<MatrixCellDTO> noLabelRow = new ArrayList<>();
    for (SnapshotProjectClass predClass : classes) {
      int count = noLabelCounts.getOrDefault(predClass.getId(), 0);
      noLabelRow.add(
          MatrixCellDTO.builder()
              .groundTruthClassId(-2L) // Special ID for "No label"
              .predictionClassId(predClass.getId())
              .count(count)
              .isDiagonal(false)
              .build());
    }
    // Add bottom-right corner cell (No label × No prediction) - should always be 0 or N/A
    noLabelRow.add(
        MatrixCellDTO.builder()
            .groundTruthClassId(-2L)
            .predictionClassId(-1L)
            .count(0) // This cell doesn't make sense, so always 0
            .isDiagonal(false)
            .build());
    matrix.add(noLabelRow);

    // 9. Calculate class-level metrics
    log.info(
        "Calculating class-level metrics. noLabelCounts: {}, noPredictionCounts: {}",
        noLabelCounts,
        noPredictionCounts);

    List<ClassMetricsDTO> classMetrics = new ArrayList<>();
    for (SnapshotProjectClass cls : classes) {
      int tp = confusionCounts.getOrDefault(cls.getId() + "_" + cls.getId(), 0);

      // False Positives: predictions for this class that were wrong
      // Includes: 1) other GT classes predicted as this class
      //           2) "No label" (FP) predicted as this class
      int fp = 0;
      for (SnapshotProjectClass gtClass : classes) {
        if (!gtClass.getId().equals(cls.getId())) {
          fp += confusionCounts.getOrDefault(gtClass.getId() + "_" + cls.getId(), 0);
        }
      }
      int fpFromNoLabel = noLabelCounts.getOrDefault(cls.getId(), 0);
      fp += fpFromNoLabel;

      // False Negatives: GT labels for this class that were not correctly predicted
      // Includes: 1) this GT class predicted as other classes
      //           2) this GT class with no prediction
      int fn = 0;
      for (SnapshotProjectClass predClass : classes) {
        if (!predClass.getId().equals(cls.getId())) {
          fn += confusionCounts.getOrDefault(cls.getId() + "_" + predClass.getId(), 0);
        }
      }
      int fnFromNoPred = noPredictionCounts.getOrDefault(cls.getId(), 0);
      fn += fnFromNoPred;

      Double precision = (tp + fp > 0) ? (double) tp / (tp + fp) * 100 : null;
      Double recall = (tp + fn > 0) ? (double) tp / (tp + fn) * 100 : null;

      log.info(
          "Class {} ({}) - TP: {}, FP: {} (from other GT: {}, from No label: {}), FN: {} (from other Pred: {}, from No pred: {}), Precision: {}, Recall: {}",
          cls.getId(),
          cls.getClassName(),
          tp,
          fp,
          fp - fpFromNoLabel,
          fpFromNoLabel,
          fn,
          fn - fnFromNoPred,
          fnFromNoPred,
          precision,
          recall);

      classMetrics.add(
          ClassMetricsDTO.builder()
              .classId(cls.getId())
              .truePositives(tp)
              .falsePositives(fp)
              .falseNegatives(fn)
              .precision(precision)
              .recall(recall)
              .build());
    }

    return ConfusionMatrixResponse.builder()
        .classes(gtClassInfoList)
        .predictionClasses(predictionClassInfoList)
        .matrix(matrix)
        .classMetrics(classMetrics)
        .maxCount(maxCount)
        .build();
  }

  private String convertEvaluationSetToSplit(String evaluationSet) {
    return switch (evaluationSet.toUpperCase()) {
      case "TRAIN" -> "training";
      case "DEV" -> "dev";
      case "TEST" -> "test";
      default -> evaluationSet.toLowerCase();
    };
  }

  private ConfusionMatrixResponse buildEmptyResponse() {
    return ConfusionMatrixResponse.builder()
        .classes(new ArrayList<>())
        .predictionClasses(new ArrayList<>())
        .matrix(new ArrayList<>())
        .classMetrics(new ArrayList<>())
        .maxCount(0)
        .build();
  }

  private ConfusionMatrixResponse buildEmptyResponseWithClasses(List<ClassInfoDTO> classes) {
    // For empty response, predictionClasses should also include "No prediction"
    List<ClassInfoDTO> predictionClasses = new ArrayList<>(classes);
    if (!classes.isEmpty()) {
      predictionClasses.add(
          ClassInfoDTO.builder().id(-1L).name("No prediction").color("#CCCCCC").build());
    }

    return ConfusionMatrixResponse.builder()
        .classes(classes)
        .predictionClasses(predictionClasses)
        .matrix(new ArrayList<>())
        .classMetrics(new ArrayList<>())
        .maxCount(0)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public CellDetailResponse getCellDetail(
      Long modelId, String evaluationSet, Long gtClassId, Long predClassId) {
    log.info(
        "Getting cell detail for modelId={}, evaluationSet={}, gtClassId={}, predClassId={}",
        modelId,
        evaluationSet,
        gtClassId,
        predClassId);

    // 1. Get model and snapshot ID
    Model model =
        modelRepository
            .findById(modelId)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + modelId));

    Long snapshotId = model.getTrainingRecord().getSnapshotId();
    if (snapshotId == null) {
      throw new IllegalStateException("Model has no associated snapshot");
    }

    // 2. Get class names
    String gtClassName;
    if (gtClassId == -2L) {
      gtClassName = "No label";
    } else {
      SnapshotProjectClass gtClass =
          snapshotProjectClassRepository.findBySnapshotId(snapshotId).stream()
              .filter(c -> c.getId().equals(gtClassId))
              .findFirst()
              .orElseThrow(() -> new NotFoundException("Class not found with ID: " + gtClassId));
      gtClassName = gtClass.getClassName();
    }

    // Handle "No prediction" case (predClassId = -1)
    String predClassName;
    if (predClassId == -1L) {
      predClassName = "No prediction";
    } else {
      SnapshotProjectClass predClass =
          snapshotProjectClassRepository.findBySnapshotId(snapshotId).stream()
              .filter(c -> c.getId().equals(predClassId))
              .findFirst()
              .orElseThrow(() -> new NotFoundException("Class not found with ID: " + predClassId));
      predClassName = predClass.getClassName();
    }

    // 3. Query images for evaluation set from snapshot
    String splitValue = convertEvaluationSetToSplit(evaluationSet);
    List<SnapshotImage> images =
        snapshotImageRepository.findBySnapshotIdAndSplit(snapshotId, splitValue);
    List<Long> imageIds = images.stream().map(SnapshotImage::getId).collect(Collectors.toList());

    if (imageIds.isEmpty()) {
      return buildEmptyCellDetailResponse(gtClassName, predClassName, gtClassId, predClassId);
    }

    // 4. Query ground truth labels and predictions
    List<SnapshotImageLabel> gtLabels =
        snapshotImageLabelRepository.findBySnapshotIdAndImageIdIn(snapshotId, imageIds);
    List<ImagePredictionLabel> predLabels =
        imagePredictionLabelRepository.findByModelId(modelId).stream()
            .filter(p -> imageIds.contains(p.getImage().getId()))
            .collect(Collectors.toList());

    // 5. Build GT and Pred maps: (imageId + position) -> label (per-label matching)
    Map<String, SnapshotImageLabel> gtLabelMap = new HashMap<>();
    for (SnapshotImageLabel label : gtLabels) {
      String key = label.getImageId() + "_" + label.getPosition();
      gtLabelMap.put(key, label);
    }

    Map<String, ImagePredictionLabel> predLabelMap = new HashMap<>();
    for (ImagePredictionLabel label : predLabels) {
      String key = label.getImage().getId() + "_" + label.getPosition();
      predLabelMap.put(key, label);
    }

    // 6. Build image map for grouping labels by image
    Map<Long, SnapshotImage> imageMap = new HashMap<>();
    for (SnapshotImage image : images) {
      imageMap.put(image.getId(), image);
    }

    // 7. Find matching label pairs and group by image
    Map<Long, ImageWithLabelsDTO> imageResultMap = new HashMap<>();
    int totalLabelPairCount = 0; // Count actual label pairs, not unique images

    if (gtClassId == -2L) {
      // "No label" case: predictions without GT
      for (ImagePredictionLabel predLabel : predLabels) {
        if (!predLabel.getProjectClass().getId().equals(predClassId)) {
          continue;
        }
        String key = predLabel.getImage().getId() + "_" + predLabel.getPosition();
        if (!gtLabelMap.containsKey(key)) {
          // Has prediction but no GT at this position
          totalLabelPairCount++; // Count this label pair
          Long imageId = predLabel.getImage().getId();
          if (!imageResultMap.containsKey(imageId)) {
            imageResultMap.put(
                imageId,
                buildImageWithLabelsDTO(
                    imageMap.get(imageId), new ArrayList<>(), new ArrayList<>()));
          }
          imageResultMap.get(imageId).getPredictionLabels().add(buildLabelInfoDTO(predLabel));
        }
      }
    } else if (predClassId == -1L) {
      // "No prediction" case: GT labels without predictions
      for (SnapshotImageLabel gtLabel : gtLabels) {
        if (!gtLabel.getClassId().equals(gtClassId)) {
          continue;
        }
        String key = gtLabel.getImageId() + "_" + gtLabel.getPosition();
        if (!predLabelMap.containsKey(key)) {
          // Has GT but no prediction at this position
          totalLabelPairCount++; // Count this label pair
          Long imageId = gtLabel.getImageId();
          if (!imageResultMap.containsKey(imageId)) {
            imageResultMap.put(
                imageId,
                buildImageWithLabelsDTO(
                    imageMap.get(imageId), new ArrayList<>(), new ArrayList<>()));
          }
          imageResultMap
              .get(imageId)
              .getGroundTruthLabels()
              .add(buildLabelInfoDTO(gtLabel, snapshotId));
        }
      }
    } else {
      // Normal case: match GT and Pred by position
      for (SnapshotImageLabel gtLabel : gtLabels) {
        if (!gtLabel.getClassId().equals(gtClassId)) {
          continue;
        }
        String key = gtLabel.getImageId() + "_" + gtLabel.getPosition();
        ImagePredictionLabel predLabel = predLabelMap.get(key);

        if (predLabel != null && predLabel.getProjectClass().getId().equals(predClassId)) {
          // Found matching GT×Pred pair
          totalLabelPairCount++; // Count this label pair
          Long imageId = gtLabel.getImageId();
          if (!imageResultMap.containsKey(imageId)) {
            imageResultMap.put(
                imageId,
                buildImageWithLabelsDTO(
                    imageMap.get(imageId), new ArrayList<>(), new ArrayList<>()));
          }
          imageResultMap
              .get(imageId)
              .getGroundTruthLabels()
              .add(buildLabelInfoDTO(gtLabel, snapshotId));
          imageResultMap.get(imageId).getPredictionLabels().add(buildLabelInfoDTO(predLabel));
        }
      }
    }

    List<ImageWithLabelsDTO> matchingImages = new ArrayList<>(imageResultMap.values());

    return CellDetailResponse.builder()
        .groundTruthClassId(gtClassId)
        .predictionClassId(predClassId)
        .groundTruthClassName(gtClassName)
        .predictionClassName(predClassName)
        .totalCount(totalLabelPairCount) // Use label pair count, not unique image count
        .images(matchingImages)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ImageWithLabelsDTO> getAllImages(Long modelId, String evaluationSet) {
    log.info("Getting all images for modelId={}, evaluationSet={}", modelId, evaluationSet);

    // 1. Get model and snapshot ID
    Model model =
        modelRepository
            .findById(modelId)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + modelId));

    Long snapshotId = model.getTrainingRecord().getSnapshotId();
    if (snapshotId == null) {
      throw new IllegalStateException("Model has no associated snapshot");
    }

    // 2. Query images for evaluation set from snapshot
    String splitValue = convertEvaluationSetToSplit(evaluationSet);
    List<SnapshotImage> images =
        snapshotImageRepository.findBySnapshotIdAndSplit(snapshotId, splitValue);

    if (images.isEmpty()) {
      return new ArrayList<>();
    }

    List<Long> imageIds = images.stream().map(SnapshotImage::getId).collect(Collectors.toList());

    // 3. Query ground truth labels and predictions
    List<SnapshotImageLabel> gtLabels =
        snapshotImageLabelRepository.findBySnapshotIdAndImageIdIn(snapshotId, imageIds);
    List<ImagePredictionLabel> predLabels =
        imagePredictionLabelRepository.findByModelId(modelId).stream()
            .filter(p -> imageIds.contains(p.getImage().getId()))
            .collect(Collectors.toList());

    // 4. Build GT and Pred maps: imageId -> labels
    Map<Long, List<SnapshotImageLabel>> gtLabelMap = new HashMap<>();
    for (SnapshotImageLabel label : gtLabels) {
      gtLabelMap.computeIfAbsent(label.getImageId(), k -> new ArrayList<>()).add(label);
    }

    Map<Long, List<ImagePredictionLabel>> predLabelMap = new HashMap<>();
    for (ImagePredictionLabel label : predLabels) {
      predLabelMap.computeIfAbsent(label.getImage().getId(), k -> new ArrayList<>()).add(label);
    }

    // 5. Build image DTOs with correctness flag
    List<ImageWithLabelsDTO> result = new ArrayList<>();
    for (SnapshotImage image : images) {
      List<SnapshotImageLabel> imageGtLabels =
          gtLabelMap.getOrDefault(image.getId(), new ArrayList<>());
      List<ImagePredictionLabel> imagePredLabels =
          predLabelMap.getOrDefault(image.getId(), new ArrayList<>());

      ImageWithLabelsDTO dto =
          buildImageWithLabelsDTO(image, imageGtLabels, imagePredLabels, snapshotId);

      // Calculate isCorrect: all GT classes match Pred classes
      boolean isCorrect = calculateIsCorrect(imageGtLabels, imagePredLabels);
      dto.setIsCorrect(isCorrect);

      result.add(dto);
    }

    return result;
  }

  private ImageWithLabelsDTO buildImageWithLabelsDTO(
      SnapshotImage image,
      List<SnapshotImageLabel> gtLabels,
      List<ImagePredictionLabel> predLabels) {
    List<LabelInfoDTO> gtLabelDTOs =
        gtLabels.stream()
            .map(l -> buildLabelInfoDTO(l, image.getSnapshotId()))
            .collect(Collectors.toList());

    List<LabelInfoDTO> predLabelDTOs =
        predLabels.stream().map(this::buildLabelInfoDTO).collect(Collectors.toList());

    return ImageWithLabelsDTO.builder()
        .imageId(image.getId())
        .fileName(image.getFileName())
        .imageUrl("/api/images/" + image.getId() + "/file")
        .evaluationSet(image.getSplit())
        .groundTruthLabels(gtLabelDTOs)
        .predictionLabels(predLabelDTOs)
        .isCorrect(null) // Will be set by caller if needed
        .build();
  }

  private ImageWithLabelsDTO buildImageWithLabelsDTO(
      SnapshotImage image,
      List<SnapshotImageLabel> gtLabels,
      List<ImagePredictionLabel> predLabels,
      Long snapshotId) {
    List<LabelInfoDTO> gtLabelDTOs =
        gtLabels.stream().map(l -> buildLabelInfoDTO(l, snapshotId)).collect(Collectors.toList());

    List<LabelInfoDTO> predLabelDTOs =
        predLabels.stream().map(this::buildLabelInfoDTO).collect(Collectors.toList());

    return ImageWithLabelsDTO.builder()
        .imageId(image.getId())
        .fileName(image.getFileName())
        .imageUrl("/api/images/" + image.getId() + "/file")
        .evaluationSet(image.getSplit())
        .groundTruthLabels(gtLabelDTOs)
        .predictionLabels(predLabelDTOs)
        .isCorrect(null) // Will be set by caller if needed
        .build();
  }

  private LabelInfoDTO buildLabelInfoDTO(SnapshotImageLabel label, Long snapshotId) {
    // Get class info from snapshot
    SnapshotProjectClass projectClass =
        snapshotProjectClassRepository.findBySnapshotId(snapshotId).stream()
            .filter(c -> c.getId().equals(label.getClassId()))
            .findFirst()
            .orElse(null);

    if (projectClass == null) {
      log.warn("Class not found for classId={} in snapshotId={}", label.getClassId(), snapshotId);
      return LabelInfoDTO.builder()
          .labelId(label.getId())
          .classId(label.getClassId())
          .className("Unknown")
          .classColor("#000000")
          .position(label.getPosition())
          .confidenceRate(null)
          .build();
    }

    return LabelInfoDTO.builder()
        .labelId(label.getId())
        .classId(label.getClassId())
        .className(projectClass.getClassName())
        .classColor(projectClass.getColorCode())
        .position(label.getPosition())
        .confidenceRate(null) // Ground truth labels don't have confidence rate
        .build();
  }

  private LabelInfoDTO buildLabelInfoDTO(ImagePredictionLabel label) {
    return LabelInfoDTO.builder()
        .labelId(label.getId())
        .classId(label.getProjectClass().getId())
        .className(label.getProjectClass().getClassName())
        .classColor(label.getProjectClass().getColorCode())
        .position(label.getPosition())
        .confidenceRate(label.getConfidenceRate())
        .build();
  }

  private boolean calculateIsCorrect(
      List<SnapshotImageLabel> gtLabels, List<ImagePredictionLabel> predLabels) {
    if (gtLabels.isEmpty() || predLabels.isEmpty()) {
      return false;
    }

    // Simple correctness check: GT class IDs match Pred class IDs
    List<Long> gtClassIds =
        gtLabels.stream().map(SnapshotImageLabel::getClassId).sorted().collect(Collectors.toList());
    List<Long> predClassIds =
        predLabels.stream()
            .map(l -> l.getProjectClass().getId())
            .sorted()
            .collect(Collectors.toList());

    return gtClassIds.equals(predClassIds);
  }

  private CellDetailResponse buildEmptyCellDetailResponse(
      String gtClassName, String predClassName, Long gtClassId, Long predClassId) {
    return CellDetailResponse.builder()
        .groundTruthClassId(gtClassId)
        .predictionClassId(predClassId)
        .groundTruthClassName(gtClassName)
        .predictionClassName(predClassName)
        .totalCount(0)
        .images(new ArrayList<>())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PredictionLabelDTO> getPredictionLabels(Long modelId, String evaluationSet) {
    log.info(
        "Getting prediction labels for model ID: {}, evaluationSet: {}", modelId, evaluationSet);

    // 1. Get model and snapshot ID
    Model model =
        modelRepository
            .findById(modelId)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + modelId));

    Long snapshotId = model.getTrainingRecord().getSnapshotId();
    if (snapshotId == null) {
      log.error("Model {} has no snapshot_id in training record", modelId);
      throw new IllegalStateException("Model has no associated snapshot");
    }

    // 2. Query images for evaluation set from snapshot
    String splitValue = convertEvaluationSetToSplit(evaluationSet);
    List<SnapshotImage> images =
        snapshotImageRepository.findBySnapshotIdAndSplit(snapshotId, splitValue);

    if (images.isEmpty()) {
      log.info(
          "No images found for snapshotId={}, split={}, returning empty list",
          snapshotId,
          splitValue);
      return new ArrayList<>();
    }

    List<Long> imageIds = images.stream().map(SnapshotImage::getId).collect(Collectors.toList());

    // 3. Query prediction labels for this model, filtered by imageIds
    List<ImagePredictionLabel> labels =
        imagePredictionLabelRepository.findByModelId(modelId).stream()
            .filter(p -> imageIds.contains(p.getImage().getId()))
            .collect(Collectors.toList());

    // 4. Convert to DTO with className
    List<PredictionLabelDTO> dtos =
        labels.stream()
            .map(
                label ->
                    PredictionLabelDTO.builder()
                        .id(label.getId())
                        .imageId(label.getImage().getId())
                        .classId(label.getProjectClass().getId())
                        .className(label.getProjectClass().getClassName())
                        .position(label.getPosition())
                        .confidenceRate(label.getConfidenceRate())
                        .createdAt(
                            label.getCreatedAt() != null ? label.getCreatedAt().toString() : null)
                        .createdBy(label.getCreatedBy())
                        .build())
            .collect(Collectors.toList());

    log.info(
        "Found {} prediction labels for model ID: {}, evaluationSet: {}",
        dtos.size(),
        modelId,
        evaluationSet);

    return dtos;
  }

  @Override
  @Transactional(readOnly = true)
  public List<GroundTruthLabelDTO> getGroundTruthLabels(Long modelId, String evaluationSet) {
    log.info(
        "Getting ground truth labels for model ID: {}, evaluationSet: {}", modelId, evaluationSet);

    // 1. Get model and snapshot ID
    Model model =
        modelRepository
            .findById(modelId)
            .orElseThrow(() -> new NotFoundException("Model not found with ID: " + modelId));

    // Get snapshot ID from training record
    Long snapshotId = model.getTrainingRecord().getSnapshotId();
    if (snapshotId == null) {
      log.error("Model {} has no snapshot_id in training record", modelId);
      throw new IllegalStateException("Model has no associated snapshot");
    }

    log.info("Using snapshot_id={} for model={}", snapshotId, modelId);

    // 2. Query images for evaluation set from snapshot
    String splitValue = convertEvaluationSetToSplit(evaluationSet);
    List<SnapshotImage> images =
        snapshotImageRepository.findBySnapshotIdAndSplit(snapshotId, splitValue);

    if (images.isEmpty()) {
      log.info(
          "No images found for snapshotId={}, split={}, returning empty list",
          snapshotId,
          splitValue);
      return new ArrayList<>();
    }

    List<Long> imageIds = images.stream().map(SnapshotImage::getId).collect(Collectors.toList());

    // 3. Query ground truth labels from snapshot, filtered by imageIds
    List<SnapshotImageLabel> labels =
        snapshotImageLabelRepository.findBySnapshotIdAndImageIdIn(snapshotId, imageIds);

    // 4. Get class information from snapshot
    List<SnapshotProjectClass> classes =
        snapshotProjectClassRepository.findBySnapshotId(snapshotId);
    Map<Long, String> classNameMap =
        classes.stream()
            .collect(
                Collectors.toMap(SnapshotProjectClass::getId, SnapshotProjectClass::getClassName));

    // 5. Convert to DTO with className
    List<GroundTruthLabelDTO> dtos =
        labels.stream()
            .map(
                label ->
                    GroundTruthLabelDTO.builder()
                        .id(label.getId())
                        .imageId(label.getImageId())
                        .classId(label.getClassId())
                        .className(classNameMap.getOrDefault(label.getClassId(), "Unknown"))
                        .position(label.getPosition())
                        .createdAt(
                            label.getCreatedAt() != null ? label.getCreatedAt().toString() : null)
                        .createdBy(label.getCreatedBy())
                        .build())
            .collect(Collectors.toList());

    log.info(
        "Found {} ground truth labels for snapshot ID: {}, evaluationSet: {}",
        dtos.size(),
        snapshotId,
        evaluationSet);

    return dtos;
  }
}
