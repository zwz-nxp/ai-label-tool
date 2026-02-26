package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.Image;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

  @Transactional(readOnly = true)
  List<Image> findByProject_IdOrderByCreatedAtDesc(Long projectId);

  @Transactional(readOnly = true)
  Optional<Image> findByFileName(String fileName);

  @Transactional(readOnly = true)
  List<Image> findByProject_IdAndSplit(Long projectId, String split);

  @Transactional(readOnly = true)
  List<Image> findByProject_IdAndIsNoClassTrue(Long projectId);

  /**
   * Find all images for a project.
   *
   * @param projectId the project ID
   * @return list of images
   */
  List<Image> findByProject_Id(Long projectId);

  /**
   * Find all images for a project, ordered by creation date ascending.
   *
   * @param projectId the project ID
   * @return list of images ordered by creation date
   */
  List<Image> findByProject_IdOrderByCreatedAtAsc(Long projectId);

  /**
   * Find the first image for a project, ordered by creation date ascending.
   *
   * @param projectId the project ID
   * @return optional containing the first image if exists
   */
  Optional<Image> findFirstByProject_IdOrderByCreatedAtAsc(Long projectId);

  /**
   * Count images for a specific project.
   *
   * @param projectId the project ID
   * @return the count of images in the project
   */
  Long countByProject_Id(Long projectId);

  /**
   * Count images for a specific project (alternative method name for consistency).
   *
   * @param projectId the project ID
   * @return the count of images in the project
   */
  default long countByProjectId(Long projectId) {
    Long count = countByProject_Id(projectId);
    return count != null ? count : 0L;
  }

  /**
   * Find images for a project with pagination support.
   *
   * @param projectId the project ID
   * @param pageable pagination information (page number, size, sort)
   * @return page of images
   */
  Page<Image> findByProject_Id(Long projectId, Pageable pageable);

  /**
   * Count labeled images for a specific project.
   *
   * @param projectId the project ID
   * @return the count of labeled images in the project
   */
  Long countByProject_IdAndIsLabeled(Long projectId, Boolean isLabeled);

  /**
   * Count images for a specific project and split.
   *
   * @param projectId the project ID
   * @param split the split name (training, dev, test)
   * @return the count of images in the project with the specified split
   */
  Long countByProject_IdAndSplit(Long projectId, String split);

  /**
   * Find images by IDs and project ID.
   *
   * @param ids the list of image IDs
   * @param projectId the project ID
   * @return list of images matching the IDs and project
   */
  List<Image> findByIdInAndProject_Id(List<Long> ids, Long projectId);

  /**
   * Count labeled images (is_labeled = true and is_no_class = false).
   *
   * @param projectId the project ID
   * @return the count of labeled images
   */
  Long countByProject_IdAndIsLabeledTrueAndIsNoClassFalse(Long projectId);

  /**
   * Count unlabeled images (is_labeled = false).
   *
   * @param projectId the project ID
   * @return the count of unlabeled images
   */
  Long countByProject_IdAndIsLabeledFalse(Long projectId);

  /**
   * Count no_class images (is_labeled = true and is_no_class = true).
   *
   * @param projectId the project ID
   * @return the count of no_class images
   */
  Long countByProject_IdAndIsLabeledTrueAndIsNoClassTrue(Long projectId);

  /**
   * Count images by project and split (case insensitive).
   *
   * @param projectId the project ID
   * @param split the split name
   * @return the count of images with the specified split
   */
  Long countByProject_IdAndSplitIgnoreCase(Long projectId, String split);

  /**
   * Count images with no split assigned (null or empty).
   *
   * @param projectId the project ID
   * @param emptySplit empty string for comparison
   * @return the count of images with no split
   */
  @org.springframework.data.jpa.repository.Query(
      "SELECT COUNT(i) FROM Image i WHERE i.project.id = :projectId AND (i.split IS NULL OR i.split = :emptySplit)")
  Long countByProject_IdAndSplitIsNullOrSplitEquals(
      @org.springframework.data.repository.query.Param("projectId") Long projectId,
      @org.springframework.data.repository.query.Param("emptySplit") String emptySplit);

  /**
   * Find image IDs with assigned splits (training, dev, test).
   *
   * @param projectId the project ID
   * @param splits list of split values to match (lowercase)
   * @return list of image IDs matching the split criteria
   */
  @org.springframework.data.jpa.repository.Query(
      "SELECT i.id FROM Image i WHERE i.project.id = :projectId AND LOWER(i.split) IN :splits")
  List<Long> findImageIdsByAssignedSplits(
      @org.springframework.data.repository.query.Param("projectId") Long projectId,
      @org.springframework.data.repository.query.Param("splits") List<String> splits);

  /**
   * Find image IDs with unassigned split (null or empty).
   *
   * @param projectId the project ID
   * @return list of image IDs with no split assigned
   */
  @org.springframework.data.jpa.repository.Query(
      "SELECT i.id FROM Image i WHERE i.project.id = :projectId AND (i.split IS NULL OR i.split = '')")
  List<Long> findImageIdsWithUnassignedSplit(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);

  /**
   * Find all image IDs for a project.
   *
   * @param projectId the project ID
   * @return list of all image IDs in the project
   */
  @org.springframework.data.jpa.repository.Query(
      "SELECT i.id FROM Image i WHERE i.project.id = :projectId")
  List<Long> findImageIdsByProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);

  /**
   * Find image IDs where is_labeled = true (labeled images).
   *
   * @param projectId the project ID
   * @return list of image IDs that are labeled
   */
  @org.springframework.data.jpa.repository.Query(
      "SELECT i.id FROM Image i WHERE i.project.id = :projectId AND i.isLabeled = true")
  List<Long> findLabeledImageIds(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);

  /**
   * Find image IDs where is_labeled = false (unlabeled images).
   *
   * @param projectId the project ID
   * @return list of image IDs that are unlabeled
   */
  @org.springframework.data.jpa.repository.Query(
      "SELECT i.id FROM Image i WHERE i.project.id = :projectId AND (i.isLabeled = false OR i.isLabeled IS NULL)")
  List<Long> findUnlabeledImageIds(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);

  /**
   * Delete all images for a specific project.
   *
   * @param projectId the project ID
   */
  @org.springframework.data.jpa.repository.Modifying
  @Transactional
  @org.springframework.data.jpa.repository.Query(
      "DELETE FROM Image i WHERE i.project.id = :projectId")
  void deleteByProjectId(
      @org.springframework.data.repository.query.Param("projectId") Long projectId);

  /**
   * Find images by project ID with offset and limit for batch processing.
   *
   * @param projectId the project ID
   * @param offset the starting position
   * @param limit the maximum number of results
   * @return list of images
   */
  @org.springframework.data.jpa.repository.Query(
      value =
          "SELECT * FROM la_images WHERE project_id = :projectId ORDER BY id LIMIT :limit OFFSET :offset",
      nativeQuery = true)
  List<Image> findByProjectIdOrderById(
      @org.springframework.data.repository.query.Param("projectId") Long projectId,
      @org.springframework.data.repository.query.Param("offset") int offset,
      @org.springframework.data.repository.query.Param("limit") int limit);
}
