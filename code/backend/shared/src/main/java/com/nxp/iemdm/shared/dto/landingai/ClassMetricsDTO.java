package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for class-level metrics (Precision, Recall, TP, FP, FN). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMetricsDTO {

  /** Class ID */
  private Long classId;

  /** True Positives count */
  private Integer truePositives;

  /** False Positives count */
  private Integer falsePositives;

  /** False Negatives count */
  private Integer falseNegatives;

  /** Precision (TP / (TP + FP)), null if undefined */
  private Double precision;

  /** Recall (TP / (TP + FN)), null if undefined */
  private Double recall;
}
