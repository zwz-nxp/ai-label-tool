package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.SnapshotImageMetadata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository for snapshot image metadata data. */
@Repository
public interface SnapshotImageMetadataRepository
    extends JpaRepository<SnapshotImageMetadata, Long> {

  /**
   * Find all metadata for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   * @return list of snapshot image metadata
   */
  @Transactional(readOnly = true)
  List<SnapshotImageMetadata> findBySnapshotId(Long snapshotId);

  /**
   * Delete all metadata for a specific snapshot.
   *
   * @param snapshotId the snapshot ID
   */
  @Modifying
  @Transactional
  void deleteBySnapshotId(Long snapshotId);
}
