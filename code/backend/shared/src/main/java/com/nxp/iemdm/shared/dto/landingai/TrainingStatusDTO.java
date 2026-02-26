package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingStatusDTO {
  private Long id;
  private String status;
  private Integer progress; // 0-100 percentage
  private String currentPhase; // e.g., "Preparing data", "Training", "Evaluating"
  private Instant startedAt;
  private Instant estimatedCompletionAt;
  private String errorMessage;
}
