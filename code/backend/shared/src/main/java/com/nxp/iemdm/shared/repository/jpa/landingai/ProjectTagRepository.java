package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ProjectTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectTagRepository extends JpaRepository<ProjectTag, Long> {

  @Transactional(readOnly = true)
  List<ProjectTag> findByProject_Id(Long projectId);

  @Transactional(readOnly = true)
  Optional<ProjectTag> findByProject_IdAndName(Long projectId, String name);

  /**
   * Find a tag by project ID and name (case-insensitive)
   *
   * @param projectId the project ID
   * @param name the tag name
   * @return optional containing the tag if found
   */
  @Transactional(readOnly = true)
  @org.springframework.data.jpa.repository.Query(
      "SELECT pt FROM ProjectTag pt WHERE pt.project.id = :projectId AND LOWER(pt.name) = LOWER(:name)")
  Optional<ProjectTag> findByProjectIdAndNameIgnoreCase(
      @org.springframework.data.repository.query.Param("projectId") Long projectId,
      @org.springframework.data.repository.query.Param("name") String name);

  /**
   * Delete all tags for a specific project.
   *
   * @param projectId the project ID
   */
  @org.springframework.data.jpa.repository.Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM ProjectTag pt WHERE pt.project.id = :projectId")
  void deleteByProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);
}
