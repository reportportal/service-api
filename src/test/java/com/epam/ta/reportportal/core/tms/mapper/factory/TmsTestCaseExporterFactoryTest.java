package com.epam.ta.reportportal.core.tms.mapper.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseExportFormat;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestCaseExporter;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestCaseExporterFactoryTest {

  @Mock
  private TmsTestCaseExporter jsonExporter;

  @Mock
  private TmsTestCaseExporter csvExporter;

  private TmsTestCaseExporterFactory factory;

  @BeforeEach
  void setUp() {
    when(jsonExporter.getSupportedFormat()).thenReturn(TmsTestCaseExportFormat.JSON);
    when(csvExporter.getSupportedFormat()).thenReturn(TmsTestCaseExportFormat.CSV);

    factory = new TmsTestCaseExporterFactory(Arrays.asList(jsonExporter, csvExporter));
  }

  @Test
  void shouldReturnJsonExporter() {
    // When
    var exporter = factory.getExporter("JSON");

    // Then
    assertThat(exporter).isEqualTo(jsonExporter);
  }

  @Test
  void shouldReturnCsvExporter() {
    // When
    var exporter = factory.getExporter("CSV");

    // Then
    assertThat(exporter).isEqualTo(csvExporter);
  }

  @Test
  void shouldReturnExporterCaseInsensitive() {
    // When
    var jsonExporter = factory.getExporter("json");
    var csvExporter = factory.getExporter("csv");

    // Then
    assertThat(jsonExporter).isEqualTo(this.jsonExporter);
    assertThat(csvExporter).isEqualTo(this.csvExporter);
  }

  @Test
  void shouldThrowExceptionForInvalidFormat() {
    // When & Then
    var exception = assertThrows(IllegalArgumentException.class, () ->
        factory.getExporter("XML"));

    assertThat(exception.getMessage()).contains("Unsupported export format");
  }
}
