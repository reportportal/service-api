package com.epam.ta.reportportal.core.tms.mapper.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TmsTestCaseCsvImporterTest {

  private TmsTestCaseCsvImporter csvImporter;
  private TmsTestCaseTestFolderRQ testFolderRQ;

  @BeforeEach
  void setUp() {
    csvImporter = new TmsTestCaseCsvImporter();
    testFolderRQ = TmsTestCaseTestFolderRQ.builder()
        .id(1L)
        .name("Default Folder")
        .build();
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
    var csvContent = "name,description,priority,externalId\n" +
        "Test Case 1,Description 1,HIGH,123\n" +
        "Test Case 2,Description 2,LOW,321";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(testCases).hasSize(2);

    var testCase1 = testCases.getFirst();
    assertThat(testCase1.getName()).isEqualTo("Test Case 1");
    assertThat(testCase1.getDescription()).isEqualTo("Description 1");
    assertThat(testCase1.getTestFolder()).isEqualTo(testFolderRQ);
    assertThat(testCase1.getTestFolder().getId()).isEqualTo(1L);
    assertThat(testCase1.getTestFolder().getName()).isEqualTo("Default Folder");
    assertThat(testCase1.getPriority()).isEqualTo("HIGH");
    assertThat(testCase1.getExternalId()).isEqualTo("123");

    var testCase2 = testCases.get(1);
    assertThat(testCase2.getName()).isEqualTo("Test Case 2");
    assertThat(testCase2.getDescription()).isEqualTo("Description 2");
    assertThat(testCase2.getTestFolder()).isEqualTo(testFolderRQ);
    assertThat(testCase2.getTestFolder().getId()).isEqualTo(1L);
    assertThat(testCase2.getTestFolder().getName()).isEqualTo("Default Folder");
    assertThat(testCase2.getPriority()).isEqualTo("LOW");
    assertThat(testCase2.getExternalId()).isEqualTo("321");
  }

  @Test
  void shouldHandleEmptyOptionalFields() {
    // Given
    var csvContent = "name,description,priority,externalId\n" +
        "Test Case,Description,,";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(testCases).hasSize(1);

    var testCase = testCases.getFirst();
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getDescription()).isEqualTo("Description");
    assertThat(testCase.getTestFolder()).isEqualTo(testFolderRQ);
    assertThat(testCase.getPriority()).isEmpty();
    assertThat(testCase.getExternalId()).isEmpty();
  }

  @Test
  void shouldImportTestCasesWithTestFolderById() {
    // Given
    var testFolderById = TmsTestCaseTestFolderRQ.builder()
        .id(5L)
        .build();
    var csvContent = "name,description,priority,externalId\n" +
        "Test Case,Description,MEDIUM,456";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderById);

    // Then
    assertThat(testCases).hasSize(1);

    var testCase = testCases.getFirst();
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getTestFolder()).isEqualTo(testFolderById);
    assertThat(testCase.getTestFolder().getId()).isEqualTo(5L);
    assertThat(testCase.getTestFolder().getName()).isNull();
  }

  @Test
  void shouldImportTestCasesWithTestFolderByName() {
    // Given
    var testFolderByName = TmsTestCaseTestFolderRQ.builder()
        .name("Integration Tests")
        .build();
    var csvContent = "name,description,priority,externalId\n" +
        "Test Case,Description,LOW,789";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderByName);

    // Then
    assertThat(testCases).hasSize(1);

    var testCase = testCases.getFirst();
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getTestFolder()).isEqualTo(testFolderByName);
    assertThat(testCase.getTestFolder().getId()).isNull();
    assertThat(testCase.getTestFolder().getName()).isEqualTo("Integration Tests");
  }

  @Test
  void shouldImportTestCasesWithNullTestFolder() {
    // Given
    var csvContent = "name,description,priority,externalId\n" +
        "Test Case,Description,HIGH,000";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, null);

    // Then
    assertThat(testCases).hasSize(1);

    var testCase = testCases.getFirst();
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getTestFolder()).isNull();
  }

  @Test
  void shouldThrowExceptionForInvalidCsvFormat() {
    // Given
    var csvContent = "name,description\n" + // Missing required columns
        "Test Case,Description";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When & Then
    var exception = assertThrows(IllegalArgumentException.class, () ->
        csvImporter.importFromFile(file, testFolderRQ));

    assertThat(exception.getMessage()).contains("Invalid CSV format");
    assertThat(exception.getMessage()).contains("name, description, priority, externalId");
  }

  @Test
  void shouldHandleCsvWithOnlyRequiredFields() {
    // Given
    var csvContent = "name,description,priority,externalId\n" +
        "Simple Test,Simple Description,MEDIUM,999";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(testCases).hasSize(1);

    var testCase = testCases.getFirst();
    assertThat(testCase.getName()).isEqualTo("Simple Test");
    assertThat(testCase.getDescription()).isEqualTo("Simple Description");
    assertThat(testCase.getPriority()).isEqualTo("MEDIUM");
    assertThat(testCase.getExternalId()).isEqualTo("999");
    assertThat(testCase.getTestFolder()).isEqualTo(testFolderRQ);
  }

  @Test
  void shouldHandleMultipleTestCasesWithSameTestFolder() {
    // Given
    var csvContent = "name,description,priority,externalId\n" +
        "Test 1,Description 1,HIGH,001\n" +
        "Test 2,Description 2,MEDIUM,002\n" +
        "Test 3,Description 3,LOW,003";
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(testCases).hasSize(3);

    // Verify all test cases have the same test folder
    assertThat(testCases).allSatisfy(testCase ->
        assertThat(testCase.getTestFolder()).isEqualTo(testFolderRQ));
  }

  @Test
  void shouldHandleEmptyFile() {
    // Given
    var csvContent = "name,description,priority,externalId\n"; // Only header
    var file = new MockMultipartFile("file", "testcases.csv", "text/csv", csvContent.getBytes());

    // When
    var testCases = csvImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(testCases).isEmpty();
  }
}
