package com.nxp.iemdm.mdminterface.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for submitting training data. Supports multiple zip files for large datasets that need to
 * be split (>5GB limit per file).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDataRequest {

  @JsonProperty("trackId")
  private String trackId; // Explicit trackId for the training job

  @JsonProperty("zipFilenames")
  private List<String> zipFilenames; // e.g., ["track-001_1.zip", "track-001_2.zip"]

  @JsonProperty("zipPath")
  private String zipPath; // e.g., "C:/path/to/files/"

  /** Gets the first zip filename (for mock processing or single-file scenarios). */
  public String getFirstZipFilename() {
    if (zipFilenames != null && !zipFilenames.isEmpty()) {
      return zipFilenames.get(0);
    }
    return null;
  }
}
