package com.nxp.iemdm.model.massupload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
When we specify JsonFormat.Shape.OBJECT, then we get the 3 public properties of this enum in
the serialized json string. e.g:
[{  "version": 1.0,
		"sheetName": "YIELD_CONSUMPTION_PARAM",
		"type": "yield"},]
 If we remove that specification, we get [YIELD, AVAILABILITY, SUBCON] as a json string. Because
 the readMeFileName uses the same name in order to specify what type of upload we are dealing with
 we can use the readMeFileName as the type when we are serializing this enum.
 */

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MassUploadType {
  YIELD(1.0, "YIELD_CONSUMPTION_PARAM", "yield", "Yield", "yield", 0, "Yield"),
  ;

  public final double version;
  public final String sheetName;
  public final String massUploadName;

  @JsonProperty("type")
  public final String massUploadTypeName;

  public final int setOfColumns;
  public final String category;
  public final String readMeFileName;

  MassUploadType(
      double version,
      String sheetName,
      String readMeFileName,
      String massUploadName,
      String massUploadTypeName,
      int setOfColumns,
      String category) {
    this.version = version;
    this.sheetName = sheetName;
    this.readMeFileName = readMeFileName;
    this.massUploadName = massUploadName;
    this.massUploadTypeName = massUploadTypeName;
    this.setOfColumns = setOfColumns;
    this.category = category;
  }

  public String getSheetNameIncludingVersion() {
    return String.format("%s_v%s", this.sheetName, this.version);
  }
}
