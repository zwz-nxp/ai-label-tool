package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * DTO for complete project information including all fields. Used for detailed project views and
 * API responses.
 */
public class ProjectDTO {

  private Long id;

  @NotBlank(message = "Project name is required")
  @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
  private String name;

  private String status;

  @NotBlank(message = "Project type is required")
  private String type;

  private String modelName;

  private String groupName;

  private Long locationId;

  private Instant createdAt;

  private String createdBy;

  public ProjectDTO() {}

  public ProjectDTO(
      Long id,
      String name,
      String status,
      String type,
      String modelName,
      String groupName,
      Long locationId,
      Instant createdAt,
      String createdBy) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.type = type;
    this.modelName = modelName;
    this.groupName = groupName;
    this.locationId = locationId;
    this.createdAt = createdAt;
    this.createdBy = createdBy;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Long getLocationId() {
    return locationId;
  }

  public void setLocationId(Long locationId) {
    this.locationId = locationId;
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
