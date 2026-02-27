package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ProjectSplit;
import com.nxp.iemdm.service.rest.landingai.ProjectSplitServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API Controller for ProjectSplit operations. Provides RESTful endpoints for frontend
 * communication.
 *
 * <p>Requirements: 24.7, 24.4, 24.5, 24.6
 */
@RestController
@RequestMapping("/api/landingai/project-splits")
@Slf4j
public class ProjectSplitApiController {

  private final ProjectSplitServiceREST projectSplitServiceREST;

  @Autowired
  public ProjectSplitApiController(ProjectSplitServiceREST projectSplitServiceREST) {
    this.projectSplitServiceREST = projectSplitServiceREST;
  }

  /**
   * Get split preview data for a project. Returns aggregated split distribution data for
   * visualization.
   *
   * @param projectId the project ID
   * @return split preview data with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/preview", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<SplitPreviewDTO> getSplitPreview(
      @RequestParam("projectId") Long projectId) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      SplitPreviewDTO preview = projectSplitServiceREST.getSplitPreview(projectId);
      return ResponseEntity.ok(preview);
    } catch (Exception e) {
      log.error("Error getting split preview for project {}: {}", projectId, e.getMessage());
      throw e;
    }
  }

  /**
   * Create a new project split configuration
   *
   * @param projectSplit the project split to create
   * @return the created project split with HTTP 201 status
   */
  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectSplit> createProjectSplit(
      @RequestBody @Valid ProjectSplit projectSplit) {
    try {
      ProjectSplit createdSplit = projectSplitServiceREST.createProjectSplit(projectSplit);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdSplit);
    } catch (Exception e) {
      log.error("Error creating project split: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get a project split by ID
   *
   * @param splitId the project split ID
   * @return the project split with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/{splitId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectSplit> getProjectSplitById(@PathVariable("splitId") Long splitId) {
    try {
      if (splitId == null || splitId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      ProjectSplit projectSplit = projectSplitServiceREST.getProjectSplitById(splitId);
      return ResponseEntity.ok(projectSplit);
    } catch (Exception e) {
      log.error("Error getting project split {}: {}", splitId, e.getMessage());
      throw e;
    }
  }

  /**
   * Get all project splits for a specific project
   *
   * @param projectId the project ID
   * @return list of project splits with HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ProjectSplit>> getProjectSplitsByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<ProjectSplit> splits = projectSplitServiceREST.getProjectSplitsByProjectId(projectId);
      return ResponseEntity.ok(splits);
    } catch (Exception e) {
      log.error("Error getting project splits for project {}: {}", projectId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing project split
   *
   * @param splitId the project split ID to update
   * @param projectSplit the updated project split data
   * @return the updated project split with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{splitId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectSplit> updateProjectSplit(
      @PathVariable("splitId") Long splitId, @RequestBody @Valid ProjectSplit projectSplit) {
    try {
      if (splitId == null || splitId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      ProjectSplit updatedSplit = projectSplitServiceREST.updateProjectSplit(splitId, projectSplit);
      return ResponseEntity.ok(updatedSplit);
    } catch (Exception e) {
      log.error("Error updating project split {}: {}", splitId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a project split
   *
   * @param splitId the project split ID to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{splitId}")
  public ResponseEntity<Void> deleteProjectSplit(@PathVariable("splitId") Long splitId) {
    try {
      if (splitId == null || splitId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      projectSplitServiceREST.deleteProjectSplit(splitId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting project split {}: {}", splitId, e.getMessage());
      throw e;
    }
  }
}
