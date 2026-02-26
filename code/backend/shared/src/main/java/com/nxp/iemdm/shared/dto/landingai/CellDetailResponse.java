package com.nxp.iemdm.shared.dto.landingai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for cell detail (images with specific GT×Pred combination). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CellDetailResponse {

  /** Ground truth class ID */
  private Long groundTruthClassId;

  /** Prediction class ID */
  private Long predictionClassId;

  /** Ground truth class name */
  private String groundTruthClassName;

  /** Prediction class name */
  private String predictionClassName;

  /** Total count of images */
  private Integer totalCount;

  /** List of images with labels and predictions */
  private List<ImageWithLabelsDTO> images;
}
