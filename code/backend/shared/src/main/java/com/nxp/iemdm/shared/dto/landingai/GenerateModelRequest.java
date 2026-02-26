package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating a new model with adjusted confidence threshold Requirements: 31.1,
 * 33.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateModelRequest {

  /** Source model ID */
  private Long sourceModelId;

  /** New confidence threshold (0.0 - 1.0) */
  private Double newThreshold;

  /** Recalculated metrics for all evaluation sets */
  private RecalculatedMetrics recalculatedMetrics;
}
