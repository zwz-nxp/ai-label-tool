package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ValidationChart;
import com.nxp.iemdm.service.ValidationChartService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Validation Chart data. Delegates business logic to ValidationChartService.
 * Requirements: 7.4, 7.11
 */
@RestController
@RequestMapping("/api/landingai/charts/validation")
@RequiredArgsConstructor
public class ValidationChartController {

  private final ValidationChartService validationChartService;

  /**
   * Get validation chart data for a specific model.
   *
   * @param modelId Model ID
   * @return List of validation chart data points ordered by created_at ascending
   */
  @GetMapping("/{modelId}")
  public ResponseEntity<List<ValidationChart>> getValidationChartDataByModelId(
      @PathVariable Long modelId) {
    List<ValidationChart> validationChartData =
        validationChartService.getValidationChartDataByModelId(modelId);
    return ResponseEntity.ok(validationChartData);
  }
}
