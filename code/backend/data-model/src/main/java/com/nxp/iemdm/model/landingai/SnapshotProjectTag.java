package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/** Entity representing a snapshot of a project tag record. Maps to the la_project_tag_ss table. */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "la_project_tag_ss")
@IdClass(SnapshotProjectTagId.class)
public class SnapshotProjectTag implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private Long id;

  @Id
  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
