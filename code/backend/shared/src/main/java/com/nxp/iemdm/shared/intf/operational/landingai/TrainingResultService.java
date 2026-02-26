package com.nxp.iemdm.shared.intf.operational.landingai;

import com.nxp.iemdm.shared.dto.landingai.ProcessingResult;

/**
 * Service interface for processing training results from Databricks API. This service retrieves
 * training results and persists them to the database.
 */
public interface TrainingResultService {

  /**
   * Process all training records with status WAITFORRESULT. Queries the database for pending
   * training records, calls the Databricks API, and persists the results to multiple related
   * tables.
   *
   * @return ProcessingResult containing success/failure counts and error details
   */
  ProcessingResult processWaitingTrainingRecords();

  /**
   * Process a single training record by ID. Useful for manual processing or retry of specific
   * records.
   *
   * @param trainingRecordId ID of the training record to process
   * @return true if processing was successful, false otherwise
   */
  boolean processSingleTrainingRecord(Long trainingRecordId);
}
