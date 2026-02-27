package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.TrainingRecord;
import com.nxp.iemdm.service.TrainingRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for TrainingRecord operations. Delegates business logic to TrainingRecordService.
 */
@RestController
@RequestMapping("/api/training-records")
@RequiredArgsConstructor
public class TrainingRecordController {

  private final TrainingRecordService trainingRecordService;

  /**
   * Get training record by ID.
   *
   * @param id Training record ID
   * @return Training record
   */
  @GetMapping("/{id}")
  public ResponseEntity<TrainingRecord> getTrainingRecordById(@PathVariable Long id) {
    return trainingRecordService
        .getTrainingRecordById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
