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
@Table(name = "la_project_class")
@SequenceGenerator(
    sequenceName = "hibernate_sequence",
    allocationSize = 1,
    name = "hibernate_sequence")
@Audited
public class ProjectClass implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "class_name", length = 100)
  private String className;

  @Column(name = "description", length = 100)
  private String description;

  @Column(name = "color_code", length = 7)
  private String colorCode;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
