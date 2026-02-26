package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.model.massupload.MassUploadExtract;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import java.util.Collection;

public interface Transformer {
  Collection<MassUploadExtract> transform(
      Collection<MassUploadExtract> entities,
      MassUploadResponse massUploadResponse,
      MassUploadConfiguration configuration);
}
