package com.nxp.iemdm.operational.repository.audit;

import com.nxp.iemdm.model.location.Location;
import org.springframework.data.repository.history.RevisionRepository;

public interface LocationAuditRepository extends RevisionRepository<Location, Integer, Integer> {}
