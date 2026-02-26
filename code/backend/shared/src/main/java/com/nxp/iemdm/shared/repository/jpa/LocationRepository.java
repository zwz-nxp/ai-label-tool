package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.enums.location.LocationStatus;
import com.nxp.iemdm.model.location.Location;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface LocationRepository extends JpaRepository<Location, Integer> {

  @Transactional(readOnly = true)
  List<Location> findAllByStatusOrderByAcronym(LocationStatus status);

  @Transactional(readOnly = true)
  List<Location> findAllByTmdbCodeAndStatus(String tmdbCode, LocationStatus status);

  /**
   * Finds all locations for the given SAP plant code.
   *
   * <p>As location ID and SAP plant code are used to identify a location/site in several places, it
   * would be nicer not to return a list. This could however give issues with existing data,
   * therefore it seems prudent to deal with the list for the time being.
   *
   * <p>A SAP manufacturer code and SAP vendor code (for sub-contractors) are introduced and could
   * be better suitable alternatives than the SAP plant code in specific contexts.<br>
   * At the time this code was added the existing systems did not yet have proper data for these
   * newer fields, and thus the SAP plant code is still used.
   *
   * @param sapCode SAP plant code
   */
  @Transactional(readOnly = true)
  List<Location> findAllBySapCode(String sapCode);

  @Transactional(readOnly = true)
  Optional<Location> findByAcronym(String acronym);

  @Override
  @Transactional(readOnly = true)
  List<Location> findAll();

  @Transactional
  Page<Location> findAll(@Nullable Specification<Location> spec, Pageable pageable);

  @Transactional
  @Query(value = "FROM Location WHERE id in :locationIds")
  Set<Location> findAllByIds(@Param("locationIds") Set<Integer> locationIds);

  @Transactional(readOnly = true)
  @Query(
      value = "FROM Location WHERE planningEngine in :planningEngines AND status = :locationStatus")
  List<Location> findAllByPlanningEnginesAndStatus(
      @Param("planningEngines") List<String> planningEngines,
      @Param("locationStatus") LocationStatus locationStatus);
}
