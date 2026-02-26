package com.nxp.iemdm.shared.intf.controller;

public interface ErrorService {
  void handleException(Exception exception);

  void mailExceptions();
}
