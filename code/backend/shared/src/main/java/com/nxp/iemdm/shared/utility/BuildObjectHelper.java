package com.nxp.iemdm.shared.utility;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nxp.iemdm.exception.BadRequestException;
import java.util.Map;

public class BuildObjectHelper {

  private BuildObjectHelper() {}

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static Object buildObject(Map<?, ?> map, Class<?> clazz) {
    try {
      String json = objectMapper.writeValueAsString(map);
      return objectMapper.readValue(json, clazz);
    } catch (Exception ex) {
      String msg = String.format("Error building %s : %s", clazz.getSimpleName(), ex.getMessage());
      throw new BadRequestException(msg);
    }
  }

  public static Object buildObject(String json, Class<?> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (Exception ex) {
      String msg = String.format("Error building %s : %s", clazz.getSimpleName(), ex.getMessage());
      throw new BadRequestException(msg);
    }
  }
}
