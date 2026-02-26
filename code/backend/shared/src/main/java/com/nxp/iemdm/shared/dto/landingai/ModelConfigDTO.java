package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for model configuration in training requests. Contains model alias, hyperparameters,
 * transforms, and augmentations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigDTO {

  @NotBlank(message = "Model alias is required")
  private String modelAlias;

  /** Training status (e.g., PENDING, TRAINING, COMPLETED) */
  private String status;

  @NotNull(message = "Epochs is required")
  @Min(value = 1, message = "Epochs must be at least 1")
  @Max(value = 100, message = "Epochs must be at most 100")
  private Integer epochs;

  @NotBlank(message = "Model size is required")
  private String modelSize;

  @Valid private TransformConfigDTO transforms;

  @Valid private AugmentationConfigDTO augmentations;

  /**
   * Raw JSON string for model parameters, directly from the Model Parameters editor. When present,
   * this takes priority over the structured {@link #augmentations} field.
   */
  private String modelParam;
}
