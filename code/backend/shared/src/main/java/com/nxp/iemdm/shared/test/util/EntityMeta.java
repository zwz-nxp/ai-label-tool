package com.nxp.iemdm.shared.test.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated // no longer in use
public class EntityMeta {
  private final String name;
  private final Class<?> clazz;
  private List<FieldMeta> fields = new ArrayList<>();

  public EntityMeta(String name, Class<?> clazz, List<FieldMeta> fields) {
    super();
    this.name = name;
    this.clazz = clazz;
    this.fields = fields;
  }

  public EntityMeta(String name, Class<?> clazz, FieldMeta[] fields) {
    super();
    this.name = name;
    this.clazz = clazz;

    Collections.addAll(this.fields, fields);
  }

  public String getName() {
    return name;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public List<FieldMeta> getFields() {
    return fields;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EntityMeta other = (EntityMeta) obj;
    if (name == null) {
      return other.name == null;
    } else return name.equals(other.name);
  }
}
