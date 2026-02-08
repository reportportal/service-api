package com.epam.reportportal.core.tms.mapper.importer;

import com.epam.reportportal.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.core.tms.dto.TmsTestCaseImportFormat;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseImportParseResult;
import com.epam.reportportal.core.tms.dto.TmsTestCaseAttributeImportRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseImportRQ;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * CSV importer for TMS test cases. Responsible ONLY for parsing CSV files into ImportTmsTestCaseRQ
 * objects.
 */
@Slf4j
@Component
public class TmsTestCaseCsvImporter implements TmsTestCaseImporter {

  // CSV column names (case-insensitive matching)
  private static final String COL_SUMMARY = "summary";
  private static final String COL_DESCRIPTION = "description";
  private static final String COL_PRIORITY = "priority";
  private static final String COL_LABELS = "labels";
  private static final String COL_PATH = "path";
  private static final String COL_TEST_STEPS = "test steps";
  private static final String COL_EXPECTED_RESULT = "expected result";
  private static final String COL_REQUIREMENTS = "requirements";
  private static final String COL_PRECONDITIONS = "preconditions";

  // Separators
  private static final String LABEL_SEPARATOR = ";";
  private static final String PATH_SEPARATOR = "/";

  @Override
  public TmsTestCaseImportFormat getSupportedFormat() {
    return TmsTestCaseImportFormat.CSV;
  }

  @Override
  public TmsTestCaseImportParseResult parse(InputStream inputStream) {
    List<TmsTestCaseImportRQ> testCases = new ArrayList<>();
    int totalRows = 0;

    try (var bomInputStream = BOMInputStream.builder()
        .setInputStream(inputStream)
        .get();
        var reader = new BufferedReader(
            new InputStreamReader(bomInputStream, StandardCharsets.UTF_8));
        var csvParser = createCsvParser(reader)) {

      var headerMap = buildHeaderMap(csvParser);
      validateRequiredHeaders(headerMap);

      for (var record : csvParser) {
        totalRows++;
        int rowNumber = (int) record.getRecordNumber() + 1;

        try {
          var testCase = parseRow(record, headerMap);
          if (testCase != null) {
            testCases.add(testCase);
          }
        } catch (Exception e) {
          log.warn("Failed to parse row {}: {}", rowNumber, e.getMessage());
          throw new IllegalArgumentException(
              "Failed to parse row " + rowNumber + ": " + e.getMessage(), e);
        }
      }
    } catch (IOException e) {
      log.error("Failed to parse CSV stream", e);
      throw new IllegalArgumentException("Failed to parse CSV: " + e.getMessage(), e);
    }

    return TmsTestCaseImportParseResult.builder()
        .testCases(testCases)
        .totalRows(totalRows)
        .build();
  }

  private CSVParser createCsvParser(BufferedReader reader) throws IOException {
    return new CSVParser(reader, CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreHeaderCase(true)
        .setTrim(true)
        .setIgnoreEmptyLines(true)
        .build());
  }

  private Map<String, Integer> buildHeaderMap(CSVParser parser) {
    return parser.getHeaderMap().entrySet().stream()
        .collect(Collectors.toMap(
            e -> e.getKey().toLowerCase().trim(),
            Map.Entry::getValue,
            (v1, v2) -> v1
        ));
  }

  private void validateRequiredHeaders(Map<String, Integer> headerMap) {
    if (!headerMap.containsKey(COL_SUMMARY)) {
      throw new IllegalArgumentException(
          "CSV file is missing required header: " + COL_SUMMARY);
    }
  }

  private TmsTestCaseImportRQ parseRow(CSVRecord record, Map<String, Integer> headerMap) {
    var summary = getValueSafe(record, headerMap, COL_SUMMARY);

    // Skip rows without summary
    if (StringUtils.isBlank(summary)) {
      return null;
    }

    return TmsTestCaseImportRQ
        .builder()
        .name(summary)
        .description(getValueSafe(record, headerMap, COL_DESCRIPTION))
        .priority(getValueSafe(record, headerMap, COL_PRIORITY))
        .folderPath(parsePath(getValueSafe(record, headerMap, COL_PATH)))
        .attributes(parseLabels(getValueSafe(record, headerMap, COL_LABELS)))
        .manualScenario(buildManualScenario(
            getValueSafe(record, headerMap, COL_TEST_STEPS),
            getValueSafe(record, headerMap, COL_EXPECTED_RESULT),
            getValueSafe(record, headerMap, COL_REQUIREMENTS),
            getValueSafe(record, headerMap, COL_PRECONDITIONS)
        ))
        .build();
  }

  private String getValueSafe(CSVRecord record, Map<String, Integer> headerMap, String column) {
    Integer index = headerMap.get(column.toLowerCase());
    if (index == null || index >= record.size()) {
      return null;
    }
    String value = record.get(index);
    return StringUtils.isBlank(value) ? null : value.trim();
  }

  private List<String> parsePath(String path) {
    if (StringUtils.isBlank(path)) {
      return Collections.emptyList();
    }
    return Arrays.stream(path.split(PATH_SEPARATOR))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .toList();
  }

  private List<TmsTestCaseAttributeImportRQ> parseLabels(String labelsCell) {
    if (StringUtils.isBlank(labelsCell)) {
      return Collections.emptyList();
    }
    return Arrays.stream(labelsCell.split(LABEL_SEPARATOR))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .distinct()
        .map(key -> TmsTestCaseAttributeImportRQ.builder().key(key).build())
        .toList();
  }

  private TmsTextManualScenarioRQ buildManualScenario(String testSteps, String expectedResult,
      String requirements, String preconditions) {
    var preconditionsValue = preconditions != null ? preconditions : "";

    return TmsTextManualScenarioRQ.builder()
        .instructions(testSteps)
        .expectedResult(expectedResult)
        .linkToRequirements(requirements)
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .preconditions(TmsManualScenarioPreconditionsRQ.builder()
            .value(preconditionsValue)
            .build())
        .build();
  }
}
