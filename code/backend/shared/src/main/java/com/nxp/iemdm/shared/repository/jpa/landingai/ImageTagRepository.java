package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ImageTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {

  @Transactional(readOnly = true)
  List<ImageTag> findByImage_Id(Long imageId);

  @Transactional(readOnly = true)
  List<ImageTag> findByProjectTagId(Long tagId);

  @Transactional(readOnly = true)
  boolean existsByImage_IdAndProjectTagId(Long imageId, Long projectTagId);

  @Modifying
  @Transactional
  void deleteByImage_Id(Long imageId);

  /**
   * Find image IDs that have any of the specified tag IDs for a given project. Used for tag
   * filtering.
   *
   * @param projectId the project ID
   * @param tagIds list of tag IDs to match
   * @return list of image IDs that have any of the specified tags
   */
  @Transactional(readOnly = true)
  @org.springframework.data.jpa.repository.Query(
      "SELECT DISTINCT it.image.id FROM ImageTag it "
          + "WHERE it.image.project.id = :projectId "
          + "AND it.projectTag.id IN :tagIds")
  List<Long> findImageIdsByProjectIdAndTagIds(
      @org.springframework.data.repository.query.Param("projectId") Long projectId,
      @org.springframework.data.repository.query.Param("tagIds") List<Long> tagIds);

  /**
   * Delete all tags for images belonging to a specific project.
   *
   * @param projectId the project ID
   */
  @Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM ImageTag it WHERE it.image.project.id = :projectId")
  void deleteByImageProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);
}
