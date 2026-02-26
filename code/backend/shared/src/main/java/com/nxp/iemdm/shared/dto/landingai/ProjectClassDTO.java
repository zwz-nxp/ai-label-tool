package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for ProjectClass with additional computed fields like label count */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectClassDTO {
  private Long id;
  private Long projectId;
  private String className;
  private String description;
  private String colorCode;
  private Instant createdAt;
  private String createdBy;
  private Integer labelCount;
}
