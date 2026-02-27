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
    sequenceName = "hibernate_sequence",
    allocationSize = 1,
    name = "hibernate_sequence")
@Table(name = "la_project_split")
public class ProjectSplit implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "train_ratio")
  private Integer trainRatio = 70;

  @Column(name = "dev_ratio")
  private Integer devRatio = 20;

  @Column(name = "test_ratio")
  private Integer testRatio = 10;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id", nullable = true)
  private ProjectClass projectClass;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
