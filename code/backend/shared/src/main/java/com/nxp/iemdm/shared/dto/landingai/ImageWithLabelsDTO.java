package com.nxp.iemdm.shared.dto.landingai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for image with ground truth and prediction labels. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageWithLabelsDTO {

  /** Image ID */
  private Long imageId;

  /** Image file name */
  private String fileName;

  /** Image URL or path */
  private String imageUrl;

  /** Evaluation set (TRAIN, DEV, TEST) */
  private String evaluationSet;

  /** Ground truth labels */
  private List<LabelInfoDTO> groundTruthLabels;

  /** Prediction labels */
  private List<LabelInfoDTO> predictionLabels;

  /** Whether the prediction is correct (GT matches Pred) */
  private Boolean isCorrect;
}
