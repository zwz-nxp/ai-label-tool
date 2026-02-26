package com.nxp.iemdm.shared.utility;

import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.model.massupload.MassUploadType;
import lombok.extern.java.Log;

@Log
public class MassUploadUtility {

  private MassUploadUtility() {}

  public static MassUploadType getMassUploadType(String massUploadType) {
    for (var type : MassUploadType.values()) {
      if (type.massUploadTypeName.equals(massUploadType)) {
        return type;
      }
    }
    log.severe(
        String.format(
            "Could not find the enum based on the mass upload type string: '%s'", massUploadType));
    throw new BadRequestException(
        "Mass upload type was not found. Please report an incident in ServiceNow");
  }
}
