package com.nxp.iemdm.mdminterface.service;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TriggerService {
  protected static final Logger LOGGER = LoggerFactory.getLogger(TriggerService.class.getName());

  private final Executor interfaceTaskExecutor;

  public TriggerService(Executor interfaceTaskExecutor) {
    this.interfaceTaskExecutor = interfaceTaskExecutor;
  }

  public boolean enqueueJob(String jobType, String triggeredByWbi) {

    boolean isEnqueued = false;
    if (isEnqueued) {}
    return isEnqueued;
  }

  private void runJob(String jobType) {}
}
