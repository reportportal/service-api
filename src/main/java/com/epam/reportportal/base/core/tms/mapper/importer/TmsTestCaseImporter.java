package com.epam.reportportal.base.core.tms.mapper.importer;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportParseResult;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportFormat;
import java.io.InputStream;

/**
 * Interface for importing test cases from various file formats. Implementations are responsible
 * ONLY for parsing - no validation or persistence logic.
 */
public interface TmsTestCaseImporter {

  /**
   * Returns the format this importer supports.
   */
  TmsTestCaseImportFormat getSupportedFormat();

  /**
   * Parses input stream and returns parsed test cases.
   *
   * @param inputStream the input stream to parse
   * @return parse result containing list of ImportTmsTestCaseRQ
   * @throws IllegalArgumentException if parsing fails
   */
  TmsTestCaseImportParseResult parse(InputStream inputStream);
}
