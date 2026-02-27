package com.nxp.iemdm.service;

import com.nxp.iemdm.model.landingai.ValidationChart;
import java.util.List;

/**
 * Service interface for ValidationChart operations. Provides business logic layer for validation
 * chart data management.
 */
public interface ValidationChartService {

  /**
   * Get validation chart data for a specific model.
   *
   * @param modelId Model ID
   * @return List of validation chart data points ordered by created_at ascending
   */
  List<ValidationChart> getValidationChartDataByModelId(Long modelId);
}
