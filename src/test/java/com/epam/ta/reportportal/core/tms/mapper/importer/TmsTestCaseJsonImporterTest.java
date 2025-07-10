package com.epam.ta.reportportal.core.tms.mapper.importer;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TmsTestCaseJsonImporterTest {

  private TmsTestCaseJsonImporter jsonImporter;
  private ObjectMapper objectMapper;

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
    var testCase1 = createTestCaseRQ("Test Case 1", "Description 1");
    var testCase2 = createTestCaseRQ("Test Case 2", "Description 2");
    var testCases = Arrays.asList(testCase1, testCase2);

    var jsonContent = objectMapper.writeValueAsString(testCases);
    var file = new MockMultipartFile("file", "testcases.json", "application/json", jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file);

    // Then
    assertThat(importedTestCases).hasSize(2);

    assertThat(importedTestCases.get(0).getName()).isEqualTo("Test Case 1");
    assertThat(importedTestCases.get(0).getDescription()).isEqualTo("Description 1");

    assertThat(importedTestCases.get(1).getName()).isEqualTo("Test Case 2");
    assertThat(importedTestCases.get(1).getDescription()).isEqualTo("Description 2");
  }

  @Test
  void shouldImportEmptyList() throws Exception {
    // Given
    var jsonContent = "[]";
    var file = new MockMultipartFile("file", "testcases.json", "application/json", jsonContent.getBytes());

    // When
    var importedTestCases = jsonImporter.importFromFile(file);

    // Then
    assertThat(importedTestCases).isEmpty();
  }


  private TmsTestCaseRQ createTestCaseRQ(String name, String description) {
    var testCaseRQ = new TmsTestCaseRQ();
    testCaseRQ.setName(name);
    testCaseRQ.setDescription(description);
    testCaseRQ.setPriority("HIGH");
    return testCaseRQ;
  }
}
