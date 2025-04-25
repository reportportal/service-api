package com.epam.ta.reportportal.core.tms.mapper.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.mapper.exporter.TmsTestFolderExporter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsTestFolderExporterFactoryTest {

  @InjectMocks
  private TmsTestFolderExporterFactory factory;

  private TmsTestFolderExporter csvExporter;

  @BeforeEach
  void setUp() {
    // Create mock CSV exporter
    csvExporter = mock(TmsTestFolderExporter.class);
  }

  @Test
  void testSetTmsTestFolderExporters_WithCsvExporter() {
    // Configure mock exporter
    when(csvExporter.getTmsTestFolderExportFileType()).thenReturn(TmsTestFolderExportFileType.CSV);

    // Arrange
    List<TmsTestFolderExporter> exporters = Collections.singletonList(csvExporter);

    // Act
    factory.setTmsTestFolderExporters(exporters);

    // Assert
    TmsTestFolderExporter result = factory.getExporter(TmsTestFolderExportFileType.CSV);
    assertNotNull(result);
    assertEquals(csvExporter, result);
  }

  @Test
  void testSetTmsTestFolderExporters_WithEmptyList() {
    // Arrange
    List<TmsTestFolderExporter> exporters = Collections.emptyList();

    // Act
    factory.setTmsTestFolderExporters(exporters);

    // Assert - Should throw exception for CSV type
    var exception = assertThrows(UnsupportedOperationException.class, () ->
        factory.getExporter(TmsTestFolderExportFileType.CSV));

    assertEquals("Unsupported file type.", exception.getMessage());
  }

  @Test
  void testGetExporter_WithUnsupportedEnum() {
    // Configure mock exporter
    when(csvExporter.getTmsTestFolderExportFileType()).thenReturn(TmsTestFolderExportFileType.CSV);

    // Arrange
    List<TmsTestFolderExporter> exporters = Collections.singletonList(csvExporter);
    factory.setTmsTestFolderExporters(exporters);

    // Create a custom enum value for testing
    TmsTestFolderExportFileType unsupportedType = TmsTestFolderExportFileType.valueOf(
        "CSV"); // Same name but different instance

    // Act & Assert - Should work because it's the same enum value
    TmsTestFolderExporter result = factory.getExporter(unsupportedType);
    assertEquals(csvExporter, result);
  }

  @Test
  void testGetExporter_WithNullFileType() {
    // Configure mock exporter
    when(csvExporter.getTmsTestFolderExportFileType()).thenReturn(TmsTestFolderExportFileType.CSV);
    // Arrange
    List<TmsTestFolderExporter> exporters = Collections.singletonList(csvExporter);
    factory.setTmsTestFolderExporters(exporters);

    // Act & Assert
    assertThrows(UnsupportedOperationException.class, () ->
        factory.getExporter(null));
  }
}
