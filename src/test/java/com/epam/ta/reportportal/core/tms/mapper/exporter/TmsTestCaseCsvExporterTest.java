package com.epam.ta.reportportal.core.tms.mapper.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRS;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseCsvExporterTest {

  private TmsTestCaseCsvExporter csvExporter;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletOutputStream outputStream;

  @BeforeEach
  void setUp() {
    csvExporter = new TmsTestCaseCsvExporter();
  }

  @Test
  void shouldReturnCorrectSupportedFormat() {
    // When
    var format = csvExporter.getSupportedFormat();

    // Then
    assertThat(format).isEqualTo(TmsTestCaseExportFormat.CSV);
  }

  @Test
  void shouldExportTestCasesToCsv() throws IOException {
    // Given
    var testCases = createTestCases();
    var mockResponse = new MockHttpServletResponse();

    // When
    csvExporter.export(testCases, false, mockResponse);

    // Then
    var content = mockResponse.getContentAsString();
    assertThat(content).contains("ID,Name,Description,Test Folder,Priority");
    assertThat(content).contains("1,Test Case 1,Description 1,1,HIGH");
    assertThat(content).contains("2,Test Case 2,Description 2,2,LOW");

    assertThat(mockResponse.getContentType()).isEqualTo("text/csv;charset=UTF-8");
    assertThat(mockResponse.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(mockResponse.getHeader("Content-Disposition"))
        .isEqualTo("attachment; filename=\"test_cases_export.csv\"");
  }

  @Test
  void shouldHandleEmptyTestCasesList() throws IOException {
    // Given
    var emptyList = List.<TmsTestCaseRS>of();
    var mockResponse = new MockHttpServletResponse();

    // When
    csvExporter.export(emptyList, false, mockResponse);

    // Then
    var content = mockResponse.getContentAsString();
    assertThat(content).contains("ID,Name,Description,Test Folder,Priority");
    assertThat(content.split("\n")).hasSize(1); // Only header
  }

  @Test
  void shouldHandleTestCasesWithNullValues() throws IOException {
    // Given
    var testCase = new TmsTestCaseRS();
    testCase.setId(1L);
    testCase.setName("Test Case");
    // description, testFolder, priority are null
    var testCases = List.of(testCase);
    var mockResponse = new MockHttpServletResponse();

    // When
    csvExporter.export(testCases, false, mockResponse);

    // Then
    var content = mockResponse.getContentAsString();
    assertThat(content).contains("1,Test Case");
  }

  private List<TmsTestCaseRS> createTestCases() {
    var folder1 = new TmsTestCaseTestFolderRS();
    folder1.setId(1L);

    var folder2 = new TmsTestCaseTestFolderRS();
    folder2.setId(2L);

    var testCase1 = new TmsTestCaseRS();
    testCase1.setId(1L);
    testCase1.setName("Test Case 1");
    testCase1.setDescription("Description 1");
    testCase1.setTestFolder(folder1);
    testCase1.setPriority("HIGH");

    var testCase2 = new TmsTestCaseRS();
    testCase2.setId(2L);
    testCase2.setName("Test Case 2");
    testCase2.setDescription("Description 2");
    testCase2.setTestFolder(folder2);
    testCase2.setPriority("LOW");

    return Arrays.asList(testCase1, testCase2);
  }
}
