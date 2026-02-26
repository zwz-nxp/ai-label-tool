package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.Snapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {

  /**
   * Find all snapshots for a specific project.
   *
   * @param projectId the project ID
   * @return list of snapshots for the project
   */
  @Query("SELECT s FROM Snapshot s WHERE s.project.id = :projectId ORDER BY s.createdAt DESC")
  List<Snapshot> findByProjectId(@Param("projectId") Long projectId);

  /**
   * Check if a snapshot name already exists for a project.
   *
   * @param projectId the project ID
   * @param snapshotName the snapshot name
   * @return true if snapshot name exists
   */
  @Query(
      "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Snapshot s WHERE s.project.id ="
          + " :projectId AND s.snapshotName = :snapshotName")
  boolean existsByProjectIdAndSnapshotName(
      @Param("projectId") Long projectId, @Param("snapshotName") String snapshotName);

  /**
   * Count snapshots for a specific project.
   *
   * @param projectId the project ID
   * @return the count of snapshots in the project
   */
  @Query("SELECT COUNT(s) FROM Snapshot s WHERE s.project.id = :projectId")
  long countByProjectId(@Param("projectId") Long projectId);
}
