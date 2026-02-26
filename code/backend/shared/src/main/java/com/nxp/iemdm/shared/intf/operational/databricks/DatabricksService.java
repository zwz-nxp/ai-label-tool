package com.nxp.iemdm.shared.intf.operational.databricks;

import com.nxp.iemdm.shared.dto.databricks.TestModelRequest;
import com.nxp.iemdm.shared.dto.databricks.TestModelResponse;

/**
 * Service interface for Databricks model testing operations.
 *
 * <p>This service handles communication with the Databricks API for model inference and testing.
 */
public interface DatabricksService {

  /**
   * Tests a model with provided images and returns prediction results.
   *
   * @param request the test model request containing model info, image URLs, and confidence
   *     threshold
   * @return the test model response with prediction results for all images
   */
  TestModelResponse testModel(TestModelRequest request);
}
