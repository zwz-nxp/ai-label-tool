package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.Model;
import com.nxp.iemdm.shared.dto.landingai.ModelWithMetricsDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

  /**
   * Find all models for a specific project ordered by creation date descending
   *
   * @param projectId the project ID
   * @return list of models
   */
  List<Model> findByProjectIdOrderByCreatedAtDesc(Long projectId);

  /** Query models by project ID */
  List<Model> findByProjectId(Long projectId);

  /** Query model by track ID */
  Model findByTrackId(String trackId);

  /** Query all favorite models */
  List<Model> findByIsFavoriteTrue();

  /** Search models by model alias or creator */
  @Query(
      "SELECT m FROM Model m WHERE "
          + "LOWER(m.modelAlias) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(m.createdBy) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<Model> findByModelAliasContainingIgnoreCaseOrCreatedByContainingIgnoreCase(
      @Param("query") String modelAlias, @Param("query") String createdBy);

  /** Search favorite models by model alias or creator */
  @Query(
      "SELECT m FROM Model m WHERE m.isFavorite = true AND ("
          + "LOWER(m.modelAlias) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(m.createdBy) LIKE LOWER(CONCAT('%', :query, '%')))")
  List<Model> findByIsFavoriteTrueAndModelAliasContainingIgnoreCaseOrCreatedByContainingIgnoreCase(
      @Param("query") String modelAlias, @Param("query") String createdBy);

  // ============================================================================
  // Helper methods: Convert Native SQL query results to DTO
  // ============================================================================

  /** Convert Object[] to ModelWithMetricsDto */
  default ModelWithMetricsDto convertToDto(Object[] row) {
    // Safely convert Boolean
    Boolean isFavorite = false;
    if (row[7] != null) {
      if (row[7] instanceof Boolean) {
        isFavorite = (Boolean) row[7];
      } else if (row[7] instanceof Number) {
        isFavorite = ((Number) row[7]).intValue() != 0;
      } else if (row[7] instanceof String) {
        isFavorite =
            "true".equalsIgnoreCase((String) row[7])
                || "t".equalsIgnoreCase((String) row[7])
                || "1".equals(row[7]);
      }
    }

    // Safely convert createdAt
    java.time.Instant createdAt;
    if (row[8] instanceof java.sql.Timestamp) {
      createdAt = ((java.sql.Timestamp) row[8]).toInstant();
    } else if (row[8] instanceof java.time.Instant) {
      createdAt = (java.time.Instant) row[8];
    } else {
      createdAt = java.time.Instant.now();
    }

    return new ModelWithMetricsDto(
        ((Number) row[0]).longValue(), // id
        ((Number) row[1]).longValue(), // project_id
        row[2] != null ? ((Number) row[2]).longValue() : null, // training_record_id
        (String) row[3], // model_alias
        (String) row[4], // model_version
        (String) row[5], // track_id
        (String) row[6], // created_by
        isFavorite, // is_favorite
        createdAt, // created_at
        row[9] != null ? ((Number) row[9]).intValue() : null, // training_correct_rate
        row[10] != null ? ((Number) row[10]).intValue() : null, // dev_correct_rate
        row[11] != null ? ((Number) row[11]).intValue() : null, // test_correct_rate
        row[12] != null ? ((Number) row[12]).intValue() : null, // confidence_threshold
        row[13] != null ? ((Number) row[13]).doubleValue() : null, // training_f1_rate
        row[14] != null ? ((Number) row[14]).doubleValue() : null, // training_precision_rate
        row[15] != null ? ((Number) row[15]).doubleValue() : null, // training_recall_rate
        row[16] != null ? ((Number) row[16]).doubleValue() : null, // dev_f1_rate
        row[17] != null ? ((Number) row[17]).doubleValue() : null, // dev_precision_rate
        row[18] != null ? ((Number) row[18]).doubleValue() : null, // dev_recall_rate
        row[19] != null ? ((Number) row[19]).doubleValue() : null, // test_f1_rate
        row[20] != null ? ((Number) row[20]).doubleValue() : null, // test_precision_rate
        row[21] != null ? ((Number) row[21]).doubleValue() : null, // test_recall_rate
        row[22] != null ? ((Number) row[22]).intValue() : null, // image_count
        row[23] != null ? ((Number) row[23]).intValue() : null // label_count
        );
  }

  // ============================================================================
  // New: Query Model with JOIN ConfidentialReport, return DTO
  // ============================================================================

  /** Query all models with evaluation metrics (using Native SQL to avoid cache issues) */
  @Query(
      value =
          "SELECT "
              + "m.id, m.project_id, m.training_record_id, m.model_alias, m.model_version, m.track_id, "
              + "m.created_by, m.is_favorite, m.created_at, "
              + "cr.training_correct_rate, cr.dev_correct_rate, cr.test_correct_rate, cr.confidence_threshold, "
              + "m.training_f1_rate, m.training_precision_rate, m.training_recall_rate, "
              + "m.dev_f1_rate, m.dev_precision_rate, m.dev_recall_rate, "
              + "m.test_f1_rate, m.test_precision_rate, m.test_recall_rate, "
              + "m.image_count, m.label_count "
              + "FROM la_model m "
              + "LEFT JOIN la_confidential_report cr ON m.id = cr.model_id "
              + "WHERE m.status != 'INACTIVE' "
              + "ORDER BY m.created_at DESC",
      nativeQuery = true)
  List<Object[]> findAllModelsWithMetricsNative();

  /** Query all models with evaluation metrics (return DTO list) */
  default List<ModelWithMetricsDto> findAllModelsWithMetrics() {
    List<Object[]> results = findAllModelsWithMetricsNative();
    if (results == null || results.isEmpty()) {
      return List.of();
    }
    return results.stream().map(this::convertToDto).toList();
  }

  /** Query models with evaluation metrics by project ID (using Native SQL) */
  @Query(
      value =
          "SELECT "
              + "m.id, m.project_id, m.training_record_id, m.model_alias, m.model_version, m.track_id, "
              + "m.created_by, m.is_favorite, m.created_at, "
              + "cr.training_correct_rate, cr.dev_correct_rate, cr.test_correct_rate, cr.confidence_threshold, "
              + "m.training_f1_rate, m.training_precision_rate, m.training_recall_rate, "
              + "m.dev_f1_rate, m.dev_precision_rate, m.dev_recall_rate, "
              + "m.test_f1_rate, m.test_precision_rate, m.test_recall_rate, "
              + "m.image_count, m.label_count "
              + "FROM la_model m "
              + "LEFT JOIN la_confidential_report cr ON m.id = cr.model_id "
              + "WHERE m.project_id = :projectId AND m.status != 'INACTIVE' "
              + "ORDER BY m.created_at DESC",
      nativeQuery = true)
  List<Object[]> findModelsWithMetricsByProjectIdNative(@Param("projectId") Long projectId);

  /** Query models with evaluation metrics by project ID (return DTO list) */
  default List<ModelWithMetricsDto> findModelsWithMetricsByProjectId(Long projectId) {
    List<Object[]> results = findModelsWithMetricsByProjectIdNative(projectId);
    if (results == null || results.isEmpty()) {
      return List.of();
    }
    return results.stream().map(this::convertToDto).toList();
  }

  /** Search models with evaluation metrics by model alias or creator (using Native SQL) */
  @Query(
      value =
          "SELECT "
              + "m.id, m.project_id, m.training_record_id, m.model_alias, m.model_version, m.track_id, "
              + "m.created_by, m.is_favorite, m.created_at, "
              + "cr.training_correct_rate, cr.dev_correct_rate, cr.test_correct_rate, cr.confidence_threshold, "
              + "m.training_f1_rate, m.training_precision_rate, m.training_recall_rate, "
              + "m.dev_f1_rate, m.dev_precision_rate, m.dev_recall_rate, "
              + "m.test_f1_rate, m.test_precision_rate, m.test_recall_rate, "
              + "m.image_count, m.label_count "
              + "FROM la_model m "
              + "LEFT JOIN la_confidential_report cr ON m.id = cr.model_id "
              + "WHERE m.status != 'INACTIVE' "
              + "AND (LOWER(m.model_alias) LIKE LOWER(CONCAT('%', :query, '%')) "
              + "OR LOWER(m.created_by) LIKE LOWER(CONCAT('%', :query, '%'))) "
              + "ORDER BY m.created_at DESC",
      nativeQuery = true)
  List<Object[]> searchModelsWithMetricsNative(@Param("query") String query);

  /** Search models with evaluation metrics (return DTO list) */
  default List<ModelWithMetricsDto> searchModelsWithMetrics(String query) {
    List<Object[]> results = searchModelsWithMetricsNative(query);
    if (results == null || results.isEmpty()) {
      return List.of();
    }
    return results.stream().map(this::convertToDto).toList();
  }

  /** Search favorite models with evaluation metrics (using Native SQL) */
  @Query(
      value =
          "SELECT "
              + "m.id, m.project_id, m.training_record_id, m.model_alias, m.model_version, m.track_id, "
              + "m.created_by, m.is_favorite, m.created_at, "
              + "cr.training_correct_rate, cr.dev_correct_rate, cr.test_correct_rate, cr.confidence_threshold, "
              + "m.training_f1_rate, m.training_precision_rate, m.training_recall_rate, "
              + "m.dev_f1_rate, m.dev_precision_rate, m.dev_recall_rate, "
              + "m.test_f1_rate, m.test_precision_rate, m.test_recall_rate, "
              + "m.image_count, m.label_count "
              + "FROM la_model m "
              + "LEFT JOIN la_confidential_report cr ON m.id = cr.model_id "
              + "WHERE m.is_favorite = true AND m.status != 'INACTIVE' "
              + "ORDER BY m.created_at DESC",
      nativeQuery = true)
  List<Object[]> findFavoriteModelsWithMetricsNative();

  /** Search favorite models with evaluation metrics (return DTO list) */
  default List<ModelWithMetricsDto> findFavoriteModelsWithMetrics() {
    List<Object[]> results = findFavoriteModelsWithMetricsNative();
    if (results == null || results.isEmpty()) {
      return List.of();
    }
    return results.stream().map(this::convertToDto).toList();
  }

  /** Search favorite models with evaluation metrics by model alias or creator (using Native SQL) */
  @Query(
      value =
          "SELECT "
              + "m.id, m.project_id, m.training_record_id, m.model_alias, m.model_version, m.track_id, "
              + "m.created_by, m.is_favorite, m.created_at, "
              + "cr.training_correct_rate, cr.dev_correct_rate, cr.test_correct_rate, cr.confidence_threshold, "
              + "m.training_f1_rate, m.training_precision_rate, m.training_recall_rate, "
              + "m.dev_f1_rate, m.dev_precision_rate, m.dev_recall_rate, "
              + "m.test_f1_rate, m.test_precision_rate, m.test_recall_rate, "
              + "m.image_count, m.label_count "
              + "FROM la_model m "
              + "LEFT JOIN la_confidential_report cr ON m.id = cr.model_id "
              + "WHERE m.is_favorite = true AND m.status != 'INACTIVE' "
              + "AND (LOWER(m.model_alias) LIKE LOWER(CONCAT('%', :query, '%')) "
              + "OR LOWER(m.created_by) LIKE LOWER(CONCAT('%', :query, '%'))) "
              + "ORDER BY m.created_at DESC",
      nativeQuery = true)
  List<Object[]> searchFavoriteModelsWithMetricsNative(@Param("query") String query);

  /** Search favorite models with evaluation metrics (return DTO list) */
  default List<ModelWithMetricsDto> searchFavoriteModelsWithMetrics(String query) {
    List<Object[]> results = searchFavoriteModelsWithMetricsNative(query);
    if (results == null || results.isEmpty()) {
      return List.of();
    }
    return results.stream().map(this::convertToDto).toList();
  }

  @Query(
      value =
          "SELECT "
              + "m.id, m.project_id, m.training_record_id, m.model_alias, m.model_version, m.track_id, "
              + "m.created_by, m.is_favorite, m.created_at, "
              + "cr.training_correct_rate, cr.dev_correct_rate, cr.test_correct_rate, cr.confidence_threshold, "
              + "m.training_f1_rate, m.training_precision_rate, m.training_recall_rate, "
              + "m.dev_f1_rate, m.dev_precision_rate, m.dev_recall_rate, "
              + "m.test_f1_rate, m.test_precision_rate, m.test_recall_rate, "
              + "m.image_count, m.label_count "
              + "FROM la_model m "
              + "LEFT JOIN la_confidential_report cr ON m.id = cr.model_id "
              + "WHERE m.id = :id "
              + "LIMIT 1",
      nativeQuery = true)
  List<Object[]> findModelWithMetricsByIdNative(@Param("id") Long id);

  /** Query single model with evaluation metrics by ID (return single result) */
  default ModelWithMetricsDto findModelWithMetricsById(Long id) {
    List<Object[]> results = findModelWithMetricsByIdNative(id);
    if (results == null || results.isEmpty()) {
      return null;
    }
    return convertToDto(results.get(0));
  }

  /** Update model favorite status (using Native SQL for direct update, explicit type conversion) */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      value = "UPDATE la_model SET is_favorite = CAST(:isFavorite AS boolean) WHERE id = :id",
      nativeQuery = true)
  void updateFavoriteStatus(@Param("id") Long id, @Param("isFavorite") Boolean isFavorite);

  /** Update model status (using Native SQL for direct update) */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "UPDATE la_model SET status = :status WHERE id = :id", nativeQuery = true)
  void updateStatus(@Param("id") Long id, @Param("status") String status);

  /**
   * Count models for a specific project.
   *
   * @param projectId the project ID
   * @return the count of models in the project
   */
  long countByProjectId(Long projectId);
}
