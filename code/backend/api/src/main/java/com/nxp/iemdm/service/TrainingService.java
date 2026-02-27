package com.nxp.iemdm.service;

import com.nxp.iemdm.shared.dto.landingai.ModelConfigDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRecordDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRequest;
import com.nxp.iemdm.shared.dto.landingai.TrainingStatusDTO;
import java.util.List;

/** Service interface for Landing AI training operations. */
public interface TrainingService {

  /**
   * Start training for a project (legacy single-model support).
   *
   * @param request the training request
   * @param userId the user identifier
   * @return the created training record
   * @deprecated Use {@link #startMultiConfigTraining(TrainingRequest, String)} for multi-model
   *     support
   */
  @Deprecated
  TrainingRecordDTO startTraining(TrainingRequest request, String userId);

  /**
   * Start training with multiple model configurations. Creates independent training records for
   * each model configuration.
   *
   * @param request the training request containing multiple model configs
   * @param userId the user identifier
   * @return list of created training records, one per model configuration
   */
  List<TrainingRecordDTO> startMultiConfigTraining(TrainingRequest request, String userId);

  /**
   * Create a single training record from a model configuration.
   *
   * @param projectId the project ID
   * @param snapshotId the snapshot ID (optional)
   * @param modelConfig the model configuration
   * @param userId the user identifier
   * @return the created training record
   */
  TrainingRecordDTO createTrainingRecord(
      Long projectId, Long snapshotId, ModelConfigDTO modelConfig, String userId);

  /**
   * Get training status by ID.
   *
   * @param id the training record ID
   * @return the training status
   */
  TrainingStatusDTO getTrainingStatus(Long id);

  /**
   * Get training record by ID.
   *
   * @param id the training record ID
   * @return the training record details
   */
  TrainingRecordDTO getTrainingRecord(Long id);

  /**
   * Cancel training by ID.
   *
   * @param id the training record ID
   * @param userId the user identifier
   */
  void cancelTraining(Long id, String userId);
}
