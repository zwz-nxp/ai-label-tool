package com.nxp.iemdm.mdminterface.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for classification training results. Different from detection - no bounding boxes, just
 * class predictions per image.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResultsResponse {

  @JsonProperty("track_id")
  private String trackId;

  @JsonProperty("model_full_name")
  private String modelFullName;

  @JsonProperty("model_version")
  private String modelVersion;

  @JsonProperty("confidence_threshold")
  private String confidenceThreshold;

  // Metrics
  @JsonProperty("training_accuracy")
  private String trainingAccuracy;

  @JsonProperty("training_f1_rate")
  private String trainingF1Rate;

  @JsonProperty("training_precision_rate")
  private String trainingPrecisionRate;

  @JsonProperty("training_recall_rate")
  private String trainingRecallRate;

  @JsonProperty("dev_accuracy")
  private String devAccuracy;

  @JsonProperty("dev_f1_rate")
  private String devF1Rate;

  @JsonProperty("dev_precision_rate")
  private String devPrecisionRate;

  @JsonProperty("dev_recall_rate")
  private String devRecallRate;

  @JsonProperty("test_accuracy")
  private String testAccuracy;

  @JsonProperty("test_f1_rate")
  private String testF1Rate;

  @JsonProperty("test_precision_rate")
  private String testPrecisionRate;

  @JsonProperty("test_recall_rate")
  private String testRecallRate;

  // Charts
  @JsonProperty("loss_chart")
  private List<LossChartPoint> lossChart;

  @JsonProperty("accuracy_chart")
  private List<AccuracyChartPoint> accuracyChart;

  // Class mapping (index -> name)
  @JsonProperty("class_names")
  private Map<String, String> classNames;

  // Per-image predictions
  @JsonProperty("predictions")
  private List<ImagePrediction> predictions;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LossChartPoint {
    private String loss;
    private String epoch;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AccuracyChartPoint {
    private String accuracy;
    private String epoch;
  }

  /**
   * Classification prediction for a single image. ground_truth and prediction are class indices as
   * strings.
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ImagePrediction {
    private String image;

    @JsonProperty("ground_truth")
    private String groundTruth;

    private String prediction;
    private double confidence;
  }
}
