package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for data augmentation configuration. Contains settings for various image augmentation
 * operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AugmentationConfigDTO {

  @Valid private HorizontalFlipDTO horizontalFlip;

  @Valid private RandomAugmentDTO randomAugment;

  @Valid private RandomBrightnessDTO randomBrightness;

  @Valid private BlurDTO blur;

  @Valid private MotionBlurDTO motionBlur;

  @Valid private GaussianBlurDTO gaussianBlur;

  @Valid private HueSaturationValueDTO hueSaturationValue;

  @Valid private RandomContrastDTO randomContrast;

  @Valid private VerticalFlipDTO verticalFlip;

  @Valid private RandomRotateDTO randomRotate;

  /** Configuration for horizontal flip augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class HorizontalFlipDTO {
    @DecimalMin(value = "0.0", message = "Probability must be at least 0")
    @DecimalMax(value = "1.0", message = "Probability must be at most 1")
    private Double probability;
  }

  /** Configuration for random augment. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RandomAugmentDTO {
    @Min(value = 1, message = "Number of transforms must be at least 1")
    private Integer numTransforms;

    @Min(value = 0, message = "Magnitude cannot be negative")
    private Integer magnitude;
  }

  /** Configuration for random brightness augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RandomBrightnessDTO {
    @DecimalMin(value = "-1.0", message = "Limit must be at least -1")
    @DecimalMax(value = "1.0", message = "Limit must be at most 1")
    private Double limit;
  }

  /** Configuration for blur augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BlurDTO {
    @Min(value = 1, message = "Blur limit must be at least 1")
    private Integer blurLimit;
  }

  /** Configuration for motion blur augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MotionBlurDTO {
    @Min(value = 1, message = "Blur limit must be at least 1")
    private Integer blurLimit;
  }

  /** Configuration for gaussian blur augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GaussianBlurDTO {
    @Min(value = 1, message = "Blur limit must be at least 1")
    private Integer blurLimit;

    @DecimalMin(value = "0.0", message = "Sigma must be non-negative")
    private Double sigma;
  }

  /** Configuration for hue saturation value augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class HueSaturationValueDTO {
    private Integer hueShiftLimit;
    private Integer saturationShiftLimit;
    private Integer valueShiftLimit;
  }

  /** Configuration for random contrast augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RandomContrastDTO {
    private Double limit;
  }

  /** Configuration for vertical flip augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VerticalFlipDTO {
    @DecimalMin(value = "0.0", message = "Probability must be at least 0")
    @DecimalMax(value = "1.0", message = "Probability must be at most 1")
    private Double probability;
  }

  /** Configuration for random rotate augmentation. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RandomRotateDTO {
    private Integer limit;
    private String borderMode;
  }
}
