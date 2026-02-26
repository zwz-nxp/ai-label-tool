package com.nxp.iemdm.shared.dto.landingai;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/**
 * Data Transfer Object for ImageLabel Used for API communication between frontend and backend
 *
 * <p>Requirements: 24.1, 25.1
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageLabelDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private Long id;

  @NotNull(message = "Image ID is required")
  private Long imageId;

  @NotNull(message = "Class ID is required")
  private Long classId;

  private String position; // JSON string, can be null for Classification

  private Integer confidenceRate;

  private String annotationType; // Ground Truth, Prediction

  private Instant createdAt;

  private String createdBy;
}
