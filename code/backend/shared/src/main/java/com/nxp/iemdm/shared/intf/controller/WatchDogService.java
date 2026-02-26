package com.nxp.iemdm.shared.intf.controller;

public interface WatchDogService {
  void verifyTibcoJobs();

  void verifyWeekNumberStartDate();

  void processAllPassedEvents();
}
