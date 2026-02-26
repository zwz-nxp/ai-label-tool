package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ImageLabel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageLabelRepository extends JpaRepository<ImageLabel, Long> {

  @Transactional(readOnly = true)
  List<ImageLabel> findByImageId(Long imageId);

  @Transactional(readOnly = true)
  List<ImageLabel> findByImage_Id(Long imageId);

  @Transactional(readOnly = true)
  List<ImageLabel> findByProjectClassId(Long classId);

  @Transactional(readOnly = true)
  List<ImageLabel> findByImage_IdIn(List<Long> imageIds);

  /**
   * Find all labels where the image ID is in the provided list. Uses explicit query to ensure
   * correct image_id column matching.
   */
  @Transactional(readOnly = true)
  @Query("SELECT il FROM ImageLabel il WHERE il.image.id IN :imageIds")
  List<ImageLabel> findByImageIds(@Param("imageIds") List<Long> imageIds);

  /**
   * Delete all labels for the specified image IDs. This is more efficient than loading entities and
   * calling deleteAll() as it executes a single DELETE statement.
   *
   * @param imageIds list of image IDs
   * @return number of labels deleted
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM ImageLabel il WHERE il.image.id IN :imageIds")
  int deleteByImageIds(@Param("imageIds") List<Long> imageIds);

  @Transactional(readOnly = true)
  long countByImage_Id(Long imageId);

  @Transactional(readOnly = true)
  long countByImage_Project_Id(Long projectId);

  @Transactional(readOnly = true)
  long countByProjectClassId(Long projectClassId);

  @Modifying
  @Transactional
  void deleteByImage_Id(Long imageId);

  /**
   * Count distinct images that have labels for a project (excluding images with split assigned).
   */
  @Transactional(readOnly = true)
  @Query(
      "SELECT COUNT(DISTINCT il.image.id) FROM ImageLabel il WHERE il.image.project.id = :projectId AND (il.image.split IS NULL OR il.image.split = '')")
  long countDistinctImagesWithLabelsNoSplit(@Param("projectId") Long projectId);

  /** Count distinct images that have labels for a project (including all images). */
  @Transactional(readOnly = true)
  @Query(
      "SELECT COUNT(DISTINCT il.image.id) FROM ImageLabel il WHERE il.image.project.id = :projectId")
  long countDistinctImagesWithLabels(@Param("projectId") Long projectId);

  /**
   * Count distinct images that have labels for a specific class (excluding images with split
   * assigned).
   */
  @Transactional(readOnly = true)
  @Query(
      "SELECT COUNT(DISTINCT il.image.id) FROM ImageLabel il WHERE il.image.project.id = :projectId AND il.projectClass.id = :classId AND (il.image.split IS NULL OR il.image.split = '')")
  long countDistinctImagesByClassNoSplit(
      @Param("projectId") Long projectId, @Param("classId") Long classId);

  /** Count distinct images that have labels for a specific class (including all images). */
  @Transactional(readOnly = true)
  @Query(
      "SELECT COUNT(DISTINCT il.image.id) FROM ImageLabel il WHERE il.image.project.id = :projectId AND il.projectClass.id = :classId")
  long countDistinctImagesByClass(
      @Param("projectId") Long projectId, @Param("classId") Long classId);

  /** Find distinct image IDs that have ground truth labels with specific class IDs. */
  @Transactional(readOnly = true)
  @Query(
      "SELECT DISTINCT il.image.id FROM ImageLabel il WHERE il.image.project.id = :projectId AND il.projectClass.id IN :classIds")
  List<Long> findImageIdsByGroundTruthClassIds(
      @Param("projectId") Long projectId, @Param("classIds") List<Long> classIds);

  /** Find all distinct image IDs that have any ground truth labels for a project. */
  @Transactional(readOnly = true)
  @Query("SELECT DISTINCT il.image.id FROM ImageLabel il WHERE il.image.project.id = :projectId")
  List<Long> findLabeledImageIds(@Param("projectId") Long projectId);

  /** Find distinct image IDs that have labels created by a specific user (partial match). */
  @Transactional(readOnly = true)
  @Query(
      "SELECT DISTINCT il.image.id FROM ImageLabel il WHERE il.image.project.id = :projectId AND LOWER(il.createdBy) LIKE LOWER(CONCAT('%', :labeler, '%'))")
  List<Long> findImageIdsByLabeler(
      @Param("projectId") Long projectId, @Param("labeler") String labeler);

  /**
   * Count images grouped by class and split for a project. Returns rows of [classId, split, count].
   * Only counts Ground Truth labels for labeled images (isLabeled = true).
   */
  @Transactional(readOnly = true)
  @Query(
      "SELECT il.projectClass.id, il.image.split, COUNT(DISTINCT il.image.id) "
          + "FROM ImageLabel il "
          + "WHERE il.image.project.id = :projectId "
          + "AND il.image.isLabeled = true "
          + "GROUP BY il.projectClass.id, il.image.split")
  List<Object[]> countSplitsByClassForProject(@Param("projectId") Long projectId);

  /**
   * Delete all labels for images belonging to a specific project.
   *
   * @param projectId the project ID
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM ImageLabel il WHERE il.image.project.id = :projectId")
  void deleteByImageProjectId(@Param("projectId") Long projectId);
}
