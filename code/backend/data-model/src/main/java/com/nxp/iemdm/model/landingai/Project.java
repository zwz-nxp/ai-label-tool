package com.nxp.iemdm.model.landingai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nxp.iemdm.model.location.Location;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(
    name = "la_projects",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "location_id"})})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false, length = 255)
  @NotBlank
  private String name;

  @Column(name = "status", length = 20)
  private String status; // Upload, Label, Train, Predict

  @Column(name = "type", length = 20)
  private String type; // Object Detection, Segmentation, Classification

  @Column(name = "model_name", length = 36)
  private String modelName;

  @Column(name = "group_name", length = 20)
  private String groupName; // WT, FE, BE, QA

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Location location;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
