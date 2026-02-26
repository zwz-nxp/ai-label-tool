package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for image transform configuration. Contains settings for rescale with padding, crop, and
 * manual resize operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformConfigDTO {

  @Valid private RescaleWithPaddingDTO rescaleWithPadding;

  @Valid private CropDTO crop;

  @Valid private ManualResizeDTO manualResize;

  /** Configuration for rescale with padding transform. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RescaleWithPaddingDTO {
    private boolean enabled;

    @Min(value = 1, message = "Width must be at least 1")
    private Integer width;

    @Min(value = 1, message = "Height must be at least 1")
    private Integer height;
  }

  /** Configuration for crop transform. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CropDTO {
    @Min(value = 0, message = "X offset cannot be negative")
    private Integer xOffset;

    @Min(value = 0, message = "Y offset cannot be negative")
    private Integer yOffset;

    @Min(value = 1, message = "Width must be at least 1")
    private Integer width;

    @Min(value = 1, message = "Height must be at least 1")
    private Integer height;
  }

  /** Configuration for manual resize transform. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ManualResizeDTO {
    @Min(value = 1, message = "Width must be at least 1")
    private Integer width;

    @Min(value = 1, message = "Height must be at least 1")
    private Integer height;

    private boolean keepAspectRatio;
  }
}
