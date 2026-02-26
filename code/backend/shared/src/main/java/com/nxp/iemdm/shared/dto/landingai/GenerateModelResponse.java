package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for generating a new model with adjusted confidence threshold Requirements: 31.1,
 * 33.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateModelResponse {

  /** New model ID */
  private Long newModelId;

  /** Model alias */
  private String modelAlias;

  /** Confidence threshold */
  private Double confidenceThreshold;
}
