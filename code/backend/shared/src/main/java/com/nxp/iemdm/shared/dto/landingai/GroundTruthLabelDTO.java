package com.nxp.iemdm.shared.dto.landingai;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ground truth label with class information. Used for Adjust Threshold Dialog.
 * Requirements: 26.5, 31.1, 35.3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroundTruthLabelDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** Label ID */
  private Long id;

  /** Image ID */
  private Long imageId;

  /** Class ID */
  private Long classId;

  /** Class name */
  private String className;

  /** Position (JSON string) */
  private String position;

  /** Created at timestamp */
  private String createdAt;

  /** Created by user */
  private String createdBy;
}
