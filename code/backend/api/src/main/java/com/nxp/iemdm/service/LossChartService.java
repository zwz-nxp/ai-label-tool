package com.nxp.iemdm.service;

import com.nxp.iemdm.model.landingai.LossChart;
import java.util.List;

/**
 * Service interface for LossChart operations. Provides business logic layer for loss chart data
 * management.
 */
public interface LossChartService {

  /**
   * Get loss chart data for a specific model.
   *
   * @param modelId Model ID
   * @return List of loss chart data points ordered by created_at ascending
   */
  List<LossChart> getLossChartDataByModelId(Long modelId);
}
