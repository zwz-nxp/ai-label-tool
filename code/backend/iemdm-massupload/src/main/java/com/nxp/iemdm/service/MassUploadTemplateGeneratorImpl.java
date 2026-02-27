package com.nxp.iemdm.service;

import static com.nxp.iemdm.shared.utility.ConsParamsFormatter.parseValue;

import com.nxp.iemdm.model.massupload.MassUploadType;
import com.nxp.iemdm.shared.aop.annotations.MethodJobLog;
import com.nxp.iemdm.shared.intf.massupload.MassUploadConfiguration;
import com.nxp.iemdm.shared.intf.massupload.MassUploadTemplateGenerator;
import com.nxp.iemdm.shared.utility.ExcelUtility;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MassUploadTemplateGeneratorImpl implements MassUploadTemplateGenerator {
  private static final String BASE_PATH = "readme/";
  private static final String READ_ME_SHEET_NAME = "ReadMe";

  @MethodJobLog
  public byte[] generateMassUploadTemplate(MassUploadType massUploadType) {
    return this.workbookToBytes(this.generateMassUploadTemplateWorkbook(massUploadType));
  }

  @MethodJobLog
  public <T> byte[] generateMassUploadTemplate(
      MassUploadType massUploadType, Collection<T> allData) {
    log.info(
        String.format(
            "Template download called with '%d' records for mu type '%s'.",
            allData.size(), massUploadType));
    return this.workbookToBytes(this.generateMassUploadTemplateWorkbook(massUploadType, allData));
  }

  public XSSFWorkbook generateMassUploadTemplateWorkbook(MassUploadType massUploadType) {
    var configuration = new MassUploadConfiguration(massUploadType);
    var workbook = new XSSFWorkbook();

    // Styling
    DataFormat dataFormat = workbook.createDataFormat();
    XSSFCellStyle defaultStyle = this.createDefaultStyle(workbook, dataFormat);

    XSSFSheet readmeSheet = workbook.createSheet(READ_ME_SHEET_NAME);
    XSSFSheet templateSheet = workbook.createSheet(massUploadType.getSheetNameIncludingVersion());

    List<String> readMeTextLines =
        this.readMeTextFromFile(configuration.massUploadType.readMeFileName);
    this.addTextToReadMeSheet(readmeSheet, readMeTextLines, configuration, defaultStyle);
    ExcelUtility.autoSizeColumns(readmeSheet);

    this.addColumnsToTemplateSheet(
        templateSheet, configuration.getRequiredAndOptionalColumns(), defaultStyle);
    ExcelUtility.autoSizeColumns(templateSheet);

    return workbook;
  }

  public <T> XSSFWorkbook generateMassUploadTemplateWorkbook(
      MassUploadType massUploadType, Collection<T> allData) {
    var configuration = new MassUploadConfiguration(massUploadType);
    var workbook = new XSSFWorkbook();

    // Styling
    DataFormat dataFormat = workbook.createDataFormat();
    XSSFCellStyle defaultStyle = this.createDefaultStyle(workbook, dataFormat);

    XSSFSheet readmeSheet = workbook.createSheet(READ_ME_SHEET_NAME);
    XSSFSheet templateSheet = workbook.createSheet(massUploadType.getSheetNameIncludingVersion());

    // Add content to readme sheet
    List<String> readMeTextLines =
        this.readMeTextFromFile(configuration.massUploadType.readMeFileName);
    this.addTextToReadMeSheet(readmeSheet, readMeTextLines, configuration, defaultStyle);
    ExcelUtility.autoSizeColumns(readmeSheet);

    // Add content to template sheet
    this.addColumnsToTemplateSheet(templateSheet, configuration.getAllColumns(), defaultStyle);
    this.addDataToSheet(
        massUploadType, templateSheet, configuration.getAllColumns(), allData, defaultStyle);
    ExcelUtility.autoSizeColumns(templateSheet);

    return workbook;
  }

  private XSSFCellStyle createDefaultStyle(XSSFWorkbook workbook, DataFormat dataFormat) {
    XSSFCellStyle defaultStyle = workbook.createCellStyle();
    defaultStyle.setDataFormat(dataFormat.getFormat("@"));
    return defaultStyle;
  }

  private byte[] workbookToBytes(XSSFWorkbook workbook) {
    var outputStream = new ByteArrayOutputStream();
    try {
      workbook.write(outputStream);
    } catch (IOException ioException) {
      log.error("Could not write workbook to ByteArrayOutPutStream.", ioException);
    }

    try {
      workbook.close();
    } catch (IOException ioException) {
      log.error("Could not close workbook used to generate mass upload template.", ioException);
    }

    byte[] templateAsBytes = outputStream.toByteArray();
    try {
      outputStream.close();
    } catch (IOException ioException) {
      log.error("Could not close outputStream of generated template.", ioException);
    }

    return templateAsBytes;
  }

  private void addColumnsToTemplateSheet(
      XSSFSheet templateSheet, Collection<String> columns, XSSFCellStyle defaultStyle) {
    int counter = 0;
    var row = templateSheet.createRow(0);

    for (String columnName : columns) {
      this.createCell(row, counter, columnName, defaultStyle);
      templateSheet.setDefaultColumnStyle(counter, defaultStyle);
      counter++;
    }
  }

  private void addTextToReadMeSheet(
      XSSFSheet sheet,
      List<String> readMeTextLines,
      MassUploadConfiguration config,
      XSSFCellStyle defaultStyle) {
    int counter = 0;

    for (var line : readMeTextLines) {
      var row = sheet.createRow(counter);
      var cell = row.createCell(0);
      cell.setCellValue(line);
      cell.setCellStyle(defaultStyle);

      counter++;
    }
  }

  private List<String> readMeTextFromFile(String readmeFileName) {
    String templatePath = BASE_PATH + readmeFileName;
    List<String> lines;

    try (var inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath)) {
      if (inputStream == null) {
        throw new NullPointerException();
      }
      lines =
          new Scanner(inputStream)
              .useDelimiter("\n").tokens().collect(Collectors.toList()).stream()
                  .map(String::trim)
                  .collect(Collectors.toList());
    } catch (IOException ioException) {
      log.error("Cannot access read me file as stream.", ioException);
      lines = new ArrayList<>();
    } catch (NullPointerException nullPointerException) {
      log.error("Could not close inputStream for read me file.", nullPointerException);
      lines = new ArrayList<>();
    }

    return lines;
  }

  private <T> void addDataToSheet(
      MassUploadType massUploadType,
      XSSFSheet sheet,
      Collection<String> allColumns,
      Collection<T> allData,
      XSSFCellStyle defaultStyle) {

    switch (massUploadType) {
      default:
        break;
    }
  }

  private void createCell(Row row, int columnIndex, String data, XSSFCellStyle defaultStyle) {
    Cell cell = row.createCell(columnIndex);
    cell.setCellStyle(defaultStyle);
    cell.setCellType(CellType.STRING);
    cell.setCellValue(parseValue(data));
  }
}
