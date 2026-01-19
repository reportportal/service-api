package com.epam.reportportal.core.tms.mapper.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.epam.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void shouldParseBasicTestCasesFromCsv() {
    // Given
    var csvContent = "summary,description,priority\n" +
        "Test Case 1,Description 1,HIGH\n" +
        "Test Case 2,Description 2,LOW";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTotalRows()).isEqualTo(2);
    assertThat(result.getTestCases()).hasSize(2);

    var testCase1 = result.getTestCases().get(0);
    assertThat(testCase1.getName()).isEqualTo("Test Case 1");
    assertThat(testCase1.getDescription()).isEqualTo("Description 1");
    assertThat(testCase1.getPriority()).isEqualTo("HIGH");

    var testCase2 = result.getTestCases().get(1);
    assertThat(testCase2.getName()).isEqualTo("Test Case 2");
    assertThat(testCase2.getDescription()).isEqualTo("Description 2");
    assertThat(testCase2.getPriority()).isEqualTo("LOW");
  }

  @Test
  void shouldParseLabelsAsSemicolonSeparatedList() {
    // Given
    var csvContent = "summary,labels\n" +
        "Test Case,smoke;regression;critical";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getAttributes()).hasSize(3);
    assertThat(testCase.getAttributes())
        .extracting("key")
        .containsExactly("smoke", "regression", "critical");
  }

  @Test
  void shouldParseFolderPathAsSlashSeparatedHierarchy() {
    // Given
    var csvContent = "summary,path\n" +
        "Test Case,Folder1/Folder2/Folder3";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getFolderPath())
        .containsExactly("Folder1", "Folder2", "Folder3");
  }

  @Test
  void shouldParseManualScenarioFromTestStepsAndExpectedResult() {
    // Given
    var csvContent = "summary,test steps,expected result,requirements\n" +
        "Test Case,Step 1. Do something,Result should be visible,https://req.example.com";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getManualScenario()).isNotNull();

    var manualScenario = (com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ)
        testCase.getManualScenario();
    assertThat(manualScenario.getManualScenarioType()).isEqualTo(TmsManualScenarioType.TEXT);
    assertThat(manualScenario.getInstructions()).isEqualTo("Step 1. Do something");
    assertThat(manualScenario.getExpectedResult()).isEqualTo("Result should be visible");
    assertThat(manualScenario.getLinkToRequirements()).isEqualTo("https://req.example.com");
  }

  @Test
  void shouldNotCreateManualScenarioWhenTestStepsAndExpectedResultAreEmpty() {
    // Given
    var csvContent = "summary,test steps,expected result\n" +
        "Test Case,,";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getManualScenario()).isNull();
  }

  @Test
  void shouldParseFullCsvWithAllColumns() {
    // Given
    var csvContent = "summary,description,priority,labels,path,test steps,expected result,requirements\n" +
        "Login Test,Test login functionality,HIGH,smoke;auth,Auth/Login,1. Enter credentials,User logged in,REQ-001";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);

    assertThat(testCase.getName()).isEqualTo("Login Test");
    assertThat(testCase.getDescription()).isEqualTo("Test login functionality");
    assertThat(testCase.getPriority()).isEqualTo("HIGH");
    assertThat(testCase.getFolderPath()).containsExactly("Auth", "Login");
    assertThat(testCase.getAttributes()).hasSize(2);
    assertThat(testCase.getManualScenario()).isNotNull();
  }

  @Test
  void shouldHandleEmptyOptionalFields() {
    // Given
    var csvContent = "summary,description,priority,labels,path\n" +
        "Test Case,,,  ,";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getDescription()).isNull();
    assertThat(testCase.getPriority()).isNull();
    assertThat(testCase.getAttributes()).isEmpty();
    assertThat(testCase.getFolderPath()).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenSummaryHeaderIsMissing() {
    // Given
    var csvContent = "description,priority\n" +
        "Description,HIGH";
    var inputStream = toInputStream(csvContent);

    // When & Then
    assertThatThrownBy(() -> csvImporter.parse(inputStream))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("missing required header")
        .hasMessageContaining("summary");
  }

  @Test
  void shouldSkipRowsWithBlankSummary() {
    // Given
    var csvContent = "summary,description\n" +
        "Test Case 1,Description 1\n" +
        ",Empty summary row\n" +
        "   ,Blank summary row\n" +
        "Test Case 2,Description 2";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTotalRows()).isEqualTo(4);
    assertThat(result.getTestCases()).hasSize(2);
    assertThat(result.getTestCases())
        .extracting("name")
        .containsExactly("Test Case 1", "Test Case 2");
  }

  @Test
  void shouldHandleEmptyFile() {
    // Given
    var csvContent = "summary,description,priority\n";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTotalRows()).isZero();
    assertThat(result.getTestCases()).isEmpty();
    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  void shouldHandleCaseInsensitiveHeaders() {
    // Given
    var csvContent = "SUMMARY,Description,PRIORITY,Labels\n" +
        "Test Case,Description,HIGH,label1";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getDescription()).isEqualTo("Description");
    assertThat(testCase.getPriority()).isEqualTo("HIGH");
    assertThat(testCase.getAttributes()).hasSize(1);
  }

  @Test
  void shouldTrimWhitespaceFromValues() {
    // Given
    var csvContent = "summary,description,labels\n" +
        "  Test Case  ,  Description  ,  label1 ; label2  ";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getDescription()).isEqualTo("Description");
    assertThat(testCase.getAttributes())
        .extracting("key")
        .containsExactly("label1", "label2");
  }

  @Test
  void shouldDeduplicateLabels() {
    // Given
    var csvContent = "summary,labels\n" +
        "Test Case,smoke;smoke;regression;smoke";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getAttributes()).hasSize(2);
    assertThat(testCase.getAttributes())
        .extracting("key")
        .containsExactly("smoke", "regression");
  }

  @Test
  void shouldHandlePathWithLeadingAndTrailingSlashes() {
    // Given
    var csvContent = "summary,path\n" +
        "Test Case,/Folder1/Folder2/";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getFolderPath())
        .containsExactly("Folder1", "Folder2");
  }

  @Test
  void shouldHandleMultipleTestCases() {
    // Given
    var csvContent = "summary,description,priority,path\n" +
        "Test 1,Description 1,HIGH,Folder1\n" +
        "Test 2,Description 2,MEDIUM,Folder1/Subfolder\n" +
        "Test 3,Description 3,LOW,Folder2";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTotalRows()).isEqualTo(3);
    assertThat(result.getTestCases()).hasSize(3);
  }

  @Test
  void shouldCreateManualScenarioWithOnlyTestSteps() {
    // Given
    var csvContent = "summary,test steps\n" +
        "Test Case,Step 1. Do something";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getManualScenario()).isNotNull();

    var manualScenario = (com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ)
        testCase.getManualScenario();
    assertThat(manualScenario.getInstructions()).isEqualTo("Step 1. Do something");
    assertThat(manualScenario.getExpectedResult()).isNull();
  }

  @Test
  void shouldCreateManualScenarioWithOnlyExpectedResult() {
    // Given
    var csvContent = "summary,expected result\n" +
        "Test Case,User should see success message";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getManualScenario()).isNotNull();

    var manualScenario = (com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ)
        testCase.getManualScenario();
    assertThat(manualScenario.getInstructions()).isNull();
    assertThat(manualScenario.getExpectedResult()).isEqualTo("User should see success message");
  }

  @Test
  void shouldHandleExtraColumnsNotInSpec() {
    // Given
    var csvContent = "summary,custom_column,description,another_column\n" +
        "Test Case,custom_value,Description,another_value";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(1);
    var testCase = result.getTestCases().get(0);
    assertThat(testCase.getName()).isEqualTo("Test Case");
    assertThat(testCase.getDescription()).isEqualTo("Description");
  }

  @Test
  void shouldHandleOnlySummaryColumn() {
    // Given
    var csvContent = "summary\n" +
        "Test Case 1\n" +
        "Test Case 2";
    var inputStream = toInputStream(csvContent);

    // When
    var result = csvImporter.parse(inputStream);

    // Then
    assertThat(result.getTestCases()).hasSize(2);
    assertThat(result.getTestCases())
        .extracting("name")
        .containsExactly("Test Case 1", "Test Case 2");
  }

  private ByteArrayInputStream toInputStream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }
}
