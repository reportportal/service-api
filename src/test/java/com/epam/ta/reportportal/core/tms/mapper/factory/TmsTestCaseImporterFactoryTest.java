package com.epam.ta.reportportal.core.tms.mapper.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.mapper.importer.TmsTestCaseImporter;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseImporterFactoryTest {

  @Mock
  private TmsTestCaseImporter jsonImporter;

  @Mock
  private TmsTestCaseImporter csvImporter;

  private TmsTestCaseImporterFactory factory;

  @BeforeEach
  void setUp() {
    when(jsonImporter.getSupportedFormat()).thenReturn(TmsTestCaseImportFormat.JSON);
    when(csvImporter.getSupportedFormat()).thenReturn(TmsTestCaseImportFormat.CSV);

    factory = new TmsTestCaseImporterFactory(Arrays.asList(jsonImporter, csvImporter));
  }

  @Test
  void shouldReturnJsonImporter() {
    // Given
    var file = new MockMultipartFile("file", "testcases.json", "application/json", "[]".getBytes());

    // When
    var importer = factory.getImporter(file);

    // Then
    assertThat(importer).isEqualTo(jsonImporter);
  }

  @Test
  void shouldReturnCsvImporter() {
    // Given
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv",
        "name,description".getBytes());

    // When
    var importer = factory.getImporter(file);

    // Then
    assertThat(importer).isEqualTo(csvImporter);
  }

  @Test
  void shouldThrowExceptionForFileWithoutExtension() {
    // Given
    var file = new MockMultipartFile("file", "testcases", "text/plain", "data".getBytes());

    // When & Then
    var exception = assertThrows(IllegalArgumentException.class, () ->
        factory.getImporter(file));

    assertThat(exception.getMessage()).contains("Invalid file name");
  }
}
