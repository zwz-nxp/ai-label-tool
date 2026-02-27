package com.nxp.iemdm.mdminterface.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response for classification training submission. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationTrainingResponse {

  @JsonProperty("error")
  private String error;

  @JsonProperty("trackId")
  private String trackId;
}
