package com.nxp.iemdm.shared.dto.landingai;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model with Metrics DTO
 *
 * <p>Combines Model and ConfidentialReport data for frontend use. Avoids JPA association and JSON
 * serialization issues.
 */
@Data
@NoArgsConstructor
public class ModelWithMetricsDto implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  // ============================================================================
  // Model basic fields
  // ============================================================================
  private Long id;
  private Long projectId;
  private Long trainingRecordId; // Training record ID for loading training details
  private String modelAlias;
  private String modelVersion;
  private String trackId;
  private String createdBy;
  private Boolean isFavorite;
  private Instant createdAt;

  // ============================================================================
  // ConfidentialReport evaluation metric fields
  // ============================================================================
  /** Training set correct rate (0-100) */
  private Integer trainingCorrectRate;

  /** Validation set correct rate (0-100) */
  private Integer devCorrectRate;

  /** Test set correct rate (0-100 or null) */
  private Integer testCorrectRate;

  /** Confidence threshold (0-100, frontend will divide by 100 to convert to 0-1) */
  private Integer confidenceThreshold;

  // ============================================================================
  // Model performance metrics fields (from la_model table)
  // ============================================================================
  /** Training set F1 rate (0-100, with 1 decimal place) */
  private Double trainingF1Rate;

  /** Training set Precision rate (0-100, with 1 decimal place) */
  private Double trainingPrecisionRate;

  /** Training set Recall rate (0-100, with 1 decimal place) */
  private Double trainingRecallRate;

  /** Dev set F1 rate (0-100, with 1 decimal place) */
  private Double devF1Rate;

  /** Dev set Precision rate (0-100, with 1 decimal place) */
  private Double devPrecisionRate;

  /** Dev set Recall rate (0-100, with 1 decimal place) */
  private Double devRecallRate;

  /** Test set F1 rate (0-100, with 1 decimal place) */
  private Double testF1Rate;

  /** Test set Precision rate (0-100, with 1 decimal place) */
  private Double testPrecisionRate;

  /** Test set Recall rate (0-100, with 1 decimal place) */
  private Double testRecallRate;

  private Integer imageCount;
  private Integer labelCount;

  // ============================================================================
  // Constructor: Used for JPQL queries
  // ============================================================================
  /**
   * Full constructor for creating DTO in JPQL queries
   *
   * @param id Model ID
   * @param projectId Project ID
   * @param trainingRecordId Training record ID
   * @param modelAlias Model alias
   * @param modelVersion Model version
   * @param trackId Track ID
   * @param createdBy Creator
   * @param isFavorite Is favorite
   * @param createdAt Created timestamp
   * @param trainingCorrectRate Training set correct rate
   * @param devCorrectRate Validation set correct rate
   * @param testCorrectRate Test set correct rate
   * @param confidenceThreshold Confidence threshold
   * @param trainingF1Rate Training F1 rate (Double)
   * @param trainingPrecisionRate Training Precision rate (Double)
   * @param trainingRecallRate Training Recall rate (Double)
   * @param devF1Rate Dev F1 rate (Double)
   * @param devPrecisionRate Dev Precision rate (Double)
   * @param devRecallRate Dev Recall rate (Double)
   * @param testF1Rate Test F1 rate (Double)
   * @param testPrecisionRate Test Precision rate (Double)
   * @param testRecallRate Test Recall rate (Double)
   * @param imageCount Image count
   * @param labelCount Label count
   */
  public ModelWithMetricsDto(
      Long id,
      Long projectId,
      Long trainingRecordId,
      String modelAlias,
      String modelVersion,
      String trackId,
      String createdBy,
      Boolean isFavorite,
      Instant createdAt,
      Integer trainingCorrectRate,
      Integer devCorrectRate,
      Integer testCorrectRate,
      Integer confidenceThreshold,
      Double trainingF1Rate,
      Double trainingPrecisionRate,
      Double trainingRecallRate,
      Double devF1Rate,
      Double devPrecisionRate,
      Double devRecallRate,
      Double testF1Rate,
      Double testPrecisionRate,
      Double testRecallRate,
      Integer imageCount,
      Integer labelCount) {
    this.id = id;
    this.projectId = projectId;
    this.trainingRecordId = trainingRecordId;
    this.modelAlias = modelAlias;
    this.modelVersion = modelVersion;
    this.trackId = trackId;
    this.createdBy = createdBy;
    this.isFavorite = isFavorite != null ? isFavorite : false;
    this.createdAt = createdAt;
    this.trainingCorrectRate = trainingCorrectRate;
    this.devCorrectRate = devCorrectRate;
    this.testCorrectRate = testCorrectRate;
    this.confidenceThreshold = confidenceThreshold;
    this.trainingF1Rate = trainingF1Rate;
    this.trainingPrecisionRate = trainingPrecisionRate;
    this.trainingRecallRate = trainingRecallRate;
    this.devF1Rate = devF1Rate;
    this.devPrecisionRate = devPrecisionRate;
    this.devRecallRate = devRecallRate;
    this.testF1Rate = testF1Rate;
    this.testPrecisionRate = testPrecisionRate;
    this.testRecallRate = testRecallRate;
    this.imageCount = imageCount;
    this.labelCount = labelCount;
  }
}
