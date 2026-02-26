package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.exception.MassUploadInvalidExcelSheetException;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public interface MassUploadVerifier {

  boolean verify(XSSFSheet sheet) throws MassUploadInvalidExcelSheetException;
}
