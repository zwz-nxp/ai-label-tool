package com.nxp.iemdm.shared.utility;

import com.nxp.iemdm.exception.MassUploadInvalidExcelFileException;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.model.massupload.MassUploadType;
import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtility {
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final DateFormat SIMPLE_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
  public static final String VERSION_DELIMITER = "_v";

  private ExcelUtility() {}

  public static List<Cell> getRowCells(Row row) {
    List<Cell> cells = new ArrayList<>();
    for (Cell cell : row) {
      cells.add(cell);
    }

    return cells;
  }

  /**
   * @param colIndex is zero-based
   */
  public static String getString(List<Cell> rowCells, int colIndex) {
    Cell cell = rowCells.get(colIndex);
    return getString(cell);
  }

  public static String getString(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }

    if (cell.getCellType() == CellType.STRING) {
      return cell.getStringCellValue();
    } else if (cell.getCellType() == CellType.NUMERIC) {
      long longValue = (long) cell.getNumericCellValue();
      return "" + longValue;
    } else {
      return "???";
    }
  }

  public static LocalDate getLocalDate(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }

    if (cell.getCellType() == CellType.STRING) {
      String str = cell.getStringCellValue();
      if (str.isBlank()) {
        return null;
      } else {
        return LocalDate.parse(str, DATE_FORMATTER);
      }
    } else {
      Date date = cell.getDateCellValue();
      return LocalDate.parse(SIMPLE_DATEFORMAT.format(date));
    }
  }

  /**
   * @param colIndex is zero-based
   */
  public static Double getDouble(List<Cell> rowCells, int colIndex) {
    Cell cell = rowCells.get(colIndex);
    return getDouble(cell);
  }

  public static Double getDouble(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }

    if (cell.getCellType() == CellType.STRING) {
      String str = cell.getStringCellValue();
      if (str.isBlank()) {
        return null;
      } else {
        return Double.valueOf(str);
      }
    } else {
      return cell.getNumericCellValue();
    }
  }

  public static Integer getInteger(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return null;
    }

    if (cell.getCellType() == CellType.STRING) {
      String str = cell.getStringCellValue();
      return Integer.valueOf(str);
    } else {
      return (int) cell.getNumericCellValue();
    }
  }

  /**
   * If cell is empty or null then false
   *
   * @param colIndex, zero based.
   */
  public static boolean getBoolean(List<Cell> rowCells, int colIndex) {
    Cell cell = rowCells.get(colIndex);
    return getBoolean(cell);
  }

  public static boolean getBoolean(Cell cell) {
    if (cell == null || cell.getCellType() == CellType.BLANK) {
      return false;
    }

    if (cell.getCellType() == CellType.STRING) {
      String str = cell.getStringCellValue().strip();
      return str.toUpperCase().startsWith("Y");
    } else if (cell.getCellType() == CellType.BOOLEAN) {
      return cell.getBooleanCellValue();
    } else {
      return false;
    }
  }

  public static XSSFWorkbook constructWorkbook(byte[] excelFile)
      throws MassUploadInvalidExcelFileException {

    if (excelFile == null || excelFile.length < 1) {
      throw new MassUploadInvalidExcelFileException("Supplied file is not allowed to be empty");
    }

    return constructWorkbook(new ByteArrayInputStream(excelFile));
  }

  public static XSSFWorkbook constructWorkbook(ByteArrayInputStream excelFile)
      throws MassUploadInvalidExcelFileException {
    XSSFWorkbook workbook;
    try {
      workbook = new XSSFWorkbook(excelFile);
    } catch (Exception e) {
      throw new MassUploadInvalidExcelFileException(
          "Selected file must be an excel file (.xlsx, .xlsm)", e);
    }

    return workbook;
  }

  public static void verifyVersion(String sheetName, MassUploadType massUploadType)
      throws MassUploadInvalidExcelFileException {
    // Using floor, because we consider only the first number when comparing. So, if we are on
    // version 2.5 and the user supplies a mass upload file that has the version 2.2, then we
    // consider that file ok. On the other hand, if they supply us with a file that is on 1.6,
    // then we reject it.
    double currentMajorVersion = Math.floor(massUploadType.version);
    double uploadedSheetMajorVersion = getMajorSheetVersion(sheetName);

    if (currentMajorVersion != uploadedSheetMajorVersion) {
      DecimalFormat df = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
      throw new MassUploadInvalidExcelFileException(
          String.format(
              "Rejected: The selected file is on version %s and is not currently supported. Latest version is %s.",
              df.format(uploadedSheetMajorVersion), df.format(massUploadType.version)));
    }
  }

  public static XSSFSheet getSheet(XSSFWorkbook workbook, String sheetName) {
    XSSFSheet sheet;
    sheet = workbook.getSheet(sheetName);
    if (sheet == null) {
      throw new MassUploadInvalidExcelFileException(
          String.format("Sheet with name '%s' must be present in the excel file.", sheetName));
    }

    return sheet;
  }

  public static XSSFSheet getSheetAtIndex(XSSFWorkbook workbook, int index) {
    XSSFSheet sheet;
    sheet = workbook.getSheetAt(index);

    return sheet;
  }

  public static XSSFSheet getSheet(XSSFWorkbook workbook, MassUploadType massUploadType) {
    XSSFSheet sheet;
    sheet = workbook.getSheet(massUploadType.getSheetNameIncludingVersion());
    if (sheet != null) {
      return sheet;
    }

    for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
      sheet = workbook.getSheetAt(index);
      if (sheet.getSheetName().contains(massUploadType.sheetName)) {
        return sheet;
      }
    }

    throw new MassUploadInvalidExcelFileException(
        String.format(
            "Sheet with name '%s' must be present in the excel file.",
            massUploadType.getSheetNameIncludingVersion()));
  }

  public static boolean isRowEmpty(Row row) {
    for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
      Cell cell = row.getCell(cellIndex);
      if (cell != null && cell.getCellType() != CellType.BLANK) return false;
    }
    return true;
  }

  public static void autoSizeColumns(XSSFSheet sheet) {
    Row firstRow = sheet.getRow(0);

    if (firstRow != null) {
      for (int i = 0; i < firstRow.getLastCellNum(); i++) {
        sheet.autoSizeColumn(i);
      }
    }
  }

  public static void addMassUploadSummaryToEmptySheet(
      XSSFSheet emptySheet, MassUploadResponse massUploadResponse) {
    Row row1 = emptySheet.createRow(0);

    row1.createCell(0).setCellValue("Successfully processed: ");
    row1.createCell(1).setCellValue(getText(massUploadResponse.getSuccessCount()));

    Row row2 = emptySheet.createRow(1);

    row2.createCell(0).setCellValue("Inserted: ");
    row2.createCell(1).setCellValue(getText(massUploadResponse.getInsertCount()));

    Row row3 = emptySheet.createRow(2);

    row3.createCell(0).setCellValue("Updated: ");
    row3.createCell(1).setCellValue(getText(massUploadResponse.getUpdateCount()));

    Row row4 = emptySheet.createRow(3);

    row4.createCell(0).setCellValue("Duplicates: ");
    row4.createCell(1).setCellValue(getText(massUploadResponse.getDuplicateCount()));

    Row row5 = emptySheet.createRow(4);

    row5.createCell(0).setCellValue("Ignored: ");
    row5.createCell(1).setCellValue(getText(massUploadResponse.getIgnoreCount()));

    Row row6 = emptySheet.createRow(5);
    row6.createCell(0).setCellValue("Deleted: ");
    row6.createCell(1).setCellValue(getText(massUploadResponse.getDeleteCount()));
  }

  private static String getText(int number) {
    String singularOrPlural = number == 1 ? "record" : "records";
    return String.format("%d %s", number, singularOrPlural);
  }

  public static void addMassUploadResponseErrorMessagesToSheet(
      XSSFSheet sheet, MassUploadResponse massUploadResponse) {

    // Find last column on a sheet basis.
    int sheetLastCellNumber = 0;
    for (int i = 0; i < sheet.getLastRowNum(); i++) {
      XSSFRow row = sheet.getRow(i);
      if (row != null && row.getLastCellNum() > sheetLastCellNumber) {
        sheetLastCellNumber = row.getLastCellNum();
      }
    }

    int successIndex = sheetLastCellNumber;
    int firstCellIndex = 0;
    int warningAndErrorMessageIndex = sheetLastCellNumber + 1;

    // Create headers for success and error messages
    Row firstRow = sheet.getRow(0);

    Cell successHeader = firstRow.createCell(successIndex);
    Cell errorMessageHeader = firstRow.createCell(warningAndErrorMessageIndex);
    successHeader.setCellValue("STATUS");
    errorMessageHeader.setCellValue("ERRORS");

    // Add success status for each row. Later, when we put error messages for rows, we will
    // overwrite the success message with 'Error'. By default, a row is considered a success.
    for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
      Row row = sheet.getRow(i);
      if (row != null && !ExcelUtility.isRowEmpty(row)) {
        Cell cell = row.getCell(successIndex);
        Cell firstCell = row.getCell(firstCellIndex);

        if (cell == null) {
          cell = row.createCell(successIndex);
        }
        if (firstCell != null && "N".equalsIgnoreCase(firstCell.getStringCellValue())) {
          cell.setCellValue("Ignored");
        } else {
          cell.setCellValue("Success");
        }
      }
    }

    // Add warning messages to rows
    for (int i = 0; i < massUploadResponse.getWarningCount(); i++) {
      int index = massUploadResponse.getRowNumberOfWarningMessages().get(i);
      Row row = sheet.getRow(index);

      Cell cell = row.getCell(warningAndErrorMessageIndex);
      if (cell == null) {
        cell = row.createCell(warningAndErrorMessageIndex);
        row.getCell(successIndex).setCellValue("Warning");
      }
      if (cell.getStringCellValue().isEmpty()) {
        cell.setCellValue(massUploadResponse.getWarningMessages().get(i));

      } else {
        cell.setCellValue(
            cell.getStringCellValue() + " " + massUploadResponse.getWarningMessages().get(i));
      }
    }

    // Add error messages to rows
    for (int i = 0; i < massUploadResponse.getErrorCount(); i++) {
      int index = massUploadResponse.getRowNumberOfErrorMessages().get(i);
      Row row = sheet.getRow(index);

      Cell cell = row.getCell(warningAndErrorMessageIndex);
      if (cell == null) {
        cell = row.createCell(warningAndErrorMessageIndex);
      }
      if (cell.getStringCellValue().isEmpty()) {
        cell.setCellValue(massUploadResponse.getErrorMessages().get(i));

      } else {
        // We might have multiple error messages for the same row, for those we append them to the
        // old content
        cell.setCellValue(
            cell.getStringCellValue() + " " + massUploadResponse.getErrorMessages().get(i));
      }
      row.getCell(successIndex).setCellValue("Error");
    }
  }

  private static double getMajorSheetVersion(String sheetName) {
    double majorSheetVersion;
    try {
      String substringSheetVersion =
          sheetName.substring(
              sheetName.lastIndexOf(ExcelUtility.VERSION_DELIMITER)
                  + ExcelUtility.VERSION_DELIMITER.length());

      double sheetVersion = Double.parseDouble(substringSheetVersion);
      majorSheetVersion = Math.floor(sheetVersion);
    } catch (Exception exception) {
      throw new MassUploadInvalidExcelFileException(
          "Rejected: Cannot read version information of the selected file. "
              + "Download a new template to resolve this issue.",
          exception);
    }
    return majorSheetVersion;
  }
}
