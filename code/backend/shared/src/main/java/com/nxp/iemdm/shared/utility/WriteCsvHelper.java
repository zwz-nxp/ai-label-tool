package com.nxp.iemdm.shared.utility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WriteCsvHelper {

  public List<String> generateCsv(Set<? extends Object> objectSet, String[] fieldNames)
      throws Exception {
    return this.generateCsv(List.copyOf(objectSet), fieldNames);
  }

  public List<String> generateCsv(List<? extends Object> objectList, String[] fieldNames)
      throws Exception {
    List<String> result = new ArrayList<>();
    result.add(String.join(",", Arrays.asList(fieldNames)));
    objectList.forEach(o -> result.add(this.addCsvLine(o, fieldNames)));
    Collections.sort(result);
    return result;
  }

  // ------------------------------

  private String addCsvLine(Object object, String[] fieldNames) {
    List<String> values = new ArrayList<>();

    for (String fieldName : fieldNames) {
      values.add(this.getStringValue(object, fieldName));
    }

    return String.join(",", values);
  }

  private String getStringValue(Object object, String fieldName) {

    try {
      Class<?> clazz = object.getClass();
      Field fld = clazz.getDeclaredField(fieldName);
      fld.setAccessible(true);
      Object value = fld.get(object);
      return this.parseValue(value);
    } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private String parseValue(Object value) {
    if (value != null) {
      try {
        Double d = Double.parseDouble(value.toString());
        return String.format("%.4f", d).replace(',', '.');
      } catch (NumberFormatException e) {
        // continue
      }
      return value.toString();
    } else {
      return "null";
    }
  }
}
