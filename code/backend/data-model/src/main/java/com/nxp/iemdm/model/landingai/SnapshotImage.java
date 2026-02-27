package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/** Entity representing a snapshot of an image record. Maps to the la_images_ss table. */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "la_images_ss")
@IdClass(SnapshotImageId.class)
public class SnapshotImage implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private Long id;

  @Id
  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "file_name", length = 255)
  private String fileName;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  @Column(name = "split", length = 10)
  private String split;

  @Column(name = "is_no_class")
  private Boolean isNoClass;

  @Column(name = "is_labeled")
  private Boolean isLabeled;

  @Column(name = "thumbnail_image")
  private byte[] thumbnailImage;

  @Column(name = "thumbnail_width_ratio")
  private Double thumbnailWidthRatio;

  @Column(name = "thumbnail_height_ratio")
  private Double thumbnailHeightRatio;

  @Column(name = "file_id")
  private Long fileId;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
