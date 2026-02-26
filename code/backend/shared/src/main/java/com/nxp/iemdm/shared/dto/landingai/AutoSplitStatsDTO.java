package com.nxp.iemdm.shared.dto.landingai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoSplitStatsDTO {
  private Long totalImagesToSplit;
  private List<ClassStatsDTO> classStats;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ClassStatsDTO {
    private Long classId;
    private String className;
    private String color;
    private Long imageCount;
  }
}
