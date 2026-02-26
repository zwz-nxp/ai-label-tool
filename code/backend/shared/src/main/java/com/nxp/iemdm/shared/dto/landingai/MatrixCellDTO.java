package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for a single cell in the confusion matrix. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrixCellDTO {

  /** Ground truth class ID */
  private Long groundTruthClassId;

  /** Prediction class ID */
  private Long predictionClassId;

  /** Number of images with this GT×Pred combination */
  private Integer count;

  /** Whether this is a diagonal cell (correct prediction) */
  private Boolean isDiagonal;
}
