package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@SequenceGenerator(
    sequenceName = "image_label_sequence",
    allocationSize = 1,
    name = "image_label_sequence")
@Table(name = "la_images_label")
public class ImageLabel implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_label_sequence")
  @Column(name = "id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_id", nullable = false)
  private Image image;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id", nullable = false)
  private ProjectClass projectClass;

  @Column(name = "position", columnDefinition = "TEXT")
  private String position; // JSON string, Yolo and Coco format

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
