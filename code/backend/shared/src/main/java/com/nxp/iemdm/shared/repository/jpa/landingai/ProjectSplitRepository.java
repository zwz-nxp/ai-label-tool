package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ProjectSplit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectSplitRepository extends JpaRepository<ProjectSplit, Long> {

  @Transactional(readOnly = true)
  List<ProjectSplit> findByProject_Id(Long projectId);

  @Transactional(readOnly = true)
  Optional<ProjectSplit> findByProject_IdAndProjectClassId(Long projectId, Long classId);

  @Transactional(readOnly = true)
  Optional<ProjectSplit> findByProject_IdAndProjectClassIsNull(Long projectId);

  /**
   * Delete all splits for a specific project.
   *
   * @param projectId the project ID
   */
  @org.springframework.data.jpa.repository.Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM ProjectSplit ps WHERE ps.project.id = :projectId")
  void deleteByProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);
}
