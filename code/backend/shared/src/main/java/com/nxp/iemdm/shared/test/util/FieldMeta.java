package com.nxp.iemdm.shared.test.util;

public class FieldMeta {
  private String name = null;
  private String fieldName = null;
  private PostProcess postProcess = null;

  public FieldMeta(String name, String fieldName) {
    super();
    this.name = name;
    this.fieldName = fieldName;
  }

  public FieldMeta(String name, PostProcess postProcess) {
    super();
    this.name = name;
    this.postProcess = postProcess;
  }

  public String getName() {
    return name;
  }

  public String getFieldName() {
    return fieldName;
  }

  public PostProcess getPostProcess() {
    return postProcess;
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
    FieldMeta other = (FieldMeta) obj;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    return true;
  }
}
