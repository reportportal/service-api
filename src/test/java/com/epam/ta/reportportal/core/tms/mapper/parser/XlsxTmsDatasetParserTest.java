package com.epam.ta.reportportal.core.tms.mapper.parser;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class XlsxTmsDatasetParserTest {

  private final TmsDatasetParser sut = new XlsxTmsDatasetParser();

  @Mock
  private MultipartFile multipartFile;

  @Test
  @SneakyThrows
  void shouldParseValidXlsxFile() {
    var workbook = new XSSFWorkbook();
    var sheet = workbook.createSheet("Datasets");
    var headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("name");
    headerRow.createCell(1).setCellValue("key");
    headerRow.createCell(2).setCellValue("value");

    var firstRow = sheet.createRow(1);
    firstRow.createCell(0).setCellValue("Dataset1");
    firstRow.createCell(1).setCellValue("Key1");
    firstRow.createCell(2).setCellValue("Value1");

    var secondRow = sheet.createRow(2);
    secondRow.createCell(0).setCellValue("Dataset1");
    secondRow.createCell(1).setCellValue("Key2");
    secondRow.createCell(2).setCellValue("Value2");

    var thirdRow = sheet.createRow(3);
    thirdRow.createCell(0).setCellValue("Dataset2");
    thirdRow.createCell(1).setCellValue("Key3");
    thirdRow.createCell(2).setCellValue("Value3");

    // Write workbook to a byte array
    var byteArrayOutputStream = new ByteArrayOutputStream();
    workbook.write(byteArrayOutputStream);
    var inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    workbook.close();

    when(multipartFile.getInputStream()).thenReturn(inputStream);

    var result = assertDoesNotThrow(() -> sut.parse(multipartFile));

    assertThat(result).isNotNull().hasSize(2);

    var firstDataset = result.get(0);
    assertEquals("Dataset1", firstDataset.getName());
    assertEquals(2, firstDataset.getAttributes().size());
    assertEquals("Key1", firstDataset.getAttributes().get(0).getKey());
    assertEquals("Value1", firstDataset.getAttributes().get(0).getValue());
    assertEquals("Key2", firstDataset.getAttributes().get(1).getKey());
    assertEquals("Value2", firstDataset.getAttributes().get(1).getValue());

    var secondDataset = result.get(1);
    assertEquals("Dataset2", secondDataset.getName());
    assertEquals(1, secondDataset.getAttributes().size());
    assertEquals("Key3", secondDataset.getAttributes().get(0).getKey());
    assertEquals("Value3", secondDataset.getAttributes().get(0).getValue());
  }

  @Test
  @SneakyThrows
  void shouldNotParseXlsxFileWithInvalidRowFormat() {
    // Create an Excel workbook with an invalid row
    var workbook = new XSSFWorkbook();
    var sheet = workbook.createSheet("Datasets");
    var headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("name");
    headerRow.createCell(1).setCellValue("key");
    headerRow.createCell(2).setCellValue("value");

    var row = sheet.createRow(1);
    row.createCell(0).setCellValue("Dataset1");
    row.createCell(1).setCellValue("Key1");
    // Missing value column

    // Write workbook to a byte array
    var byteArrayOutputStream = new ByteArrayOutputStream();
    workbook.write(byteArrayOutputStream);
    var inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    workbook.close();

    when(multipartFile.getInputStream()).thenReturn(inputStream);

    assertThrows(
        IllegalArgumentException.class,
        () -> sut.parse(multipartFile)
    );
  }

  @Test
  @SneakyThrows
  void shouldParseEmptyXlsxFile() {
    var workbook = new XSSFWorkbook();

    var sheet = workbook.createSheet("Datasets");
    var headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("name");
    headerRow.createCell(1).setCellValue("key");
    headerRow.createCell(2).setCellValue("value");

    var byteArrayOutputStream = new ByteArrayOutputStream();
    workbook.write(byteArrayOutputStream);
    var inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    workbook.close();

    when(multipartFile.getInputStream()).thenReturn(inputStream);

    var result = assertDoesNotThrow(() -> sut.parse(multipartFile));

    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void shouldGetTmsDatasetFileType() {
    assertEquals(TmsDatasetFileType.XLSX, sut.getTmsDatasetFileType());
  }
}
