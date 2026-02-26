package com.nxp.iemdm.shared.repository.jpa;

import com.nxp.iemdm.model.user.Person;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PersonRepository extends CrudRepository<Person, String> {

  @Transactional(readOnly = true)
  Optional<Person> findByWbiIgnoreCase(String wbi);

  @Transactional(readOnly = true)
  Page<Person> findAll(Specification<Person> spec, Pageable pageable);

  @Transactional(readOnly = true)
  boolean existsByWbiIgnoreCase(String wbi);
}
