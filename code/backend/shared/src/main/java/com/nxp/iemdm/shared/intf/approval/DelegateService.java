package com.nxp.iemdm.shared.intf.approval;

import com.nxp.iemdm.model.request.approval.Delegate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.web.bind.annotation.RequestBody;

public interface DelegateService {
  Delegate saveDelegate(@RequestBody Delegate delegate);

  List<Delegate> getAllDelegates();

  Optional<String> findDelegateWbi(String approverWbi);

  /**
   * Gets all delegate objects for a given wbi of a user that is a delegate for another user
   *
   * @param delegateWbi wbi of the delegate for a user
   * @return
   */
  List<Delegate> getAllDelegatesByDelegateWbi(String delegateWbi);

  /**
   * Gets all delegate objects for a given wbi of a user that is currently an active delegate for
   * another user
   *
   * @param delegateWbi wbi of the current active delegate for a user
   * @return
   */
  List<Delegate> getAllActiveDelegatesByDelegateWbi(String delegateWbi);

  /**
   * Gets all active delegatess as a set
   *
   * @return
   */
  Set<Delegate> getAllActiveDelegates();
}
