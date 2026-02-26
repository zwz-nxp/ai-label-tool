package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ProjectClass;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectClassRepository extends JpaRepository<ProjectClass, Long> {

  @Transactional(readOnly = true)
  List<ProjectClass> findByProject_Id(Long projectId);

  @Transactional(readOnly = true)
  List<ProjectClass> findByProject_IdOrderByCreatedAt(Long projectId);

  /**
   * Find all classes for a project ordered by ID ascending. This ensures consistent class index
   * mapping for YOLO training.
   *
   * @param projectId the project ID
   * @return list of classes ordered by ID
   */
  @Transactional(readOnly = true)
  List<ProjectClass> findByProjectIdOrderByIdAsc(Long projectId);

  @Transactional(readOnly = true)
  Optional<ProjectClass> findByProject_IdAndClassName(Long projectId, String className);

  /**
   * Delete all classes for a specific project.
   *
   * @param projectId the project ID
   */
  @org.springframework.data.jpa.repository.Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM ProjectClass pc WHERE pc.project.id = :projectId")
  void deleteByProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);
}
