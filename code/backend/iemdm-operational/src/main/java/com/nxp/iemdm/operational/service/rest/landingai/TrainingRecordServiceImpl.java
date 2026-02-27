package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.model.landingai.TrainingRecord;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.repository.jpa.landingai.TrainingRecordRepository;
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
 * REST controller for TrainingRecord operations in the operational layer. Provides internal
 * endpoints that are called by the API service layer.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/training-records")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainingRecordServiceImpl {

  private final TrainingRecordRepository trainingRecordRepository;

  /**
   * Get training record by ID.
   *
   * @param id Training record ID
   * @return TrainingRecord or 404 if not found
   */
  @MethodLog
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<TrainingRecord> getTrainingRecordById(@PathVariable Long id) {
    log.debug("Operational REST: Getting training record by ID: {}", id);
    return trainingRecordRepository
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
