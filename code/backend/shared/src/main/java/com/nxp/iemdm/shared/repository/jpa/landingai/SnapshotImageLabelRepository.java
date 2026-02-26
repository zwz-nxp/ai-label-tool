package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.SnapshotImageLabel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository for snapshot image label data. */
@Repository
public interface SnapshotImageLabelRepository extends JpaRepository<SnapshotImageLabel, Long> {

  /**
   * Find all labels for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot image labels
   */
  @Transactional(readOnly = true)
  List<SnapshotImageLabel> findBySnapshotId(Long snapshotId);

  /**
   * Count labels for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return count of labels
   */
  @Transactional(readOnly = true)
  long countBySnapshotId(Long snapshotId);

  /**
   * Delete all labels for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   */
  @Modifying
  @Transactional
  void deleteBySnapshotId(Long snapshotId);

  /**
   * Find labels by snapshot ID and image IDs.
   *
   * @param snapshotId the snapshot ID
   * @param imageIds list of image IDs
   * @return list of snapshot image labels
   */
  @Transactional(readOnly = true)
  List<SnapshotImageLabel> findBySnapshotIdAndImageIdIn(Long snapshotId, List<Long> imageIds);
}
