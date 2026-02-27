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
@Table(name = "la_confidential_report")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConfidentialReport implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "model_id", nullable = false)
  @JsonIgnoreProperties({
    "hibernateLazyInitializer",
    "handler",
    "trainingRecord",
    "confidentialReport"
  })
  private Model model;

  @Column(name = "training_correct_rate")
  private Integer trainingCorrectRate;

  @Column(name = "dev_correct_rate")
  private Integer devCorrectRate;

  @Column(name = "test_correct_rate")
  private Integer testCorrectRate;

  @Column(name = "confidence_threshold")
  private Integer confidenceThreshold;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 50)
  private String createdBy;
}
