package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO for updating project information. Only allows updating name and model name. */
public class ProjectUpdateRequest {

  @NotBlank(message = "Project name is required")
  @Size(min = 3, max = 255, message = "Project name must be between 3 and 255 characters")
  private String name;

  @NotBlank(message = "Model name is required")
  private String modelName;

  private String groupName;

  public ProjectUpdateRequest() {}

  public ProjectUpdateRequest(String name, String modelName) {
    this.name = name;
    this.modelName = modelName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
