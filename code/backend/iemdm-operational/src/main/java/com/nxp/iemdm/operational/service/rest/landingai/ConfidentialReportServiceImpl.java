package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.ConfidentialReport;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.repository.jpa.landingai.ConfidentialReportRepository;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for ConfidentialReport operations in the operational layer. Provides internal
 * endpoints that are called by the API service layer.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/confidential-reports")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfidentialReportServiceImpl {

  private final ConfidentialReportRepository confidentialReportRepository;

  /**
   * Get confidential report by model ID.
   *
   * @param modelId Model ID
   * @return ConfidentialReport or 404 if not found
   */
  @MethodLog
  @GetMapping(path = "/model/{modelId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ConfidentialReport> getConfidentialReportByModelId(
      @PathVariable Long modelId) {
    log.debug("Operational REST: Getting confidential report for model ID: {}", modelId);
    return confidentialReportRepository
        .findByModelId(modelId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get confidential report by ID.
   *
   * @param id Confidential report ID
   * @return ConfidentialReport or 404 if not found
   */
  @MethodLog
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ConfidentialReport> getConfidentialReportById(@PathVariable Long id) {
    log.debug("Operational REST: Getting confidential report by ID: {}", id);
    return confidentialReportRepository
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
