package com.nxp.iemdm.mdminterface.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadModelRequest {
  private String modelName;
  private Integer version;
  private String trackId;
}
