package com.nxp.iemdm.service;

import com.nxp.iemdm.MassUploadExtractorImpl;
import com.nxp.iemdm.MassUploadVerifierImpl;
import com.nxp.iemdm.ProgressReportUtility;
import com.nxp.iemdm.exception.BadRequestException;
import com.nxp.iemdm.exception.MassUploadInvalidExcelFileException;
import com.nxp.iemdm.model.massupload.MassUploadExtract;
import com.nxp.iemdm.model.massupload.MassUploadResponse;
import com.nxp.iemdm.model.massupload.MassUploadType;
import com.nxp.iemdm.model.user.Person;
import com.nxp.iemdm.shared.intf.massupload.Loader;
import com.nxp.iemdm.shared.intf.massupload.MassUploadConfiguration;
import com.nxp.iemdm.shared.intf.massupload.MassUploadExtractor;
import com.nxp.iemdm.shared.intf.massupload.MassUploadHandler;
import com.nxp.iemdm.shared.intf.massupload.MassUploadVerifier;
import com.nxp.iemdm.shared.intf.massupload.Transformer;
import com.nxp.iemdm.shared.intf.notification.MailService;
import com.nxp.iemdm.shared.intf.operational.PersonService;
import com.nxp.iemdm.shared.utility.ExcelUtility;
import jakarta.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class MassUploadHandlerImpl implements MassUploadHandler {

  private final MailService mailService;
  private final PersonService personService;
  private final ProgressReportUtility progressReportUtility;
  private final ApplicationContext applicationContext;

  public MassUploadHandlerImpl(
      MailService mailService,
      PersonService personService,
      ProgressReportUtility progressReportUtility,
      ApplicationContext applicationContext) {
    this.mailService = mailService;
    this.personService = personService;
    this.progressReportUtility = progressReportUtility;
    this.applicationContext = applicationContext;
  }

  public MassUploadResponse verifyMassUpload(
      MultipartFile file, String wbi, MassUploadType massUploadType)
      throws MassUploadInvalidExcelFileException {
    byte[] fileContent = getBytes(file);

    XSSFWorkbook workbook = ExcelUtility.constructWorkbook(fileContent);
    XSSFSheet sheet = ExcelUtility.getSheet(workbook, massUploadType);
    try {
      ExcelUtility.verifyVersion(sheet.getSheetName(), massUploadType);
    } catch (MassUploadInvalidExcelFileException e) {
      MassUploadResponse response = new MassUploadResponse();
      response.appendErrorMessage(e.getMessage());
      return response;
    }

    MassUploadConfiguration config = new MassUploadConfiguration(massUploadType, wbi);
    MassUploadResponse response = new MassUploadResponse();

    MassUploadVerifier verifier = new MassUploadVerifierImpl(config, response);
    try {
      verifier.verify(sheet);
    } finally {
      try {
        workbook.close();
      } catch (IOException ioException) {
        log.error(
            String.format("Could not close excel workbook. MU type '%s'.", massUploadType),
            ioException);
      }
    }

    return response;
  }

  public MassUploadResponse processMassUpload(
      MultipartFile file, String wbi, boolean sendEmail, MassUploadType massUploadType)
      throws MassUploadInvalidExcelFileException {
    Transformer transformer = getTransformer(massUploadType);
    Loader loader = getLoader(massUploadType);

    byte[] fileContent = getBytes(file);

    XSSFWorkbook workbook = ExcelUtility.constructWorkbook(fileContent);
    XSSFSheet sheet = ExcelUtility.getSheet(workbook, massUploadType);

    MassUploadConfiguration config = new MassUploadConfiguration(massUploadType, wbi);
    MassUploadResponse response = new MassUploadResponse();

    MassUploadVerifier verifier = new MassUploadVerifierImpl(config, response);
    MassUploadExtractor extractor =
        new MassUploadExtractorImpl(config, response, progressReportUtility);

    try {
      // V-ETL
      verifier.verify(sheet);
      if (response.getErrorCount() == 0) {
        Collection<MassUploadExtract> result = extractor.extract(sheet);
        result = transformer.transform(result, response, config);
        loader.load(result, response, config.getWbi());
      }

      if (sendEmail) {
        this.sendMail(wbi, workbook, sheet, response, massUploadType);
      }
    } finally {
      try {
        workbook.close();
      } catch (IOException ioException) {
        log.error(
            String.format("Could not close excel workbook. MU type '%s'.", massUploadType),
            ioException);
      }
    }

    return response;
  }

  public byte[] getResultsAsFile(
      MultipartFile file, MassUploadResponse response, MassUploadType massUploadType) {

    byte[] fileContent = getBytes(file);

    XSSFWorkbook workbook = ExcelUtility.constructWorkbook(fileContent);
    XSSFSheet originalSheet = ExcelUtility.getSheet(workbook, massUploadType);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
    String formattedInstant = formatter.format(LocalDateTime.now());

    XSSFSheet summarySheet = workbook.createSheet(formattedInstant);
    ExcelUtility.addMassUploadSummaryToEmptySheet(summarySheet, response);
    ExcelUtility.autoSizeColumns(summarySheet);

    ExcelUtility.addMassUploadResponseErrorMessagesToSheet(originalSheet, response);
    ExcelUtility.autoSizeColumns(originalSheet);

    var workbookContent = new ByteArrayOutputStream();
    try {
      workbook.write(workbookContent);
    } catch (IOException ioException) {
      log.error("Could not write mutated workbook to a byteArrayOutPutStream ", ioException);
    }

    try {
      workbookContent.close();
    } catch (IOException ioException) {
      log.error("Could not close ByteArrayOutputStream.", ioException);
    }

    byte[] resultsAsBytes = workbookContent.toByteArray();
    try {
      workbookContent.close();
    } catch (IOException ioException) {
      log.error("Could not close outputStream of generated file.", ioException);
    }

    return resultsAsBytes;
  }

  private static byte[] getBytes(MultipartFile file) {
    byte[] fileContent;
    try {
      fileContent = file.getBytes();
    } catch (IOException ioException) {
      throw new MassUploadInvalidExcelFileException("Could not access file content.");
    }
    return fileContent;
  }

  private Transformer getTransformer(MassUploadType massUploadType) {
    Transformer transformer;
    try {
      transformer =
          (Transformer)
              applicationContext.getBean(
                  String.format("%sTransformer", massUploadType.massUploadTypeName));
    } catch (BeansException beansException) {
      // If this occurs it is usually because the naming specified in the mass upload type does not
      // match the transformer. This also applies for the loader.
      log.error(
          String.format(
              "Could not get transformer from the application context. MU type: '%s'.",
              massUploadType),
          beansException);
      throw new BadRequestException(
          "If you see this message please report an incident in ServiceNow");
    }
    return transformer;
  }

  private Loader getLoader(MassUploadType massUploadType) {
    Loader loader;
    try {
      loader =
          (Loader)
              applicationContext.getBean(
                  String.format("%sLoader", massUploadType.massUploadTypeName));
    } catch (BeansException beansException) {
      log.error(
          String.format(
              "Could not get loader from the application context. MU type: '%s'", massUploadType),
          beansException);
      throw new BadRequestException(
          "If you see this message please report an incident in ServiceNow");
    }
    return loader;
  }

  private void sendMail(
      String wbi,
      XSSFWorkbook workbook,
      XSSFSheet originalSheet,
      MassUploadResponse response,
      MassUploadType massUploadType) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
    String formattedInstant = formatter.format(LocalDateTime.now());

    XSSFSheet summarySheet = workbook.createSheet(formattedInstant);
    ExcelUtility.addMassUploadSummaryToEmptySheet(summarySheet, response);
    ExcelUtility.autoSizeColumns(summarySheet);

    ExcelUtility.addMassUploadResponseErrorMessagesToSheet(originalSheet, response);
    ExcelUtility.autoSizeColumns(originalSheet);

    Person person = this.personService.getPersonByWBI(wbi, false);

    String body =
        String.format(
            "Dear %s This mail contains an attachment with feedback of a mass upload for %s.",
            person.getName(), massUploadType);
    Map<String, byte[]> attachments = new HashMap<>();

    var workbookContent = new ByteArrayOutputStream();
    try {
      workbook.write(workbookContent);
    } catch (IOException ioException) {
      log.error(
          String.format(
              "Could not write mutated workbook to a byteArrayOutPutStream for MU type '%s'.",
              massUploadType),
          ioException);
    }
    attachments.put(
        String.format("mu_feedback_%s_%s%s", person.getWbi(), formattedInstant, ".xlsx"),
        workbookContent.toByteArray());

    try {
      workbookContent.close();
    } catch (IOException ioException) {
      log.error("Could not close ByteArrayOutputStream.", ioException);
    }

    try {
      this.mailService.sendMail(
          body,
          attachments,
          3,
          person.getEmail(),
          String.format("Mass upload feedback - %s", massUploadType));
    } catch (MessagingException messagingException) {
      log.error("Exception sending MU feedback as mail.", messagingException);
    } catch (Exception exception) {
      log.error(
          String.format(
              "An exception occurred when trying to send an email with MU feedback %s.",
              massUploadType),
          exception);
    }
  }
}
