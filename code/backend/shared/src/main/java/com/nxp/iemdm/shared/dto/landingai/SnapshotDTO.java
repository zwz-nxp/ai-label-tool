package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotDTO {
  private Long id;
  private Long projectId;
  private String name;
  private String description;
  private Integer imageCount; // Calculated dynamically
  private Integer classCount; // Calculated dynamically
  private Instant createdAt;
  private String createdBy;
}
