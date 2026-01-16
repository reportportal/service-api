package com.epam.reportportal.core.tms.dto.csv;

import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of CSV import operation containing imported test cases and any errors/warnings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseImportResult {

  @Builder.Default
  private List<TmsTestCaseRS> importedTestCases = new ArrayList<>();

  private int totalRows;

  @Builder.Default
  private int successCount = 0;

  @Builder.Default
  private int failedCount = 0;

  @Builder.Default
  private int warningCount = 0;

  @Builder.Default
  private List<TmsTestCaseImportRowError> errors = new ArrayList<>();

  @Builder.Default
  private List<TmsTestCaseImportRowWarning> warnings = new ArrayList<>();

  public void addImportedTestCase(TmsTestCaseRS testCase) {
    importedTestCases.add(testCase);
    successCount++;
  }

  public void addError(TmsTestCaseImportRowError error) {
    errors.add(error);
    failedCount++;
  }

  public void addWarning(TmsTestCaseImportRowWarning warning) {
    warnings.add(warning);
    warningCount++;
  }

  public void addWarnings(List<TmsTestCaseImportRowWarning> newWarnings) {
    warnings.addAll(newWarnings);
    warningCount += newWarnings.size();
  }
}
