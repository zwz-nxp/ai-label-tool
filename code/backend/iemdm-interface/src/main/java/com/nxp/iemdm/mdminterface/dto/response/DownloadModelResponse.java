package com.nxp.iemdm.mdminterface.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadModelResponse {
  private String modelFullName;
  private Integer version;
  private String trackId;
  private ArtifactInfo artifact;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ArtifactInfo {
    private String downloadUrl;
  }
}
