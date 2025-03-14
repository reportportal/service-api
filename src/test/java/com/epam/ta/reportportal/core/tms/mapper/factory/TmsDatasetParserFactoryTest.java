package com.epam.ta.reportportal.core.tms.mapper.factory;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.mapper.parser.TmsDatasetFileType;
import com.epam.ta.reportportal.core.tms.mapper.parser.TmsDatasetParser;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TmsDatasetParserFactoryTest {

  private final TmsDatasetParserFactory sut = new TmsDatasetParserFactory();

  @Mock
  private TmsDatasetParser csvParser;

  @Mock
  private TmsDatasetParser xlsxParser;

  @Mock
  private MultipartFile multipartFile;

  @BeforeEach
  void setUp() {
    // Mock the behavior of the parsers
    when(csvParser.getTmsDatasetFileType()).thenReturn(TmsDatasetFileType.CSV);
    when(xlsxParser.getTmsDatasetFileType()).thenReturn(TmsDatasetFileType.XLSX);

    // Set the parsers into the factory using setter injection
    sut.setTmsDatasetParsers(List.of(csvParser, xlsxParser));
  }

  @Test
  void shouldGetParserForCsvFile() {
    when(multipartFile.getOriginalFilename()).thenReturn("data.csv");

    var parser = assertDoesNotThrow(() -> sut.getParser(multipartFile));

    assertThat(parser).isNotNull().isEqualTo(csvParser);
  }

  @Test
  void shouldGetParserForXlsxFile() {
    when(multipartFile.getOriginalFilename()).thenReturn("data.xlsx");

    var parser = assertDoesNotThrow(() -> sut.getParser(multipartFile));

    assertThat(parser).isNotNull().isEqualTo(xlsxParser);
  }

  @Test
  void shouldNotGetParserForUnsupportedFileType() {
    when(multipartFile.getOriginalFilename()).thenReturn("data.pdf");

    var exception = assertThrows(
        UnsupportedOperationException.class,
        () -> sut.getParser(multipartFile)
    );

    assertEquals("Unsupported file type.", exception.getMessage());
  }

  @Test
  void shouldNotGetParserWithNullFileName() {
    when(multipartFile.getOriginalFilename()).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getParser(multipartFile)
    );
  }

  @Test
  void shouldNotGetParserWithEmptyFileName() {
    when(multipartFile.getOriginalFilename()).thenReturn("");

    assertThrows(
        IllegalArgumentException.class,
        () -> sut.getParser(multipartFile)
    );
  }

  @Test
  void shouldNotGetParserWhenTheListOfSupportedParsersIsNull() {
    sut.setTmsDatasetParsers(Collections.emptyList());

    when(multipartFile.getOriginalFilename()).thenReturn("data.csv");

    assertThrows(
        UnsupportedOperationException.class,
        () -> sut.getParser(multipartFile)
    );
  }
}
