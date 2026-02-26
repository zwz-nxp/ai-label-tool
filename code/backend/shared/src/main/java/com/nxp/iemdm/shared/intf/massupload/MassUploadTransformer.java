package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.model.massupload.MassUploadExtract;
import java.util.Collection;

public interface MassUploadTransformer {

  Collection<MassUploadExtract> transform(Collection<MassUploadExtract> entities);
}
