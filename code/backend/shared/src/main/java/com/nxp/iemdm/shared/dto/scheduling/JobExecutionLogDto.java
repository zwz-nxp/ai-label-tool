package com.nxp.iemdm.shared.dto.scheduling;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobExecutionLogDto {
  LocalDateTime time;
  String info;
}
