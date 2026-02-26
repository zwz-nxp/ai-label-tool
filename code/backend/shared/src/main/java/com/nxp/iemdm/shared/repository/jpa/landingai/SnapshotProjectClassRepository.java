package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.SnapshotProjectClass;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository for snapshot project class data. */
@Repository
public interface SnapshotProjectClassRepository extends JpaRepository<SnapshotProjectClass, Long> {

  /**
   * Find all classes for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot project classes
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectClass> findBySnapshotId(Long snapshotId);

  /**
   * Find all classes for a specific snapshot, ordered by ID ascending.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot project classes ordered by ID
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectClass> findBySnapshotIdOrderByIdAsc(Long snapshotId);

  /**
   * Find all classes for a specific snapshot, ordered by sequence ascending.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot project classes ordered by sequence
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectClass> findBySnapshotIdOrderBySequenceAsc(Long snapshotId);

  /**
   * Count classes for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return count of classes
   */
  @Transactional(readOnly = true)
  long countBySnapshotId(Long snapshotId);

  /**
   * Delete all classes for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   */
  @Modifying
  @Transactional
  void deleteBySnapshotId(Long snapshotId);

  /**
   * Find classes by snapshot ID ordered by sequence.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot project classes ordered by sequence
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectClass> findBySnapshotIdOrderBySequence(Long snapshotId);

  /**
   * Find all classes for a specific project and snapshot. Used by TrainingResultJob to build class
   * ID mapping.
   *
   * @param projectId the project ID
   * @param snapshotId the snapshot ID
   * @return list of snapshot project classes
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectClass> findByProjectIdAndSnapshotId(Long projectId, Long snapshotId);
}
