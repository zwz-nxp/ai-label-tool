package com.nxp.iemdm.model.configuration.pojo;

import com.nxp.iemdm.enums.configuration.UpdateType;
import lombok.Data;

@Data
public class Update {
  public static final String ADDRESS = "/topic/update";

  private final UpdateType updatedType;
  private final Integer locationId;
  private final Object updateData;
  private final String userWbi; // used with USER_ALERT
}
