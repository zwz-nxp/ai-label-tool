package com.nxp.iemdm.shared.dto.landingai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Databricks API training results response. Maps to the JSON response from
 * TrainingResultsResponse (Mock Service). 使用 snake_case 欄位名稱以對應 Mock Service 的 JSON 格式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResultResponse {

  @JsonProperty("track_id")
  private String trackId;

  @JsonProperty("model_full_name")
  private String modelFullName;

  @JsonProperty("model_version")
  private String modelVersion;

  @JsonProperty("training_f1_rate")
  private String trainingF1Rate;

  @JsonProperty("training_precision_rate")
  private String trainingPrecisionRate;

  @JsonProperty("training_recall_rate")
  private String trainingRecallRate;

  @JsonProperty("training_correct_rate")
  private String trainingCorrectRate;

  @JsonProperty("confidence_threshold")
  private String confidenceThreshold;

  @JsonProperty("dev_f1_rate")
  private String devF1Rate;

  @JsonProperty("dev_precision_rate")
  private String devPrecisionRate;

  @JsonProperty("dev_recall_rate")
  private String devRecallRate;

  @JsonProperty("dev_correct_rate")
  private String devCorrectRate;

  @JsonProperty("test_f1_rate")
  private String testF1Rate;

  @JsonProperty("test_precision_rate")
  private String testPrecisionRate;

  @JsonProperty("test_recall_rate")
  private String testRecallRate;

  @JsonProperty("test_correct_rate")
  private String testCorrectRate;

  @JsonProperty("loss_chart")
  private List<LossChartPoint> lossChart;

  @JsonProperty("validation_chart")
  private List<ValidationChartPoint> validationChart;

  @JsonProperty("prediction_images")
  private List<PredictionImage> predictionImages;

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
  public static class ValidationChartPoint {
    private String map;
    private String epoch;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PredictionImage {
    private String image;
    private List<Prediction> predictions;

    @JsonProperty("ground_truth")
    private List<GroundTruth> groundTruth;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Prediction {
    private double confidence;
    private List<Double> bbox; // [xcenter, ycenter, width, height] normalized

    @JsonProperty("class_id")
    private int classId;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroundTruth {
    @JsonProperty("class_id")
    private int classId;

    private List<Double> bbox; // [xcenter, ycenter, width, height] normalized
  }

  // Getter methods for backward compatibility (使用舊的 camelCase 名稱)
  public String getF1Rate() {
    return trainingF1Rate;
  }

  public String getPrecisionRate() {
    return trainingPrecisionRate;
  }

  public String getRecallRate() {
    return trainingRecallRate;
  }

  public String getConfidenceThreshhold() {
    return confidenceThreshold;
  }
}
