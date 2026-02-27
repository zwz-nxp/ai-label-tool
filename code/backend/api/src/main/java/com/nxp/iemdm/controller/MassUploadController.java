package com.nxp.iemdm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.model.massupload.MassUploadType;
import com.nxp.iemdm.shared.aop.annotations.MethodLog;
import com.nxp.iemdm.shared.utility.MassUploadUtility;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Log
@RestController
@RequestMapping("/api/massUpload")
public class MassUploadController {

  private final RestTemplate restTemplate;
  private final String servicesUri;

  public MassUploadController(
      RestTemplate restTemplate, @Value("${rest.iemdm-services.uri}") String servicesUri) {
    this.restTemplate = restTemplate;
    this.servicesUri = servicesUri;
  }

  @GetMapping("/template/{massUploadType}")
  public ResponseEntity<ByteArrayResource> generateTemplate(
      @PathVariable @NotNull String massUploadType) {

    var templateAsBytes =
        this.restTemplate
            .getForEntity(
                this.servicesUri + "massUpload/template/" + massUploadType, ByteArrayResource.class)
            .getBody();

    assert templateAsBytes != null;
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format("attachment; filename=%s", templateAsBytes.getFilename()))
        .contentLength(templateAsBytes.getByteArray().length)
        .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
        .body(templateAsBytes);
  }

  @PostMapping("/verify/{massUploadType}")
  public MassUploadResponse verify(
      @RequestParam MultipartFile file, @PathVariable String massUploadType) {

    MassUploadType type = MassUploadUtility.getMassUploadType(massUploadType);
    String url = this.servicesUri + "massUpload/verify/" + massUploadType;
    ResponseEntity<MassUploadResponse> response;
    byte[] fileContent;

    try {
      fileContent = file.getBytes();
    } catch (IOException ioException) {
      response = new ResponseEntity<>(new MassUploadResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
      log.log(
          Level.WARNING, String.format("Error processing mass upload for %s", type), ioException);
      return response.getBody();
    }

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
        this.createRequestEntity(file, fileContent);
    response = this.restTemplate.postForEntity(url, requestEntity, MassUploadResponse.class);

    return response.getBody();
  }

  @PostMapping("/load/{massUploadType}")
  public MassUploadResponse load(
      @RequestParam MultipartFile file,
      @RequestParam boolean sendEmail,
      @PathVariable String massUploadType) {

    var type = MassUploadUtility.getMassUploadType(massUploadType);
    String url = this.servicesUri + "massUpload/load/" + massUploadType;
    ResponseEntity<MassUploadResponse> response;
    byte[] fileContent;

    try {
      fileContent = file.getBytes();
    } catch (IOException ioException) {
      response = new ResponseEntity<>(new MassUploadResponse(), HttpStatus.INTERNAL_SERVER_ERROR);
      log.log(
          Level.WARNING, String.format("Error processing mass upload for %s", type), ioException);
      return response.getBody();
    }

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
        this.createRequestEntity(file, sendEmail, fileContent);
    response = this.restTemplate.postForEntity(url, requestEntity, MassUploadResponse.class);

    return response.getBody();
  }

  @PostMapping("/results/{massUploadType}")
  public ResponseEntity<ByteArrayResource> downloadResult(
      @RequestParam MultipartFile file,
      @RequestParam String responseString,
      @PathVariable String massUploadType) {
    ObjectMapper objectMapper = new ObjectMapper();
    MassUploadResponse response;
    try {
      response = objectMapper.readValue(responseString, MassUploadResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    String url = this.servicesUri + "massUpload/results/" + massUploadType;
    byte[] fileContent;

    try {
      fileContent = file.getBytes();
    } catch (IOException e) {
      log.log(Level.WARNING, "Error downloading mass upload results", e);
      return new ResponseEntity<>(
          new ByteArrayResource(new byte[0]), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    ContentDisposition contentDisposition =
        ContentDisposition.builder("form-data").name("file").filename(file.getName()).build();

    MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

    HttpEntity<byte[]> fileEntity = new HttpEntity<>(fileContent, fileMap);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", fileEntity);
    body.add("responseString", response);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ByteArrayResource resultAsBytes =
        this.restTemplate.postForEntity(url, requestEntity, ByteArrayResource.class).getBody();

    assert resultAsBytes != null;
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            String.format("attachment; filename=%s", resultAsBytes.getFilename()))
        .contentLength(resultAsBytes.getByteArray().length)
        .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
        .body(resultAsBytes);
  }

  @MethodLog
  @GetMapping("/metaData")
  public MassUploadType[] massUploadMetaData() {
    return MassUploadType.values();
  }

  private HttpEntity<MultiValueMap<String, Object>> createRequestEntity(
      MultipartFile file, byte[] fileContent) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    ContentDisposition contentDisposition =
        ContentDisposition.builder("form-data").name("file").filename(file.getName()).build();

    MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

    HttpEntity<byte[]> fileEntity = new HttpEntity<>(fileContent, fileMap);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", fileEntity);

    return new HttpEntity<>(body, headers);
  }

  private HttpEntity<MultiValueMap<String, Object>> createRequestEntity(
      MultipartFile file, boolean sendEmail, byte[] fileContent) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    ContentDisposition contentDisposition =
        ContentDisposition.builder("form-data").name("file").filename(file.getName()).build();

    MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

    HttpEntity<byte[]> fileEntity = new HttpEntity<>(fileContent, fileMap);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", fileEntity);
    body.add("sendEmail", sendEmail);

    return new HttpEntity<>(body, headers);
  }
}
