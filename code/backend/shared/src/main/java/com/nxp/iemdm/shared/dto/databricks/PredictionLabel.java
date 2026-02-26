package com.nxp.iemdm.shared.dto.databricks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Label data for a single detected object in an image.
 *
 * <p>Contains normalized bounding box coordinates (0-1 range), class identifier, and confidence
 * score.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionLabel {

  /** Normalized x-coordinate of bounding box center (0-1 range) */
  private String xcenter;

  /** Normalized y-coordinate of bounding box center (0-1 range) */
  private String ycenter;

  /** Normalized width of bounding box (0-1 range) */
  private String width;

  /** Normalized height of bounding box (0-1 range) */
  private String height;

  /** Class identifier for the detected object */
  private Integer classId;

  /** Confidence score for the prediction (0-1 range) */
  private String confidenceRate;
}
