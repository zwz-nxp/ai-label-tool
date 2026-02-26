package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;

/**
 * DTO for label overlay information displayed on image thumbnails. Contains label details including
 * class information, position, confidence, and annotation type.
 */
public class LabelOverlayDTO {

  private Long id;
  private Long classId;
  private String className;
  private String colorCode;
  private String position; // JSON: {x, y, width, height} in percentages
  private Integer confidenceRate;
  private String annotationType; // Ground Truth, Prediction
  private Instant createdAt; // Label creation timestamp for sorting

  public LabelOverlayDTO() {}

  public LabelOverlayDTO(
      Long id,
      Long classId,
      String className,
      String colorCode,
      String position,
      Integer confidenceRate,
      String annotationType) {
    this.id = id;
    this.classId = classId;
    this.className = className;
    this.colorCode = colorCode;
    this.position = position;
    this.confidenceRate = confidenceRate;
    this.annotationType = annotationType;
  }

  public LabelOverlayDTO(
      Long id,
      Long classId,
      String className,
      String colorCode,
      String position,
      Integer confidenceRate,
      String annotationType,
      Instant createdAt) {
    this.id = id;
    this.classId = classId;
    this.className = className;
    this.colorCode = colorCode;
    this.position = position;
    this.confidenceRate = confidenceRate;
    this.annotationType = annotationType;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getClassId() {
    return classId;
  }

  public void setClassId(Long classId) {
    this.classId = classId;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getColorCode() {
    return colorCode;
  }

  public void setColorCode(String colorCode) {
    this.colorCode = colorCode;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public Integer getConfidenceRate() {
    return confidenceRate;
  }

  public void setConfidenceRate(Integer confidenceRate) {
    this.confidenceRate = confidenceRate;
  }

  public String getAnnotationType() {
    return annotationType;
  }

  public void setAnnotationType(String annotationType) {
    this.annotationType = annotationType;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
