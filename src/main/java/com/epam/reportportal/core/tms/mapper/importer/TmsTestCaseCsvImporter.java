package com.epam.reportportal.core.tms.mapper.importer;

import com.epam.reportportal.core.tms.dto.TmsStepRQ;
import com.epam.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.core.tms.dto.csv.TmsTestCaseCsvRow;
import com.epam.reportportal.core.tms.dto.csv.TmsTestCaseImportRowError;
import com.epam.reportportal.core.tms.dto.csv.TmsTestCaseImportRowWarning;
import com.epam.reportportal.core.tms.dto.csv.TmsTestCaseTemplate;
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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * CSV importer for TMS test cases with streaming support. Parses CSV files according to the TMS
 * import specification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmsTestCaseCsvImporter implements TmsTestCaseImporter {

  // CSV column names (case-insensitive matching)
  private static final String COL_TEMPLATE = "template";
  private static final String COL_SUMMARY = "summary";
  private static final String COL_DESCRIPTION = "description";
  private static final String COL_STATUS = "status";
  private static final String COL_PRIORITY = "priority";
  private static final String COL_TEST_TYPE = "test type";
  private static final String COL_LABELS = "labels";
  private static final String COL_COMPONENTS = "components";
  private static final String COL_VERSIONS = "versions";
  private static final String COL_REQUIREMENTS = "requirements";
  private static final String COL_PATH = "path";
  private static final String COL_TEST_STEPS = "test steps";
  private static final String COL_EXPECTED_RESULT = "expected result";
  private static final String COL_BUGS = "bugs (not imported)";

  // Label separator within cell
  private static final String LABEL_SEPARATOR = ";";

  // Path separator for folder hierarchy
  private static final String PATH_SEPARATOR = "/";

  @Override
  public TmsTestCaseImportFormat getSupportedFormat() {
    return TmsTestCaseImportFormat.CSV;
  }

  @Override
  public List<TmsTestCaseRQ> importFromFile(MultipartFile file) {
    try (var inputStream = file.getInputStream()) {
      var result = parseFromStream(inputStream, true);
      return result.rows().stream()
          .map(row -> convertToTestCaseRQ(row, true))
          .filter(Objects::nonNull)
          .toList();
    } catch (IOException e) {
      log.error("Failed to parse CSV file: {}", file.getOriginalFilename(), e);
      throw new IllegalArgumentException("Failed to parse CSV file: " + e.getMessage(), e);
    }
  }

  /**
   * Parses CSV from InputStream with streaming support.
   *
   * @param inputStream      the input stream to parse
   * @param rejectEmptySteps whether to reject Steps template rows with empty steps
   * @return parse result containing parsed rows, errors, and warnings
   */
  public CsvParseResult parseFromStream(InputStream inputStream, boolean rejectEmptySteps) {
    List<TmsTestCaseCsvRow> rows = new ArrayList<>();
    List<TmsTestCaseImportRowError> errors = new ArrayList<>();
    List<TmsTestCaseImportRowWarning> warnings = new ArrayList<>();
    int totalRows = 0;

    try (var reader = new BufferedReader(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        var csvParser = createCsvParser(reader)) {

      Map<String, Integer> headerMap = buildHeaderMap(csvParser);
      validateRequiredHeaders(headerMap);

      for (CSVRecord record : csvParser) {
        totalRows++;
        int rowNumber = (int) record.getRecordNumber() + 1; // +1 for header row

        try {
          var csvRow = parseRow(record, headerMap, rowNumber);
          var validationResult = validateRow(csvRow, rejectEmptySteps);

          if (validationResult.hasErrors()) {
            errors.addAll(validationResult.errors());
          } else {
            rows.add(csvRow);
            warnings.addAll(validationResult.warnings());
          }
        } catch (Exception e) {
          log.warn("Failed to parse row {}: {}", rowNumber, e.getMessage());
          errors.add(TmsTestCaseImportRowError.of(
              rowNumber,
              getValueSafe(record, headerMap, COL_SUMMARY),
              "Failed to parse row: " + e.getMessage()
          ));
        }
      }
    } catch (IOException e) {
      log.error("Failed to parse CSV stream", e);
      throw new IllegalArgumentException("Failed to parse CSV: " + e.getMessage(), e);
    }

    return new CsvParseResult(rows, errors, warnings, totalRows);
  }

  /**
   * Converts a parsed CSV row to TmsTestCaseRQ.
   *
   * @param row              the parsed CSV row
   * @param rejectEmptySteps whether to reject Steps template with empty steps
   * @return TmsTestCaseRQ or null if conversion fails
   */
  public TmsTestCaseRQ convertToTestCaseRQ(TmsTestCaseCsvRow row, boolean rejectEmptySteps) {
    var template = TmsTestCaseTemplate.fromString(row.getTemplate());
    if (template == null) {
      return null;
    }

    var testCaseRQ = TmsTestCaseRQ.builder()
        .name(row.getSummary())
        .description(row.getDescription())
        .priority(row.getPriority())
        .build();

    // Set manual scenario based on template type
    switch (template) {
      case TEXT -> testCaseRQ.setManualScenario(buildTextManualScenario(row));
      case STEPS -> {
        var stepsScenario = buildStepsManualScenario(row);
        if (rejectEmptySteps && (stepsScenario.getSteps() == null
            || stepsScenario.getSteps().isEmpty())) {
          return null;
        }
        testCaseRQ.setManualScenario(stepsScenario);
      }
    }

    return testCaseRQ;
  }

  /**
   * Extracts labels from a CSV row.
   *
   * @param row the parsed CSV row
   * @return list of label strings
   */
  public List<String> extractLabels(TmsTestCaseCsvRow row) {
    return row.getLabels() != null ? row.getLabels() : Collections.emptyList();
  }

  /**
   * Parses path string into folder hierarchy list.
   * Example: "folder1/folder2/folder3" -> ["folder1", "folder2", "folder3"]
   *
   * @param row the parsed CSV row
   * @return list of folder names representing the hierarchy (from root to leaf)
   */
  public List<String> extractPathHierarchy(TmsTestCaseCsvRow row) {
    if (row == null || StringUtils.isBlank(row.getPath())) {
      return Collections.emptyList();
    }
    return parsePath(row.getPath());
  }

  /**
   * Parses path string into folder hierarchy list.
   * Example: "folder1/folder2/folder3" -> ["folder1", "folder2", "folder3"]
   *
   * @param path the path string from CSV
   * @return list of folder names representing the hierarchy (from root to leaf)
   */
  public List<String> parsePath(String path) {
    if (StringUtils.isBlank(path)) {
      return Collections.emptyList();
    }

    return Arrays.stream(path.split(PATH_SEPARATOR))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .toList();
  }

  /**
   * Extracts requirements from a CSV row.
   *
   * @param row the parsed CSV row
   * @return requirements string or null
   */
  public String extractRequirements(TmsTestCaseCsvRow row) {
    return row.getRequirements();
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
    List<String> missingHeaders = new ArrayList<>();

    if (!headerMap.containsKey(COL_TEMPLATE)) {
      missingHeaders.add(COL_TEMPLATE);
    }
    if (!headerMap.containsKey(COL_SUMMARY)) {
      missingHeaders.add(COL_SUMMARY);
    }

    if (!missingHeaders.isEmpty()) {
      throw new IllegalArgumentException(
          "CSV file is missing required headers: " + String.join(", ", missingHeaders)
      );
    }
  }

  private TmsTestCaseCsvRow parseRow(CSVRecord record, Map<String, Integer> headerMap,
      int rowNumber) {
    return TmsTestCaseCsvRow.builder()
        .rowNumber(rowNumber)
        .template(getValueSafe(record, headerMap, COL_TEMPLATE))
        .summary(getValueSafe(record, headerMap, COL_SUMMARY))
        .description(getValueSafe(record, headerMap, COL_DESCRIPTION))
        .status(getValueSafe(record, headerMap, COL_STATUS))
        .priority(getValueSafe(record, headerMap, COL_PRIORITY))
        .testType(getValueSafe(record, headerMap, COL_TEST_TYPE))
        .labels(parseLabels(getValueSafe(record, headerMap, COL_LABELS)))
        .components(getValueSafe(record, headerMap, COL_COMPONENTS))
        .versions(getValueSafe(record, headerMap, COL_VERSIONS))
        .requirements(getValueSafe(record, headerMap, COL_REQUIREMENTS))
        .path(getValueSafe(record, headerMap, COL_PATH))
        .testSteps(getValueSafe(record, headerMap, COL_TEST_STEPS))
        .expectedResult(getValueSafe(record, headerMap, COL_EXPECTED_RESULT))
        .bugs(getValueSafe(record, headerMap, COL_BUGS))
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

  private List<String> parseLabels(String labelsCell) {
    if (StringUtils.isBlank(labelsCell)) {
      return Collections.emptyList();
    }
    return Arrays.stream(labelsCell.split(LABEL_SEPARATOR))
        .map(String::trim)
        .filter(StringUtils::isNotBlank)
        .distinct()
        .toList();
  }

  private RowValidationResult validateRow(TmsTestCaseCsvRow row, boolean rejectEmptySteps) {
    List<TmsTestCaseImportRowError> errors = new ArrayList<>();
    List<TmsTestCaseImportRowWarning> warnings = new ArrayList<>();

    if (StringUtils.isBlank(row.getTemplate())) {
      errors.add(TmsTestCaseImportRowError.of(
          row.getRowNumber(),
          row.getSummary(),
          "Missing required field: template"
      ));
    } else if (!TmsTestCaseTemplate.isValid(row.getTemplate())) {
      errors.add(TmsTestCaseImportRowError.of(
          row.getRowNumber(),
          row.getSummary(),
          "Invalid template value: '" + row.getTemplate()
              + "'. Allowed values: Text, Steps"
      ));
    }

    if (StringUtils.isBlank(row.getSummary())) {
      errors.add(TmsTestCaseImportRowError.of(
          row.getRowNumber(),
          null,
          "Missing required field: summary"
      ));
    }

    if (TmsTestCaseTemplate.STEPS.getValue().equalsIgnoreCase(row.getTemplate())) {
      if (rejectEmptySteps
          && StringUtils.isBlank(row.getTestSteps())
          && StringUtils.isBlank(row.getExpectedResult())) {
        errors.add(TmsTestCaseImportRowError.of(
            row.getRowNumber(),
            row.getSummary(),
            "Steps template requires at least 'test steps' or 'expected result'"
        ));
      }
    }

    if (StringUtils.isNotBlank(row.getStatus())) {
      warnings.add(TmsTestCaseImportRowWarning.of(
          row.getRowNumber(),
          row.getSummary(),
          "status",
          "Field 'status' is ignored in this version"
      ));
    }

    if (StringUtils.isNotBlank(row.getTestType())) {
      warnings.add(TmsTestCaseImportRowWarning.of(
          row.getRowNumber(),
          row.getSummary(),
          "test type",
          "Field 'test type' is ignored in this version"
      ));
    }

    if (StringUtils.isNotBlank(row.getComponents())) {
      warnings.add(TmsTestCaseImportRowWarning.of(
          row.getRowNumber(),
          row.getSummary(),
          "components",
          "Field 'components' is ignored in this version"
      ));
    }

    if (StringUtils.isNotBlank(row.getVersions())) {
      warnings.add(TmsTestCaseImportRowWarning.of(
          row.getRowNumber(),
          row.getSummary(),
          "versions",
          "Field 'versions' is ignored in this version"
      ));
    }

    if (StringUtils.isNotBlank(row.getBugs())) {
      warnings.add(TmsTestCaseImportRowWarning.of(
          row.getRowNumber(),
          row.getSummary(),
          "bugs",
          "Field 'bugs' is ignored"
      ));
    }

    return new RowValidationResult(errors, warnings);
  }

  private TmsTextManualScenarioRQ buildTextManualScenario(TmsTestCaseCsvRow row) {
    return TmsTextManualScenarioRQ.builder()
        .instructions(row.getTestSteps())
        .expectedResult(row.getExpectedResult())
        .linkToRequirements(row.getRequirements())
        .build();
  }

  private TmsStepsManualScenarioRQ buildStepsManualScenario(TmsTestCaseCsvRow row) {
    List<TmsStepRQ> steps = parseSteps(row.getTestSteps(), row.getExpectedResult());

    return TmsStepsManualScenarioRQ.builder()
        .steps(steps)
        .linkToRequirements(row.getRequirements())
        .build();
  }

  private List<TmsStepRQ> parseSteps(String testSteps, String expectedResults) {
    if (StringUtils.isBlank(testSteps) && StringUtils.isBlank(expectedResults)) {
      return Collections.emptyList();
    }

    List<String> stepLines = splitByNewlines(testSteps);
    List<String> expectedLines = splitByNewlines(expectedResults);

    List<TmsStepRQ> steps = new ArrayList<>();
    int maxSize = Math.max(stepLines.size(), expectedLines.size());

    if (stepLines.isEmpty() && !expectedLines.isEmpty()) {
      stepLines = Collections.nCopies(expectedLines.size(), "");
    }

    for (int i = 0; i < maxSize && i < stepLines.size(); i++) {
      String instruction = i < stepLines.size() ? stepLines.get(i) : "";
      String expected = i < expectedLines.size() ? expectedLines.get(i) : null;

      if (StringUtils.isBlank(instruction) && StringUtils.isBlank(expected)) {
        continue;
      }

      steps.add(TmsStepRQ.builder()
          .instructions(StringUtils.isNotBlank(instruction) ? instruction : null)
          .expectedResult(StringUtils.isNotBlank(expected) ? expected : null)
          .build());
    }

    return steps;
  }

  private List<String> splitByNewlines(String text) {
    if (StringUtils.isBlank(text)) {
      return Collections.emptyList();
    }
    return Arrays.stream(text.split("\\r?\\n"))
        .map(String::trim)
        .toList();
  }

  /**
   * Result of CSV parsing operation.
   */
  public record CsvParseResult(
      List<TmsTestCaseCsvRow> rows,
      List<TmsTestCaseImportRowError> errors,
      List<TmsTestCaseImportRowWarning> warnings,
      int totalRows
  ) {

    public boolean hasErrors() {
      return !errors.isEmpty();
    }

    public boolean hasWarnings() {
      return !warnings.isEmpty();
    }
  }

  /**
   * Result of single row validation.
   */
  private record RowValidationResult(
      List<TmsTestCaseImportRowError> errors,
      List<TmsTestCaseImportRowWarning> warnings
  ) {

    public boolean hasErrors() {
      return !errors.isEmpty();
    }
  }
}