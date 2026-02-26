package com.nxp.iemdm.shared.dto.databricks;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for testing a model with images via Databricks API.
 *
 * <p>This DTO contains all necessary information to invoke model prediction including model
 * identification, image URLs, and confidence threshold settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestModelRequest {

  /** Model full name identifier */
  @NotBlank(message = "Model full name is required")
  private String modelFullName;

  /** Model version number */
  @NotNull(message = "Version is required")
  private Integer version;

  /** Training track identifier */
  @NotBlank(message = "Track ID is required")
  private String trackId;

  /** List of image URLs to test (blob URLs or HTTP URLs) */
  @NotEmpty(message = "At least one image URL is required")
  private List<String> imageUrls;

  /**
   * Confidence threshold for filtering predictions (0.0 to 1.0).
   *
   * <p>Note: The API uses 'threshhold' spelling (with double 'h')
   */
  @DecimalMin(value = "0.0", message = "Confidence threshold must be between 0 and 1")
  @DecimalMax(value = "1.0", message = "Confidence threshold must be between 0 and 1")
  private Double confidenceThreshhold;
}
