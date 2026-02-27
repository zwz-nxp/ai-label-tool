package com.nxp.iemdm.operational.service.rest;

import com.nxp.iemdm.model.request.approval.Delegate;
import com.nxp.iemdm.model.user.UserRole;
import com.nxp.iemdm.operational.repository.jpa.DelegateRepository;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.intf.approval.DelegateService;
import com.nxp.iemdm.shared.intf.operational.UserRoleService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delegate")
public class DelegateServiceImpl implements DelegateService {

  private final DelegateRepository delegateRepository;
  private final UserRoleService userRoleService;

  public DelegateServiceImpl(
      DelegateRepository delegateRepository, UserRoleService userRoleService) {
    this.delegateRepository = delegateRepository;
    this.userRoleService = userRoleService;
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/approver/{wbi}")
  public Delegate getDelegateForApprover(@PathVariable("wbi") String wbi) {
    Optional<Delegate> delegateOpt = delegateRepository.findByApproverWbi(wbi);
    return delegateOpt.orElse(this.getEmptyDelegate());
  }

  @MethodLog
  @Transactional
  @GetMapping(path = "/approver/active/{wbi}")
  public Delegate getActiveDelegateForApprover(@PathVariable("wbi") String wbi) {
    Optional<Delegate> delegateOpt =
        delegateRepository.findByActiveApproverWbi(wbi, LocalDate.now());
    return delegateOpt.orElse(getEmptyDelegate());
  }

  @MethodLog
  @Transactional
  @PostMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public Delegate saveDelegate(@RequestBody Delegate delegate) {
    return delegateRepository.save(delegate);
  }

  @MethodLog
  @DeleteMapping(path = "/{id}")
  public void deleteDelegate(@PathVariable("id") Integer id) {
    this.delegateRepository.deleteById(id);
  }

  @MethodLog
  @GetMapping(path = "/search")
  public Set<String> searchDelegates() {
    List<UserRole> userRoleList = this.userRoleService.findAll();
    return userRoleList.stream()
        .filter(this::isApprovalRole)
        .map(ur -> ur.getUser().getWbi())
        .collect(Collectors.toSet());
  }

  @Override
  public List<Delegate> getAllDelegates() {
    Iterable<Delegate> iterable = this.delegateRepository.findAll();
    return StreamSupport.stream(iterable.spliterator(), false).toList();
  }

  /**
   * Here we recursively look for the final delegate, because the first delegate may also have a
   * delegate etc. But we have to take care that we dont end up in a loop because both eventually a
   * delegate may point to the (input arg) approverWbi
   */
  @Override
  public Optional<String> findDelegateWbi(String approverWbi) {
    Optional<String> result = Optional.empty();

    Optional<Delegate> currentDelegate =
        this.delegateRepository.findByActiveApproverWbi(approverWbi, LocalDate.now());
    if (currentDelegate.isPresent()) {
      result =
          Optional.of(
              this.findDelegateRecursive(approverWbi, currentDelegate.get(), 5).getDelegateWbi());
    }

    return result;
  }

  public List<Delegate> getAllDelegatesByDelegateWbi(String delegateWbi) {
    return this.delegateRepository.findAllByDelegateWbi(delegateWbi);
  }

  public List<Delegate> getAllActiveDelegatesByDelegateWbi(String delegateWbi) {
    return this.delegateRepository.findAllByActiveDelegateWbi(delegateWbi, LocalDate.now());
  }

  @Override
  public Set<Delegate> getAllActiveDelegates() {
    return this.delegateRepository.getAllActiveDelegates(LocalDate.now());
  }

  // --------------------- private -----------------

  private Delegate getEmptyDelegate() {
    Delegate emptyDelegate = new Delegate();
    emptyDelegate.setId(0);
    return emptyDelegate;
  }

  private boolean isApprovalRole(UserRole userRole) {
    String roleId = userRole.getRole().getId().toLowerCase();
    return roleId.contains("approver") || roleId.contains("administrator");
  }

  private Delegate findDelegateRecursive(
      String approverWbi, Delegate previousDelegate, int recursionCount) {
    Optional<Delegate> currentDelegateOptional =
        this.delegateRepository.findByActiveApproverWbi(approverWbi, LocalDate.now());
    if (recursionCount > 0 && currentDelegateOptional.isPresent()) {
      Delegate currentDelegate = currentDelegateOptional.get();
      return this.findDelegateRecursive(
          currentDelegate.getApproverWbi(), currentDelegate, recursionCount - 1);
    }
    return previousDelegate;
  }
}
