package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new project from a snapshot. Contains the project name for the new
 * project to be created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectFromSnapshotRequest {

  @NotBlank(message = "Project name is required")
  @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
  private String projectName;
}
