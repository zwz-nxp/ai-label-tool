package com.nxp.iemdm.mdminterface.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for submitting classification training data. Same structure as detection training
 * request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationTrainingRequest {

  @JsonProperty("trackId")
  private String trackId;

  @JsonProperty("zipFilenames")
  private List<String> zipFilenames;

  @JsonProperty("zipPath")
  private String zipPath;

  public String getFirstZipFilename() {
    if (zipFilenames != null && !zipFilenames.isEmpty()) {
      return zipFilenames.get(0);
    }
    return null;
  }
}
