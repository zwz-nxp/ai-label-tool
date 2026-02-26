package com.nxp.iemdm.shared.dto.databricks;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for model testing results from Databricks API.
 *
 * <p>Contains overall model metrics and per-image prediction results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestModelResponse {

  /** F1 score rate */
  private String f1Rate;

  /** Precision rate */
  private String precisionRate;

  /** Recall rate */
  private String recallRate;

  /** Model version used for prediction */
  private String modelVersion;

  /** Training set correct rate */
  private String trainingCorrectRate;

  /** Development set correct rate */
  private String devCorrectRate;

  /** Test set correct rate */
  private String testCorrectRate;

  /**
   * Confidence threshold used for filtering.
   *
   * <p>Note: The API uses 'threshhold' spelling (with double 'h')
   */
  private String confidenceThreshhold;

  /** List of prediction results for each image */
  private List<ImagePrediction> imageList;
}
