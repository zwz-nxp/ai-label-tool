package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.location.Location;
import com.nxp.iemdm.model.location.SapCode;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SapCodeRepository extends CrudRepository<SapCode, String> {
  List<SapCode> findAllByManagedBy(Location location);
}
