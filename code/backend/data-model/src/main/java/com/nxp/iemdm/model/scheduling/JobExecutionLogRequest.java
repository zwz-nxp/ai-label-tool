package com.nxp.iemdm.model.scheduling;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobExecutionLogRequest {
  String jobName;
  LocalDateTime timestampUpperBound;
  Integer maxResults;
}
