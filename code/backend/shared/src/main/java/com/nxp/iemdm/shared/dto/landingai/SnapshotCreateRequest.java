package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotCreateRequest {
  @NotNull(message = "Project ID is required")
  private Long projectId;

  @NotBlank(message = "Snapshot name is required")
  private String snapshotName;

  private String description;
}
