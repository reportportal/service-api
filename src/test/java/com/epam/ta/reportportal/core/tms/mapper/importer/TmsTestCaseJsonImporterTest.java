package com.epam.ta.reportportal.core.tms.mapper.importer;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TmsTestCaseJsonImporterTest {

  private TmsTestCaseJsonImporter jsonImporter;
  private ObjectMapper objectMapper;
  private TmsTestCaseTestFolderRQ testFolderRQ;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    jsonImporter = new TmsTestCaseJsonImporter(objectMapper);
    testFolderRQ = TmsTestCaseTestFolderRQ.builder()
        .id(1L)
        .name("Default Folder")
        .build();
  }

  @Test
  void shouldReturnCorrectSupportedFormat() {
    // When
    var format = jsonImporter.getSupportedFormat();

    // Then
    assertThat(format).isEqualTo(TmsTestCaseImportFormat.JSON);
  }

  @Test
  void shouldImportTestCasesFromJson() throws Exception {
    // Given
    var testCase1 = createTestCaseRQ("Test Case 1", "Description 1", "HIGH", "123");
    var testCase2 = createTestCaseRQ("Test Case 2", "Description 2", "LOW", "321");
    var testCases = Arrays.asList(testCase1, testCase2);

    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(importedTestCases).hasSize(2);

    var firstTestCase = importedTestCases.getFirst();
    assertThat(firstTestCase.getName()).isEqualTo("Test Case 1");
    assertThat(firstTestCase.getDescription()).isEqualTo("Description 1");
    assertThat(firstTestCase.getPriority()).isEqualTo("HIGH");
    assertThat(firstTestCase.getExternalId()).isEqualTo("123");
    assertThat(firstTestCase.getTestFolder()).isEqualTo(testFolderRQ);
    assertThat(firstTestCase.getTestFolder().getId()).isEqualTo(1L);
    assertThat(firstTestCase.getTestFolder().getName()).isEqualTo("Default Folder");

    var secondTestCase = importedTestCases.get(1);
    assertThat(secondTestCase.getName()).isEqualTo("Test Case 2");
    assertThat(secondTestCase.getDescription()).isEqualTo("Description 2");
    assertThat(secondTestCase.getPriority()).isEqualTo("LOW");
    assertThat(secondTestCase.getExternalId()).isEqualTo("321");
    assertThat(secondTestCase.getTestFolder()).isEqualTo(testFolderRQ);
    assertThat(secondTestCase.getTestFolder().getId()).isEqualTo(1L);
    assertThat(secondTestCase.getTestFolder().getName()).isEqualTo("Default Folder");
  }

  @Test
  void shouldImportTestCasesWithTestFolderById() throws Exception {
    // Given
    var testFolderById = TmsTestCaseTestFolderRQ.builder()
        .id(5L)
        .build();
    var testCase = createTestCaseRQ("Test Case", "Description", "MEDIUM", "456");
    var testCases = Collections.singletonList(testCase);

    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderById);

    // Then
    assertThat(importedTestCases).hasSize(1);

    var importedTestCase = importedTestCases.getFirst();
    assertThat(importedTestCase.getName()).isEqualTo("Test Case");
    assertThat(importedTestCase.getTestFolder()).isEqualTo(testFolderById);
    assertThat(importedTestCase.getTestFolder().getId()).isEqualTo(5L);
    assertThat(importedTestCase.getTestFolder().getName()).isNull();
  }

  @Test
  void shouldImportTestCasesWithTestFolderByName() throws Exception {
    // Given
    var testFolderByName = TmsTestCaseTestFolderRQ.builder()
        .name("Integration Tests")
        .build();
    var testCase = createTestCaseRQ("Test Case", "Description", "LOW", "789");
    var testCases = Collections.singletonList(testCase);

    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderByName);

    // Then
    assertThat(importedTestCases).hasSize(1);

    var importedTestCase = importedTestCases.getFirst();
    assertThat(importedTestCase.getName()).isEqualTo("Test Case");
    assertThat(importedTestCase.getTestFolder()).isEqualTo(testFolderByName);
    assertThat(importedTestCase.getTestFolder().getId()).isNull();
    assertThat(importedTestCase.getTestFolder().getName()).isEqualTo("Integration Tests");
  }

  @Test
  void shouldImportTestCasesWithNullTestFolder() throws Exception {
    // Given
    var testCase = createTestCaseRQ("Test Case", "Description", "HIGH", "000");
    var testCases = Collections.singletonList(testCase);

    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, null);

    // Then
    assertThat(importedTestCases).hasSize(1);

    var importedTestCase = importedTestCases.getFirst();
    assertThat(importedTestCase.getName()).isEqualTo("Test Case");
    assertThat(importedTestCase.getTestFolder()).isNull();
  }

  @Test
  void shouldImportEmptyList() throws Exception {
    // Given
    var jsonContent = "[]";
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(importedTestCases).isEmpty();
  }

  @Test
  void shouldOverrideExistingTestFolderFromJson() throws Exception {
    // Given - test case with existing testFolder in JSON will be overridden
    var existingTestFolder = TmsTestCaseTestFolderRQ.builder()
        .id(999L)
        .name("Existing Folder")
        .build();

    var testCase = createTestCaseRQ("Test Case", "Description", "MEDIUM", "555");
    testCase.setTestFolder(existingTestFolder); // This should be overridden

    var testCases = Collections.singletonList(testCase);
    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(importedTestCases).hasSize(1);

    var importedTestCase = importedTestCases.getFirst();
    assertThat(importedTestCase.getName()).isEqualTo("Test Case");
    // The testFolder should be overridden with the provided one, not the existing one
    assertThat(importedTestCase.getTestFolder()).isEqualTo(testFolderRQ);
    assertThat(importedTestCase.getTestFolder().getId()).isEqualTo(1L);
    assertThat(importedTestCase.getTestFolder().getName()).isEqualTo("Default Folder");
  }

  @Test
  void shouldHandleMultipleTestCasesWithSameTestFolder() throws Exception {
    // Given
    var testCase1 = createTestCaseRQ("Test 1", "Description 1", "HIGH", "001");
    var testCase2 = createTestCaseRQ("Test 2", "Description 2", "MEDIUM", "002");
    var testCase3 = createTestCaseRQ("Test 3", "Description 3", "LOW", "003");
    var testCases = Arrays.asList(testCase1, testCase2, testCase3);

    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(importedTestCases).hasSize(3);

    // Verify all test cases have the same test folder
    assertThat(importedTestCases).allSatisfy(testCase ->
        assertThat(testCase.getTestFolder()).isEqualTo(testFolderRQ));
  }

  @Test
  void shouldImportTestCaseWithCompleteData() throws Exception {
    // Given
    var testCase = new TmsTestCaseRQ();
    testCase.setName("Complete Test Case");
    testCase.setDescription("Complete Description");
    testCase.setPriority("HIGH");
    testCase.setExternalId("COMPLETE-001");
    // Add other fields if they exist in TmsTestCaseRQ

    var testCases = Collections.singletonList(testCase);
    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file, testFolderRQ);

    // Then
    assertThat(importedTestCases).hasSize(1);

    var importedTestCase = importedTestCases.getFirst();
    assertThat(importedTestCase.getName()).isEqualTo("Complete Test Case");
    assertThat(importedTestCase.getDescription()).isEqualTo("Complete Description");
    assertThat(importedTestCase.getPriority()).isEqualTo("HIGH");
    assertThat(importedTestCase.getExternalId()).isEqualTo("COMPLETE-001");
    assertThat(importedTestCase.getTestFolder()).isEqualTo(testFolderRQ);
  }

  private TmsTestCaseRQ createTestCaseRQ(String name, String description, String priority,
      String externalId) {
    var testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName(name);
    testCaseRQ.setDescription(description);
    testCaseRQ.setPriority(priority);
    testCaseRQ.setExternalId(externalId);
    return testCaseRQ;
  }
}
