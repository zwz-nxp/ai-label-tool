package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.LossChart;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.repository.jpa.landingai.LossChartRepository;
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
 * REST controller for LossChart operations in the operational layer. Provides internal endpoints
 * that are called by the API service layer.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/charts/loss")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LossChartServiceImpl {

  private final LossChartRepository lossChartRepository;

  /**
   * Get loss chart data for a specific model.
   *
   * @param modelId Model ID
   * @return List of loss chart data points ordered by created_at ascending
   */
  @MethodLog
  @GetMapping(path = "/{modelId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<LossChart>> getLossChartDataByModelId(@PathVariable Long modelId) {
    log.debug("Operational REST: Fetching loss chart data for model ID: {}", modelId);
    List<LossChart> lossChartData = lossChartRepository.findByModelIdOrderByCreatedAtAsc(modelId);
    log.debug("Found {} loss chart data points for model ID: {}", lossChartData.size(), modelId);
    return ResponseEntity.ok(lossChartData);
  }
}
