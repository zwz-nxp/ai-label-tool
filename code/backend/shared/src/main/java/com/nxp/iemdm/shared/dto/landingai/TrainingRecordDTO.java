package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordDTO {
  private Long id;
  private Long projectId;
  private String status;
  private String modelAlias;
  private String trackId;
  private Integer epochs;
  private String modelSize;
  private Integer trainingCount;
  private Integer devCount;
  private Integer testCount;
  private Instant startedAt;
  private Instant completedAt;
  private String createdBy;
}
