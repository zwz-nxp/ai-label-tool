package com.nxp.iemdm.operational.service.landingai;

import com.nxp.iemdm.model.landingai.Project;
import com.nxp.iemdm.model.landingai.ProjectMetadata;
import com.nxp.iemdm.shared.repository.jpa.landingai.ImageMetadataRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectMetadataRepository;
import com.nxp.iemdm.shared.repository.jpa.landingai.ProjectRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProjectMetadataService {

  private final ProjectMetadataRepository projectMetadataRepository;
  private final ImageMetadataRepository imageMetadataRepository;
  private final ProjectRepository projectRepository;

  public ProjectMetadataService(
      ProjectMetadataRepository projectMetadataRepository,
      ImageMetadataRepository imageMetadataRepository,
      ProjectRepository projectRepository) {
    this.projectMetadataRepository = projectMetadataRepository;
    this.imageMetadataRepository = imageMetadataRepository;
    this.projectRepository = projectRepository;
  }

  @Transactional(readOnly = true)
  public List<ProjectMetadata> getMetadataByProjectId(Long projectId) {
    log.info("Getting metadata for project: {}", projectId);
    return projectMetadataRepository.findByProject_Id(projectId);
  }

  @Transactional(readOnly = true)
  public ProjectMetadata getMetadataById(Long id) {
    log.info("Getting metadata by id: {}", id);
    return projectMetadataRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Metadata not found with id: " + id));
  }

  /**
   * Create a new metadata definition
   *
   * @param projectId the project ID to associate the metadata with
   * @param metadata the metadata to create
   * @return the created metadata
   * @throws RuntimeException if project not found
   */
  @Transactional
  public ProjectMetadata createMetadata(Long projectId, ProjectMetadata metadata) {
    log.info("Creating metadata: {} for project: {}", metadata.getName(), projectId);

    // Fetch the project entity
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

    // Set the project on the metadata
    metadata.setProject(project);

    return projectMetadataRepository.save(metadata);
  }

  @Transactional
  public ProjectMetadata updateMetadata(Long id, ProjectMetadata metadataUpdate) {
    log.info("Updating metadata: {}", id);
    ProjectMetadata existing = getMetadataById(id);

    if (metadataUpdate.getName() != null) {
      existing.setName(metadataUpdate.getName());
    }
    if (metadataUpdate.getType() != null) {
      existing.setType(metadataUpdate.getType());
    }
    if (metadataUpdate.getValueFrom() != null) {
      existing.setValueFrom(metadataUpdate.getValueFrom());
    }
    if (metadataUpdate.getPredefinedValues() != null) {
      existing.setPredefinedValues(metadataUpdate.getPredefinedValues());
    }
    if (metadataUpdate.getMultipleValues() != null) {
      existing.setMultipleValues(metadataUpdate.getMultipleValues());
    }

    return projectMetadataRepository.save(existing);
  }

  /**
   * Delete a metadata definition Prevents deletion if images reference the metadata
   *
   * @param id the metadata ID to delete
   * @throws RuntimeException if metadata not found
   * @throws IllegalStateException if metadata is referenced by images
   */
  @Transactional
  public void deleteMetadata(Long id) {
    log.info("Deleting metadata: {}", id);

    if (!projectMetadataRepository.existsById(id)) {
      throw new RuntimeException("Metadata not found with id: " + id);
    }

    // Check if any images reference this metadata
    List<?> referencingImages = imageMetadataRepository.findByProjectMetadataId(id);
    if (!referencingImages.isEmpty()) {
      throw new IllegalStateException(
          "Cannot delete metadata: "
              + referencingImages.size()
              + " image(s) reference this metadata");
    }

    projectMetadataRepository.deleteById(id);
    log.info("Deleted metadata {}", id);
  }
}
