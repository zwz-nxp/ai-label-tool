package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    sequenceName = "hibernate_sequence",
    allocationSize = 1,
    name = "hibernate_sequence")
@Table(name = "la_model")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Model implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "training_record_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "project"})
  private TrainingRecord trainingRecord;

  @Column(name = "model_alias", length = 256)
  private String modelAlias;

  @Column(name = "track_id", length = 256)
  private String trackId;

  @Column(name = "model_version", length = 36)
  private String modelVersion;

  @Column(name = "status", length = 50)
  private String status;

  @Column(name = "training_f1_rate")
  private Double trainingF1Rate;

  @Column(name = "training_precision_rate")
  private Double trainingPrecisionRate;

  @Column(name = "training_recall_rate")
  private Double trainingRecallRate;

  @Column(name = "dev_f1_rate")
  private Double devF1Rate;

  @Column(name = "dev_precision_rate")
  private Double devPrecisionRate;

  @Column(name = "dev_recall_rate")
  private Double devRecallRate;

  @Column(name = "test_f1_rate")
  private Double testF1Rate;

  @Column(name = "test_precision_rate")
  private Double testPrecisionRate;

  @Column(name = "test_recall_rate")
  private Double testRecallRate;

  @Column(name = "image_count")
  private Integer imageCount;

  @Column(name = "label_count")
  private Integer labelCount;

  @Column(name = "is_favorite")
  private Boolean isFavorite;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 50)
  private String createdBy;
}
