package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.configuration.ActivityLog;
import com.nxp.iemdm.model.location.Location;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ActivityLogRepository
    extends CrudRepository<ActivityLog, ActivityLog.ActivityLogKey> {
  @Transactional(readOnly = true)
  Optional<ActivityLog> findTopByActionAndLocationOrderByTimestampDesc(
      String action, Location location);
}
