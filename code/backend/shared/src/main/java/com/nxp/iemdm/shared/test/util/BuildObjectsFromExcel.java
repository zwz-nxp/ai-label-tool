package com.nxp.iemdm.shared.test.util;

import com.nxp.iemdm.shared.utility.ExcelUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Helper class to build object like Equipment and Event's of different type from an Excel file. */
public class BuildObjectsFromExcel {

  private final List<EntityMeta> entityTypes = new ArrayList<>();

  private static final Map<String, DayOfWeek> WEEKDAY_MAP = new HashMap<>();

  private XSSFWorkbook workbook;
  private int rowNum = 0;
  private final List<List<Cell>> allExcelValues = new ArrayList<>();
  private final Map<Class<?>, List<List<Cell>>> valuesMap = new HashMap<>();
  private final Map<Class<?>, Map<String, Integer>> columsMap =
      new HashMap<>(); // Per entity the columnindex is mapped to the fields
  private final Map<Class<?>, List<Object>> entityListMap = new HashMap<>();
  private final Map<Class<?>, EntityMeta> entityTypeMap = new HashMap<>();
  private Class<?> currentEntity = null;

  public BuildObjectsFromExcel(EntityMeta... entityTypes) {
    Collections.addAll(this.entityTypes, entityTypes);
    this.init();
  }

  public void parseExcelFiles(
      String excelFilename, int sheetIndex, List<String> additionalExcelFiles) throws Exception {
    this.parseExcelFile(excelFilename, sheetIndex);
    for (String additionalFile : additionalExcelFiles) {
      this.parseExcelFile(additionalFile, 0);
    }
  }

  public void parseExcelFile(String filename, int sheetIndex) throws Exception {
    this.readTheExcelSheet(filename, sheetIndex);
    this.mapValues();
    this.buildEntities();
    this.runNestedProcesses();
  }

  public List<Object> getEntityList(Class<?> clazz) {
    return this.entityListMap.get(clazz);
  }

  public int getRowIndex(Class<?> clazz, String name) {
    int index = 0;
    for (FieldMeta fld : this.entityTypeMap.get(clazz).getFields()) {
      if (fld.getName().equals(name)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  public List<FieldMeta> getFieldMetaList(Class<?> clazz) {
    return this.entityTypeMap.get(clazz).getFields();
  }

  // ---------- private -----------------

  private void init() {
    this.initEntityListMap();
    this.initEntityTypeMap();
    this.initWeeekDayMap();
  }

  private void initEntityTypeMap() {
    for (EntityMeta entityMeta : this.entityTypes) {
      this.entityTypeMap.put(entityMeta.getClazz(), entityMeta);
    }
  }

  private void initEntityListMap() {
    for (EntityMeta entityMeta : this.entityTypes) {
      this.entityListMap.put(entityMeta.getClazz(), new ArrayList<>());
    }
  }

  private void initWeeekDayMap() {
    for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
      WEEKDAY_MAP.put(dayOfWeek.name().substring(0, 3), dayOfWeek);
    }
  }

  private void readTheExcelSheet(String filename, int sheetIndex) throws IOException {
    try (FileInputStream excelFile = new FileInputStream(new File(filename));
        Workbook workbook = new XSSFWorkbook(excelFile)) {

      Sheet sheet = workbook.getSheetAt(sheetIndex);
      Iterator<Row> iterator = sheet.iterator();
      allExcelValues.clear();
      while (iterator.hasNext()) {
        List<Cell> rowValues = new ArrayList<>();
        Row currentRow = iterator.next();
        Iterator<Cell> cellIterator = currentRow.iterator();

        while (cellIterator.hasNext()) {
          Cell currentCell = cellIterator.next();
          rowValues.add(currentCell);
        }
        if (this.isValidRow(rowValues)) {
          allExcelValues.add(rowValues);
        }
      }
    }
  }

  private boolean isValidRow(List<Cell> rowValues) {
    return !rowValues.isEmpty()
        && !"BLANK".equals(rowValues.get(0).getCellType().name())
        && !isComment(rowValues.get(0));
  }

  private boolean isComment(Cell cell) {
    if (CellType.STRING.equals(cell.getCellType())) {
      String strValue = cell.getStringCellValue().trim();
      return strValue.startsWith("//") || strValue.startsWith("--") || strValue.startsWith("#");
    } else {
      return false;
    }
  }

  private void mapValues() {
    for (int i = 0; i < this.allExcelValues.size(); i++) {
      if (this.isEntityIndicator(i)) {
        Class<?> entity = this.getEntity(i);
        this.currentEntity = entity;
        if (!this.valuesMap.containsKey(entity)) {
          this.valuesMap.put(entity, new ArrayList<>());
          this.columsMap.put(entity, new HashMap<>());
        }

        this.fillColumnsMap(i, entity);
      } else if (currentEntity != null) {
        this.valuesMap.get(currentEntity).add(this.allExcelValues.get(i));
      }
    }
  }

  private void fillColumnsMap(int i, Class<?> entity) {
    Map<String, Integer> colMap = this.columsMap.get(entity);
    List<Cell> entityHeaders = this.allExcelValues.get(i + 1);
    List<FieldMeta> fields = this.getFieldMetaList(entity);
    for (int col = 0; col < entityHeaders.size(); col++) {
      String hdr = entityHeaders.get(col).getStringCellValue();
      if (!hdr.isEmpty()) {
        Optional<FieldMeta> fldOpt =
            fields.stream().filter(e -> e.getName().equals(hdr)).findFirst();
        if (fldOpt.isPresent()) {
          colMap.put(fldOpt.get().getFieldName(), col);
        } else {
          System.out.printf("Can not find fieldname for %s : %s%n", entity.getSimpleName(), hdr);
        }
      }
    }
  }

  private void buildEntities() throws Exception {
    for (EntityMeta entityMeta : this.entityTypes) {
      List<Object> targetList = this.entityListMap.get(entityMeta.getClazz());
      if (targetList.isEmpty()) {
        List<List<Cell>> cellValues = this.valuesMap.get(entityMeta.getClazz());
        this.buildList(entityMeta, cellValues);
      }
    }
  }

  private void runNestedProcesses() throws Exception {
    for (EntityMeta entityMeta : this.entityTypes) {
      for (FieldMeta fldMeta : entityMeta.getFields()) {
        if (fldMeta.getPostProcess() != null) {
          List<List<Cell>> cells = this.valuesMap.get(entityMeta.getClazz());
          if (cells != null) {
            for (int idx = 0; idx < cells.size(); idx++) {
              if (this.entityListMap.get(entityMeta.getClazz()).size() > idx) {
                Object entity = this.entityListMap.get(entityMeta.getClazz()).get(idx);
                int rowIndex = this.getRowIndex(entityMeta.getClazz(), fldMeta.getName());
                List<Cell> rowCells = cells.get(idx);
                fldMeta
                    .getPostProcess()
                    .process(entity, fldMeta, rowCells, rowCells.get(rowIndex), this);
              }
            }
          }
        }
      }
    }
  }

  private void buildList(EntityMeta entityMeta, List<List<Cell>> cellValues) throws Exception {
    if (cellValues != null) {
      List<Object> targetList = this.entityListMap.get(entityMeta.getClazz());
      for (List<Cell> excelRow : cellValues) {
        Object object = this.buildObject(entityMeta, excelRow);
        this.addObjectToList(object, targetList);
      }
    }
  }

  private Object buildObject(EntityMeta entityMeta, List<Cell> excelRow) {
    try {
      Object object = entityMeta.getClazz().getDeclaredConstructor(new Class[] {}).newInstance();
      for (FieldMeta fld : entityMeta.getFields()) {
        this.setValue(object, fld, entityMeta.getFields(), excelRow);
      }
      return object;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private void setValue(Object object, FieldMeta fld, List<FieldMeta> fields, List<Cell> cells) {
    try {
      String fldName = fld.getFieldName();
      if (fldName == null) return;

      Field field = this.getField(object, fldName);
      if (field == null) return;

      field.setAccessible(true);

      Class<?> clazz = object.getClass();
      Object value = null;
      int colIndex = this.getColumIndex(fldName, clazz);
      if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
        value = this.parseIntCell(cells.get(colIndex));
      } else if (field.getType().equals(Double.class)) {
        value = this.parseNumericCell(cells.get(colIndex));
      } else if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
        value = this.parseBooleanCell(cells.get(colIndex));
      } else if (field.getType().equals(LocalDate.class)) {
        value = this.parseLocalDateCell(cells.get(colIndex));
      } else {
        value = ExcelUtility.getString(cells, colIndex);
      }

      field.set(object, value);
    } catch (Exception e) {
      System.out.println("Error: " + e);
    }
  }

  private Field getField(Object object, String fldName) {
    try {
      Field field = null;
      Class<?> clazz = object.getClass();
      if (this.hasField(clazz, fldName)) {
        field = clazz.getDeclaredField(fldName);
      } else {
        if (this.hasField(clazz.getSuperclass(), fldName)) {
          field = clazz.getSuperclass().getDeclaredField(fldName);
        }
      }
      return field;
    } catch (NoSuchFieldException | SecurityException e) {
      System.out.printf(
          "Could not find field %s in %s%n", fldName, object.getClass().getSimpleName());
      return null;
    }
  }

  private boolean hasField(Class<?> clazz, String fldName) {
    Field[] flds = clazz.getDeclaredFields();
    Set<String> fldSet =
        (new HashSet<>(Arrays.asList(flds)))
            .stream().map(Field::getName).collect(Collectors.toSet());
    return fldSet.contains(fldName);
  }

  public void addObjectToList(Object object, List<?> targetList) throws Exception {
    if (object == null) {
      System.out.println("??");
    }
    java.lang.reflect.Method m = List.class.getDeclaredMethod("add", Object.class);
    m.setAccessible(true);
    m.invoke(targetList, object);
  }

  public Object getTargetList(Object object, String fieldName) throws Exception {
    Field f = List.class.getDeclaredField(fieldName);
    f.setAccessible(true);
    return f.get(object);
  }

  private boolean isEntityIndicator(int rowNr) {
    if (!this.allExcelValues.get(rowNr).isEmpty()) {
      return this.isEntityIndicator(this.allExcelValues.get(rowNr).get(0));
    } else {
      return false;
    }
  }

  private boolean isEntityIndicator(Cell cell) {
    if (CellType.STRING.equals(cell.getCellType())) {
      return this.entityTypes.stream().anyMatch(e -> e.getName().equals(cell.getStringCellValue()));
    } else {
      return false;
    }
  }

  private Class<?> getEntity(int rowNr) {
    String entityName = this.allExcelValues.get(rowNr).get(0).getStringCellValue();
    Optional<EntityMeta> entityOpt =
        this.entityTypes.stream().filter(e -> e.getName().equals(entityName)).findFirst();
    if (entityOpt.isPresent()) {
      return entityOpt.get().getClazz();
    } else {
      throw new RuntimeException("Excel entity " + entityName + " could not be mapped");
    }
  }

  public String getFieldName(Class<?> clazz, String name) {
    Optional<FieldMeta> fieldOpt =
        this.getFieldMetaList(clazz).stream().filter(e -> e.getName().equals(name)).findFirst();
    return fieldOpt.get().getFieldName();
  }

  // --- private methods to write template excel file
  private XSSFSheet createSheet(String sheetName) {
    if (this.workbook == null) {
      this.workbook = new XSSFWorkbook();
    }
    XSSFSheet sheet = this.workbook.createSheet(sheetName);
    for (EntityMeta entityMeta : this.entityTypes) {
      this.addRowHeaders(sheet, entityMeta);
    }
    return sheet;
  }

  private void addRowHeaders(XSSFSheet sheet, EntityMeta entityMeta) {
    XSSFRow row = sheet.createRow(this.rowNum);
    this.rowNum++;

    XSSFCell cell = row.createCell(0);
    cell.setCellStyle(this.getHeaderStyle());
    cell.setCellValue(entityMeta.getName());

    row = sheet.createRow(this.rowNum);
    int i = 0;
    for (FieldMeta fieldMeta : entityMeta.getFields()) {
      String name = this.getRowName(fieldMeta.getName());
      cell = row.createCell(i++);
      cell.setCellStyle(this.getHeaderStyle());
      cell.setCellValue(name);
    }
    this.rowNum++;
    this.rowNum++;
    this.rowNum++;
  }

  private CellStyle getHeaderStyle() {
    CellStyle style = this.workbook.createCellStyle();
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
    return style;
  }

  private String getRowName(String rowName) {
    String[] tokens = rowName.split(":");
    return tokens[0];
  }

  private int parseIntCell(Cell cell) {
    try {
      return ExcelUtility.getInteger(cell);
    } catch (IllegalStateException ex) {
      System.out.println("error : " + ex);
      return 0;
    }
  }

  private Double parseNumericCell(Cell cell) {
    try {
      Double value = ExcelUtility.getDouble(cell);
      return value;
    } catch (IllegalStateException ex) {
      System.out.println("TODO");
      return 0.0;
    }
  }

  private LocalDate parseLocalDateCell(Cell cell) {
    try {
      return ExcelUtility.getLocalDate(cell);
    } catch (Exception ex) {
      System.out.println(ex);
      return LocalDate.now();
    }
  }

  private String parseStringCell(Cell cell) {
    String s = ExcelUtility.getString(cell);
    return s.replace("\"", "");
  }

  private boolean parseBooleanCell(Cell cell) {
    return ExcelUtility.getBoolean(cell);
  }

  private int getColumIndex(String fieldName, Class<?> clazz) {
    Map<String, Integer> colMap = this.columsMap.get(clazz);
    if (colMap != null) {

      if (colMap.containsKey(fieldName)) {
        return colMap.get(fieldName);
      }
    }

    System.out.printf(
        "Could not find column-index for %s : %s%n", clazz.getSimpleName(), fieldName);
    return -1;
  }

  private boolean checkValue(Object expObj, Object checkObj, String fieldName) {
    if (!expObj.getClass().equals(checkObj.getClass())) {
      throw new RuntimeException(
          String.format(
              "Expected and check class dont match: %s %s",
              expObj.getClass().getSimpleName(), checkObj.getClass().getSimpleName()));
    }
    Class<?> clazz = expObj.getClass();

    Object expValue;
    Object checkValue;
    try {
      Field f = clazz.getDeclaredField(fieldName);
      f.setAccessible(true);
      expValue = f.get(expObj);
      checkValue = f.get(checkObj);

      if (f.getType().equals(Double.class)) {
        expValue = this.roundIt((Double) expValue);
        checkValue = this.roundIt((Double) checkValue);
      }

      if (expValue.equals(checkValue)) {
        return true;
      } else {
        System.out.printf(
            "Value for %s do not match in %s : %s != %s%n",
            fieldName, clazz.getSimpleName(), expValue, checkValue);
        return false;
      }
    } catch (Exception e) {
      throw new RuntimeException("Error during checkvalue of " + fieldName + ", " + e);
    }
  }

  private Double roundIt(Double inputValue) {
    return new BigDecimal(inputValue.toString()).setScale(8, RoundingMode.HALF_UP).doubleValue();
  }
}
