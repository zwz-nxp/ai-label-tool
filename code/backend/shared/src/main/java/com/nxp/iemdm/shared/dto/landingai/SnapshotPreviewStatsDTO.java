package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for snapshot preview statistics. Contains image status and split distribution information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotPreviewStatsDTO {
  /** Number of labeled images */
  private Integer labeled;

  /** Number of unlabeled images */
  private Integer unlabeled;

  /** Number of images marked as no_class (is_labeled=true, is_no_class=true) */
  private Integer noClass;

  /** Number of images in training split */
  private Integer trainCount;

  /** Number of images in dev split */
  private Integer devCount;

  /** Number of images in test split */
  private Integer testCount;

  /** Number of images with no split assigned */
  private Integer unassignedCount;
}
