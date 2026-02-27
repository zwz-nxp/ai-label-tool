package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/**
 * Entity representing a snapshot of an image metadata record. Maps to the la_images_metadata_ss
 * table.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "la_images_metadata_ss")
@IdClass(SnapshotImageMetadataId.class)
public class SnapshotImageMetadata implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private Long id;

  @Id
  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "image_id")
  private Long imageId;

  @Column(name = "metadata_id")
  private Long metadataId;

  @Column(name = "value", length = 500)
  private String value;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
