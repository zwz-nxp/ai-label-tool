package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ProjectSplit;
import com.nxp.iemdm.operational.service.landingai.ProjectSplitService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.SplitPreviewDTO;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for ProjectSplit operations in the Operational Layer. Provides internal endpoints
 * for project split configuration CRUD operations.
 *
 * <p>Requirements: 25.7, 25.4, 25.5
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/project-splits")
public class ProjectSplitController {

  private final ProjectSplitService projectSplitService;

  public ProjectSplitController(ProjectSplitService projectSplitService) {
    this.projectSplitService = projectSplitService;
  }

  /**
   * Get split preview data for a project. Returns aggregated split distribution data for
   * visualization.
   *
   * @param projectId the project ID
   * @return split preview data
   */
  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(path = "/preview", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<SplitPreviewDTO> getSplitPreview(
      @RequestParam("projectId") Long projectId) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      SplitPreviewDTO preview = projectSplitService.getSplitPreview(projectId);
      return ResponseEntity.ok(preview);
    } catch (Exception e) {
      log.error("Error getting split preview for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Create a new project split configuration
   *
   * @param projectSplit the project split to create
   * @return the created project split
   */
  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectSplit> createProjectSplit(@RequestBody ProjectSplit projectSplit) {
    try {
      ProjectSplit savedSplit = projectSplitService.createProjectSplit(projectSplit);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedSplit);
    } catch (IllegalArgumentException e) {
      log.error("Validation error creating project split: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating project split", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get a project split by ID
   *
   * @param splitId the project split ID
   * @return the project split
   */
  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(path = "/{splitId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectSplit> getProjectSplitById(@PathVariable("splitId") Long splitId) {
    try {
      ProjectSplit projectSplit = projectSplitService.getProjectSplitById(splitId);
      return ResponseEntity.ok(projectSplit);
    } catch (NotFoundException e) {
      log.error("Project split not found: {}", splitId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error retrieving project split {}", splitId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get all project splits for a specific project
   *
   * @param projectId the project ID
   * @return list of project splits
   */
  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ProjectSplit>> getProjectSplitsByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      List<ProjectSplit> splits = projectSplitService.getProjectSplitsByProjectId(projectId);
      return ResponseEntity.ok(splits);
    } catch (Exception e) {
      log.error("Error retrieving project splits for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update an existing project split
   *
   * @param splitId the project split ID to update
   * @param projectSplit the updated project split data
   * @return the updated project split
   */
  @MethodLog
  @Transactional
  @PutMapping(
      path = "/{splitId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectSplit> updateProjectSplit(
      @PathVariable("splitId") Long splitId, @RequestBody ProjectSplit projectSplit) {
    try {
      ProjectSplit updatedSplit = projectSplitService.updateProjectSplit(splitId, projectSplit);
      return ResponseEntity.ok(updatedSplit);
    } catch (NotFoundException e) {
      log.error("Project split not found: {}", splitId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error updating project split: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error updating project split {}", splitId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a project split
   *
   * @param splitId the project split ID to delete
   * @return no content on success
   */
  @MethodLog
  @Transactional
  @DeleteMapping(path = "/{splitId}")
  public ResponseEntity<Void> deleteProjectSplit(@PathVariable("splitId") Long splitId) {
    try {
      projectSplitService.deleteProjectSplit(splitId);
      return ResponseEntity.noContent().build();
    } catch (NotFoundException e) {
      log.error("Project split not found: {}", splitId);
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error deleting project split {}", splitId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
