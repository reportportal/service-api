package com.epam.ta.reportportal.core.tms.mapper.parser;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CsvTmsDatasetParserTest {

  private final TmsDatasetParser sut = new CsvTmsDatasetParser();

  @Mock
  private MultipartFile multipartFile;

  @Test
  @SneakyThrows
  void shouldParseValidCsvFile() {
    var csvContent = "name,key,value\n"
        + "Dataset1,Key1,Value1\n"
        + "Dataset1,Key2,Value2\n"
        + "Dataset2,Key3,Value3";
    var inputStream = new ByteArrayInputStream(csvContent.getBytes());
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    var result = assertDoesNotThrow(() -> sut.parse(multipartFile));

    assertThat(result)
        .isNotNull()
        .hasSize(2);

    var firstTmsDataset = result.get(0);
    assertEquals("Dataset1", firstTmsDataset.getName());
    assertEquals(2, firstTmsDataset.getAttributes().size());
    assertEquals("Key1", firstTmsDataset.getAttributes().get(0).getKey());
    assertEquals("Value1", firstTmsDataset.getAttributes().get(0).getValue());
    assertEquals("Key2", firstTmsDataset.getAttributes().get(1).getKey());
    assertEquals("Value2", firstTmsDataset.getAttributes().get(1).getValue());

    var secondTmsDataset = result.get(1);
    assertEquals("Dataset2", secondTmsDataset.getName());
    assertEquals(1, secondTmsDataset.getAttributes().size());
    assertEquals("Key3", secondTmsDataset.getAttributes().get(0).getKey());
    assertEquals("Value3", secondTmsDataset.getAttributes().get(0).getValue());
  }

  @Test
  @SneakyThrows
  void shouldNotParseCsvFileWithInvalidRowFormat() {
    var csvContent = "name,key,value\n"
        + "Dataset1,Key1,Value1\n"
        + "InvalidRow,MissingKey"; // Invalid format
    var inputStream = new ByteArrayInputStream(csvContent.getBytes());
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    assertThrows(
        IllegalArgumentException.class,
        () -> sut.parse(multipartFile)
    );
  }

  @Test
  @SneakyThrows
  void shouldParseEmptyCsvFile() {
    // Mock the MultipartFile's input stream with empty content
    var csvContent = "";
    var inputStream = new ByteArrayInputStream(csvContent.getBytes());
    when(multipartFile.getInputStream()).thenReturn(inputStream);

    var result = assertDoesNotThrow(() -> sut.parse(multipartFile));

    assertThat(result)
        .isNotNull()
        .isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldNotParseCsvFileWithIOException() {
    when(multipartFile.getInputStream()).thenThrow(new IOException("Failed to read file"));
    assertThrows(
        IOException.class,
        () -> sut.parse(multipartFile)
    );
  }

  @Test
  void shouldGetTmsDatasetFileType() {
    assertEquals(TmsDatasetFileType.CSV, sut.getTmsDatasetFileType());
  }
}
