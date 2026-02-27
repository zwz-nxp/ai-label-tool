package com.nxp.iemdm.mdminterface.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {
  private String imageUrl;
  private String dataSet; // train, dev, test
  private List<LabelData> labelList;
}
