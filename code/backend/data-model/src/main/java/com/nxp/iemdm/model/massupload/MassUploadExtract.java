package com.nxp.iemdm.model.massupload;

import java.util.HashMap;
import java.util.Map;

public class MassUploadExtract {
  private Object transformedEntity;
  private final Map<String, Object> extractedEntity;
  private final MassUploadAction massUploadAction;
  private final int rowNumber;

  public MassUploadExtract(MassUploadAction massUploadAction, int rowNumber) {
    this.extractedEntity = new HashMap<>();
    this.transformedEntity = null;
    this.massUploadAction = massUploadAction;
    this.rowNumber = rowNumber;
  }

  public Map<String, Object> getExtractedEntity() {
    return this.extractedEntity;
  }

  public MassUploadAction getMassUploadAction() {
    return this.massUploadAction;
  }

  public void put(String key, Object value) {
    this.extractedEntity.put(key, value);
  }

  public void setTransformedEntity(Object transformedEntity) {
    this.transformedEntity = transformedEntity;
  }

  public Object getTransformedEntity() {
    return this.transformedEntity;
  }

  public int getRowNumber() {
    return this.rowNumber;
  }
}
