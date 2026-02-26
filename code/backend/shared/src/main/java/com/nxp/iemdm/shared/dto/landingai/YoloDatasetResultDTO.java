package com.nxp.iemdm.shared.dto.landingai;

import com.nxp.iemdm.model.landingai.DatabricksRequest;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for YOLO dataset generation result. Contains information about generated zip files. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoloDatasetResultDTO {

  /** The model alias used for the dataset */
  private String modelAlias;

  /** The project ID */
  private Long projectId;

  /** The project type (Object Detection, Segmentation, Classification) */
  private String projectType;

  /** The base directory path where files are generated */
  private String basePath;

  /** List of generated zip file paths */
  private List<String> zipFilePaths;

  /** Total number of images processed */
  private int totalImages;

  /** Number of training images */
  private int trainingImages;

  /** Number of validation images */
  private int validationImages;

  /** Number of test images */
  private int testImages;

  /** Number of classes */
  private int classCount;

  /** Total size of all zip files in bytes */
  private long totalSize;

  /** Whether the generation was successful */
  private boolean success;

  /** Error message if generation failed */
  private String errorMessage;

  /** Databricks request containing trackId, zipFilenames and zipPath */
  private DatabricksRequest databricksRequest;
}
