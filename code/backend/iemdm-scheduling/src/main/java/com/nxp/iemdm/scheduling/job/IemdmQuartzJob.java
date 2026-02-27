package com.nxp.iemdm.scheduling.job;

import org.quartz.Job;

public interface IemdmQuartzJob extends Job {

  /**
   * Returns an initial description of the job. This description is to the discretion of the
   * developer, but can be overwritten by users in the application.
   *
   * @return initial description of the job
   */
  String getDescription();

  default boolean isDisplayed() {
    return true;
  }
}
