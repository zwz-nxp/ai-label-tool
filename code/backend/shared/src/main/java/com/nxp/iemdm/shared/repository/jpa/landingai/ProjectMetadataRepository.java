package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ProjectMetadata;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectMetadataRepository extends JpaRepository<ProjectMetadata, Long> {

  @Transactional(readOnly = true)
  List<ProjectMetadata> findByProject_Id(Long projectId);

  @Transactional(readOnly = true)
  Optional<ProjectMetadata> findByProject_IdAndName(Long projectId, String name);

  /**
   * Delete all metadata for a specific project.
   *
   * @param projectId the project ID
   */
  @org.springframework.data.jpa.repository.Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM ProjectMetadata pm WHERE pm.project.id = :projectId")
  void deleteByProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);
}
