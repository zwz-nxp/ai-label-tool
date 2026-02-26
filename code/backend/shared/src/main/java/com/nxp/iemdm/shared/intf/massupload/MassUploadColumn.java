package com.nxp.iemdm.shared.intf.massupload;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class MassUploadColumn implements Comparable<MassUploadColumn> {

  private final String name;
  private final Class<?> type;
  private int index = -1;
  private final boolean required;

  public MassUploadColumn(String name, Class<?> type) {
    this(name, type, true);
  }

  public MassUploadColumn(String name, Class<?> type, boolean isRequired) {
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    this.required = isRequired;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public static Collection<Integer> getIndexesAsList(Collection<MassUploadColumn> collection) {
    return collection.stream().map(MassUploadColumn::getIndex).collect(Collectors.toList());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MassUploadColumn)) {
      return false;
    }

    if (o == this) {
      return true;
    }

    return this.name.equals(((MassUploadColumn) o).name);
  }

  @Override
  public int hashCode() {
    return 53 * this.name.hashCode();
  }

  @Override
  public int compareTo(MassUploadColumn o) {
    return getName().compareTo(o.getName());
  }
}
