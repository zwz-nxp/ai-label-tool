package com.nxp.iemdm.mdminterface.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingResultsRequest {
  private String modelFullName;
  private String trackId;

  /** Databricks run ID for checking job status before downloading results */
  private Long runId;

  public TrainingResultsRequest(String modelFullName, String trackId) {
    this.modelFullName = modelFullName;
    this.trackId = trackId;
  }
}
