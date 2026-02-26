package com.nxp.iemdm.shared.repository.jpa.landingai;

import com.nxp.iemdm.model.landingai.LossChart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LossChartRepository extends JpaRepository<LossChart, Long> {

  /** Query loss chart data by model ID, ordered by created_at ascending */
  @Query("SELECT lc FROM LossChart lc WHERE lc.model.id = :modelId ORDER BY lc.createdAt ASC")
  List<LossChart> findByModelIdOrderByCreatedAtAsc(@Param("modelId") Long modelId);
}
