package com.nxp.iemdm.shared.test.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public abstract class PostProcess {

  protected Object entity;
  protected FieldMeta fieldMeta;
  protected List<Cell> rowCells;
  protected Cell cell;
  protected BuildObjectsFromExcel builder;

  public void process(
      Object entity,
      FieldMeta fieldMeta,
      List<Cell> rowCells,
      Cell cell,
      BuildObjectsFromExcel builder)
      throws Exception {

    this.entity = entity;
    this.fieldMeta = fieldMeta;
    this.rowCells = rowCells;
    this.cell = cell;
    this.builder = builder;
  }

  protected String getStrValue(int colIndex) {
    Cell cell = this.rowCells.get(colIndex);
    return cell.getStringCellValue();
  }

  protected int getIntValue(int colIndex) {
    Cell cell = this.rowCells.get(colIndex);
    return Double.valueOf(cell.getNumericCellValue()).intValue();
  }

  protected List<Integer> getNestedIds(Cell cell) {
    List<Integer> ids = new ArrayList<>();

    if (CellType.NUMERIC.equals(cell.getCellType())) {
      ids.add(Double.valueOf(cell.getNumericCellValue()).intValue());
    } else {
      String[] strIds = (cell.getStringCellValue() + ",").split(",");
      for (String strid : strIds) {
        ids.add(Integer.parseInt(strid));
      }
    }

    return ids;
  }

  protected Object findObjectColIndex(Class<?> clazz, int colIndex) {
    String name = this.getName(clazz, colIndex);
    Integer id = this.getIntValue(colIndex);
    List<Object> objects = this.builder.getEntityList(clazz);
    Optional<Object> objOpt = objects.stream().filter(o -> isMatchingId(o, name, id)).findFirst();
    return objOpt.orElse(null);
  }

  protected List<Object> getEntityList(Class<?> clazz) {
    return this.builder.getEntityList(clazz);
  }

  protected boolean isMatchingId(Object object, String name, Object id) {
    try {
      Class<?> clazz = object.getClass();
      String fieldName = this.getFieldName(clazz, name);
      Field f = clazz.getDeclaredField(fieldName);
      f.setAccessible(true);
      Object objValue = f.get(object);
      return objValue.equals(id);
    } catch (Exception ex) {
      throw new RuntimeException("could not get id for " + name + " ," + ex);
    }
  }

  protected String getFieldName(Class<?> clazz, String name) {
    return this.builder.getFieldName(clazz, name);
  }

  protected String getName(Class<?> clazz, int colIndex) {
    return this.builder.getFieldMetaList(clazz).get(colIndex).getName();
  }
}
