package com.nxp.iemdm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.exception.MassUploadInvalidExcelFileException;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.model.massupload.MassUploadType;
import com.nxp.iemdm.shared.IemdmConstants;
import com.nxp.iemdm.shared.intf.massupload.MassUploadHandler;
import com.nxp.iemdm.shared.intf.massupload.MassUploadTemplateGenerator;
import com.nxp.iemdm.shared.utility.MassUploadUtility;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/massUpload")
public class MassUploadController {

  @Value("${app.environment}")
  private String environment;

  private final MassUploadHandler massUploadHandler;
  private final MassUploadTemplateGenerator massUploadTemplateGenerator;

  public MassUploadController(
      MassUploadHandler massUploadHandler,
      MassUploadTemplateGenerator massUploadTemplateGenerator) {
    this.massUploadHandler = massUploadHandler;
    this.massUploadTemplateGenerator = massUploadTemplateGenerator;
  }

  @GetMapping("/template/{massUploadType}")
  public ResponseEntity<ByteArrayResource> generateTemplate(
      @PathVariable @NotNull String massUploadType) {

    var type = MassUploadUtility.getMassUploadType(massUploadType);

    String filename =
        String.format("IE-MDM_%s_MU_%s.xlsx", this.environment, type.readMeFileName.toUpperCase());

    String headerValues = String.format("attachment; filename=%s", filename);

    var byteArrayResource =
        new ByteArrayResource(this.massUploadTemplateGenerator.generateMassUploadTemplate(type));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
        .contentLength(byteArrayResource.getByteArray().length)
        .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
        .body(byteArrayResource);
  }

  @PostMapping("/verify/{massUploadType}")
  public MassUploadResponse verify(
      @RequestParam MultipartFile file,
      @PathVariable String massUploadType,
      @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi) {
    var type = MassUploadUtility.getMassUploadType(massUploadType);

    return this.massUploadHandler.verifyMassUpload(file, wbi, type);
  }

  @PostMapping("/load/{massUploadType}")
  public MassUploadResponse load(
      @PathVariable String massUploadType,
      @RequestParam MultipartFile file,
      @RequestParam boolean sendEmail,
      @RequestHeader(IemdmConstants.USER_WBI_HEADER) String wbi)
      throws MassUploadInvalidExcelFileException {

    var type = MassUploadUtility.getMassUploadType(massUploadType);

    return this.massUploadHandler.processMassUpload(file, wbi, sendEmail, type);
  }

  @PostMapping("/results/{massUploadType}")
  public ResponseEntity<ByteArrayResource> downloadResults(
      @PathVariable String massUploadType,
      @RequestParam MultipartFile file,
      @RequestParam String responseString)
      throws MassUploadInvalidExcelFileException {
    MassUploadType type = MassUploadUtility.getMassUploadType(massUploadType);

    ObjectMapper objectMapper = new ObjectMapper();
    MassUploadResponse response;
    try {
      response = objectMapper.readValue(responseString, MassUploadResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    ByteArrayResource byteArrayResource =
        new ByteArrayResource(this.massUploadHandler.getResultsAsFile(file, response, type));

    String filename =
        String.format(
            "IE-MDM_%s_MU_%s_RESULTS.xlsx", this.environment, type.readMeFileName.toUpperCase());

    String headerValues = String.format("attachment; filename=%s", filename);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
        .contentLength(byteArrayResource.getByteArray().length)
        .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
        .body(byteArrayResource);
  }
}
