package com.nxp.iemdm.model.landingai;

import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DatabricksRequest {

  private String trackId;

  private List<String> zipFilenames;

  private String zipPath;
}
