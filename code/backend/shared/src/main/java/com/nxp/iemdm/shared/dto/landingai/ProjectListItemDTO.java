package com.nxp.iemdm.shared.dto.landingai;

import java.time.Instant;

/**
 * DTO for project list items with display fields and counts. Used for project card display in the
 * project list view.
 */
public class ProjectListItemDTO {

  private Long id;
  private String name;
  private String type;
  private String modelName;
  private String groupName;
  private String createdBy;
  private Instant createdAt;
  private Integer imageCount;
  private Integer labelCount;
  private byte[] firstImageThumbnail;

  public ProjectListItemDTO() {}

  public ProjectListItemDTO(
      Long id,
      String name,
      String type,
      String modelName,
      String groupName,
      String createdBy,
      Instant createdAt,
      Integer imageCount,
      Integer labelCount,
      byte[] firstImageThumbnail) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.modelName = modelName;
    this.groupName = groupName;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
    this.imageCount = imageCount;
    this.labelCount = labelCount;
    this.firstImageThumbnail = firstImageThumbnail;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Integer getImageCount() {
    return imageCount;
  }

  public void setImageCount(Integer imageCount) {
    this.imageCount = imageCount;
  }

  public Integer getLabelCount() {
    return labelCount;
  }

  public void setLabelCount(Integer labelCount) {
    this.labelCount = labelCount;
  }

  public byte[] getFirstImageThumbnail() {
    return firstImageThumbnail;
  }

  public void setFirstImageThumbnail(byte[] firstImageThumbnail) {
    this.firstImageThumbnail = firstImageThumbnail;
  }
}
