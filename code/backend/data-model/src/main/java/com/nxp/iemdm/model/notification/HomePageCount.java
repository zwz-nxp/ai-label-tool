package com.nxp.iemdm.model.notification;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

// POJO to inform front-end about several counts
@Data
public class HomePageCount implements Serializable {

  @Serial private static final long serialVersionUID = 407139233506086752L;

  private int unreadNotificationCount;
  private int requestCount;
  private int approvalCount;
  private int maxNotifications;
}
