package com.nxp.iemdm.shared.dto.landingai;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.*;

/**
 * DTO for split preview data containing distribution information. Used to display the preview of
 * how images are distributed across train/dev/test splits.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SplitPreviewDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** Total number of labeled images in the dataset */
  private int totalImages;

  /** Number of images without assigned split */
  private int unassignedCount;

  /** Distribution data grouped by class */
  private List<ClassSplitDataDTO> byClass;

  /** Distribution data grouped by split type */
  private List<SplitClassDataDTO> bySplit;

  /** Split distribution data for a single class. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ClassSplitDataDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /** Name of the class */
    private String className;

    /** Color assigned to the class for visualization */
    private String classColor;

    /** Number of images in training set */
    private int train;

    /** Number of images in development set */
    private int dev;

    /** Number of images in test set */
    private int test;

    /** Number of unassigned images */
    private int unassigned;
  }

  /** Class distribution data for a single split type. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SplitClassDataDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /** Type of split (train, dev, test, unassigned) */
    private String splitType;

    /** Classes and their counts within this split */
    private List<SplitClassCountDTO> classes;
  }

  /** Class count within a split. */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class SplitClassCountDTO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /** Name of the class */
    private String className;

    /** Color assigned to the class */
    private String classColor;

    /** Number of images of this class in the split */
    private int count;
  }
}
