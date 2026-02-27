package com.nxp.iemdm.mdminterface.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabelData {
  private String xcenter;
  private String ycenter;
  private String width;
  private String height;
  private Integer classId;
  private String confidenceRate; // Only used in responses
}
