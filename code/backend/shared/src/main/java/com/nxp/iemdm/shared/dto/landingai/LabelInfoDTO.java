package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for label information (ground truth or prediction). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelInfoDTO {

  /** Label ID */
  private Long labelId;

  /** Class ID */
  private Long classId;

  /** Class name */
  private String className;

  /** Class color (hex format) */
  private String classColor;

  /** Bounding box position (JSON string) */
  private String position;

  /** Confidence rate (0-100, only for predictions) */
  private Integer confidenceRate;
}
