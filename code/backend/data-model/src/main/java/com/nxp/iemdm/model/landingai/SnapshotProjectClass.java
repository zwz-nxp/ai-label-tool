package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/**
 * Entity representing a snapshot of a project class record. Maps to the la_project_class_ss table.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "la_project_class_ss")
@IdClass(SnapshotProjectClassId.class)
public class SnapshotProjectClass implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private Long id;

  @Id
  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "class_name", length = 100)
  private String className;

  @Column(name = "description", length = 100)
  private String description;

  @Column(name = "color_code", length = 7)
  private String colorCode;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
