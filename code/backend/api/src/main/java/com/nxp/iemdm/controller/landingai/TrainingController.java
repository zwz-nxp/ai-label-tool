package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.SnapshotService;
import com.nxp.iemdm.service.TrainingService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.SnapshotDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRecordDTO;
import com.nxp.iemdm.shared.dto.landingai.TrainingRequest;
import com.nxp.iemdm.shared.dto.landingai.TrainingStatusDTO;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Landing AI training operations. Provides endpoints for starting training and
 * checking training status.
 *
 * <p>Exception handling is delegated to the global LandingAiExceptionHandler.
 */
@Slf4j
@RestController
@RequestMapping("/api/landingai/training")
public class TrainingController {

  private final TrainingService trainingService;
  private final SnapshotService snapshotService;

  @Autowired
  public TrainingController(TrainingService trainingService, SnapshotService snapshotService) {
    this.trainingService = trainingService;
    this.snapshotService = snapshotService;
  }

  /**
   * Start training with the provided parameters. Supports multiple model configurations in a single
   * request. Creates independent training records for each model configuration.
   *
   * @param request the training request containing model configurations
   * @param user the authenticated user
   * @return list of created training record DTOs, one per model configuration
   */
  @MethodLog
  @PostMapping("/start")
  public ResponseEntity<List<TrainingRecordDTO>> startTraining(
      @Valid @RequestBody TrainingRequest request, @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Starting training for project: {} by user: {} with {} model configurations",
        request.getProjectId(),
        user.getUsername(),
        request.getModelConfigs() != null ? request.getModelConfigs().size() : 0);

    List<TrainingRecordDTO> trainingRecords =
        trainingService.startMultiConfigTraining(request, user.getUsername());

    return ResponseEntity.status(HttpStatus.CREATED).body(trainingRecords);
  }

  /**
   * Get training status by ID.
   *
   * @param id the training record ID
   * @param user the authenticated user
   * @return the training status DTO
   */
  @MethodLog
  @GetMapping("/{id}/status")
  public ResponseEntity<TrainingStatusDTO> getTrainingStatus(
      @PathVariable("id") @NotNull Long id, @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting training status for ID: {} by user: {}", id, user.getUsername());

    TrainingStatusDTO status = trainingService.getTrainingStatus(id);

    return ResponseEntity.ok(status);
  }

  /**
   * Get all snapshots for a project. Used by the training configuration UI to allow users to select
   * a data version.
   *
   * @param projectId the project ID
   * @param user the authenticated user
   * @return list of snapshot DTOs for the project
   */
  @MethodLog
  @GetMapping("/snapshots")
  public ResponseEntity<List<SnapshotDTO>> getSnapshots(
      @RequestParam("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting snapshots for project: {} by user: {}", projectId, user.getUsername());

    List<SnapshotDTO> snapshots = snapshotService.getSnapshotsForProject(projectId);

    return ResponseEntity.ok(snapshots);
  }
}
