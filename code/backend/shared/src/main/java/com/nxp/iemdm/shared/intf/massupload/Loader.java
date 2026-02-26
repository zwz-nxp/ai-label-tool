package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.model.massupload.MassUploadExtract;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import java.util.Collection;

public interface Loader {
  boolean load(
      Collection<MassUploadExtract> entities, MassUploadResponse massUploadResponse, String wbi);
}
