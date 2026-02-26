package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.SnapshotProjectTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository for snapshot project tag data. */
@Repository
public interface SnapshotProjectTagRepository extends JpaRepository<SnapshotProjectTag, Long> {

  /**
   * Find all tags for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot project tags
   */
  @Transactional(readOnly = true)
  List<SnapshotProjectTag> findBySnapshotId(Long snapshotId);

  /**
   * Delete all tags for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   */
  @Modifying
  @Transactional
  void deleteBySnapshotId(Long snapshotId);
}
