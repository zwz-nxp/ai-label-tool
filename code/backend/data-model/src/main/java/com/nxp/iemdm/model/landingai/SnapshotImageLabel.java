package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/**
 * Entity representing a snapshot of an image label record. Maps to the la_images_label_ss table.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "la_images_label_ss")
@IdClass(SnapshotImageLabelId.class)
public class SnapshotImageLabel implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private Long id;

  @Id
  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "image_id")
  private Long imageId;

  @Column(name = "class_id")
  private Long classId;

  @Column(name = "position", columnDefinition = "TEXT")
  private String position;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
