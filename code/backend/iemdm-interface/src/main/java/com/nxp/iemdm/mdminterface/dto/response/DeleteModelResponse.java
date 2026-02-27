package com.nxp.iemdm.mdminterface.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteModelResponse {
  private String stateCode;
  private String message;
  private ModelInfo model;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ModelInfo {
    private String modelFullName;
    private Integer version;
    private String trackId;
  }
}
