package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.LossChart;
import com.nxp.iemdm.service.LossChartService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Loss Chart data. Delegates business logic to LossChartService. Requirements:
 * 7.3, 7.10
 */
@RestController
@RequestMapping("/api/landingai/charts/loss")
@RequiredArgsConstructor
public class LossChartController {

  private final LossChartService lossChartService;

  /**
   * Get loss chart data for a specific model.
   *
   * @param modelId Model ID
   * @return List of loss chart data points ordered by created_at ascending
   */
  @GetMapping("/{modelId}")
  public ResponseEntity<List<LossChart>> getLossChartDataByModelId(@PathVariable Long modelId) {
    List<LossChart> lossChartData = lossChartService.getLossChartDataByModelId(modelId);
    return ResponseEntity.ok(lossChartData);
  }
}
