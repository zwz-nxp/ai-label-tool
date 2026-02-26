package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.model.logging.SysJobLog;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SysJobLogRepository extends JpaRepository<SysJobLog, Instant> {

  default List<SysJobLog> findAllByJobNameBeforeTimestampLimitToMaxResults(
      String jobName, Instant timestamp, int maxResults) {

    return this.findAllByJobNameAndTimestampLessThanOrderByTimestampDesc(
        jobName, timestamp, PageRequest.of(0, maxResults));
  }

  List<SysJobLog> findAllByJobNameAndTimestampLessThanOrderByTimestampDesc(
      String jobName, Instant timestamp, Pageable page);

  @Transactional(readOnly = true)
  @Query("SELECT DISTINCT trackingId FROM SysJobLog WHERE jobName LIKE ?1")
  Set<String> getDistinctTrackingIds(String jobName);
}
