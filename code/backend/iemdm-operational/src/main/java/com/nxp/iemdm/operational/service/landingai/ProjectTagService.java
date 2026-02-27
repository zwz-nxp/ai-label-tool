package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.exception.NotFoundException;
import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.landingai.ProjectTag;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageTagRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectTagRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProjectTagService {

  private final ProjectTagRepository projectTagRepository;
  private final ImageTagRepository imageTagRepository;
  private final ProjectRepository projectRepository;

  public ProjectTagService(
      ProjectTagRepository projectTagRepository,
      ImageTagRepository imageTagRepository,
      ProjectRepository projectRepository) {
    this.projectTagRepository = projectTagRepository;
    this.imageTagRepository = imageTagRepository;
    this.projectRepository = projectRepository;
  }

  /**
   * Create a new tag
   *
   * @param projectId the project ID
   * @param projectTag the tag to create
   * @return the created tag
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProjectTag createTag(Long projectId, ProjectTag projectTag) {
    // Fetch the project and set it on the tag
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new IllegalArgumentException("Project not found with id: " + projectId));

    projectTag.setProject(project);

    validateProjectTag(projectTag);

    // Check for duplicate tag name within the same project (case-insensitive)
    Optional<ProjectTag> existing =
        projectTagRepository.findByProjectIdAndNameIgnoreCase(projectId, projectTag.getName());

    if (existing.isPresent()) {
      throw new IllegalArgumentException(
          "Tag with name '" + projectTag.getName() + "' already exists in this project");
    }

    return projectTagRepository.save(projectTag);
  }

  /**
   * Get all tags for a specific project
   *
   * @param projectId the project ID
   * @return list of tags
   */
  @Transactional(readOnly = true)
  public List<ProjectTag> getTagsByProjectId(Long projectId) {
    return projectTagRepository.findByProject_Id(projectId);
  }

  /**
   * Update an existing tag
   *
   * @param tagId the tag ID to update
   * @param updatedTag the updated tag data
   * @return the updated tag
   * @throws NotFoundException if tag not found
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional
  public ProjectTag updateTag(Long tagId, ProjectTag updatedTag) {
    ProjectTag existingTag =
        projectTagRepository
            .findById(tagId)
            .orElseThrow(() -> new NotFoundException("Tag not found with id: " + tagId));

    // Validate and update the tag name
    if (updatedTag.getName() != null && !updatedTag.getName().trim().isEmpty()) {
      // Check for duplicate tag name (excluding current tag, case-insensitive)
      Optional<ProjectTag> duplicate =
          projectTagRepository.findByProjectIdAndNameIgnoreCase(
              existingTag.getProject().getId(), updatedTag.getName());

      if (duplicate.isPresent() && !duplicate.get().getId().equals(tagId)) {
        throw new IllegalArgumentException(
            "Tag with name '" + updatedTag.getName() + "' already exists in this project");
      }

      existingTag.setName(updatedTag.getName());
    }

    return projectTagRepository.save(existingTag);
  }

  /**
   * Delete a tag Prevents deletion if images reference the tag
   *
   * @param tagId the tag ID to delete
   * @throws NotFoundException if tag not found
   * @throws IllegalStateException if tag is referenced by images
   */
  @Transactional
  public void deleteTag(Long tagId) {
    if (!projectTagRepository.existsById(tagId)) {
      throw new NotFoundException("Tag not found with id: " + tagId);
    }

    // Check if any images reference this tag
    List<?> referencingImages = imageTagRepository.findByProjectTagId(tagId);
    if (!referencingImages.isEmpty()) {
      throw new IllegalStateException(
          "Cannot delete tag: " + referencingImages.size() + " image(s) reference this tag");
    }

    projectTagRepository.deleteById(tagId);
    log.info("Deleted tag {}", tagId);
  }

  // -------------------- Private Helper Methods --------------------

  /**
   * Validate project tag data
   *
   * @param projectTag the tag to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateProjectTag(ProjectTag projectTag) {
    if (projectTag == null) {
      throw new IllegalArgumentException("Project tag cannot be null");
    }

    if (projectTag.getProject() == null) {
      throw new IllegalArgumentException("Project tag must be associated with a project");
    }

    if (projectTag.getName() == null || projectTag.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Tag name is required");
    }
  }
}
