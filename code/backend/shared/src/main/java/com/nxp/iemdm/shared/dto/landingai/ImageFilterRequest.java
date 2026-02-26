package com.nxp.iemdm.shared.dto.landingai;

import java.util.List;
import java.util.Map;

/**
 * DTO for image filtering criteria. Supports multiple filter types that can be applied
 * simultaneously.
 */
public class ImageFilterRequest {

  /** Filter by media status (e.g., "labeled", "unlabeled") */
  private List<String> mediaStatus;

  /** Filter by ground truth label class IDs */
  private List<Long> groundTruthLabels;

  /** Filter by prediction label class IDs */
  private List<Long> predictionLabels;

  /** Filter by annotation type (e.g., "Ground truth", "Prediction") */
  private String annotationType;

  /** Filter by model ID (for prediction labels only) */
  private Long modelId;

  /** Filter by split (e.g., "training", "dev", "test") */
  private List<String> split;

  /** Filter by tag IDs */
  private List<Long> tags;

  /** Filter by media name (partial match) */
  private String mediaName;

  /** Filter by labeler username */
  private String labeler;

  /** Filter by media ID */
  private String mediaId;

  /** Filter by metadata key-value pairs */
  private Map<String, String> metadata;

  /** Filter for No Class images (is_labeled=true AND is_no_class=true) */
  private Boolean noClass;

  /** Filter for images without any prediction labels for the selected model */
  private Boolean predictionNoClass;

  // Constructors

  public ImageFilterRequest() {}

  public ImageFilterRequest(
      List<String> mediaStatus,
      List<Long> groundTruthLabels,
      List<Long> predictionLabels,
      String annotationType,
      Long modelId,
      List<String> split,
      List<Long> tags,
      String mediaName,
      String labeler,
      String mediaId,
      Map<String, String> metadata,
      Boolean noClass,
      Boolean predictionNoClass) {
    this.mediaStatus = mediaStatus;
    this.groundTruthLabels = groundTruthLabels;
    this.predictionLabels = predictionLabels;
    this.annotationType = annotationType;
    this.modelId = modelId;
    this.split = split;
    this.tags = tags;
    this.mediaName = mediaName;
    this.labeler = labeler;
    this.mediaId = mediaId;
    this.metadata = metadata;
    this.noClass = noClass;
    this.predictionNoClass = predictionNoClass;
  }

  // Getters and Setters

  public List<String> getMediaStatus() {
    return mediaStatus;
  }

  public void setMediaStatus(List<String> mediaStatus) {
    this.mediaStatus = mediaStatus;
  }

  public List<Long> getGroundTruthLabels() {
    return groundTruthLabels;
  }

  public void setGroundTruthLabels(List<Long> groundTruthLabels) {
    this.groundTruthLabels = groundTruthLabels;
  }

  public List<Long> getPredictionLabels() {
    return predictionLabels;
  }

  public void setPredictionLabels(List<Long> predictionLabels) {
    this.predictionLabels = predictionLabels;
  }

  public String getAnnotationType() {
    return annotationType;
  }

  public void setAnnotationType(String annotationType) {
    this.annotationType = annotationType;
  }

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public List<String> getSplit() {
    return split;
  }

  public void setSplit(List<String> split) {
    this.split = split;
  }

  public List<Long> getTags() {
    return tags;
  }

  public void setTags(List<Long> tags) {
    this.tags = tags;
  }

  public String getMediaName() {
    return mediaName;
  }

  public void setMediaName(String mediaName) {
    this.mediaName = mediaName;
  }

  public String getLabeler() {
    return labeler;
  }

  public void setLabeler(String labeler) {
    this.labeler = labeler;
  }

  public String getMediaId() {
    return mediaId;
  }

  public void setMediaId(String mediaId) {
    this.mediaId = mediaId;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public Boolean getNoClass() {
    return noClass;
  }

  public void setNoClass(Boolean noClass) {
    this.noClass = noClass;
  }

  public Boolean getPredictionNoClass() {
    return predictionNoClass;
  }

  public void setPredictionNoClass(Boolean predictionNoClass) {
    this.predictionNoClass = predictionNoClass;
  }

  /** Check if any filters are applied */
  public boolean hasFilters() {
    return (mediaStatus != null && !mediaStatus.isEmpty())
        || (groundTruthLabels != null && !groundTruthLabels.isEmpty())
        || (predictionLabels != null && !predictionLabels.isEmpty())
        || (annotationType != null && !annotationType.isEmpty())
        || (modelId != null)
        || (split != null && !split.isEmpty())
        || (tags != null && !tags.isEmpty())
        || (mediaName != null && !mediaName.isEmpty())
        || (labeler != null && !labeler.isEmpty())
        || (mediaId != null && !mediaId.isEmpty())
        || (metadata != null && !metadata.isEmpty())
        || (noClass != null && noClass)
        || (predictionNoClass != null && predictionNoClass);
  }

  @Override
  public String toString() {
    return "ImageFilterRequest{"
        + "mediaStatus="
        + mediaStatus
        + ", groundTruthLabels="
        + groundTruthLabels
        + ", predictionLabels="
        + predictionLabels
        + ", annotationType='"
        + annotationType
        + '\''
        + ", modelId="
        + modelId
        + ", split="
        + split
        + ", tags="
        + tags
        + ", mediaName='"
        + mediaName
        + '\''
        + ", labeler='"
        + labeler
        + '\''
        + ", mediaId='"
        + mediaId
        + '\''
        + ", metadata="
        + metadata
        + ", noClass="
        + noClass
        + ", predictionNoClass="
        + predictionNoClass
        + '}';
  }
}
