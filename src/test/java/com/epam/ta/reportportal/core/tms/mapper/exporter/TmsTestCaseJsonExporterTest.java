package com.epam.ta.reportportal.core.tms.mapper.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

class TmsTestCaseJsonExporterTest {

  private TmsTestCaseJsonExporter jsonExporter;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    jsonExporter = new TmsTestCaseJsonExporter(objectMapper);
  }

  @Test
  void shouldReturnCorrectSupportedFormat() {
    // When
    var format = jsonExporter.getSupportedFormat();

    // Then
    assertThat(format).isEqualTo(TmsTestCaseExportFormat.JSON);
  }

  @Test
  void shouldExportTestCasesToJson() throws Exception {
    // Given
    var testCases = createTestCases();
    var mockResponse = new MockHttpServletResponse();

    // When
    jsonExporter.export(testCases, false, mockResponse);

    // Then
    var content = mockResponse.getContentAsString();
    var exportedTestCases = objectMapper.readValue(content, TmsTestCaseRS[].class);

    assertThat(exportedTestCases).hasSize(2);
    assertThat(exportedTestCases[0].getName()).isEqualTo("Test Case 1");
    assertThat(exportedTestCases[1].getName()).isEqualTo("Test Case 2");

    assertThat(mockResponse.getContentType()).isEqualTo("application/json;charset=UTF-8");
    assertThat(mockResponse.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(mockResponse.getHeader("Content-Disposition"))
        .isEqualTo("attachment; filename=\"test_cases_export.json\"");
  }

  @Test
  void shouldExportWithAttachmentsFilename() throws Exception {
    // Given
    var testCases = createTestCases();
    var mockResponse = new MockHttpServletResponse();

    // When
    jsonExporter.export(testCases, true, mockResponse);

    // Then
    assertThat(mockResponse.getHeader("Content-Disposition"))
        .isEqualTo("attachment; filename=\"test_cases_export_with_attachments.json\"");
  }

  @Test
  void shouldHandleEmptyTestCasesList() throws Exception {
    // Given
    var emptyList = List.<TmsTestCaseRS>of();
    var mockResponse = new MockHttpServletResponse();

    // When
    jsonExporter.export(emptyList, false, mockResponse);

    // Then
    var content = mockResponse.getContentAsString();
    var exportedTestCases = objectMapper.readValue(content, TmsTestCaseRS[].class);

    assertThat(exportedTestCases).isEmpty();
  }

  private List<TmsTestCaseRS> createTestCases() {
    var testCase1 = new TmsTestCaseRS();
    testCase1.setId(1L);
    testCase1.setName("Test Case 1");
    testCase1.setDescription("Description 1");

    var testCase2 = new TmsTestCaseRS();
    testCase2.setId(2L);
    testCase2.setName("Test Case 2");
    testCase2.setDescription("Description 2");

    return Arrays.asList(testCase1, testCase2);
  }
}
