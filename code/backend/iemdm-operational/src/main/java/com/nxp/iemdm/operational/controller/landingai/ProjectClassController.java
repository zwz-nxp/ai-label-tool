package com.nxp.iemdm.operational.controller.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.operational.service.landingai.ProjectClassService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ProjectClassDTO;
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
 * REST Controller for Project Class operations in the Operational Layer. Provides internal
 * endpoints for class CRUD operations.
 *
 * <p>Requirements: 25.3, 25.4, 25.5
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/project-classes")
public class ProjectClassController {

  private final ProjectClassService projectClassService;

  public ProjectClassController(ProjectClassService projectClassService) {
    this.projectClassService = projectClassService;
  }

  /**
   * Create a new project class
   *
   * @param projectId the project ID to associate the class with
   * @param projectClass the class to create
   * @return the created class
   */
  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectClass> createClass(
      @RequestParam("projectId") Long projectId, @RequestBody ProjectClass projectClass) {
    try {
      ProjectClass savedClass = projectClassService.createClass(projectId, projectClass);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedClass);
    } catch (IllegalArgumentException e) {
      log.error("Validation error creating class: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating class", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Get all classes for a specific project
   *
   * @param projectId the project ID
   * @return list of class DTOs with label counts
   */
  @MethodLog
  @Transactional(readOnly = true)
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ProjectClassDTO>> getClassesByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      List<ProjectClassDTO> classes = projectClassService.getClassesByProjectId(projectId);
      return ResponseEntity.ok(classes);
    } catch (Exception e) {
      log.error("Error retrieving classes for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Update an existing class
   *
   * @param classId the class ID to update
   * @param projectClass the updated class data
   * @return the updated class
   */
  @MethodLog
  @Transactional
  @PutMapping(
      path = "/{classId}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectClass> updateClass(
      @PathVariable("classId") Long classId, @RequestBody ProjectClass projectClass) {
    try {
      ProjectClass updatedClass = projectClassService.updateClass(classId, projectClass);
      return ResponseEntity.ok(updatedClass);
    } catch (NotFoundException e) {
      log.error("Class not found: {}", classId);
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException e) {
      log.error("Validation error updating class: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error updating class {}", classId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Delete a class
   *
   * @param classId the class ID to delete
   * @return no content on success
   */
  @MethodLog
  @Transactional
  @DeleteMapping(path = "/{classId}")
  public ResponseEntity<Void> deleteClass(@PathVariable("classId") Long classId) {
    try {
      projectClassService.deleteClass(classId);
      return ResponseEntity.noContent().build();
    } catch (NotFoundException e) {
      log.error("Class not found: {}", classId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.error("Cannot delete class: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
      log.error("Error deleting class {}", classId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
