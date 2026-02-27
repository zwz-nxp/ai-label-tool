package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@SequenceGenerator(sequenceName = "image_sequence", allocationSize = 1, name = "image_sequence")
@Table(name = "la_images")
public class Image implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_sequence")
  @Column(name = "id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "file_name", length = 255)
  private String fileName;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  @Column(name = "split", length = 10)
  private String split; // training/dev/test

  @Column(name = "is_no_class")
  private Boolean isNoClass;

  @Column(name = "is_labeled")
  private Boolean isLabeled = false;

  @Column(name = "thumbnail_image")
  private byte[] thumbnailImage;

  @Column(name = "thumbnail_width_ratio")
  private Double thumbnailWidthRatio;

  @Column(name = "thumbnail_height_ratio")
  private Double thumbnailHeightRatio;

  @Column(name = "file_id")
  private Long fileId;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;

  // JSON serialization helper - expose projectId instead of full project object
  @JsonProperty("projectId")
  public Long getProjectId() {
    return project != null ? project.getId() : null;
  }
}
