package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for training request. Supports both legacy single-model training and new multi-model training
 * configurations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRequest {
  @NotNull(message = "Project ID is required")
  private Long projectId;

  /** Snapshot ID for versioned data training (optional, uses current version if not specified) */
  private Long snapshotId;

  /** File path for training output */
  private String filePath;

  /** File name for training output */
  private String fileName;

  /**
   * List of model configurations for multi-model training. Each configuration creates an
   * independent training record.
   */
  @Valid private List<ModelConfigDTO> modelConfigs;

  // Legacy fields for backward compatibility
  private String modelAlias;
  private Integer epochs;
  private String modelSize;
  private Map<String, Object> transformParams;
  private Map<String, Object> modelParams;
  private boolean isCustomTraining;
}
