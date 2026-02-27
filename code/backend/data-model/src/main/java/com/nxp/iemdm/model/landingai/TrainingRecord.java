package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;
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
@Table(name = "la_training_record")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TrainingRecord implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "trainingRecords", "location"})
  private Project project;

  @Column(name = "status", length = 20)
  private String status; // pending,omplete

  @Column(name = "model_alias", length = 256)
  private String modelAlias;

  @Column(name = "track_id", length = 256)
  private String trackId;

  @Column(name = "epochs")
  private Integer epochs;

  @Column(name = "model_size", length = 50)
  private String modelSize;

  @Column(name = "transform_param", length = 500)
  private String transformParam; // JSON

  @Column(name = "augmentation_param")
  private String modelParam; // JSON

  @Column(name = "credit_consumption", length = 500)
  private String creditConsumption; // JSON

  @Column(name = "training_count")
  private Integer trainingCount;

  @Column(name = "dev_count")
  private Integer devCount;

  @Column(name = "test_count")
  private Integer testCount;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;

  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "model_track_key", length = 1024)
  private String modelTrackKey;
}
