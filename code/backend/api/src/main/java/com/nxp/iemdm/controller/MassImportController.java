package com.nxp.iemdm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nxp.iemdm.model.search.GenericSearchArguments;
import com.nxp.iemdm.service.GenericSearchService;
import com.nxp.iemdm.service.LocationService;
import com.nxp.iemdm.service.MassImportService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import lombok.extern.java.Log;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
@RequestMapping("/api/massimport")
public class MassImportController {

  @Value("${app.environment}")
  private String environment;

  private static final int MAX_LIMITED = 10_000;
  private final LocationService locationService;
  private final MassImportService massImportService;
  private final GenericSearchService genericSearchService;

  public MassImportController(
      LocationService locationService,
      MassImportService massImportService,
      GenericSearchService genericSearchService) {

    this.locationService = locationService;
    this.massImportService = massImportService;
    this.genericSearchService = genericSearchService;
  }

  private static final ObjectMapper OBJECT_MAPPER =
      JsonMapper.builder().findAndAddModules().build();
  private static final String TEST = "TEST";
  private static final String PACKING = "PACKING";

  private static GenericSearchArguments decodeSearchArguments(String base64EncodedSearchArguments)
      throws JsonProcessingException {

    byte[] bytesDecoded = Base64.decodeBase64(base64EncodedSearchArguments);
    String bytesAsString = new String(bytesDecoded, StandardCharsets.UTF_8);
    return OBJECT_MAPPER.readValue(bytesAsString, GenericSearchArguments.class);
  }

  // -------------- private methods ----------------------

  private XSSFSheet getSheetContainingName(XSSFWorkbook workBook, String sheetName) {
    Iterator<Sheet> sheetIterator = workBook.sheetIterator();

    XSSFSheet sheet = null;

    while (sheetIterator.hasNext()) {
      Sheet next = sheetIterator.next();

      if (next.getSheetName().contains(sheetName)) {
        sheet = (XSSFSheet) next;
      }
    }

    if (sheet == null) {
      throw new IllegalArgumentException("Incorrect workbook received, missing sheet " + sheetName);
    }
    return sheet;
  }

  private String getCellValue(Row row, int column) {
    if (row.getCell(column) != null) {
      Cell cell = row.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
      if (cell.getCellType() != CellType.STRING) {
        cell.setCellType(CellType.STRING);
      }
      return row.getCell(column).getStringCellValue().strip();
    } else {
      return null;
    }
  }

  private LocalDate getCellDateValue(Row row, int column) {
    if (row.getCell(column) != null) {
      Cell cell = row.getCell(column, MissingCellPolicy.CREATE_NULL_AS_BLANK);
      if (cell.getCellType() == CellType.STRING) {
        return LocalDate.parse(Objects.requireNonNull(cell.getStringCellValue()));
      } else {
        Date date = cell.getDateCellValue();
        if (date != null) {
          return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
      }
    }
    return null;
  }

  /**
   * Rows are zero indexed, the view in Excel however isn't. For correct user feedback about what
   * the user has uploaded, we therefore add 1 to the current row number.
   */
  private int getExcelRowNumber(Row row) {
    return row.getRowNum() + 1;
  }

  // ---- static classes

  private static class ActivationFlowOverviewIndex {
    int uploadFlag = -1;
    int sapLocation = -1;
    int part12Nc = -1;
    int activationDate = -1;
    int priority1 = -1;
    int priority2 = -1;
    int priority3 = -1;
    int priority4 = -1;
    int priority5 = -1;
    int priority6 = -1;
    int priority7 = -1;
    int priority8 = -1;
  }

  private static class PlanningFlowMassUploadIndex {
    int planningFlowNameColumn = -1;
    int arasFlowNameColumn = -1;
    int arasStepNameColumn = -1;
    int arasStageNameColumn = -1;
  }
}
