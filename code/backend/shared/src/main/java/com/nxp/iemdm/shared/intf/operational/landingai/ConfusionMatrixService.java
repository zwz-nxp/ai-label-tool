package com.nxp.iemdm.shared.intf.operational.landingai;

import com.nxp.iemdm.shared.dto.landingai.CellDetailResponse;
import com.nxp.iemdm.shared.dto.landingai.ConfusionMatrixResponse;
import com.nxp.iemdm.shared.dto.landingai.GroundTruthLabelDTO;
import com.nxp.iemdm.shared.dto.landingai.ImageWithLabelsDTO;
import com.nxp.iemdm.shared.dto.landingai.PredictionLabelDTO;
import java.util.List;

/** Service interface for confusion matrix operations. */
public interface ConfusionMatrixService {

  /**
   * Calculate confusion matrix for a model and evaluation set.
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return Confusion matrix response with grid, classes, and metrics
   */
  ConfusionMatrixResponse calculateConfusionMatrix(Long modelId, String evaluationSet);

  /**
   * Get detail for a specific cell (GT×Pred combination).
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @param gtClassId Ground truth class ID
   * @param predClassId Prediction class ID
   * @return Cell detail response with images
   */
  CellDetailResponse getCellDetail(
      Long modelId, String evaluationSet, Long gtClassId, Long predClassId);

  /**
   * Get all images in evaluation set with correctness indicators.
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of images with ground truth and prediction labels
   */
  List<ImageWithLabelsDTO> getAllImages(Long modelId, String evaluationSet);

  /**
   * Get prediction labels for a model filtered by evaluation set. Backend 根據 evaluationSet 參數過濾資料，
   * 只回傳指定 evaluation set 的資料。 Requirements: 26.5, 31.1, 35.3
   *
   * @param modelId Model ID
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of prediction labels for the specified evaluation set
   */
  List<PredictionLabelDTO> getPredictionLabels(Long modelId, String evaluationSet);

  /**
   * Get ground truth labels for a model's project filtered by evaluation set. Backend 根據
   * evaluationSet 參數過濾資料，只回傳指定 evaluation set 的資料。 Requirements: 26.5, 31.1, 35.3
   *
   * @param modelId Model ID (用來找到對應的 snapshot)
   * @param evaluationSet Evaluation set (TRAIN, DEV, TEST)
   * @return List of ground truth labels for the specified evaluation set
   */
  List<GroundTruthLabelDTO> getGroundTruthLabels(Long modelId, String evaluationSet);
}
