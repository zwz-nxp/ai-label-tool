package com.nxp.iemdm.shared.dto.landingai;

import com.nxp.iemdm.shared.validator.ValidJson;
import com.nxp.iemdm.shared.validator.ValidModelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for model parameter update requests with validation annotations. Used when updating an
 * existing model parameter configuration via the API. Validates: Requirements 7.1, 7.2, 7.3, 7.4,
 * 7.5
 */
public class ModelParamUpdateRequest {

  @NotBlank(message = "Model name is required")
  @Size(max = 50, message = "Model name must not exceed 50 characters")
  private String modelName;

  @NotBlank(message = "Model type is required")
  @ValidModelType
  private String modelType;

  @NotBlank(message = "Parameters are required")
  @ValidJson
  private String parameters;

  public ModelParamUpdateRequest() {}

  public ModelParamUpdateRequest(String modelName, String modelType, String parameters) {
    this.modelName = modelName;
    this.modelType = modelType;
    this.parameters = parameters;
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
}
