package com.nxp.iemdm.mdminterface.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing local file paths for downloaded training results. Files are downloaded
 * from Databricks volume to local server path.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingResultsFilePathsResponse {

  /** Track ID for the training job */
  @JsonProperty("trackId")
  private String trackId;

  /** Databricks run ID for the job */
  @JsonProperty("runId")
  private Long runId;

  /** Local path to results.csv file */
  @JsonProperty("results")
  private String results;

  /** Local path to args.yaml file */
  @JsonProperty("args")
  private String args;

  /** Local path to objectdetection_predictions_metrics.json file */
  @JsonProperty("predictions_metrics")
  private String predictionMetrics;

  /** Local path to YOLO_ObjectDetection_train_predictions.json file */
  @JsonProperty("train_predictions")
  private String trainPrediction;

  /** Local path to YOLO_ObjectDetection_val_predictions.json file */
  @JsonProperty("val_predictions")
  private String valPrediction;

  /** Local path to YOLO_ObjectDetection_test_predictions.json file */
  @JsonProperty("test_predictions")
  private String testPrediction;

  /** Error message if download failed */
  @JsonProperty("error")
  private String error;

  /** Status of the download operation */
  @JsonProperty("status")
  private String status;
}
