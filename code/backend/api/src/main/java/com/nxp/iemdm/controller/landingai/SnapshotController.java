package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.service.SnapshotService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.CreateProjectFromSnapshotRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.SnapshotDTO;
import com.nxp.iemdm.shared.dto.landingai.SnapshotPreviewStatsDTO;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Landing AI snapshot operations. Provides endpoints for creating and
 * retrieving snapshots.
 */
@Slf4j
@RestController
@RequestMapping("/api/landingai/snapshots")
public class SnapshotController {

  private final SnapshotService snapshotService;

  @Autowired
  public SnapshotController(SnapshotService snapshotService) {
    this.snapshotService = snapshotService;
  }

  /**
   * Create a new snapshot for a project.
   *
   * @param request the snapshot creation request
   * @param user the authenticated user
   * @return the created snapshot DTO
   */
  @MethodLog
  @PostMapping
  public ResponseEntity<SnapshotDTO> createSnapshot(
      @Valid @RequestBody SnapshotCreateRequest request,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Creating snapshot '{}' for project: {} by user: {}",
        request.getSnapshotName(),
        request.getProjectId(),
        user.getUsername());

    SnapshotDTO snapshot = snapshotService.createSnapshot(request, user.getUsername());

    return ResponseEntity.status(HttpStatus.CREATED).body(snapshot);
  }

  /**
   * Get all snapshots for a project.
   *
   * @param projectId the project ID
   * @param user the authenticated user
   * @return list of snapshot DTOs
   */
  @MethodLog
  @GetMapping("/project/{projectId}")
  public ResponseEntity<List<SnapshotDTO>> getSnapshotsForProject(
      @PathVariable("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting snapshots for project: {} by user: {}", projectId, user.getUsername());

    List<SnapshotDTO> snapshots = snapshotService.getSnapshotsForProject(projectId);

    return ResponseEntity.ok(snapshots);
  }

  /**
   * Get snapshot preview stats for a project.
   *
   * @param projectId the project ID
   * @param user the authenticated user
   * @return preview stats DTO
   */
  @MethodLog
  @GetMapping("/project/{projectId}/preview-stats")
  public ResponseEntity<SnapshotPreviewStatsDTO> getSnapshotPreviewStats(
      @PathVariable("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Getting snapshot preview stats for project: {} by user: {}",
        projectId,
        user.getUsername());

    SnapshotPreviewStatsDTO stats = snapshotService.getSnapshotPreviewStats(projectId);

    return ResponseEntity.ok(stats);
  }

  /**
   * Get paginated images for a snapshot with optional filtering and sorting. Requirements: 1.1,
   * 3.1, 3.2
   *
   * @param snapshotId the snapshot ID
   * @param page the page number (0-indexed)
   * @param size the page size
   * @param sortBy the sort method (optional, defaults to upload_time_desc)
   * @param filterRequest the filter criteria (optional, sent in request body)
   * @param user the authenticated user
   * @return paginated response with image list items
   */
  @MethodLog
  @PostMapping("/{snapshotId}/images/search")
  public ResponseEntity<?> getSnapshotImages(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "sortBy", defaultValue = "upload_time_desc") String sortBy,
      @RequestBody(required = false)
          com.nxp.iemdm.shared.dto.landingai.ImageFilterRequest filterRequest,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Getting images for snapshot: {} (page: {}, size: {}, sortBy: {}, filters: {}) by user: {}",
        snapshotId,
        page,
        size,
        sortBy,
        filterRequest,
        user.getUsername());

    return ResponseEntity.ok(
        snapshotService.getSnapshotImages(snapshotId, page, size, sortBy, filterRequest));
  }

  /**
   * Create a new project from a snapshot.
   *
   * @param snapshotId the snapshot ID to create project from
   * @param request the create project request containing project name
   * @param user the authenticated user
   * @return the created project DTO
   */
  @MethodLog
  @PostMapping("/{snapshotId}/create-project")
  public ResponseEntity<ProjectDTO> createProjectFromSnapshot(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @Valid @RequestBody CreateProjectFromSnapshotRequest request,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Creating project '{}' from snapshot: {} by user: {}",
        request.getProjectName(),
        snapshotId,
        user.getUsername());

    ProjectDTO project =
        snapshotService.createProjectFromSnapshot(
            snapshotId, request.getProjectName(), user.getUsername());

    return ResponseEntity.status(HttpStatus.CREATED).body(project);
  }

  /**
   * Revert a project to a snapshot state.
   *
   * @param snapshotId the snapshot ID to revert to
   * @param projectId the project ID to revert
   * @param user the authenticated user
   * @return success response
   */
  @MethodLog
  @PostMapping("/{snapshotId}/revert")
  public ResponseEntity<Void> revertToSnapshot(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @RequestParam("projectId") @NotNull Long projectId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Reverting project: {} to snapshot: {} by user: {}",
        projectId,
        snapshotId,
        user.getUsername());

    snapshotService.revertProjectToSnapshot(snapshotId, projectId, user.getUsername());

    return ResponseEntity.ok().build();
  }

  /**
   * Download snapshot dataset.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return byte array with snapshot data
   */
  @MethodLog
  @GetMapping("/{snapshotId}/download")
  public ResponseEntity<byte[]> downloadSnapshot(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Downloading snapshot: {} by user: {}", snapshotId, user.getUsername());

    byte[] data = snapshotService.downloadSnapshotDataset(snapshotId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", "snapshot-" + snapshotId + ".zip");
    headers.setContentLength(data.length);

    return ResponseEntity.ok().headers(headers).body(data);
  }

  /**
   * Delete a snapshot.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return success response
   */
  @MethodLog
  @DeleteMapping("/{snapshotId}")
  public ResponseEntity<Void> deleteSnapshot(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Deleting snapshot: {} by user: {}", snapshotId, user.getUsername());

    snapshotService.deleteSnapshot(snapshotId, user.getUsername());

    return ResponseEntity.noContent().build();
  }

  /**
   * Get project classes from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return list of project classes from snapshot
   */
  @MethodLog
  @GetMapping("/{snapshotId}/classes")
  public ResponseEntity<?> getSnapshotClasses(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting classes for snapshot: {} by user: {}", snapshotId, user.getUsername());

    return ResponseEntity.ok(snapshotService.getSnapshotClasses(snapshotId));
  }

  /**
   * Get project tags from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return list of project tags from snapshot
   */
  @MethodLog
  @GetMapping("/{snapshotId}/tags")
  public ResponseEntity<?> getSnapshotTags(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting tags for snapshot: {} by user: {}", snapshotId, user.getUsername());

    return ResponseEntity.ok(snapshotService.getSnapshotTags(snapshotId));
  }

  /**
   * Get project metadata from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return list of project metadata from snapshot
   */
  @MethodLog
  @GetMapping("/{snapshotId}/metadata")
  public ResponseEntity<?> getSnapshotMetadata(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting metadata for snapshot: {} by user: {}", snapshotId, user.getUsername());

    return ResponseEntity.ok(snapshotService.getSnapshotMetadata(snapshotId));
  }

  /**
   * Get project splits from snapshot.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return list of project splits from snapshot
   */
  @MethodLog
  @GetMapping("/{snapshotId}/splits")
  public ResponseEntity<?> getSnapshotSplits(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting splits for snapshot: {} by user: {}", snapshotId, user.getUsername());

    return ResponseEntity.ok(snapshotService.getSnapshotSplits(snapshotId));
  }

  /**
   * Download snapshot dataset as ZIP file.
   *
   * @param snapshotId the snapshot ID
   * @param user the authenticated user
   * @return ZIP file containing the snapshot dataset
   */
  @MethodLog
  @GetMapping("/{snapshotId}/download-dataset")
  public ResponseEntity<byte[]> downloadSnapshotDataset(
      @PathVariable("snapshotId") @NotNull Long snapshotId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Downloading dataset for snapshot: {} by user: {}", snapshotId, user.getUsername());

    try {
      byte[] zipData = snapshotService.downloadSnapshotDataset(snapshotId);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentDispositionFormData(
          "attachment",
          String.format(
              "snapshot-%d-%s.zip",
              snapshotId,
              java.time.LocalDateTime.now()
                  .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))));

      return ResponseEntity.ok().headers(headers).body(zipData);
    } catch (Exception e) {
      log.error("Failed to download snapshot dataset: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
