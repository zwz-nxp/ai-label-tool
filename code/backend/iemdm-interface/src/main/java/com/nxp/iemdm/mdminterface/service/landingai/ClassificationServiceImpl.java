package com.nxp.iemdm.mdminterface.service.landingai;

import com.nxp.iemdm.mdminterface.dto.request.ClassificationTrainingRequest;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationResultsResponse;
import com.nxp.iemdm.mdminterface.dto.response.ClassificationTrainingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Real implementation of ClassificationService. Placeholder until classification job is set up in
 * Databricks.
 */
@Service
@ConditionalOnProperty(name = "databricks.mode", havingValue = "real")
@Slf4j
public class ClassificationServiceImpl implements ClassificationService {

  @Override
  public ClassificationTrainingResponse submitTraining(ClassificationTrainingRequest request) {
    log.info(
        "[DATABRICKS] Classification training not yet implemented for trackId: {}",
        request.getTrackId());
    throw new UnsupportedOperationException(
        "Real Databricks classification training not yet implemented.");
  }

  @Override
  public ClassificationResultsResponse getTrainingResults(String trackId) {
    log.info("[DATABRICKS] Classification results not yet implemented for trackId: {}", trackId);
    throw new UnsupportedOperationException(
        "Real Databricks classification results not yet implemented.");
  }

  @Override
  public ClassificationResultsResponse testModel(ClassificationTrainingRequest request) {
    log.info(
        "[DATABRICKS] Classification model test not yet implemented for trackId: {}",
        request.getTrackId());
    throw new UnsupportedOperationException(
        "Real Databricks classification model test not yet implemented.");
  }
}
