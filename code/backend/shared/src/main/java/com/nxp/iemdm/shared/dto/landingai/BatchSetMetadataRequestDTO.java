package com.nxp.iemdm.shared.dto.landingai;

import java.util.List;
import java.util.Map;

/** DTO for batch set metadata request */
public class BatchSetMetadataRequestDTO {
  private List<Long> imageIds;
  private Map<String, String> metadata;

  public BatchSetMetadataRequestDTO() {}

  public BatchSetMetadataRequestDTO(List<Long> imageIds, Map<String, String> metadata) {
    this.imageIds = imageIds;
    this.metadata = metadata;
  }

  public List<Long> getImageIds() {
    return imageIds;
  }

  public void setImageIds(List<Long> imageIds) {
    this.imageIds = imageIds;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }
}
