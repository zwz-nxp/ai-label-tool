package com.nxp.iemdm.shared.dto.landingai;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoSplitRequestDTO {
  private Long projectId;
  private Boolean includeAssigned;
  private Boolean adjustAllTogether;
  private Integer trainRatio;
  private Integer devRatio;
  private Integer testRatio;
  private Map<Long, ClassRatioDTO> classRatios;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ClassRatioDTO {
    private Integer train;
    private Integer dev;
    private Integer test;
  }
}
