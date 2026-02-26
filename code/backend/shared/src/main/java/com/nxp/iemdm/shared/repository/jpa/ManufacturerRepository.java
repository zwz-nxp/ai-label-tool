package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.model.location.Manufacturer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

public interface ManufacturerRepository extends CrudRepository<Manufacturer, String> {

  @Transactional
  Page<Manufacturer> findAll(@Nullable Specification<Manufacturer> spec, Pageable pageable);

  @Transactional(readOnly = true)
  Optional<Manufacturer> findByManufacturerCode(String manufacturerCode);

  @Transactional(readOnly = true)
  List<Manufacturer> findByManufacturerCodeIn(Collection<String> manufacturerCodes);

  @Transactional
  @Modifying
  @Query("DELETE FROM Manufacturer WHERE manufacturerCode = ?1")
  void deleteByManufacturerCode(String manufacturerCode);
}
