package com.nxp.iemdm.mdminterface.service.landingai;

import com.nxp.iemdm.mdminterface.dto.request.ClassificationTrainingRequest;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationResultsResponse;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationTrainingResponse;

/**
 * Service interface for Classification operations. Separate from DatabricksService (detection) for
 * future flexibility.
 */
public interface ClassificationService {

  /**
   * Submit classification training data. Zip structure: train/val/test folders with class
   * subfolders containing images.
   */
  ClassificationTrainingResponse submitTraining(ClassificationTrainingRequest request);

  /** Retrieve classification training results. */
  ClassificationResultsResponse getTrainingResults(String trackId);

  /** Test a trained classification model with new images. */
  ClassificationResultsResponse testModel(ClassificationTrainingRequest request);
}
