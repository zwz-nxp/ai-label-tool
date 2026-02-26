package com.nxp.iemdm.shared.intf.notification;

import lombok.Getter;

@Getter
public enum EmailTemplate {
  ACTION("action-mail-template"),
  NEW_EVENTS_NOTIFICATION("neweventsnotification"),
  VERSION("version-info"),
  WARNING("warning-mail-template"),
  DQM_ERROR("dqm-error"),
  APPROVAL_REQUEST("approval-request"),
  I2_CAPACITY_STATEMENT("i2capacitystatementtemplate_1"),
  I2_CAPACITYSTATEMENT_EXTENDEDFACTORY("i2capacitystatementtemplate_1_extendedfactory"),
  I2CAPACITYSTATEMENT_2("i2capacitystatementtemplate_2"),
  I2CAPACITYSTATEMENT_2_EXTENDEDFACTORY("i2capacitystatementtemplate_2_extendedfactory"),
  I2_CAPACITYSTATEMENT_FOOTER("i2capacitystatementtemplate_footer"),
  MAIL_TEMPLATE("mail-template"),
  WEEKLY_NOTIFICATION("weeklynotification"),
  USAGE_RATE_ISSUES("usageRateIssues"),
  USAGE_RATE_NEW_STAGES("usageRateNewArasStages");

  private final String name;

  EmailTemplate(String name) {
    this.name = name;
  }
}
