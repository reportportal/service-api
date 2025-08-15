package com.epam.ta.reportportal.core.tms.mapper.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TmsTestCaseCsvImporterTest {

  private TmsTestCaseCsvImporter csvImporter;

  @BeforeEach
  void setUp() {
    csvImporter = new TmsTestCaseCsvImporter();
  }

  @Test
  void shouldReturnCorrectSupportedFormat() {
    // When
    var format = csvImporter.getSupportedFormat();

    // Then
    assertThat(format).isEqualTo(TmsTestCaseImportFormat.CSV);
  }

  @Test
  void shouldImportTestCasesFromCsv() {
    // Given
    var csvContent = "name,description,testFolder,priority\n" +
        "Test Case 1,Description 1,Folder 1,HIGH,123\n" +
        "Test Case 2,Description 2,Folder 2,LOW,321";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file);

    // Then
    assertThat(testCases).hasSize(2);

    var testCase1 = testCases.getFirst();
    assertThat(testCase1.getName()).isEqualTo("Test Case 1");
    assertThat(testCase1.getDescription()).isEqualTo("Description 1");
    assertThat(testCase1.getTestFolder().getName()).isEqualTo("Folder 1");
    assertThat(testCase1.getPriority()).isEqualTo("HIGH");
    assertThat(testCase1.getExternalId()).isEqualTo("123");

    var testCase2 = testCases.get(1);
    assertThat(testCase2.getName()).isEqualTo("Test Case 2");
    assertThat(testCase2.getDescription()).isEqualTo("Description 2");
    assertThat(testCase2.getTestFolder().getName()).isEqualTo("Folder 2");
    assertThat(testCase2.getPriority()).isEqualTo("LOW");
    assertThat(testCase2.getExternalId()).isEqualTo("321");
  }

  @Test
  void shouldHandleEmptyOptionalFields() {
    // Given
    var csvContent = "name,description,testFolder,priority,externalId\n" +
        "Test Case,Description,,,";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file);

    // Then
    assertThat(testCases).hasSize(1);

    var testCase = testCases.getFirst();
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getDescription()).isEqualTo("Description");
  }

  @Test
  void shouldThrowExceptionForInvalidCsvFormat() {
    // Given
    var csvContent = "name,description\n" + // Missing required columns
        "Test Case,Description";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When & Then
    var exception = assertThrows(IllegalArgumentException.class, () ->
        csvImporter.importFromFile(file));

    assertThat(exception.getMessage()).contains("Invalid CSV format");
  }
}
