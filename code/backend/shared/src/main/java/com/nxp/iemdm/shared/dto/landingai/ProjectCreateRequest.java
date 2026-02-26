package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for project creation requests with validation annotations. Used when creating a new project
 * via the API.
 */
public class ProjectCreateRequest {

  @NotBlank(message = "Project name is required")
  @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
  private String name;

  @NotBlank(message = "Project type is required")
  private String type;

  private String modelName;

  private String groupName;

  public ProjectCreateRequest() {}

  public ProjectCreateRequest(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public ProjectCreateRequest(String name, String type, String modelName) {
    this.name = name;
    this.type = type;
    this.modelName = modelName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
}
