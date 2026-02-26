package com.nxp.iemdm.shared.dto.landingai;

/** DTO for image upload results. Contains upload status and metadata for each uploaded image. */
public class ImageUploadResponse {

  private Long id;
  private String fileName;
  private Long fileSize;
  private Integer width;
  private Integer height;
  private boolean success;
  private String errorMessage;

  public ImageUploadResponse() {}

  public ImageUploadResponse(
      Long id,
      String fileName,
      Long fileSize,
      Integer width,
      Integer height,
      boolean success,
      String errorMessage) {
    this.id = id;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.width = width;
    this.height = height;
    this.success = success;
    this.errorMessage = errorMessage;
  }

  // Success constructor
  public ImageUploadResponse(
      Long id, String fileName, Long fileSize, Integer width, Integer height) {
    this.id = id;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.width = width;
    this.height = height;
    this.success = true;
    this.errorMessage = null;
  }

  // Failure constructor
  public ImageUploadResponse(String fileName, String errorMessage) {
    this.fileName = fileName;
    this.success = false;
    this.errorMessage = errorMessage;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public Integer getWidth() {
    return width;
  }

  public void setWidth(Integer width) {
    this.width = width;
  }

  public Integer getHeight() {
    return height;
  }

  public void setHeight(Integer height) {
    this.height = height;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
