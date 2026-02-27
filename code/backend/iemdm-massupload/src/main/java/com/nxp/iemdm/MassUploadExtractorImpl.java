package com.nxp.iemdm;

import com.nxp.iemdm.model.massupload.MassUploadAction;
import com.nxp.iemdm.model.massupload.MassUploadExtract;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.shared.intf.massupload.MassUploadColumn;
import com.nxp.iemdm.shared.intf.massupload.MassUploadConfiguration;
import com.nxp.iemdm.shared.intf.massupload.MassUploadExtractor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class MassUploadExtractorImpl implements MassUploadExtractor {

  private final MassUploadConfiguration massUploadConfiguration;
  private final MassUploadResponse massUploadResponse;
  private final ProgressReportUtility progressReportUtility;
  private final DataFormatter dataFormatter = new DataFormatter();

  public MassUploadExtractorImpl(
      MassUploadConfiguration massUploadConfiguration, MassUploadResponse massUploadResponse) {
    this(massUploadConfiguration, massUploadResponse, null);
  }

  public MassUploadExtractorImpl(
      MassUploadConfiguration massUploadConfiguration,
      MassUploadResponse massUploadResponse,
      ProgressReportUtility progressReportUtility) {
    this.massUploadConfiguration = massUploadConfiguration;
    this.massUploadResponse = massUploadResponse;
    this.progressReportUtility = progressReportUtility;
  }

  @Override
  public Collection<MassUploadExtract> extract(XSSFSheet sheet) {
    Collection<MassUploadExtract> entities = new ArrayList<>();

    int actionIndex = this.massUploadConfiguration.getIndexOfMassUploadAction();
    int lastRowNumber = sheet.getLastRowNum();
    int progressReportInterval =
        Math.max(lastRowNumber / ProgressReportUtility.PROGRESS_REPORT_INSTANCES, 1);

    for (int rowIndex = 1; rowIndex < sheet.getLastRowNum() + 1; rowIndex++) { // Skip header row
      boolean shouldRowBeAdded = true;
      XSSFRow row = sheet.getRow(rowIndex);

      if (row == null
          || row.getLastCellNum() == -1
          || this.massUploadConfiguration.rowShouldBeSkipped(rowIndex)) {
        continue;
      }

      var uploadAction = this.getMassUploadAction(row, actionIndex);
      if (uploadAction == null) {
        continue;
      }

      MassUploadExtract extract = new MassUploadExtract(uploadAction, rowIndex);

      for (MassUploadColumn massUploadColumn :
          Stream.concat(
                  this.massUploadConfiguration.getFilteredRequiredColumns().stream(),
                  this.massUploadConfiguration.getFilteredOptionalColumns().stream())
              .toList()) {
        XSSFCell cell = row.getCell(massUploadColumn.getIndex());

        // Cell can be null in the case of optional columns that have not been filled in
        if (cell == null || cell.getCellType() == CellType.BLANK) {
          continue;
        }

        Object result = parseCellContent(cell, massUploadColumn);

        // In the case that any required column has a null value, because of a parse error
        // that means that we are not interested in transforming the resulting entity. Therefore,
        // we will not add it to the list.
        if (result == null && massUploadColumn.isRequired()) {
          shouldRowBeAdded = false;
        }
        extract.put(massUploadColumn.getName(), result);
      }
      // Should not add if any required cells have null values.
      if (shouldRowBeAdded) {
        entities.add(extract);
      }

      if (this.progressReportUtility != null
          && (rowIndex % progressReportInterval == 0 || rowIndex == lastRowNumber)) {
        this.progressReportUtility.reportProgress(
            rowIndex,
            lastRowNumber,
            ProgressReportUtility.EXTRACT_OFFSET,
            this.massUploadConfiguration.getWbi());
      }
    }
    return entities;
  }

  private MassUploadAction getMassUploadAction(XSSFRow row, int actionIndex) {
    // Special case, if UPLOAD_ACTION column is not required
    if (actionIndex == -1) {
      return MassUploadAction.ADD_ONLY;
    }

    MassUploadAction massUploadAction;
    String action = row.getCell(actionIndex).getStringCellValue().toUpperCase();

    massUploadAction =
        switch (action) {
          case "ADD_OR_CHANGE" -> MassUploadAction.ADD_OR_CHANGE;
          case "ADD_ONLY" -> MassUploadAction.ADD_ONLY;
          case "CHANGE_ONLY" -> MassUploadAction.CHANGE_ONLY;
          case "DELETE" -> MassUploadAction.DELETE;
          default -> {
            this.massUploadResponse.appendErrorMessage(
                String.format(
                    "Could not determine which action should be performed for row '%d'.",
                    row.getCell(actionIndex).getRowIndex()),
                row.getRowNum());
            yield null;
          }
        };
    return massUploadAction;
  }

  private Object parseCellContent(XSSFCell cell, MassUploadColumn massUploadColumn) {
    Object value;
    Class<?> expectedType = massUploadColumn.getType();

    if (expectedType.equals(Integer.class)) {
      cell.setCellType(CellType.STRING);
      value = this.tryGetValue(cell, String.class);
      try {
        value = Integer.parseInt((String) value);
      } catch (NumberFormatException numberFormatException) {
        this.massUploadResponse.appendErrorMessage(
            this.createErrorMessage(cell, massUploadColumn), cell.getRowIndex());
        value = null; // Because we could not parse it to int, something is wrong with the value
      }
    } else if (expectedType.equals(Double.class)) {
      value = this.tryGetValue(cell, expectedType);
      if (value == null) {
        cell.setCellType(CellType.STRING);
        value = this.tryGetValue(cell, String.class);

        if (value != null && ((String) value).contains(",")) {
          this.massUploadResponse.appendErrorMessage(
              "Excel cells of type Text are not allowed to contain commas", cell.getRowIndex());
          value = null;
        } else {
          try {
            assert value != null;
            value = Double.parseDouble((String) value);
          } catch (Exception exception) {
            this.massUploadResponse.appendErrorMessage(
                this.createErrorMessage(cell, massUploadColumn), cell.getRowIndex());
            value =
                null; // Because we could not parse it to double, something is wrong with the value
          }
        }
      }
    } else if (expectedType.equals(LocalDate.class)) {
      // The input might be text in proper format (yyyy-MM-dd)
      value = tryGetValue(cell, expectedType);

      if (value == null) {
        value = this.tryGetLocalDateFromExcelEpoch(cell);
      }
      // If the value is null after all these tries, then we must conclude that the input is
      // incorrect.
      if (value == null) {
        this.massUploadResponse.appendErrorMessage(
            String.format(
                "Cell %s:Rejected: Date/Time required format (e.g. 2022-12-01).",
                cell.getAddress()),
            cell.getRowIndex());
      }
    } else if (expectedType.equals(BigDecimal.class)) {
      value = this.tryGetValue(cell, expectedType);
      try {
        value = new BigDecimal((String) value);
      } catch (NumberFormatException numberFormatException) {
        this.massUploadResponse.appendErrorMessage(
            this.createErrorMessage(cell, massUploadColumn), cell.getRowIndex());
        value =
            null; // Because we could not parse it to Big Decimal, something is wrong with the value
      }
    } else {
      cell.setCellType(CellType.STRING);
      value = this.tryGetValue(cell, expectedType);
      if (value == null) {
        this.massUploadResponse.appendErrorMessage(
            this.createErrorMessage(cell, massUploadColumn), cell.getRowIndex());
      }
    }

    return value;
  }

  private Object tryGetValue(XSSFCell cell, Class<?> type) {
    Object value;
    if (type.equals(String.class)) {
      try {
        value = cell.getStringCellValue().trim();
      } catch (Exception ignored) {
        value = null;
      }

    } else if (type.equals(Double.class)) {
      try {
        value = cell.getNumericCellValue();
      } catch (Exception ignored) {
        value = null;
      }
    } else if (type.equals(LocalDate.class)) {
      try {
        value = LocalDate.parse(cell.getStringCellValue());
      } catch (Exception ignored) {
        value = null;
      }
    } else if (type.equals(BigDecimal.class)) {
      try {
        value = dataFormatter.formatCellValue(cell);
      } catch (Exception ignored) {
        value = null;
      }
    } else {
      value = null;
    }

    return value;
  }

  private LocalDate tryGetLocalDateFromExcelEpoch(XSSFCell cell) {
    LocalDate value = null;

    try {
      double days = cell.getNumericCellValue();
      Date utilDate = DateUtil.getJavaDate(days); // POI DateUtil class
      value = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    } catch (Exception ignored) {
    }

    return value;
  }

  private String createErrorMessage(XSSFCell cell, MassUploadColumn massUploadColumn) {
    return String.format(
        "Could not parse cell content of cell at position %s. Expected type of content to be '%s'",
        cell.getAddress().formatAsString(), massUploadColumn.getType().getSimpleName());
  }
}
