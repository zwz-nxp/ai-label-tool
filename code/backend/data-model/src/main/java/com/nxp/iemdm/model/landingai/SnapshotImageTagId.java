package com.nxp.iemdm.model.landingai;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.*;

/** Composite primary key for SnapshotImageTag entity. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotImageTagId implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  private Long id;
  private Long snapshotId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SnapshotImageTagId that = (SnapshotImageTagId) o;
    return Objects.equals(id, that.id) && Objects.equals(snapshotId, that.snapshotId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, snapshotId);
  }
}
