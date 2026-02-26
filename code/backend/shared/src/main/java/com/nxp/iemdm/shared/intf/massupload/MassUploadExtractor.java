package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.model.massupload.MassUploadExtract;
import java.util.Collection;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public interface MassUploadExtractor {

  Collection<MassUploadExtract> extract(XSSFSheet sheet);
}
