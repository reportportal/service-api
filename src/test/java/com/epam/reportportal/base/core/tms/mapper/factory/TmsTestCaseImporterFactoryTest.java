package com.epam.reportportal.base.core.tms.mapper.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.reportportal.base.core.tms.mapper.importer.TmsTestCaseImporter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    factory = new TmsTestCaseImporterFactory(List.of(jsonImporter, csvImporter));
  }

  // ==================== GET IMPORTER BY FORMAT TESTS ====================

  @Test
  void shouldReturnJsonImporterByFormat() {
    // When
    var importer = factory.getImporter(TmsTestCaseImportFormat.JSON);

    // Then
    assertThat(importer).isEqualTo(jsonImporter);
  }

  @Test
  void shouldReturnCsvImporterByFormat() {
    // When
    var importer = factory.getImporter(TmsTestCaseImportFormat.CSV);

    // Then
    assertThat(importer).isEqualTo(csvImporter);
  }

  @Test
  void shouldThrowExceptionForUnsupportedFormat() {
    // Given
    var factoryWithOnlyCsv = new TmsTestCaseImporterFactory(List.of(csvImporter));

    // When & Then
    assertThatThrownBy(() -> factoryWithOnlyCsv.getImporter(TmsTestCaseImportFormat.JSON))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("Unsupported import format")
        .hasMessageContaining("JSON");
  }

  // ==================== GET IMPORTER BY FILE NAME TESTS ====================

  @Test
  void shouldReturnJsonImporterByFileName() {
    // When
    var importer = factory.getImporter("testcases.json");

    // Then
    assertThat(importer).isEqualTo(jsonImporter);
  }

  @Test
  void shouldReturnCsvImporterByFileName() {
    // When
    var importer = factory.getImporter("testcases.csv");

    // Then
    assertThat(importer).isEqualTo(csvImporter);
  }

  @Test
  void shouldReturnImporterByFileNameCaseInsensitive() {
    // When
    var importerUpperCase = factory.getImporter("testcases.JSON");
    var importerMixedCase = factory.getImporter("testcases.CsV");

    // Then
    assertThat(importerUpperCase).isEqualTo(jsonImporter);
    assertThat(importerMixedCase).isEqualTo(csvImporter);
  }

  @Test
  void shouldReturnImporterByFileNameWithPath() {
    // When
    var importer = factory.getImporter("/path/to/testcases.csv");

    // Then
    assertThat(importer).isEqualTo(csvImporter);
  }

  @Test
  void shouldThrowExceptionForFileWithoutExtension() {
    // When & Then
    assertThatThrownBy(() -> factory.getImporter("testcases"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid file name");
  }

  @Test
  void shouldThrowExceptionForFileWithUnsupportedExtension() {
    // When & Then
    assertThatThrownBy(() -> factory.getImporter("testcases.xml"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionForNullFileName() {
    // When & Then
    assertThatThrownBy(() -> factory.getImporter((String) null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionForEmptyFileName() {
    // When & Then
    assertThatThrownBy(() -> factory.getImporter(""))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
