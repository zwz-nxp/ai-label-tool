package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for epoch-level metrics from results.csv. Contains loss and validation metrics for a single
 * training epoch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EpochMetrics {

  /** Epoch number */
  private Integer epoch;

  /** Training time for this epoch */
  private String time;

  /** Training box loss value */
  private Double trainBoxLoss;

  /** Validation mAP50(B) metric */
  private Double metricsMAP50B;
}
