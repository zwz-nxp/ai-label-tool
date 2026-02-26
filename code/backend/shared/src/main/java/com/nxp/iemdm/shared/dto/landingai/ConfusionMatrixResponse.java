package com.nxp.iemdm.shared.dto.landingai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for confusion matrix data. Contains the matrix grid, class information, and metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfusionMatrixResponse {

  /** List of class information (id, name, color) in display order for Ground Truth axis */
  private List<ClassInfoDTO> classes;

  /** List of class information for Prediction axis (includes "No prediction" at the end) */
  private List<ClassInfoDTO> predictionClasses;

  /** N×N matrix of prediction counts [groundTruthIndex][predictionIndex] */
  private List<List<MatrixCellDTO>> matrix;

  /** Class-level metrics (Precision, Recall, TP, FP, FN) for each class */
  private List<ClassMetricsDTO> classMetrics;

  /** Maximum count value in the matrix (for color scaling) */
  private Integer maxCount;
}
