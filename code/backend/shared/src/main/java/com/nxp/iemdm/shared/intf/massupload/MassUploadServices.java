package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.shared.repository.jpa.LocationRepository;

public interface MassUploadServices {
  LocationRepository getLocationRepository();
}
