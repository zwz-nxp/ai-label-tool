package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.service.rest.landingai.ProjectClassServiceREST;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ProjectClassDTO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * API Controller for Project Class operations. Provides RESTful endpoints for frontend
 * communication.
 *
 * <p>Requirements: 24.3, 24.4, 24.5, 24.6
 */
@RestController
@RequestMapping("/api/landingai/project-classes")
@Slf4j
public class ProjectClassApiController {

  private final ProjectClassServiceREST projectClassServiceREST;

  @Autowired
  public ProjectClassApiController(ProjectClassServiceREST projectClassServiceREST) {
    this.projectClassServiceREST = projectClassServiceREST;
  }

  /**
   * Create a new project class
   *
   * @param projectId the project ID to associate the class with
   * @param projectClass the class to create
   * @return the created class with HTTP 201 status
   */
  @MethodLog
  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectClass> createClass(
      @RequestParam("projectId") Long projectId, @RequestBody @Valid ProjectClass projectClass) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      ProjectClass createdClass = projectClassServiceREST.createClass(projectId, projectClass);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
    } catch (Exception e) {
      log.error("Error creating project class: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Get all classes for a specific project
   *
   * @param projectId the project ID
   * @return list of class DTOs with label counts and HTTP 200 status
   */
  @MethodLog
  @GetMapping(path = "/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ProjectClassDTO>> getClassesByProjectId(
      @PathVariable("projectId") Long projectId) {
    try {
      if (projectId == null || projectId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      List<ProjectClassDTO> classes = projectClassServiceREST.getClassesByProjectId(projectId);
      return ResponseEntity.ok(classes);
    } catch (Exception e) {
      log.error("Error getting classes for project {}: {}", projectId, e.getMessage());
      throw e;
    }
  }

  /**
   * Update an existing class
   *
   * @param classId the class ID to update
   * @param projectClass the updated class data
   * @return the updated class with HTTP 200 status
   */
  @MethodLog
  @PutMapping(
      path = "/{classId}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProjectClass> updateClass(
      @PathVariable("classId") Long classId, @RequestBody @Valid ProjectClass projectClass) {
    try {
      if (classId == null || classId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      ProjectClass updatedClass = projectClassServiceREST.updateClass(classId, projectClass);
      return ResponseEntity.ok(updatedClass);
    } catch (Exception e) {
      log.error("Error updating class {}: {}", classId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete a class
   *
   * @param classId the class ID to delete
   * @return HTTP 204 No Content status on success
   */
  @MethodLog
  @DeleteMapping(path = "/{classId}")
  public ResponseEntity<Void> deleteClass(@PathVariable("classId") Long classId) {
    try {
      if (classId == null || classId <= 0) {
        return ResponseEntity.badRequest().build();
      }
      projectClassServiceREST.deleteClass(classId);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting class {}: {}", classId, e.getMessage());
      throw e;
    }
  }
}
