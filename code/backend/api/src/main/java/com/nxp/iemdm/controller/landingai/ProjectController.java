package com.nxp.iemdm.controller.landingai;

import com.nxp.iemdm.exception.landingai.DuplicateProjectNameException;
import com.nxp.iemdm.exception.landingai.InvalidProjectTypeException;
import com.nxp.iemdm.service.ProjectService;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ProjectCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectUpdateRequest;
import com.nxp.iemdm.spring.security.IEMDMPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Landing AI project operations. Provides endpoints for creating, retrieving,
 * and listing projects with location-based filtering.
 */
@Slf4j
@RestController
@RequestMapping("/api/landingai/projects")
@CrossOrigin(
    origins = "*",
    methods = {
      org.springframework.web.bind.annotation.RequestMethod.GET,
      org.springframework.web.bind.annotation.RequestMethod.POST,
      org.springframework.web.bind.annotation.RequestMethod.PUT,
      org.springframework.web.bind.annotation.RequestMethod.DELETE
    })
public class ProjectController {

  private final ProjectService projectService;

  @Autowired
  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  /**
   * Get projects for the current user with optional view all mode.
   *
   * @param viewAll if true, return all projects in location; if false, return only user's projects
   * @param locationId the location ID from request header
   * @param user the authenticated user
   * @return list of project list items
   */
  @MethodLog
  @GetMapping(produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<List<ProjectListItemDTO>> getProjects(
      @RequestParam(value = "viewAll", defaultValue = "false") boolean viewAll,
      @RequestParam(value = "locationId", required = true) Long locationId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Getting projects for user: {}, location: {}, viewAll: {}",
        user.getUsername(),
        locationId,
        viewAll);

    List<ProjectListItemDTO> projects =
        projectService.getProjectsForUser(user.getUsername(), locationId, viewAll);

    return ResponseEntity.ok(projects);
  }

  /**
   * Create a new project.
   *
   * @param request the project creation request
   * @param locationId the location ID from request header
   * @param user the authenticated user
   * @return the created project
   */
  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectDTO> createProject(
      @RequestBody @Valid ProjectCreateRequest request,
      @RequestParam(value = "locationId", required = true) Long locationId,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Creating project: name={}, type={}, user={}, location={}",
        request.getName(),
        request.getType(),
        user.getUsername(),
        locationId);

    ProjectDTO createdProject =
        projectService.createProject(request, user.getUsername(), locationId);

    return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
  }

  /**
   * Get project by ID.
   *
   * @param id the project ID
   * @param user the authenticated user
   * @return the project details
   */
  @MethodLog
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectDTO> getProjectById(
      @PathVariable("id") @NotNull Long id, @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Getting project by id: {} for user: {}", id, user.getUsername());

    ProjectDTO project = projectService.getProjectById(id);

    return ResponseEntity.ok(project);
  }

  /**
   * Update project name and model name.
   *
   * @param id the project ID
   * @param request the project update request
   * @param user the authenticated user
   * @return the updated project
   */
  @MethodLog
  @PutMapping(
      path = "/{id}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  public ResponseEntity<ProjectDTO> updateProject(
      @PathVariable("id") @NotNull Long id,
      @RequestBody @Valid ProjectUpdateRequest request,
      @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info(
        "Updating project: id={}, name={}, modelName={}, user={}",
        id,
        request.getName(),
        request.getModelName(),
        user.getUsername());

    ProjectDTO updatedProject = projectService.updateProject(id, request, user.getUsername());

    return ResponseEntity.ok(updatedProject);
  }

  /**
   * Delete project by ID.
   *
   * @param id the project ID
   * @param user the authenticated user
   * @return no content response
   */
  @MethodLog
  @DeleteMapping(path = "/{id}")
  public ResponseEntity<Void> deleteProject(
      @PathVariable("id") @NotNull Long id, @AuthenticationPrincipal IEMDMPrincipal user) {

    log.info("Deleting project: id={}, user={}", id, user.getUsername());

    projectService.deleteProject(id, user.getUsername());

    return ResponseEntity.noContent().build();
  }

  /**
   * Exception handler for DuplicateProjectNameException.
   *
   * @param ex the exception
   * @return error response with 409 Conflict status
   */
  @ExceptionHandler(DuplicateProjectNameException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateProjectName(
      DuplicateProjectNameException ex) {
    log.error("Duplicate project name error: {}", ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.CONFLICT.value());
    errorResponse.put("error", "Conflict");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  /**
   * Exception handler for InvalidProjectTypeException.
   *
   * @param ex the exception
   * @return error response with 400 Bad Request status
   */
  @ExceptionHandler(InvalidProjectTypeException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidProjectType(
      InvalidProjectTypeException ex) {
    log.error("Invalid project type error: {}", ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
    errorResponse.put("error", "Bad Request");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Exception handler for EntityNotFoundException.
   *
   * @param ex the exception
   * @return error response with 404 Not Found status
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
    log.error("Entity not found error: {}", ex.getMessage());

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
    errorResponse.put("error", "Not Found");
    errorResponse.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * Exception handler for general exceptions.
   *
   * @param ex the exception
   * @return error response with 500 Internal Server Error status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
    log.error("Internal server error: {}", ex.getMessage(), ex);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now().toString());
    errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    errorResponse.put("error", "Internal Server Error");
    errorResponse.put("message", "An unexpected error occurred");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
