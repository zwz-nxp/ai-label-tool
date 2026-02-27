package com.nxp.iemdm.service;

import com.nxp.iemdm.model.landingai.ConfidentialReport;
import java.util.Optional;

/**
 * Service interface for ConfidentialReport operations. Provides business logic layer for
 * confidential report management.
 */
public interface ConfidentialReportService {

  /**
   * Get confidential report by model ID.
   *
   * @param modelId Model ID
   * @return Optional of ConfidentialReport
   */
  Optional<ConfidentialReport> getConfidentialReportByModelId(Long modelId);

  /**
   * Get confidential report by ID.
   *
   * @param id Confidential report ID
   * @return Optional of ConfidentialReport
   */
  Optional<ConfidentialReport> getConfidentialReportById(Long id);
}
