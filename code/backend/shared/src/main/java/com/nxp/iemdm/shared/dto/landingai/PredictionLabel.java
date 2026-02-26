package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for prediction label data from prediction file. Contains prediction results for a single
 * image.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionLabel {

  /** Image ID */
  private Long imageId;

  /** Class sequence number from API (needs to be mapped to actual class_id) */
  private Integer classSequence;

  /** Bounding box position (JSON string) */
  private String position;

  /** Confidence rate (0-100) */
  private Integer confidenceRate;
}
