package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.model.massupload.MassUploadType;
import java.util.Collection;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface MassUploadTemplateGenerator {
  byte[] generateMassUploadTemplate(MassUploadType massUploadType);

  <T> byte[] generateMassUploadTemplate(MassUploadType massUploadType, Collection<T> allData);

  <T> XSSFWorkbook generateMassUploadTemplateWorkbook(
      MassUploadType massUploadType, Collection<T> allData);

  XSSFWorkbook generateMassUploadTemplateWorkbook(MassUploadType massUploadType);
}
