package com.nxp.iemdm.shared.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** helper class to read Excel sheet and generate Sql insert statements */
@Log
public class GenericExcelReader {
  private static final String TABLE_NAME = "DQM_INPUT";
  private final Map<SheetType, String[]> sheetTypeMap;

  private static final String[] DQM_INPUT_COLUMNS =
      new String[] {
        "PART_12NC:S",
        "SITE:S",
        "EFFECTIVE_DATE:D",
        "PARAMETER:S",
        "PREVIOUS_COMMIT:F",
        "PART_12NC_ACTUAL:F",
        "ACTUAL_MIN:F",
        "ACTUAL_MAX:F",
        "PART_12NC_CHANGE:F",
        "DQM_STATUS:S",
        "DQM_ADVICE:S",
        "GROUP_NAME:S",
        "GROUP_ACTUAL:F"
      };

  private List<String> columns = new ArrayList<>();
  private final List<List<String>> values = new ArrayList<>();
  private final List<String> dataTypes = new ArrayList<>();

  public GenericExcelReader() {
    sheetTypeMap = new EnumMap<>(SheetType.class);
    this.initHeadersMap();
  }

  private void initHeadersMap() {
    sheetTypeMap.put(SheetType.DQM_INPUT, DQM_INPUT_COLUMNS);
  }

  public void readExcelSheet(String filename, String sheetName, SheetType type) throws Exception {
    this.readTheExcelSheet(filename, sheetName);
    this.getDataTypes(type);
    this.writeSqlDeleteStatements(type);
    this.writeSqlInsertStatements();
  }

  private void writeSqlDeleteStatements(SheetType type) {
    StringBuilder sb = new StringBuilder();
    for (List<String> row : this.values) {
      sb.append(
          String.format("delete from %s where %s;%n", TABLE_NAME, this.getWhereClause(type, row)));
    }
    log.info(sb.toString());
  }

  private void writeSqlInsertStatements() {
    String columnNames = this.getColumnNames();
    StringBuilder sb = new StringBuilder();
    for (List<String> row : this.values) {
      sb.append(
          String.format(
              "insert into %s (%s) values (%s);%n",
              TABLE_NAME, columnNames, this.getColumnValues(row)));
    }
    log.info(sb.toString());
  }

  private void getDataTypes(SheetType type) {
    String[] strings = sheetTypeMap.get(type);
    for (String str : strings) {
      String[] elems = str.split(":");
      this.dataTypes.add(elems[1]);
    }
  }

  private String getColumnNames() {
    return listToString(this.columns);
  }

  private String getColumnValues(List<String> rowValues) {
    List<String> outputStrings = new ArrayList<>();
    for (int i = 0; i < rowValues.size(); i++) {
      if (this.dataTypes.get(i).equals("S")) {
        outputStrings.add(toSqlStr(rowValues.get(i)));
      } else if (this.dataTypes.get(i).equals("D")) {
        outputStrings.add(toSqlDate(rowValues.get(i)));
      } else {
        outputStrings.add(toSql(rowValues.get(i)));
      }
    }
    return listToString(outputStrings);
  }

  private String getWhereClause(SheetType type, List<String> rowValues) {
    if (type.equals(SheetType.DQM_INPUT)) {
      return this.getWhereClauseDqmInput(rowValues);
    } else {
      throw new RuntimeException("Currently only DqmInput is supported");
    }
  }

  private String getWhereClauseDqmInput(List<String> rowValues) {
    return String.format(" %s = %s", "PART_12NC", toSqlStr(rowValues.get(0)))
        + String.format(" and %s = %s", "SITE", toSqlStr(rowValues.get(1)))
        + String.format(" and %s = %s", "EFFECTIVE_DATE", toSqlDate(rowValues.get(2)));
  }

  private String toSqlDate(String value) {
    return String.format("to_timestamp('%s', 'YYYY-MM-DD')", value);
  }

  private String toSqlStr(String value) {
    return String.format("'%s'", value);
  }

  private String toSql(String value) {
    if (value == null || value.isEmpty()) {
      return "null";
    } else {
      return value;
    }
  }

  private String listToString(List<String> outputStrings) {
    return outputStrings.stream().map(String::valueOf).collect(Collectors.joining(",", "", ""));
  }

  private void readTheExcelSheet(String filename, String sheetName) throws IOException {
    try (FileInputStream excelFile = new FileInputStream(new File(filename));
        Workbook workbook = new XSSFWorkbook(excelFile)) {
      Sheet sheet = workbook.getSheet(sheetName);
      Iterator<Row> iterator = sheet.iterator();
      values.clear();
      int row = 0;
      while (iterator.hasNext()) {
        List<String> rowValues = new ArrayList<>();
        Row currentRow = iterator.next();
        Iterator<Cell> cellIterator = currentRow.iterator();

        while (cellIterator.hasNext()) {
          Cell currentCell = cellIterator.next();
          if (currentCell.getCellType() == CellType.STRING) {
            rowValues.add(currentCell.getStringCellValue());
          } else if (currentCell.getCellType() == CellType.NUMERIC) {
            rowValues.add("" + currentCell.getNumericCellValue());
          }
        }
        if (row == 0) {
          this.columns = rowValues;
        } else {
          values.add(rowValues);
        }
        row++;
      }
    }
  }

  // -----------------------------
  public enum SheetType {
    DQM_INPUT,
    GLOBAL_DQM_ACTUAL,
    DQM_ISSUE,
    CONS_PARAM,
    YIELD_OUTPUT
  }
}
