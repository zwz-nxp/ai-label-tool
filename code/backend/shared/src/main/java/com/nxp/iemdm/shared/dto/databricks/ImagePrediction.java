package com.nxp.iemdm.shared.dto.databricks;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prediction results for a single image.
 *
 * <p>Contains the image URL, dataset classification, and list of detected object labels.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagePrediction {

  /** URL of the image that was tested */
  private String imageUrl;

  /** Dataset classification (e.g., "train", "dev", "test") */
  private String dataSet;

  /** List of detected object labels with bounding boxes */
  private List<PredictionLabel> labelList;
}
