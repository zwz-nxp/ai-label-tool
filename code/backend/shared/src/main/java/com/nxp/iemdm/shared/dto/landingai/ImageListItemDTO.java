package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;
import java.util.List;

/**
 * DTO for image list items with display fields and label information. Used for displaying images in
 * the image upload page grid view with pagination support.
 */
public class ImageListItemDTO {

  private Long id;
  private String fileName;
  private Long fileSize;
  private Integer width;
  private Integer height;
  private String split;
  private Boolean isLabeled;
  private Boolean isNoClass;
  private Integer labelCount;
  private byte[] thumbnailImage;
  private Double thumbnailWidthRatio;
  private Double thumbnailHeightRatio;
  private Instant createdAt;
  private List<LabelOverlayDTO> labels;

  // For instances view: indicates which label this instance represents (null for images view)
  private Long instanceLabelId;
  // For instances view: the specific label being focused on
  private LabelOverlayDTO focusedLabel;

  public ImageListItemDTO() {}

  public ImageListItemDTO(
      Long id,
      String fileName,
      Long fileSize,
      Integer width,
      Integer height,
      String split,
      Boolean isLabeled,
      Integer labelCount,
      byte[] thumbnailImage,
      Instant createdAt,
      List<LabelOverlayDTO> labels) {
    this.id = id;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.width = width;
    this.height = height;
    this.split = split;
    this.isLabeled = isLabeled;
    this.labelCount = labelCount;
    this.thumbnailImage = thumbnailImage;
    this.createdAt = createdAt;
    this.labels = labels;
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

  public String getSplit() {
    return split;
  }

  public void setSplit(String split) {
    this.split = split;
  }

  public Boolean getIsLabeled() {
    return isLabeled;
  }

  public void setIsLabeled(Boolean isLabeled) {
    this.isLabeled = isLabeled;
  }

  public Boolean getIsNoClass() {
    return isNoClass;
  }

  public void setIsNoClass(Boolean isNoClass) {
    this.isNoClass = isNoClass;
  }

  public Integer getLabelCount() {
    return labelCount;
  }

  public void setLabelCount(Integer labelCount) {
    this.labelCount = labelCount;
  }

  public byte[] getThumbnailImage() {
    return thumbnailImage;
  }

  public void setThumbnailImage(byte[] thumbnailImage) {
    this.thumbnailImage = thumbnailImage;
  }

  public Double getThumbnailWidthRatio() {
    return thumbnailWidthRatio;
  }

  public void setThumbnailWidthRatio(Double thumbnailWidthRatio) {
    this.thumbnailWidthRatio = thumbnailWidthRatio;
  }

  public Double getThumbnailHeightRatio() {
    return thumbnailHeightRatio;
  }

  public void setThumbnailHeightRatio(Double thumbnailHeightRatio) {
    this.thumbnailHeightRatio = thumbnailHeightRatio;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public List<LabelOverlayDTO> getLabels() {
    return labels;
  }

  public void setLabels(List<LabelOverlayDTO> labels) {
    this.labels = labels;
  }

  public Long getInstanceLabelId() {
    return instanceLabelId;
  }

  public void setInstanceLabelId(Long instanceLabelId) {
    this.instanceLabelId = instanceLabelId;
  }

  public LabelOverlayDTO getFocusedLabel() {
    return focusedLabel;
  }

  public void setFocusedLabel(LabelOverlayDTO focusedLabel) {
    this.focusedLabel = focusedLabel;
  }
}
