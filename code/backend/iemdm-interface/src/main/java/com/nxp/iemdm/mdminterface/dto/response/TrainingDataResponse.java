package com.nxp.iemdm.mdminterface.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainingDataResponse {
  private String errorMessage;
  private String trackId;

  /** Databricks run ID returned when a job is submitted. Used to poll job status. */
  private Long runId;

  /** Constructor for mock responses (trackId only, no runId) */
  public TrainingDataResponse(String errorMessage, String trackId) {
    this.errorMessage = errorMessage;
    this.trackId = trackId;
  }
}
