package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.SnapshotProjectSplit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository for snapshot project split data. */
@Repository
public interface SnapshotProjectSplitRepository extends JpaRepository<SnapshotProjectSplit, Long> {

  /**
   * Find all splits for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot project splits
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectSplit> findBySnapshotId(Long snapshotId);

  /**
   * Delete all splits for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   */
  @Modifying
  @Transactional
  void deleteBySnapshotId(Long snapshotId);
}
