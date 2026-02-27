package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.landingai.ProjectClass;
import com.nxp.iemdm.shared.dto.landingai.ProjectClassDTO;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageLabelRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectClassRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProjectClassService {

  private final ProjectClassRepository projectClassRepository;
  private final ImageLabelRepository imageLabelRepository;
  private final ProjectRepository projectRepository;

  // Regex pattern for hex color code validation (#RRGGBB or #RGB)
  private static final Pattern HEX_COLOR_PATTERN =
      Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");

  public ProjectClassService(
      ProjectClassRepository projectClassRepository,
      ImageLabelRepository imageLabelRepository,
      ProjectRepository projectRepository) {
    this.projectClassRepository = projectClassRepository;
    this.imageLabelRepository = imageLabelRepository;
    this.projectRepository = projectRepository;
  }

  /**
   * Create a new class
   *
   * @param projectId the project ID to associate the class with
   * @param projectClass the class to create
   * @return the created class
   * @throws IllegalArgumentException if validation fails
   * @throws NotFoundException if project not found
   */
  @Transactional
  public ProjectClass createClass(Long projectId, ProjectClass projectClass) {
    // Fetch the project entity
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

    // Set the project on the class
    projectClass.setProject(project);

    validateProjectClass(projectClass);

    // Check for duplicate class name within the same project
    if (projectClass.getClassName() != null) {
      Optional<ProjectClass> existing =
          projectClassRepository.findByProject_IdAndClassName(
              projectId, projectClass.getClassName());

      if (existing.isPresent()) {
        throw new IllegalArgumentException(
            "Class with name '" + projectClass.getClassName() + "' already exists in this project");
      }
    }

    return projectClassRepository.save(projectClass);
  }

  /**
   * Get all classes for a specific project, ordered by creation time
   *
   * @param projectId the project ID
   * @return list of class DTOs with label counts
   */
  @Transactional(readOnly = true)
  public List<ProjectClassDTO> getClassesByProjectId(Long projectId) {
    List<ProjectClass> classes = projectClassRepository.findByProject_IdOrderByCreatedAt(projectId);

    return classes.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  /**
   * Convert ProjectClass entity to DTO with label count
   *
   * @param projectClass the entity to convert
   * @return the DTO with label count populated
   */
  private ProjectClassDTO convertToDTO(ProjectClass projectClass) {
    Long labelCount = imageLabelRepository.countByProjectClassId(projectClass.getId());

    ProjectClassDTO dto = new ProjectClassDTO();
    dto.setId(projectClass.getId());
    dto.setProjectId(projectClass.getProject().getId());
    dto.setClassName(projectClass.getClassName());
    dto.setDescription(projectClass.getDescription());
    dto.setColorCode(projectClass.getColorCode());
    dto.setCreatedAt(projectClass.getCreatedAt());
    dto.setCreatedBy(projectClass.getCreatedBy());
    dto.setLabelCount(labelCount != null ? labelCount.intValue() : 0);

    return dto;
  }

  /**
   * Update an existing class
   *
   * @param classId the class ID to update
   * @param updatedClass the updated class data
   * @return the updated class
   * @throws NotFoundException if class not found
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProjectClass updateClass(Long classId, ProjectClass updatedClass) {
    ProjectClass existingClass =
        projectClassRepository
            .findById(classId)
            .orElseThrow(() -> new NotFoundException("Class not found with id: " + classId));

    // Validate the updated class
    if (updatedClass.getClassName() != null && !updatedClass.getClassName().trim().isEmpty()) {
      // Check for duplicate class name (excluding current class)
      Optional<ProjectClass> duplicate =
          projectClassRepository.findByProject_IdAndClassName(
              existingClass.getProject().getId(), updatedClass.getClassName());

      if (duplicate.isPresent() && !duplicate.get().getId().equals(classId)) {
        throw new IllegalArgumentException(
            "Class with name '" + updatedClass.getClassName() + "' already exists in this project");
      }

      existingClass.setClassName(updatedClass.getClassName());
    }

    if (updatedClass.getColorCode() != null) {
      validateColorCode(updatedClass.getColorCode());
      existingClass.setColorCode(updatedClass.getColorCode());
    }

    if (updatedClass.getDescription() != null) {
      existingClass.setDescription(updatedClass.getDescription());
    }

    return projectClassRepository.save(existingClass);
  }

  /**
   * Delete a class Prevents deletion if labels reference the class
   *
   * @param classId the class ID to delete
   * @throws NotFoundException if class not found
   * @throws IllegalStateException if class is referenced by labels
   */
  @Transactional
  public void deleteClass(Long classId) {
    if (!projectClassRepository.existsById(classId)) {
      throw new NotFoundException("Class not found with id: " + classId);
    }

    // Check if any labels reference this class
    List<?> referencingLabels = imageLabelRepository.findByProjectClassId(classId);
    if (!referencingLabels.isEmpty()) {
      throw new IllegalStateException(
          "Cannot delete class: " + referencingLabels.size() + " label(s) reference this class");
    }

    projectClassRepository.deleteById(classId);
    log.info("Deleted class {}", classId);
  }

  // -------------------- Private Helper Methods --------------------

  /**
   * Validate project class data
   *
   * @param projectClass the class to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateProjectClass(ProjectClass projectClass) {
    if (projectClass == null) {
      throw new IllegalArgumentException("Project class cannot be null");
    }

    if (projectClass.getProject() == null) {
      throw new IllegalArgumentException("Project class must be associated with a project");
    }

    if (projectClass.getClassName() == null || projectClass.getClassName().trim().isEmpty()) {
      throw new IllegalArgumentException("Class name is required");
    }

    if (projectClass.getColorCode() == null || projectClass.getColorCode().trim().isEmpty()) {
      throw new IllegalArgumentException("Color code is required");
    }

    validateColorCode(projectClass.getColorCode());
  }

  /**
   * Validate color code format (hex color)
   *
   * @param colorCode the color code to validate
   * @throws IllegalArgumentException if color code is invalid
   */
  private void validateColorCode(String colorCode) {
    if (colorCode == null || colorCode.trim().isEmpty()) {
      throw new IllegalArgumentException("Color code cannot be null or empty");
    }

    String trimmedColor = colorCode.trim();

    if (!HEX_COLOR_PATTERN.matcher(trimmedColor).matches()) {
      throw new IllegalArgumentException(
          "Invalid color code format. Must be a hex color code (e.g., #FF5733 or #F57)");
    }
  }
}
