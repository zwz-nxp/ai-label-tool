package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.model.scheduling.JobOverview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobOverviewRepository extends JpaRepository<JobOverview, Integer> {
  @Query(
      "FROM JobOverview WHERE jobName = :jobName AND triggerName = :triggerName ORDER BY id DESC")
  List<JobOverview> findAllByJobNameAndTriggerNameOrderByIdDesc(
      @Param("jobName") String jobName, @Param("triggerName") String triggerName);

  @Query("FROM JobOverview WHERE jobName = :jobName AND triggerName is null ORDER BY id DESC")
  List<JobOverview> findAllByJobNameOrderByIdDesc(@Param("jobName") String jobName);
}
