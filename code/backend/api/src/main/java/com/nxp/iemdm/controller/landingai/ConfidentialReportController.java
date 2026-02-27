package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ConfidentialReport;
import com.nxp.iemdm.service.ConfidentialReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for ConfidentialReport operations. Delegates business logic to
 * ConfidentialReportService.
 */
@RestController
@RequestMapping("/api/confidential-reports")
@RequiredArgsConstructor
public class ConfidentialReportController {

  private final ConfidentialReportService confidentialReportService;

  /**
   * Get confidential report by model ID.
   *
   * @param modelId Model ID
   * @return Confidential report
   */
  @GetMapping("/model/{modelId}")
  public ResponseEntity<ConfidentialReport> getConfidentialReportByModelId(
      @PathVariable Long modelId) {
    return confidentialReportService
        .getConfidentialReportByModelId(modelId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get confidential report by ID.
   *
   * @param id Confidential report ID
   * @return Confidential report
   */
  @GetMapping("/{id}")
  public ResponseEntity<ConfidentialReport> getConfidentialReportById(@PathVariable Long id) {
    return confidentialReportService
        .getConfidentialReportById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
