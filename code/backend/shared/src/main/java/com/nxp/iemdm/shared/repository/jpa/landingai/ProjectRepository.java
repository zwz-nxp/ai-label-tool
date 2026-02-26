package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Transactional(readOnly = true)
  Optional<Project> findByName(String name);

  @Transactional(readOnly = true)
  List<Project> findByStatusOrderByCreatedAtDesc(String status);

  @Transactional(readOnly = true)
  List<Project> findByCreatedByOrderByCreatedAtDesc(String createdBy);

  @Transactional(readOnly = true)
  List<Project> findAllByOrderByCreatedAtDesc();

  /**
   * Find projects by location ID and creator, ordered by name ascending, then by ID descending.
   *
   * @param locationId the location ID
   * @param createdBy the creator's user identifier
   * @return list of projects matching the criteria, ordered by name ASC, id DESC
   */
  List<Project> findByLocation_IdAndCreatedByOrderByNameAscIdDesc(
      Long locationId, String createdBy);

  /**
   * Find all projects by location ID, ordered by name ascending, then by ID descending.
   *
   * @param locationId the location ID
   * @return list of projects in the location, ordered by name ASC, id DESC
   */
  List<Project> findByLocation_IdOrderByNameAscIdDesc(Long locationId);

  /**
   * Check if a project with the given name exists in the specified location.
   *
   * @param name the project name
   * @param locationId the location ID
   * @return true if a project with the name exists in the location
   */
  boolean existsByNameAndLocation_Id(String name, Long locationId);

  /**
   * Count images for a specific project.
   *
   * @param projectId the project ID
   * @return the count of images in the project
   */
  @Query("SELECT COUNT(i) FROM Image i WHERE i.project.id = :projectId")
  Long countImagesByProjectId(@Param("projectId") Long projectId);

  /**
   * Count labels for a specific project. Labels are associated with images, which are associated
   * with projects.
   *
   * @param projectId the project ID
   * @return the count of labels in the project
   */
  @Query("SELECT COUNT(il) FROM ImageLabel il WHERE il.image.project.id = :projectId")
  Long countLabelsByProjectId(@Param("projectId") Long projectId);

  /**
   * Count labeled images for a specific project using the is_labeled flag.
   *
   * @param projectId the project ID
   * @return the count of labeled images in the project
   */
  @Query("SELECT COUNT(i) FROM Image i WHERE i.project.id = :projectId AND i.isLabeled = true")
  Long countLabeledImagesByProjectId(@Param("projectId") Long projectId);
}
