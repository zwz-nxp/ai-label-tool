package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ConfidentialReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfidentialReportRepository extends JpaRepository<ConfidentialReport, Long> {

  /**
   * Find confidential report by model ID
   *
   * @param modelId Model ID
   * @return Optional of ConfidentialReport
   */
  @Query("SELECT cr FROM ConfidentialReport cr WHERE cr.model.id = :modelId")
  Optional<ConfidentialReport> findByModelId(@Param("modelId") Long modelId);
}
