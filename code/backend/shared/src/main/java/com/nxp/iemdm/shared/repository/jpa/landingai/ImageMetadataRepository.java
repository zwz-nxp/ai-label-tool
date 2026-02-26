package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ImageMetadata;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Long> {

  @Transactional(readOnly = true)
  List<ImageMetadata> findByImage_Id(Long imageId);

  @Transactional(readOnly = true)
  List<ImageMetadata> findByProjectMetadataId(Long metadataId);

  @Modifying
  @Transactional
  void deleteByImage_Id(Long imageId);

  /**
   * Delete all metadata for images belonging to a specific project.
   *
   * @param projectId the project ID
   */
  @Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM ImageMetadata im WHERE im.image.project.id = :projectId")
  void deleteByImageProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);
}
