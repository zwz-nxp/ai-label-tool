package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ModelParam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelParamRepository extends JpaRepository<ModelParam, Long> {

  /**
   * Find all model parameters for a specific location
   *
   * @param locationId the location ID
   * @return list of model parameters
   */
  List<ModelParam> findByLocation_IdOrderByCreatedAtDesc(Long locationId);

  /**
   * Find model parameters by location and model type
   *
   * @param locationId the location ID
   * @param modelType the model type (Object Detection, Classification, Segmentation)
   * @return list of model parameters
   */
  List<ModelParam> findByLocation_IdAndModelTypeOrderByCreatedAtDesc(
      Long locationId, String modelType);

  /**
   * Find a specific model parameter by location, model name, and model type
   *
   * @param locationId the location ID
   * @param modelName the model name
   * @param modelType the model type
   * @return optional model parameter
   */
  Optional<ModelParam> findByLocation_IdAndModelNameAndModelType(
      Long locationId, String modelName, String modelType);

  /**
   * Find all model parameters by model type
   *
   * @param modelType the model type
   * @return list of model parameters
   */
  List<ModelParam> findByModelTypeOrderByCreatedAtDesc(String modelType);

  /**
   * Check if a model parameter exists for a location and model name
   *
   * @param locationId the location ID
   * @param modelName the model name
   * @return true if exists, false otherwise
   */
  boolean existsByLocation_IdAndModelName(Long locationId, String modelName);

  /**
   * Delete all model parameters for a specific location
   *
   * @param locationId the location ID
   */
  void deleteByLocation_Id(Long locationId);

  /**
   * Count model parameters for a specific location
   *
   * @param locationId the location ID
   * @return the count of model parameters
   */
  long countByLocation_Id(Long locationId);

  /**
   * Find model parameters by model name (case-insensitive search)
   *
   * @param modelName the model name to search for
   * @return list of model parameters
   */
  @Query(
      "SELECT mp FROM ModelParam mp WHERE LOWER(mp.modelName) LIKE LOWER(CONCAT('%', :modelName, '%')) ORDER BY mp.createdAt DESC")
  List<ModelParam> searchByModelName(@Param("modelName") String modelName);
}
