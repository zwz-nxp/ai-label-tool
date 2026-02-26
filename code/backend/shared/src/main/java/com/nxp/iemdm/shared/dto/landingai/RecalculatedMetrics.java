package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Recalculated metrics for all evaluation sets Requirements: 31.1 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecalculatedMetrics {

  /** Train F1 score */
  private Double trainF1;

  /** Train Precision */
  private Double trainPrecision;

  /** Train Recall */
  private Double trainRecall;

  /** Dev F1 score */
  private Double devF1;

  /** Dev Precision */
  private Double devPrecision;

  /** Dev Recall */
  private Double devRecall;

  /** Test F1 score */
  private Double testF1;

  /** Test Precision */
  private Double testPrecision;

  /** Test Recall */
  private Double testRecall;
}
