package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.SnapshotImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository for snapshot image data. */
@Repository
public interface SnapshotImageRepository extends JpaRepository<SnapshotImage, Long> {

  /**
   * Find all images for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot images
   */
  @Transactional(readOnly = true)
  List<SnapshotImage> findBySnapshotId(Long snapshotId);

  /**
   * Count images for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return count of images
   */
  @Transactional(readOnly = true)
  long countBySnapshotId(Long snapshotId);

  /**
   * Delete all images for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   */
  @Modifying
  @Transactional
  void deleteBySnapshotId(Long snapshotId);

  /**
   * Find image IDs for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of image IDs
   */
  @Transactional(readOnly = true)
  @Query("SELECT si.id FROM SnapshotImage si WHERE si.snapshotId = :snapshotId")
  List<Long> findImageIdsBySnapshotId(@Param("snapshotId") Long snapshotId);

  /**
   * Count images for a specific snapshot and split.
   *
   * @param snapshotId the snapshot ID
   * @param split the split type (training, dev, test)
   * @return count of images
   */
  @Transactional(readOnly = true)
  Long countBySnapshotIdAndSplit(Long snapshotId, String split);

  /**
   * Count labeled images for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @param isLabeled the labeled status
   * @return count of labeled images
   */
  @Transactional(readOnly = true)
  Long countBySnapshotIdAndIsLabeled(Long snapshotId, Boolean isLabeled);

  /**
   * Find images by snapshot ID and split.
   *
   * @param snapshotId the snapshot ID
   * @param split the split value (training/dev/test)
   * @return list of snapshot images
   */
  @Transactional(readOnly = true)
  List<SnapshotImage> findBySnapshotIdAndSplit(Long snapshotId, String split);

  /**
   * Find all labeled images for a specific snapshot (isLabeled = true).
   *
   * @param snapshotId the snapshot ID
   * @return list of labeled snapshot images
   */
  @Transactional(readOnly = true)
  List<SnapshotImage> findBySnapshotIdAndIsLabeledTrue(Long snapshotId);
}
