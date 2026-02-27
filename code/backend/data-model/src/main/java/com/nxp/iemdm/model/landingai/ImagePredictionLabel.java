package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SequenceGenerator(
    sequenceName = "hibernate_sequence",
    allocationSize = 1,
    name = "hibernate_sequence")
@Table(
    name = "la_images_prediction_label",
    indexes = {
      @Index(name = "fki_fk_prediction_label_class", columnList = "class_id"),
      @Index(name = "fki_fk_prediction_label_image", columnList = "image_id")
    })
public class ImagePredictionLabel implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_id")
  private Image image;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id")
  private ProjectClass projectClass;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "model_id")
  private Model model;

  @Column(name = "position", columnDefinition = "TEXT")
  private String position;

  @Column(name = "confidence_rate", columnDefinition = "integer")
  private Integer confidenceRate;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
