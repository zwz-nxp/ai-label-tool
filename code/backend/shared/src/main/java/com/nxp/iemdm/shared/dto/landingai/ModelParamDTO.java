package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;

/**
 * DTO for complete model parameter information including all fields. Used for detailed model
 * parameter views and API responses.
 */
public class ModelParamDTO {

  private Long id;

  private Long locationId;

  private String locationName;

  private String modelName;

  private String modelType;

  private String parameters;

  private Instant createdAt;

  private String createdBy;

  public ModelParamDTO() {}

  public ModelParamDTO(
      Long id,
      Long locationId,
      String locationName,
      String modelName,
      String modelType,
      String parameters,
      Instant createdAt,
      String createdBy) {
    this.id = id;
    this.locationId = locationId;
    this.locationName = locationName;
    this.modelName = modelName;
    this.modelType = modelType;
    this.parameters = parameters;
    this.createdAt = createdAt;
    this.createdBy = createdBy;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getLocationId() {
    return locationId;
  }

  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getModelType() {
    return modelType;
  }

  public void setModelType(String modelType) {
    this.modelType = modelType;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }
}
