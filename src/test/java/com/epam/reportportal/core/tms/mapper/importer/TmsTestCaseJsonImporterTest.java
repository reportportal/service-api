package com.epam.reportportal.core.tms.mapper.importer;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TmsTestCaseJsonImporterTest {

  private TmsTestCaseJsonImporter jsonImporter;
  private ObjectMapper objectMapper;
  private NewTestFolderRQ testFolderRQ;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    jsonImporter = new TmsTestCaseJsonImporter(objectMapper);
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
    var importedTestCases = jsonImporter.importFromFile(file);

    // Then
    assertThat(importedTestCases).hasSize(2);

    var firstTestCase = importedTestCases.getFirst();
    assertThat(firstTestCase.getName()).isEqualTo("Test Case 1");
    assertThat(firstTestCase.getDescription()).isEqualTo("Description 1");
    assertThat(firstTestCase.getPriority()).isEqualTo("HIGH");
    assertThat(firstTestCase.getExternalId()).isEqualTo("123");

    var secondTestCase = importedTestCases.get(1);
    assertThat(secondTestCase.getName()).isEqualTo("Test Case 2");
    assertThat(secondTestCase.getDescription()).isEqualTo("Description 2");
    assertThat(secondTestCase.getPriority()).isEqualTo("LOW");
    assertThat(secondTestCase.getExternalId()).isEqualTo("321");
  }

  @Test
  void shouldImportEmptyList() throws Exception {
    // Given
    var jsonContent = "[]";
    var file = new MockMultipartFile("file", "testcases.json", "application/json",
        jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file);

    // Then
    assertThat(importedTestCases).isEmpty();
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
    var importedTestCases = jsonImporter.importFromFile(file);

    // Then
    assertThat(importedTestCases).hasSize(1);

    var importedTestCase = importedTestCases.getFirst();
    assertThat(importedTestCase.getName()).isEqualTo("Complete Test Case");
    assertThat(importedTestCase.getDescription()).isEqualTo("Complete Description");
    assertThat(importedTestCase.getPriority()).isEqualTo("HIGH");
    assertThat(importedTestCase.getExternalId()).isEqualTo("COMPLETE-001");
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
