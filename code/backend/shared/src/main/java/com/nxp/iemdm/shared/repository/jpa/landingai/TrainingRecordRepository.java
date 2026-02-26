package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.TrainingRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {

  /**
   * Count training records for a specific project.
   *
   * @param projectId the project ID
   * @return the count of training records in the project
   */
  long countByProjectId(Long projectId);

  /**
   * Find a training record by ID with its project eagerly fetched. This is used for async YOLO
   * dataset generation to avoid LazyInitializationException.
   *
   * @param id the training record ID
   * @return the training record with project loaded, or empty if not found
   */
  @Query("SELECT tr FROM TrainingRecord tr JOIN FETCH tr.project WHERE tr.id = :id")
  Optional<TrainingRecord> findByIdWithProject(@Param("id") Long id);

  /**
   * Find all training records for a specific project that have snapshot_id and status is PENDING.
   *
   * @param projectId the project ID
   * @return list of training records
   */
  @Transactional(readOnly = true)
  @Query(
      "SELECT tr FROM TrainingRecord tr WHERE tr.project.id = :projectId "
          + "AND tr.snapshotId IS NOT NULL "
          + "AND UPPER(tr.status) = 'PENDING' "
          + "ORDER BY tr.startedAt DESC")
  List<TrainingRecord> findCompletedByProjectIdWithSnapshot(@Param("projectId") Long projectId);

  /**
   * Find all training records with a specific status, ordered by started_at descending. Used by
   * TrainingResultJob to find records awaiting results.
   *
   * @param status the status to filter by (e.g., "WAITFORRESULT")
   * @return list of training records with the specified status
   */
  @Transactional(readOnly = true)
  @Query(
      "SELECT tr FROM TrainingRecord tr WHERE UPPER(tr.status) = UPPER(:status) "
          + "ORDER BY tr.startedAt DESC")
  List<TrainingRecord> findByStatusOrderByStartedAtDesc(@Param("status") String status);
}
