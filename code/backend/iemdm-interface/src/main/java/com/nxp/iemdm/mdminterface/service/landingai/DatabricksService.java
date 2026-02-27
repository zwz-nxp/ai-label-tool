package com.nxp.iemdm.mdminterface.service.landingai;

import com.nxp.iemdm.mdminterface.dto.request.*;
import com.nxp.iemdm.mdminterface.dto.response.*;

/**
 * Service interface for Databricks operations. Implementations can be Mock or Real based on
 * configuration.
 */
public interface DatabricksService {

  /** Submit training data to Databricks for model training */
  TrainingDataResponse submitTraining(TrainingDataRequest request);

  // DEPRECATED: Old method that returns parsed JSON data directly.
  // Replaced by getTrainingResultsAsFilePaths() which returns file paths.
  // Kept for backward compatibility reference. Remove once confirmed no longer needed.
  // TrainingResultsResponse getTrainingResults(TrainingResultsRequest request);

  /**
   * Retrieve training results as file paths. Downloads files from Databricks volume to local server
   * path and returns local file paths.
   */
  TrainingResultsFilePathsResponse getTrainingResultsAsFilePaths(TrainingResultsRequest request);

  /**
   * Test a trained model with new images. Generates 6 result files and returns trackId. Use
   * getTestModelResultsAsFilePaths to retrieve the file paths.
   */
  TrainingDataResponse testModel(ModelTestRequest request);

  /**
   * Retrieve test model results as file paths. Checks job status if runId is provided. Returns
   * local file paths for the 6 result files generated during model testing.
   */
  TrainingResultsFilePathsResponse getTestModelResultsAsFilePaths(TrainingResultsRequest request);

  /** Get download URL for a trained model */
  DownloadModelResponse downloadModel(DownloadModelRequest request);

  /** Delete a model version from registry */
  DeleteModelResponse deleteModel(DeleteModelRequest request);

  /**
   * Read file content from local file system.
   *
   * @param filePath Local file path to read
   * @return File content as String (JSON format)
   */
  String readFileContent(String filePath);
}
