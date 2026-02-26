package com.nxp.iemdm.shared.intf.massupload;

import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.model.massupload.MassUploadType;
import org.springframework.web.multipart.MultipartFile;

public interface MassUploadHandler {
  MassUploadResponse verifyMassUpload(
      MultipartFile file, String wbi, MassUploadType massUploadType);

  MassUploadResponse processMassUpload(
      MultipartFile file, String wbi, boolean sendEmail, MassUploadType massUploadType);

  byte[] getResultsAsFile(
      MultipartFile file, MassUploadResponse response, MassUploadType massUploadType);
}
