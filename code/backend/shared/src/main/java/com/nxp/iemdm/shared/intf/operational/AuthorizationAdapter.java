package com.nxp.iemdm.shared.intf.operational;

public interface AuthorizationAdapter {
  void restrictToReadOnlyAccess();

  void removeReadOnlyRestriction();
}
