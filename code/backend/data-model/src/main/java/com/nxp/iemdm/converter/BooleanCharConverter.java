package com.nxp.iemdm.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Currently not in use. But can be helpful if hibernate does not understand that we use '0' for
 * false and '1' for true. Default supplied converters are numeric to boolean (number(1,0), tinyInt,
 * bit, etc...) and YesNo ('Y', 'N')
 */
@Converter(autoApply = true)
public class BooleanCharConverter implements AttributeConverter<Boolean, Character> {
  public static final BooleanCharConverter INSTANCE = new BooleanCharConverter();

  @Override
  public Character convertToDatabaseColumn(Boolean attribute) {
    if (attribute == null) {
      return '0';
    }

    return attribute ? '1' : '0';
  }

  @Override
  public Boolean convertToEntityAttribute(Character dbData) {
    if (dbData == null) {
      return false;
    }

    return switch (dbData) {
      case '1' -> true;
      case '0' -> false;
      default -> null;
    };
  }
}
