package com.nxp.iemdm.model.landingai;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.*;

/**
 * Entity representing a snapshot of a project split record. Maps to the la_project_split_ss table.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "la_project_split_ss")
@IdClass(SnapshotProjectSplitId.class)
public class SnapshotProjectSplit implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "id")
  private Long id;

  @Id
  @Column(name = "snapshot_id")
  private Long snapshotId;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "train_ratio")
  private Integer trainRatio;

  @Column(name = "dev_ratio")
  private Integer devRatio;

  @Column(name = "test_ratio")
  private Integer testRatio;

  @Column(name = "class_id")
  private Long classId;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "created_by", length = 36)
  private String createdBy;
}
