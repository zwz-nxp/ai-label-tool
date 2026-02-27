package com.nxp.iemdm.hibernate.dialect;

import java.sql.Types;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

public class CustomPostgreSQLDialect extends PostgreSQLDialect {

  @Override
  public JdbcType resolveSqlTypeDescriptor(
      String columnTypeName,
      int jdbcTypeCode,
      int precision,
      int scale,
      JdbcTypeRegistry jdbcTypeRegistry) {
    int toBeResolvedTypeCode;
    switch (jdbcTypeCode) {
      case Types.CHAR -> toBeResolvedTypeCode = StandardBasicTypes.BOOLEAN.getSqlTypeCode();
      case Types.NUMERIC -> toBeResolvedTypeCode = StandardBasicTypes.DOUBLE.getSqlTypeCode();
      case Types.DATE, Types.TIME, Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE ->
          toBeResolvedTypeCode = StandardBasicTypes.INSTANT.getSqlTypeCode();
      default -> toBeResolvedTypeCode = jdbcTypeCode;
    }
    return super.resolveSqlTypeDescriptor(
        columnTypeName, toBeResolvedTypeCode, precision, scale, jdbcTypeRegistry);
  }
}
