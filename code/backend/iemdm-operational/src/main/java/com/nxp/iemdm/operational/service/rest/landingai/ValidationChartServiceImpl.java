package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ValidationChart;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.repository.jpa.landingai.ValidationChartRepository;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for ValidationChart operations in the operational layer. Provides internal
 * endpoints that are called by the API service layer.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/charts/validation")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ValidationChartServiceImpl {

  private final ValidationChartRepository validationChartRepository;

  /**
   * Get validation chart data for a specific model.
   *
   * @param modelId Model ID
   * @return List of validation chart data points ordered by created_at ascending
   */
  @MethodLog
  @GetMapping(path = "/{modelId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ValidationChart>> getValidationChartDataByModelId(
      @PathVariable Long modelId) {
    log.debug("Operational REST: Fetching validation chart data for model ID: {}", modelId);
    List<ValidationChart> validationChartData =
        validationChartRepository.findByModelIdOrderByCreatedAtAsc(modelId);
    log.debug(
        "Found {} validation chart data points for model ID: {}",
        validationChartData.size(),
        modelId);
    return ResponseEntity.ok(validationChartData);
  }
}
