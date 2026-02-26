package com.nxp.iemdm.shared.dto.landingai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for class information in confusion matrix. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassInfoDTO {

  /** Class ID */
  private Long id;

  /** Class name */
  private String name;

  /** Class color (hex format, e.g., "#FF5733") */
  private String color;
}
