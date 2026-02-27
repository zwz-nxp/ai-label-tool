package com.nxp.iemdm.operational.service.rest.landingai;

import com.nxp.iemdm.exception.landingai.DuplicateProjectNameException;
import com.nxp.iemdm.exception.landingai.InvalidProjectTypeException;
import com.nxp.iemdm.model.landingai.Image;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.dto.landingai.ProjectCreateRequest;
import com.nxp.iemdm.shared.dto.landingai.ProjectDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectListItemDTO;
import com.nxp.iemdm.shared.dto.landingai.ProjectUpdateRequest;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * REST controller for Landing AI project operations in the operational layer. Provides internal
 * endpoints that are called by the API service layer. Includes all business logic for project
 * management.
 */
@Slf4j
@RestController
@RequestMapping("/operational/landingai/projects")
public class ProjectServiceImpl {

  private static final String OBJECT_DETECTION = "Object Detection";
  private static final String CLASSIFICATION = "Classification";

  private final ProjectRepository projectRepository;
  private final ImageRepository imageRepository;

  @PersistenceContext private EntityManager entityManager;

  @Autowired
  public ProjectServiceImpl(ProjectRepository projectRepository, ImageRepository imageRepository) {
    this.projectRepository = projectRepository;
    this.imageRepository = imageRepository;
  }

  /**
   * Get projects for a user with optional view all mode.
   *
   * @param userId the user identifier
   * @param locationId the location ID
   * @param viewAll if true, return all projects in location; if false, return only user's projects
   * @return list of project list items
   */
  @MethodLog
  @GetMapping(produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public List<ProjectListItemDTO> getProjectsForUser(
      @RequestParam("userId") String userId,
      @RequestParam("locationId") Long locationId,
      @RequestParam(value = "viewAll", defaultValue = "false") boolean viewAll) {

    log.info(
        "Operational REST: Getting projects for user: {}, location: {}, viewAll: {}",
        userId,
        locationId,
        viewAll);

    List<Project> projects;
    if (viewAll) {
      projects = projectRepository.findByLocation_IdOrderByNameAscIdDesc(locationId);
    } else {
      projects =
          projectRepository.findByLocation_IdAndCreatedByOrderByNameAscIdDesc(locationId, userId);
    }

    return projects.stream().map(this::convertToListItemDTO).collect(Collectors.toList());
  }

  /**
   * Create a new project.
   *
   * @param request the project creation request
   * @param userId the user identifier
   * @param locationId the location ID
   * @return the created project
   */
  @MethodLog
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  @Transactional
  public ProjectDTO createProject(
      @RequestBody @Valid ProjectCreateRequest request,
      @RequestParam("userId") String userId,
      @RequestParam("locationId") Long locationId) {

    log.info(
        "Operational REST: Creating project: name={}, type={}, modelName={}, user={}, location={}",
        request.getName(),
        request.getType(),
        request.getModelName(),
        userId,
        locationId);

    // Validate project type
    if (!OBJECT_DETECTION.equals(request.getType()) && !CLASSIFICATION.equals(request.getType())) {
      throw new InvalidProjectTypeException(request.getType());
    }

    // Check for duplicate name
    if (isProjectNameUnique(request.getName(), locationId)) {
      throw new DuplicateProjectNameException(request.getName(), locationId);
    }

    // Create project entity
    Project project = new Project();
    project.setName(request.getName());
    project.setType(request.getType());
    // Handle null or empty modelName
    if (request.getModelName() != null && !request.getModelName().trim().isEmpty()) {
      project.setModelName(request.getModelName());
    }
    // Handle groupName
    if (request.getGroupName() != null && !request.getGroupName().trim().isEmpty()) {
      project.setGroupName(request.getGroupName());
    }
    project.setStatus("Upload"); // Initial status
    project.setCreatedBy(userId);

    // Set location reference
    Location location = entityManager.getReference(Location.class, locationId);
    project.setLocation(location);

    // Save project
    Project savedProject = projectRepository.save(project);
    log.info("Created project with id: {}", savedProject.getId());

    return convertToDTO(savedProject);
  }

  /**
   * Get project by ID.
   *
   * @param id the project ID
   * @return the project details
   */
  @MethodLog
  @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public ProjectDTO getProjectById(@PathVariable("id") @NotNull Long id) {

    log.info("Operational REST: Getting project by id: {}", id);

    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Project not found with id: " + id));

    return convertToDTO(project);
  }

  /**
   * Update project name and model name.
   *
   * @param id the project ID
   * @param request the project update request
   * @param userId the user identifier
   * @return the updated project
   */
  @MethodLog
  @PutMapping(
      path = "/{id}",
      consumes = MediaType.APPLICATION_JSON,
      produces = MediaType.APPLICATION_JSON)
  @Transactional
  public ProjectDTO updateProject(
      @PathVariable("id") @NotNull Long id,
      @RequestBody @Valid ProjectUpdateRequest request,
      @RequestParam("userId") String userId) {

    log.info(
        "Operational REST: Updating project: id={}, name={}, modelName={}, user={}",
        id,
        request.getName(),
        request.getModelName(),
        userId);

    // Find existing project
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Project not found with id: " + id));

    // Check if name is being changed and if new name is unique
    if (!project.getName().equals(request.getName())) {
      if (isProjectNameUnique(request.getName(), project.getLocation().getId().longValue())) {
        throw new DuplicateProjectNameException(
            request.getName(), project.getLocation().getId().longValue());
      }
    }

    // Update fields
    project.setName(request.getName());
    // Handle null or empty modelName
    if (request.getModelName() != null && !request.getModelName().trim().isEmpty()) {
      project.setModelName(request.getModelName());
    } else {
      project.setModelName(null);
    }
    // Handle groupName
    if (request.getGroupName() != null && !request.getGroupName().trim().isEmpty()) {
      project.setGroupName(request.getGroupName());
    } else {
      project.setGroupName(null);
    }

    // Save updated project
    Project updatedProject = projectRepository.save(project);
    log.info("Updated project with id: {}", updatedProject.getId());

    return convertToDTO(updatedProject);
  }

  /**
   * Delete project by ID.
   *
   * @param id the project ID
   * @param userId the user identifier
   */
  @MethodLog
  @DeleteMapping(path = "/{id}")
  @Transactional
  public void deleteProject(
      @PathVariable("id") @NotNull Long id, @RequestParam("userId") String userId) {

    log.info("Operational REST: Deleting project: id={}, user={}", id, userId);

    // Find existing project
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new jakarta.persistence.EntityNotFoundException(
                        "Project not found with id: " + id));

    // Delete all images associated with the project first
    List<Image> images = imageRepository.findByProject_Id(id);
    if (!images.isEmpty()) {
      log.info("Deleting {} images for project id: {}", images.size(), id);
      imageRepository.deleteAll(images);
      // Flush to ensure images are deleted before project deletion
      entityManager.flush();
    }

    // Delete project (cascade will handle related entities like ProjectClass, ProjectTag, etc.)
    projectRepository.delete(project);
    log.info("Deleted project with id: {}", id);
  }

  /**
   * Check if a project name is unique within a location.
   *
   * @param name the project name
   * @param locationId the location ID
   * @return true if name already exists, false if unique
   */
  @Transactional(readOnly = true)
  public boolean isProjectNameUnique(String name, Long locationId) {
    return projectRepository.existsByNameAndLocation_Id(name, locationId);
  }

  /**
   * Convert Project entity to ProjectDTO.
   *
   * @param project the project entity
   * @return the project DTO
   */
  private ProjectDTO convertToDTO(Project project) {
    Long locationId = null;
    if (project.getLocation() != null && project.getLocation().getId() != null) {
      locationId = project.getLocation().getId().longValue();
    }

    return new ProjectDTO(
        project.getId(),
        project.getName(),
        project.getStatus(),
        project.getType(),
        project.getModelName(),
        project.getGroupName(),
        locationId,
        project.getCreatedAt(),
        project.getCreatedBy());
  }

  /**
   * Convert Project entity to ProjectListItemDTO with counts and thumbnail.
   *
   * @param project the project entity
   * @return the project list item DTO
   */
  private ProjectListItemDTO convertToListItemDTO(Project project) {
    // Calculate image count
    Long imageCount = projectRepository.countImagesByProjectId(project.getId());

    // Calculate labeled images count using is_labeled flag
    Long labelCount = projectRepository.countLabeledImagesByProjectId(project.getId());

    // Get first image thumbnail
    byte[] thumbnail = null;
    Optional<Image> firstImage =
        imageRepository.findFirstByProject_IdOrderByCreatedAtAsc(project.getId());
    if (firstImage.isPresent()) {
      thumbnail = firstImage.get().getThumbnailImage();
    }

    // Convert counts to Integer, defaulting to 0 if null
    Integer imageCountInt = (imageCount != null) ? imageCount.intValue() : Integer.valueOf(0);
    Integer labelCountInt = (labelCount != null) ? labelCount.intValue() : Integer.valueOf(0);

    return new ProjectListItemDTO(
        project.getId(),
        project.getName(),
        project.getType(),
        project.getModelName(),
        project.getGroupName(),
        project.getCreatedBy(),
        project.getCreatedAt(),
        imageCountInt,
        labelCountInt,
        thumbnail);
  }
}
