package com.nxp.iemdm.operational.repository.jpa;

import com.nxp.iemdm.model.request.approval.Delegate;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DelegateRepository extends JpaRepository<Delegate, Integer> {

  @Transactional
  @Query("From Delegate WHERE ?1 BETWEEN startDate AND endDate")
  Set<Delegate> getAllActiveDelegates(LocalDate localDate);

  @Transactional
  @Query("From Delegate WHERE approverWbi = ?1")
  Optional<Delegate> findByApproverWbi(String approverWbi);

  @Transactional
  @Query("From Delegate WHERE approverWbi = ?1 AND ?2 BETWEEN startDate AND endDate")
  Optional<Delegate> findByActiveApproverWbi(String approverWbi, LocalDate currentDate);

  @Transactional
  @Query("From Delegate WHERE delegateWbi = ?1")
  List<Delegate> findAllByDelegateWbi(String delegateWbi);

  @Transactional
  @Query("From Delegate WHERE delegateWbi = ?1 AND ?2 BETWEEN startDate AND endDate")
  List<Delegate> findAllByActiveDelegateWbi(String delegateWbi, LocalDate currentDate);
}
