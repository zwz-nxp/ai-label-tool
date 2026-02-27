package com.nxp.iemdm.service;

import com.nxp.iemdm.model.landingai.TrainingRecord;
import java.util.Optional;

/**
 * Service interface for TrainingRecord operations. Provides business logic layer for training
 * record management.
 */
public interface TrainingRecordService {

  /**
   * Get training record by ID.
   *
   * @param id Training record ID
   * @return Optional of TrainingRecord
   */
  Optional<TrainingRecord> getTrainingRecordById(Long id);
}
