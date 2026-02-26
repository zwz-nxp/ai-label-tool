package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ImagePredictionLabel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImagePredictionLabelRepository extends JpaRepository<ImagePredictionLabel, Long> {

  @Transactional(readOnly = true)
  List<ImagePredictionLabel> findByImage_Id(Long imageId);

  @Transactional(readOnly = true)
  List<ImagePredictionLabel> findByImage_IdAndModel_Id(Long imageId, Long modelId);

  @Transactional(readOnly = true)
  List<ImagePredictionLabel> findByModelId(Long modelId);

  @Transactional
  void deleteByImage_Id(Long imageId);

  @Transactional
  void deleteByModelId(Long modelId);

  /** Find distinct image IDs that have prediction labels with specific class IDs. */
  @Transactional(readOnly = true)
  @Query(
      "SELECT DISTINCT ipl.image.id FROM ImagePredictionLabel ipl WHERE ipl.image.project.id = :projectId AND ipl.projectClass.id IN :classIds")
  List<Long> findImageIdsByPredictionClassIds(
      @Param("projectId") Long projectId, @Param("classIds") List<Long> classIds);

  /** Find all distinct image IDs that have any prediction labels for a project. */
  @Transactional(readOnly = true)
  @Query(
      "SELECT DISTINCT ipl.image.id FROM ImagePredictionLabel ipl WHERE ipl.image.project.id = :projectId")
  List<Long> findLabeledImageIds(@Param("projectId") Long projectId);

  /** Find all distinct image IDs that have prediction labels for a specific model. */
  @Transactional(readOnly = true)
  @Query(
      "SELECT DISTINCT ipl.image.id FROM ImagePredictionLabel ipl WHERE ipl.image.project.id = :projectId AND ipl.model.id = :modelId")
  List<Long> findImageIdsWithPredictionLabelsForModel(
      @Param("projectId") Long projectId, @Param("modelId") Long modelId);

  /**
   * Find distinct image IDs that have prediction labels with specific class IDs for a specific
   * model.
   */
  @Transactional(readOnly = true)
  @Query(
      "SELECT DISTINCT ipl.image.id FROM ImagePredictionLabel ipl WHERE ipl.image.project.id = :projectId AND ipl.model.id = :modelId AND ipl.projectClass.id IN :classIds")
  List<Long> findImageIdsByPredictionClassIdsAndModelId(
      @Param("projectId") Long projectId,
      @Param("modelId") Long modelId,
      @Param("classIds") List<Long> classIds);
}
