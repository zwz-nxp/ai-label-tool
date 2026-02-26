package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.ValidationChart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationChartRepository extends JpaRepository<ValidationChart, Long> {

  /** Query validation chart data by model ID, ordered by created_at ascending */
  @Query("SELECT vc FROM ValidationChart vc WHERE vc.model.id = :modelId ORDER BY vc.createdAt ASC")
  List<ValidationChart> findByModelIdOrderByCreatedAtAsc(@Param("modelId") Long modelId);
}
